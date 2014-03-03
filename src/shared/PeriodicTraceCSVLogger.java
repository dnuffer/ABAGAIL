package shared;

import java.io.PrintWriter;

import org.joda.time.Duration;
import org.joda.time.Instant;

/**
 * Periodically write an algorithm trace in CSV format.
 * @author Andrew Guillory gtg008g@mail.gatech.edu
 * @version 1.0
 */
public class PeriodicTraceCSVLogger implements Tracer {
    private String staticPrefix;
    private Duration printDelay;
    private Instant nextPrint;
    private PrintWriter output;
    
    /**
     * Make a new occasional printer
     * @param iterationsPerPrint the number of iterations per print
     * @param t the trainer
     */
    public PeriodicTraceCSVLogger(String staticPrefix, Duration printDelay, PrintWriter output) {
    	this.staticPrefix = staticPrefix;
        this.printDelay = printDelay;
        this.nextPrint = new Instant().plus(printDelay);
        this.output = output;
    }

    /* (non-Javadoc)
	 * @see shared.Tracer#trace(int, double)
	 */
    @Override
	public void trace(int iteration, double fitness) {
        if (nextPrint.isBeforeNow()) {
        	output.println(staticPrefix + "," + iteration + "," + fitness);
            nextPrint = nextPrint.plus(printDelay);
        }
    }

}
