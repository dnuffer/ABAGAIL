package opt.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.Duration;

import dist.DiscreteDependencyTree;
import dist.DiscreteUniformDistribution;
import dist.Distribution;
import opt.DiscreteChangeOneNeighbor;
import opt.EvaluationFunction;
import opt.GenericHillClimbingProblem;
import opt.HillClimbingProblem;
import opt.NeighborFunction;
import opt.RandomizedHillClimbing;
import opt.RandomizedHillClimbingWithRestart;
import opt.SimulatedAnnealing;
import opt.ga.CrossoverFunction;
import opt.ga.DiscreteChangeOneMutation;
import opt.ga.SingleCrossOver;
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
public class DansContinuousPeaksTest {
	final static int NUM_PROBLEMS = 5;
	final static int NUM_RUNS = 2;
	final static Duration TIME_LIMIT = Duration.millis(1000);
	//final static int NUM_RUNS = 30;
	//final static Duration TIME_LIMIT = Duration.millis(10000);

//	
//	
//    /** The n value */
//    private static final int N = 60;
//    /** The t value */
//    private static final int T = N / 10;
    
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
			int n = (int) Math.pow(2, 3 + p);
			int t = n / 10;
			problems.add(makeProblem(n, t));
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
			iters.put("GA_200_100_10", 15);
			iters.put("GA_300_270_10", 10);
			iters.put("MIMIC_200_20", 15);
			iters.put("MIMIC_300_15", 10);
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
    
	static Problem makeProblem(int n, int t) throws IOException {
		String problemName = "ContinuousPeaksTest_" + n + "_" + t;
		String problemDataFilename = problemName + ".csv";
		System.out.println("Loaded " + problemDataFilename);
		Problem problem = new DansContinuousPeaksProblem(n, t, problemName);
		return problem;
	}

	static ArrayList<TrainResults> doRunAll(ProblemRun runParameters)
			throws Exception {
		List<ParameterizedAlgorithmRun> runs = new ArrayList<ParameterizedAlgorithmRun>();
		runs.add(makeHCPRun(runParameters));
		runs.add(makeHCPWRRun(runParameters, 6000));
		runs.add(makeSARun(runParameters, 1E12, .95));
		runs.add(makeSARun(runParameters, 1E12, .93));
		runs.add(makeSGARun(runParameters, 200, 100, 10));
		runs.add(makeSGARun(runParameters, 300, 270, 10));
		runs.add(makeMIMICRun(runParameters, 200, 20));
		runs.add(makeMIMICRun(runParameters, 300, 15));
		
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
		NeighborFunction nf = new DiscreteChangeOneNeighbor(ranges); 
		HillClimbingProblem hcp = new GenericHillClimbingProblem(ef, odd, nf);
		return hcp;
	}

	private static GeneticAlgorithmProblem makeGAP(int[] ranges, EvaluationFunction ef, Distribution odd) {
		// TODO: See what effect changing these has
		MutationFunction mf = new DiscreteChangeOneMutation(ranges);
		CrossoverFunction cf = new SingleCrossOver();
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
