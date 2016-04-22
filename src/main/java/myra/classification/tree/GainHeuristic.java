/*
 * GainHeuristic.java
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

package myra.classification.tree;

import static myra.Config.CONFIG;
import static myra.classification.attribute.C45Split.EPSILON;
import static myra.datamining.Attribute.Type.CONTINUOUS;
import static myra.datamining.Attribute.Type.NOMINAL;
import static myra.datamining.Dataset.RULE_COVERED;
import static myra.datamining.IntervalBuilder.MINIMUM_CASES;

import java.util.Arrays;

import myra.datamining.Attribute;
import myra.datamining.Dataset;
import myra.datamining.IntervalBuilder;
import myra.datamining.Attribute.Condition;
import myra.datamining.Dataset.Instance;

/**
 * This class is responsible to calculate the heuristic information based on the
 * entropy for each term (vertex).
 * 
 * @author Fernando Esteban Barril Otero
 */
public class GainHeuristic implements Heuristic {
    /**
     * Computes the information gain of each attribute of the dataset.
     * 
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the covered instances flags.
     * 
     * @return the information gain of each attribute of the dataset.
     */
    public double[] compute(Dataset dataset,
			    Instance[] instances,
			    boolean[] used) {
	double[] gain = new double[dataset.attributes().length - 1];
	Arrays.fill(gain, -EPSILON);

	for (int i = 0; i < gain.length; i++) {
	    if (!used[i]) {
		Attribute attribute = dataset.attributes()[i];

		// nominal attributes
		if (attribute.getType() == NOMINAL
			&& i != dataset.classIndex()) {
		    processNominal(dataset, instances, attribute, gain);
		}
		// continuous attributes
		else if (attribute.getType() == CONTINUOUS) {
		    processContinuous(dataset, instances, attribute, gain);
		}
	    }
	}

	return gain;
    }

    /**
     * Computes the information gain for a continuous attribute.
     * 
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the covered instances flags.
     * @param attribute
     *            the continuous attribute.
     * @param gain
     *            the array of information gain values.
     */
    private void processContinuous(Dataset dataset,
				   Instance[] instances,
				   Attribute attribute,
				   double[] gain) {
	double[] distribution = new double[dataset.classLength()];
	// sum of the fractional weights of the all examples
	double length = 0;
	// sum of the fractional weights of the examples with known outcomes
	double size = 0;

	for (int i = 0; i < dataset.size(); i++) {
	    double w = instances[i].weight;

	    if (instances[i].flag == RULE_COVERED) {
		double v = dataset.value(i, attribute.getIndex());

		if (!Double.isNaN(v)) {
		    int index = (int) dataset.value(i, dataset.classIndex());
		    distribution[index] += w;
		    size += w;

		    index++;
		}

		length += w;
	    }
	}

	// info(T) (the information of the current attribute)

	double info = 0.0;

	for (int i = 0; i < distribution.length; i++) {
	    if (distribution[i] > 0) {
		double p = distribution[i] / size;
		info -= (p * log2(p));
	    }
	}

	Condition[] conditions = IntervalBuilder.singleton()
		.multiple(dataset, instances, attribute.getIndex());

	if (conditions == null) {
	    gain[attribute.getIndex()] = -EPSILON;
	} else {
	    // info_x(T) (the information given by the partitions)

	    double infoX = 0.0;

	    for (Condition c : conditions) {
		infoX += ((c.length / size) * c.entropy);
	    }

	    // the penalty term (log2(available) / length) is based on C4.5
	    // implementation

	    gain[attribute.getIndex()] = ((size / length) * (info - infoX))
		    - (log2(conditions[0].tries) / length);
	}
    }

    /**
     * Computes the information gain for a nominal attribute.
     * 
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the covered instances flags.
     * @param attribute
     *            the nominal attribute.
     * @param gain
     *            the array of information gain values.
     */
    private void processNominal(Dataset dataset,
				Instance[] instances,
				Attribute attribute,
				double[] gain) {
	// class distribution for each value of the attribute

	double[] terms = new double[attribute.size() * dataset.classLength()];
	double[] counter = new double[attribute.size()];

	double[] distribution = new double[dataset.classLength()];
	// sum of the fractional weights of the all examples
	double length = 0;
	// sum of the fractional weights of the examples with known outcomes
	double size = 0;

	for (int j = 0; j < dataset.size(); j++) {
	    if (instances[j].flag == RULE_COVERED) {
		double w = instances[j].weight;
		double v = dataset.value(j, attribute.getIndex());
		double k = dataset.value(j, dataset.classIndex());

		if ((v != Dataset.MISSING_VALUE_INDEX)) {
		    terms[(int) ((v * dataset.classLength()) + k)] += w;
		    counter[(int) v] += w;

		    distribution[(int) k] += w;
		    size += w;
		}

		length += w;
	    }
	}

	int valid = 0;

	for (int i = 0; i < counter.length; i++) {
	    if (counter[i] >= CONFIG.get(MINIMUM_CASES)) {
		valid++;
	    }
	}

	if (valid < 2) {
	    gain[attribute.getIndex()] = -EPSILON;
	} else {
	    // info(T) (the information of the current attribute)

	    double info = 0.0;

	    for (int j = 0; j < dataset.classLength(); j++) {
		if (distribution[j] > 0) {
		    double p = distribution[j] / size;
		    info -= (p * log2(p));
		}
	    }

	    // info_x(T) (the information given by the partitions)

	    double infoX = 0.0;

	    for (int j = 0; j < attribute.size(); j++) {
		if (counter[j] > 0) {
		    int index = j * dataset.classLength();
		    double entropy = 0;

		    for (int k = 0; k < dataset.classLength(); k++) {
			if (terms[index + k] > 0) {
			    double p = terms[index + k] / counter[j];
			    entropy -= (p * log2(p));
			}
		    }

		    infoX += ((counter[j] / size) * entropy);
		}
	    }

	    // gain

	    gain[attribute.getIndex()] = (size / length) * (info - infoX);
	}
    }

    /**
     * Returns the <i>base 2</i> logarithm of a <code>double</code> value.
     * 
     * @param value
     *            a value.
     * 
     * @return the <i>base 2</i> logarithm of <code>value</code>.
     */
    private double log2(double value) {
	return Math.log(value) / Math.log(2.0);
    }
}