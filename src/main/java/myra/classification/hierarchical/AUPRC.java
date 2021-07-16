/*
 * AUPRC.java
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

package myra.classification.hierarchical;

import static myra.Config.CONFIG;
import static myra.datamining.Hierarchy.IGNORE;

import java.util.ArrayList;
import java.util.Arrays;

import myra.Cost;
import myra.Cost.Maximise;
import myra.classification.ClassificationModel;
import myra.datamining.Dataset;
import myra.rule.ListMeasure;
import myra.rule.RuleList;

/**
 * This class represents a precision-recall evaluation measure, consisting on
 * calculating the area under the average precision-recall curve.
 * 
 * @author Fernando Esteban Barril Otero
 * 
 * @since 5.0
 */
public class AUPRC extends HierarchicalMeasure implements ListMeasure {
    @Override
    public Cost evaluate(Dataset dataset, ClassificationModel model) {
        boolean[] ignore = CONFIG.get(IGNORE);
        final int size = dataset.size();
        final int labelSize = dataset.getHierarchy().size() - cardinality(ignore);

        // array with each individual class label (prediction, actual) pair
        double[][] values = new double[size * labelSize][2];

        for (int i = 0; i < size; i++) {
            boolean[] actual = dataset.label(i).active();
            double[] predicted = model.predict(dataset, i).probabilities();
            int index = 0;

            for (int j = 0; j < actual.length; j++) {
                if (!ignore[j]) {
                    int offset = (i * labelSize) + index;
                    values[offset][0] = predicted[j];
                    values[offset][1] = (actual[j] ? 1 : 0);
                    index++;
                }
            }
        }

        return new Maximise(area(values));
    }

    @Override
    public Cost evaluate(Dataset dataset, RuleList list) {
        return evaluate(dataset, new ClassificationModel(list));
    }

    /**
     * Return the AUPRC given a list of PR pairs.
     * 
     * @param pairs
     *            the list of PR points.
     * 
     * @return the AUPRC of the curve represented by the PR points.
     */
    protected double area(double[][] pairs) {
        Arrays.sort(pairs, (double[] v1, double[] v2) -> {
            int c = Double.compare(v1[0], v2[0]);

            if (c == 0) {
                return Double.compare(v2[1], v1[1]);
            }

            return c;
        });

        // creates the list of PR points of the curve
        Curve curve = new Curve(pairs.length + 1);
        long positive = 0;
        long negative = 0;

        double probability = pairs[pairs.length - 1][0];

        if (pairs[pairs.length - 1][1] == 1) {
            positive++;
        } else {
            negative++;
        }

        for (int i = pairs.length - 2; i >= 0; i--) {
            double p = pairs[i][0];

            if (p != probability) {
                // negative observations are considered false positive at this
                // points, since the probability is considered the recall
                // threshold
                curve.add(positive, negative);
            }

            probability = p;

            if (pairs[i][1] == 1) {
                positive++;
            } else {
                negative++;
            }
        }

        curve.addLast(positive, negative);
        curve.interpolate();

        return curve.area();
    }

    /**
     * Returns the number of <code>true</code> values in the given array.
     * 
     * @param array a boolean array.
     * 
     * @return the number of <code>true</code> values in the given array.
     */
    protected int cardinality(boolean[] array) {
        int count = 0;

        for (boolean value : array) {
            if (value) {
                count++;
            }
        }

        return count;
    }

    /**
     * Represents a point in the PR space.
     */
    public static class Point implements Comparable<Point> {
        /**
         * The positive (TP) count.
         */
        public double tp;

        /**
         * The negative (FP) count.
         */
        public double fp;

        /**
         * Default constructor.
         * 
         * @param tp
         *            the positive (TP) count.
         * @param fp
         *            the negative (FP) count.
         */
        Point(double tp, double fp) {
            this.tp = tp;
            this.fp = fp;
        }

        @Override
        public int compareTo(Point o) {
            int c = Double.compare(tp, o.tp);

            if (c == 0) {
                c = Double.compare(fp, o.fp);
            }

            return c;
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof Point) && (compareTo((Point) o) == 0);
        }

        @Override
        public String toString() {
            return "(" + tp + ", " + fp + ")";
        }
    }

    /**
     * Represents a curve in the PR space.
     */
    public static class Curve {
        /**
         * The points of the curve.
         */
        private ArrayList<Point> points;

        /**
         * The total number of positive observations.
         */
        private double positive;

        /**
         * The total number of negative observations. Not used in the current
         * implementation of the Precision-Recall measure, but useful to
         * implement a ROC measure.
         */
        @SuppressWarnings("unused")
        private double negative;

        /**
         * Default constructor.
         * 
         * @param size
         *            the (initial) number of points of in the curve.
         */
        public Curve(int size) {
            points = new ArrayList<>(size);
        }

        /**
         * Adds a point to the curve.
         * 
         * @param point
         *            the point to add.
         */
        public void add(Point point) {
            points.add(point);
        }

        /**
         * Adds a point to the curve.
         * 
         * @param positive
         *            the positive count.
         * @param negative
         *            the negative count.
         */
        public void add(double positive, double negative) {
            add(new Point(positive, negative));
        }

        /**
         * Adds the last point to the curve. The <code>positive</code> and
         * <code>negative</code> values specified are considered the total
         * (maximum) values. Only use this method to add the last point of the
         * curve.
         * 
         * @param positive
         *            the positive (TP) count.
         * @param negative
         *            the negative (FP) count.
         */
        public void addLast(double positive, double negative) {
            this.positive = positive;
            this.negative = negative;

            add(new Point(positive, negative));
        }

        /**
         * Interpolate between points.
         */
        public void interpolate() {
            for (int i = 0; i < points.size() - 1; i++) {
                Point p1 = points.get(i);
                Point p2 = points.get(i + 1);

                double deltaTP = p1.tp - p2.tp;
                double deltaFP = p1.fp - p2.fp;
                double skew = deltaFP / deltaTP;
                double tp = p1.tp;
                double fp = p1.fp;

                while (Math.abs(p1.tp - p2.tp) > 1) {
                    double interpolate = fp + (p1.tp - tp + 1) * skew;
                    Point p = new Point(p1.tp + 1, interpolate);
                    points.add(++i, p);
                    p1 = p;
                }
            }
        }

        /**
         * Calculates the are under the curve.
         * 
         * @return the are under the curve.
         */
        public double area() {
            if (points.isEmpty()) {
                throw new IllegalStateException("Insufficient points: "
                        + points.size());
            }

            Point p1 = points.get(0);
            double area = (p1.tp / positive) * (p1.tp / (p1.tp + p1.fp));

            for (int i = 1; i < points.size(); i++) {
                Point p2 = points.get(i);

                double base1 = p1.tp / (p1.tp + p1.fp);
                double base2 = p2.tp / (p2.tp + p2.fp);
                double height = (p2.tp / positive) - (p1.tp / positive);

                area += 0.5 * height * (base1 + base2);

                p1 = p2;
            }

            return area;
        }

        /**
         * Returns the number of points of the curve.
         * 
         * @return the number of points of the curve.
         */
        public int size() {
            return points.size();
        }
    }
}