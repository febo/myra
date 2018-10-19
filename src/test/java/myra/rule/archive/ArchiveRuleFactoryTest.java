/*
 * ArchiveRuleFactoryTest.java
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

package myra.rule.archive;

import static myra.Config.CONFIG;
import static myra.datamining.Algorithm.RANDOM_GENERATOR;
import static myra.datamining.Dataset.NOT_COVERED;

import java.io.InputStreamReader;
import java.util.Random;

import junit.framework.TestCase;
import myra.Archive;
import myra.classification.rule.ClassificationRule;
import myra.datamining.ARFFReader;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.datamining.VariableArchive;
import myra.rule.Graph.Entry;
import myra.rule.Heuristic;
import myra.rule.Rule;

/**
 * @author Fernando Esteban Barril Otero
 */
public class ArchiveRuleFactoryTest extends TestCase {
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
	CONFIG.set(Archive.ARCHIVE_SIZE, 5);
	CONFIG.set(VariableArchive.PRECISION, 2.0);
	CONFIG.set(RANDOM_GENERATOR, new Random(System.currentTimeMillis()));
	CONFIG.set(Rule.DEFAULT_RULE, ClassificationRule.class);

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

    public void testCreate() {
	Instance[] instances = Instance.newArray(dataset.size());
	Instance.markAll(instances, NOT_COVERED);

	Entry[] heuristic =
		new Heuristic.None().compute(graph, dataset, instances);

	Rule rule = new ArchiveRuleFactory()
		.create(0, graph, heuristic, dataset, instances);
	assertTrue(!rule.isEmpty());
    }
}