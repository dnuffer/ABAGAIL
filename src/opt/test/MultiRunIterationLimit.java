package opt.test;

import java.util.Map;

public class MultiRunIterationLimit implements TrainingTerminationCondition {
	private final Map<String, Integer> limits;

	public MultiRunIterationLimit(Map<String, Integer> limits) {
		this.limits = limits;
	}

	@Override
	public TrainingTerminationState start(ParameterizedAlgorithm pa) {
		if (!limits.containsKey(pa.getShortName())) {
			throw new IllegalArgumentException("pa.getShortName(): " + pa.getShortName() + " is not valid");
		}
		return new MultiRunIterationLimitState(limits.get(pa.getShortName()));
	}

	private class MultiRunIterationLimitState implements TrainingTerminationState {
		private final int limit;
		public MultiRunIterationLimitState(int limit) {
			this.limit = limit;
		}
		private int iteration = 0;
		@Override
		public boolean shouldContinue(ParameterizedAlgorithmRun algRun) {
			iteration++;
			return iteration < limit;
		}
	}
}