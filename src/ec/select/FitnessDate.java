/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.select;

import ec.EvolutionState;
import ec.Individual;

/**
 * This class represents a "Date" that two individuals can go on where a match
 * is based on similarity in fitness.
 */
public class FitnessDate implements Date {

    @Override
    public double match(Individual first, Individual second, EvolutionState state) {
        return Math.abs(first.fitness.fitness() - second.fitness.fitness());
    }
}
