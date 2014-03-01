package opt;

import shared.Instance;

/**
 * A randomized hill climbing algorithm
 * 
 * @author Andrew Guillory gtg008g@mail.gatech.edu
 * @version 1.0
 */
public class RandomizedHillClimbingWithRestart extends OptimizationAlgorithm {

	/**
	 * The current optimization data
	 */
	private Instance cur;
	
	/**
	 * The best found so far
	 */
	private Instance best;
	private double bestVal;

	/**
	 * The current value of the data
	 */
	private double curVal;

	/**
	 * Limit on how many iterations before choosing a new random starting value
	 */
	private int maxIterationsWithoutRestart;

	/**
	 * The number of iterations without a restart
	 */
	private int iterationsWithoutRestart = 0;

	/**
	 * Make a new randomized hill climbing with a restart max iteration of 100
	 */
	public RandomizedHillClimbingWithRestart(HillClimbingProblem hcp) {
		this(hcp, 100);
	}

	/**
	 * Make a new randomized hill climbing with restart
	 */
	public RandomizedHillClimbingWithRestart(HillClimbingProblem hcp, int maxIterationsWithoutRestart) {
		super(hcp);
		best = cur = hcp.random();
		bestVal = curVal = hcp.value(cur);
		this.maxIterationsWithoutRestart = maxIterationsWithoutRestart;
	}

	/**
	 * @see shared.Trainer#train()
	 */
	public double train() {
		HillClimbingProblem hcp = (HillClimbingProblem) getOptimizationProblem();
		if (iterationsWithoutRestart < maxIterationsWithoutRestart) {
			Instance neigh = hcp.neighbor(cur);
			double neighVal = hcp.value(neigh);
			if (neighVal > curVal) {
				curVal = neighVal;
				cur = neigh;
			}
			iterationsWithoutRestart++;
		} else {
			iterationsWithoutRestart = 0;
			cur = hcp.random();
			curVal = hcp.value(cur);
		}

		if (curVal > bestVal) {
			best = cur;
			bestVal = curVal;
		}
		
		return curVal;
	}

	/**
	 * @see opt.OptimizationAlgorithm#getOptimalData()
	 */
	public Instance getOptimal() {
		return best;
	}

}
