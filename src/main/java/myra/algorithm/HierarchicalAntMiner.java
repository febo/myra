/*
 * HierarchicalAntMiner.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2021 Fernando Esteban Barril Otero
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

package myra.algorithm;

import static myra.Config.CONFIG;
import static myra.IterativeActivity.MAX_ITERATIONS;
import static myra.IterativeActivity.STAGNATION;
import static myra.Scheduler.COLONY_SIZE;
import static myra.datamining.Hierarchy.IGNORE_LIST;
import static myra.datamining.IntervalBuilder.DEFAULT_BUILDER;
import static myra.rule.Assignator.ASSIGNATOR;
import static myra.rule.Heuristic.DEFAULT_HEURISTIC;
import static myra.rule.Heuristic.DYNAMIC_HEURISTIC;
import static myra.rule.Pruner.DEFAULT_PRUNER;
import static myra.rule.RuleFunction.DEFAULT_FUNCTION;
import static myra.rule.pittsburgh.FindRuleListActivity.UNCOVERED;
import static myra.rule.pittsburgh.LevelPheromonePolicy.EVAPORATION_FACTOR;
import static myra.rule.pittsburgh.LevelPheromonePolicy.P_BEST;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import myra.Option;
import myra.Option.DoubleOption;
import myra.classification.hierarchical.VarianceHeuristic;
import myra.classification.hierarchical.VarianceSplit;
import myra.classification.hierarchical.hFmeasure;
import myra.classification.rule.hierarchical.ProbabilisticRule;
import myra.classification.rule.hierarchical.Pruner;
import myra.classification.rule.hierarchical.SinglePathAssignator;
import myra.datamining.Dataset;
import myra.datamining.Hierarchy;
import myra.datamining.IntervalBuilder;
import myra.datamining.Model;
import myra.rule.Heuristic;
import myra.rule.Rule;
import myra.util.Logger;

/**
 * Default executable class file for the <code><i>hm</i>Ant-Miner</code>
 * algorithm.
 * 
 * @author Fernando Esteban Barril Otero
 * 
 * @since 5.0
 */
public class HierarchicalAntMiner extends ContinuousAntMiner {
    @Override
    protected void defaults() {
        super.defaults();

        // configuration not set via command line

        CONFIG.set(ASSIGNATOR, new SinglePathAssignator());
        CONFIG.set(P_BEST, 0.05);
        CONFIG.set(IntervalBuilder.MAXIMUM_LIMIT, 25);
        CONFIG.set(Rule.DEFAULT_RULE, ProbabilisticRule.class);
        CONFIG.set(DEFAULT_PRUNER, new Pruner());
        CONFIG.set(DEFAULT_FUNCTION,
                   new myra.classification.rule.function.hFmeasure());
        CONFIG.set(DEFAULT_BUILDER, new VarianceSplit());

        // default configuration values

        CONFIG.set(COLONY_SIZE, 5);
        CONFIG.set(MAX_ITERATIONS, 500);
        CONFIG.set(IntervalBuilder.MINIMUM_CASES, 10);
        CONFIG.set(EVAPORATION_FACTOR, 0.9);
        CONFIG.set(UNCOVERED, 0.01);
        CONFIG.set(STAGNATION, 40);
        CONFIG.set(DEFAULT_HEURISTIC, new VarianceHeuristic());
        CONFIG.set(DYNAMIC_HEURISTIC, Boolean.FALSE);
    }

    @Override
    protected void evaluate(Dataset dataset, Model model) {
        hFmeasure measure = new hFmeasure();
        double hF = measure.evaluate(dataset, model).raw();
        Logger.log("hF-measure on training set: %f\n", hF);
    }

    @Override
    protected void test(Dataset dataset, Model model) {
        hFmeasure measure = new hFmeasure();
        double hF = measure.evaluate(dataset, model).raw();
        Logger.log("hF-measure on test set: %f\n", hF);
    }

    @Override
    protected Collection<Option<?>> options() {
        List<Option<?>> options = new ArrayList<Option<?>>();
        options.addAll(super.options());

        removeOption(options, DEFAULT_FUNCTION);
        removeOption(options, DEFAULT_BUILDER);

        // evaporation factor
        options.add(new DoubleOption(EVAPORATION_FACTOR,
                                     "e",
                                     "set the MAX-MIN evaporation %s",
                                     "factor"));

        // heuristic information
        Option<Heuristic> heuristic =
                new Option<Heuristic>(DEFAULT_HEURISTIC,
                                      "h",
                                      "specify the heuristic %s",
                                      true,
                                      "method");
        heuristic.add("variance", CONFIG.get(DEFAULT_HEURISTIC));
        heuristic.add("none", new Heuristic.None());
        options.add(heuristic);

        // remove exclusive class labels
        Option<String> ignore =
                new Option<String>(IGNORE_LIST,
                                   "-ignore-root",
                                   "ignore node(s) from the quality calculations;"
                                           + " multiple nodes are separated by "
                                           + Hierarchy.SEPARATOR
                                           + "; root node is always ignored even if"
                                           + " no node is specified",
                                   true,
                                   "node(s)");
        options.add(ignore);

        return options;
    }

    @Override
    public String description() {
        return "Hierarchical Ant-Miner";
    }

    /**
     * <code><i>h</i>Ant-Miner</code> entry point.
     * 
     * @param args
     *            command-line arguments.
     * 
     * @throws Exception
     *             If an error occurs &mdash; e.g., I/O error.
     */
    public static void main(String[] args) throws Exception {
        HierarchicalAntMiner algorithm = new HierarchicalAntMiner();
        algorithm.run(args);
    }
}