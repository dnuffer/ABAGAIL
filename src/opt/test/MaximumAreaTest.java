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

import org.joda.time.Duration;

import dist.DiscreteDependencyTree;
import dist.DiscretePermutationDistribution;
import dist.DiscreteUniformDistribution;
import dist.Distribution;
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

		for (int p = 1; p <= NUM_PROBLEMS; p++) {
			String problemName = "MaximumAreaTest_" + p * 10;
			String problemDataFilename = problemName + ".csv";
			double[][] points = loadProblem(problemDataFilename);
			System.out.println("Loaded " + problemDataFilename + " " + points.length);
			doProblem(points, problemName);
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

	private static double[][] loadProblem(String problemDataFilename) throws IOException {
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

	private static void doProblem(double[][] points, String problemName) throws FileNotFoundException {
		int N = points.length;

		PrintWriter results_csv = new PrintWriter(problemName + "_results.csv");
		results_csv.print("Algorithm,Run,Fitness,Time,Iterations,BestIteration,SecondsPerIteration\n");
		PrintWriter traces_output = new PrintWriter("MaximumAreaTest_" + N + "_traces.csv");
    	traces_output.print("Algorithm,Run,TraceIdx,Iteration,Fitness\n");

    	// for rhc, sa, and ga we use a permutation based encoding
		MaximumAreaEvaluationFunction ef = new MaximumAreaEvaluationFunction(points, N / 2);
		Distribution odd = new DiscretePermutationDistribution(N);

		for (int run = 0; run < NUM_RUNS; run++) {
			doTrainAndRecord(run, results_csv, ef, traces_output, new RandomizedHillClimbing(makeHCP(ef, odd)));

			doTrainAndRecord(run, results_csv, ef, traces_output, new RandomizedHillClimbingWithRestart(makeHCP(ef, odd), 6000));
			doTrainAndRecord(run, results_csv, ef, traces_output, new SimulatedAnnealing(1E12, .95, makeHCP(ef, odd)));
			doTrainAndRecord(run, results_csv, ef, traces_output, new SimulatedAnnealing(1E12, .99, makeHCP(ef, odd)));

			doTrainAndRecord(run, results_csv, ef, traces_output, new StandardGeneticAlgorithm(200, 150, 20, makeGAP(N, ef, odd)));
			doTrainAndRecord(run, results_csv, ef, traces_output, new StandardGeneticAlgorithm(200, 180, 10, makeGAP(N, ef, odd)));
			doTrainAndRecord(run, results_csv, ef, traces_output, new MIMIC(200, 100, createMIMICPOP(N, ef)));
			doTrainAndRecord(run, results_csv, ef, traces_output, new MIMIC(300, 50, createMIMICPOP(N, ef)));
		}
		results_csv.close();
		traces_output.close();
	}

	private static void doTrainAndRecord(int run, PrintWriter results_csv, MaximumAreaEvaluationFunction ef, PrintWriter traces_output, 
			OptimizationAlgorithm optAlg) throws FileNotFoundException {
		Tracer tracer = new PeriodicTraceCSVLogger(optAlg.getShortName() + "," + run, Duration.millis(SECONDS * 1000 / 100), traces_output);

		double start = System.nanoTime();
		TrainResults results = doTrain(ef, optAlg, tracer);
		double end = System.nanoTime();
		double time = (end - start) / 10e9;
		results_csv.print(optAlg.getShortName() + "," + run + "," + results.optimalFitness + "," + time + "," + results.iterations + ","
				+ results.bestIteration + "," + time / results.iterations + "\n");
	}

	private static HillClimbingProblem makeHCP(MaximumAreaEvaluationFunction ef, Distribution odd) {
		NeighborFunction nf = new SwapNeighbor();
		HillClimbingProblem hcp = new GenericHillClimbingProblem(ef, odd, nf);
		return hcp;
	}

	private static GeneticAlgorithmProblem makeGAP(int N, MaximumAreaEvaluationFunction ef, Distribution odd) {
		MutationFunction mf = new SwapMutation();
		CrossoverFunction cf = new OrderedCrossOver(N);
		GeneticAlgorithmProblem gap = new GenericGeneticAlgorithmProblem(ef, odd, mf, cf);
		return gap;
	}

	private static ProbabilisticOptimizationProblem createMIMICPOP(int N, MaximumAreaEvaluationFunction ef) {
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

	private static TrainResults doTrain(MaximumAreaEvaluationFunction ef, OptimizationAlgorithm oa, Tracer tracer) {
		System.out.println("Starting " + oa.getDescription());
		TimeLimitTrainer fit = new TimeLimitTrainer(new OccasionalPrinter(oa, Duration.millis(200)), Duration.standardSeconds(SECONDS), tracer);
		double finalFitness = fit.train();
		System.out.println(oa.getDescription() + " optimal: " + ef.value(oa.getOptimal()) + " final: " + finalFitness);
		System.out.println("Best iteration: " + fit.getBestIteration());
		return new TrainResults(finalFitness, ef.value(oa.getOptimal()), fit.getIterations(), fit.getBestIteration());
	}
}
