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
    
    /**
     * A convenience method that gets the tournament size from the parameters
     * database. It ensures that a valid value has been specified.
     * 
     * @return the tournament size for determining the first parent
     */
    private int loadTournamentSize(final EvolutionState state, final Parameter base) {
        Parameter def = defaultBase();
        int size = state.parameters.getInt(base.push(P_TOUR_SIZE), def.push(P_TOUR_SIZE), 1);
        
        if(size < 1) {
            state.output.fatal("SpeedDating tournament size ("+P_TOUR_SIZE+") must be >= 1.", base.push(P_TOUR_SIZE), def.push(P_TOUR_SIZE));
        }
        
        return size;
    }
    
    /**
     * A convenience method that gets the dating size from the parameters
     * database. It ensures that a valid value has been specified.
     * 
     * @return the number of individuals the first parent will go on a date with
     */
    private int loadDatingSize(final EvolutionState state, final Parameter base) {
        Parameter def = defaultBase();
        int size = state.parameters.getInt(base.push(P_DATE_SIZE), def.push(P_DATE_SIZE), 1);
        
        if(size < 1) {
            state.output.fatal("SpeedDating dating size ("+P_DATE_SIZE+") must be >= 1.", base.push(P_DATE_SIZE), def.push(P_DATE_SIZE));
        }
        
        return size;
    }
    
    /**
     * A convenience method that gets dating method from the parameters
     * database. It ensures that a valid class has been specified.
     * 
     * @return the dating method
     */
    private Date setMatchType(EvolutionState state, Parameter base) {
        Parameter def = defaultBase();
        Date type = (Date)state.parameters.getInstanceForParameterEq(base.push(P_MATCH_TYPE), def.push(P_MATCH_TYPE), Date.class);
        
        if (type == null) {
            state.output.fatal("SpeedDating match type was not found.", base.push(P_MATCH_TYPE), def.push(P_MATCH_TYPE));
        }
        
        return type;
    }
    
    /**
     * A constructor-type method that is used by ECJ. Loads the parameters
     * relevant to the SpeedDateSelection.
     */
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

        this.tournamentSize = loadTournamentSize(state, base);
        this.datingSize = loadDatingSize(state, base);
        this.date = setMatchType(state, base);
    }
    
    /**
     * Returns whether the first parent has been selected.
     */
    private boolean isParent1Set() {
        return firstParent != -1;
    }
    
    /**
     * Resets this selection method to the point where the first parent has not
     * been chosen.
     */
    private void resetSpeedDate() {
        firstParent = -1;
    }
    
    
    /**
     * Produces the index of a randomly chosen individual from the population.
     */
    private int getRandomIndividual(int subpopulationSize, final EvolutionState state, final int thread) {
        return state.random[thread].nextInt(subpopulationSize);
    }
    
    /**
     * Gets a list of individuals that will be matched with the first parent
     * from the subpopulation. Ensures that the first parent does not go on a
     * date with itself.
     * 
     * @return a list of indexes, representing the individuals to be dated
     */
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
    
    /**
     * Matches each candidate individual with the first in order to determine
     * which candidate best matches the first parent.
     * 
     * @return the individual which best matched the first parent
     */
    private int speedDate(int parent1, int[] candidates, final int subpopulation, final EvolutionState state) {
        Individual[] inds = state.population.subpops[subpopulation].individuals;
        
        double bestScore = Double.MAX_VALUE;
        int bestCandidate = -1;
        
        for(int candidate : candidates) {
            // Get the score from the date
            double score = this.date.match(inds[parent1], inds[candidate], state);
            if(score < bestScore) {
                bestScore = score;
                bestCandidate = candidate;
            }
            if(bestScore == 0) break;
        }
        return bestCandidate;
    }
    
    /**
     * Performs a tournament selection to determine the first parent. This is
     * effectively a wrapper for the TournameSelection class.
     * 
     * @return the index of the tournament-selected individual
     */
    private int tournament(final int subpopulation, final EvolutionState state, final int thread) {
        TournamentSelection tournament = new TournamentSelection();
        tournament.size = this.tournamentSize;
        tournament.pickWorst = false;
        return tournament.produce(subpopulation, state, thread);
    }
    
    /**
     * This method is the entry point for the speed-dating algorithm. The first
     * time it is entered, the standard tournament selection is ran to get the
     * first parent. The second time, a matching method is ran, comparing the
     * first parent with potential candidate individuals. The the second parent
     * for the crossover is the candidate which best matches the first parent.
     * 
     * @return a index of the individual to be used with crossover
     */
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
