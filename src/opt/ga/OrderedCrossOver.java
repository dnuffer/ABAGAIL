package opt.ga;

import dist.Distribution;

import shared.Instance;
import java.util.*;

/**
 * A single point cross over function
 * 
 * @author Andrew Guillory gtg008g@mail.gatech.edu
 * @version 1.0
 */
public class OrderedCrossOver implements CrossoverFunction {
	private Random rand;
	private int len;
	private int[] copied;
	private int mark;

	public OrderedCrossOver(int permlength) {
		len = permlength;
		rand = new Random();
		copied = new int[permlength];
		mark = 0;
	}

	/**
	 * @see opt.CrossOverFunction#mate(opt.OptimizationData,
	 *      opt.OptimizationData)
	 */
	public Instance mate(Instance a, Instance b) {
		double[] child = new double[len];

		// choose 2 random points
		int start = rand.nextInt(len);
		int end = rand.nextInt(len);
		if (start == end)
			end = (end + 1) % len;

		// copy between from first parent
		if (++mark == 0)
			copied = new int[len];
		for (int i = start; i != end; i = (i + 1) % len) {
			child[i] = a.getContinuous(i);
			copied[(int) child[i]] = mark;
		}

		// copy rest from second parent
		for (int i = 0, curs = end, chpos = end; i < len; i++, curs = (curs + 1) % len) {
			if (copied[b.getDiscrete(curs)] != mark) {
				child[chpos] = b.getDiscrete(curs);
				chpos = (chpos + 1) % len;
			}
		}

		// Map<Double, Double> mapping2 = new HashMap<Double, Double>(length *
		// 2);
		// for (int i = 0; i < length; i++)
		// {
		// int index = (i + point1) % a.size();
		// double item1 = a.getContinuous(index);
		// double item2 = b.getContinuous(index);
		// offspring1[index] = item2;
		// mapping2.put(item2, item1);
		// }
		//
		// checkUnmappedElements(offspring1, mapping2, point1, point2);
		return new Instance(child);
	}
	//
	// /**
	// * Checks elements that are outside of the partially mapped section to
	// * see if there are any duplicate items in the list. If there are, they
	// * are mapped appropriately.
	// */
	// private void checkUnmappedElements(double[] offspring,
	// Map<Double, Double> mapping,
	// int mappingStart,
	// int mappingEnd)
	// {
	// for (int i = 0; i < offspring.length; i++)
	// {
	// if (!isInsideMappedRegion(i, mappingStart, mappingEnd))
	// {
	// Double mapped = offspring[i];
	// while (mapping.containsKey(mapped))
	// {
	// mapped = mapping.get(mapped);
	// }
	// offspring[i] = mapped;
	// }
	// }
	// }
	//
	//
	// /**
	// * Checks whether a given list position is within the partially mapped
	// * region used for cross-over.
	// * @param position The list position to check.
	// * @param startPoint The starting index (inclusive) of the mapped region.
	// * @param endPoint The end index (exclusive) of the mapped region.
	// * @return True if the specified position is in the mapped region, false
	// * otherwise.
	// */
	// private boolean isInsideMappedRegion(int position,
	// int startPoint,
	// int endPoint)
	// {
	// boolean enclosed = (position < endPoint && position >= startPoint);
	// boolean wrapAround = (startPoint > endPoint && (position >= startPoint ||
	// position < endPoint));
	// return enclosed || wrapAround;
	// }

}