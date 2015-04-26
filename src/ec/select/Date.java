package ec.select;

import ec.EvolutionState;
import ec.Individual;

/**
 * Represents a "date" that two individuals can go on using the
 * SpeedDateSelection selection method. This interface allows speed-date
 * selection to "match" individuals using any sort of metric.
 */
public interface Date {
    /**
     * Takes two individuals and rates them based on how well they match. The
     * lower the result, the more the two individuals "match".
     * 
     * @param first  the first person on the date
     * @param second the second person on the date
     * @return       how similar the two individuals are
     */
    public double match(Individual first, Individual second, EvolutionState state);
}
