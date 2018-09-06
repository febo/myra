/*
 * Graph.java
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

import java.util.Arrays;

import myra.datamining.Attribute;
import myra.datamining.Attribute.Condition;
import myra.datamining.Dataset;

/**
 * This class represents the construction graph. The graph holds the information
 * about the solution's components, and their associated pheromone and heuristic
 * information.
 * <p>
 * In this implementation, vertices represent attributes to be selected. Each
 * attribute holds a solution archive to sample values.
 * </p>
 * 
 * @author Fernando Esteban Barril Otero
 */
public class Graph extends myra.rule.Graph {
    /**
     * The index of the (final) end vertex.
     */
    public static final int END_INDEX = 1;

    /**
     * Default constructor. Subclasses are responsible for initialising the
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
	vertices = new Vertex[attributes.length + 1];
	// start and end virtual vertices
	vertices[START_INDEX] = new Vertex(null);
	vertices[END_INDEX] = new Vertex(null);

	int index = 2;

	for (int i = 0; i < (attributes.length - 1); i++) {
	    switch (attributes[i].getType()) {
	    case NOMINAL: {
		Vertex v =
			new Vertex(new Variable.Nominal(attributes[i].size()));
		v.attribute = i;

		vertices[index] = v;
		index++;

		break;
	    }

	    case CONTINUOUS: {
		Vertex v = new Vertex(new Variable.Continuous(attributes[i]
			.lower(), attributes[i].upper()));
		v.attribute = i;

		vertices[index] = v;
		index++;

		break;
	    }
	    }
	}

	// creates the pheromone matrix

	matrix = new Entry[vertices.length][vertices.length];

	for (int i = 0; i < matrix.length; i++) {
	    for (int j = 0; j < matrix[i].length; j++) {
		if (i == END_INDEX) {
		    matrix[i][j] = null;
		} else if (i == START_INDEX && j == END_INDEX) {
		    matrix[i][j] = null;
		} else if (i != j && j > 0) {
		    matrix[i][j] = new Entry();
		} else {
		    matrix[i][j] = null;
		}
	    }
	}

	matrix[START_INDEX][END_INDEX] = null;
    }

    /**
     * Returns the vertices of the graph.
     * 
     * @return the vertices of the graph.
     */
    public Vertex[] vertices() {
	return (Vertex[]) vertices;
    }

    /**
     * This (struct-like) class represents a vertex of the construction graph.
     * 
     * @author Fernando Esteban Barril Otero
     */
    public static class Vertex extends myra.rule.Graph.Vertex {
	/**
	 * The attribute-value solution archive array to support multiple
	 * pheromone levels.
	 */
	public Variable[] archive;

	/**
	 * The variable to initialise each pheromone level. This is not updated
	 * throughout the run of the algorithm.
	 */
	public Variable initial;

	/**
	 * Default constructor.
	 */
	public Vertex(Variable initial) {
	    super();
	    archive = new Variable[0];
	    this.initial = initial;
	}

	/**
	 * Samples a new condition using the archive.
	 * 
	 * @param level the current archive level.
	 * 
	 * @return a new condition.
	 */
	public Condition condition(int level) {
	    Condition condition = null;

	    if (level < archive.length) {
		condition = archive[level].sample();
	    } else {
		condition = initial.sample();
	    }

	    condition.attribute = attribute;
	    return condition;
	}
	
	/**
	 * Updates the archive.
	 * 
	 * @param level the current archive level.
	 * @param condition the condition.
	 * @param quality the quality of the condition.
	 */
	public void update(int level, Condition condition, double quality) {
	    if (archive.length <= level) {
		archive = Arrays.copyOf(archive, level + 1);
		archive[level] = (Variable) initial.clone();
	    }

	    archive[level].add(condition, quality);
	}
    }
}