/*
 * PheromonePolicy.java
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

import static myra.Config.CONFIG;
import static myra.classification.tree.Graph.START_INDEX;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import myra.Config.ConfigKey;
import myra.Cost;

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
public class PheromonePolicy {
    /**
     * The config key for the evaporation factor.
     */
    public final static ConfigKey<Double> EVAPORATION_FACTOR =
            new ConfigKey<>();

    /**
     * The config key for the MAX-MIN p_best value.
     */
    public final static ConfigKey<Double> P_BEST = new ConfigKey<>();

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
     * The fraction of the tree quality used to update the pheromones.
     */
    private static final double FRACTION = 10.0;

    /**
     * Default constructor.
     */
    public PheromonePolicy() {
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
        graph.matrix = new HashMap<Integer, double[]>();

        for (int i = 0; i < graph.template.length; i++) {
            graph.template[i] = INITIAL_PHEROMONE;
        }
    }

    /**
     * Updates the pheromone values, increasing the pheromone according to the
     * branches of the specified <code>Tree</code> instance. Evaporation is also
     * performed as part of the update procedure.
     * 
     * @param graph
     *            the construction graph.
     * @param tree
     *            the decision tree.
     */
    public void update(Graph graph, Tree tree) {
        final double factor = CONFIG.get(EVAPORATION_FACTOR);

        // updates the pheromone limits if we have a new best solution

        if (global == null || (tree.getQuality().compareTo(global) > 0)) {
            global = tree.getQuality();
            double n = graph.size();

            double average = (n / 2.0) * tree.size();
            double pDec = Math.pow(CONFIG.get(P_BEST), 1.0 / n);

            tMax = (1 / (1 - factor)) * (global.adjusted() / FRACTION);
            tMin = (tMax * (1 - pDec)) / ((average - 1) * pDec);

            if (tMin > tMax) {
                tMin = tMax;
            }
        }

        // updates the pheromone of the edges

        double delta = tree.getQuality().adjusted() / FRACTION;
        HashSet<Integer> updated = new HashSet<Integer>();

        Node root = tree.getRoot();

        if (!root.isLeaf()) {
            updated.add(START_INDEX);

            int index = graph.index(root.getName());
            update(graph.entry(START_INDEX, tMax), index, delta, factor);

            LinkedList<InternalNode> nodes = new LinkedList<InternalNode>();
            nodes.add((InternalNode) root);

            while (!nodes.isEmpty()) {
                InternalNode internal = nodes.removeFirst();

                for (int i = 0; i < internal.children.length; i++) {
                    if (!internal.children[i].isLeaf()) {
                        final int code = InternalNode
                                .encode(internal, internal.conditions[i]);
                        updated.add(code);

                        index = graph.index(internal.children[i].getName());
                        update(graph.entry(code, tMax), index, delta, factor);

                        nodes.add((InternalNode) internal.children[i]);
                    }
                }
            }
        }

        // evaporates the pheromone of the unused branches

        for (Integer entry : graph.entries()) {
            if (!updated.contains(entry)) {
                update(graph.pheromone(entry), -1, 0.0, factor);
            }
        }
    }

    /**
     * Checks if the pheromone values in the specified graph have converged.
     * Only the connections present in the <code>Tree</code> instance are
     * checked.
     * 
     * @param graph
     *            the construction graph.
     * @param tree
     *            the decision tree.
     * 
     * @return <code>true</code> if the pheromone values have converged;
     *         <code>false</code> otherwise.
     */
    public boolean hasConverged(Graph graph, Tree tree) {
        double truncMax = precision(tMax);
        double truncMin = precision(tMin);

        Node root = tree.getRoot();

        if (root.isLeaf()) {
            return true;
        } else {
            double[] values = graph.pheromone(START_INDEX);

            if (!check(values, truncMin, truncMax)) {
                return false;
            }
        }

        LinkedList<InternalNode> nodes = new LinkedList<InternalNode>();
        nodes.add((InternalNode) root);

        while (!nodes.isEmpty()) {
            InternalNode internal = nodes.removeFirst();

            for (int i = 0; i < internal.children.length; i++) {
                if (!internal.children[i].isLeaf()) {
                    double[] values = graph.pheromone(InternalNode
                            .encode(internal, internal.conditions[i]));

                    if (!check(values, truncMin, truncMax)) {
                        return false;
                    }

                    nodes.add((InternalNode) internal.children[i]);
                }
            }
        }

        return true;
    }

    /**
     * Returns <code>true</code> if the pheromone values of the array have
     * converged (there are <code>values.lenght - 1</code> tMin values and
     * <code>1</code> tMax value).
     * 
     * @param values
     *            the pheromone values.
     * @param tMin
     *            the lower pheromone limit.
     * @param tMax
     *            the upper pheromone limit.
     * 
     * @return <code>true</code> if the pheromone values of the array have
     *         converged.
     */
    private boolean check(double[] values, double tMin, double tMax) {
        int maxCount = 0;
        int minCount = 0;

        for (int j = 0; j < values.length; j++) {
            double truncValue = precision(values[j]);

            if (truncValue == tMax) {
                maxCount++;
            } else if (truncValue == tMin) {
                minCount++;
            }
        }

        if ((minCount != (values.length - 1)) || (maxCount != 1)) {
            return false;
        }

        return true;
    }

    /**
     * Updates the specified pheromone array.
     * 
     * @param slots
     *            the pheromone array.
     * @param index
     *            the index of the array which was present in the current best
     *            solution; <code>-1</code> in case that none of the components
     *            were present in the current best solution.
     * @param delta
     *            the increment factor.
     */
    private void update(double[] slots,
                        int index,
                        double delta,
                        double factor) {
        for (int i = 0; i < slots.length; i++) {
            slots[i] = (factor * slots[i]) + (i == index ? delta : 0.0);

            if (slots[i] > precision(tMax)) {
                slots[i] = tMax;
            } else if (slots[i] < precision(tMin)) {
                slots[i] = tMin;
            }
        }
    }

    /**
     * Truncates a <code>double</code> value to 2 digit precision.
     * 
     * @param value
     *            the value to be truncated.
     * 
     * @return the truncated value.
     */
    private final double precision(double value) {
        return ((int) (value * 100)) / 100.0;
    }
}