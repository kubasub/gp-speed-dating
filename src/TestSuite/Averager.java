package TestSuite;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Scanner;

public class Averager {

  private static final String statFileName = "out2";

  public static void average( String directory, boolean sizeIncluded ) {
    // Program Sizes
    HashMap<Integer, ArrayList<Double>> meanGenSize = new HashMap<>();
    HashMap<Integer, ArrayList<Double>> meanRunSize = new HashMap<>();
    HashMap<Integer, ArrayList<Double>> bestGenSize = new HashMap<>();
    HashMap<Integer, ArrayList<Double>> bestRunSize = new HashMap<>();

    // Individual fitness
    HashMap<Integer, ArrayList<Double>> meanGenPopulationFitness = new HashMap<>();
    HashMap<Integer, ArrayList<Double>> bestGenPopulationFitness = new HashMap<>();
    HashMap<Integer, ArrayList<Double>> bestRunPopulationFitness = new HashMap<>();

    // Read through each file and collect data
    File[] files = new File( directory ).listFiles();
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
              meanGenPopulationFitness.put( genNumber, new ArrayList<Double>() );
            }
            if ( !bestGenPopulationFitness.containsKey( genNumber ) ) {
              bestGenPopulationFitness.put( genNumber, new ArrayList<Double>() );
            }
            if ( !bestRunPopulationFitness.containsKey( genNumber ) ) {
              bestRunPopulationFitness.put( genNumber, new ArrayList<Double>() );
            }

            // Handle program sizes when included
            if ( sizeIncluded ) {
              // Make sure we have somewhere to put the values
              if ( !meanGenSize.containsKey( genNumber ) ) {
                meanGenSize.put( genNumber, new ArrayList<Double>() );
              }
              if ( !meanRunSize.containsKey( genNumber ) ) {
                meanRunSize.put( genNumber, new ArrayList<Double>() );
              }
              if ( !bestGenSize.containsKey( genNumber ) ) {
                bestGenSize.put( genNumber, new ArrayList<Double>() );
              }
              if ( !bestRunSize.containsKey( genNumber ) ) {
                bestRunSize.put( genNumber, new ArrayList<Double>() );
              }

              // Save them
              meanGenSize.get( genNumber ).add( Double.parseDouble( line[1] ) );
              meanRunSize.get( genNumber ).add( Double.parseDouble( line[2] ) );
              bestGenSize.get( genNumber ).add( Double.parseDouble( line[3] ) );
              bestRunSize.get( genNumber ).add( Double.parseDouble( line[4] ) );

              meanGenPopulationFitness.get( genNumber ).add( Double.parseDouble( line[5] ) );
              bestGenPopulationFitness.get( genNumber ).add( Double.parseDouble( line[6] ) );
              bestRunPopulationFitness.get( genNumber ).add( Double.parseDouble( line[7] ) );
            }
            else {
              meanGenPopulationFitness.get( genNumber ).add( Double.parseDouble( line[1] ) );
              bestGenPopulationFitness.get( genNumber ).add( Double.parseDouble( line[2] ) );
              bestRunPopulationFitness.get( genNumber ).add( Double.parseDouble( line[3] ) );
            }
          }
        }
        catch ( FileNotFoundException ex ) {
          System.out.println( "Averager: File not found - " + directory );
        }
        catch ( Exception ex ) {
          System.out.println( "Averager: File read error - " + directory );
        }
      }
    }

    // Print the results to a file
    LinkedHashMap<String, double[]> averages = new LinkedHashMap<>();
    if ( sizeIncluded ) {
      averages.put( "Mean Size For Generation", average( meanGenSize ) );
      averages.put( "Mean Size For Run", average( meanRunSize ) );
      averages.put( "Best Size For Generation", average( bestGenSize ) );
      averages.put( "Best Size For Run", average( bestRunSize ) );
    }

    averages.put( "Mean Fitness For Generation", average( meanGenPopulationFitness ) );
    averages.put( "Best Fitness For Generation", average( bestGenPopulationFitness ) );
    averages.put( "Best Fitness For Run", average( bestRunPopulationFitness ) );

    FileIO.saveAverages( averages, directory );
  }

  private static double[] average( HashMap<Integer, ArrayList<Double>> vals ) {
    double[] averages = new double[ vals.size() ];

    // Process by generation
    for ( Integer gen : vals.keySet() ) {
      // Add up values from each run for the current generation
      for ( Double val : vals.get( gen ) ) {
        averages[gen] += val;
      }

      // Average values
      averages[gen] = averages[gen] / vals.get( gen ).size();
    }

    return averages;
  }
}
