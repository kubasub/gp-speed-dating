package TestSuite;

public class TestCase {

  public String outputFolder;
  public String parameterFile;
  public int runs;
  public int currentTest = 0;

  public TestCase( String output, String parameters, int runs ) {
    this.outputFolder = output;
    this.parameterFile = parameters;
    this.runs = runs;
  }

  public String getOutputFolder() {
    if ( currentTest == 0 ) {
      return this.outputFolder;
    }
    else if ( currentTest == 1 ) {
      return this.outputFolder + "FitnessDate";
    }
    else {
      return this.outputFolder + "CrossOverDate";
    }
  }

  public String[] getCommandLine() {
    if (currentTest == 0 ) {
      return new String[]{ "-file", parameterFile, "-p", "jobs=" + runs };
    }
    else if (currentTest == 1) {
      return new String[]{ "-file", parameterFile, "-p", "jobs=" + runs,
        "-p", "gp.koza.xover.source.0=ec.select.SpeedDateSelection",
        "-p", "select.speed-date.tournament-size=7",
        "-p", "select.speed-date.date-size=7",
        "-p", "select.speed-date.match-type=ec.select.FitnessDate"
      };
    }
    else {
      return new String[]{ "-file", parameterFile, "-p", "jobs=" + runs,
        "-p", "gp.koza.xover.source.0=ec.select.SpeedDateSelection",
        "-p", "select.speed-date.tournament-size=7",
        "-p", "select.speed-date.date-size=7",
        "-p", "select.speed-date.match-type=ec.select.CrossoverDate"
      };
    }
  }
}
