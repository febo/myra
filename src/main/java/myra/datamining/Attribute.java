/*
 * Attribute.java
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

import java.util.Arrays;

/**
 * This class represents an attribute of the dataset.
 * 
 * @see Dataset
 * 
 * @author Fernando Esteban Barril Otero
 */
public final class Attribute implements Cloneable {
    /**
     * Constant representing the condition type
     * <code>(attribute &lt;= v)</code>.
     */
    public static final short LESS_THAN_OR_EQUAL_TO = 1;

    /**
     * Constant representing the condition type <code>(attribute &gt; v)</code>.
     */
    public static final short GREATER_THAN = 2;

    /**
     * Constant representing the condition type
     * <code>(v1 &lt;= attribute &lt; v2)</code>.
     */
    public static final short IN_RANGE = 3;

    /**
     * Constant representing the condition type <code>(attribute = v)</code>.
     */
    public static final short EQUAL_TO = 4;

    /**
     * Constant representing the condition type <code>(attribute &lt; v)</code>.
     */
    public static final short LESS_THAN = 5;

    /**
     * Constant representing the condition type
     * <code>(attribute &gt;= v)</code>.
     */
    public static final short GREATER_THAN_OR_EQUAL_TO = 6;

    /**
     * Constant representing the condition type <code>(attribute IN
     * {v<sub>1</sub>, v<sub>2</sub>, ..., v<sub>n</sub>)</code>.
     */
    public static final short ANY_OF = 7;

    /**
     * The name of the attribute.
     */
    private String name;

    /**
     * The values of the attribute, only valid for nominal attributes.
     */
    private String[] values;

    /**
     * The type of the attribute.
     */
    private Type type;

    /**
     * The index (position) of the attribute relative to an instance.
     */
    private int index;

    /**
     * The lower bound value of continuous attributes.
     */
    private double lower;

    /**
     * The upper bound value of continuous attributes.
     */
    private double upper;

    /**
     * Creates a new attribute.
     * 
     * @param type
     *            the type of the attribute.
     * @param name
     *            the name of the attribute.
     */
    public Attribute(Type type, String name) {
        this.type = type;
        this.name = name;
        values = new String[0];
        index = -1;
        lower = Double.MAX_VALUE;
        upper = Double.MIN_VALUE;
    }

    /**
     * Returns the index of the attribute in the instance array.
     * 
     * @return the index of the attribute in the instance array.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the index the attribute in the instance array.
     * 
     * @param index
     *            the index to set.
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Returns the name of the attribute.
     * 
     * @return the name of the attribute.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the attribute type.
     * 
     * @return the attribute type.
     */
    public Type getType() {
        return type;
    }

    /**
     * Adds a value to the attribute.
     * 
     * @param value
     *            the value to add.
     */
    public void add(String value) {
        values = Arrays.copyOf(values, values.length + 1);
        values[values.length - 1] = value;
    }

    /**
     * Removes the specified values.
     * 
     * @param indexes
     *            the indexes of the values to remove.
     */
    public void remove(int... indexes) {
        String[] v = new String[values.length - indexes.length];

        int source = 0;
        int target = 0;

        for (int i = 0; i < indexes.length; i++) {
            int length = indexes[i] - source;

            if (length > 0) {
                System.arraycopy(values, source, v, target, length);
                target += length;
            }

            source = indexes[i] + 1;
        }

        System.arraycopy(values, source, v, target, values.length - source);
        values = v;
    }

    /**
     * Returns the number of attribute values.
     * 
     * @return the number of attribute values.
     */
    public int length() {
        return values.length;
    }

    /**
     * Returns the attribute values.
     * 
     * @return the attribute values.
     */
    public String[] values() {
        return values;
    }

    /**
     * Returns the attribute value at the specified index.
     * 
     * @param index
     *            the attribute value index.
     * 
     * @return the attribute value at the specified index.
     */
    public String value(int index) {
        return values[index];
    }

    /**
     * Returns the index of the specified value.
     * 
     * @param value
     *            the value to look up.
     * 
     * @return the index of the specified value.
     */
    public int indexOf(String value) {
        for (int i = 0; i < values.length; i++) {
            if (value.equals(values[i])) {
                return i;
            }
        }

        throw new IllegalArgumentException("Value not found: " + value);
    }

    /**
     * Returns the number of values in the domain of the attribute.
     * 
     * @return the number of values in the domain of the attribute.
     */
    public int size() {
        return values.length;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Attribute clone() {
        try {
            Attribute clone = (Attribute) super.clone();
            clone.values = values.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.getMessage());
        }
    }

    /**
     * Updates the lower bound of the attribute.
     * 
     * @param value
     *            the candidate lower bound.
     */
    public void lower(double value) {
        if (value < lower) {
            lower = value;
        }
    }

    /**
     * Returns the lower bound value of the attribute.
     * 
     * @return the lower bound value of the attribute.
     */
    public double lower() {
        return lower;
    }

    /**
     * Updates the upper bound of the attribute.
     * 
     * @param value
     *            the candidate upper bound.
     */
    public void upper(double value) {
        if (value > upper) {
            upper = value;
        }
    }

    /**
     * Returns the upper bound value of the attribute.
     * 
     * @return the upper bound value of the attribute.
     */
    public double upper() {
        return upper;
    }

    /**
     * Enum of valid attribute types.
     * 
     * @author Fernando Esteban Barril Otero
     */
    public static enum Type {
        /**
         * Categorical attribute type.
         */
        NOMINAL,
        /**
         * Continuous attribute type.
         */
        CONTINUOUS;
    }

    /**
     * This (struct-like) class represents an attribute-value condition.
     * 
     * @author Fernando Esteban Barril Otero
     */
    public static class Condition implements Comparable<Condition> {
        /**
         * The maximum number of decimal places in the output of double values.
         */
        public static final int OUTPUT_LENGTH = 6;

        /**
         * The value(s) of the condition.
         */
        public double[] value = new double[2];

        /**
         * The computed threshold value(s).
         */
        public double[] threshold = new double[2];

        /**
         * The relation type.
         */
        public short relation = 0;

        /**
         * The entropy of the interval represented by the condition. Only valid
         * for continuous attribute conditions.
         */
        public double entropy = Double.NaN;

        /**
         * The index of the attribute of the condition.
         */
        public int attribute = -1;

        /**
         * The length of the interval represented by the condition. <b>Note:</b>
         * when this value is not initialised, it is equal to <code>0</code>.
         */
        public double length = 0.0;

        /**
         * Number of tested continuous thresholds (only valid for continuous
         * attributes).
         */
        public double tries = Double.NaN;

        /**
         * The class distribution frequency of the examples satisfying the
         * condition.
         */
        public double[] frequency;

        /**
         * The number of different class values occuring in the examples
         * satisfying the condition.
         */
        public int diversity = 0;

        /**
         * Index using during the creation of the condition. Usually this is
         * only for continuous attrutes conditions.
         */
        public int index;

        /**
         * The quality of the condition.
         */
        public double quality = Double.MIN_VALUE;

        /**
         * The weight of the condition.
         */
        public double weight = 0;

        /**
         * Returns <code>true</code> if the specified value satisfies this
         * attribute condition.
         * 
         * @param v
         *            the value to test.
         * 
         * @return <code>true</code> if the specified value satisfies this
         *         attribute condition; otherwise <code>false</code>.
         */
        public boolean satisfies(double v) {
            boolean satisfies = false;

            if (!Double.isNaN(v)) {
                switch (relation) {
                case LESS_THAN_OR_EQUAL_TO:
                    satisfies = (v <= value[0]);
                    break;

                case GREATER_THAN:
                    satisfies = (v > value[0]);
                    break;

                case IN_RANGE:
                    satisfies = (v >= value[0] && v < value[1]);
                    break;

                case EQUAL_TO:
                    satisfies = (v == value[0]);
                    break;

                case LESS_THAN:
                    satisfies = (v < value[0]);
                    break;

                case GREATER_THAN_OR_EQUAL_TO:
                    satisfies = (v >= value[0]);
                    break;

                case ANY_OF:
                    for (double option : value) {
                        if (v == option) {
                            satisfies = true;
                            break;
                        }
                    }

                    break;
                }
            }

            return satisfies;
        }

        @Override
        public int compareTo(Condition o) {
            return Double.compare(quality, o.quality);
        }

        @Override
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("(");
            buffer.append(attribute);
            buffer.append(",");
            buffer.append(relation);
            buffer.append(",");
            buffer.append(Arrays.toString(value));
            buffer.append(")");

            return buffer.toString();
        }

        /**
         * Returns a string representation of the attribute.
         * 
         * @param dataset
         *            the current dataset.
         * 
         * @return a string representation of the attribute.
         */
        public String toString(Dataset dataset) {
            StringBuffer buffer = new StringBuffer();

            String name = dataset.attributes()[attribute].getName();

            switch (relation) {
            case LESS_THAN:
                buffer.append(String.format("%s < %s", name, format(value[0])));
                break;

            case LESS_THAN_OR_EQUAL_TO:
                buffer.append(String
                        .format("%s <= %s", name, format(value[0])));
                break;

            case GREATER_THAN:
                buffer.append(String.format("%s > %s", name, format(value[0])));
                break;

            case GREATER_THAN_OR_EQUAL_TO:
                buffer.append(String
                        .format("%s >= %s", name, format(value[0])));
                break;

            case IN_RANGE:
                buffer.append(String.format("%s < %s <= %s",
                                            format(value[0]),
                                            name,
                                            format(value[1])));
                break;

            case EQUAL_TO:
                buffer.append(String.format("%s = %s",
                                            name,
                                            dataset.attributes()[attribute]
                                                    .value((int) value[0])));
                break;

            case ANY_OF:
                buffer.append(String.format("%s = {", name));

                for (int j = 0; j < value.length; j++) {

                    if (j > 0) {
                        buffer.append(", ");
                    }

                    buffer.append(String
                            .format("%s",
                                    dataset.attributes()[attribute]
                                            .value((int) value[j])));
                }

                buffer.append("}");

                break;
            }

            return buffer.toString();
        }

        /**
         * Returns a string representing the specified double value respecting
         * the maximum {@link #OUTPUT_LENGTH}.
         * 
         * @param value
         *            the double value.
         * 
         * @return a string representing the specified double value.
         */
        private String format(double value) {
            String output = Double.toString(value);
            int length = output.length() - output.indexOf('.') + 1;

            if (length > OUTPUT_LENGTH) {
                output = String.format("%.6f", value);
                return output.replaceAll("0+$", "");
            }

            return output;
        }
    }
}