package opt.example;

import opt.EvaluationFunction;
import shared.Instance;

import java.awt.Polygon;
import java.awt.geom.*;

/**
 * An implementation of the traveling salesman problem
 * where the encoding used is a permutation of [0, ..., n]
 * where there are n+1 cities.  That is the encoding
 * is just the path to take.
 * @author Andrew Guillory gtg008g@mail.gatech.edu
 * @version 1.0
 */
public class MaximumAreaEvaluationFunction implements EvaluationFunction {

	double[][] points;
	int polySize;
	
    /**
     * Make a new route evaluation function
     * @param points the points of the cities
     */
    public MaximumAreaEvaluationFunction(double[][] points, int polySize) {
        this.points = points;
        this.polySize = polySize;
    }

    /**
     * @see opt.EvaluationFunction#value(opt.OptimizationData)
     */
    public double value(Instance d) {
    	double area = 0.0;
    	
    	for (int i = 0; i < polySize; i++) {
    		int j = (i + 1) % polySize;
    		int d_i = d.getDiscrete(i);
    		int d_j = d.getDiscrete(j);
			area += points[d_i][0] * points[d_j][1];
			area -= points[d_j][0] * points[d_i][1];
    	}
    	return Math.abs(area) / 2.0;
    }

    //    http://stackoverflow.com/questions/17579053/draw-polygon-and-calculate-area-in-java
}
