/*
 * FunctionSelector.java
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

package myra.classification.rule.function;

import static myra.Config.CONFIG;
import static myra.datamining.Algorithm.RANDOM_GENERATOR;
import static myra.rule.pittsburgh.LevelPheromonePolicy.EVAPORATION_FACTOR;
import static myra.rule.pittsburgh.LevelPheromonePolicy.INITIAL_PHEROMONE;

import myra.rule.Graph.Entry;
import myra.rule.irl.PheromonePolicy;
import myra.rule.Rule;
import myra.rule.RuleFunction;
import myra.rule.RuleList;
import myra.rule.pittsburgh.LevelPheromonePolicy;

/**
 * This class provides a dynamic rule evaluation function selection. The
 * selection uses a separate pheromone matrix.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class FunctionSelector {
    /**
     * The available rule evaluation functions.
     */
    private static final RuleFunction[] FUNCTIONS =
            new RuleFunction[] { new Accuracy(),
                                 new ConfidenceCoverage(),
                                 new CostMeasure(),
                                 new Fmeasure(),
                                 new Jaccard(),
                                 new Klosgen(),
                                 new MEstimate(),
                                 new PessimisticAccuracy(),
                                 new RelativeCostMeasure(),
                                 new SensitivitySpecificity() };

    /**
     * The pheromone matrix for the selection.
     */
    private Entry[] pheromone;

    /**
     * Default constructor.
     */
    public FunctionSelector() {
        pheromone = new Entry[FUNCTIONS.length];

        for (int i = 0; i < pheromone.length; i++) {
            pheromone[i] = new Entry();
            pheromone[i].setInitial(INITIAL_PHEROMONE);
            pheromone[i].set(0, INITIAL_PHEROMONE);
        }
    }

    /**
     * Returns the index of the selected function for the specified level.
     * 
     * @param level
     *            the current level.
     * 
     * @return the index of the selected function for the specified level.
     */
    public int select(int level) {
        double[] roulette = new double[pheromone.length];
        double total = 0.0;

        for (int i = 0; i < pheromone.length; i++) {
            roulette[i] = pheromone[i].value(level);
            total += pheromone[i].value(level);
        }

        double cumulative = 0.0;

        for (int i = 0; i < pheromone.length; i++) {
            if (roulette[i] > 0) {
                roulette[i] = cumulative + (roulette[i] / total);
                cumulative = roulette[i];
            }
        }

        for (int i = (roulette.length - 1); i >= 0; i--) {
            if (roulette[i] > 0) {
                roulette[i] = 1.0;
                break;
            }
        }

        double slot = CONFIG.get(RANDOM_GENERATOR).nextDouble();
        int selected = -1;

        for (int i = 0; i < roulette.length; i++) {
            if (slot < roulette[i]) {
                selected = i;
                break;
            }
        }

        return selected;
    }

    /**
     * Returns the rule evaluation function for the specified index.
     * 
     * @param index
     *            the index of the function.
     * 
     * @return the rule evaluation function for the specified index.
     */
    public RuleFunction get(int index) {
        return FUNCTIONS[index];
    }

    /**
     * Updates the pheromone values based on the functions used to create the
     * specified list of rules.
     * 
     * @param list
     *            the list of rules.
     * @param policy
     *            the pheromone policy instance.
     * 
     * @see PheromonePolicy
     */
    public void update(RuleList list, LevelPheromonePolicy policy) {
        final double factor = CONFIG.get(EVAPORATION_FACTOR);

        Rule[] rules = list.rules();
        double delta = list.getQuality().adjusted() / 5.0;

        double truncMin = precision(policy.min());
        double truncMax = precision(policy.max());

        for (int i = 0; i < rules.length; i++) {
            if (!rules[i].isEmpty()) {
                for (int j = 0; j < FUNCTIONS.length; j++) {
                    double value = pheromone[j].value(i);
                    value = value * factor;

                    if (j == rules[i].getFunction()) {
                        value = value + delta;
                    }

                    double truncValue = precision(value);

                    if (truncValue > truncMax) {
                        value = policy.max();
                    } else if (truncValue < truncMin) {
                        value = policy.min();
                    }

                    pheromone[j].set(i, value);
                }
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
    private double precision(double value) {
        return ((int) (value * 100)) / 100.0;
    }
}