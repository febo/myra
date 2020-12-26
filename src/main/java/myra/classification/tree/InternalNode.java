/*
 * InternalNode.java
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

import static myra.datamining.Attribute.EQUAL_TO;

import myra.datamining.Attribute;
import myra.datamining.Dataset;
import myra.datamining.IntervalBuilder;
import myra.datamining.Attribute.Condition;
import myra.datamining.Dataset.Instance;

/**
 * This class represent an internal node of the decision tree. Internal nodes
 * represent attribute tests, where the branches originating from the node
 * correspond to the different test outcomes.
 * 
 * @author Fernando Esteban Barril Otero
 */
public final class InternalNode extends Node {
    /**
     * Instances reaching the node.
     */
    private Instance[] coverage;

    /**
     * The index of the attribute represented by this node.
     */
    private int attribute;

    /**
     * The nodes that can be reached by the branches of the node.
     */
    public Node[] children;

    /**
     * The branch conditions.
     */
    public Condition[] conditions;

    /**
     * Creates a new internal node.
     * 
     * @param attribute
     *            the attribute represented by this node.
     * @param level
     *            the level of the node.
     * @param conditions
     *            the conditions for the branches originating from this node.
     */
    public InternalNode(Attribute attribute,
                        int level,
                        Condition[] conditions) {
        super(attribute.getName(), level);
        this.attribute = attribute.getIndex();
        this.conditions = conditions;
        this.children = new Node[conditions.length];
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    /**
     * Returns the information of the instances reaching this node.
     * 
     * @return the information of the instances reaching this node.
     */
    public Instance[] getCoverage() {
        return coverage;
    }

    /**
     * Sets the information of the instances reaching this node.
     * 
     * @param coverage
     *            the information to set.
     */
    public void setCoverage(Instance[] coverage) {
        this.coverage = coverage;
    }

    /**
     * Returns the index of the attribute represented by this node.
     * 
     * @return the index of the attribute represented by this node.
     */
    public int attribute() {
        return attribute;
    }

    /**
     * Returns the index of the most frequent branch.
     * 
     * @return the index of the most frequent branch.
     */
    public int frequentBranch() {
        int index = 0;

        for (int i = 1; i < children.length; i++) {
            if (children[i].getTotal() >= children[index].getTotal()) {
                index = i;
            }
        }

        return index;
    }

    @Override
    public void setLevel(int level) {
        super.setLevel(level);

        for (int i = 0; i < children.length; i++) {
            if (children[i] != null) {
                children[i].setLevel(level + 1);
            }
        }
    }

    @Override
    public void sort() {
        for (int i = 0; i < children.length; i++) {
            if (!children[i].isLeaf()) {
                int toIndex = -1;

                for (int j = i + 1; j < children.length; j++) {
                    if (children[j].isLeaf()) {
                        toIndex = j;
                        break;
                    }
                }

                if (toIndex == -1) {
                    children[i].sort();
                } else {
                    Node child = children[toIndex];
                    Condition condition = conditions[toIndex];

                    for (int j = toIndex; j > i; j--) {
                        children[j] = children[j - 1];
                        conditions[j] = conditions[j - 1];
                    }

                    children[i] = child;
                    conditions[i] = condition;
                }
            }
        }
    }

    /**
     * Returns the code of the specified branch. The code uses the information
     * of the node where the branch starts and also the level in the tree.
     * 
     * @param from
     *            the node where the branch starts.
     * @param condition
     *            the condition of the branch.
     * 
     * @return the code of the specified branch.
     */
    public static int encode(InternalNode from, Condition condition) {
        StringBuffer buffer = new StringBuffer();

        buffer.append(from.getName());
        buffer.append(condition.toString());
        buffer.append(from.getLevel());

        return buffer.toString().hashCode();
    }

    /**
     * Returns a branch array for the specified attribute, one branch for each
     * attribute value.
     * 
     * @param graph
     *            the construction graph.
     * @param dataset
     *            the current dataset.
     * @param attribute
     *            the branch's attribute test.
     * @param instances
     *            the covered instances flag.
     * 
     * @return a branch array for the specified attribute, one branch for each
     *         attribute value.
     */
    public static Condition[] branch(Graph graph,
                                     Dataset dataset,
                                     Attribute attribute,
                                     Instance[] instances) {
        if (attribute.getType() == Attribute.Type.NOMINAL) {
            return branchNominal(graph, attribute);
        } else {
            return branchContinuous(graph, dataset, attribute, instances);
        }
    }

    /**
     * Returns a branch array for the specified attribute, one branch for each
     * attribute value.
     * 
     * @param metadata
     *            the metadata object.
     * @param attribute
     *            the nominal attribute to get the values from.
     * 
     * @return a branch array for the specified attribute, one branch for each
     *         nominal value.
     */
    private static Condition[] branchNominal(Graph graph, Attribute attribute) {
        Condition[] conditions = new Condition[attribute.values().length];

        for (int i = 0; i < conditions.length; i++) {
            Condition condition = new Condition();
            condition.attribute = attribute.getIndex();
            condition.relation = EQUAL_TO;
            condition.value[0] = i;

            conditions[i] = condition;
        }

        return conditions;
    }

    /**
     * Returns a branch array for the specified continuous attribute, one branch
     * for each discrete interval.
     * 
     * @param graph
     *            the construction graph.
     * @param dataset
     *            the current dataset.
     * @param attribute
     *            the continuous attribute to get the values from.
     * @param instances
     *            the instances used for creating the discrete intervals.
     * 
     * @return a branch array for the specified continuous attribute, one branch
     *         for each discrete interval.
     */
    private static Condition[] branchContinuous(Graph graph,
                                                Dataset dataset,
                                                Attribute attribute,
                                                Instance[] instances) {
        return IntervalBuilder.singleton()
                .multiple(dataset, instances, attribute.getIndex());
    }
}