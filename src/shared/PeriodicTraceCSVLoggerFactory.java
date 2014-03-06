package shared;

import java.io.PrintWriter;

import org.joda.time.Duration;
import org.joda.time.Instant;

public class PeriodicTraceCSVLoggerFactory implements TracerFactory {

	private class PeriodicTraceCSVLogger implements Tracer {
		private String staticPrefix;
		private Duration printDelay;
		private Instant nextPrint;
		private PrintWriter output;
		private int traceCount = 1;
		private String algorithmId;
		private int run;

		/**
		 * Make a new occasional printer
		 * 
		 * @param iterationsPerPrint
		 *            the number of iterations per print
		 * @param t
		 *            the trainer
		 */
		public PeriodicTraceCSVLogger(String staticPrefix, Duration printDelay, PrintWriter output, String algorithmId, int run) {
			this.staticPrefix = staticPrefix;
			this.printDelay = printDelay;
			// set it one millisecond ago so that first trace happens immediately
			this.nextPrint = new Instant().minus(Duration.millis(1)); 
			this.output = output;
			this.algorithmId = algorithmId;
			this.run = run;
			this.traceCount = 1;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see shared.Tracer#trace(int, double)
		 */
		@Override
		public synchronized void trace(int iteration, double optimalValue, double trainValue) {
			if (nextPrint.isBeforeNow()) {
				output.println(staticPrefix + algorithmId + "," + run + "," + traceCount + "," + iteration + "," + optimalValue + "," + trainValue);
				traceCount++;
				nextPrint = nextPrint.plus(printDelay);
			}
		}

	}

	private String staticPrefix;
	private Duration printDelay;
	private PrintWriter output;

	public PeriodicTraceCSVLoggerFactory(String staticPrefix, Duration printDelay, PrintWriter output) {
		this.staticPrefix = staticPrefix;
		this.printDelay = printDelay;
		this.output = output;
	}

	@Override
	public Tracer start(String algorithmId, int run) {
		return new PeriodicTraceCSVLogger(staticPrefix, printDelay, output, algorithmId, run);
	}
}