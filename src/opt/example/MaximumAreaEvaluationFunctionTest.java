package opt.example;

import static org.junit.Assert.*;

import org.junit.Test;

import shared.Instance;
import util.linalg.DenseVector;
import static org.hamcrest.CoreMatchers.*;

public class MaximumAreaEvaluationFunctionTest {

	@Test
	public void value() {
		MaximumAreaEvaluationFunction sut = new MaximumAreaEvaluationFunction(new double[][] {{0,0}, {0,1}, {1,1}, {1,0}}, 4);
		assertThat(sut.value(new Instance(new DenseVector(new double[]{0,1,2,3}))), is(1.0));
		assertThat(sut.value(new Instance(new DenseVector(new double[]{3,2,1,0}))), is(1.0));
		assertThat(sut.value(new Instance(new DenseVector(new double[]{2,1,0,3}))), is(1.0));
		assertThat(sut.value(new Instance(new DenseVector(new double[]{1,0,3,2}))), is(1.0));
		assertThat(sut.value(new Instance(new DenseVector(new double[]{0,3,2,1}))), is(1.0));
	}
	
	@Test
	public void valueUsesPolySizePoints() {
		MaximumAreaEvaluationFunction sut = new MaximumAreaEvaluationFunction(new double[][] {{0,0}, {0,1}, {1,1}, {1,0}}, 3);
		assertThat(sut.value(new Instance(new DenseVector(new double[]{0,1,2}))), is(0.5));
	}

	@Test
	public void valueCrossing1() {
		MaximumAreaEvaluationFunction sut = new MaximumAreaEvaluationFunction(new double[][] {{0,0}, {0,1}, {1,1}, {1,0}}, 4);
		assertThat(sut.value(new Instance(new DenseVector(new double[]{0,2,1,3}))), is(0.0));
	}
	@Test
	public void valueCrossingThreeTriangles() {
		MaximumAreaEvaluationFunction sut = new MaximumAreaEvaluationFunction(new double[][] {{0,0}, {0,1}, {1,1}, {1,0}, {0.5,5}}, 5);
		assertThat(sut.value(new Instance(new DenseVector(new double[]{0,1,2,3,4}))), is(1.5));
		assertThat(sut.value(new Instance(new DenseVector(new double[]{1,2,3,4,0}))), is(1.5));
		assertThat(sut.value(new Instance(new DenseVector(new double[]{3,2,1,0,4}))), is(1.5));
		// house shaped - non-crossing
		assertThat(sut.value(new Instance(new DenseVector(new double[]{0,1,4,2,3}))), is(3.0));
	}
	@Test
	public void valueRepeated() {
		MaximumAreaEvaluationFunction sut = new MaximumAreaEvaluationFunction(new double[][] {{0,0}, {0,1}, {1,1}, {1,0}}, 4);
		assertThat(sut.value(new Instance(new DenseVector(new double[]{0,0,1,1}))), is(0.0));
	}
}
