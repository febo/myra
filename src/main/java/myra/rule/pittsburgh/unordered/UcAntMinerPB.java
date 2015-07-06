/*
 * UcAntMinerPB.java
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

package myra.rule.pittsburgh.unordered;

import static myra.Config.CONFIG;
import static myra.IterativeActivity.MAX_ITERATIONS;
import static myra.IterativeActivity.STAGNATION;
import static myra.Scheduler.COLONY_SIZE;
import static myra.Scheduler.PARALLEL;
import static myra.interval.IntervalBuilder.DEFAULT_BUILDER;
import static myra.interval.IntervalBuilder.MINIMUM_CASES;
import static myra.rule.Assignator.ASSIGNATOR;
import static myra.rule.ConflictResolution.CONFIDENCE;
import static myra.rule.Heuristic.DEFAULT_HEURISTIC;
import static myra.rule.Heuristic.DYNAMIC_HEURISTIC;
import static myra.rule.ListMeasure.DEFAULT_MEASURE;
import static myra.rule.Pruner.DEFAULT_PRUNER;
import static myra.rule.RuleFunction.DEFAULT_FUNCTION;
import static myra.rule.RuleSet.CONFLICT_RESOLUTION;
import static myra.rule.pittsburgh.FindRuleListActivity.UNCOVERED;
import static myra.rule.pittsburgh.LevelPheromonePolicy.EVAPORATION_FACTOR;
import static myra.rule.pittsburgh.LevelPheromonePolicy.P_BEST;
import static myra.rule.pittsburgh.unordered.FindRuleSetActivity.DYNAMIC_FUNCTION;

import java.util.ArrayList;
import java.util.Collection;

import myra.Classifier;
import myra.Dataset;
import myra.Model;
import myra.Option;
import myra.Option.BooleanOption;
import myra.Option.DoubleOption;
import myra.Option.IntegerOption;
import myra.Scheduler;
import myra.interval.BinaryMDLSplit;
import myra.interval.BoundaryLaplaceSplit;
import myra.interval.IntervalBuilder;
import myra.rule.BacktrackPruner;
import myra.rule.ClassFrequencyHeuristic;
import myra.rule.ConflictResolution;
import myra.rule.Graph;
import myra.rule.GreedyPruner;
import myra.rule.Heuristic;
import myra.rule.ListMeasure;
import myra.rule.NoClassAssignator;
import myra.rule.PessimisticAccuracy;
import myra.rule.Pruner;
import myra.rule.RuleFunction;
import myra.rule.RuleList;
import myra.rule.function.Laplace;
import myra.rule.function.SensitivitySpecificity;

/**
 * Default executable class file for the
 * <code>Unordered <i>c</i>Ant-Miner<sub>PB</sub></code> algorithm. The full
 * implementation is described in the following paper:
 *
 * <pre>
 * &#64;INPROCEEDINGS{Otero13unordered,
 *    author    = {F.E.B. Otero and A.A. Freitas},
 *    title     = {Improving the Interpretability of Classification Rules Discovered by an Ant Colony Algorithm},
 *    booktitle = {Proceedings of the Genetic and Evolutionary Computation Conference (GECCO'13)},
 *    publisher = {ACM Press},
 *    pages     = {73--80},
 *    year      = {2013}
 * }
 * </pre>
 * 
 * @author Fernando Esteban Barril Otero
 */
public class UcAntMinerPB extends Classifier {
    @Override
    protected void defaults() {
	super.defaults();

	// configuration not set via command line

	CONFIG.set(ASSIGNATOR, new NoClassAssignator());
	CONFIG.set(P_BEST, 0.05);
	CONFIG.set(IntervalBuilder.MAXIMUM_LIMIT, 25);

	// default configuration values

	CONFIG.set(COLONY_SIZE, 5);
	CONFIG.set(MAX_ITERATIONS, 500);
	CONFIG.set(MINIMUM_CASES, 10);
	CONFIG.set(EVAPORATION_FACTOR, 0.9);
	CONFIG.set(DEFAULT_MEASURE, new ListMeasure.Accuracy());
	CONFIG.set(UNCOVERED, 0.01);
	CONFIG.set(STAGNATION, 40);
	CONFIG.set(DEFAULT_PRUNER, new BacktrackPruner());
	CONFIG.set(DEFAULT_FUNCTION, new SensitivitySpecificity());
	CONFIG.set(DEFAULT_HEURISTIC, new ClassFrequencyHeuristic());
	CONFIG.set(DYNAMIC_HEURISTIC, Boolean.FALSE);
	CONFIG.set(DYNAMIC_FUNCTION, Boolean.TRUE);
	CONFIG.set(DEFAULT_BUILDER, new BoundaryLaplaceSplit());
	CONFIG.set(CONFLICT_RESOLUTION, CONFIDENCE);
    }

    @Override
    protected Model train(Dataset dataset) {
	FindRuleSetActivity activity =
		new FindRuleSetActivity(new Graph(dataset), dataset);

	Scheduler<RuleList> scheduler = Scheduler.newInstance(1);
	scheduler.setActivity(activity);
	scheduler.run();

	return activity.getBest();
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
	options.add(new IntegerOption(IntervalBuilder.MINIMUM_CASES,
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
	Option<Pruner> pruner =
		new Option<>(DEFAULT_PRUNER,
			     "p",
			     "specify the rule pruner %s",
			     true,
			     "method");
	pruner.add("greedy", new GreedyPruner());
	pruner.add("backtrack", CONFIG.get(DEFAULT_PRUNER));
	options.add(pruner);

	// rule quality function
	Option<RuleFunction> function =
		new Option<>(DEFAULT_FUNCTION,
			     "r",
			     "specify the rule quality %s",
			     true,
			     "function");
	function.add("laplace", new Laplace());
	function.add("sen_spe", CONFIG.get(DEFAULT_FUNCTION));
	options.add(function);

	// rule quality function
	Option<ListMeasure> measure = new Option<>(DEFAULT_MEASURE,
						   "l",
						   "specify the rule list quality %s",
						   true,
						   "function");
	measure.add("accuracy", CONFIG.get(DEFAULT_MEASURE));
	measure.add("pessimistic", new PessimisticAccuracy());
	options.add(measure);

	// heuristic information
	Option<Heuristic> heuristic =
		new Option<>(DEFAULT_HEURISTIC,
			     "h",
			     "specify the heuristic %s",
			     true,
			     "method");
	heuristic.add("frequency", CONFIG.get(DEFAULT_HEURISTIC));
	heuristic.add("none", new Heuristic.None());
	options.add(heuristic);

	// dynamic heuristic calculation
	BooleanOption dynamic =
		new BooleanOption(DYNAMIC_HEURISTIC,
				  "g",
				  "enables the dynamic heuristic computation");
	options.add(dynamic);

	// dynamic discretisation procedure
	Option<IntervalBuilder> builder =
		new Option<>(DEFAULT_BUILDER,
			     "d",
			     "specify the discretisation",
			     true,
			     "method");
	builder.add("binary", new BinaryMDLSplit());
	builder.add("laplace", CONFIG.get(DEFAULT_BUILDER));
	options.add(builder);

	// dynamic function selector
	BooleanOption selector =
		new BooleanOption(DYNAMIC_FUNCTION,
				  "-selector",
				  "enable the dynamic function selector");
	options.add(selector);

	// dynamic function selector
	Option<ConflictResolution> conflict = new Option<>(CONFLICT_RESOLUTION,
							   "-conflict",
							   "specify the conflict resolution %s",
							   true,
							   "strategy");
	conflict.add("confidence", CONFIG.get(CONFLICT_RESOLUTION));
	conflict.add("frequency", ConflictResolution.FREQUENT_CLASS);
	conflict.add("weighted", ConflictResolution.WEIGHTED_FREQUENCY);
	options.add(conflict);

	return options;
    }

    @Override
    public String description() {
	return "Unordered Pittsburgh-based cAnt-Miner";
    }

    /**
     * <code>Unordered <i>c</i>Ant-Miner<sub>PB</sub></code> entry point.
     * 
     * @param args
     *            command-line arguments.
     * 
     * @throws Exception
     *             If an error occurs &mdash; e.g., I/O error.
     */
    public static void main(String[] args) throws Exception {
	UcAntMinerPB algorithm = new UcAntMinerPB();
	algorithm.run(args);
    }
}