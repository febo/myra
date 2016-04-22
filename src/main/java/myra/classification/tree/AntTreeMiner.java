/*
 * AntTreeMiner.java
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
import static myra.IterativeActivity.MAX_ITERATIONS;
import static myra.IterativeActivity.STAGNATION;
import static myra.Scheduler.COLONY_SIZE;
import static myra.Scheduler.PARALLEL;
import static myra.classification.tree.AbstractPruner.DEFAULT_PRUNER;
import static myra.classification.tree.FindTreeActivity.DEFAULT_MEASURE;
import static myra.classification.tree.GainRatioHeuristic.FILTER_GAIN;
import static myra.classification.tree.Heuristic.DEFAULT_HEURISTIC;
import static myra.classification.tree.PheromonePolicy.EVAPORATION_FACTOR;
import static myra.classification.tree.PheromonePolicy.P_BEST;
import static myra.classification.tree.StochasticBuilder.DYNAMIC_HEURISTIC;
import static myra.classification.tree.TreeMeasure.PESSIMISTIC;
import static myra.datamining.IntervalBuilder.DEFAULT_BUILDER;
import static myra.datamining.IntervalBuilder.MAXIMUM_LIMIT;
import static myra.datamining.IntervalBuilder.MINIMUM_CASES;

import java.util.ArrayList;
import java.util.Collection;

import myra.Option;
import myra.Option.BooleanOption;
import myra.Option.DoubleOption;
import myra.Option.IntegerOption;
import myra.Scheduler;
import myra.classification.ClassificationModel;
import myra.classification.Classifier;
import myra.classification.attribute.BoundarySplit;
import myra.classification.attribute.C45Split;
import myra.classification.attribute.MDLSplit;
import myra.datamining.Dataset;
import myra.datamining.IntervalBuilder;

/**
 * Default executable class file for the <code>Ant-Tree-Miner</code> algorithm.
 * The full description of the algorithm can be found in:
 *
 * <pre>
 * &#64;ARTICLE{Otero12tree,
 *    author  = {F.E.B. Otero, A.A. Freitas and C.G. Johnson},
 *    title   = {Inducing Decision Trees with An Ant Colony Optimization Algorithm},
 *    journal = {Applied Soft Computing},
 *    year    = {2012},
 *    volume  = {12},
 *    number  = {11},
 *    pages   = {3615--3626}
 * }
 * </pre>
 * 
 * @author Fernando Esteban Barril Otero
 */
public class AntTreeMiner extends Classifier {
    @Override
    protected void defaults() {
	super.defaults();

	// configuration not set via command line

	CONFIG.set(P_BEST, 0.05);
	CONFIG.set(MAXIMUM_LIMIT, 25);
	CONFIG.set(FILTER_GAIN, false);

	// default configuration values

	CONFIG.set(COLONY_SIZE, 5);
	CONFIG.set(MAX_ITERATIONS, 500);
	CONFIG.set(MINIMUM_CASES, 3);
	CONFIG.set(EVAPORATION_FACTOR, 0.9);
	CONFIG.set(DEFAULT_MEASURE, PESSIMISTIC);
	CONFIG.set(STAGNATION, 40);
	CONFIG.set(DEFAULT_PRUNER, new PessimisticPruner());
	CONFIG.set(DEFAULT_HEURISTIC, new GainRatioHeuristic());
	CONFIG.set(DYNAMIC_HEURISTIC, Boolean.FALSE);
	CONFIG.set(DEFAULT_BUILDER, new BoundarySplit());
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
	Option<AbstractPruner> pruner =
		new Option<>(DEFAULT_PRUNER,
			     "p",
			     "specify the rule pruner %s",
			     true,
			     "method");
	pruner.add("accuracy", new AccuracyPruner());
	pruner.add("pessimistic", CONFIG.get(DEFAULT_PRUNER));
	options.add(pruner);

	// rule quality function
	Option<TreeMeasure> measure = new Option<>(DEFAULT_MEASURE,
						   "l",
						   "specify the tree quality %s",
						   true,
						   "measure");
	measure.add("accuracy", TreeMeasure.ACCURACY);
	measure.add("pessimistic", CONFIG.get(DEFAULT_MEASURE));
	options.add(measure);

	// heuristic information
	Option<Heuristic> heuristic =
		new Option<>(DEFAULT_HEURISTIC,
			     "h",
			     "specify the heuristic %s",
			     true,
			     "method");
	heuristic.add("gain", new GainHeuristic());
	heuristic.add("gain-ratio", CONFIG.get(DEFAULT_HEURISTIC));
	heuristic.add("none", new Heuristic.None());
	options.add(heuristic);

	// dynamic heuristic calculation
	BooleanOption dynamic = new BooleanOption(DYNAMIC_HEURISTIC,
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
	builder.add("c45", new C45Split());
	builder.add("boundary", CONFIG.get(DEFAULT_BUILDER));
	builder.add("mdl", new MDLSplit(new BoundarySplit()));
	options.add(builder);

	return options;
    }

    @Override
    protected ClassificationModel train(Dataset dataset) {
	FindTreeActivity activity =
		new FindTreeActivity(new Graph(dataset), dataset);

	Scheduler<Tree> scheduler = Scheduler.newInstance(1);
	scheduler.setActivity(activity);
	scheduler.run();

	Tree tree = activity.getBest();
	tree.getRoot().sort();
	tree.fixThresholds(dataset);

	return new ClassificationModel(tree);
    }

    @Override
    protected String description() {
	return "Ant-Tree-Miner decision tree generator";
    }

    /**
     * <code>Ant-Tree-Miner</code> entry point.
     * 
     * @param args
     *            command-line arguments.
     * 
     * @throws Exception
     *             If an error occurs &mdash; e.g., I/O error.
     */
    public static void main(String[] args) throws Exception {
	AntTreeMiner algorithm = new AntTreeMiner();
	algorithm.run(args);
    }
}