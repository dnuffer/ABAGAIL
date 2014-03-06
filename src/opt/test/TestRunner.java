package opt.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.joda.time.Duration;

import shared.IntervalTraceCSVLoggerFactory;
import shared.PeriodicTraceCSVLoggerFactory;
import shared.TracerFactory;

public class TestRunner {

	static void doProblemPeriodicTrace(Problem problem, Map<String, Integer> iterationLimits, int runs, ProblemRunner runner) throws Exception {		
		PrintWriter traces_output = new PrintWriter(problem.getProblemName() + "_traces.csv");
		traces_output.print("Algorithm,Run,TraceIdx,Iteration,BestFitness,CurrentFitness\n");
		TracerFactory tracerFactory = new PeriodicTraceCSVLoggerFactory("", Duration.millis(100), traces_output);
		TestRunner.doProblem(problem, iterationLimits, tracerFactory, runs, runner);
		traces_output.close();
	}

	static void doProblemPeriodicTrace(Problem problem, Duration timeLimit, int runs, ProblemRunner runner) throws Exception {		
		PrintWriter traces_output = new PrintWriter(problem.getProblemName() + "_traces.csv");
		traces_output.print("Algorithm,Run,TraceIdx,Iteration,BestFitness,CurrentFitness\n");
		TracerFactory tracerFactory = new PeriodicTraceCSVLoggerFactory("", Duration.millis(Math.max(timeLimit.getMillis() / 100, 1)), traces_output);
		TestRunner.doProblem(problem, timeLimit, tracerFactory, runs, runner);
		traces_output.close();
	}

	static void doProblemAllIterationsTrace(Problem problem, Map<String, Integer> iterationLimits, int traceInterval, int runs, ProblemRunner runner) throws Exception {		
		PrintWriter traces_output = new PrintWriter(problem.getProblemName() + "_traces.csv");
		traces_output.print("Algorithm,Run,TraceIdx,Iteration,BestFitness,CurrentFitness\n");
		TracerFactory tracerFactory = new IntervalTraceCSVLoggerFactory("", traceInterval, traces_output);
		TestRunner.doProblem(problem, iterationLimits, tracerFactory, runs, runner);
		traces_output.close();
	}

	static interface ProblemRunner {
		ArrayList<TrainResults> runAll(ProblemRun problemRun) throws Exception;
	}
	
	static ArrayList<TrainResults> doProblem(Problem problem, Map<String, Integer> iterationLimits, TracerFactory tracerFactory, int runs, ProblemRunner runner) throws Exception {		
		try(ResultCSVWriter results_csv = new ResultCSVWriter(new PrintWriter(problem.getProblemName() + "_results.csv"))) {
			ArrayList<TrainResults> results = new ArrayList<TrainResults>();
			for (int run = 0; run < runs; run++) {
				results.addAll(runner.runAll(new ProblemRun(problem, results_csv, tracerFactory, run, new MultiRunIterationLimit(iterationLimits))));
			}
			return results;
		}
	}

	static ArrayList<TrainResults> doProblem(Problem problem, Duration timeLimit, TracerFactory tracerFactory, int runs, ProblemRunner runner) throws Exception {		
		try(ResultCSVWriter results_csv = new ResultCSVWriter(new PrintWriter(problem.getProblemName() + "_results.csv"))) {
			ArrayList<TrainResults> results = new ArrayList<TrainResults>();
			for (int run = 0; run < runs; run++) {
				results.addAll(runner.runAll(new ProblemRun(problem, results_csv, tracerFactory, run, new TimeLimit(timeLimit))));
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

	static ArrayList<TrainResults> runInParallel(List<ParameterizedAlgorithmRun> runs) throws InterruptedException, ExecutionException {
		// lots of complication to run these all in parallel in a thread pool
		
		// 1. make a list of "Callable" closures around each run
		List<Callable<TrainResults>> callables = new ArrayList<Callable<TrainResults>>();
		for (final ParameterizedAlgorithmRun run : runs) {
			callables.add(new Callable<TrainResults>() {
				public TrainResults call() throws Exception {
					return run.doTrainAndRecord();
				}
			});
		}
		
		// 2. start them all, and save a list of futures
		ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
		List<Future<TrainResults>> futures = es.invokeAll(callables);
		
		// 3. Wait for all of the futures to get the results
		ArrayList<TrainResults> results = new ArrayList<TrainResults>();
		for (Future<TrainResults> future : futures) {
			results.add(future.get());
		}
		es.shutdown();
		return results;
	}

}
