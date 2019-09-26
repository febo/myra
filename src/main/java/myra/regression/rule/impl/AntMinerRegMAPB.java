/*
 * AntMinerRegMAPB.java
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

package myra.regression.rule.impl;

import static myra.Archive.ARCHIVE_SIZE;
import static myra.Archive.Q;
import static myra.Config.CONFIG;
import static myra.IterativeActivity.MAX_ITERATIONS;
import static myra.IterativeActivity.STAGNATION;
import static myra.Scheduler.COLONY_SIZE;
import static myra.Scheduler.PARALLEL;
import static myra.datamining.IntervalBuilder.DEFAULT_BUILDER;
import static myra.datamining.IntervalBuilder.MAXIMUM_LIMIT;
import static myra.datamining.VariableArchive.CONVERGENCE_SPEED;
import static myra.datamining.VariableArchive.PRECISION;
import static myra.regression.rule.function.RRMSECoverage.ALPHA;
import static myra.rule.Assignator.ASSIGNATOR;
import static myra.rule.Heuristic.DEFAULT_HEURISTIC;
import static myra.rule.Heuristic.DYNAMIC_HEURISTIC;
import static myra.rule.ListMeasure.DEFAULT_MEASURE;
import static myra.rule.ListPruner.DEFAULT_LIST_PRUNER;
import static myra.rule.Pruner.DEFAULT_PRUNER;
import static myra.rule.RuleFunction.DEFAULT_FUNCTION;
import static myra.rule.archive.ArchiveFindRuleListActivity.UNCOVERED;
import static myra.rule.pittsburgh.LevelPheromonePolicy.EVAPORATION_FACTOR;
import static myra.rule.pittsburgh.LevelPheromonePolicy.P_BEST;

import java.util.ArrayList;
import java.util.Collection;

import myra.Option;
import myra.Scheduler;
import myra.Option.BooleanOption;
import myra.Option.DoubleOption;
import myra.Option.IntegerOption;
import myra.classification.ClassificationModel;
import myra.classification.attribute.BoundarySplit;
import myra.classification.attribute.C45Split;
import myra.classification.attribute.MDLSplit;
import myra.classification.rule.EntropyHeuristic;
import myra.classification.rule.ListAccuracy;
import myra.classification.rule.PessimisticAccuracy;
import myra.classification.rule.SinglePassPruner;
import myra.classification.rule.function.Laplace;
import myra.datamining.Dataset;
import myra.datamining.IntervalBuilder;
import myra.datamining.Model;
import myra.regression.RRMSE;
import myra.regression.RRMSEListMeasure;
import myra.regression.RegressionModel;
import myra.regression.Regressor;
import myra.regression.SinglePassRegPruner;
import myra.regression.rule.MeanAssignator;
import myra.regression.rule.RegressionRule;
import myra.regression.rule.function.RRMSECoverage;
import myra.rule.BacktrackPruner;
import myra.rule.GreedyPruner;
import myra.rule.Heuristic;
import myra.rule.ListMeasure;
import myra.rule.ListPruner;
import myra.rule.Pruner;
import myra.rule.Rule;
import myra.rule.RuleFunction;
import myra.rule.RuleList;
import myra.rule.TopDownListPruner;
import myra.rule.archive.ArchiveFindRuleListActivity;
import myra.rule.archive.Graph;

/**
 * @author amh58
 */
public class AntMinerRegMAPB extends Regressor {
    @Override
    protected void defaults() {
	super.defaults();

	// configuration not set via command line

	CONFIG.set(ASSIGNATOR, new MeanAssignator());
	CONFIG.set(P_BEST, 0.05);
	CONFIG.set(IntervalBuilder.MAXIMUM_LIMIT, 25);
	CONFIG.set(Rule.DEFAULT_RULE, RegressionRule.class);

	// default configuration values
	CONFIG.set(ALPHA, 0.59);
	CONFIG.set(COLONY_SIZE, 10);
	CONFIG.set(ARCHIVE_SIZE, 10);
	CONFIG.set(Q, 0.369);
	CONFIG.set(CONVERGENCE_SPEED, 0.6795);
	CONFIG.set(PRECISION, 2.0);
	CONFIG.set(MAX_ITERATIONS, 500);
	CONFIG.set(IntervalBuilder.MINIMUM_CASES, 10);
	CONFIG.set(EVAPORATION_FACTOR, 0.9);
	CONFIG.set(DEFAULT_MEASURE, new RRMSEListMeasure());
	CONFIG.set(UNCOVERED, 0.01);
	CONFIG.set(STAGNATION, 40);
	CONFIG.set(DEFAULT_PRUNER, new SinglePassRegPruner());
	CONFIG.set(DEFAULT_LIST_PRUNER, new ListPruner.None());
	CONFIG.set(DEFAULT_FUNCTION, new RRMSECoverage());
	CONFIG.set(DEFAULT_HEURISTIC, new EntropyHeuristic.None());
	CONFIG.set(DYNAMIC_HEURISTIC, Boolean.FALSE);
	CONFIG.set(DEFAULT_BUILDER, new MDLSplit(new BoundarySplit()));
    }

    /* (non-Javadoc)
     * @see myra.datamining.Algorithm#train(myra.datamining.Dataset)
     */
    @Override
    protected Model train(Dataset dataset) {
	ArchiveFindRuleListActivity activity =
		new ArchiveFindRuleListActivity(new Graph(dataset), dataset);

	Scheduler<RuleList> scheduler = Scheduler.newInstance(1);
	scheduler.setActivity(activity);
	scheduler.run();

	return new RegressionModel(activity.getBest());
    }

    @Override
    protected Collection<Option<?>> options() {
	ArrayList<Option<?>> options = new ArrayList<Option<?>>();
	options.addAll(super.options());
	
	// minimum number of covered examples
	options.add(new IntegerOption(MAXIMUM_LIMIT,
			 "l",
			"set the maximum %s of covered examples per rule in the MDL",
			 "number"));
		// maximum number of iterations
	options.add(new DoubleOption(ALPHA,
				"a",
				"set the alpha %s value of RRMSECoverage",
				"percentage"));
	
	// archive size 
	options.add(new IntegerOption(ARCHIVE_SIZE,
			"v",
			"specify the size %s of the archive",
			"size"));
		
		
	// influence 
	options.add(new DoubleOption(Q,
					"q",
					"specify influnce  %s of the high quality rules",
					"influnce"));
		
	// convergence speed
	options.add(new DoubleOption(CONVERGENCE_SPEED,
				"s",
				"specify the %s speed of conversion",
				"size"));
		
	
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
	Option<Pruner> pruner = new Option<Pruner>(DEFAULT_PRUNER,
						   "p",
						   "specify the rule pruner %s",
						   true,
						   "method");
	pruner.add("greedy", new GreedyPruner());
	pruner.add("backtrack", CONFIG.get(DEFAULT_PRUNER));
	pruner.add("none", new Pruner.None());
	options.add(pruner);

	// list rule pruner
	Option<ListPruner> listPruner =
		new Option<ListPruner>(DEFAULT_LIST_PRUNER,
				       "z",
				       "specify the rule list pruner %s",
				       true,
				       "method");

	options.add(listPruner);

	// rule quality function
	Option<RuleFunction> function =
		new Option<RuleFunction>(DEFAULT_FUNCTION,
					 "r",
					 "specify the rule quality %s",
					 true,
					 "function");

	options.add(function);

	// rule quality function
	Option<ListMeasure> measure =
		new Option<ListMeasure>(DEFAULT_MEASURE,
					"l",
					"specify the rule list quality %s",
					true,
					"function");

	options.add(measure);

	// heuristic information
	Option<Heuristic> heuristic =
		new Option<Heuristic>(DEFAULT_HEURISTIC,
				      "h",
				      "specify the heuristic %s",
				      true,
				      "method");

	options.add(heuristic);

	// dynamic heuristic calculation
	BooleanOption dynamic =
		new BooleanOption(DYNAMIC_HEURISTIC,
				  "g",
				  "enables the dynamic heuristic computation");
	options.add(dynamic);

	// dynamic discretisation procedure
	Option<IntervalBuilder> builder =
		new Option<IntervalBuilder>(DEFAULT_BUILDER,
					    "d",
					    "specify the discretisation",
					    true,
					    "method");

	options.add(builder);

	return options;
    }

    @Override
    public String description() {
	return "Pittsburgh-based with Archive Ant-Miner-RegMA";
    }

    /**
     * <code><i>c</i>Ant-Miner-Reg<sub>MA<sub>PB</sub></sub></code> entry point.
     * 
     * @param args
     *            command-line arguments.
     * 
     * @throws Exception
     *             If an error occurs &mdash; e.g., I/O error.
     */
    public static void main(String[] args) throws Exception {
	AntMinerRegMAPB algorithm = new AntMinerRegMAPB();
	algorithm.run(args);
    }
}
