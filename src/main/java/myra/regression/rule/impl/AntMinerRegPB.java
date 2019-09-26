/*
 * AntMinerRegPB.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2016 Fernando Esteban Barril Otero
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

import static myra.Config.CONFIG;
import static myra.IterativeActivity.MAX_ITERATIONS;
import static myra.IterativeActivity.STAGNATION;
import static myra.Scheduler.COLONY_SIZE;
import static myra.Scheduler.PARALLEL;
import static myra.datamining.IntervalBuilder.DEFAULT_BUILDER;
import static myra.datamining.IntervalBuilder.MAXIMUM_LIMIT;
import static myra.datamining.IntervalBuilder.MINIMUM_CASES;
import static myra.regression.rule.function.RRMSECoverage.ALPHA;
import static myra.rule.Assignator.ASSIGNATOR;
import static myra.rule.Heuristic.DEFAULT_HEURISTIC;
import static myra.rule.Heuristic.DYNAMIC_HEURISTIC;
import static myra.rule.ListMeasure.DEFAULT_MEASURE;
import static myra.rule.ListPruner.DEFAULT_LIST_PRUNER;
import static myra.rule.Pruner.DEFAULT_PRUNER;
import static myra.rule.RuleFunction.DEFAULT_FUNCTION;
import static myra.rule.pittsburgh.FindRuleListActivity.UNCOVERED;
import static myra.rule.pittsburgh.LevelPheromonePolicy.EVAPORATION_FACTOR;
import static myra.rule.pittsburgh.LevelPheromonePolicy.P_BEST;


import java.util.ArrayList;
import java.util.Collection;

import myra.Option;
import myra.Scheduler;
import myra.Option.DoubleOption;
import myra.Option.IntegerOption;
import myra.datamining.Dataset;
import myra.datamining.Model;
import myra.regression.RRMSEListMeasure;
import myra.regression.RegressionModel;
import myra.regression.Regressor;
import myra.regression.attribute.StandardDeviationSplit;
import myra.regression.rule.MeanAssignator;
import myra.regression.rule.RegressionRule;
import myra.regression.rule.function.RRMSECoverage;
import myra.rule.BacktrackPruner;
import myra.rule.Graph;
import myra.rule.Heuristic;
import myra.rule.ListPruner;
import myra.rule.Rule;
import myra.rule.RuleList;
import myra.rule.pittsburgh.FindRuleListActivity;

/**
 * Implementation of a Pittsburgh based <code>Ant-Miner-Reg</code> algorithm
 * <code>Ant-Miner-Reg<sub>PB</sub></code>.
 * 
 * @author James Brookhouse
 *
 */
public class AntMinerRegPB extends Regressor {

	protected void defaults() {
		super.defaults();

		// configuration not set via command line
		CONFIG.set(ASSIGNATOR, new MeanAssignator());
		CONFIG.set(Rule.DEFAULT_RULE, RegressionRule.class);

		CONFIG.set(DEFAULT_BUILDER, new StandardDeviationSplit());
		CONFIG.set(DEFAULT_PRUNER, new BacktrackPruner());
		CONFIG.set(DEFAULT_FUNCTION, new RRMSECoverage());
		CONFIG.set(DEFAULT_HEURISTIC, new Heuristic.None());
		CONFIG.set(DYNAMIC_HEURISTIC, Boolean.FALSE);
		CONFIG.set(DEFAULT_LIST_PRUNER, new ListPruner.None());
		CONFIG.set(DEFAULT_MEASURE, new RRMSEListMeasure());
		CONFIG.set(P_BEST, 0.05);

		// default configuration values
		CONFIG.set(ALPHA, 0.59);
		CONFIG.set(COLONY_SIZE, 10);
		CONFIG.set(MAX_ITERATIONS, 500);
		CONFIG.set(MINIMUM_CASES, 10);
		CONFIG.set(MAXIMUM_LIMIT, 25);
		CONFIG.set(UNCOVERED, 0.01);
		CONFIG.set(STAGNATION, 10);
		CONFIG.set(EVAPORATION_FACTOR, 0.9);
	}

	@Override
	protected Collection<Option<?>> options() {
		ArrayList<Option<?>> options = new ArrayList<Option<?>>();
		options.addAll(super.options());

		// colony size
		options.add(new IntegerOption(COLONY_SIZE, "c", "specify the %s of the colony", "size"));

		// maximum number of iterations
		options.add(new IntegerOption(MAX_ITERATIONS, "i", "set the maximum %s of iterations", "number"));

		// support to parallel execution
		options.add(new IntegerOption(PARALLEL, "-parallel", "enable parallel execution in multiple %s;"
				+ " if no cores are specified, use" + " all available cores", "cores") {
			@Override
			public void set(String value) {
				if (value == null) {
					value = String.format("%d", Runtime.getRuntime().availableProcessors());
				}

				super.set(value);
			}
		});

		// minimum number of covered examples
		options.add(new IntegerOption(MINIMUM_CASES, "m", "set the minimum %s of covered examples per rule", "number"));

		// number of uncovered examples
		options.add(new DoubleOption(UNCOVERED,"u","set the %s of allowed uncovered examples","percentage"));

		// convergence test
		options.add(new IntegerOption(STAGNATION, "x", "set the number of %s for convergence test", "iterations"));
		
		// evaporation factor
		options.add(new DoubleOption(EVAPORATION_FACTOR,"e","set the MAX-MIN evaporation %s","factor"));
		
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
		return options;
	}

	@Override
	protected Model train(Dataset dataset) {
		FindRuleListActivity activity = new FindRuleListActivity(new Graph(dataset), dataset);

		Scheduler<RuleList> scheduler = Scheduler.newInstance(1);
		scheduler.setActivity(activity);
		scheduler.run();

		return new RegressionModel(activity.getBest());
	}

	@Override
	protected String description() {
		return "Pittsburgh based Ant-Miner-Reg regression rule induction";
	}

	/**
	 * <code>Ant-Miner-Reg<sub>PB</sub></code> entry point.
	 * 
	 * @param args
	 *            command-line arguments.
	 * 
	 * @throws Exception
	 *             If an error occurs &mdash; e.g., I/O error.
	 */
	public static void main(String[] args) throws Exception {
		AntMinerRegPB algorithm = new AntMinerRegPB();
		algorithm.run(args);
	}
}
