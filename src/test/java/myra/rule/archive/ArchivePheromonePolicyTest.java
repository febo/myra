/*
 * ArchivePheromonePolicyTest.java
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
import static myra.datamining.IntervalBuilder.MINIMUM_CASES;
import static myra.rule.Assignator.ASSIGNATOR;
import static myra.rule.Heuristic.DEFAULT_HEURISTIC;
import static myra.rule.ListPruner.DEFAULT_LIST_PRUNER;
import static myra.rule.Pruner.DEFAULT_PRUNER;
import static myra.rule.RuleFunction.DEFAULT_FUNCTION;
import static myra.rule.pittsburgh.FindRuleListActivity.UNCOVERED;
import static myra.rule.pittsburgh.LevelPheromonePolicy.EVAPORATION_FACTOR;
import static myra.rule.pittsburgh.LevelPheromonePolicy.P_BEST;

import java.io.InputStreamReader;
import java.util.Random;

import junit.framework.TestCase;
import myra.Archive;
import myra.classification.rule.ClassificationRule;
import myra.classification.rule.ListAccuracy;
import myra.classification.rule.MajorityAssignator;
import myra.classification.rule.SinglePassPruner;
import myra.classification.rule.function.Accuracy;
import myra.datamining.ARFFReader;
import myra.datamining.Dataset;
import myra.datamining.VariableArchive;
import myra.rule.Heuristic;
import myra.rule.ListMeasure;
import myra.rule.ListPruner;
import myra.rule.Rule;
import myra.rule.RuleList;
import myra.rule.pittsburgh.FindRuleListActivity;

/**
 * @author Fernando Esteban Barril Otero
 */
public class ArchivePheromonePolicyTest extends TestCase {
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
	CONFIG.set(Archive.Q, Archive.DEFAULT_Q);
	CONFIG.set(VariableArchive.PRECISION, 2.0);
	CONFIG.set(UNCOVERED, 0.01);
	CONFIG.set(RANDOM_GENERATOR, new Random(System.currentTimeMillis()));
	CONFIG.set(Rule.DEFAULT_RULE, ClassificationRule.class);
	CONFIG.set(ASSIGNATOR, new MajorityAssignator());
	CONFIG.set(DEFAULT_HEURISTIC, new Heuristic.None());
	CONFIG.set(DEFAULT_PRUNER, new SinglePassPruner());
	CONFIG.set(DEFAULT_FUNCTION, new Accuracy());
	CONFIG.set(DEFAULT_LIST_PRUNER, new ListPruner.None());
	CONFIG.set(ListMeasure.DEFAULT_MEASURE, new ListAccuracy());
	CONFIG.set(MINIMUM_CASES, 2);
	CONFIG.set(EVAPORATION_FACTOR, 0.9);
	CONFIG.set(P_BEST, 0.05);

	ARFFReader reader = new ARFFReader();
	dataset = reader.read(new InputStreamReader(getClass()
		.getResourceAsStream("/weather.arff")));

	graph = new Graph(dataset);
    }

    public void testUpdate() {
	FindRuleListActivity activity =
		new FindRuleListActivity(graph,
					 dataset,
					 new ArchiveRuleFactory(),
					 new ArchivePheromonePolicy());

	activity.initialise();
	RuleList list = activity.create();

	ArchivePheromonePolicy policy = new ArchivePheromonePolicy();
	policy.update(graph, list);
    }
}