package opt.test;

import opt.EvaluationFunction;
import dist.Distribution;

public interface Problem {
	EvaluationFunction getEvaluationFunction();
	void setProblemName(String string);
	String getProblemName();
	Distribution getDistribution();
	int[] getRanges();
}