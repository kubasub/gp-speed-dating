package TestSuite;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Scanner;

public class Averager {

  private static final String statFileName = "out2";

  public static void average( String directory, TestCase test, boolean sizeIncluded ) {
    // Program Sizes
    HashMap<Integer, HashMap<Integer, Double>> meanGenSize = new HashMap<>();
    HashMap<Integer, HashMap<Integer, Double>> meanRunSize = new HashMap<>();
    HashMap<Integer, HashMap<Integer, Double>> bestGenSize = new HashMap<>();
    HashMap<Integer, HashMap<Integer, Double>> bestRunSize = new HashMap<>();

    // Individual fitness
    HashMap<Integer, HashMap<Integer, Double>> meanGenPopulationFitness = new HashMap<>();
    HashMap<Integer, HashMap<Integer, Double>> bestGenPopulationFitness = new HashMap<>();
    HashMap<Integer, HashMap<Integer, Double>> bestRunPopulationFitness = new HashMap<>();

    // Read through each file and collect data
    File[] files = new File( directory ).listFiles();
    int jobNumber = 0;
    for ( File file : files ) {
      if ( file.getName().contains( statFileName ) ) {

        // Process the stat file
        try ( Scanner fileScanner = new Scanner( file ) ) {
          while ( fileScanner.hasNextLine() ) {
            // Read line
            String[] line = fileScanner.nextLine().split( " " );
            int genNumber = Integer.parseInt( line[0] );

            // Make sure we have somewhere to put the values
            if ( !meanGenPopulationFitness.containsKey( genNumber ) ) {
              meanGenPopulationFitness.put( genNumber, new HashMap<Integer, Double>() );
            }
            if ( !bestGenPopulationFitness.containsKey( genNumber ) ) {
              bestGenPopulationFitness.put( genNumber, new HashMap<Integer, Double>() );
            }
            if ( !bestRunPopulationFitness.containsKey( genNumber ) ) {
              bestRunPopulationFitness.put( genNumber, new HashMap<Integer, Double>() );
            }

            // Handle program sizes when included
            if ( sizeIncluded ) {
              // Make sure we have somewhere to put the values
              if ( !meanGenSize.containsKey( genNumber ) ) {
                meanGenSize.put( genNumber, new HashMap<Integer, Double>() );
              }
              if ( !meanRunSize.containsKey( genNumber ) ) {
                meanRunSize.put( genNumber, new HashMap<Integer, Double>() );
              }
              if ( !bestGenSize.containsKey( genNumber ) ) {
                bestGenSize.put( genNumber, new HashMap<Integer, Double>() );
              }
              if ( !bestRunSize.containsKey( genNumber ) ) {
                bestRunSize.put( genNumber, new HashMap<Integer, Double>() );
              }

              // Save them
              meanGenSize.get( genNumber ).put( jobNumber, Double.parseDouble( line[1] ) );
              meanRunSize.get( genNumber ).put( jobNumber, Double.parseDouble( line[2] ) );
              bestGenSize.get( genNumber ).put( jobNumber, Double.parseDouble( line[3] ) );
              bestRunSize.get( genNumber ).put( jobNumber, Double.parseDouble( line[4] ) );

              meanGenPopulationFitness.get( genNumber ).put( jobNumber, Double.parseDouble( line[5] ) );
              bestGenPopulationFitness.get( genNumber ).put( jobNumber, Double.parseDouble( line[6] ) );
              bestRunPopulationFitness.get( genNumber ).put( jobNumber, Double.parseDouble( line[7] ) );
            }
            else {
              meanGenPopulationFitness.get( genNumber ).put( jobNumber, Double.parseDouble( line[1] ) );
              bestGenPopulationFitness.get( genNumber ).put( jobNumber, Double.parseDouble( line[2] ) );
              bestRunPopulationFitness.get( genNumber ).put( jobNumber, Double.parseDouble( line[3] ) );
            }
          }
        }
        catch ( FileNotFoundException ex ) {
          System.out.println( "Averager: File not found - " + directory );
        }
        catch ( Exception ex ) {
          System.out.println( "Averager: File read error - " + directory );
        }
        jobNumber++;
      }
    }

    // Print the results to a file
    LinkedHashMap<String, double[]> averages = new LinkedHashMap<>();
    if ( sizeIncluded ) {
      averages.put( "Mean Size For Generation", average( meanGenSize, jobNumber ) );
      averages.put( "Mean Size For Run", average( meanRunSize, jobNumber ) );
      averages.put( "Best Size For Generation", average( bestGenSize, jobNumber ) );
      averages.put( "Best Size For Run", average( bestRunSize, jobNumber ) );
    }

    averages.put( "Mean Fitness For Generation", average( meanGenPopulationFitness, jobNumber ) );
    averages.put( "Best Fitness For Generation", average( bestGenPopulationFitness, jobNumber ) );
    averages.put( "Best Fitness For Run", average( bestRunPopulationFitness, jobNumber ) );

    FileIO.saveAverages( averages, test, directory );
  }

  private static double[] average( HashMap<Integer, HashMap<Integer, Double>> vals, int jobs ) {
    double[] averages = new double[ vals.size() ];

    HashMap<Integer, Double> lastValue = new HashMap<>();

    // Process by generation
    for ( Integer gen : vals.keySet() ) {

      // Add up values from each run for the current generation
      for ( int i = 0; i < jobs; i++ ) {
        if ( vals.get( gen ).containsKey( i ) ) {
          averages[gen] += vals.get( gen ).get( i );
          lastValue.put( i, vals.get( gen ).get( i ) );
        }
        else {
          // If the job is finished add its last value
          averages[gen] += lastValue.get( i );
        }
      }

      // Average values
      averages[gen] = averages[gen] / jobs;
    }

    return averages;
  }
}
