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

	static void doProblemPeriodicTrace(Problem problem, int[] iterationLimits) throws FileNotFoundException {		
		PrintWriter traces_output = new PrintWriter(problem.getProblemName() + "_traces.csv");
		traces_output.print("Algorithm,Run,TraceIdx,Iteration,BestFitness,CurrentFitness\n");
		Tracer tracer = new PeriodicTraceCSVLogger("", Duration.millis(100), traces_output);
		TestRunner.doProblem(problem, iterationLimits, tracer);
		traces_output.close();
	}

	static void doProblemAllIterationsTrace(Problem problem, int[] iterationLimits, int traceInterval) throws FileNotFoundException {		
		PrintWriter traces_output = new PrintWriter(problem.getProblemName() + "_traces.csv");
		traces_output.print("Algorithm,Run,TraceIdx,Iteration,BestFitness,CurrentFitness\n");
		Tracer tracer = new IntervalTraceCSVLogger("", traceInterval, traces_output);
		TestRunner.doProblem(problem, iterationLimits, tracer);
		traces_output.close();
	}

	static void doProblem(Problem problem, int[] iterationLimits, Tracer tracer) throws FileNotFoundException {		
		ResultCSVWriter results_csv = new ResultCSVWriter(new PrintWriter(problem.getProblemName() + "_results.csv"));
		
		for (int run = 0; run < MaximumAreaTest.NUM_RUNS; run++) {
			MaximumAreaTest.doRunAll(new ProblemRun(problem, results_csv, tracer, run, new MultiRunIterationLimit(iterationLimits)));
		}
		
		results_csv.close();
	}

	static Problem loadProblem(int p) throws IOException {
		String problemName = "MaximumAreaTest_" + p * 10;
		String problemDataFilename = problemName + ".csv";
		double[][] points = TestRunner.loadProblemData(problemDataFilename);
		System.out.println("Loaded " + problemDataFilename + " " + points.length);
		Problem problem = new MaximumAreaProblem(points, problemName);
		return problem;
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
				Scanner scan = new Scanner(line);
				scan.useDelimiter(",");
	
				ArrayList<Double> lineArray = new ArrayList<Double>();
				results.add(lineArray);
	
				while (scan.hasNext())
					lineArray.add(Double.parseDouble(scan.next()));
			}
			return toDoubleArrayArray(results);
		} finally {
			br.close();
		}
	}

}
