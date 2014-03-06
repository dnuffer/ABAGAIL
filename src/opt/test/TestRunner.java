package opt.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.joda.time.Duration;

import shared.IntervalTraceCSVLogger;
import shared.PeriodicTraceCSVLogger;
import shared.Tracer;

public class TestRunner {

	static void doProblemPeriodicTrace(Problem problem, int[] iterationLimits, int runs, ProblemRunner runner) throws Exception {		
		PrintWriter traces_output = new PrintWriter(problem.getProblemName() + "_traces.csv");
		traces_output.print("Algorithm,Run,TraceIdx,Iteration,BestFitness,CurrentFitness\n");
		Tracer tracer = new PeriodicTraceCSVLogger("", Duration.millis(100), traces_output);
		TestRunner.doProblem(problem, iterationLimits, tracer, runs, runner);
		traces_output.close();
	}

	static void doProblemPeriodicTrace(Problem problem, Duration timeLimit, int runs, ProblemRunner runner) throws Exception {		
		PrintWriter traces_output = new PrintWriter(problem.getProblemName() + "_traces.csv");
		traces_output.print("Algorithm,Run,TraceIdx,Iteration,BestFitness,CurrentFitness\n");
		Tracer tracer = new PeriodicTraceCSVLogger("", Duration.millis(100), traces_output);
		TestRunner.doProblem(problem, timeLimit, tracer, runs, runner);
		traces_output.close();
	}

	static void doProblemAllIterationsTrace(Problem problem, int[] iterationLimits, int traceInterval, int runs, ProblemRunner runner) throws Exception {		
		PrintWriter traces_output = new PrintWriter(problem.getProblemName() + "_traces.csv");
		traces_output.print("Algorithm,Run,TraceIdx,Iteration,BestFitness,CurrentFitness\n");
		Tracer tracer = new IntervalTraceCSVLogger("", traceInterval, traces_output);
		TestRunner.doProblem(problem, iterationLimits, tracer, runs, runner);
		traces_output.close();
	}

	static interface ProblemRunner {
		ArrayList<TrainResults> runAll(ProblemRun problemRun) throws FileNotFoundException;
	}
	
	static ArrayList<TrainResults> doProblem(Problem problem, int[] iterationLimits, Tracer tracer, int runs, ProblemRunner runner) throws Exception {		
		try(ResultCSVWriter results_csv = new ResultCSVWriter(new PrintWriter(problem.getProblemName() + "_results.csv"))) {
			ArrayList<TrainResults> results = new ArrayList<TrainResults>();
			for (int run = 0; run < runs; run++) {
				results.addAll(runner.runAll(new ProblemRun(problem, results_csv, tracer, run, new MultiRunIterationLimit(iterationLimits))));
			}
			return results;
		}
	}

	static ArrayList<TrainResults> doProblem(Problem problem, Duration timeLimit, Tracer tracer, int runs, ProblemRunner runner) throws Exception {		
		try(ResultCSVWriter results_csv = new ResultCSVWriter(new PrintWriter(problem.getProblemName() + "_results.csv"))) {
			ArrayList<TrainResults> results = new ArrayList<TrainResults>();
			for (int run = 0; run < runs; run++) {
				results.addAll(runner.runAll(new ProblemRun(problem, results_csv, tracer, run, new TimeLimit(timeLimit))));
			}
			return results;
		}
	}

	static double[] toDoubleArray(List<Double> list) {
		double[] ret = new double[list.size()];
		int i = 0;
		for (Double e : list)
			ret[i++] = e.doubleValue();
		return ret;
	}

	static double[][] toDoubleArrayArray(List<List<Double>> list) {
		double[][] ret = new double[list.size()][];
		int i = 0;
		for (List<Double> e : list)
			ret[i++] = toDoubleArray(e);
		return ret;
	}

	static double[][] loadProblemData(String problemDataFilename) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(problemDataFilename)));
		try {
			ArrayList<List<Double>> results = new ArrayList<List<Double>>();
			// skip CSV column names
			String line = br.readLine();
	
			while ((line = br.readLine()) != null) {
				try(Scanner scan = new Scanner(line)) {
					scan.useDelimiter(",");
		
					ArrayList<Double> lineArray = new ArrayList<Double>();
					results.add(lineArray);
		
					while (scan.hasNext())
						lineArray.add(Double.parseDouble(scan.next()));
				}
			}
			return toDoubleArrayArray(results);
		} finally {
			br.close();
		}
	}

}
