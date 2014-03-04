package opt.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import dist.DiscreteDependencyTree;
import dist.DiscretePermutationDistribution;
import dist.DiscreteUniformDistribution;
import dist.Distribution;
import opt.EvaluationFunction;
import opt.OptimizationAlgorithm;
import opt.RandomizedHillClimbingWithRestart;
import opt.SwapNeighbor;
import opt.GenericHillClimbingProblem;
import opt.HillClimbingProblem;
import opt.NeighborFunction;
import opt.RandomizedHillClimbing;
import opt.SimulatedAnnealing;
import opt.example.*;
import opt.ga.CrossoverFunction;
import opt.ga.OrderedCrossOver;
import opt.ga.SwapMutation;
import opt.ga.GenericGeneticAlgorithmProblem;
import opt.ga.GeneticAlgorithmProblem;
import opt.ga.MutationFunction;
import opt.ga.StandardGeneticAlgorithm;
import opt.prob.GenericProbabilisticOptimizationProblem;
import opt.prob.MIMIC;
import opt.prob.ProbabilisticOptimizationProblem;
import shared.*;

/**
 * 
 * @author Andrew Guillory gtg008g@mail.gatech.edu
 * @version 1.0
 */
public class MaximumAreaTest {
	final static int NUM_PROBLEMS = 5;
	// final static int NUM_RUNS = 30, SECONDS = 5;
	final static int NUM_RUNS = 2, SECONDS = 1;

	/**
	 * The test main
	 * 
	 * @param args
	 *            ignored
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		List<Problem> problems = new ArrayList<Problem>();
		for (int p = 1; p <= NUM_PROBLEMS; p++) {
			problems.add(loadProblem(p));
		}
		

		// now do a run for the iteration limit
		System.out.println("JIT PRIMING AND COUNTING ITERATIONS");
		ArrayList<TrainResults> results = doRunAll(new ProblemRun(problems.get(0), null, null, 0, new TimeLimit(Duration.standardSeconds(MaximumAreaTest.SECONDS))));
		int[] iterationLimits = new int[results.size()];
		for (int i = 0; i < results.size(); i++) {
			iterationLimits[i] = results.get(i).iterations;
			//System.out.println("iterationLimits[" + i + "]: " + iterationLimits[i]);
		}
		System.out.println("DONE JIT PRIMING AND COUNTING ITERATIONS");
		
		
		for (Problem problem : problems) {
			doProblem(problem, iterationLimits);
		}
	}

	private static Problem loadProblem(int p) throws IOException {
		String problemName = "MaximumAreaTest_" + p * 10;
		String problemDataFilename = problemName + ".csv";
		double[][] points = loadProblemData(problemDataFilename);
		System.out.println("Loaded " + problemDataFilename + " " + points.length);
		Problem problem = new Problem(points, problemName);
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

	private static double[][] loadProblemData(String problemDataFilename) throws IOException {
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


	public static class Problem {
		public double[][] points;
		public String problemName;
		public int n;

		public Problem(double[][] points, String problemName) {
			this.points = points;
			this.problemName = problemName;
			this.n = points.length;
		}
		
		public EvaluationFunction getEvaluationFunction() {
			return new MaximumAreaEvaluationFunction(points, n / 2);
		}
		
		public Distribution getDistribution() {
			return new DiscretePermutationDistribution(n);
		}
	}

	public static class ProblemRun {
		public Problem problem;
		public ResultHandler resultHandler;
		public PrintWriter traces_output;
		public int run;
		public TrainingTerminationCondition termination;

		public ProblemRun(Problem p, ResultHandler resultHandler, PrintWriter traces_output, int run, TrainingTerminationCondition termination) {
			this.problem = p;
			this.resultHandler = resultHandler;
			this.traces_output = traces_output;
			this.run = run;
			this.termination = termination;
		}
	}
	
	public static interface TrainingTerminationCondition {
		public void start();
		public boolean shouldContinue(ParameterizedAlgorithmRun run); 
	}
	
	public static class TimeLimit implements TrainingTerminationCondition {
		private Duration timeLimit;
		private DateTime end;
		public TimeLimit(Duration timeLimit) {
			this.timeLimit = timeLimit;
		}
		@Override
		public void start() {
			end = DateTime.now().plus(timeLimit);
		}
		@Override
		public boolean shouldContinue(ParameterizedAlgorithmRun run) {
			return end.isAfterNow();
		}
	}
	
	public static class MultiRunIterationLimit implements TrainingTerminationCondition {
		private int[] iterationLimits;
		private int run = -1;
		private int iteration = 0;
		public MultiRunIterationLimit(int[] iterationLimits) {
			this.iterationLimits = iterationLimits;
		}
		@Override
		public void start() {
			run = (run + 1) % iterationLimits.length;
			iteration = 0;
		}
		@Override
		public boolean shouldContinue(ParameterizedAlgorithmRun algRun) {
			iteration++;
			return iteration < iterationLimits[run];
		}
		
	}
	
	public static interface ResultHandler {
		public void handle(String shortName, int run, double optimalFitness, double seconds, int iterations, int bestIteration);
	}
	
	public static class ResultCSVWriter implements ResultHandler {
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


	private static void doProblem(Problem problem, int[] iterationLimits) throws FileNotFoundException {		
		ResultCSVWriter results_csv = new ResultCSVWriter(new PrintWriter(problem.problemName + "_results.csv"));
		PrintWriter traces_output = new PrintWriter("MaximumAreaTest_" + problem.n + "_traces.csv");
    	traces_output.print("Algorithm,Run,TraceIdx,Iteration,BestFitness,CurrentFitness\n");

		for (int run = 0; run < NUM_RUNS; run++) {
			// TODO: use iteration count from first run here
			doRunAll(new ProblemRun(problem, results_csv, traces_output, run, new MultiRunIterationLimit(iterationLimits)));
		}
		results_csv.close();
		traces_output.close();
	}
	
	public static class ParameterizedAlgorithmRun extends OptimizationAlgorithm {
		ParameterizedAlgorithm pa;
		ProblemRun pr;
		Tracer tracer;
		public ParameterizedAlgorithmRun(ParameterizedAlgorithm pa, ProblemRun pr) {
			super(pa.oa.getOptimizationProblem());
			this.pa = pa;
			this.pr = pr;
			this.tracer = makeTracer(); // call this last
		}
		
		public String getShortName() {
			return pa.oa.getShortName();
		}

		@Override
		public double train() {
			return pa.oa.train();
		}

		@Override
		public Instance getOptimal() {
			return pa.oa.getOptimal();
		}

		@Override
		public String getDescription() {
			return pa.oa.getDescription();
		}

		public double getOptimalValue() {
			return pa.ef.value(pa.oa.getOptimal());
		}

		private Tracer makeTracer() {
			if (pr.traces_output != null) {
				return new PeriodicTraceCSVLogger(getShortName() + "," + pr.run, Duration.millis(SECONDS * 1000 / 100), pr.traces_output);
			} else {
				return null;
			}
		}

		void recordRun(MaximumAreaTest.TrainResults results, double time) {
			if (pr.resultHandler != null) {
				pr.resultHandler.handle(getShortName(), pr.run, results.optimalFitness, time, results.iterations, results.bestIteration);
			}
		}

		TrainResults doTrain() {
			System.out.println("Starting " + getDescription());
//			TimeLimitTrainer fit = new TimeLimitTrainer(new OccasionalPrinter(this, Duration.millis(200), getOptimizationProblem()), Duration.standardSeconds(MaximumAreaTest.SECONDS), 
//					makeTracer());
//			double finalFitness = fit.train();
			
			int iterations = 0;
			double value = Double.MAX_VALUE;
			int bestIteration = 0;
			double best = Double.MIN_VALUE;
			
			pr.termination.start();
			
			while (pr.termination.shouldContinue(this)) {
				iterations++;
				value = train();
				if (tracer != null) {
					tracer.trace(iterations, getOptimalValue(), value);
				}
				if (value > best) {
					best = value;
					bestIteration = iterations;
				}
			}

			double finalFitness = value;
			
			System.out.println(getDescription() + " optimal: " + getOptimalValue() + " final: " + finalFitness);
			System.out.println("Best iteration: " + bestIteration);
			return new MaximumAreaTest.TrainResults(finalFitness, getOptimalValue(), iterations, bestIteration);
		}

		TrainResults doTrainAndRecord() throws FileNotFoundException {
			double start = System.nanoTime();
			MaximumAreaTest.TrainResults results = doTrain();
			double end = System.nanoTime();
			double time = (end - start) / 10e9;
			recordRun(results, time);
			return results;
		}
		
	}
	
	public static class ParameterizedAlgorithm extends OptimizationAlgorithm {
		EvaluationFunction ef;
		OptimizationAlgorithm oa;
		Distribution odd;
		
		public ParameterizedAlgorithm(EvaluationFunction ef, OptimizationAlgorithm oa, Distribution odd) {
			super(oa.getOptimizationProblem());
			this.ef = ef;
			this.oa = oa;
			this.odd = odd;
		}

		public String getShortName() {
			return oa.getShortName();
		}

		@Override
		public double train() {
			return oa.train();
		}

		@Override
		public Instance getOptimal() {
			return oa.getOptimal();
		}

		@Override
		public String getDescription() {
			return oa.getDescription();
		}

		public double getOptimalValue() {
			return ef.value(oa.getOptimal());
		}
	}

	private static ArrayList<TrainResults> doRunAll(ProblemRun runParameters)
			throws FileNotFoundException {
		ArrayList<TrainResults> results = new ArrayList<TrainResults>();
		results.add(makeHCPRun(runParameters).doTrainAndRecord());
		results.add(makeHCPWRRun(runParameters, 6000).doTrainAndRecord());
		results.add(makeSARun(runParameters, 1E12, .95).doTrainAndRecord());
		results.add(makeSARun(runParameters, 1E12, .99).doTrainAndRecord());
		results.add(makeSGARun(runParameters, 200, 150, 20).doTrainAndRecord());
		results.add(makeSGARun(runParameters, 200, 180, 10).doTrainAndRecord());
		results.add(makeMIMICRun(runParameters, 200, 100).doTrainAndRecord());
		results.add(makeMIMICRun(runParameters, 300, 50).doTrainAndRecord());
		return results;
	}

	private static ParameterizedAlgorithmRun makeHCPRun(ProblemRun runParameters) {
		EvaluationFunction ef = runParameters.problem.getEvaluationFunction();
		Distribution odd = runParameters.problem.getDistribution();
		return new ParameterizedAlgorithmRun(new ParameterizedAlgorithm(ef, new RandomizedHillClimbing(makeHCP(ef,  odd)), odd), runParameters);
	}

	private static ParameterizedAlgorithmRun makeHCPWRRun(ProblemRun runParameters, int restartInterval) {
		EvaluationFunction ef = runParameters.problem.getEvaluationFunction();
		Distribution odd = runParameters.problem.getDistribution();
		return new ParameterizedAlgorithmRun(new ParameterizedAlgorithm(ef, new RandomizedHillClimbingWithRestart(makeHCP(ef,  odd), restartInterval), odd), runParameters);
	}

	private static ParameterizedAlgorithmRun makeSARun(ProblemRun runParameters, double t, double cooling) {
		EvaluationFunction ef = runParameters.problem.getEvaluationFunction();
		Distribution odd = runParameters.problem.getDistribution();
		return new ParameterizedAlgorithmRun(new ParameterizedAlgorithm(ef, new SimulatedAnnealing(t, cooling, makeHCP(ef,  odd)), odd), runParameters);
	}

	private static ParameterizedAlgorithmRun makeSGARun(ProblemRun runParameters, int populationSize, int toMate, int toMutate) {
		EvaluationFunction ef = runParameters.problem.getEvaluationFunction();
		Distribution odd = runParameters.problem.getDistribution();
		return new ParameterizedAlgorithmRun(new ParameterizedAlgorithm(ef, new StandardGeneticAlgorithm(populationSize, toMate, toMutate, makeGAP(runParameters.problem.n, ef,  odd)), odd), runParameters);
	}

	private static ParameterizedAlgorithmRun makeMIMICRun(ProblemRun runParameters, int samples, int toKeep) {
		EvaluationFunction ef = runParameters.problem.getEvaluationFunction();
		Distribution odd = runParameters.problem.getDistribution();
		return new ParameterizedAlgorithmRun(new ParameterizedAlgorithm(ef, new MIMIC(samples, toKeep, createMIMICPOP(runParameters.problem.n, ef)), odd), runParameters);
	}

	private static HillClimbingProblem makeHCP(EvaluationFunction ef, Distribution odd) {
		NeighborFunction nf = new SwapNeighbor();
		HillClimbingProblem hcp = new GenericHillClimbingProblem(ef, odd, nf);
		return hcp;
	}

	private static GeneticAlgorithmProblem makeGAP(int N, EvaluationFunction ef, Distribution odd) {
		MutationFunction mf = new SwapMutation();
		CrossoverFunction cf = new OrderedCrossOver(N);
		GeneticAlgorithmProblem gap = new GenericGeneticAlgorithmProblem(ef, odd, mf, cf);
		return gap;
	}

	private static ProbabilisticOptimizationProblem createMIMICPOP(int N, EvaluationFunction ef) {
		Distribution odd;
		int[] ranges = new int[N];
		Arrays.fill(ranges, N);
		odd = new DiscreteUniformDistribution(ranges);
		Distribution df = new DiscreteDependencyTree(.1, ranges);
		ProbabilisticOptimizationProblem pop = new GenericProbabilisticOptimizationProblem(ef, odd, df);
		return pop;
	}

	static class TrainResults {

		public TrainResults(double finalFitness, double optimalFitness, int iterations, int bestIteration) {
			this.finalFitness = finalFitness;
			this.optimalFitness = optimalFitness;
			this.iterations = iterations;
			this.bestIteration = bestIteration;
		}

		public double finalFitness;
		public double optimalFitness;
		public int iterations;
		public int bestIteration;
	}
}
