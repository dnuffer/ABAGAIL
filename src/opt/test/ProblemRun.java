package opt.test;

import shared.Tracer;

public class ProblemRun {
	public Problem problem;
	public ResultHandler resultHandler;
	public Tracer traces_output;
	public int run;
	public TrainingTerminationCondition termination;

	public ProblemRun(Problem p, ResultHandler resultHandler, Tracer traces_output, int run, TrainingTerminationCondition termination) {
		this.problem = p;
		this.resultHandler = resultHandler;
		this.traces_output = traces_output;
		this.run = run;
		this.termination = termination;
	}
}