package opt.test;

import opt.EvaluationFunction;
import opt.OptimizationAlgorithm;
import shared.Instance;
import dist.Distribution;

public class ParameterizedAlgorithm extends OptimizationAlgorithm {
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