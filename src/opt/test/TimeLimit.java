package opt.test;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public class TimeLimit implements TrainingTerminationCondition {
	private Duration timeLimit;

	public TimeLimit(Duration timeLimit) {
		this.timeLimit = timeLimit;
	}

	@Override
	public TrainingTerminationState start(ParameterizedAlgorithm pa) {
		return new TimeLimitState(timeLimit);
	}

	private class TimeLimitState implements TrainingTerminationState {
		private DateTime end;
		public TimeLimitState(Duration timeLimit) {
			end = DateTime.now().plus(timeLimit);			
		}
		@Override
		public boolean shouldContinue(ParameterizedAlgorithmRun run) {
			return end.isAfterNow();
		}
	}
}