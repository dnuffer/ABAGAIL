package opt.test;

import java.util.Arrays;
import java.util.Random;

import org.joda.time.Duration;

import dist.DiscreteDependencyTree;
import dist.DiscretePermutationDistribution;
import dist.DiscreteUniformDistribution;
import dist.Distribution;
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
    /** The n value */
    private static final int N = 40; // TODO: increase?
    /**
     * The test main
     * @param args ignored
     */
    public static void main(String[] args) {
        Random random = new Random();
        // create the random points
        double[][] points = new double[N][2];
        for (int i = 0; i < points.length; i++) {
            points[i][0] = random.nextDouble();
            points[i][1] = random.nextDouble();   
        }
        
        // for rhc, sa, and ga we use a permutation based encoding
        MaximumAreaEvaluationFunction ef = new MaximumAreaEvaluationFunction(points, N/2);
        Distribution odd = new DiscretePermutationDistribution(N);
        NeighborFunction nf = new SwapNeighbor();
        MutationFunction mf = new SwapMutation();
        CrossoverFunction cf = new OrderedCrossOver(N);
        HillClimbingProblem hcp = new GenericHillClimbingProblem(ef, odd, nf);
        GeneticAlgorithmProblem gap = new GenericGeneticAlgorithmProblem(ef, odd, mf, cf);
        
        System.out.println("Starting Randomized Hill Climbing");
        RandomizedHillClimbing rhc = new RandomizedHillClimbing(hcp);      
        TimeLimitTrainer fit = new TimeLimitTrainer(new OccasionalPrinter(rhc, Duration.standardSeconds(1)), Duration.standardSeconds(5)); //new ConvergenceTrainer(rhc, 1e-10, 20000000, 10000); //new FixedIterationTrainer(rhc, 200000);
        fit.train();
        System.out.println("Randomized Hill Climbing optimal: " + ef.value(rhc.getOptimal()));
        System.out.println("Best iteration: " + fit.getBestIteration());
        
        System.out.println("Starting Randomized Hill Climbing with Restart");
        RandomizedHillClimbingWithRestart rhcwr = new RandomizedHillClimbingWithRestart(hcp, 6000);      
        fit = new TimeLimitTrainer(new OccasionalPrinter(rhcwr, Duration.standardSeconds(1)), Duration.standardSeconds(5)); //new ConvergenceTrainer(rhc, 1e-10, 20000000, 10000); //new FixedIterationTrainer(rhc, 200000);
        fit.train();
        System.out.println("Randomized Hill Climbing With Restart optimal: " + ef.value(rhcwr.getOptimal()));
        System.out.println("Best iteration: " + fit.getBestIteration());
        
        System.out.println("Starting Simulated Annealing .95");
        SimulatedAnnealing sa = new SimulatedAnnealing(1E12, .95, hcp);
        fit = new TimeLimitTrainer(new OccasionalPrinter(sa, Duration.standardSeconds(1)), Duration.standardSeconds(5)); // new ConvergenceTrainer(sa, 1e-10, 20000000, 10000); //new FixedIterationTrainer(sa, 200000);
        fit.train();
        System.out.println("Simulated Annealing .95 optimal: " + ef.value(sa.getOptimal()));
        System.out.println("Best iteration: " + fit.getBestIteration());
        
        System.out.println("Starting Simulated Annealing .99");
        SimulatedAnnealing sa99 = new SimulatedAnnealing(1E12, .99, hcp);
        fit = new TimeLimitTrainer(new OccasionalPrinter(sa99, Duration.standardSeconds(1)), Duration.standardSeconds(5)); // new ConvergenceTrainer(sa, 1e-10, 20000000, 10000); //new FixedIterationTrainer(sa, 200000);
        fit.train();
        System.out.println("Simulated Annealing .99 optimal: " + ef.value(sa99.getOptimal()));
        System.out.println("Best iteration: " + fit.getBestIteration());
        
        System.out.println("Starting Genetic 200-150-20 Algorithm");
        StandardGeneticAlgorithm ga = new StandardGeneticAlgorithm(200, 150, 20, gap);
        fit = new TimeLimitTrainer(new OccasionalPrinter(ga, Duration.standardSeconds(1)), Duration.standardSeconds(5)); // new ConvergenceTrainer(ga, 1e-10, 20000000, 1000); //new FixedIterationTrainer(ga, 10000);
        fit.train();
        System.out.println("Genetic Algorithm 200-150-20 optimal: " + ef.value(ga.getOptimal()));
        System.out.println("Best iteration: " + fit.getBestIteration());
        
        System.out.println("Starting Genetic 200-180-10 Algorithm");
        StandardGeneticAlgorithm ga2 = new StandardGeneticAlgorithm(200, 180, 10, gap);
        fit = new TimeLimitTrainer(new OccasionalPrinter(ga2, Duration.standardSeconds(1)), Duration.standardSeconds(5)); // new ConvergenceTrainer(ga, 1e-10, 20000000, 1000); //new FixedIterationTrainer(ga, 10000);
        fit.train();
        System.out.println("Genetic Algorithm 200-180-10 optimal: " + ef.value(ga2.getOptimal()));
        System.out.println("Best iteration: " + fit.getBestIteration());
        
        // for mimic we use a sort encoding
        //ef = new TravelingSalesmanSortEvaluationFunction(points);
        int[] ranges = new int[N];
        Arrays.fill(ranges, N);
        odd = new  DiscreteUniformDistribution(ranges);
        Distribution df = new DiscreteDependencyTree(.1, ranges); 
        ProbabilisticOptimizationProblem pop = new GenericProbabilisticOptimizationProblem(ef, odd, df);
        
        System.out.println("Starting MIMIC 200-100 Algorithm");
        MIMIC mimic = new MIMIC(200, 100, pop);
        fit = new TimeLimitTrainer(new OccasionalPrinter(mimic, Duration.standardSeconds(1)), Duration.standardSeconds(5)); // new FixedIterationTrainer(mimic, 1000);
        fit.train();
        System.out.println("MIMIC 200-100 optimal: " + ef.value(mimic.getOptimal()));
        System.out.println("Best iteration: " + fit.getBestIteration());
        
        System.out.println("Starting MIMIC 300-50 Algorithm");
        MIMIC mimic2 = new MIMIC(300, 50, pop);
        fit = new TimeLimitTrainer(new OccasionalPrinter(mimic2, Duration.standardSeconds(1)), Duration.standardSeconds(5)); // new FixedIterationTrainer(mimic, 1000);
        fit.train();
        System.out.println("MIMIC 300-50 optimal: " + ef.value(mimic2.getOptimal()));
        System.out.println("Best iteration: " + fit.getBestIteration());
        
    }
}
