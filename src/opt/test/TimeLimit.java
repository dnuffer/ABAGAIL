package opt.test;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public class TimeLimit implements TrainingTerminationCondition {
	private Duration timeLimit;
	private DateTime end;
	public TimeLimit(Duration timeLimit) {
		this.timeLimit = timeLimit;
	}
	@Override
	public void start() {
		end = DateTime.now().plus(timeLimit);
	}
	@Override
	public boolean shouldContinue(ParameterizedAlgorithmRun run) {
		return end.isAfterNow();
	}
}