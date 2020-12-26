/*
 * ProbabilisticRule.java
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

import static myra.datamining.Dataset.COVERED;
import static myra.datamining.Dataset.NOT_COVERED;
import static myra.datamining.Dataset.RULE_COVERED;

import java.util.TreeSet;

import myra.classification.rule.ClassificationRule;
import myra.datamining.Attribute;
import myra.datamining.Attribute.Condition;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.datamining.Hierarchy;
import myra.datamining.Hierarchy.Node;

/**
 * @author Fernando Esteban Barril Otero
 */
public class ProbabilisticRule extends ClassificationRule {
    /**
     * Creates a new <code>ProbabilisticRule</code>.
     */
    public ProbabilisticRule() {
        this(0);
    }

    /**
     * Creates a new <code>ProbabilisticRule</code> with the specified capacity.
     * 
     * @param capacity
     *            the allocated size of the rule.
     */
    public ProbabilisticRule(int capacity) {
        super(capacity);
    }

    @Override
    public int apply(Dataset dataset, Instance[] instances) {
        int total = 0;

        for (int i = 0; i < dataset.size(); i++) {
            if (instances[i].flag != COVERED) {
                if (covers(dataset, i)) {
                    total++;
                    instances[i].flag = RULE_COVERED;
                } else {
                    instances[i].flag = NOT_COVERED;
                }
            }
        }

        return total;
    }

    /**
     * Returns the string representation of the rule.
     * 
     * @param dataset
     *            the current dataset.
     * 
     * @return the string representation of the rule.
     */
    public String toString(Dataset dataset) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("IF ");

        if (size == 0) {
            buffer.append("<empty>");
        } else {
            for (int i = 0; i < size; i++) {
                if (!terms[i].isEnabeld()) {
                    throw new IllegalStateException("A rule should not contain disabled terms.");
                }

                if (i > 0) {
                    buffer.append(" AND ");
                }

                Condition condition = terms[i].condition();
                buffer.append(condition.toString(dataset));
            }
        }

        buffer.append(" THEN ");

        if (getConsequent() == null) {
            buffer.append("<undefined>");
        } else {
            Attribute target = dataset.attributes()[dataset.classIndex()];
            Hierarchy hierarchy = dataset.getHierarchy();

            String[] values = target.values();
            boolean[] active = getConsequent().active();
            TreeSet<Node> nodes = new TreeSet<>();

            for (int i = 0; i < active.length; i++) {
                if (active[i]) {
                    nodes.add(hierarchy.get(values[i]));
                }
            }

            for (int i = 0; i < active.length; i++) {
                if (active[i]) {
                    nodes.removeAll(hierarchy.get(values[i]).getAncestors());
                }
            }

            StringBuffer label = new StringBuffer();

            for (Node node : nodes) {
                if (label.length() > 0) {
                    label.append(Hierarchy.SEPARATOR);
                }

                label.append(node.getLabel());
            }

            buffer.append(label);
        }

        return buffer.toString();
    }

    /**
     * Returns always <code>true</code>.
     */
    @Override
    public boolean isDiverse() {
        return true;
    }

    @Override
    public int[] covered() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void covered(int[] covered) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] uncovered() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void uncovered(int[] uncovered) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int available() {
        throw new UnsupportedOperationException();
    }
}