package shared;

import java.io.PrintWriter;

import org.joda.time.Duration;
import org.joda.time.Instant;

/**
 * Periodically write an algorithm trace in CSV format.
 * @author Andrew Guillory gtg008g@mail.gatech.edu
 * @version 1.0
 */
public class IntervalTraceCSVLogger implements Tracer {
    private String staticPrefix;
    private PrintWriter output;
    private int interval;
    private int traceCount = 1;
    
    /**
     * Make a new occasional printer
     * @param iterationsPerPrint the number of iterations per print
     * @param t the trainer
     */
    public IntervalTraceCSVLogger(String staticPrefix, int interval, PrintWriter output) {
    	this.staticPrefix = staticPrefix;
    	this.interval = interval;
        this.output = output;
    }

    /* (non-Javadoc)
	 * @see shared.Tracer#trace(int, double)
	 */
    @Override
	public void trace(int iteration, double fitness) {
        if (iteration % interval == 0) {
        	output.println(staticPrefix + "," + traceCount + "," + iteration + "," + fitness);
        }
    }

}
