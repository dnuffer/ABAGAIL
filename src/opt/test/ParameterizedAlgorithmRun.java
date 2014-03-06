package opt.test;

import java.io.FileNotFoundException;

import opt.OptimizationAlgorithm;
import shared.Instance;
import shared.Tracer;

public class ParameterizedAlgorithmRun extends OptimizationAlgorithm {
		ParameterizedAlgorithm pa;
		ProblemRun pr;
		public ParameterizedAlgorithmRun(ParameterizedAlgorithm pa, ProblemRun pr) {
			super(pa.oa.getOptimizationProblem());
			this.pa = pa;
			this.pr = pr;
		}
		
		public String getShortName() {
			return pa.oa.getShortName();
		}

		@Override
		public double train() {
			return pa.oa.train();
		}

		@Override
		public Instance getOptimal() {
			return pa.oa.getOptimal();
		}

		@Override
		public String getDescription() {
			return pa.oa.getDescription();
		}

		public double getOptimalFitnessValue() {
			return pa.ef.value(pa.oa.getOptimal());
		}

		private String getOptimalSolution() {
			return pa.ef.asString(pa.oa.getOptimal());
		}

		void recordRun(TrainResults results, double time) {
			if (pr.resultHandler != null) {
				pr.resultHandler.handle(getShortName(), pr.run, results.optimalFitness, time, results.iterations, results.bestIteration);
			}
		}

		TrainResults doTrain() {
			System.out.println("Starting " + getDescription());
//			TimeLimitTrainer fit = new TimeLimitTrainer(new OccasionalPrinter(this, Duration.millis(200), getOptimizationProblem()), Duration.standardSeconds(MaximumAreaTest.SECONDS), 
//					makeTracer());
//			double finalFitness = fit.train();
			
			int iterations = 0;
			double value = Double.MAX_VALUE;
			int bestIteration = 0;
			double best = Double.MIN_VALUE;
						
			TrainingTerminationState termination = pr.termination.start(pa);
			Tracer tracer = null;
			if (pr.tracerFactory != null) {
				tracer = pr.tracerFactory.start(pa.getShortName(), pr.run);
			}
			
			// never want less than 2 iterations
			while (termination.shouldContinue(this) || iterations < 2) {
				iterations++;
				value = train();
				
				if (tracer != null) {
					tracer.trace(iterations, getOptimalFitnessValue(), value);
				}
				
				if (value > best) {
					best = value;
					bestIteration = iterations;
				}
			}

			double finalFitness = value;
			
			System.out.println(getDescription() + " optimal fitness: " + getOptimalFitnessValue() + " final fitness: " + finalFitness);
			System.out.println("Best iteration: " + bestIteration);
			System.out.println("Optimal Solution:\n" + getOptimalSolution()); 
			return new TrainResults(finalFitness, getOptimalFitnessValue(), iterations, bestIteration);
		}

		TrainResults doTrainAndRecord() throws FileNotFoundException {
			double start = System.nanoTime();
			TrainResults results = doTrain();
			double end = System.nanoTime();
			double time = (end - start) / 10e9;
			recordRun(results, time);
			return results;
		}
		
	}