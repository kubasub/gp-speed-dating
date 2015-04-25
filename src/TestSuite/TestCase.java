package TestSuite;

public class TestCase {

  public String outputFolder;
  public String parameterFile;
  public int runs;
  public boolean dateTest;

  public TestCase( String output, String parameters, int runs, boolean date ) {
    this.outputFolder = output;
    this.parameterFile = parameters;
    this.runs = runs;
    this.dateTest = date;
    
    if (this.dateTest){
     this.outputFolder += "SpeedDate"; 
    }  
  }

  public String[] getCommandLine() {
    if ( !this.dateTest ) {
      return new String[]{ "-file", parameterFile, "-p", "jobs=" + runs };
    }
    else {
      return new String[]{ "-file", parameterFile, "-p", "jobs=" + runs,
        "-p", "gp.koza.xover.source.0=ec.select.SpeedDateSelection",
        "-p", "select.speed-date.tournament-size=7",
        "-p", "select.speed-date.date-size=7",
        "-p", "select.speed-date.match-type=ec.select.FitnessDate"
      };
    }
  }
}
