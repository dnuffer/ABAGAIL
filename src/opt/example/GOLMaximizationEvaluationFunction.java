package opt.example;

import opt.EvaluationFunction;
import shared.Instance;

public class GOLMaximizationEvaluationFunction implements EvaluationFunction {
	int width;
	int height;
	int iterations;
    public GOLMaximizationEvaluationFunction(int width, int height, int iterations) {
		this.width = width;
		this.height = height;
		this.iterations = iterations;
	}
    
	public double value(Instance d) {
    	GOL gol = golFromInstance(d);

    	int beforeLivingCells = 0;
    	for (int i = 0; i < width; i++) {
    		for (int j = 0; j < height; j++) {
    			beforeLivingCells += gol.getCell(i, j) ? 1 : 0;
    		}
    	}
    	
    	for (int iter = 0; iter < this.iterations; iter++) {
    		gol.next();
    	}
    	
    	int afterLivingCells = 0;
    	for (int i = 0; i < width; i++) {
    		for (int j = 0; j < height; j++) {
    			afterLivingCells += gol.getCell(i, j) ? 1 : 0;
    		}
    	}
    	
    	return afterLivingCells - beforeLivingCells;
    	//return afterLivingCells;
    }

	private GOL golFromInstance(Instance d) {
		boolean[][] board = new boolean[width][height];
    	for (int i = 0; i < width; i++) {
    		for (int j = 0; j < height; j++) {
    			board[i][j] = d.getDiscrete(i * height + j) != 0;
    		}
    	}
    	
    	GOL gol = new GOL(board);
		return gol;
	}
	
	@Override
	public String asString(Instance optimal) {
		GOL gol = golFromInstance(optimal);
		String golBefore = gol.toString();
    	for (int iter = 0; iter < this.iterations; iter++) {
    		gol.next();
    	}
		String golAfter = gol.toString();

		return "GOL[" + width + "x" + height + "/" + iterations + "] Start:\n" + golBefore + "\nFinal:\n" + golAfter;
	}

}
