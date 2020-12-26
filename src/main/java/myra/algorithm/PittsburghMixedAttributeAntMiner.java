/*
 * PittsburghMixedAttributeAntMiner.java
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

import static myra.Archive.ARCHIVE_SIZE;
import static myra.Archive.DEFAULT_Q;
import static myra.Archive.Q;
import static myra.Config.CONFIG;
import static myra.Scheduler.PARALLEL;
import static myra.datamining.IntervalBuilder.MINIMUM_CASES;
import static myra.datamining.VariableArchive.CONVERGENCE_SPEED;
import static myra.datamining.VariableArchive.DEFAULT_CONVERGENCE_SPEED;
import static myra.datamining.VariableArchive.PRECISION;
import static myra.rule.Assignator.ASSIGNATOR;
import static myra.rule.Heuristic.DEFAULT_HEURISTIC;
import static myra.rule.ListMeasure.DEFAULT_MEASURE;
import static myra.rule.ListPruner.DEFAULT_LIST_PRUNER;
import static myra.rule.Pruner.DEFAULT_PRUNER;
import static myra.rule.Rule.DEFAULT_RULE;
import static myra.rule.RuleFunction.DEFAULT_FUNCTION;
import static myra.IterativeActivity.MAX_ITERATIONS;
import static myra.IterativeActivity.STAGNATION;
import static myra.Scheduler.COLONY_SIZE;
import static myra.rule.pittsburgh.FindRuleListActivity.UNCOVERED;
import static myra.rule.pittsburgh.LevelPheromonePolicy.EVAPORATION_FACTOR;
import static myra.rule.pittsburgh.LevelPheromonePolicy.P_BEST;

import java.util.ArrayList;
import java.util.Collection;

import myra.IterativeActivity;
import myra.Option;
import myra.Option.DoubleOption;
import myra.Option.IntegerOption;
import myra.Scheduler;
import myra.classification.ClassificationModel;
import myra.classification.rule.ClassificationRule;
import myra.classification.rule.ListAccuracy;
import myra.classification.rule.MajorityAssignator;
import myra.classification.rule.PessimisticAccuracy;
import myra.classification.rule.RuleClassifier;
import myra.classification.rule.SinglePassPruner;
import myra.classification.rule.function.Accuracy;
import myra.classification.rule.function.Laplace;
import myra.classification.rule.function.MEstimate;
import myra.classification.rule.function.SensitivitySpecificity;
import myra.datamining.Dataset;
import myra.datamining.Model;
import myra.rule.Heuristic;
import myra.rule.ListMeasure;
import myra.rule.ListPruner;
import myra.rule.Pruner;
import myra.rule.RuleFunction;
import myra.rule.RuleList;
import myra.rule.TopDownListPruner;
import myra.rule.archive.ArchivePheromonePolicy;
import myra.rule.archive.ArchiveRuleFactory;
import myra.rule.archive.Graph;
import myra.rule.pittsburgh.FindRuleListActivity;

/**
 * Default executable class file for the
 * <code>Pittsburgh Mixed-Attribute Ant-Miner</code> algorithm.
 * 
 * @author Fernando Esteban Barril Otero
 * 
 * @since 5.0
 */
public class PittsburghMixedAttributeAntMiner extends RuleClassifier {
    @Override
    protected void defaults() {
        super.defaults();

        // configuration not set via command line

        CONFIG.set(P_BEST, 0.05);
        CONFIG.set(ASSIGNATOR, new MajorityAssignator());
        CONFIG.set(DEFAULT_HEURISTIC, new Heuristic.None());
        CONFIG.set(DEFAULT_RULE, ClassificationRule.class);
        CONFIG.set(PRECISION, 2.0);

        // default configuration values

        CONFIG.set(COLONY_SIZE, 100);
        CONFIG.set(MAX_ITERATIONS, 500);
        CONFIG.set(UNCOVERED, 0.01);
        CONFIG.set(MINIMUM_CASES, 10);
        CONFIG.set(STAGNATION, 10);
        CONFIG.set(EVAPORATION_FACTOR, 0.9);
        CONFIG.set(DEFAULT_PRUNER, new SinglePassPruner());
        CONFIG.set(DEFAULT_LIST_PRUNER, new ListPruner.None());
        CONFIG.set(DEFAULT_FUNCTION, new SensitivitySpecificity());
        CONFIG.set(DEFAULT_MEASURE, new PessimisticAccuracy());
        CONFIG.set(ARCHIVE_SIZE, 20);
        CONFIG.set(Q, DEFAULT_Q);
        CONFIG.set(CONVERGENCE_SPEED, DEFAULT_CONVERGENCE_SPEED);
    }

    @Override
    protected Collection<Option<?>> options() {
        ArrayList<Option<?>> options = new ArrayList<Option<?>>();
        options.addAll(super.options());

        // colony size
        options.add(new IntegerOption(COLONY_SIZE,
                                      "c",
                                      "specify the %s of the colony",
                                      "size"));

        // maximum number of iterations
        options.add(new IntegerOption(MAX_ITERATIONS,
                                      "i",
                                      "set the maximum %s of iterations",
                                      "number"));

        // support to parallel execution
        options.add(new IntegerOption(PARALLEL,
                                      "-parallel",
                                      "enable parallel execution in multiple %s;"
                                              + " if no cores are specified, use"
                                              + " all available cores",
                                      "cores") {
            @Override
            public void set(String value) {
                if (value == null) {
                    value = String
                            .format("%d",
                                    Runtime.getRuntime().availableProcessors());
                }

                super.set(value);
            }
        });

        // minimum number of covered examples
        options.add(new IntegerOption(MINIMUM_CASES,
                                      "m",
                                      "set the minimum %s of covered examples per rule",
                                      "number"));

        // number of uncovered examples
        options.add(new DoubleOption(UNCOVERED,
                                     "u",
                                     "set the %s of allowed uncovered examples",
                                     "percentage"));

        // convergence test
        options.add(new IntegerOption(STAGNATION,
                                      "x",
                                      "set the number of %s for convergence test",
                                      "iterations"));

        // evaporation factor
        options.add(new DoubleOption(EVAPORATION_FACTOR,
                                     "e",
                                     "set the MAX-MIN evaporation %s",
                                     "factor"));

        // rule pruner
        Option<Pruner> pruner = new Option<Pruner>(DEFAULT_PRUNER,
                                                   "p",
                                                   "specify the rule pruner %s",
                                                   true,
                                                   "method");
        pruner.add("single-pass", CONFIG.get(DEFAULT_PRUNER));
        pruner.add("none", new Pruner.None());
        options.add(pruner);

        // list rule pruner
        Option<ListPruner> listPruner =
                new Option<ListPruner>(DEFAULT_LIST_PRUNER,
                                       "z",
                                       "specify the rule list pruner %s",
                                       true,
                                       "method");
        listPruner.add("none", CONFIG.get(DEFAULT_LIST_PRUNER));
        listPruner.add("top-down", new TopDownListPruner());
        options.add(listPruner);

        // rule quality function
        Option<RuleFunction> function =
                new Option<RuleFunction>(DEFAULT_FUNCTION,
                                         "r",
                                         "specify the rule quality %s",
                                         true,
                                         "function");
        function.add("laplace", new Laplace());
        function.add("mestimate", new MEstimate());
        function.add("accurate", new Accuracy());
        function.add("sen_spe", CONFIG.get(DEFAULT_FUNCTION));
        options.add(function);

        // rule quality function
        Option<ListMeasure> measure =
                new Option<ListMeasure>(DEFAULT_MEASURE,
                                        "l",
                                        "specify the rule list quality %s",
                                        true,
                                        "function");
        measure.add("accuracy", new ListAccuracy());
        measure.add("pessimistic", CONFIG.get(DEFAULT_MEASURE));
        options.add(measure);

        // archive size
        options.add(new IntegerOption(ARCHIVE_SIZE,
                                      "a",
                                      "set the archive %s",
                                      "size"));

        // convergence speed
        options.add(new DoubleOption(CONVERGENCE_SPEED,
                                     "v",
                                     "specify the converge speed %s",
                                     "value"));

        // weight calculation q
        options.add(new DoubleOption(Q,
                                     "q",
                                     "specify the influence of best quality solutions %s",
                                     "value"));

        return options;
    }

    @Override
    protected Model train(Dataset dataset) {
        IterativeActivity<RuleList> activity =
                new FindRuleListActivity(new Graph(dataset),
                                         dataset,
                                         new ArchiveRuleFactory(),
                                         new ArchivePheromonePolicy());

        Scheduler<RuleList> scheduler = Scheduler.newInstance(1);
        scheduler.setActivity(activity);
        scheduler.run();

        return new ClassificationModel(activity.getBest());
    }

    @Override
    protected String description() {
        return "Mixed-Attribute Ant-Miner";
    }

    /**
     * <code>Pittsburgh Mixed-Attribute Ant-Miner</code> entry point.
     * 
     * @param args
     *            command-line arguments.
     * 
     * @throws Exception
     *             If an error occurs &mdash; e.g., I/O error.
     */
    public static void main(String[] args) throws Exception {
        PittsburghMixedAttributeAntMiner algorithm =
                new PittsburghMixedAttributeAntMiner();
        algorithm.run(args);
    }
}