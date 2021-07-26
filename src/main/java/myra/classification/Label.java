/*
 * Label.java
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

package myra.classification;

import java.util.Arrays;
import java.util.Collection;

import myra.datamining.Attribute;
import myra.datamining.Dataset;
import myra.datamining.Prediction;

/**
 * The <code>Label</code> represents the predicted value of a classification
 * algorithm. The label is represented as an array of all possible labels. Note
 * that for hierarchical and/or multi-label problems, where more than one label
 * can be associated with an instance, the array can have more than one position
 * active.
 * 
 * @since 4.5
 * 
 * @author Fernando Esteban Barril Otero
 */
public final class Label implements Prediction, Cloneable {
    /**
     * Information of active values.
     */
    private boolean[] active;

    /**
     * Probability of each class label.
     */
    private double[] probabilities;

    /**
     * Creates a new label using the information from the specified attribute.
     * 
     * @param target
     *            the target attribute.
     */
    public Label(Attribute target) {
        this(target, Dataset.MISSING_VALUE_INDEX);
    }

    /**
     * Creates a new label using the information from the specified attribute.
     * 
     * @param target
     *            the target attribute.
     * @param index
     *            the active value index.
     */
    public Label(Attribute target, int index) {
        this(target.length(), index);
    }

    /**
     * Default constructor.
     * 
     * @param length
     *            the number of different values.
     * @param index
     *            the class value index.
     */
    public Label(int length, int index) {
        active = new boolean[length];

        if (index != Dataset.MISSING_VALUE_INDEX) {
            active[index] = true;
        }

        probabilities = new double[length];
        Arrays.fill(probabilities, 1.0);
    }

    /**
     * Returns the class value index represented by the label.
     * 
     * @return the class value index represented by the label.
     */
    public int value() {
        int index = Dataset.MISSING_VALUE_INDEX;

        for (int i = 0; i < active.length; i++) {
            if (active[i]) {
                if (index == Dataset.MISSING_VALUE_INDEX) {
                    index = i;
                } else {
                    throw new IllegalStateException("Multiple labels active");
                }
            }
        }

        return index;
    }

    /**
     * Returns <code>true</code> if the specified value is active.
     * 
     * @param index
     *            index of the value.
     * 
     * @return <code>true</code> if the specified value is active.
     */
    public boolean active(int index) {
        return active[index];
    }

    /**
     * Returns the information of active values.
     * 
     * @return the information of active values.
     */
    public boolean[] active() {
        return active;
    }

    /**
     * Returns the probability of the specified class label.
     * 
     * @param index
     *            index of the value.
     * 
     * @return the probability of the specified class label.
     */
    public double probability(int index) {
        return probabilities[index];
    }

    /**
     * Returns the probability of each class label.
     * 
     * @return the probability of each class label.
     */
    public double[] probabilities() {
        return probabilities;
    }

    /**
     * Returns the number of class labels present in this <code>Label</code>
     * instance.
     * 
     * @return the number of class labels present in this <code>Label</code>
     *         instance.
     */
    public int cardinality() {
        int count = 0;

        for (int i = 0; i < active.length; i++) {
            if (active[i]) {
                count++;
            }
        }

        return count;
    }

    /**
     * Returns the number of labels present in both <code>Label</code> objects.
     * 
     * @param other
     *            the <code>Label</code> object for comparison.
     * 
     * @return the number of labels present in both <code>Label</code> objects.
     */
    public int intersect(Label other) {
        if (active.length != other.active.length) {
            throw new IllegalArgumentException("Label size do not match: "
                    + active.length + " expected, " + other.active.length
                    + " found");
        }

        int count = 0;

        for (int i = 0; i < active.length; i++) {
            if (active[i] && other.active[i]) {
                count++;
            }
        }

        return count;
    }

    /**
     * Removes the specified label values from all instances lavels.
     * 
     * @param indexes
     *            the indexes of the labels to remove.
     */
    public void remove(int... indexes) {
        double[] p = new double[probabilities.length - indexes.length];
        boolean[] a = new boolean[active.length - indexes.length];

        int source = 0;
        int target = 0;

        for (int i = 0; i < indexes.length; i++) {
            int length = indexes[i] - source;
            if (length > 0) {
                System.arraycopy(probabilities, source, p, target, length);
                System.arraycopy(active, source, a, target, length);
                target += length;
            }
            source = indexes[i] + 1;
        }

        System.arraycopy(probabilities,
                         source,
                         p,
                         target,
                         probabilities.length - source);
        System.arraycopy(active, source, a, target, active.length - source);

        probabilities = p;
        active = a;
    }

    @Override
    public String toString(Attribute target) {
        StringBuffer values = new StringBuffer();

        for (int i = 0; i < active.length; i++) {
            if (active[i]) {
                values.append(values.length() == 0 ? target.value(i)
                        : "," + target.value(i));
            }
        }

        return values.toString();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(active);
    }

    /**
     * Indicates whether some label is "equal to" this label. The comparison
     * only takes into consideration the active label, i.e., the probabilities
     * of the labels are not compared.
     * 
     * @param other
     *            the label with which to compare.
     * 
     * @return <code>true</code> is this label has the same active values as the
     *         other label; <code>false</code> otherwise.
     */
    public boolean equals(Label other) {
        return Arrays.equals(active, other.active);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Label) {
            return equals((Label) object);
        }

        return false;
    }

    /**
     * Returns a new <code>Label</code> instance representing the specified
     * array of labels.
     * 
     * @param target
     *            the target attribute.
     * @param values
     *            the array of labels.
     * 
     * @return a new <code>Label</code> instance representing the specified
     *         array of labels.
     */
    public static Label toLabel(Attribute target, String... values) {
        return toLabel(target, Arrays.asList(values));
    }

    /**
     * Returns a new <code>Label</code> instance representing the specified
     * array of labels.
     * 
     * @param target
     *            the target attribute.
     * @param probabilities
     *            the probability associared with each label.
     * 
     * @return a new <code>Label</code> instance representing the specified
     *         array of labels.
     */
    public static Label toLabel(Attribute target, double[] probabilities) {
        boolean[] active = new boolean[target.length()];
        String[] values = target.values();

        for (int i = 0; i < values.length; i++) {
            active[i] = (probabilities[i] > 0);
        }

        Label label = new Label(target);
        label.active = active;
        label.probabilities = probabilities;
        return label;
    }

    /**
     * Returns a new <code>Label</code> instance representing the specified list
     * of labels.
     * 
     * @param target
     *            the target attribute.
     * @param labels
     *            the list of labels.
     * 
     * @return a new <code>Label</code> instance representing the specified list
     *         of labels.
     */
    public static Label toLabel(Attribute target, Collection<String> labels) {
        if (target.length() < labels.size()) {
            throw new IllegalArgumentException("Invalid number of labels: <"
                    + target.length() + "> expected, <" + labels.size()
                    + "> found");
        }

        boolean[] active = new boolean[target.length()];
        String[] values = target.values();

        for (int i = 0; i < values.length; i++) {
            active[i] = labels.contains(values[i]);
        }

        Label label = new Label(target);
        label.active = active;
        return label;
    }

    /**
     * Returns a new <code>Label</code> instance representing the specified list
     * of labels.
     * 
     * @param target
     *            the target attribute.
     * @param active
     *            the information of active values.
     * 
     * @return a new <code>Label</code> instance representing the specified list
     *         of labels.
     */
    public static Label toLabel(Attribute target, boolean[] active) {
        if (target.length() < active.length) {
            throw new IllegalArgumentException("Invalid number of labels: <"
                    + target.length() + "> expected, <" + active.length
                    + "> found");
        }

        Label label = new Label(target);
        label.active = active;
        return label;
    }

    /**
     * Returns a new <code>Label</code> instance representing the specified list
     * of labels.
     * 
     * @param target
     *            the target attribute.
     * @param active
     *            the information of active values.
     * @param probabilities
     *            the probability associared with each label.
     * 
     * @return a new <code>Label</code> instance representing the specified list
     *         of labels.
     */
    public static Label toLabel(Attribute target,
                                boolean[] active,
                                double[] probabilities) {
        if (target.length() < active.length) {
            throw new IllegalArgumentException("Invalid number of labels: <"
                    + target.length() + "> expected, <" + active.length
                    + "> found");
        }

        Label label = new Label(target);
        label.active = active;
        label.probabilities = probabilities;
        return label;
    }
}