package opt.test;

import opt.EvaluationFunction;
import opt.OptimizationAlgorithm;
import shared.Instance;

public class ParameterizedAlgorithm extends OptimizationAlgorithm {
	EvaluationFunction ef;
	OptimizationAlgorithm oa;
	
	public ParameterizedAlgorithm(EvaluationFunction ef, OptimizationAlgorithm oa) {
		super(oa.getOptimizationProblem());
		this.ef = ef;
		this.oa = oa;
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