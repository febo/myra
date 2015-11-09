/*
 * cAntMiner.java
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
import static myra.interval.IntervalBuilder.DEFAULT_BUILDER;
import static myra.rule.Pruner.DEFAULT_PRUNER;
import static myra.rule.irl.PheromonePolicy.DEFAULT_POLICY;
import static myra.rule.irl.RuleFactory.DEFAULT_FACTORY;

import java.util.ArrayList;
import java.util.Collection;

import myra.Option;
import myra.interval.C45Split;
import myra.interval.IntervalBuilder;
import myra.interval.MDLSplit;
import myra.interval.MinimalSplit;
import myra.rule.BacktrackPruner;
import myra.rule.GreedyPruner;
import myra.rule.Pruner;

/**
 * Default executable class file for the <code><i>c</i>Ant-Miner</code>
 * algorithm. This implementation corresponds to the
 * <code><i>c</i>Ant-Miner2-MDL</code> described in the following paper:
 *
 * <pre>
 * &#64;INPROCEEDINGS{Otero09datamining,
 *    author    = {F.E.B. Otero and A.A. Freitas and C.G. Johnson},
 *    title     = {Handling continuous attributes in ant colony classification algorithms},
 *    booktitle = {Proceedings of the 2009 IEEE Symposium on Computational Intelligence in Data Mining (CIDM 2009)},
 *    publisher = {IEEE},
 *    pages     = {225--231},
 *    year      = {2009}
 * }
 * </pre>
 * 
 * The original <code><i>c</i>Ant-Miner</code> algorithm is described in:
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
public class cAntMiner extends AntMiner {
    @Override
    protected void defaults() {
	super.defaults();

	// configuration not set via command line

	CONFIG.set(DEFAULT_FACTORY, new EdgeRuleFactory());
	CONFIG.set(DEFAULT_POLICY, new EdgePheromonePolicy());

	// default configuration values

	CONFIG.set(DEFAULT_BUILDER, new MDLSplit(new MinimalSplit(), false));
	CONFIG.set(DEFAULT_PRUNER, new BacktrackPruner());
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
	return "cAnt-Miner rule induction";
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
	cAntMiner algorithm = new cAntMiner();
	algorithm.run(args);
    }
}