package opt.test;

import shared.Tracer;
import shared.TracerFactory;

public class ProblemRun {
	public Problem problem;
	public ResultHandler resultHandler;
	public TracerFactory tracerFactory;
	public int run;
	public TrainingTerminationCondition termination;

	public ProblemRun(Problem p, ResultHandler resultHandler, TracerFactory tracerFactory, int run, TrainingTerminationCondition termination) {
		this.problem = p;
		this.resultHandler = resultHandler;
		this.tracerFactory = tracerFactory;
		this.run = run;
		this.termination = termination;
	}
}