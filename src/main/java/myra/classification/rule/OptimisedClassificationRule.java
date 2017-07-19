/*
 * OptimisedClassificationRule.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2017 Fernando Esteban Barril Otero
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

package myra.classification.rule;

import static myra.datamining.Dataset.COVERED;
import static myra.datamining.Dataset.NOT_COVERED;
import static myra.datamining.Dataset.RULE_COVERED;

import java.util.Arrays;

import myra.datamining.Attribute;
import myra.datamining.Dataset;
import myra.datamining.Attribute.Condition;
import myra.datamining.Dataset.Instance;
import myra.rule.Rule.Term;

/**
 * @author amh58
 *
 */
public class OptimisedClassificationRule extends ClassificationRule {

	/**
    * The class distribution of the covered examples for each term
    */
	private int[][] covered;

   /**
    * The class distribution of the uncovered examples for each term
    */
	private int[][] uncovered;
	
	/**
     * Creates a new <code>ClassificationRule</code>.
     */
    public OptimisedClassificationRule() {
	this(0);
    }

    /**
     * Creates a new <code>ClassificationRule</code> with the specified
     * capacity.
     * 
     * @param capacity
     *            the allocated size of the rule.
     */
    public OptimisedClassificationRule(int capacity) {
	super(capacity);
	covered = new int[0][];
	uncovered = new int[0][];
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
    @Override
    public int apply(Dataset dataset, Instance[] instances) {
	
    int total = 0;
    int termslength = size();
    // extend the covered terms to extra size
    if(covered.length <= termslength){
    	covered = Arrays.copyOf(covered, termslength + 1);
    	uncovered = Arrays.copyOf(uncovered, termslength + 1);	
    	covered[termslength] = new int[0];
    	uncovered[termslength] = new int[0]; 
    }
    // copy arrays
	covered[termslength] = Arrays.copyOf(covered[termslength], dataset.classLength());
	uncovered[termslength] = Arrays.copyOf(uncovered[termslength], dataset.classLength());
	
	Arrays.fill(covered[termslength], 0);
	Arrays.fill(uncovered[termslength], 0);

    for (int i = 0; i < dataset.size(); i++) {
	    if (instances[i].flag != COVERED) {
			if (covers(dataset, i)) {
			    total++;
			    covered[termslength][(int) dataset.value(i, dataset.classIndex())]++;
			    instances[i].flag = RULE_COVERED;
			} else {
			    uncovered[termslength][(int) dataset.value(i, dataset.classIndex())]++;
			    instances[i].flag = NOT_COVERED;
			}
	    }
	}

	return total;
    }
    
    
    /**
     * Returns the number of different class values of the covered instances.
     * 
     * @return the number of different class values of the covered instances.
     */
    @Override
    public int diversity() {
	int diverse = 0;
	int termslength = size();
	for (int i = 0; i < covered[termslength].length; i++) {
	    if (covered[termslength][i] > 0) {
		diverse++;
	    }
	}

	if (diverse == 0) {
	    throw new IllegalStateException("Covered information empty.");
	}

	return diverse;
    }
    
    /**
     * Removes the last term of the antecedent of the rule.
     * 
     * @return the last term of the antecedent of the rule.
     */
    @Override
    public Term pop() {
	int termslength = size();
	covered = Arrays.copyOf(covered, termslength);
	uncovered = Arrays.copyOf(uncovered, termslength);
	return super.pop();
    }
    
    
    /**
     * Returns the class distribution of the covered examples.
     * 
     * @return the class distribution of the covered examples.
     */
    public int[] covered() {
    int termslength = size();
	return covered[termslength];
    }

    /**
     * Returns the class distribution of the uncovered examples.
     * 
     * @return the class distribution of the uncovered examples.
     */
    public int[] uncovered() {
    int termslength = size();
	return uncovered[termslength];
    }
    
    
    
    public String toString(Dataset dataset) {
    	StringBuffer buffer = new StringBuffer();
    	buffer.append("IF ");

    	if (size() == 0) {
    	    buffer.append("<empty>");
    	} else {
    	    for (int i = 0; i < size(); i++) {
    		if (!terms()[i].isEnabeld()) {
    		    throw new IllegalStateException("A rule should not contain disabled terms.");
    		}

    		if (i > 0) {
    		    buffer.append(" AND ");
    		}

    		Condition condition = terms()[i].condition();
    		buffer.append(condition.toString(dataset));
    	    }
    	}

    	buffer.append(" THEN ");

    	if (getConsequent() == null) {
    	    buffer.append("<undefined>");
    	} else {
    	    Attribute target = dataset.attributes()[dataset.classIndex()];
    	    buffer.append(getConsequent().toString(target));
    	    
    	      buffer.append(" (");
    	      
    	      for (int i = 0; i < dataset.classLength(); i++) { if (i > 0) {
    	      buffer.append(","); }
    	      
    	      buffer.append(covered()[i]); }
    	      
    	      buffer.append(")");
    	      
    	      buffer.append(String.format(" Q  = %f",getQuality().raw()));
    	     
    	}

    	return buffer.toString();
        }
}
