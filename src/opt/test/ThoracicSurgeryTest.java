package opt.test;

import dist.*;
import opt.*;
import opt.example.*;
import opt.ga.*;
import shared.*;
import func.nn.backprop.*;

import java.util.*;
import java.io.*;
import java.text.*;

/**
 * Implementation of randomized hill climbing, simulated annealing, and genetic algorithm to
 * find optimal weights to a neural network that is classifying Thoracic Surgery Survival. 
 *
 * @author Hannah Lau
 * @version 1.0
 */
public class ThoracicSurgeryTest {
    private static Instance[] instances = initializeInstances();

    private static int inputLayer = 24, hiddenLayer = 9, outputLayer = 1, trainingIterations = 100;
    private static BackPropagationNetworkFactory factory = new BackPropagationNetworkFactory();
    
    private static ErrorMeasure measure = new SumOfSquaresError();

    private static DataSet set = new DataSet(instances);

    private static int numAlgs = 5;
    private static int numRuns = 3;
    private static double classThreshold = 0.5;
    private static BackPropagationNetwork networks[] = new BackPropagationNetwork[numAlgs * numRuns];
    private static NeuralNetworkOptimizationProblem[] nnop = new NeuralNetworkOptimizationProblem[numAlgs * numRuns];

    private static OptimizationAlgorithm[] oa = new OptimizationAlgorithm[numAlgs * numRuns];
    private static String[] oaNames = {"RHC", "SA1E11_95", "SA1E11_99", "GA200_100_10", "GA200_190_10"};
    private static String results = "";

    private static DecimalFormat df = new DecimalFormat("0.000");

    public static void main(String[] args) throws FileNotFoundException {
    	PrintWriter traces_csv = new PrintWriter("ThoracicSurgeryUP_traces.csv");
    	traces_csv.print("Algorithm,Run,Iteration,Cost\n");

    	PrintWriter results_csv = new PrintWriter("ThoracicSurgeryUP_results.csv");
        results_csv.print("Algorithm,TruePositives,FalseNegatives,FalsePositives,TrueNegatives,Run,TrainingTime,TestingTime\n");
        
        for(int i = 0; i < oa.length; i++) {
            networks[i] = factory.createClassificationNetwork(
                new int[] {inputLayer, hiddenLayer, outputLayer});
            nnop[i] = new NeuralNetworkOptimizationProblem(set, networks[i], measure);
        }

        for(int i = 0; i < oa.length; i+=numAlgs) {
	        oa[i+0] = new RandomizedHillClimbing(nnop[i+0]);
	        oa[i+1] = new SimulatedAnnealing(1E11, .95, nnop[i+1]);
	        oa[i+2] = new SimulatedAnnealing(1E11, .99, nnop[i+2]);
	        oa[i+3] = new StandardGeneticAlgorithm(200, 100, 10, nnop[i+3]);
	        oa[i+4] = new StandardGeneticAlgorithm(200, 190, 10, nnop[i+4]);
        }
        
        for(int i = 0; i < oa.length; i++) {
            double start = System.nanoTime(), end, trainingTime, testingTime;//, correct = 0, incorrect = 0;
            int truePositives = 0;
            int falseNegatives = 0;
            int falsePositives = 0;
            int trueNegatives = 0;
            double finalError = train(oa[i], networks[i], oaNames[i % numAlgs], i, traces_csv); //trainer.train();
            end = System.nanoTime();
            trainingTime = end - start;
            trainingTime /= Math.pow(10,9);

            Instance optimalInstance = oa[i].getOptimal();
            networks[i].setWeights(optimalInstance.getData());

            double predicted, actual;
            start = System.nanoTime();
            for(int j = 0; j < instances.length; j++) {
                networks[i].setInputValues(instances[j].getData());
                networks[i].run();

                predicted = Double.parseDouble(instances[j].getLabel().toString());
                actual = Double.parseDouble(networks[i].getOutputValues().toString());

                if (actual >= classThreshold) {
                	if (predicted >= classThreshold)
                		truePositives++;
                	else
                		falseNegatives++;
                } else {
                	if (predicted < classThreshold)
                		trueNegatives++;
                	else
                		falsePositives++;
                }
            }
            end = System.nanoTime();
            testingTime = end - start;
            testingTime /= Math.pow(10,9);

            results_csv.print(oaNames[i % numAlgs] + "," + truePositives + "," + falseNegatives + "," + falsePositives + "," + trueNegatives + "," + i + "," + 
            			trainingTime + "," + testingTime + "\n");
            
             
            
            results +=  "\nResults for " + oaNames[i % numAlgs] +	": " + 
            			"\nFinal Error " + df.format(finalError) +
            			"\nTrue Positives " + truePositives + " instances." +
            			"\nFalse Negatives " + falseNegatives + " instances." +
                        "\nFalse Positives " + falsePositives + " instances." +
                        "\nTrue Negatives " + trueNegatives + " instances." +
            			"\nAccuracy: " + df.format(((double)truePositives + trueNegatives)/(truePositives + falseNegatives + falsePositives + trueNegatives)*100) + "%" +
                        "\nMCC: " + df.format(computeMCC(truePositives, falseNegatives, falsePositives, trueNegatives)) + 
                        "\nTraining time: " + df.format(trainingTime) + " seconds" + 
            			"\nTesting time: " + df.format(testingTime) + " seconds\n";
        }

        System.out.println(results);
        traces_csv.close();
        results_csv.close();
    }

	private static double computeMCC(double truePositives, double falseNegatives, double falsePositives, double trueNegatives) {
		double denom = Math.sqrt((truePositives + falsePositives) * (truePositives + falseNegatives) * (trueNegatives + falsePositives) * (trueNegatives + falseNegatives));
		if (denom == 0.0)
			denom = 1.0;
		return (truePositives * trueNegatives - falsePositives * falseNegatives) / denom;
	}

    private static double train(OptimizationAlgorithm oa, BackPropagationNetwork network, String oaName, int runNumber, PrintWriter traces_csv) {
        System.out.println("\nError results for " + oaName + "\n---------------------------");

        double error = 0;
        for(int i = 0; i < trainingIterations; i++) {
            oa.train();

            error = 0;
            for(int j = 0; j < instances.length; j++) {
                network.setInputValues(instances[j].getData());
                network.run();

                Instance output = instances[j].getLabel(), example = new Instance(network.getOutputValues());
                example.setLabel(new Instance(Double.parseDouble(network.getOutputValues().toString())));
                error += measure.value(output, example);
            }

            System.out.println(df.format(error));
            traces_csv.println(oaName + "," + runNumber + "," + i + "," + error);
        }
        return error;
    }

    private static Instance[] initializeInstances() {

        double[][][] attributes = new double[800][][];

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("src/opt/test/ThoracicSurgeryUPMM.csv")));

            // skip CSV column names
            br.readLine();
            
            for(int i = 0; i < attributes.length; i++) {
                Scanner scan = new Scanner(br.readLine());
                scan.useDelimiter(",");

                attributes[i] = new double[2][];
                attributes[i][0] = new double[24]; // 24 attributes
                attributes[i][1] = new double[1];

                for(int j = 0; j < 24; j++)
                    attributes[i][0][j] = Double.parseDouble(scan.next());

                attributes[i][1][0] = Double.parseDouble(scan.next());
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        Instance[] instances = new Instance[attributes.length];

        for(int i = 0; i < instances.length; i++) {
            instances[i] = new Instance(attributes[i][0]);
            instances[i].setLabel(new Instance(attributes[i][1][0]));
        }

        return instances;
    }
}
