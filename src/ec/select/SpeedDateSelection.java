/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.select;

import ec.*;
import ec.steadystate.*;
import ec.util.Parameter;

/**
 *
 */
public class SpeedDateSelection extends SelectionMethod implements SteadyStateBSourceForm {
    
    // Base namespace for this selection method
    public static final String P_SPEED_DATE = "speed-date";
    // Number of candidates for tournament selection
    public static final String P_TOUR_SIZE = "tournament-size";
    // Number of candidates for speed dating
    public static final String P_DATE_SIZE = "date-size";
    // The factor on which we want to match the parents
    public static final String P_MATCH_TYPE = "match-type";
    
    
    // Indicator whether the first parent has been determined
    private static int firstParent = -1;
    
    // Whether we match the first parent with the least alike date
    public boolean pickWorst;
    // Size for tournament selection
    public int tournamentSize;
    // Size for speed dating
    public int datingSize;
    // The type of matching being done
    public Date date;
  
    
    @Override
    public Parameter defaultBase() {
        return SelectDefaults.base().push(P_SPEED_DATE);
    }
    
    private int loadTournamentSize(final EvolutionState state, final Parameter base) {
        Parameter def = defaultBase();
        int size = state.parameters.getInt(base.push(P_TOUR_SIZE), def.push(P_TOUR_SIZE), 1);
        
        if(size < 1) {
            state.output.fatal("SpeedDating tournament size ("+P_TOUR_SIZE+") must be >= 1.", base.push(P_TOUR_SIZE), def.push(P_TOUR_SIZE));
        }
        
        return size;
    }
    
    private int loadDatingSize(final EvolutionState state, final Parameter base) {
        Parameter def = defaultBase();
        int size = state.parameters.getInt(base.push(P_DATE_SIZE), def.push(P_DATE_SIZE), 1);
        
        if(size < 1) {
            state.output.fatal("SpeedDating dating size ("+P_DATE_SIZE+") must be >= 1.", base.push(P_DATE_SIZE), def.push(P_DATE_SIZE));
        }
        
        return size;
    }
    
    private Date setMatchType(EvolutionState state, Parameter base) {
        Parameter def = defaultBase();
        Date type = (Date)state.parameters.getInstanceForParameterEq(base.push(P_MATCH_TYPE), def.push(P_MATCH_TYPE), Date.class);
        
        if (type == null) {
            state.output.fatal("SpeedDating match type was not found.", base.push(P_MATCH_TYPE), def.push(P_MATCH_TYPE));
        }
        
        return type;
    }
    
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

        this.tournamentSize = loadTournamentSize(state, base);
        this.datingSize = loadDatingSize(state, base);
        this.date = setMatchType(state, base);
    }
    
    private boolean isParent1Set() {
        return firstParent != -1;
    }
    
    private void resetSpeedDate() {
        firstParent = -1;
    }
    
    
    /**
     * Produces the index of a randomly chosen individual from the population.
     */
    private int getRandomIndividual(int subpopulationSize, final EvolutionState state, final int thread) {
        return state.random[thread].nextInt(subpopulationSize);
    }
    
    private int[] getIndividualsToDate(int parent1, final int subpopulation, final EvolutionState state, final int thread) {
        int [] candidates = new int[this.datingSize];
        int subpopulationSize = state.population.subpops[subpopulation].individuals.length;
        for(int i=0; i < this.datingSize; i++) {            
            int potentialCand;
            do {
                potentialCand = getRandomIndividual(subpopulationSize, state, thread);
            } while (potentialCand == parent1);
            
            candidates[i] = potentialCand;
        }
        return candidates;
    }
    
    private int speedDate(int parent1, int[] candidates, final int subpopulation, final EvolutionState state) {
        Individual[] inds = state.population.subpops[subpopulation].individuals;
        
        double bestScore = Double.MAX_VALUE;
        int bestCandidate = -1;
        
        for(int candidate : candidates) {
            double score = this.date.match(inds[parent1], inds[candidate], state);
            if(score < bestScore) {
                bestScore = score;
                bestCandidate = candidate;
            }
            if(bestScore == 0) break;
        }
        return bestCandidate;
    }
    
    private int tournament(final int subpopulation, final EvolutionState state, final int thread) {
        TournamentSelection tournament = new TournamentSelection();
        tournament.size = this.tournamentSize;
        tournament.pickWorst = false;
        return tournament.produce(subpopulation, state, thread);
    }

    @Override
    public int produce(final int subpopulation, final EvolutionState state, final int thread) {
        int individual;
        if(!isParent1Set()) {
            individual = tournament(subpopulation, state, thread);
            firstParent = individual;
        } else {
            int[] candidates = getIndividualsToDate(firstParent, subpopulation, state, thread);
            individual = speedDate(firstParent, candidates, subpopulation, state);
            resetSpeedDate();
        }
        return individual;
    }
    
    /**
     * Overridden for steady state evolution.
     */
    @Override
    public void individualReplaced(SteadyStateEvolutionState state, int subpopulation, int thread, int individual) { }

    /**
     * Overridden for steady state evolution.
     */
    @Override
    public void sourcesAreProperForm(SteadyStateEvolutionState state) { }
}
