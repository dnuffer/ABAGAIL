package opt.test;

public interface TrainingTerminationCondition {
	public TrainingTerminationState start(ParameterizedAlgorithm pa);
}