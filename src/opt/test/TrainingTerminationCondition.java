package opt.test;


public interface TrainingTerminationCondition {
	public void start();
	public boolean shouldContinue(ParameterizedAlgorithmRun run); 
}