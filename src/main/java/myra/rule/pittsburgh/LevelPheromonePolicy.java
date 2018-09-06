/*
 * LevelPheromonePolicy.java
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

package myra.rule.pittsburgh;

import static myra.Config.CONFIG;
import static myra.rule.Graph.START_INDEX;

import myra.Config.ConfigKey;
import myra.Cost;
import myra.rule.Graph;
import myra.rule.Graph.Entry;
import myra.rule.Rule;
import myra.rule.RuleList;

/**
 * This class is responsible for maintaining the pheromone values of the
 * construction graph. The basic operations are:
 * 
 * <ul>
 * <li>initialisation</li>
 * <li>update (including evaporation)</li>
 * <li>convergence check</li>
 * </ul>
 * 
 * Pheromone values respect a MAX-MIN update rule, therefore no value is lower
 * than <code>MIN</code> and no value is greater than <code>MAX</code>. The
 * algorithm converges when the current path has a <code>MAX</code> value and
 * the alternative paths have a <code>MIN</code> value.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class LevelPheromonePolicy {
    /**
     * The config key for the evaporation factor.
     */
    public final static ConfigKey<Double> EVAPORATION_FACTOR =
	    new ConfigKey<Double>();

    /**
     * The config key for the MAX-MIN p_best value.
     */
    public final static ConfigKey<Double> P_BEST = new ConfigKey<Double>();

    /**
     * The default initial pheromone value.
     */
    public final static double INITIAL_PHEROMONE = 10.0;

    /**
     * The quality of the global best solution.
     */
    private Cost global;

    /**
     * The MAX-MIN upper pheromone limit.
     */
    private double tMax;

    /**
     * The MAX-MIN lower pheromone limit.
     */
    private double tMin;

    /**
     * Default constructor.
     */
    public LevelPheromonePolicy() {
	global = null;
	tMax = 0.0;
	tMin = 0.0;
    }

    /**
     * Initialises the pheromone values of the specified graph.
     * 
     * @param graph
     *            the construction graph to be initialised.
     */
    public void initialise(Graph graph) {
	Entry[][] matrix = graph.matrix();

	for (int i = 0; i < graph.size(); i++) {
	    for (int j = 0; j < graph.size(); j++) {
		if (matrix[i][j] != null) {
		    matrix[i][j] =
			    new Entry(INITIAL_PHEROMONE, INITIAL_PHEROMONE);
		}
	    }
	}
    }

    /**
     * Updates the pheromone values, increasing the pheromone according to the
     * rules of the specified <code>RuleList</code> instance. Evaporation is
     * also performed as part of the update procedure.
     * 
     * @param graph
     *            the construction graph.
     * @param list
     *            the list of rules.
     */
    public void update(Graph graph, RuleList list) {
	final double factor = CONFIG.get(EVAPORATION_FACTOR);
	// the default rule is not used
	final int size = list.size() - (list.hasDefault() ? 1 : 0);

	// updates the pheromone limits if we have a new best solution

	if (global == null || list.getQuality().compareTo(global) > 0) {
	    global = list.getQuality();
	    double n = graph.size();

	    double average = (n / 2.0);
	    double pDec = Math.pow(CONFIG.get(P_BEST), 1.0 / n);

	    tMax = (1 / (1 - factor)) * (global.raw() / 5.0);
	    tMin = (tMax * (1 - pDec)) / ((average - 1) * pDec);

	    if (tMin > tMax) {
		tMin = tMax;
	    }
	}

	// evaporates the pheromone values

	double truncMax = precision(tMax);
	double truncMin = precision(tMin);

	for (int i = 0; i < graph.size(); i++) {
	    Entry[] neighbours = graph.matrix()[i];

	    for (int j = 0; j < neighbours.length; j++) {
		if (neighbours[j] != null) {
		    Entry entry = neighbours[j];
		    int length = entry.size();

		    if (length < size) {
			// grows the entry values array
			entry.set(size - 1, entry.initial());
		    }

		    int dimension = Math.max(size, length);

		    for (int k = 0; k < dimension; k++) {
			double value = 0.0;

			if (k < length) {
			    value = entry.value(k) * factor;
			} else {
			    value = entry.initial() * factor;
			}

			double truncValue = precision(value);

			if (truncValue > truncMax) {
			    entry.set(k, tMax);
			} else if (truncValue < truncMin) {
			    entry.set(k, tMin);
			} else {
			    entry.set(k, value);
			}
		    }
		}
	    }
	}

	// updates the pheromone of the edges

	double delta = list.getQuality().raw() / 5.0;
	int level = 0;

	for (int i = 0; i < size; i++) {
	    Rule rule = list.rules()[i];
	    int from = START_INDEX;

	    for (int j = 0; j < rule.size(); j++) {
		Entry entry = graph.matrix()[from][rule.terms()[j].index()];
		double value = entry.value(level) + delta;

		if (precision(value) > truncMax) {
		    entry.set(level, tMax);
		} else {
		    entry.set(level, value);
		}

		from = rule.terms()[j].index();
	    }

	    level++;
	}
    }

    /**
     * Checks if the pheromone values in the specified graph have converged.
     * Only the connections present in the <code>RuleList</code> instance are
     * checked.
     * 
     * @param graph
     *            the construction graph.
     * @param list
     *            the list of rules.
     * 
     * @return <code>true</code> if the pheromone values have converged;
     *         <code>false</code> otherwise.
     */
    public boolean hasConverged(Graph graph, RuleList list) {
	double truncMax = precision(tMax);
	double truncMin = precision(tMin);
	int level = 0;

	for (int i = 0; i < list.size(); i++) {
	    Rule rule = list.rules()[i];
	    int from = 0;

	    for (int j = 0; j < rule.size(); j++) {
		Entry[] neighbours = graph.matrix()[from];
		int available = 0;
		int maxCount = 0;
		int minCount = 0;

		for (int k = 0; k < neighbours.length; k++) {
		    if (neighbours[k] != null) {
			available++;
			double truncValue =
				precision(neighbours[k].value(level));

			if (truncValue == truncMax) {
			    maxCount++;
			} else if (truncValue == truncMin) {
			    minCount++;
			}
		    }
		}

		if ((minCount != (available - 1)) || (maxCount != 1)) {
		    return false;
		}

		from = rule.terms()[j].index();
	    }

	    level++;
	}

	return (list.size() > 0) ? true : false;
    }

    /**
     * Returns the MAX-MIN upper pheromone limit.
     * 
     * @return the MAX-MIN upper pheromone limit.
     */
    public double max() {
	return tMax;
    }

    /**
     * Returns the MAX-MIN lower pheromone limit.
     * 
     * @return the MAX-MIN lower pheromone limit.
     */
    public double min() {
	return tMin;
    }

    /**
     * Truncates a <code>double</code> value to 2 digit precision.
     * 
     * @param value
     *            the value to be truncated.
     * 
     * @return the truncated value.
     */
    private double precision(double value) {
	return ((int) (value * 100)) / 100.0;
    }
}