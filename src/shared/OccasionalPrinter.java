package shared;

import org.joda.time.Duration;
import org.joda.time.Instant;

/**
 * An occasional printer prints out a trainer ever once in a while
 * @author Andrew Guillory gtg008g@mail.gatech.edu
 * @version 1.0
 */
public class OccasionalPrinter implements Trainer {
    /**
     * The trainer being trained
     */
    private Trainer trainer;
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
    public OccasionalPrinter(Trainer t, Duration printDelay) {
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

}
