/*
 * FairnessHeuristic.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2022 Fernando Esteban Barril Otero
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

package myra.classification.tree.fairness;

import static myra.Config.CONFIG;
import static myra.datamining.Attribute.Type.CONTINUOUS;
import static myra.datamining.Attribute.Type.NOMINAL;
import static myra.datamining.Dataset.NOT_COVERED;
import static myra.datamining.Dataset.RULE_COVERED;

import myra.Config.ConfigKey;
import myra.classification.attribute.C45Split;
import myra.classification.tree.Heuristic;
import myra.datamining.Attribute;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;

public class FairnessHeuristic implements Heuristic {
    /**
     * The config key to indicate the sensitive attribute.
     */
    public static final ConfigKey<String> SENSITIVE_ATTRIBUTE =
            new ConfigKey<>();

    /**
     * The config key to indicate the sensitive attribute value.
     */
    public static final ConfigKey<String> SENSITIVE_VALUE = new ConfigKey<>();

    /**
     * The config key to indicate the class positive (priviledge) value.
     */
    public static final ConfigKey<String> POSITIVE_VALUE = new ConfigKey<>();

    /**
     * List of fairness metrics to use.
     */
    private Metric[] metrics;

    public FairnessHeuristic() {
        metrics = new Metric[2];
        metrics[0] = new DiscriminationScore();
        metrics[1] = new ImpactRatio();
    }

    /**
     * Computes the gain ratio for each attribute. This involves calculating the
     * information gain and dividing it by the split information. Note that when
     * an attribute does not present any gain, its gain ratio value is set to
     * negative {@link C45Split#EPSILON}.
     * 
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the covered instances flags.
     * 
     * @return the information gain ratio of each attribute of the dataset.
     */
    public double[] compute(Dataset dataset,
                            Instance[] instances,
                            boolean[] used) {
        double[] values = new double[dataset.attributes().length - 1];

        for (int i = 0; i < values.length; i++) {
            if (!used[i]) {
                Attribute attribute = dataset.attributes()[i];

                // nominal attributes
                if (attribute.getType() == NOMINAL
                        && i != dataset.classIndex()) {
                    processNominal(dataset, instances, attribute);
                }
                // continuous attributes
                else if (attribute.getType() == CONTINUOUS) {
                    processContinuous(dataset, instances, attribute);
                }
            }
        }

        return values;
    }

    /**
     * Computes the information gain for a nominal attribute.
     * 
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the covered instances flags.
     * @param attribute
     *            the nominal attribute.
     */
    private double[] processNominal(Dataset dataset,
                                    Instance[] instances,
                                    Attribute attribute) {
        // determines the splits based on the attribute values

        Instance[][] split = new Instance[attribute.size()][];
        double missing = 0.0;

        for (int i = 0; i < split.length; i++) {
            split[i] = Instance.newArray(instances.length);
        }

        for (int i = 0; i < dataset.size(); i++) {
            if (instances[i].flag == RULE_COVERED) {
                double v = dataset.value(i, attribute.getIndex());
                double w = instances[i].weight;

                if (v == Dataset.MISSING_VALUE_INDEX) {
                    // missing values represent an extra split when
                    // calculating the gain ratio
                    missing += w;
                } else {
                    split[(int) v][i].flag = RULE_COVERED;
                    split[(int) v][i].weight = w;
                }
            }
        }

        // calculates the fairness distribution

        FairnessInfo info = split(dataset, instances);
        double length = info.length();

        FairnessInfo[] partition = new FairnessInfo[split.length];
        double[] pLength = new double[split.length];

        for (int i = 0; i < split.length; i++) {
            partition[i] = split(dataset, split[i]);
            pLength[i] = info.length();
        }

        // calculates the fairness gain for each metric
        double[] fairness = new double[metrics.length];

        for (int i = 0; i < metrics.length; i++) {
            double subset = 0;

            for (int j = 0; j < partition.length; j++) {
                subset += (pLength[j] / length)
                        * metrics[i].calculate(dataset, split[j], partition[j]);
            }

            fairness[i] =
                    subset - metrics[i].calculate(dataset, instances, info);
        }

        return fairness;
    }

    /**
     * Computes the information gain for a continuous attribute.
     * 
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the covered instances flags.
     * @param attribute
     *            the continuous attribute.
     * @param gain
     *            the array of information gain values.
     * @param ratio
     *            the array of information gain ratio values.
     */
    private void processContinuous(Dataset dataset,
                                   Instance[] instances,
                                   Attribute attribute,
                                   double[] gain,
                                   double[] ratio) {
        double[] distribution = new double[dataset.classLength()];
        // sum of the fractional weights of the all examples
        double length = 0;
        // sum of the fractional weights of the examples with known outcomes
        double size = 0;
        // sum of the fractional weights of the examples with unknown outcomes
        double missing = 0;

        for (int i = 0; i < dataset.size(); i++) {
            if (instances[i].flag == RULE_COVERED) {
                double v = dataset.value(i, attribute.getIndex());
                double w = instances[i].weight;

                if (!Double.isNaN(v)) {
                    int index = (int) dataset.value(i, dataset.classIndex());
                    distribution[index] += w;
                    size += w;
                } else {
                    missing += w;
                }

                length += w;
            }
        }

        // info(T) (the information of the current attribute)

        double info = 0.0;

        for (int i = 0; i < distribution.length; i++) {
            if (distribution[i] > 0) {
                double p = distribution[i] / size;
                info -= (p * log2(p));
            }
        }

        Condition[] conditions = IntervalBuilder.singleton()
                .multiple(dataset, instances, attribute.getIndex());

        if (conditions == null) {
            ratio[attribute.getIndex()] = -EPSILON;
        } else {
            // info_x(T) (the information given by the partitions)

            double infoX = 0.0;
            double split = 0.0;

            for (Condition c : conditions) {
                infoX += ((c.length / size) * c.entropy);

                double f = c.length / (size + missing);
                split -= (f * log2(f));
            }

            if (missing > 0) {
                double f = missing / (size + missing);
                split -= (f * log2(f));
            }

            // gain ratio

            // the penalty term (log2(available) / length) is based on C4.5
            // implementation

            int index = attribute.getIndex();

            gain[index] = ((size / length) * (info - infoX))
                    - (log2(conditions[0].tries) / length);
            double value = gain[index] / split;

            ratio[index] = Double.isNaN(value) ? -EPSILON : value;
        }
    }

    /**
     * Returns the group of instances beloging to the favoured and unfavoured
     * groups. Note that instances with missing values associated with the
     * sensitive attribute are part of both groups.
     * 
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the covered instances flags.
     * 
     * @return an array with the favoured group instances at index
     *         <code>0</code> and unfavoured group instances at index
     *         <code>1</code>.
     */
    public FairnessInfo split(Dataset dataset, Instance[] instances) {
        Attribute attribute =
                dataset.findAttribute(CONFIG.get(SENSITIVE_ATTRIBUTE));
        int sensitive = attribute.indexOf(CONFIG.get(SENSITIVE_VALUE));

        FairnessInfo info = new FairnessInfo(dataset.classLength());
        Instance[] favoured = Instance.copyOf(instances);
        Instance[] unfavoured = Instance.copyOf(instances);

        for (int i = 0; i < dataset.size(); i++) {
            if (instances[i].flag == RULE_COVERED) {
                double v = dataset.value(i, attribute.getIndex());
                double w = instances[i].weight;
                int index = (int) dataset.value(i, dataset.classIndex());

                if (Double.isNaN(v)) {
                    info.missing[index] += w;
                } else if (v == sensitive) {
                    // instance covered by the unfavoured (sensitive) group
                    info.distribution(FairnessInfo.UNFAVOURED)[index] += w;
                    favoured[i].flag = NOT_COVERED;
                } else {
                    // instance is not part of the sensitive group
                    info.distribution(FairnessInfo.FAVOURED)[index] += w;
                    unfavoured[i].flag = NOT_COVERED;
                }
            }
        }

        info.set(FairnessInfo.FAVOURED, favoured);
        info.set(FairnessInfo.UNFAVOURED, unfavoured);

        return info;
    }

    /**
     * Struct-like class to hold the information about an instance.
     */
    public static final class FairnessInfo {
        /**
         * Favoured group index.
         */
        public final static int FAVOURED = 0;

        /**
         * Unavoured group index.
         */
        public final static int UNFAVOURED = 1;

        private Instance[][] groups;

        /**
         * The frequency of class values of each group.
         */
        private double[][] distribution;

        /**
         * The frequency of class values of instances with missing values.
         */
        public double[] missing;

        /**
         * Creates a new <code>FairnessInfo</code> object.
         * 
         * @param length
         *            the number of class values.
         */
        public FairnessInfo(int length) {
            groups = new Instance[2][];
            distribution = new double[2][length];
            missing = new double[length];
        }

        /**
         * Sets the instances belonging to the specified group.
         * 
         * @param index
         *            the index represeting the group.
         * @param group
         *            the covered instances flags of the group.
         */
        public void set(int index, Instance[] group) {
            groups[index] = group;
        }

        /**
         * Returns the instances of the specified group.
         * 
         * @param group
         *            the index representing the group.
         * 
         * @return the instances of the specified group.
         */
        public Instance[] instances(int group) {
            return groups[group];
        }

        /**
         * Returns the class distribution of the specified group.
         * 
         * @param group
         *            the index representing the group.
         * 
         * @return the class distribution of the specified group.
         */
        public double[] distribution(int group) {
            return distribution[group];
        }

        /**
         * Returns the fractional weights of the examples with known outcomes.
         * 
         * @return the fractional weights of the examples with known outcomes.
         */
        public double length() {
            double length = 0.0;

            for (double[] values : distribution) {
                for (double v : values) {
                    length += v;
                }
            }

            return length;
        }

        /**
         * Returns the fractional weights of the examples with known outcomes.
         * 
         * @return the fractional weights of the examples with known outcomes.
         */
        public double length(int group) {
            double length = 0.0;

            for (double v : distribution[group]) {
                length += v;
            }

            return length;
        }

        /**
         * Returns fractional weights of the examples with unknown outcomes.
         * 
         * @return fractional weights of the examples with unknown outcomes.
         */
        public double missingLength() {
            double length = 0.0;

            for (double v : missing) {
                length += v;
            }

            return length;
        }
    }
}