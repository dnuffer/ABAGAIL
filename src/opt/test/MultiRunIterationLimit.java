package opt.test;


public class MultiRunIterationLimit implements TrainingTerminationCondition {
	private int[] iterationLimits;
	private int run = -1;
	private int iteration = 0;
	public MultiRunIterationLimit(int[] iterationLimits) {
		this.iterationLimits = iterationLimits;
	}
	@Override
	public void start() {
		run = (run + 1) % iterationLimits.length;
		iteration = 0;
	}
	@Override
	public boolean shouldContinue(ParameterizedAlgorithmRun algRun) {
		iteration++;
		return iteration < iterationLimits[run];
	}
	
}