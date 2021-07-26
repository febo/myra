/*
 * SinglePathAssignator.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2020 Fernando Esteban Barril Otero
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

package myra.classification.rule.hierarchical;

import java.util.TreeSet;

import myra.classification.Label;
import myra.datamining.Attribute;
import myra.datamining.Dataset;
import myra.datamining.Hierarchy;
import myra.datamining.Dataset.Instance;
import myra.datamining.Hierarchy.Node;
import myra.rule.Rule;

/**
 * 
 * 
 * @author Fernando Esteban Barril Otero
 * 
 * @since 5.0
 */
public class SinglePathAssignator extends ProbabilisticAssignator {
    @Override
    public int assign(Dataset dataset, Rule rule, Instance[] instances) {
        // determines the probabilities of each label
        int available = super.assign(dataset, rule, instances);
        Label label = (Label) rule.getConsequent();

        Attribute target = dataset.attributes()[dataset.classIndex()];
        Hierarchy hierarchy = dataset.getHierarchy();

        String[] values = target.values();
        double[] probabilities = label.probabilities();

        // identifies the leaf node at the lowest depth
        Node node = null;
        double p = 0;

        for (int i = 0; i < probabilities.length; i++) {
            if (probabilities[i] > 0) {
                Node n = hierarchy.get(values[i]);
                if (node == null || node.getDepth() < n.getDepth()
                        || (node.getDepth() == n.getDepth()
                                && probabilities[i] > p)) {
                    node = n;
                    p = probabilities[i];
                }
            }
        }

        TreeSet<String> labels = new TreeSet<>();
        labels.add(node.getLabel());

        for (Node ancestor : node.getAncestors()) {
            labels.add(ancestor.getLabel());
        }

        boolean[] active = new boolean[target.length()];

        for (int i = 0; i < values.length; i++) {
            active[i] = labels.contains(values[i]);
            if (!active[i]) {
                probabilities[i] = 0;
            }
        }

        rule.setConsequent(Label
                .toLabel(dataset.getTarget(), active, probabilities));

        return available;
    }
}