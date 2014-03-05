package opt.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.Duration;

import dist.DiscreteDependencyTree;
import dist.DiscreteUniformDistribution;
import dist.Distribution;
import opt.EvaluationFunction;
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
	final static int NUM_RUNS = 2, MILLISECONDS = 100;

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
			problems.add(TestRunner.loadProblem(p));
		}
		

		// now do a run for the iteration limit
		System.out.println("JIT PRIMING AND COUNTING ITERATIONS");
		ArrayList<TrainResults> results = doRunAll(new ProblemRun(problems.get(0), null, null, 0, new TimeLimit(Duration.millis(MaximumAreaTest.MILLISECONDS))));
		int[] iterationLimits = new int[results.size()];
		for (int i = 0; i < results.size(); i++) {
			iterationLimits[i] = results.get(i).iterations;
			//System.out.println("iterationLimits[" + i + "]: " + iterationLimits[i]);
		}
		System.out.println("DONE JIT PRIMING AND COUNTING ITERATIONS");
		
		// run each of them for a fixed period of time
		for (Problem problem : problems) {
			TestRunner.doProblemPeriodicTrace(problem, iterationLimits);
		}

		// run each of them for 100 iterations (to show how MIMIC is better for expensive fitness functions)
		// but have to adjust for the populations used.
		for (Problem problem : problems) {
			String oldName = problem.getProblemName();
			problem.setProblemName(problem.getProblemName() + "_limited_iterations");
			int[] iters = new int[]{
					3000, // RHC x1 
					3000, // RHCWR x1 
					3000, // SA.95 x1 
					3000, // SA.99 x1 
					15, // GA200 x200 
					15, // GA200 x200
					15, // MIMIC200 x200
					10  // MIMIC300 x300
					};
			TestRunner.doProblemAllIterationsTrace(problem, iters, 1);
			problem.setProblemName(oldName);
		}
	
	}

	static ArrayList<TrainResults> doRunAll(ProblemRun runParameters)
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
		return new ParameterizedAlgorithmRun(new ParameterizedAlgorithm(ef, new StandardGeneticAlgorithm(populationSize, toMate, toMutate, makeGAP(runParameters.problem.getN(), ef,  odd)), odd), runParameters);
	}

	private static ParameterizedAlgorithmRun makeMIMICRun(ProblemRun runParameters, int samples, int toKeep) {
		EvaluationFunction ef = runParameters.problem.getEvaluationFunction();
		Distribution odd = runParameters.problem.getDistribution();
		return new ParameterizedAlgorithmRun(new ParameterizedAlgorithm(ef, new MIMIC(samples, toKeep, createMIMICPOP(runParameters.problem.getN(), ef)), odd), runParameters);
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
}
