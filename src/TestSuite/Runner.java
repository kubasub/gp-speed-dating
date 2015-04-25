package TestSuite;

import ec.Evolve;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Runner {

  public Runner() {
    ArrayList<TestCase> tests = this.getTestCases();

    // Run each test
    for ( TestCase test : tests ) {
      Evolve.main( new String[]{
        "-file",
        test.parameterFile
      } );

      // Get the current time and make a folder
      Calendar cal = Calendar.getInstance();
      cal.getTime();
      SimpleDateFormat sdf = new SimpleDateFormat( "HH-mm-ss" );
      String currentDirectory = test.outputFolder + "-" + sdf.format( cal.getTime() );
      boolean success = ( new File( currentDirectory ) ).mkdirs();
      if ( !success ) {
        System.out.println( "Could not make folder: " + currentDirectory );
        continue; // Try the next test
      }

      // Run each test and copy the output data
      for ( int i = 0; i < test.runs; i++ ) {
        copyStatFiles( i, currentDirectory );
      }

      // Finished test, process the data for charts etc
      Averager.average( currentDirectory, true );
    }
  }

  private ArrayList<TestCase> getTestCases() {
    ArrayList<TestCase> tests = new ArrayList<>();
    
    return tests;
  }

  private void copyStatFiles( int run, String folder ) {
    File source = new File( "./job." + run + ".out.stat" );
    File dest = new File( folder + "/job." + run + ".out.stat" );
    try {
      FileIO.copyFile( source, dest );
    }
    catch ( IOException e ) {
      e.printStackTrace();
    }

    File source2 = new File( "./job." + run + ".out2.stat" );
    File dest2 = new File( folder + "/job." + run + ".out2.stat" );
    try {
      FileIO.copyFile( source2, dest2 );
    }
    catch ( IOException e ) {
      e.printStackTrace();
    }
  }

  public static void main( String args[] ) {
    Runner r = new Runner();
  }
}
