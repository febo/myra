/*
 * HierarchicalMultiLabelAntMiner.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2020 Fernando Esteban Barril Otero
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
import static myra.classification.hierarchical.WeightedAUPRC.Type.FREQUENCY;
import static myra.datamining.IntervalBuilder.DEFAULT_BUILDER;
import static myra.rule.Assignator.ASSIGNATOR;
import static myra.rule.Heuristic.DEFAULT_HEURISTIC;
import static myra.rule.Heuristic.DYNAMIC_HEURISTIC;
import static myra.rule.ListMeasure.DEFAULT_MEASURE;
import static myra.rule.Pruner.DEFAULT_PRUNER;
import static myra.rule.RuleFunction.DEFAULT_FUNCTION;
import static myra.rule.pittsburgh.FindRuleListActivity.UNCOVERED;
import static myra.rule.pittsburgh.LevelPheromonePolicy.EVAPORATION_FACTOR;
import static myra.rule.pittsburgh.LevelPheromonePolicy.P_BEST;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import myra.Config.ConfigKey;
import myra.Option;
import myra.Option.DoubleOption;
import myra.classification.hierarchical.AUPRC;
import myra.classification.hierarchical.VarianceHeuristic;
import myra.classification.hierarchical.VarianceSplit;
import myra.classification.hierarchical.WeightedAUPRC;
import myra.classification.rule.hierarchical.ListAUPRC;
import myra.classification.rule.hierarchical.ProbabilisticAssignator;
import myra.classification.rule.hierarchical.ProbabilisticRule;
import myra.classification.rule.hierarchical.VarianceGain;
import myra.datamining.Dataset;
import myra.datamining.Hierarchy;
import myra.datamining.IntervalBuilder;
import myra.datamining.Model;
import myra.rule.BacktrackPruner;
import myra.rule.Heuristic;
import myra.rule.Rule;
import myra.util.Logger;

/**
 * Default executable class file for the <code><i>hm</i>Ant-Miner</code>
 * algorithm.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class HierarchicalMultiLabelAntMiner extends ContinuousAntMiner {
    /**
     * The config key for the flag to indicate that non-exclusive class labels
     * should not be predicted.
     */
    public final static ConfigKey<Boolean> NON_EXCLUSIVE = new ConfigKey<>();

    @Override
    protected void defaults() {
        super.defaults();

        // configuration not set via command line

        CONFIG.set(ASSIGNATOR, new ProbabilisticAssignator());
        CONFIG.set(P_BEST, 0.05);
        CONFIG.set(IntervalBuilder.MAXIMUM_LIMIT, 25);
        CONFIG.set(Rule.DEFAULT_RULE, ProbabilisticRule.class);
        CONFIG.set(Hierarchy.WEIGHT, Hierarchy.DEFAULT_WEIGHT);
        CONFIG.set(DEFAULT_MEASURE, new ListAUPRC());
        CONFIG.set(DEFAULT_PRUNER, new BacktrackPruner());
        CONFIG.set(DEFAULT_FUNCTION, new VarianceGain());
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
        CONFIG.set(NON_EXCLUSIVE, Boolean.TRUE);
    }

    @Override
    protected void evaluate(Dataset dataset, Model model) {
        AUPRC measure = new AUPRC();
        double area1 = measure.evaluate(dataset, model).raw();
        measure = new WeightedAUPRC();
        double area2 = measure.evaluate(dataset, model).raw();
        measure = new WeightedAUPRC(FREQUENCY);
        double area3 = measure.evaluate(dataset, model).raw();

        Logger.log("Evaluation on training set: Area Under the Precision-Recall Curve%n");
        Logger.log("-> AU(PRC) = %.4f%n", area1);
        Logger.log("-> AUPRC   = %.4f%n", area2);
        Logger.log("-> AUPRCw  = %.4f%n", area3);
    }

    @Override
    protected void test(Dataset dataset, Model model) {
        AUPRC measure = new AUPRC();
        double area1 = measure.evaluate(dataset, model).raw();
        measure = new WeightedAUPRC();
        double area2 = measure.evaluate(dataset, model).raw();
        measure = new WeightedAUPRC(FREQUENCY);
        double area3 = measure.evaluate(dataset, model).raw();

        Logger.log("Hierarchical evaluation: Area Under the Precision-Recall Curve%n");
        Logger.log("-> AU(PRC) = %.4f%n", area1);
        Logger.log("-> AUPRC   = %.4f%n", area2);
        Logger.log("-> AUPRCw  = %.4f%n", area3);
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
                new Option<String>(AUPRC.IGNORE_LIST,
                                   "-ignore-root",
                                   "ignore root %s from the AUPRC calculation",
                                   false,
                                   "nodes");
        options.add(ignore);

        return options;
    }

    @Override
    public String description() {
        return "Hierarchical Multi-Label Ant-Miner";
    }

    /**
     * <code><i>hm</i>Ant-Miner</code> entry point.
     * 
     * @param args
     *            command-line arguments.
     * 
     * @throws Exception
     *             If an error occurs &mdash; e.g., I/O error.
     */
    public static void main(String[] args) throws Exception {
        HierarchicalMultiLabelAntMiner algorithm =
                new HierarchicalMultiLabelAntMiner();
        algorithm.run(args);
    }
}