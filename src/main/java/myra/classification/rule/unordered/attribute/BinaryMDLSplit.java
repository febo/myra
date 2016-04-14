/*
 * BinaryMDLSplit.java
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

package myra.classification.rule.unordered.attribute;

import static myra.data.Dataset.RULE_COVERED;

import java.util.Arrays;

import myra.classification.attribute.MDLSplit;
import myra.data.Attribute.Condition;
import myra.data.Dataset;
import myra.data.Dataset.Instance;
import myra.data.IntervalBuilder;

/**
 * This class creates discrete intervals based on minimum description length
 * (MDL) principle proposed by Fayyad and Irani, using a 1-against all approach.
 * Note that this class only implements the methods defined by
 * {@link ClassAwareSplit}.
 * 
 * @author Fernando Esteban Barril Otero
 * 
 * @see ClassAwareSplit
 */
public class BinaryMDLSplit extends MDLSplit implements ClassAwareSplit {
    @Override
    public Condition[] multiple(Dataset dataset,
				Instance[] instances,
				int attribute) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Condition single(Dataset dataset,
			    Instance[] instances,
			    int attribute) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Condition[] multiple(Dataset dataset,
				Instance[] instances,
				int attribute,
				int target) {
	Pair[] candidates = new Pair[dataset.size()];
	double[] frequency = new double[dataset.classLength()];

	int index = 0;
	double size = 0;

	for (int i = 0; i < dataset.size(); i++) {
	    // the dynamc discretisation only considers the instances covered
	    // by the current rule
	    if (instances[i].flag == RULE_COVERED) {
		double v = dataset.value(i, attribute);

		if (!Double.isNaN(v)) {
		    Pair pair = new Pair();
		    pair.value = v;
		    pair.classValue =
			    (dataset.value(i, dataset.classIndex()) == target)
				    ? 1 // positive (target)
				    : 0; // negative
		    pair.weight = instances[i].weight;
		    candidates[index] = pair;

		    size += pair.weight;

		    index++;
		}
	    }
	}

	if (index == 0) {
	    // there are no candidate threshold values
	    return null;
	}

	candidates = Arrays.copyOf(candidates, index);
	Arrays.sort(candidates);

	Condition[] conditions = create(candidates,
					0,
					candidates.length,
					frequency,
					size,
					IntervalBuilder.minimumCases(dataset,
								     size));

	if (conditions == null) {
	    // no interval was created
	    return null;
	} else {
	    for (Condition c : conditions) {
		c.attribute = attribute;
	    }

	    return conditions;
	}
    }

    @Override
    public Condition single(Dataset dataset,
			    Instance[] instances,
			    int attribute,
			    int target) {
	Condition[] conditions =
		multiple(dataset, instances, attribute, target);
	Condition best = null;
	double ratio = Double.MIN_VALUE;

	if (conditions != null && conditions.length > 0) {
	    for (Condition c : conditions) {
		// index 1 is the target (positive) value
		double r = c.frequency[target] / c.length;

		if (r > ratio) {
		    best = c;
		    ratio = r;
		} else if (r == ratio && c.length > best.length) {
		    best = c;
		}
	    }
	}

	return best;
    }
}