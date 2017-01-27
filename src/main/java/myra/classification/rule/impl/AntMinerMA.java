/*
 * AntMinerMA.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2017 Fernando Esteban Barril Otero
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

import static myra.Config.CONFIG;

import static myra.datamining.IntervalBuilder.DEFAULT_BUILDER;
import static myra.rule.Pruner.DEFAULT_PRUNER;
import static myra.rule.irl.PheromonePolicy.DEFAULT_POLICY;
import static myra.rule.irl.RuleFactory.DEFAULT_FACTORY;
import static myra.rule.irl.ArchiveSequentialCovering.UNCOVERED;
import static myra.datamining.AttributeArchive.ARCHIVE_SIZE;
import static myra.datamining.AttributeArchive.Q;
import static myra.datamining.AttributeArchive.DEFAULT_Q;
import static myra.datamining.AttributeArchive.CONVERGENCE;
import static myra.datamining.AttributeArchive.DEFAULT_CONVERGENCE;


import java.util.ArrayList;
import java.util.Collection;

import myra.Option;
import myra.classification.ClassificationModel;
import myra.classification.attribute.BoundarySplit;
import myra.classification.attribute.C45Split;
import myra.classification.attribute.MDLSplit;
import myra.datamining.Dataset;
import myra.datamining.IntervalBuilder;
import myra.rule.BacktrackPruner;
import myra.rule.GreedyPruner;
import myra.rule.Pruner;
import myra.rule.irl.ArchivePhermonePolicy;
import myra.rule.irl.ArchiveRuleFactory;
import myra.rule.irl.ArchiveSequentialCovering;


/**
 * @author amh58
 *
 */
public class AntMinerMA extends AntMiner {
    @Override
    protected void defaults() {
	super.defaults();

	// configuration not set via command line

	CONFIG.set(DEFAULT_FACTORY, new ArchiveRuleFactory());
	CONFIG.set(DEFAULT_POLICY, new ArchivePhermonePolicy());

	// default configuration values

	CONFIG.set(DEFAULT_BUILDER, new MDLSplit(new BoundarySplit(), false));
	CONFIG.set(DEFAULT_PRUNER, new BacktrackPruner());
	CONFIG.set(UNCOVERED, 10);
	CONFIG.set(ARCHIVE_SIZE, 10);
	CONFIG.set(Q, DEFAULT_Q);
	CONFIG.set(CONVERGENCE, DEFAULT_CONVERGENCE);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Collection<Option<?>> options() {
	ArrayList<Option<?>> options = new ArrayList<Option<?>>();
	options.addAll(super.options());

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

	// replaces the default pruner method
	for (Option<?> option : options) {
	    if (option.getKey() == DEFAULT_PRUNER) {
		Option<Pruner> pruner = (Option<Pruner>) option;
		pruner.add("greedy", new GreedyPruner());
		pruner.add("backtrack", CONFIG.get(DEFAULT_PRUNER));
	    }
	}

	return options;
    }

    @Override
    public String description() {
	return "Ant-MinerMA rule induction";
    }
    
    @Override
    public ClassificationModel train(Dataset dataset) {
    	ArchiveSequentialCovering seco = new ArchiveSequentialCovering();
    	return new ClassificationModel(seco.train(dataset));
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