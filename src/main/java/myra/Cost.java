/*
 * Cost.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2016 Fernando Esteban Barril Otero
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

package myra;

/**
 * The <code>Cost</code> interface represents the cost of a solution.
 * 
 * @since 4.5
 * 
 * @author Fernando Esteban Barril Otero
 */
public interface Cost extends Comparable<Cost> {
    /**
     * Returns the numeric value of the cost.
     * 
     * @return the numeric value of the cost.
     */
    public abstract double raw();

    /**
     * Returns the adjusted numeric value of the cost. This is usually used for
     * pheromone update.
     * 
     * @return the adjusted numeric value of the cost.
     */
    public abstract double adjusted();

    /**
     * Class to represent a cost value that should be minimised, where the lower
     * the value the better the solution.
     */
    public static class Minimise implements Cost {
        /**
         * The cost value.
         */
        private double cost;

        /**
         * Creates a new <code>Minimise</code> object.
         */
        public Minimise() {
            this(Double.MAX_VALUE);
        }

        /**
         * Creates a new <code>Minimise</code> object.
         * 
         * @param cost
         *            the cost value.
         */
        public Minimise(double cost) {
            this.cost = cost;
        }

        @Override
        public int compareTo(Cost o) {
            return compareTo((Minimise) o);
        }

        /**
         * Compares this cost with the specified cost <code>o</code> for order.
         * Returns a negative integer, zero, or a positive integer as this cost
         * is less than, equal to, or greater than the specified cost.
         * 
         * @param o
         *            the cost to be compared.
         * 
         * @return a negative integer, zero, or a positive integer as this
         *         object is less than, equal to, or greater than the specified
         *         object.
         */
        public int compareTo(Minimise o) {
            return Double.compare(o.cost, cost);
        }

        @Override
        public double raw() {
            return cost;
        }

        /**
         * @return the value of <code>1/cost</code>.
         */
        @Override
        public double adjusted() {
            return 1 / cost;
        }
    }

    /**
     * Class to represent a cost value that should be maximised, where the
     * higher the value the better a solution.
     */
    public static class Maximise implements Cost {
        /**
         * The cost value.
         */
        private double cost;

        /**
         * Creates a new <code>Maximise</code> object.
         */
        public Maximise() {
            this(Double.MIN_VALUE);
        }

        /**
         * Creates a new <code>Maximise</code> object.
         * 
         * @param cost
         *            the cost value.
         */
        public Maximise(double cost) {
            this.cost = cost;
        }

        @Override
        public int compareTo(Cost o) {
            return compareTo((Maximise) o);
        }

        /**
         * Compares this cost with the specified cost <code>o</code> for order.
         * Returns a negative integer, zero, or a positive integer as this cost
         * is less than, equal to, or greater than the specified cost.
         * 
         * @param o
         *            the cost to be compared.
         * 
         * @return a negative integer, zero, or a positive integer as this
         *         object is less than, equal to, or greater than the specified
         *         object.
         */
        public int compareTo(Maximise o) {
            return Double.compare(cost, o.cost);
        }

        @Override
        public double adjusted() {
            return cost;
        }

        /**
         * @return the value of the cost.
         */
        @Override
        public double raw() {
            return cost;
        }
    }
}