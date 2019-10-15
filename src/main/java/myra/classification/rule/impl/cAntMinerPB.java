/*
 * cAntMinerPB.java
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

package myra.classification.rule.impl;

import static myra.Config.CONFIG;
import static myra.IterativeActivity.MAX_ITERATIONS;
import static myra.IterativeActivity.STAGNATION;
import static myra.Scheduler.COLONY_SIZE;
import static myra.Scheduler.PARALLEL;
import static myra.classification.rule.unordered.ConflictResolution.QUALITY;
import static myra.datamining.IntervalBuilder.DEFAULT_BUILDER;
import static myra.rule.Assignator.ASSIGNATOR;
import static myra.rule.Heuristic.DEFAULT_HEURISTIC;
import static myra.rule.Heuristic.DYNAMIC_HEURISTIC;
import static myra.rule.ListMeasure.DEFAULT_MEASURE;
import static myra.rule.ListPruner.DEFAULT_LIST_PRUNER;
import static myra.rule.Pruner.DEFAULT_PRUNER;
import static myra.rule.RuleFunction.DEFAULT_FUNCTION;
import static myra.rule.RuleSet.CONFLICT_RESOLUTION;
import static myra.rule.pittsburgh.FindRuleListActivity.UNCOVERED;
import static myra.rule.pittsburgh.FindRuleSetActivity.UNORDERED;
import static myra.rule.pittsburgh.LevelPheromonePolicy.EVAPORATION_FACTOR;
import static myra.rule.pittsburgh.LevelPheromonePolicy.P_BEST;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import myra.IterativeActivity;
import myra.Option;
import myra.Option.BooleanOption;
import myra.Option.DoubleOption;
import myra.Option.IntegerOption;
import myra.Scheduler;
import myra.classification.ClassificationModel;
import myra.classification.attribute.BoundarySplit;
import myra.classification.attribute.C45Split;
import myra.classification.attribute.MDLSplit;
import myra.classification.rule.ClassificationRule;
import myra.classification.rule.EntropyHeuristic;
import myra.classification.rule.ListAccuracy;
import myra.classification.rule.MajorityAssignator;
import myra.classification.rule.PessimisticAccuracy;
import myra.classification.rule.RuleClassifier;
import myra.classification.rule.function.Laplace;
import myra.classification.rule.function.MEstimate;
import myra.classification.rule.function.SensitivitySpecificity;
import myra.datamining.Dataset;
import myra.datamining.IntervalBuilder;
import myra.rule.BacktrackPruner;
import myra.rule.Graph;
import myra.rule.GreedyPruner;
import myra.rule.Heuristic;
import myra.rule.ListMeasure;
import myra.rule.ListPruner;
import myra.rule.Pruner;
import myra.rule.Rule;
import myra.rule.RuleFunction;
import myra.rule.RuleList;
import myra.rule.TopDownListPruner;
import myra.rule.pittsburgh.FindRuleListActivity;
import myra.rule.pittsburgh.FindRuleSetActivity;

/**
 * This class represents the <code><i>c</i>Ant-Miner<sub>PB</sub></code>
 * implementation, as described in the paper:
 * 
 * <pre>
 * &#64;ARTICLE{Otero13covering,
 *    author  = {F.E.B. Otero and A.A. Freitas and C.G. Johnson},
 *    title   = {A New Sequential Covering Strategy for Inducing Classification Rules with Ant Colony Algorithms},
 *    journal = {IEEE Transactions on Evolutionary Computation},
 *    year    = {2013},
 *    volume  = {17},
 *    number  = {1},
 *    pages   = {64--74}
 * }
 * </pre>
 * 
 * This implementation uses an error-based list quality function by default, as
 * suggested in:
 * 
 * <pre>
 * &#64;INPROCEEDINGS{Medland12datamining,
 *    author    = {M. Medland and F.E.B. Otero and A.A. Freitas},
 *    title     = {Improving the $c$Ant-Miner$_{\mathrm{PB}}$ Classification Algorithm},
 *    booktitle = {Swarm Intelligence, Lecture Notes in Computer Science 7461},
 *    editor    = {M. Dorigo and M. Birattari and C. Blum and A.L. Christensen and A.P. Engelbrecht and R. Gro{\ss} and T. St{\"u}tzle},
 *    publisher = {Springer-Verlag},
 *    pages     = {73–-84},
 *    year      = {2012}
 * }
 * </pre>
 * 
 * @author Fernando Esteban Barril Otero
 */
public class cAntMinerPB extends RuleClassifier {
    @Override
    protected void defaults() {
	super.defaults();

	// configuration not set via command line

	CONFIG.set(ASSIGNATOR, new MajorityAssignator());
	CONFIG.set(P_BEST, 0.05);
	CONFIG.set(IntervalBuilder.MAXIMUM_LIMIT, 25);
	CONFIG.set(Rule.DEFAULT_RULE, ClassificationRule.class);

	// default configuration values

	CONFIG.set(COLONY_SIZE, 5);
	CONFIG.set(MAX_ITERATIONS, 500);
	CONFIG.set(IntervalBuilder.MINIMUM_CASES, 10);
	CONFIG.set(EVAPORATION_FACTOR, 0.9);
	CONFIG.set(UNCOVERED, 0.01);
	CONFIG.set(STAGNATION, 40);
	CONFIG.set(DEFAULT_MEASURE, new PessimisticAccuracy());
	CONFIG.set(DEFAULT_PRUNER, new BacktrackPruner());
	CONFIG.set(DEFAULT_LIST_PRUNER, new ListPruner.None());
	CONFIG.set(DEFAULT_FUNCTION, new SensitivitySpecificity());
	CONFIG.set(DEFAULT_HEURISTIC, new EntropyHeuristic());
	CONFIG.set(DYNAMIC_HEURISTIC, Boolean.FALSE);
	CONFIG.set(DEFAULT_BUILDER, new MDLSplit(new BoundarySplit()));
    }

    @Override
    protected ClassificationModel train(Dataset dataset) {
	IterativeActivity<RuleList> activity = CONFIG.isPresent(UNORDERED)
		? new FindRuleSetActivity(new Graph(dataset), dataset)
		: new FindRuleListActivity(new Graph(dataset), dataset);

	Scheduler<RuleList> scheduler = Scheduler.newInstance(1);
	scheduler.setActivity(activity);
	scheduler.run();

	RuleList list = activity.getBest();

	// if the list of rules was created in an unordered fashion, rules are ordered
	// by quality an added to a (ordered) list of rules
	if (CONFIG.isPresent(UNORDERED)) {
	    ArrayList<Rule> rules =
		    new ArrayList<Rule>(Arrays.asList(list.rules()));
	    // sort rules in descending
	    Collections.sort(rules, new Comparator<Rule>() {
		@Override
		public int compare(Rule o1, Rule o2) {
		    return o2.compareTo(o1);
		}
	    });
	
	    RuleList ordered = new RuleList();
	    ordered.setIteration(list.getIteration());
	    ordered.setQuality(list.getQuality());
	    
	    for (Rule rule : rules) {
		if (!rule.isEmpty()) {
		    ordered.add(rule);
		}
	    }
	    // make sure the default rule is the last rule on the list
	    ordered.add(list.defaultRule());
	    
	    list = ordered;
	}

	return new ClassificationModel(list);
    }

    @Override
    protected Map<String, String> processCommandLine(String[] args,
						     Collection<Option<?>> options) {
	Map<String, String> parameters =
		super.processCommandLine(args, options);
	// in case we are creating a list of rules in an unordered fashion,
	// the default values are different than the ordered case
	if (CONFIG.isPresent(UNORDERED)) {
	    for (Option<?> option : options) {
		if (option.getModifier().equals("l")
			&& !parameters.containsKey("l")) {
		    // sets the default list measure to accuracy
		    option.set("accuracy");
		} else if (option.getModifier().equals("r")
			&& !parameters.containsKey("r")) {
		    // sets the default rule function to MEstimate
		    option.set("mestimate");
		}
	    }
	    // always using the quality of rules to resolve conflicts
	    CONFIG.set(CONFLICT_RESOLUTION, QUALITY);
	}

	return parameters;
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

	// heuristic information
	Option<Heuristic> heuristic =
		new Option<Heuristic>(DEFAULT_HEURISTIC,
				      "h",
				      "specify the heuristic %s",
				      true,
				      "method");
	heuristic.add("gain", CONFIG.get(DEFAULT_HEURISTIC));
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
		new Option<IntervalBuilder>(DEFAULT_BUILDER,
					    "d",
					    "specify the discretisation",
					    true,
					    "method");
	builder.add("c45", new C45Split());
	builder.add("mdl", CONFIG.get(DEFAULT_BUILDER));
	options.add(builder);

	// unordered rule list discovery
	BooleanOption unordered =
		new BooleanOption(UNORDERED,
				  "-unordered",
				  "enables the unordered rule list creation");
	options.add(unordered);

	return options;
    }

    @Override
    public String description() {
	return "Pittsburgh-based cAnt-Miner";
    }

    /**
     * <code><i>c</i>Ant-Miner<sub>PB</sub></code> entry point.
     * 
     * @param args
     *            command-line arguments.
     * 
     * @throws Exception
     *             If an error occurs &mdash; e.g., I/O error.
     */
    public static void main(String[] args) throws Exception {
	cAntMinerPB algorithm = new cAntMinerPB();
	algorithm.run(args);
    }
}