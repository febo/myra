/*
 * Rule.java
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

package myra.rule;

import static myra.Config.CONFIG;

import java.util.Arrays;

import myra.Config.ConfigKey;
import myra.Cost;
import myra.Weighable;
import myra.datamining.Attribute;
import myra.datamining.Attribute.Condition;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.datamining.Prediction;
import myra.util.ObjectFactory;

/**
 * This class represents a classification rule.
 * 
 * @author Fernando Esteban Barril Otero
 */
public abstract class Rule implements Weighable<Rule> {
    /**
     * The config key for the default rule implementation class.
     */
    public static final ConfigKey<Class<? extends Rule>> DEFAULT_RULE =
	    new ConfigKey<>();

    /**
     * The quality (cost) of the rule during training.
     */
    protected Cost quality;

    /**
     * The list of terms in the antecedent of the rule.
     */
    protected Term[] terms;

    /**
     * The number of active terms.
     */
    protected int size;

    /**
     * The index of the function that evaluated the rule.
     */
    protected int function;

    /**
     * Indicates if the rule is enabled or not.
     */
    protected boolean enabled;
    
    /**
     * The solution weight.
     */
    private double weight;

    /**
     * Default constructor.
     */
    public Rule() {
	this(0);
    }

    /**
     * Creates a new <code>Rule</code> with the specified capacity.
     * 
     * @param capacity
     *            the allocated size of the rule.
     */
    public Rule(int capacity) {
	terms = new Term[capacity];
	size = 0;
	function = -1;
	enabled = true;
    }

    /**
     * Returns a new <code>Rule</code> object using the default class
     * implementation.
     * 
     * @return a new <code>Rule</code> object.
     * 
     * @see #DEFAULT_RULE
     */
    public static Rule newInstance() {
	return ObjectFactory.create(CONFIG.get(DEFAULT_RULE));
    }

    /**
     * Returns a new <code>Rule</code> object with the specified capacity using
     * the default class implementation.
     * 
     * @param capacity
     *            the allocated size of the rule.
     * 
     * @return a new <code>Rule</code> object.
     * 
     * @see #DEFAULT_RULE
     */
    public static Rule newInstance(int capacity) {
	return ObjectFactory.create(CONFIG.get(DEFAULT_RULE),
				    new Class<?>[] { int.class },
				    new Object[] { capacity });
    }

    /**
     * Adds a term to the rule.
     * 
     * @param vertex
     *            the vertex index.
     * @param condition
     *            the attribute condition; <code>null</code> in case of nominal
     *            attributes.
     */
    public void add(int vertex, Condition condition) {
	add(new Term(vertex, condition));
    }

    /**
     * Adds a term to the rule.
     * 
     * @param term
     *            the term to be added.
     */
    public void add(Term term) {
	if (terms.length == size) {
	    terms = Arrays.copyOf(terms, size + 1);
	}

	terms[size] = term;
	size++;
    }

    /**
     * Sets a term at the specified index.
     * 
     * @param index
     *            the term index.
     * @param vertex
     *            the vertex index.
     * @param condition
     *            the attribute condition; <code>null</code> in case of nominal
     *            attributes.
     */
    public void set(int index, int vertex, Condition condition) {
	if (index < size) {
	    terms[index] = new Term(vertex, condition);
	} else {
	    throw new IllegalArgumentException("Invalid term index: " + index);
	}
    }

    /**
     * Returns <code>true</code> if the rule is enabled.
     * 
     * @return <code>true</code> if the rule is enabled.
     */
    public boolean isEnabled() {
	return enabled;
    }

    /**
     * Sets the enabled flag.
     * 
     * @param enabled
     *            the flag to set.
     */
    public void setEnabled(boolean enabled) {
	this.enabled = enabled;
    }

    /**
     * Removes the last term of the antecedent of the rule.
     * 
     * @return the last term of the antecedent of the rule.
     */
    public Term pop() {
	size--;
	return terms[size];
    }

    /**
     * Adds a term to the end of the antecedent of the rule.
     * 
     * @param term
     *            the term to add.
     */
    public void push(Term term) {
	if (size < terms.length) {
	    terms[size] = term;
	    size++;
	} else {
	    add(term);
	}
    }

    /**
     * Resizes the capacity of the rule to match the size of the rule. This
     * method will also remove any term that has been disabled.
     * 
     * @see Term#setEnabeld(boolean)
     */
    public void compact() {
	if (terms.length != size) {
	    terms = Arrays.copyOf(terms, size);
	}

	int index = 0;

	for (int i = 0; i < terms.length; i++) {
	    if (terms[i].enabeld) {
		if (index != i) {
		    terms[index] = terms[i];
		}

		index++;
	    }
	}

	if (index != size) {
	    size = index;
	    terms = Arrays.copyOf(terms, size);
	}
    }

    /**
     * Checks if the specified instance satisfies the antecedent of the rule.
     * 
     * @param dataset
     *            the current dataset.
     * @param instance
     *            the instance index.
     * 
     * @return <code>true</code> if the rule covers the instance; otherwise
     *         <code>false</code>.
     */
    public boolean covers(Dataset dataset, int instance) {
	boolean covered = true;

	for (int i = 0; i < size; i++) {
	    if (terms[i].isEnabeld()) {
		Condition condition = terms[i].condition();
		double v = dataset.value(instance, condition.attribute);

		if (!condition.satisfies(v)) {
		    covered = false;
		    break;
		}
	    }
	}

	return covered;
    }

    /**
     * Returns the number of terms in the antecedent of the rule.
     * 
     * @return the number of terms in the antecedent of the rule.
     */
    public int size() {
	return size;
    }

    /**
     * Returns <code>true</code> if the antecedent of the rule is empty.
     * 
     * @return <code>true</code> if the antecedent of the rule is empty;
     *         <code>false</code> otherwise.
     */
    public boolean isEmpty() {
	return size() == 0;
    }

    /**
     * Returns the array of terms of this rule.
     * 
     * @return the array of terms of this rule.
     */
    public Term[] terms() {
	return terms;
    }

    /**
     * Applies the rule and returns the number of covered instances. Only
     * instances that have not been previously covered are considered.
     * 
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the covered instances flag.
     * 
     * @return the number of covered instances by the rule.
     */
    public abstract int apply(Dataset dataset, Instance[] instances);

    /**
     * Sets the predicted value.
     * 
     * @param prediction
     *            the value to set.
     */
    public abstract void setConsequent(Prediction prediction);

    /**
     * Returns the predicted value.
     * 
     * @return the predicted value.
     */
    public abstract Prediction getConsequent();

    /**
     * Returns the function that evaluated the rule.
     * 
     * @return the function that evaluated the rule.
     */
    public int getFunction() {
	return function;
    }

    /**
     * Returns the quality of the rule.
     * 
     * @return the quality of the rule.
     */
    public Cost getQuality() {
	return quality;
    }

    /**
     * Sets the function that evaluated the rule.
     * 
     * @param function
     *            the function index to set.
     */
    public void setFunction(int function) {
	this.function = function;
    }

    /**
     * Sets the quality of the rule.
     * 
     * @param quality
     *            the quality to set.
     */
    public void setQuality(Cost quality) {
	this.quality = quality;
    }

    /**
     * Compares this rule with the specified rule. The first criteria is the
     * quality; in the case that both rules have the same quality, their are
     * compared in terms of size (occam's razor criteria).
     */
    @Override
    public int compareTo(Rule o) {
	int c = (quality == null ? 0 : quality.compareTo(o.quality));

	if (c == 0) {
	    c = Double.compare(o.size(), size());
	}

	return c;
    }

    /**
     * Returns <code>true</code> if the rule covers diverse instances.
     * 
     * @return <code>true</code> if the rule covers diverse instances;
     *         <code>flase</code> otherwise.
     */
    public abstract boolean isDiverse();

    /**
     * Substitutes continuous attributes' threshold values with values that
     * occur in the dataset.
     * 
     * @param dataset
     *            the current dataset.
     */
    public void fixThresholds(Dataset dataset) {
	for (int i = 0; i < size; i++) {
	    Condition c = terms[i].condition();

	    if (c != null) {
		// if a condition was created, we substitute the threshold
		// values with values that occur in the dataset (this is to
		// avoid having threshold values that don't represent values
		// from the dataset)

		for (int j = 0; j < dataset.size(); j++) {
		    double v = dataset.value(j, c.attribute);

		    for (int k = 0; k < c.value.length; k++) {
			if (v <= c.value[k] && v > c.threshold[k]) {
			    c.threshold[k] = v;
			}
		    }
		}

		// at the end of this procedure, the threshold ad value
		// should be the same
		for (int k = 0; k < c.value.length; k++) {
		    c.value[k] = c.threshold[k];
		}
	    }
	}
    }

    /**
     * Returns the string representation of the rule.
     * 
     * @param dataset
     *            the current dataset.
     * 
     * @return the string representation of the rule.
     */
    public String toString(Dataset dataset) {
	StringBuffer buffer = new StringBuffer();
	buffer.append("IF ");

	if (size == 0) {
	    buffer.append("<empty>");
	} else {
	    for (int i = 0; i < size; i++) {
		if (!terms[i].isEnabeld()) {
		    throw new IllegalStateException("A rule should not contain disabled terms.");
		}

		if (i > 0) {
		    buffer.append(" AND ");
		}

		Condition condition = terms[i].condition();
		buffer.append(condition.toString(dataset));
	    }
	}

	buffer.append(" THEN ");

	if (getConsequent() == null) {
	    buffer.append("<undefined>");
	} else {
	    Attribute target = dataset.attributes()[dataset.classIndex()];
	    buffer.append(getConsequent().toString(target));
	}

	return buffer.toString();
    }
    
    @Override
    public double getWeight() {
        return weight;
    }
    
    @Override
    public void setWeight(double weight) {
	this.weight = weight;
    }

    /**
     * This (struct-like) class represents a rule term.
     */
    public static class Term {
	/**
	 * The vertex index.
	 */
	private int index;

	/**
	 * The attribute condition.
	 */
	private Condition condition;

	/**
	 * Flag to indicate if the term is active or not.
	 */
	private boolean enabeld;

	/**
	 * Creates a <code>Term</code>.
	 * 
	 * @param index
	 *            the vertex index.
	 */
	public Term(int index) {
	    this(index, null);
	}

	/**
	 * Creates a <code>Term</code>.
	 * 
	 * @param index
	 *            the vertex index.
	 * @param condition
	 *            the attribute condition.
	 */
	public Term(int index, Condition condition) {
	    this.index = index;
	    this.condition = condition;
	    this.enabeld = true;
	}

	/**
	 * Returns the vertex index.
	 * 
	 * @return the vertex index.
	 */
	public int index() {
	    return index;
	}

	/**
	 * Returns the attribute condition.
	 * 
	 * @return the attribute condition.
	 */
	public Condition condition() {
	    return condition;
	}

	/**
	 * Returns <code>true</code> if the term is enabled.
	 * 
	 * @return <code>true</code> if the term is enabled; <code>false</code>
	 *         otherwise.
	 */
	public boolean isEnabeld() {
	    return enabeld;
	}

	/**
	 * Sets the <code>enabled</code> flag. Note that setting the flag to
	 * <code>false</code> does not remove the term from the antecedent, only
	 * prevents its evaluation in
	 * {@link Rule#apply(Dataset, Dataset.Instance[])}. When the method
	 * {@link Rule#compact()} is called, any term that has the flag set to
	 * <code>false</code> will be removed from the antecedent.
	 * 
	 * @param enabeld
	 *            the flag to set.
	 */
	public void setEnabeld(boolean enabeld) {
	    this.enabeld = enabeld;
	}
    }
}