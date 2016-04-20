/*
 * Dataset.java
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

package myra.data;

import static myra.Config.CONFIG;
import static myra.data.Attribute.Type.CONTINUOUS;
import static myra.data.Attribute.Type.NOMINAL;

import java.util.ArrayList;
import java.util.Arrays;

import myra.classification.Classifier;

/**
 * This class represents the data.
 * 
 * @author Fernando Esteban Barril Otero
 */
public final class Dataset {
    /**
     * The index representing a missing value.
     */
    public static final int MISSING_VALUE_INDEX = -1;

    /**
     * The string representing a missing value.
     */
    public static final String MISSING_VALUE = "?";

    /**
     * Flag indicating an uncovered instance.
     */
    public static final byte NOT_COVERED = 0;

    /**
     * Flag indicating an instance covered by a rule.
     */
    public static final byte RULE_COVERED = 1;

    /**
     * Flag indicating a previously covered instance.
     */
    public static final byte COVERED = 3;

    /**
     * The name of the dataset.
     */
    private String name;

    /**
     * The attributes of the dataset.
     */
    private Attribute[] attributes;

    /**
     * The instances of the dataset, represented as an array. Each instance has
     * the length of <code>attributes.length</code>.
     */
    private double[] instances;

    /**
     * Class frequency distribution (only valid for classificatino problems).
     */
    private double[] distribution;

    /**
     * Mean of the target values (only valid for regression problems).
     */
    private double mean;

    /**
     * Default constructor.
     */
    public Dataset() {
	attributes = new Attribute[0];
	instances = new double[0];
	distribution = new double[0];
	mean = 0;
    }

    /**
     * Returns the attribute of the dataset at the specified index.
     * 
     * @param index
     *            the attribute index.
     * 
     * @return the attribute of the dataset at the specified index.
     */
    public Attribute getAttribute(int index) {
	return attributes[index];
    }

    /**
     * Returns the attributes of the dataset.
     * 
     * @return the attributes of the dataset.
     */
    public Attribute[] attributes() {
	return attributes;
    }

    /**
     * Returns the attribute with the specified name.
     * 
     * @param name
     *            the attribute name.
     * 
     * @return the attribute with the specified name.
     */
    public Attribute findAttribute(String name) {
	for (Attribute attribute : attributes) {
	    if (attribute.getName().equals(name)) {
		return attribute;
	    }
	}

	throw new IllegalArgumentException("Attribute not found: " + name);
    }

    /**
     * Returns the dataset name.
     * 
     * @return the dataset name.
     */
    public String getName() {
	return name;
    }

    /**
     * Sets the name of the dataset.
     * 
     * @param name
     *            the name to set.
     */
    public void setName(String name) {
	this.name = name;
    }

    /**
     * Returns the number of instances in the dataset.
     * 
     * @return the number of instances in the dataset.
     */
    public final int size() {
	return (instances.length / attributes.length);
    }

    /**
     * Returns the number of instances in the dataset associated with the
     * specified class value.
     * 
     * @param value
     *            the class value.
     * 
     * @return the number of instances in the dataset associated with the
     *         specified class value.
     */
    public final int size(int value) {
	int count = 0;

	for (int i = 0; i < size(); i++) {
	    if (instances[(i * attributes.length) + classIndex()] == value) {
		count++;
	    }
	}

	return count;
    }

    /**
     * Adds an attribute to the dataset.
     * 
     * @param attribute
     *            the attribute to add.
     */
    public void add(Attribute attribute) {
	if (instances.length > 0) {
	    throw new IllegalStateException("Dataset metadata cannot"
		    + " change after adding instances.");
	}

	int index = attributes.length;
	attributes = Arrays.copyOf(attributes, attributes.length + 1);

	attributes[index] = attribute;
	attribute.setIndex(index);
    }

    /**
     * Adds an instance to the dataset.
     * 
     * @param values
     *            the values of the instance to add.
     */
    public void add(String[] values) {
	if (values.length != attributes.length) {
	    throw new IllegalArgumentException("Invalid instance length: "
		    + values.length + " (expected " + attributes.length + ")");
	}

	double[] instance = new double[attributes.length];

	for (int i = 0; i < attributes.length; i++) {
	    if (values[i].equals(MISSING_VALUE)) {
		switch (attributes[i].getType()) {
		case NOMINAL:
		    instance[i] = MISSING_VALUE_INDEX;
		    break;

		case CONTINUOUS:
		    instance[i] = Double.NaN;
		    break;
		}
	    } else if (attributes[i].getType() == CONTINUOUS) {
		instance[i] = Double.parseDouble(values[i]);
	    } else if (attributes[i].getType() == NOMINAL) {
		int index = MISSING_VALUE_INDEX;

		for (int j = 0; j < attributes[i].values().length; j++) {
		    if (values[i].equals(attributes[i].value(j))) {
			index = j;
			break;
		    }
		}

		instance[i] = index;
	    }
	}

	add(instance);
    }

    /**
     * Adds an instance to the dataset.
     * 
     * @param values
     *            the values of the instance to add.
     */
    public void add(double[] values) {
	if (values.length != attributes.length) {
	    throw new IllegalArgumentException("Invalid instance length: "
		    + values.length + " (expected " + attributes.length + ")");
	}

	int offset = instances.length;

	instances =
		Arrays.copyOf(instances, instances.length + attributes.length);

	System.arraycopy(values, 0, instances, offset, values.length);

	// increments the class distribution
	// (if dealing with a classification problem)
	if (attributes[classIndex()].getType() == NOMINAL) {
	    if (distribution.length == 0) {
		distribution = new double[classLength()];
	    }

	    distribution[(int) values[classIndex()]]++;
	}
	// increments the mean
	// (if dealing with a regression problem)
	else if (attributes[classIndex()].getType() == CONTINUOUS) {
	    mean += values[classIndex()];
	}
    }

    /**
     * Returns the values of the specified instance.
     * 
     * @param index
     *            the index of the instance.
     * 
     * @return the values of the specified instance.
     */
    public double[] get(int index) {
	double[] values = new double[attributes.length];
	System.arraycopy(instances,
			 index * values.length,
			 values,
			 0,
			 values.length);

	return values;
    }

    /**
     * Removes the specified instances from the dataset.
     * 
     * @param indexes
     *            the array of indexes to remove.
     * 
     * @return the size of the dataset after the removal of the instances.
     */
    public int remove(int... indexes) {
	double[] resized = new double[instances.length
		- (indexes.length * attributes.length)];

	int current = 0;
	int target = 0;

	for (int removed : indexes) {
	    if (attributes[classIndex()].getType() == NOMINAL) {
		// updates the class frequency
		distribution[(int) value(removed * attributes.length,
					 classIndex())]--;
	    } else if (attributes[classIndex()].getType() == CONTINUOUS) {
		// updates the mean
		mean -= distribution[(int) value(removed * attributes.length,
						 classIndex())];
	    }

	    if (current == removed) {
		current++;
	    } else {
		int length = (removed - current) * attributes.length;
		int source = current * attributes.length;

		System.arraycopy(instances, source, resized, target, length);

		target += length;
		current = removed + 1;
	    }
	}

	if (target != resized.length) {
	    int length = resized.length - target;
	    int source = current * attributes.length;

	    System.arraycopy(instances, source, resized, target, length);
	}

	this.instances = resized;

	return size();
    }

    /**
     * Returns the index of the majority class.
     * 
     * @param instances
     *            the instances array.
     * @param flag
     *            the type of instances to be considered (valid values are
     *            {@link #COVERED}, {@link #NOT_COVERED} and
     *            {@link #RULE_COVERED}).
     * 
     * @return the index of the majority class.
     */
    public int findMajority(Instance[] instances, byte flag) {
	int classIndex = classIndex();
	double[] frequencies =
		new double[attributes[classIndex].values().length];

	for (int i = 0; i < size(); i++) {
	    if (instances[i].flag == flag) {
		int index = (i * attributes.length) + classIndex;
		frequencies[(int) this.instances[index]] += instances[i].weight;
	    }
	}

	return findMajority(frequencies, findMajority(distribution, -1));
    }

    /**
     * Returns the index of the majority frequency of the distribution. In case
     * of a tie, the <code>bias</code> is selected if it is part of the
     * majority; otherwise a majority index is chosen at random.
     * 
     * @param values
     *            the frequency array.
     * @param bias
     *            the bias for the majority selection in case of a tie (
     *            <code>-1</code> for no bias).
     * 
     * @return the index of the majority frequency of the distribution.
     */
    private int findMajority(double[] values, int bias) {
	ArrayList<Integer> candidates = new ArrayList<>(values.length);
	int majority = -1;

	for (int i = 0; i < values.length; i++) {
	    if (majority == -1 || values[majority] < values[i]) {
		majority = i;
		// removes previous values
		candidates.clear();
		candidates.add(majority);
	    } else if (values[majority] == values[i]) {
		candidates.add(i);
	    }
	}

	if (candidates.size() > 1) {
	    if (candidates.contains(bias)) {
		majority = bias;
	    } else {
		majority =
			candidates.get(CONFIG.get(Classifier.RANDOM_GENERATOR)
				.nextInt(candidates.size()));
	    }
	}

	return majority;
    }

    /**
     * Returns the index of the class attribute.
     * 
     * @return the index of the class attribute.
     */
    public final int classIndex() {
	return attributes.length - 1;
    }

    /**
     * Returns the number of class values.
     * 
     * @return the number of class values.
     */
    public final int classLength() {
	if (attributes[classIndex()].getType() == Attribute.Type.NOMINAL) {
	    return attributes[classIndex()].values().length;
	}

	throw new UnsupportedOperationException("Invalid class attribute: "
		+ attributes[classIndex()].getType());
    }

    /**
     * Returns the class frequency distribution.
     * 
     * @param index
     *            the class value index.
     * 
     * @return the class frequency distribution.
     */
    public final int distribution(int index) {
	if (attributes[classIndex()].getType() == Attribute.Type.NOMINAL) {
	    return (int) distribution[index];
	}

	throw new UnsupportedOperationException("Invalid class attribute: "
		+ attributes[classIndex()].getType());
    }

    /**
     * Returns the mean of the target values across all instances of the
     * dataset.
     * 
     * @return the mean of the target values across all instances of the
     *         dataset.
     */
    public final double mean() {
	return mean / size();
    }

    /**
     * Returns <code>true</code> if the specified value represents a missing
     * value.
     * 
     * @param attribute
     *            the attribute object.
     * @param value
     *            the value to check.
     * 
     * @return <code>true</code> if the specified value represents a missing
     *         value; <code>false</code> otherwise.
     */
    public boolean isMissing(Attribute attribute, double value) {
	boolean missing = false;

	switch (attribute.getType()) {
	case CONTINUOUS:
	    missing = Double.isNaN(value);
	    break;

	case NOMINAL:
	    missing = (value == MISSING_VALUE_INDEX);
	    break;
	}

	return missing;
    }

    /**
     * Returns the attribute value of a given instance.
     * 
     * @param instance
     *            the instance index.
     * @param attribute
     *            the attribute index.
     * 
     * @return the attribute value of a given instance.
     */
    public double value(int instance, int attribute) {
	return instances[(instance * attributes.length) + attribute];
    }

    /**
     * Marks the current <code>RULE_COVERED</code> instances as
     * <code>COVERED</code>.
     * 
     * @param covered
     *            the instances array.
     * 
     * @return the number of available instances (<code>NOT_COVERED</code>
     *         instances).
     */
    public static int markCovered(Instance[] covered) {
	int available = 0;

	for (int j = 0; j < covered.length; j++) {
	    if (covered[j].flag == RULE_COVERED) {
		covered[j].flag = COVERED;
	    } else if (covered[j].flag == NOT_COVERED) {
		available++;
	    }
	}

	return available;
    }

    /**
     * Marks the current <code>RULE_COVERED</code> instances as
     * <code>COVERED</code> associated with the specified class value. Instances
     * that are covered but associated with a different class value are set as
     * <code>NOT_COVERED</code>.
     * 
     * @param dataset
     *            the current dataset.
     * @param covered
     *            the instances array.
     * @param predicted
     *            the predicted class value.
     * 
     * @return the number of correctly covered instances.
     */
    public static int markCorrect(Dataset dataset,
				  Instance[] covered,
				  int predicted) {
	int marked = 0;

	for (int j = 0; j < covered.length; j++) {
	    if (covered[j].flag == RULE_COVERED) {
		if (dataset.value(j, dataset.classIndex()) == predicted) {
		    covered[j].flag = COVERED;
		    marked++;
		} else {
		    covered[j].flag = NOT_COVERED;
		}
	    }
	}

	return marked;
    }

    /**
     * Returns a copy of the dataset including only the instances associated
     * with the specified flag.
     * 
     * @param dataset
     *            the dataset to copy.
     * @param covered
     *            the instance array information.
     * @param flag
     *            the instances flag.
     * 
     * @return a copy of the dataset.
     */
    public static Dataset filter(Dataset dataset,
				 Instance[] covered,
				 int flag) {
	Dataset clone = new Dataset();
	clone.attributes = dataset.attributes.clone();
	clone.name = dataset.name;

	for (int i = 0; i < dataset.size(); i++) {
	    if (covered[i].flag == flag) {
		int start = clone.instances.length;
		int length = dataset.attributes.length;

		clone.instances =
			Arrays.copyOf(clone.instances,
				      start + clone.attributes.length);

		System.arraycopy(dataset.instances,
				 (i * length),
				 clone.instances,
				 start,
				 length);
	    }
	}

	return clone;
    }

    /**
     * Struct-like class to hold the information about an instance.
     */
    public static final class Instance implements Cloneable {
	/**
	 * The weight of the instance.
	 */
	public double weight = 1.0;

	/**
	 * The coverage flag of the instance.
	 */
	public byte flag = NOT_COVERED;

	/**
	 * Default constructor.
	 */
	public Instance() {
	    this(1.0, NOT_COVERED);
	}

	/**
	 * Create a new <code>Instance</code>.
	 * 
	 * @param weight
	 *            the weight of the instance.
	 * @param flag
	 *            the covered flag of the instance.
	 */
	public Instance(double weight, byte flag) {
	    this.weight = weight;
	    this.flag = flag;
	}

	/**
	 * Returns a new instance array.
	 * 
	 * @param size
	 *            the size of the array.
	 * 
	 * @return a new instance array.
	 */
	public static Instance[] newArray(int size) {
	    Instance[] instances = new Instance[size];

	    for (int i = 0; i < size; i++) {
		instances[i] = new Instance();
	    }

	    return instances;
	}

	/**
	 * Returns a string representation of the instances' flags.
	 * 
	 * @param instances
	 *            the array of instances.
	 * 
	 * @return a string representation of the instances' flags.
	 */
	public static String toString(Instance[] instances) {
	    StringBuffer output = new StringBuffer();
	    output.append("[");

	    for (Instance instance : instances) {
		output.append(" ");
		output.append(instance.flag);
	    }

	    output.append(" ]");

	    return output.toString();
	}

	/**
	 * Returns a copy of the specified instance array.
	 * 
	 * @param instances
	 *            the instance array.
	 * 
	 * @return a copy of the specified instance array.
	 */
	public static Instance[] copyOf(Instance[] instances) {
	    Instance[] copy = new Instance[instances.length];

	    for (int i = 0; i < copy.length; i++) {
		copy[i] = new Instance(instances[i].weight, instances[i].flag);
	    }

	    return copy;
	}

	/**
	 * Sets the instances' flags to the specified flag.
	 * 
	 * @param instances
	 *            the instance array.
	 * @param flag
	 *            the flag to set.
	 */
	public static void markAll(Instance[] instances, byte flag) {
	    for (int i = 0; i < instances.length; i++) {
		instances[i].flag = flag;
	    }
	}

	/**
	 * Sets the <code>from</code> flags to the specified <code>to</code>
	 * flag.
	 * 
	 * @param instances
	 *            the instance array.
	 * @param from
	 *            the original flag.
	 * @param to
	 *            the flag to set.
	 */
	public static void mark(Instance[] instances, byte from, byte to) {
	    for (int i = 0; i < instances.length; i++) {
		if (instances[i].flag == from) {
		    instances[i].flag = to;
		}
	    }
	}
    }
}