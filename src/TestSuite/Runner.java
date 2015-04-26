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
      for ( int i = 0; i < 3; i++ ) {
        Evolve.main( test.getCommandLine() );

        // Get the current time and make a folder
        Calendar cal = Calendar.getInstance();
        cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat( "HH-mm-ss" );
        String currentDirectory = test.getOutputFolder() + "-" + sdf.format( cal.getTime() );
        boolean success = ( new File( currentDirectory ) ).mkdirs();
        if ( !success ) {
          System.out.println( "Could not make folder: " + currentDirectory );
          continue; // Try the next test
        }

        // Run each test and copy the output data
        for ( int j = 0; j < test.runs; j++ ) {
          copyStatFiles( j, currentDirectory );
        }

        // Finished test, process the data for charts etc
        Averager.average( currentDirectory, test, true );

        test.currentTest++;
      }
    }
  }

  private ArrayList<TestCase> getTestCases() {
    ArrayList<TestCase> tests = new ArrayList<>();

    tests.add( new TestCase(
            "C:\\Users\\42000\\Documents\\Brock\\Year 5\\COSC 4V82\\SpeedDatingTests\\RegressionSextic",
            "C:\\Users\\42000\\Documents\\Brock\\Year 5\\COSC 4V82\\gp-speed-dating\\src\\ec\\app\\regression\\sexticerc.params",
            10 ) );
    tests.add( new TestCase(
            "C:\\Users\\42000\\Documents\\Brock\\Year 5\\COSC 4V82\\SpeedDatingTests\\RegressionERC",
            "C:\\Users\\42000\\Documents\\Brock\\Year 5\\COSC 4V82\\gp-speed-dating\\src\\ec\\app\\regression\\erc.params",
            10 ) );
    tests.add( new TestCase(
            "C:\\Users\\42000\\Documents\\Brock\\Year 5\\COSC 4V82\\SpeedDatingTests\\Ant",
            "C:\\Users\\42000\\Documents\\Brock\\Year 5\\COSC 4V82\\gp-speed-dating\\src\\ec\\app\\ant\\ant.params",
            10 ) );

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
