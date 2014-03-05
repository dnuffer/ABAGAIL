package opt.test;

class TrainResults {

	public TrainResults(double finalFitness, double optimalFitness, int iterations, int bestIteration) {
		this.finalFitness = finalFitness;
		this.optimalFitness = optimalFitness;
		this.iterations = iterations;
		this.bestIteration = bestIteration;
	}

	public double finalFitness;
	public double optimalFitness;
	public int iterations;
	public int bestIteration;
}