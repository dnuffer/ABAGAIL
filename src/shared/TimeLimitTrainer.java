package shared;

import opt.OptimizationAlgorithm;

import org.joda.time.*;

/**
 * A convergence trainer trains a network until convergence, using another
 * trainer
 * 
 * @author Andrew Guillory gtg008g@mail.gatech.edu
 * @version 1.0
 */
public class TimeLimitTrainer implements Trainer {

	/**
	 * The trainer
	 */
	private OptimizationAlgorithm trainer;

	private Duration timeLimit;
	private int iterations;
	private double best;
	private int bestIteration;
	private TracerFactory tracerFactory;

	/**
	 * Create a new convergence trainer
	 * 
	 * @param trainer
	 *            the thrainer to use
	 * @param tracerFactory If != null, called every iteration
	 * @param threshold
	 *            the error threshold
	 * @param maxIterations
	 *            the maximum iterations
	 */
	public TimeLimitTrainer(OptimizationAlgorithm trainer, Duration timeLimit, TracerFactory tracerFactory) {
		this.trainer = trainer;
		this.timeLimit = timeLimit;
		this.best = Double.MIN_VALUE;
		this.tracerFactory = tracerFactory;
	}

	/**
	 * @see Trainer#train()
	 */
	public double train() {
		double value = Double.MAX_VALUE;
		DateTime end = DateTime.now().plus(timeLimit);
		
		Tracer tracer = null;
		if (tracerFactory != null) {
			tracer = tracerFactory.start(trainer.getShortName(), 0);
		}
		
		while (end.isAfterNow()) {
			iterations++;
			value = trainer.train();
			if (tracer != null) {
				tracer.trace(iterations, trainer.getOptimizationProblem().value(trainer.getOptimal()), value);
			}
			if (value > best) {
				best = value;
				bestIteration = iterations;
			}
		}
		
		return value;
	}

	/**
	 * Get the number of iterations used
	 * 
	 * @return the number of iterations
	 */
	public int getIterations() {
		return iterations;
	}
	
	public int getBestIteration() {
		return bestIteration;
	}

}
