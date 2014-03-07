package opt.test;

import java.util.Arrays;

import opt.EvaluationFunction;
import opt.example.ContinuousPeaksEvaluationFunction;
import dist.DiscretePermutationDistribution;
import dist.DiscreteUniformDistribution;
import dist.Distribution;

public class DansContinuousPeaksProblem implements Problem {
	private String problemName;
	private int n;
	private int t;

	public DansContinuousPeaksProblem(int n, int t, String problemName) {
		this.problemName = problemName;
		this.n = n;
		this.t = t;
	}
	
	@Override
	public EvaluationFunction getEvaluationFunction() {
		return new ContinuousPeaksEvaluationFunction(t);
	}
	
	@Override
	public Distribution getDistribution() {
		return new DiscreteUniformDistribution(getRanges());
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
		int[] ranges = new int[n];
		Arrays.fill(ranges, n);
		return ranges;
	}
}