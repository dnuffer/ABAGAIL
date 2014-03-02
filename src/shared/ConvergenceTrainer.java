package shared;
/**
 * A convergence trainer trains a network
 * until convergence, using another trainer
 * @author Andrew Guillory gtg008g@mail.gatech.edu
 * @version 1.0
 */
public class ConvergenceTrainer implements Trainer {
    /** The default threshold */
    private static final double THRESHOLD = 1E-10;
    /** The maxium number of iterations */
    private static final int MAX_ITERATIONS = 500;
    private static final int BELOW_THRESHOLD_MAX_ITERATIONS = 100;

    /**
     * The trainer
     */
    private Trainer trainer;

    /**
     * The threshold
     */
    private double threshold;
    
    /**
     * The number of iterations trained
     */
    private int iterations;
    
    /**
     * The maximum number of iterations to use
     */
    private int maxIterations;
    
    /**
     * The maximum number of iterations where the change in error is < threshold
     */
    private int belowThresholdMaxIterations;
    
    /**
     * How long has training been below the threshold
     */
    private int belowThresholdIterations;

    /**
     * Create a new convergence trainer
     * @param trainer the thrainer to use
     * @param threshold the error threshold
     * @param maxIterations the maximum iterations
     */
    public ConvergenceTrainer(Trainer trainer, double threshold, int maxIterations, int belowThresholdMaxIterations) {
        this.trainer = trainer;
        this.threshold = threshold;
        this.maxIterations = maxIterations;
        this.belowThresholdMaxIterations = belowThresholdMaxIterations; 
    }
    

    /**
     * Create a new convergence trainer
     * @param trainer the trainer to use
     */
    public ConvergenceTrainer(Trainer trainer) {
        this(trainer, THRESHOLD, MAX_ITERATIONS, BELOW_THRESHOLD_MAX_ITERATIONS);
    }

    /**
     * @see Trainer#train()
     */
    public double train() {
        double lastError;
        double error = Double.MAX_VALUE;
        do {
           iterations++;
           lastError = error;
           error = trainer.train();
           
           if (iterations >= maxIterations)
        	   break;
           
           if (Math.abs(error - lastError) < threshold) {
               belowThresholdIterations++;
        	   if (belowThresholdIterations >= belowThresholdMaxIterations)
        		   break; // out of while loop
           } else {
               belowThresholdIterations = 0;
           }
           
        } while (true);
        return error;
    }
    
    /**
     * Get the number of iterations used
     * @return the number of iterations
     */
    public int getIterations() {
        return iterations;
    }
    

}
