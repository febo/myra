/*
 * ClassificationRule.java
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

package myra.classification.rule;

import static myra.datamining.Dataset.COVERED;
import static myra.datamining.Dataset.NOT_COVERED;
import static myra.datamining.Dataset.RULE_COVERED;

import java.util.Arrays;

import myra.classification.Label;
import myra.datamining.Dataset;
import myra.datamining.Prediction;
import myra.datamining.Dataset.Instance;
import myra.rule.Rule;

/**
 * This class represents a rule for classification problems. In classification,
 * there is a pre-defined set of predicted values.
 * 
 * @author Fernando Esteban Barril Otero
 * 
 * @since 4.5
 * 
 * @see Label
 */
public class ClassificationRule extends Rule {
    /**
     * The predicted class value.
     */
    private Label consequent;

    /**
     * The class distribution of the covered examples.
     */
    private int[] covered;

    /**
     * The class distribution of the uncovered examples.
     */
    private int[] uncovered;

    /**
     * Creates a new <code>ClassificationRule</code>.
     */
    public ClassificationRule() {
        this(0);
    }

    /**
     * Creates a new <code>ClassificationRule</code> with the specified
     * capacity.
     * 
     * @param capacity
     *            the allocated size of the rule.
     */
    public ClassificationRule(int capacity) {
        super(capacity);
        covered = new int[0];
        uncovered = new int[0];
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
    public int apply(Dataset dataset, Instance[] instances) {
        int total = 0;

        covered = Arrays.copyOf(covered, dataset.classLength());
        Arrays.fill(covered, 0);

        uncovered = Arrays.copyOf(uncovered, dataset.classLength());
        Arrays.fill(uncovered, 0);

        for (int i = 0; i < dataset.size(); i++) {
            if (instances[i].flag != COVERED) {
                if (covers(dataset, i)) {
                    total++;
                    covered[(int) dataset.value(i, dataset.classIndex())]++;
                    instances[i].flag = RULE_COVERED;
                } else {
                    uncovered[(int) dataset.value(i, dataset.classIndex())]++;
                    instances[i].flag = NOT_COVERED;
                }
            }
        }

        return total;
    }

    /**
     * Returns the number of available (uncovered) instances.
     * 
     * @return the number of available (uncovered) instances.
     */
    public int available() {
        int available = 0;

        for (int i = 0; i < uncovered.length; i++) {
            available += uncovered[i];
        }

        return available;
    }

    /**
     * Returns the number of different class values of the covered instances.
     * 
     * @return the number of different class values of the covered instances.
     */
    public int diversity() {
        int diverse = 0;

        for (int i = 0; i < covered.length; i++) {
            if (covered[i] > 0) {
                diverse++;
            }
        }

        if (diverse == 0) {
            throw new IllegalStateException("Covered information empty.");
        }

        return diverse;
    }

    @Override
    public Label getConsequent() {
        return consequent;
    }

    @Override
    public void setConsequent(Prediction prediction) {
        if (!(prediction instanceof Label)) {
            throw new IllegalArgumentException("Invalid predicted value: "
                    + prediction);
        }

        consequent = (Label) prediction;
    }

    @Override
    public boolean isDiverse() {
        return diversity() > 1;
    }

    /**
     * Returns the class distribution of the covered examples.
     * 
     * @return the class distribution of the covered examples.
     */
    public int[] covered() {
        return covered;
    }

    /**
     * Sets the class distribution of the covered examples.
     * 
     * @param covered
     *            the class distribution of the covered examples.
     */
    public void covered(int[] covered) {
        this.covered = covered;
    }

    /**
     * Returns the class distribution of the uncovered examples.
     * 
     * @return the class distribution of the uncovered examples.
     */
    public int[] uncovered() {
        return uncovered;
    }

    /**
     * Sets the class distribution of the uncovered examples.
     * 
     * @param uncovered
     *            the class distribution of the uncovered examples.
     */
    public void uncovered(int[] uncovered) {
        this.uncovered = uncovered;
    }
}