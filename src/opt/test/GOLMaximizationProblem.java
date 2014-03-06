package opt.test;

import java.util.Arrays;

import opt.EvaluationFunction;
import opt.example.GOLMaximizationEvaluationFunction;
import shared.Instance;
import dist.DiscreteDistribution;
import dist.DiscreteUniformDistribution;
import dist.Distribution;

public class GOLMaximizationProblem implements Problem {
	public String problemName;
	int width;
	int height;
	int iterations;

	public GOLMaximizationProblem(int width, int height, int iterations, String problemName) {
		this.problemName = problemName;
		this.width = width;
		this.height = height;
		this.iterations = iterations;
	}
	
	@Override
	public EvaluationFunction getEvaluationFunction() {
		return new GOLMaximizationEvaluationFunction(width, height, iterations);
	}
	
	@Override
	public Distribution getDistribution() {
		// DiscreteUniformDistribution doesn't work well for this problem, we need something that gives more like 1% chance of each cell being alive
		//return new DiscreteUniformDistribution(getRanges());
		//return new DiscreteDistribution(new double[] { .1, .9});
		class GOLUniformDistribution extends DiscreteUniformDistribution {
			int[] n;
			public GOLUniformDistribution(int[] n) {
				super(n);
				this.n = n;
			}

			// This is called to generate random instances, so we'll change it to return instances that are more likely to be good, with fewer living cells
			@Override
			public Instance sample(Instance ignored) {
		        double[] d  = new double[n.length];
		        for (int i = 0; i < d.length; i++) {
		            d[i] = random.nextDouble() > 0.90 ? 1.0 : 0.0;
		        }
		        return new Instance(d);
			}
			
		}
		return new GOLUniformDistribution(getRanges());
	}
	
	@Override
	public String getProblemName() {
		return problemName;
	}
	
	@Override
	public void setProblemName(String newProblemName) {
		problemName = newProblemName;
	}

	@Override
	public int[] getRanges() {
		int[] ranges = new int[width * height];
        Arrays.fill(ranges, 2);
        return ranges;
	}
}