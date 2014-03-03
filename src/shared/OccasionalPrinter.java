package shared;

import opt.OptimizationAlgorithm;
import opt.OptimizationProblem;

import org.joda.time.Duration;
import org.joda.time.Instant;

/**
 * An occasional printer prints out a trainer ever once in a while
 * @author Andrew Guillory gtg008g@mail.gatech.edu
 * @version 1.0
 */
public class OccasionalPrinter extends OptimizationAlgorithm {
    /**
     * The trainer being trained
     */
    private OptimizationAlgorithm trainer;
    /**
     * How long to go between prints
     */
    private Duration printDelay;
    private Instant nextPrint;
    private int iteration;
    
    /**
     * Make a new occasional printer
     * @param iterationsPerPrint the number of iterations per print
     * @param t the trainer
     */
    public OccasionalPrinter(OptimizationAlgorithm t, Duration printDelay, OptimizationProblem op) {
    	super(op);
        this.printDelay = printDelay;
        this.trainer = t;
        this.nextPrint = new Instant().plus(printDelay);
    }

    /**
     * @see shared.Trainer#train()
     */
    public double train() {
    	iteration++;
        double result = trainer.train();
        if (nextPrint.isBeforeNow()) {
            System.out.println("Iteration " + iteration + ": " + result);
            nextPrint = nextPrint.plus(printDelay);
        }
		return result;
    }

	@Override
	public Instance getOptimal() {
		return trainer.getOptimal();
	}

	@Override
	public String getDescription() {
		return trainer.getDescription();
	}

	@Override
	public String getShortName() {
		return trainer.getShortName();
	}

}
