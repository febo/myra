/*
 * Variable.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2018 Fernando Esteban Barril Otero
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

package myra.rule.archive;

import static myra.datamining.Attribute.EQUAL_TO;
import static myra.datamining.Attribute.GREATER_THAN;
import static myra.datamining.Attribute.LESS_THAN_OR_EQUAL_TO;

import myra.datamining.Attribute.Condition;
import myra.datamining.VariableArchive;

/**
 * This class represents a term (variable) for the archive construction
 * procedure.
 * 
 * @author Fernando Esteban Barril Otero
 */
public abstract class Variable implements Cloneable {
    /**
     * Returns a condition to this variable. The condition is sampled from a
     * variable archive.
     * 
     * @return a condition to this variable.
     */
    public abstract Condition sample();

    /**
     * Adds the specified condition to the archive.
     * 
     * @param condition
     *            the condition to add.
     * @param quality
     *            the quality to be associated with the value.
     */
    public abstract void add(Condition condition, double quality);

    @Override
    public Variable clone() {
	try {
	    return (Variable) super.clone();
	} catch (CloneNotSupportedException e) {
	    throw new InternalError(e);
	}
    }

    /**
     * This class represents a continuous attribute term.
     */
    public static class Continuous extends Variable {
	/**
	 * Operator archive with 2 possible values (0=less than or equal to,
	 * 1=grater than).
	 */
	private VariableArchive.Categorical operator;

	/**
	 * Value archive.
	 */
	private VariableArchive.Continuous value;

	/**
	 * Default constructor.
	 * 
	 * @param lower
	 *            lower bound for the sampling procedure.
	 * @param upper
	 *            upper bound for the sampling procedure.
	 */
	public Continuous(double lower, double upper) {
	    operator = new VariableArchive.Categorical(2);
	    value = new VariableArchive.Continuous(lower, upper);
	}

	@Override
	public Condition sample() {
	    Condition condition = new Condition();

	    condition.relation =
		    (operator.sample() == 0) ? LESS_THAN_OR_EQUAL_TO
			    : GREATER_THAN;
	    condition.value[0] = value.sample();

	    return condition;
	}

	@Override
	public void add(Condition condition, double quality) {
	    // operator archive
	    operator.add(Integer.valueOf(condition.relation), quality);
	    operator.update();
	    // value archive
	    value.add(condition.value[0], quality);

	    operator.update();

	    value.update();
	}

	@Override
	public Continuous clone() {
	    Continuous clone = (Continuous) super.clone();
	    clone.operator = operator.clone();
	    clone.value = value.clone();
	    return clone;
	}
    }

    /**
     * This class represents a nominal attribute term.
     */
    public static class Nominal extends Variable {
	/**
	 * Value archive.
	 */
	private VariableArchive.Categorical value;

	/**
	 * Default constructor.
	 * 
	 * @param length
	 *            the number of different nominal values.
	 */
	public Nominal(int length) {
	    value = new VariableArchive.Categorical(length);
	}

	@Override
	public Condition sample() {
	    Condition condition = new Condition();
	    condition.relation = EQUAL_TO;
	    condition.value[0] = value.sample();

	    return condition;
	}

	@Override
	public void add(Condition condition, double quality) {
	    value.add(Integer.valueOf((int) condition.value[0]), quality);
	    value.update();
	}

	@Override
	public Nominal clone() {
	    Nominal clone = (Nominal) super.clone();
	    clone.value = value.clone();
	    return clone;
	}
    }
}