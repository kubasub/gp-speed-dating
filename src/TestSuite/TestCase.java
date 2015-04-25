package TestSuite;

public class TestCase {

  public String outputFolder;
  public String parameterFile;
  public int runs;

  public TestCase( String output, String parameters, int runs ) {
    this.outputFolder = output;
    this.parameterFile = parameters;
    this.runs = runs;
  }
}
