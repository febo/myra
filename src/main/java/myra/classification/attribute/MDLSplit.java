/*
 * MDLSplit.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2015 Fernando Esteban Barril Otero
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package myra.classification.attribute;

import static myra.classification.attribute.C45Split.PRECISION_10;
import static myra.datamining.Attribute.GREATER_THAN;
import static myra.datamining.Attribute.IN_RANGE;
import static myra.datamining.Attribute.LESS_THAN_OR_EQUAL_TO;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

import myra.datamining.Attribute.Condition;

/**
 * This class creates discrete intervals based on minimum description length
 * (MDL) principle proposed by Fayyad and Irani.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class MDLSplit extends AbstractEntropySplit {
    /**
     * The interval builder responsible to generate candidate intervals.
     */
    private AbstractEntropySplit builder;

    /**
     * Flag to indicate if the intervals considered must have the minimum number
     * of instances. If this is set to <code>false</code>, candidate intervals
     * smaller than the minimum can be created, although they will be filtered
     * out at the end.
     */
    private boolean strict;

    /**
     * Creates a new <code>MDLInterval</code> using the {@link BoundarySplit}
     * as the interval builder.
     */
    public MDLSplit() {
	this(new BoundarySplit());
    }

    /**
     * Creates a new <code>MDLInterval</code> using the specified interval
     * builder. Note that the interval builder must be a binary builder &mdash;
     * create only 2 conditions.
     *
     * @param builder
     *            the interval builder responsible to create the split.
     */
    public MDLSplit(AbstractEntropySplit builder) {
	this(builder, true);
    }

    /**
     * Creates a new <code>MDLInterval</code> using the specified interval
     * builder. Note that the interval builder must be a binary builder &mdash;
     * create only 2 conditions.
     *
     * @param builder
     *            the interval builder responsible to create the split.
     * @param strict
     *            indicated whether the interval minimum size is strictly
     *            respected during construction or not.
     */
    public MDLSplit(AbstractEntropySplit builder, boolean strict) {
	this.builder = builder;
	this.strict = strict;
    }

    @Override
    protected Condition[] create(Pair[] candidates,
				 int start,
				 int end,
				 double[] frequency,
				 double size,
				 double minimum) {
	LinkedList<Condition> conditions = new LinkedList<>();

	int created =
		mdl(conditions,
		    candidates,
		    start,
		    end,
		    frequency,
		    size,
		    minimum,
		    true);

	if (conditions.size() != created) {
	    throw new IllegalStateException(String
		    .format("Invalid number of intervals: expected %d, found %d.",
			    created,
			    conditions.size()));
	}

	// if the size of the intervals has been relaxed, filters only the
	// intervals that satisfy the minimum size
	if (!strict) {
	    for (Iterator<Condition> i = conditions.iterator(); i.hasNext();) {
		Condition c = i.next();

		if (c.length + PRECISION_10 < minimum) {
		    i.remove();
		    created--;
		}
	    }
	}

	Condition[] sorted = conditions.toArray(new Condition[created]);
	Arrays.sort(sorted, new Comparator<Condition>() {
	    @Override
	    public int compare(Condition o1, Condition o2) {
		if (o1.threshold[0] < o2.threshold[0]) {
		    return -1;
		} else if (o1.threshold[0] > o2.threshold[0]) {
		    return 1;
		}

		return 0;
	    }
	});

	for (Condition c : sorted) {
	    // sanity check
	    if (c.length + PRECISION_10 < minimum) {
		throw new IllegalStateException("Invalid interval size: "
			+ c.length);
	    }

	    if (c.threshold[0] == 0 && (c.threshold[1] + 1) != size) {
		c.relation = LESS_THAN_OR_EQUAL_TO;

		int t = (int) c.threshold[1];
		c.threshold[0] = candidates[t].value;
		c.value[0] =
			(candidates[t].value + candidates[t + 1].value) / 2.0;

		c.threshold[1] = 0;
		c.value[1] = 0;
	    } else if ((c.threshold[1] + 1) == size) {
		c.relation = GREATER_THAN;

		int t = (int) c.threshold[0];
		c.threshold[0] = candidates[t].value;
		c.value[0] =
			(candidates[t].value + candidates[t + 1].value) / 2.0;

		c.threshold[1] = 0;
		c.value[1] = 0;
	    } else {
		c.relation = IN_RANGE;

		int t = (int) c.threshold[0];
		c.threshold[0] = candidates[t].value;
		c.value[0] =
			(candidates[t].value + candidates[t + 1].value) / 2.0;

		t = (int) c.threshold[1];
		c.threshold[1] = candidates[t].value;
		c.value[1] =
			(candidates[t].value + candidates[t + 1].value) / 2.0;
	    }
	}

	return sorted;
    }

    private int mdl(LinkedList<Condition> conditions,
		    Pair[] candidates,
		    int start,
		    int end,
		    double[] frequency,
		    double size,
		    double minimum,
		    boolean first) {
	Condition[] split =
		builder.create(candidates,
			       start,
			       end,
			       frequency.clone(),
			       size,
			       minimum);

	int lowerCreated = 0;
	int upperCreated = 0;

	if (split != null && split.length == 2) {
	    double entropy = 0.0;
	    int diversity = 0;

	    for (int i = 0; i < frequency.length; i++) {
		if (frequency[i] > 0) {
		    double p = frequency[i] / size;
		    entropy -= (p * (Math.log(p) / Math.log(2.0)));

		    diversity++;
		}
	    }

	    double bits =
		    log2(Math.pow(3, diversity) - 2) - ((diversity * entropy)
			    - (split[0].diversity * split[0].entropy)
			    - (split[1].diversity * split[1].entropy));

	    double gain =
		    entropy - ((split[0].length / size) * split[0].entropy)
			    - ((split[1].length / size) * split[1].entropy);

	    // MDL criterion
	    if (first || gain > ((log2(size - 1) / size) + (bits / size))) {
		double limit = (strict ? 2 * minimum : minimum + 1);

		if (split[0].entropy > 0 && split[0].length >= limit) {
		    lowerCreated += mdl(conditions,
					candidates,
					start,
					(int) split[0].index + 1,
					split[0].frequency,
					split[0].length,
					minimum,
					false);
		}

		if (split[1].entropy > 0 && split[1].length >= limit) {
		    upperCreated += mdl(conditions,
					candidates,
					(int) split[1].index + 1,
					end,
					split[1].frequency,
					split[1].length,
					minimum,
					false);
		}

		if (lowerCreated == 0) {
		    Condition condition = new Condition();
		    condition.entropy = split[0].entropy;
		    condition.threshold[0] = (start == 0 ? 0 : start - 1);
		    condition.threshold[1] = split[0].index;
		    condition.length = split[0].length;
		    condition.frequency = split[0].frequency;

		    conditions.add(condition);

		    lowerCreated = 1;
		}

		if (upperCreated == 0) {
		    Condition condition = new Condition();
		    condition.entropy = split[1].entropy;
		    condition.threshold[0] = split[1].index;
		    condition.threshold[1] = (end - 1);
		    condition.length = split[1].length;
		    condition.frequency = split[1].frequency;

		    conditions.add(condition);

		    upperCreated = 1;
		}
	    }
	}

	if (first) {
	    for (Condition c : conditions) {
		c.tries = split[0].tries;
	    }
	}

	return lowerCreated + upperCreated;
    }
}