/*
 * IntervalBuilder.java
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

package myra.datamining;

import static myra.Config.CONFIG;

import myra.Config.ConfigKey;
import myra.datamining.Attribute.Condition;
import myra.datamining.Attribute.Type;
import myra.datamining.Dataset.Instance;

/**
 * The <code>IntervalBuilder</code> handles continuous attributes
 * discretisation.
 * 
 * @author Fernando Esteban Barril Otero
 */
public abstract class IntervalBuilder {
    /**
     * Config key for the default <code>IntervalBuilder</code> instance.
     */
    public final static ConfigKey<IntervalBuilder> DEFAULT_BUILDER =
	    new ConfigKey<>();

    /**
     * Returns the singleton <code>IntervalBuilder</code> instance.
     * 
     * @return the singleton <code>IntervalBuilder</code> instance.
     */
    public final static IntervalBuilder singleton() {
	return CONFIG.get(DEFAULT_BUILDER);
    }

    /**
     * Config key for the maximum limit for the minimum number of cases.
     */
    public final static ConfigKey<Integer> MAXIMUM_LIMIT = new ConfigKey<>();

    /**
     * Config key for the minimum number of cases of an interval.
     */
    public final static ConfigKey<Integer> MINIMUM_CASES = new ConfigKey<>();

    /**
     * Returns attribute conditions representing the discrete interval for the
     * specified attribute.
     * 
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the covered instances flags.
     * @param attribute
     *            the index of the continuous attribute.
     * 
     * @return attribute conditions representing discrete intervals for the
     *         specified attribute
     */
    public abstract Condition[] multiple(Dataset dataset,
					 Instance[] instances,
					 int attribute);

    /**
     * Returns an attribute condition representing a single discrete interval
     * for the specified attribute.
     * 
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the covered instances flags.
     * @param attribute
     *            the index of the continuous attribute.
     * 
     * @return an attribute condition representing a discrete interval for the
     *         specified attribute
     */
    public abstract Condition single(Dataset dataset,
				     Instance[] instances,
				     int attribute);

    /**
     * Returns the minimum number of examples that an interval must contain. The
     * minimum number is determined by the same rule as the C45 algorithm.
     * 
     * @param dataset
     *            the current dataset.
     * @param length
     *            the number of candidate threshold values.
     * 
     * @return the minimum number of examples that an interval must contain.
     */
    public static double minimumCases(Dataset dataset, double length) {
	double minimum = (dataset.getTarget().getType() == Type.NOMINAL
		? 0.1 * (length / (double) dataset.classLength())
		: CONFIG.get(MINIMUM_CASES));

	if (minimum < CONFIG.get(MINIMUM_CASES)) {
	    minimum = CONFIG.get(MINIMUM_CASES);
	} else if (minimum > CONFIG.get(MAXIMUM_LIMIT)) {
	    minimum = CONFIG.get(MAXIMUM_LIMIT);
	}

	return minimum;
    }

    /**
     * This class represents a (value,class) pair.
     */
    public static class Pair implements Comparable<Pair> {
	/**
	 * The value of the pair.
	 */
	public double value;

	/**
	 * The class value of the pair.
	 */
	public double classValue;

	/**
	 * The weight of the pair.
	 */
	public double weight;

	/**
	 * Compares this <code>Pair</code> with thespecified <code>Pair</code>
	 * value. The sign of the integer value returned is the same as that of
	 * the integer that would be returned by the call:
	 * 
	 * <pre>
	 * new Double(this.value).compareTo(new Double(o.value))
	 * </pre>
	 * 
	 * @param o
	 *            the <code>Pair</code> to compare against.
	 */
	public int compareTo(Pair o) {
	    return Double.compare(value, o.value);
	}

	@Override
	public String toString() {
	    return String.format("(v=%.6f, c=%.0f, w=%.4f)",
				 value,
				 classValue,
				 weight);
	}
    }
}