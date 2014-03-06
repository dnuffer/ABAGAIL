package shared;

import java.io.PrintWriter;

public class IntervalTraceCSVLoggerFactory implements TracerFactory {
    private String staticPrefix;
    private PrintWriter output;
    private int interval;

    public IntervalTraceCSVLoggerFactory(String staticPrefix, int interval, PrintWriter output) {
    	this.staticPrefix = staticPrefix;
    	this.interval = interval;
        this.output = output;
    }

    @Override
	public Tracer start(String algorithmId, int run) {
    	return new IntervalTraceCSVLogger(staticPrefix, interval, output, algorithmId, run);
    }
    
    
    private final class IntervalTraceCSVLogger implements Tracer {
        private final String staticPrefix;
        private final PrintWriter output;
        private final int interval;
    	private final String algorithmId;
    	private final int run;
        private int traceCount = 1;
        
        /**
         * Make a new occasional printer
         * @param iterationsPerPrint the number of iterations per print
         * @param t the trainer
         */
        public IntervalTraceCSVLogger(String staticPrefix, int interval, PrintWriter output, String algorithmId, int run) {
        	this.staticPrefix = staticPrefix;
        	this.interval = interval;
            this.output = output;
            this.algorithmId = algorithmId;
            this.run = run;
        }

        /* (non-Javadoc)
    	 * @see shared.Tracer#trace(int, double)
    	 */
        @Override
    	public synchronized void trace(int iteration, double optimalValue, double trainValue) {
            if (iteration % interval == 0) {
            	output.println(staticPrefix + algorithmId + "," + run + "," + traceCount + "," + iteration + "," + optimalValue + "," + trainValue);
            	traceCount++;
            }
        }

    }

}

