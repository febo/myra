/*
 * ArchivePheromonePolicy.java
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

import myra.rule.Rule;
import myra.rule.Rule.Term;
import myra.rule.RuleList;
import myra.rule.archive.Graph.Vertex;
import myra.rule.irl.PheromonePolicy;
import myra.rule.pittsburgh.LevelPheromonePolicy;

/**
 * This class is responsible for maintaining the pheromone values of the
 * construction graph and individual vertices archives.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class ArchivePheromonePolicy extends LevelPheromonePolicy
        implements PheromonePolicy {
    @Override
    public void update(myra.rule.Graph graph, RuleList list) {
        // updates the pheromone on selected vertices
        super.update(graph, list);

        Graph g = (Graph) graph;
        final int size = list.size() - (list.hasDefault() ? 1 : 0);
        double delta = list.getQuality().raw();
        int level = 0;

        for (int i = 0; i < size; i++) {
            Rule rule = list.rules()[i];

            for (int j = 0; j < rule.size(); j++) {
                Term term = rule.terms()[j];
                Vertex vertex = g.vertices()[term.index()];
                vertex.update(level, term.condition(), delta);
            }

            level++;
        }
    }

    @Override
    public void update(myra.rule.Graph graph, Rule rule) {
        RuleList list = new RuleList();
        list.add(rule);
        list.setQuality(rule.getQuality());

        update(graph, list);
    }
}