/*
 * AntMinerMA.java
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

package myra.classification.rule.impl;

import static myra.Archive.ARCHIVE_SIZE;
import static myra.Archive.Q;
import static myra.Config.CONFIG;
import static myra.IterativeActivity.MAX_ITERATIONS;
import static myra.IterativeActivity.STAGNATION;
import static myra.Scheduler.COLONY_SIZE;
import static myra.datamining.IntervalBuilder.DEFAULT_BUILDER;
import static myra.datamining.VariableArchive.CONVERGENCE_SPEED;
import static myra.datamining.VariableArchive.PRECISION;
import static myra.rule.Pruner.DEFAULT_PRUNER;
import static myra.rule.RuleFunction.DEFAULT_FUNCTION;
import static myra.rule.archive.ArchiveFindRuleListActivity.UNCOVERED;
import static myra.rule.irl.PheromonePolicy.DEFAULT_POLICY;
import static myra.rule.irl.RuleFactory.DEFAULT_FACTORY;
import static myra.rule.pittsburgh.LevelPheromonePolicy.EVAPORATION_FACTOR;

import java.util.ArrayList;
import java.util.Collection;

import myra.Option;
import myra.Option.DoubleOption;
import myra.Option.IntegerOption;
import myra.classification.ClassificationModel;
import myra.classification.attribute.BoundarySplit;
import myra.classification.attribute.C45Split;
import myra.classification.attribute.MDLSplit;
import myra.classification.rule.SinglePassPruner;
import myra.classification.rule.function.SensitivitySpecificity;
import myra.datamining.Dataset;
import myra.datamining.IntervalBuilder;

import myra.rule.irl.EdgeArchivePhermonePolicy;
import myra.rule.irl.EdgeArchiveRuleFactory;
import myra.rule.irl.SequentialCovering;
import myra.rule.irl.SequentialCoveringArchive;


/**
 * @author amh58
 */
public class AntMinerMA  extends AntMiner {
    @Override
    protected void defaults() {
	super.defaults();

	// configuration not set via command line

	CONFIG.set(DEFAULT_FACTORY, new EdgeArchiveRuleFactory());
	CONFIG.set(DEFAULT_POLICY, new EdgeArchivePhermonePolicy());
	CONFIG.set(ARCHIVE_SIZE, 10);
	CONFIG.set(Q, 0.369);
	CONFIG.set(CONVERGENCE_SPEED, 0.6795);
	CONFIG.set(PRECISION, 2.0);
	// default configuration values
	CONFIG.set(DEFAULT_PRUNER, new SinglePassPruner());
	CONFIG.set(DEFAULT_BUILDER, new MDLSplit(new BoundarySplit(), false));

	
    }
    
    @Override
    public ClassificationModel train(Dataset dataset) {
	SequentialCoveringArchive seco = new SequentialCoveringArchive();
	return new ClassificationModel(seco.train(dataset));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Collection<Option<?>> options() {
	ArrayList<Option<?>> options = new ArrayList<Option<?>>();
	options.addAll(super.options());
	

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
				
	// discretisation
	Option<IntervalBuilder> builder =
		new Option<IntervalBuilder>(DEFAULT_BUILDER,
					    "d",
					    "specify the discretisation %s",
					    true,
					    "method");
	builder.add("c45", new C45Split());
	builder.add("mdl", CONFIG.get(DEFAULT_BUILDER));
	options.add(builder);

	

	return options;
    }

    @Override
    public String description() {
	return "Ant-MinerMA rule induction";
    }

    /**
     * <code><i>c</i>Ant-Miner</code> entry point.
     * 
     * @param args
     *            command-line arguments.
     * 
     * @throws Exception
     *             If an error occurs &mdash; e.g., I/O error.
     */
    public static void main(String[] args) throws Exception {
	AntMinerMA algorithm = new AntMinerMA();
	algorithm.run(args);
    }
}