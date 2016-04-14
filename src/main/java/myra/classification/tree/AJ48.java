/*
 * AJ48.java
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

package myra.classification.tree;

import static myra.Config.CONFIG;
import static myra.classification.tree.AbstractPruner.DEFAULT_PRUNER;
import static myra.classification.tree.GainRatioHeuristic.FILTER_GAIN;
import static myra.classification.tree.Heuristic.DEFAULT_HEURISTIC;
import static myra.data.Dataset.RULE_COVERED;
import static myra.data.IntervalBuilder.DEFAULT_BUILDER;
import static myra.data.IntervalBuilder.MAXIMUM_LIMIT;
import static myra.data.IntervalBuilder.MINIMUM_CASES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import myra.Option;
import myra.Option.IntegerOption;
import myra.classification.ClassificationModel;
import myra.classification.Classifier;
import myra.classification.attribute.BoundarySplit;
import myra.classification.attribute.C45Split;
import myra.classification.attribute.MDLSplit;
import myra.data.Dataset;
import myra.data.Dataset.Instance;
import myra.data.IntervalBuilder;

/**
 * This class represents <b>A</b>nother <b>J</b>ava implementation of C<b>4</b>.5 Release <b>8</b>
 * (<code>AJ48</code>) algorithm.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class AJ48 extends Classifier {
    @Override
    protected void defaults() {
	super.defaults();
	
	// configuration not set via command line

	CONFIG.set(MAXIMUM_LIMIT, 25);
	CONFIG.set(FILTER_GAIN, true);
	
	// default configuration values

	CONFIG.set(MINIMUM_CASES, 2);
	CONFIG.set(DEFAULT_PRUNER, new PessimisticPruner());
	CONFIG.set(DEFAULT_HEURISTIC, new GainRatioHeuristic());
	CONFIG.set(DEFAULT_BUILDER, new C45Split());
    }

    @Override
    protected Collection<Option<?>> options() {
	ArrayList<Option<?>> options = new ArrayList<Option<?>>();
	options.addAll(super.options());

	// minimum number of covered examples
	options.add(new IntegerOption(MINIMUM_CASES,
				      "m",
				      "set the minimum %s of covered examples per rule",
				      "number"));

	// tree pruner
	Option<AbstractPruner> pruner =
		new Option<>(DEFAULT_PRUNER,
			     "p",
			     "specify the rule pruner %s",
			     true,
			     "method");
	pruner.add("accuracy", new AccuracyPruner());
	pruner.add("pessimistic", CONFIG.get(DEFAULT_PRUNER));
	options.add(pruner);

	// heuristic information
	Option<Heuristic> heuristic =
		new Option<>(DEFAULT_HEURISTIC,
			     "h",
			     "specify the heuristic %s",
			     true,
			     "method");
	heuristic.add("gain", new GainHeuristic());
	heuristic.add("gain-ratio", CONFIG.get(DEFAULT_HEURISTIC));
	options.add(heuristic);

	// dynamic discretisation procedure
	Option<IntervalBuilder> builder =
		new Option<>(DEFAULT_BUILDER,
			     "d",
			     "specify the discretisation",
			     true,
			     "method");
	builder.add("c45", CONFIG.get(DEFAULT_BUILDER));
	builder.add("boundary", new BoundarySplit());
	builder.add("mdl", new MDLSplit(new BoundarySplit()));
	options.add(builder);

	return options;
    }

    @Override
    protected ClassificationModel train(Dataset dataset) {
	Graph graph = new Graph(dataset);

	Instance[] covered = Instance.newArray(dataset.size());
	Instance.markAll(covered, RULE_COVERED);

	boolean[] used = new boolean[graph.size()];
	Arrays.fill(used, false);

	TreeBuilder builder = new TreeBuilder();
	Tree tree = builder.build(graph, null, dataset, covered);

	CONFIG.get(DEFAULT_PRUNER).prune(dataset, tree);

	tree.getRoot().sort();
	tree.fixThresholds(dataset);

	return new ClassificationModel(tree);
    }

    @Override
    protected String description() {
	return "Another Java implementation of C4.5 r8";
    }

    /**
     * <code>AJ48</code> entry point.
     * 
     * @param args
     *            command-line arguments.
     * 
     * @throws Exception
     *             If an error occurs &mdash; e.g., I/O error.
     */
    public static void main(String[] args) throws Exception {
	AJ48 algorithm = new AJ48();
	algorithm.run(args);
    }
}