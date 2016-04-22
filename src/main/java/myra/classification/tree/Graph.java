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

package myra.classification.tree;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import myra.datamining.Attribute;
import myra.datamining.Dataset;

/**
 * This class represents the construction graph. The graph holds the information
 * about the solution's components, and their associated pheromone and heuristic
 * information.
 * 
 * @author Fernando Esteban Barril Otero
 */
public final class Graph {
    /**
     * The index of the (initial) start vertex.
     */
    public static final int START_INDEX = "[START]".hashCode();

    /**
     * Pheromone matrix (mapping between [branch hash code, entry]).
     */
    Map<Integer, double[]> matrix;

    /**
     * The pheromone template array. This array is returned when a new entry in
     * the pheromone matrix is needed.
     */
    double[] template;

    /**
     * The mapping between [attribute name, index].
     */
    private Map<String, Integer> mapping;

    /**
     * Creates a new graph based on the characteristics of the specified
     * dataset.
     * 
     * @param dataset
     *            the current dataset.
     */
    public Graph(Dataset dataset) {
	Attribute[] attributes = dataset.attributes();
	template = new double[attributes.length - 1];

	matrix = new HashMap<Integer, double[]>();
	mapping = new HashMap<String, Integer>(template.length, 1.0f);

	for (int i = 0; i < template.length; i++) {
	    mapping.put(dataset.attributes()[i].getName(), i);
	}
    }

    /**
     * Returns the number of vertices of the graph.
     * 
     * @return the number of vertices of the graph.
     */
    public int size() {
	return template.length;
    }

    /**
     * Returns the structure representing the pheromone matrix. If there is no
     * entry for the specified <code>encoding</code>, returns an array with the
     * initial pheromone values.
     * 
     * @param encoding
     *            the pheromone matrix index.
     * 
     * @return the structure representing the pheromone matrix.
     */
    public double[] pheromone(int encoding) {
	double[] pheromone = matrix.get(encoding);

	if (pheromone == null) {
	    return template;
	}

	return pheromone;
    }

    /**
     * Returns the pheromone entry for the specified <code>encoding</code>. If
     * no entry is found, a new will be created.
     * 
     * @param encoding
     *            the pheromone matrix index.
     * @param tMax
     *            the upper pheromone limit.
     * 
     * @return the pheromone entry for the specified <code>encoding</code>.
     */
    public double[] entry(int encoding, double tMax) {
	double[] slots = matrix.get(encoding);

	if (slots == null) {
	    slots = new double[template.length];
	    Arrays.fill(slots, tMax);
	    matrix.put(encoding, slots);
	}

	return slots;
    }

    /**
     * Returns the index of the attribute given a vertex name.
     * 
     * @param name
     *            the vertex name.
     * 
     * @return the index of the attribute given a vertex name.
     */
    public int index(String name) {
	return mapping.get(name);
    }

    /**
     * Returns the collection of entries in the pheromone matrix.
     * 
     * @return the collection of entries in the pheromone matrix.
     */
    public Collection<Integer> entries() {
	return matrix.keySet();
    }
}