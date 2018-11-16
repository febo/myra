/*
 * AntMinerRegMA.java
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
import static myra.datamining.IntervalBuilder.MINIMUM_CASES;
import static myra.datamining.VariableArchive.CONVERGENCE_SPEED;
import static myra.datamining.VariableArchive.PRECISION;
import static myra.rule.Assignator.ASSIGNATOR;
import static myra.rule.Heuristic.DEFAULT_HEURISTIC;
import static myra.rule.Heuristic.DYNAMIC_HEURISTIC;
import static myra.rule.Pruner.DEFAULT_PRUNER;
import static myra.rule.RuleFunction.DEFAULT_FUNCTION;
import static myra.rule.irl.PheromonePolicy.DEFAULT_POLICY;
import static myra.rule.irl.RuleFactory.DEFAULT_FACTORY;
import static myra.rule.irl.SequentialCovering.UNCOVERED;

import java.util.ArrayList;
import java.util.Collection;

import myra.Option;
import myra.Option.IntegerOption;
import myra.datamining.Dataset;
import myra.datamining.Model;
import myra.regression.RegressionModel;
import myra.regression.Regressor;
import myra.regression.SinglePassRegPruner;
import myra.regression.attribute.StandardDeviationSplit;
import myra.regression.rule.MeanAssignator;
import myra.regression.rule.RegressionRule;
import myra.regression.rule.function.RRMSECoverage;
import myra.rule.Heuristic;
import myra.rule.Rule;
import myra.rule.irl.EdgeArchivePhermonePolicy;
import myra.rule.irl.EdgeArchiveRuleFactory;
import myra.rule.irl.SequentialCoveringArchive;

/**
 * @author amh58
 */
public class AntMinerRegMA extends Regressor {
    @Override
    protected void defaults() {
	super.defaults();

	// configuration not set via command line

	CONFIG.set(ASSIGNATOR, new MeanAssignator());
	CONFIG.set(DEFAULT_FACTORY, new EdgeArchiveRuleFactory());
	CONFIG.set(DEFAULT_POLICY, new EdgeArchivePhermonePolicy());
	CONFIG.set(Rule.DEFAULT_RULE, RegressionRule.class);

	CONFIG.set(DEFAULT_BUILDER, new StandardDeviationSplit());
	CONFIG.set(DEFAULT_PRUNER, new SinglePassRegPruner());
	CONFIG.set(DEFAULT_FUNCTION, new RRMSECoverage());
	CONFIG.set(DEFAULT_HEURISTIC, new Heuristic.None());
	CONFIG.set(DYNAMIC_HEURISTIC, Boolean.FALSE);

	// default configuration values
	CONFIG.set(ARCHIVE_SIZE, 10);
	CONFIG.set(Q, 0.369);
	CONFIG.set(CONVERGENCE_SPEED, 0.6795);
	CONFIG.set(PRECISION, 2.0);
	CONFIG.set(COLONY_SIZE, 10);
	CONFIG.set(MAX_ITERATIONS, 1500);
	CONFIG.set(MINIMUM_CASES, 10);
	CONFIG.set(MAXIMUM_LIMIT, 25);
	CONFIG.set(UNCOVERED, 10);
	CONFIG.set(STAGNATION, 10);
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
	options.add(new IntegerOption(UNCOVERED,
				      "u",
				      "set the allowed %s of uncovered examples",
				      "number"));

	// convergence test
	options.add(new IntegerOption(STAGNATION,
				      "x",
				      "set the number of %s for convergence test",
				      "iterations"));

	return options;
    }

    @Override
    protected Model train(Dataset dataset) {
	SequentialCoveringArchive seco = new SequentialCoveringArchive();
	return new RegressionModel(seco.train(dataset));
    }

    @Override
    protected String description() {
	return "Ant-Miner-Reg regression rule induction";
    }

    /**
     * <code>Ant-Miner-Reg</code> entry point.
     * 
     * @param args
     *            command-line arguments.
     * 
     * @throws Exception
     *             If an error occurs &mdash; e.g., I/O error.
     */
    public static void main(String[] args) throws Exception {
	AntMinerRegMA algorithm = new AntMinerRegMA();
	algorithm.run(args);
    }
}

