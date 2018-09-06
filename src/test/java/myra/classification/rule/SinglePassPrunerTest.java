/*
 * SinglePassPruner.java
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

package myra.classification.rule;

import static myra.Config.CONFIG;
import static myra.datamining.Algorithm.RANDOM_GENERATOR;
import static myra.datamining.Dataset.NOT_COVERED;
import static myra.datamining.IntervalBuilder.MINIMUM_CASES;
import static myra.rule.Assignator.ASSIGNATOR;

import java.io.InputStreamReader;
import java.util.Random;

import junit.framework.TestCase;
import myra.classification.rule.function.Accuracy;
import myra.datamining.ARFFReader;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.datamining.VariableArchive;
import myra.rule.Graph.Entry;
import myra.rule.Heuristic;
import myra.rule.Rule;
import myra.rule.archive.ArchiveRuleFactory;
import myra.rule.archive.Graph;

/**
 * @author Fernando Esteban Barril Otero
 */
public class SinglePassPrunerTest extends TestCase {
    /**
     * The dataset for the test.
     */
    private Dataset dataset;

    /**
     * The graph for the test.
     */
    private Graph graph;

    @Override
    protected void setUp() throws Exception {
	CONFIG.set(VariableArchive.ARCHIVE_SIZE, 5);
	CONFIG.set(VariableArchive.PRECISION, 2.0);
	CONFIG.set(RANDOM_GENERATOR, new Random(System.currentTimeMillis()));
	CONFIG.set(Rule.DEFAULT_RULE, ClassificationRule.class);
	CONFIG.set(ASSIGNATOR, new MajorityAssignator());
	CONFIG.set(MINIMUM_CASES, 2);

	ARFFReader reader = new ARFFReader();
	dataset = reader.read(new InputStreamReader(getClass()
		.getResourceAsStream("/weather.arff")));

	graph = new Graph(dataset);
	Entry[][] matrix = graph.matrix();

	for (int i = 0; i < graph.size(); i++) {
	    for (int j = 0; j < graph.size(); j++) {
		if (matrix[i][j] != null) {
		    matrix[i][j] = new Entry(1.0, 1.0);
		}
	    }
	}
    }

    public void testPrune() {
	Instance[] instances = Instance.newArray(dataset.size());
	Instance.markAll(instances, NOT_COVERED);

	Entry[] heuristic =
		new Heuristic.None().compute(graph, dataset, instances);

	Rule rule = new ArchiveRuleFactory()
		.create(0, graph, heuristic, dataset, instances);

	SinglePassPruner pruner = new SinglePassPruner();
	pruner.prune(dataset, rule, instances, new Accuracy());

	if (rule.isEmpty()) {
	    assertNull(rule.getConsequent());
	} else {
	    assertNotNull(rule.getConsequent());
	}
    }
}