package opt.test;

import opt.EvaluationFunction;
import opt.example.MaximumAreaEvaluationFunction;
import dist.DiscretePermutationDistribution;
import dist.Distribution;

public class MaximumAreaProblem implements Problem {
	public double[][] points;
	public String problemName;
	public int n;

	public MaximumAreaProblem(double[][] points, String problemName) {
		this.points = points;
		this.problemName = problemName;
		this.n = points.length;
	}
	
	@Override
	public EvaluationFunction getEvaluationFunction() {
		return new MaximumAreaEvaluationFunction(points, n / 2);
	}
	
	@Override
	public Distribution getDistribution() {
		return new DiscretePermutationDistribution(n);
	}
	
	@Override
	public int getN() {
		return n;
	}
	
	@Override
	public String getProblemName() {
		return problemName;
	}
	
	@Override
	public void setProblemName(String newProblemName) {
		problemName = newProblemName;
	}
}