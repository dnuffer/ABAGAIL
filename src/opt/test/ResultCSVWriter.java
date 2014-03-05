package opt.test;

import java.io.PrintWriter;

public class ResultCSVWriter implements ResultHandler {
	PrintWriter results_csv;
	public ResultCSVWriter(PrintWriter results_csv) {
		this.results_csv = results_csv;
		results_csv.print("Algorithm,Run,BestFitness,Time,Iterations,BestIteration,SecondsPerIteration\n");
	}
	public void handle(String shortName, int run, double optimalFitness, double seconds, int iterations, int bestIteration) {
		results_csv.print(shortName + "," + run + "," + optimalFitness + "," + seconds + "," + iterations + ","
				+ bestIteration + "," + seconds / iterations + "\n");
	}
	public void close() {
		results_csv.close();
	}
}