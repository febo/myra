/*
 * AntMiner.java
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

package myra.rule.irl;

import static myra.Config.CONFIG;
import static myra.IterativeActivity.MAX_ITERATIONS;
import static myra.Scheduler.COLONY_SIZE;
import static myra.Scheduler.PARALLEL;
import static myra.interval.IntervalBuilder.DEFAULT_BUILDER;
import static myra.interval.IntervalBuilder.MAXIMUM_LIMIT;
import static myra.interval.IntervalBuilder.MINIMUM_CASES;
import static myra.rule.Assignator.ASSIGNATOR;
import static myra.rule.Heuristic.DEFAULT_HEURISTIC;
import static myra.rule.Heuristic.DYNAMIC_HEURISTIC;
import static myra.rule.Pruner.DEFAULT_PRUNER;
import static myra.rule.RuleFunction.DEFAULT_FUNCTION;
import static myra.rule.irl.FindRuleActivity.CONVERGENCE;
import static myra.rule.irl.PheromonePolicy.DEFAULT_POLICY;
import static myra.rule.irl.RuleFactory.DEFAULT_FACTORY;
import static myra.rule.irl.SequentialCovering.UNCOVERED;

import java.util.ArrayList;
import java.util.Collection;

import myra.Classifier;
import myra.Dataset;
import myra.Model;
import myra.Option;
import myra.Option.BooleanOption;
import myra.Option.IntegerOption;
import myra.interval.C45Split;
import myra.rule.BacktrackPruner;
import myra.rule.EntropyHeuristic;
import myra.rule.GreedyPruner;
import myra.rule.Heuristic;
import myra.rule.MajorityAssignator;
import myra.rule.Pruner;
import myra.rule.RuleFunction;
import myra.rule.function.Accuracy;
import myra.rule.function.Laplace;
import myra.rule.function.SensitivitySpecificity;

/**
 * Default executable class file for the <code>Ant-Miner</code> algorithm. The
 * full description of the algorithm can be found in:
 *
 * <pre>
 * &#64;ARTICLE{Parpinelli02datamining,
 *    author  = {R.S. Parpinelli and H.S. Lopes and A.A. Freitas},
 *    title   = {Data Mining with an Ant Colony Optimization Algorithm},
 *    journal = {IEEE Transactions on Evolutionary Computation},
 *    year    = {2002},
 *    volume  = {6},
 *    issue   = {4},
 *    pages   = {321--332}
 * }
 * </pre>
 * 
 * <p>
 * While the original <code>Ant-Miner</code> algorithm does not support
 * continuous attributes, this implementation behaves as
 * <code><i>c</i>Ant-Miner</code> if continuous attributes are present. More
 * information about <code><i>c</i>Ant-Miner</code> can be found at:
 * </p>
 * 
 * <pre>
 * &#64;INPROCEEDINGS{Otero08datamining,
 *    author    = {F.E.B. Otero and A.A. Freitas and C.G. Johnson},
 *    title     = {\emph{c}{A}nt-{M}iner: an ant colony classification algorithm to cope with continuous attributes},
 *    booktitle = {Proceedings of the 6th International Conference on Swarm Intelligence (ANTS 2008), Lecture Notes in Computer Science 5217},
 *    editor    = {M. Dorigo and M. Birattari and C. Blum and M. Clerc and T. St{\" u}tzle and A.F.T. Winfield},
 *    publisher = {Springer-Verlag},
 *    pages     = {48--59},
 *    year      = {2008}
 * }
 * </pre>
 * 
 * @author Fernando Esteban Barril Otero
 */
public class AntMiner extends Classifier {
    @Override
    protected void defaults() {
	super.defaults();

	// configuration not set via command line

	CONFIG.set(ASSIGNATOR, new MajorityAssignator());
	CONFIG.set(DEFAULT_BUILDER, new C45Split());
	CONFIG.set(DEFAULT_FACTORY, new VertexRuleFactory());
	CONFIG.set(DEFAULT_POLICY, new VertexPheromonePolicy());

	// default configuration values

	CONFIG.set(COLONY_SIZE, 60);
	CONFIG.set(MAX_ITERATIONS, 1500);
	CONFIG.set(MINIMUM_CASES, 10);
	CONFIG.set(MAXIMUM_LIMIT, 25);
	CONFIG.set(UNCOVERED, 10);
	CONFIG.set(CONVERGENCE, 10);
	CONFIG.set(DEFAULT_PRUNER, new GreedyPruner());
	CONFIG.set(DEFAULT_FUNCTION, new SensitivitySpecificity());
	CONFIG.set(DEFAULT_HEURISTIC, new EntropyHeuristic());
	CONFIG.set(DYNAMIC_HEURISTIC, Boolean.FALSE);
    }

    @Override
    public Model train(Dataset dataset) {
	SequentialCovering seco = new SequentialCovering();
	return seco.train(dataset);
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
	options.add(new IntegerOption(CONVERGENCE,
				      "x",
				      "set the number of %s for convergence test",
				      "iterations"));

	// rule pruner
	Option<Pruner> pruner = new Option<Pruner>(DEFAULT_PRUNER,
						   "p",
						   "specify the rule pruner %s",
						   true,
						   "method");
	pruner.add("greedy", CONFIG.get(DEFAULT_PRUNER));
	pruner.add("backtrack", new BacktrackPruner());
	options.add(pruner);

	// rule quality function
	Option<RuleFunction> function =
		new Option<RuleFunction>(DEFAULT_FUNCTION,
					 "r",
					 "specify the rule quality %s",
					 true,
					 "function");
	function.add("accuracy", new Accuracy());
	function.add("laplace", new Laplace());
	function.add("sen_spe", CONFIG.get(DEFAULT_FUNCTION));
	options.add(function);

	// heuristic information
	Option<Heuristic> heuristic = new Option<Heuristic>(DEFAULT_HEURISTIC,
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

	return options;
    }

    @Override
    public String description() {
	return "Ant-Miner rule induction";
    }

    /**
     * <code>Ant-Miner</code> entry point.
     * 
     * @param args
     *            command-line arguments.
     * 
     * @throws Exception
     *             If an error occurs &mdash; e.g., I/O error.
     */
    public static void main(String[] args) throws Exception {
	AntMiner algorithm = new AntMiner();
	algorithm.run(args);
    }
}