package opt.test;

public interface ResultHandler {
	public void handle(String shortName, int run, double optimalFitness, double seconds, int iterations, int bestIteration);
}