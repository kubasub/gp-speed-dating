package TestSuite;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileIO {

  public static void copyFile( File sourceFile, File destFile )
          throws IOException {
    if ( !sourceFile.exists() ) {
      return;
    }
    if ( !destFile.exists() ) {
      destFile.createNewFile();
    }

    FileChannel source = new FileInputStream( sourceFile ).getChannel();
    FileChannel destination = new FileOutputStream( destFile ).getChannel();
    if ( destination != null && source != null ) {
      destination.transferFrom( source, 0, source.size() );
    }
    if ( source != null ) {
      source.close();
    }
    if ( destination != null ) {
      destination.close();
    }
  }

  public static void saveAverages( LinkedHashMap<String, double[]> averages, String directory ) {
    // Get setup information
    String[] keyArray = averages.keySet().toArray( new String[ averages.size() ] );
    int generations = averages.get( keyArray[0] ).length;

    // Create the file
    File output = new File( directory + "\\averagedResults.csv" );
    try ( Writer writer = new BufferedWriter( new OutputStreamWriter(
            new FileOutputStream( output ), "utf-8" ) ) ) {
      writer.write( "Generation, " + join( keyArray ) );

      // Print each generation
      for ( int i = 0; i < generations; i++ ) {
        writer.write( join( i, keyArray, averages ) );
      }
    }
    catch ( UnsupportedEncodingException ex ) {
      Logger.getLogger( FileIO.class.getName() ).log( Level.SEVERE, null, ex );
    }
    catch ( FileNotFoundException ex ) {
      Logger.getLogger( FileIO.class.getName() ).log( Level.SEVERE, null, ex );
    }
    catch ( IOException ex ) {
      Logger.getLogger( FileIO.class.getName() ).log( Level.SEVERE, null, ex );
    }
  }

  private static String join( String[] arr ) {
    StringBuilder text = new StringBuilder();
    for ( int i = 0; i < arr.length; i++ ) {
      text.append( arr[i] );
      if ( i + 1 < arr.length ) {
        text.append( ", " );
      }
    }
    
    text.append( '\n');

    return text.toString();
  }

  private static String join( int generation, String[] keys, LinkedHashMap<String, double[]> averages ) {
    StringBuilder text = new StringBuilder();
    text.append( generation );
    text.append( ", " );

    for ( int i = 0; i < averages.size(); i++ ) {
      text.append( averages.get( keys[i] )[generation] );
      if ( i + 1 < averages.size() ) {
        text.append( ", " );
      }
    }
    
    text.append( '\n');

    return text.toString();
  }
}
