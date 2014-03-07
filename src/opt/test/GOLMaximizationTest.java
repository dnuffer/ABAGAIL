package opt.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.Duration;

import dist.DiscreteDependencyTree;
import dist.DiscreteUniformDistribution;
import dist.Distribution;
import opt.DiscreteChangeOneNeighbor;
import opt.EvaluationFunction;
import opt.RandomizedHillClimbingWithRestart;
import opt.GenericHillClimbingProblem;
import opt.HillClimbingProblem;
import opt.NeighborFunction;
import opt.RandomizedHillClimbing;
import opt.SimulatedAnnealing;
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
import opt.test.TestRunner.ProblemRunner;

/**
 * 
 * @author Andrew Guillory gtg008g@mail.gatech.edu
 * @version 1.0
 */
public class GOLMaximizationTest {
	final static int NUM_PROBLEMS = 3;
	// final static int NUM_RUNS = 30, SECONDS = 5;
	//final static Duration TIME_LIMIT = Duration.millis(10000);
	final static int NUM_RUNS = 2;
	final static Duration TIME_LIMIT = Duration.millis(1000);

	/**
	 * The test main
	 * 
	 * @param args
	 *            ignored
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
        Thread.setDefaultUncaughtExceptionHandler(
                new Thread.UncaughtExceptionHandler() {
                    @Override public void uncaughtException(Thread t, Throwable e) {
                        System.err.println("Uncaught thread exception: " + t.getName());
                    	e.printStackTrace();
                        System.exit(1);
                    }
                });
		
		List<Problem> problems = new ArrayList<Problem>();
		for (int p = 1; p <= NUM_PROBLEMS; p++) {
			problems.add(makeProblem((int) Math.pow(2, 2 + p)));
		}
		

		// now do a run for the iteration limit
		System.out.println("JIT PRIMING AND COUNTING ITERATIONS");
		doRunAll(new ProblemRun(problems.get(0), null, null, 0, new TimeLimit(Duration.millis(100))));
		System.out.println("DONE JIT PRIMING AND COUNTING ITERATIONS");
		
		// run each of them for a fixed period of time
		for (Problem problem : problems) {
			System.out.println("---------------- RUNNING PROBLEM: " + problem.getProblemName() + " WITH TIME LIMIT: " + TIME_LIMIT);
			TestRunner.doProblemPeriodicTrace(problem, TIME_LIMIT, NUM_RUNS, new ProblemRunner() {
				public ArrayList<TrainResults> runAll(ProblemRun problemRun) throws Exception {
					return doRunAll(problemRun);
				}
			});
		}

		// run each of them for 3000 iterations (to show how MIMIC is better for expensive fitness functions)
		// but have to adjust for the populations used.
		for (Problem problem : problems) {
			String oldName = problem.getProblemName();
			problem.setProblemName(problem.getProblemName() + "_limited_iterations");
			Map<String, Integer> iters = new HashMap<String, Integer>();
			iters.put("RHC", 3000);
			iters.put("RHCWR_6000", 3000);
			iters.put("SA_0.95", 3000);
			iters.put("SA_0.93", 3000);
			iters.put("GA_200_150_20", 15);
			iters.put("GA_300_270_10", 10);
			iters.put("MIMIC_200_100", 15);
			iters.put("MIMIC_300_50", 10);
			System.out.println("---------------- RUNNING PROBLEM: " + problem.getProblemName() + " WITH FIXED ITERATIONS");
			TestRunner.doProblemAllIterationsTrace(problem, iters, 1, NUM_RUNS, new TestRunner.ProblemRunner() {
				public ArrayList<TrainResults> runAll(ProblemRun problemRun) throws Exception {
					return doRunAll(problemRun);
				}
			});
			problem.setProblemName(oldName);
		}
		System.out.println("____COMPLETE_____");
	}

	static Problem makeProblem(int p) throws IOException {
		String problemName = "GOLMaximizationTest_" + p;
		String problemDataFilename = problemName + ".csv";
		System.out.println("Loaded " + problemDataFilename);
		Problem problem = new GOLMaximizationProblem(p, p, p, problemName);
		return problem;
	}


	static ArrayList<TrainResults> doRunAll(ProblemRun runParameters)
			throws Exception {
		List<ParameterizedAlgorithmRun> runs = new ArrayList<ParameterizedAlgorithmRun>();
		runs.add(makeHCPRun(runParameters));
		runs.add(makeHCPWRRun(runParameters, 6000));
		runs.add(makeSARun(runParameters, 1E12, .95));
		runs.add(makeSARun(runParameters, 1E12, .93));
		runs.add(makeSGARun(runParameters, 200, 150, 20));
		runs.add(makeSGARun(runParameters, 300, 270, 10));
		runs.add(makeMIMICRun(runParameters, 200, 100));
		runs.add(makeMIMICRun(runParameters, 300, 50));
		
		ArrayList<TrainResults> results = TestRunner.runInParallel(runs);

		return results;
	}

	private static ParameterizedAlgorithmRun makeHCPRun(ProblemRun runParameters) {
		EvaluationFunction ef = runParameters.problem.getEvaluationFunction();
		Distribution odd = runParameters.problem.getDistribution();
		return new ParameterizedAlgorithmRun(new ParameterizedAlgorithm(ef, new RandomizedHillClimbing(makeHCP(ef,  odd, runParameters.problem.getRanges()))), runParameters);
	}

	private static ParameterizedAlgorithmRun makeHCPWRRun(ProblemRun runParameters, int restartInterval) {
		EvaluationFunction ef = runParameters.problem.getEvaluationFunction();
		Distribution odd = runParameters.problem.getDistribution();
		return new ParameterizedAlgorithmRun(new ParameterizedAlgorithm(ef, new RandomizedHillClimbingWithRestart(makeHCP(ef, odd, runParameters.problem.getRanges()), restartInterval)), runParameters);
	}

	private static ParameterizedAlgorithmRun makeSARun(ProblemRun runParameters, double t, double cooling) {
		EvaluationFunction ef = runParameters.problem.getEvaluationFunction();
		Distribution odd = runParameters.problem.getDistribution();
		return new ParameterizedAlgorithmRun(new ParameterizedAlgorithm(ef, new SimulatedAnnealing(t, cooling, makeHCP(ef, odd, runParameters.problem.getRanges()))), runParameters);
	}

	private static ParameterizedAlgorithmRun makeSGARun(ProblemRun runParameters, int populationSize, int toMate, int toMutate) {
		EvaluationFunction ef = runParameters.problem.getEvaluationFunction();
		Distribution odd = runParameters.problem.getDistribution();
		return new ParameterizedAlgorithmRun(new ParameterizedAlgorithm(ef, new StandardGeneticAlgorithm(populationSize, toMate, toMutate, makeGAP(runParameters.problem.getRanges(), ef,  odd))), runParameters);
	}

	private static ParameterizedAlgorithmRun makeMIMICRun(ProblemRun runParameters, int samples, int toKeep) {
		EvaluationFunction ef = runParameters.problem.getEvaluationFunction();
		return new ParameterizedAlgorithmRun(new ParameterizedAlgorithm(ef, new MIMIC(samples, toKeep, createMIMICPOP(runParameters.problem.getRanges(), ef))), runParameters);
	}

	private static HillClimbingProblem makeHCP(EvaluationFunction ef, Distribution odd, int[] ranges) {
		//NeighborFunction nf = new SwapNeighbor();
		NeighborFunction nf = new DiscreteChangeOneNeighbor(ranges); 
		HillClimbingProblem hcp = new GenericHillClimbingProblem(ef, odd, nf);
		return hcp;
	}

	private static GeneticAlgorithmProblem makeGAP(int[] ranges, EvaluationFunction ef, Distribution odd) {
		// TODO: See what effect changing these has
		MutationFunction mf = new SwapMutation();
		CrossoverFunction cf = new OrderedCrossOver(ranges.length);
		GeneticAlgorithmProblem gap = new GenericGeneticAlgorithmProblem(ef, odd, mf, cf);
		return gap;
	}

	private static ProbabilisticOptimizationProblem createMIMICPOP(int[] ranges, EvaluationFunction ef) {
		Distribution odd = new DiscreteUniformDistribution(ranges);
		Distribution df = new DiscreteDependencyTree(.1, ranges);
		ProbabilisticOptimizationProblem pop = new GenericProbabilisticOptimizationProblem(ef, odd, df);
		return pop;
	}
	
}
