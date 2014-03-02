package opt.ga;

import static org.junit.Assert.*;

import org.junit.Test;

import shared.Instance;
import util.linalg.DenseVector;
import static org.hamcrest.CoreMatchers.*;

public class OrderedCrossOverTest {

	@Test
	public void testMate() {
		OrderedCrossOver sut = new OrderedCrossOver(4);
		Instance parent1 = new Instance(new DenseVector(new double[]{0,1,2,3}));
		Instance parent2 = new Instance(new DenseVector(new double[]{3,2,1,0}));
		Instance child = sut.mate(parent1, parent2);
		assertThat(child.size(), is(4));
		int[] counts = new int[4];
		for (int i = 0; i < child.size(); i++) {
			counts[child.getDiscrete(i)]++;
		}
		assertThat(counts[0], is(1));
	}

}
