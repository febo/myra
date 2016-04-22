/*
 * FindTreeActivity.java
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
import static myra.datamining.Dataset.RULE_COVERED;

import java.util.Arrays;

import myra.Archive;
import myra.Config.ConfigKey;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.IterativeActivity;

/**
 * @author Fernando Esteban Barril Otero
 */
public class FindTreeActivity extends IterativeActivity<Tree> {
    /**
     * The config key to indicate if the default pruner to use.
     */
    public static final ConfigKey<TreeMeasure> DEFAULT_MEASURE =
	    new ConfigKey<>();

    /**
     * The current dataset.
     */
    private Dataset dataset;

    /**
     * The construction graph.
     */
    private Graph graph;

    /**
     * The ACO pheromone policy.
     */
    private PheromonePolicy policy;

    /**
     * The tree builder.
     */
    private StochasticBuilder builder;

    /**
     * The convergence termination criteria counter.
     */
    private boolean reset;

    /**
     * The (initial) heuristic of the dataset. This value is not modified after
     * the {@link #initialise()} method.
     */
    private double[] INITIAL_HEURISTIC;

    /**
     * Creates a new <code>FindTreeActivity</code>.
     * 
     * @param graph
     *            the construction graph.
     * @param dataset
     *            the current dataset.
     */
    public FindTreeActivity(Graph graph, Dataset dataset) {
	this.graph = graph;
	this.dataset = dataset;
    }

    @Override
    public void initialise() {
	super.initialise();

	reset = true;
	builder = new StochasticBuilder();

	policy = new PheromonePolicy();
	policy.initialise(graph);

	Instance[] covered = Instance.newArray(dataset.size());
	Instance.markAll(covered, RULE_COVERED);
	boolean[] used = new boolean[graph.size()];
	Arrays.fill(used, false);

	Heuristic heuristic = CONFIG.get(Heuristic.DEFAULT_HEURISTIC);
	INITIAL_HEURISTIC = heuristic.compute(dataset, covered, used);
    }

    @Override
    public Tree create() {
	Instance[] covered = Instance.newArray(dataset.size());
	Instance.markAll(covered, RULE_COVERED);

	double[] heuristic = INITIAL_HEURISTIC.clone();

	Tree tree = builder.build(graph, heuristic, dataset, covered);
	tree.setIteration(iteration);

	tree = CONFIG.get(DEFAULT_PRUNER).prune(dataset, tree);
	tree.setQuality(CONFIG.get(DEFAULT_MEASURE).evaluate(dataset, tree));

	return tree;
    }

    @Override
    public void update(Archive<Tree> archive) {
	super.update(archive);
	policy.update(graph, archive.highest());
    }

    @Override
    public boolean terminate() {
	if (stagnation > CONFIG.get(STAGNATION)) {
	    if (reset) {
		policy.initialise(graph);
		stagnation = 0;
		reset = false;
	    } else {
		return true;
	    }
	}

	return super.terminate();
    }
}