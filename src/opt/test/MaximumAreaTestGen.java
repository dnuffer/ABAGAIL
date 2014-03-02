package opt.test;

import java.io.IOException;
import java.util.Random;

import shared.writer.CSVWriter;

/**
 * 
 * @author Andrew Guillory gtg008g@mail.gatech.edu
 * @version 1.0
 */
public class MaximumAreaTestGen {

	/**
	 * The test main
	 * 
	 * @param args
	 *            ignored
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Random random = new Random(5);
		int NUM_PROBLEMS = 5;

		for (int p = 1; p <= NUM_PROBLEMS; p++) {
			int N = p * 10;
			CSVWriter writer = new CSVWriter("MaximumAreaTest_" + N + ".csv", new String[]{"x", "y"});
			writer.open();
			for (int i = 0; i < N; i++) {
				writer.write(String.valueOf(random.nextDouble()));
				writer.write(String.valueOf(random.nextDouble()));
				writer.nextRecord();
			}
			writer.close();
		}

	}
}