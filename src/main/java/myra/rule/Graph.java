/*
 * Graph.java
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

import static myra.datamining.Attribute.EQUAL_TO;

import java.util.Arrays;

import myra.datamining.Attribute;
import myra.datamining.Dataset;
import myra.datamining.Attribute.Condition;

/**
 * This class represents the construction graph. The graph holds the information
 * about the solution's components, and their associated pheromone and heuristic
 * information.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class Graph {
    /**
     * The index of the (initial) start vertex.
     */
    public static final int START_INDEX = 0;

    /**
     * The pheromone matrix.
     */
    protected Entry[][] matrix;

    /**
     * The vertices of the graph.
     */
    protected Vertex[] vertices;

    /**
     * Default constructor. Subclasses are responsible for initilising the
     * properties of the graph.
     */
    protected Graph() {
    }

    /**
     * Creates a new graph based on the characteristics of the specified
     * dataset.
     * 
     * @param dataset
     *            the current dataset.
     */
    public Graph(Dataset dataset) {
        Attribute[] attributes = dataset.attributes();
        // the virtual start vertex
        int termsCount = 1;

        // the last attribute is the class attribute, so we ignore it
        for (int i = 0; i < (attributes.length - 1); i++) {
            switch (attributes[i].getType()) {
            case NOMINAL:
                termsCount += attributes[i].values().length;
                break;

            case CONTINUOUS:
                termsCount++;
                break;
            }
        }

        vertices = new Vertex[termsCount];

        vertices[START_INDEX] = new Vertex();
        vertices[START_INDEX].attribute = -1;
        vertices[START_INDEX].condition = null;

        int index = 1;

        for (int i = 0; i < (attributes.length - 1); i++) {
            switch (attributes[i].getType()) {
            case NOMINAL: {
                for (int j = 0; j < attributes[i].length(); j++) {
                    Vertex v = new Vertex();
                    v.attribute = i;

                    Condition condition = new Condition();
                    condition.attribute = i;
                    condition.relation = EQUAL_TO;
                    condition.value[0] = j;

                    v.condition = condition;
                    vertices[index] = v;

                    index++;
                }

                break;
            }

            case CONTINUOUS: {
                Vertex v = new Vertex();
                v.attribute = i;
                v.condition = null;

                vertices[index] = v;
                index++;

                break;
            }
            }
        }

        // creates the pheromone matrix

        matrix = new Entry[termsCount][termsCount];

        for (int i = 0; i < termsCount; i++) {
            Vertex current = vertices[i];

            for (int j = 0; j < termsCount; j++) {
                if (i != j && j > 0) {
                    Vertex other = vertices[j];

                    if (current.attribute != other.attribute) {
                        matrix[i][j] = new Entry();
                    } else {
                        matrix[i][j] = null;
                    }
                } else {
                    matrix[i][j] = null;
                }
            }
        }
    }

    /**
     * Returns the number of vertices of the graph.
     * 
     * @return the number of vertices of the graph.
     */
    public int size() {
        return vertices.length;
    }

    /**
     * Returns the structure representing the pheromone matrix.
     * 
     * @return the structure representing the pheromone matrix.
     */
    public Entry[][] matrix() {
        return matrix;
    }

    /**
     * Returns the vertices of the graph.
     * 
     * @return the vertices of the graph.
     */
    public Vertex[] vertices() {
        return vertices;
    }

    /**
     * Returns the index of the vertex represented by the attribute and value
     * indexes.
     * 
     * @param attribute
     *            the attribute index.
     * @param value
     *            the attribute value's index.
     * 
     * @return the index of the vertex.
     */
    public int indexOf(int attribute, int value) {
        for (int i = 0; i < vertices.length; i++) {
            if (vertices[i].attribute == attribute) {
                if (vertices[i].condition == null) {
                    return i;
                } else if (vertices[i].condition.value[0] == value) {
                    return i;
                }
            }
        }

        throw new IllegalArgumentException("Could not find vertex for attribute ["
                + attribute + "] and value [" + value + "].");
    }

    /**
     * Returns the index of the vertex represented by the attribute. This only
     * works for continuous attribute vertices.
     * 
     * @param attribute
     *            the attribute index.
     * 
     * @return the index of the vertex.
     */
    public int indexOf(int attribute) {
        return indexOf(attribute, -1);
    }

    /**
     * Decreases all pheromone values by the specified factor.
     * 
     * @param factor
     *            the evaporation factor.
     * @param level
     *            the maximum level used during evaporation.
     */
    public void evaporate(double factor, int level) {
        for (int i = 0; i < vertices.length; i++) {
            Entry[] neighbours = matrix[i];

            for (int j = 0; j < neighbours.length; j++) {
                if (neighbours[j] != null) {
                    Entry entry = neighbours[j];
                    int size = entry.size();

                    if (size < level) {
                        entry.set(level - 1, entry.initial());
                    }

                    for (int k = 0; k < level; k++) {
                        if (k < size) {
                            entry.set(k, entry.value(k) * factor);
                        } else {
                            entry.set(k, entry.initial() * factor);
                        }
                    }
                }
            }
        }
    }

    /**
     * This (struct-like) class represents an entry in the pheromone matrix.
     * 
     * @author Fernando Esteban Barril Otero
     */
    public static class Entry implements Cloneable {
        /**
         * The array of values. Each index correspond to a different level.
         */
        private double[] values;

        /**
         * The initial value.
         */
        private double initial;

        /**
         * Default constructor.
         */
        public Entry() {
            this(Double.NaN, new double[0]);
        }

        /**
         * Creates a new entry.
         * 
         * @param initial
         *            the initial value.
         * @param values
         *            the array of values.
         */
        public Entry(double initial, double... values) {
            this.initial = initial;
            this.values = values;
        }

        /**
         * Returns the initial value.
         * 
         * @return the initial value.
         */
        public double initial() {
            return initial;
        }

        /**
         * Returns the value at the specified level. If there is no value at the
         * specified level, returns the initial value.
         * 
         * @param level
         *            the value level.
         * 
         * @return the value at the specified level.
         */
        public double value(int level) {
            if (level < values.length) {
                return values[level];
            }

            return initial;
        }

        /**
         * Sets the initial value.
         * 
         * @param initial
         *            the value to set.
         */
        public void setInitial(double initial) {
            this.initial = initial;
        }

        /**
         * Sets the value of a specific level.
         * 
         * @param level
         *            the level to add the value.
         * @param value
         *            the value to set.
         */
        public void set(int level, double value) {
            if (Double.isNaN(value)) {
                throw new IllegalArgumentException("Invalid pheromone value: " + value);
            }

            if (level < values.length) {
                values[level] = value;
            } else {
                int last = values.length;
                values = Arrays.copyOf(values, level + 1);
                // make sure to set values for all previous levels
                Arrays.fill(values, last, level, initial);
                values[level] = value;
            }
        }

        /**
         * Sets the specified value to all levels of the entry.
         * 
         * @param value
         *            the value to set.
         */
        public void setAll(double value) {
            for (int i = 0; i < values.length; i++) {
                values[i] = value;
            }
        }

        /**
         * Returns the number of levels in the entry.
         * 
         * @return the number of levels in the entry.
         */
        public int size() {
            return values.length;
        }

        @Override
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("<");

            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    buffer.append(",");
                }

                buffer.append(values[i]);
            }

            buffer.append(">");
            return buffer.toString();
        }

        @Override
        public Entry clone() {
            try {
                Entry clone = (Entry) super.clone();
                clone.values = values.clone();
                return clone;
            } catch (CloneNotSupportedException exception) {
                throw new RuntimeException(exception);
            }
        }

        /**
         * Deep clones an arrays of objects.
         * 
         * @param array
         *            the array to be clone.
         * 
         * @return a clone of the array.
         */
        public static Entry[] deepClone(Entry[] array) {
            Entry[] clone = new Entry[array.length];

            for (int i = 0; i < clone.length; i++) {
                clone[i] = array[i].clone();
            }

            return clone;
        }

        /**
         * Deep clones an arrays of objects.
         * 
         * @param array
         *            the array to be clone.
         * 
         * @return a clone of the array.
         */
        public static Entry[] initialise(Entry[] array) {
            for (int i = 0; i < array.length; i++) {
                array[i] = new Entry(0.0, 0.0);
            }

            return array;
        }
    }

    /**
     * This (struct-like) class represents a vertex of the construction graph.
     * 
     * @author Fernando Esteban Barril Otero
     */
    public static class Vertex {
        /**
         * The index of the attribute that this vertex represents.
         */
        public int attribute;

        /**
         * The attribute-value condition.
         */
        public Condition condition;

        /**
         * Default constructor.
         */
        public Vertex() {
            this.attribute = -1;
            this.condition = null;
        }
    }
}