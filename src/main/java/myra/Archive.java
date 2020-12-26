/*
 * Archive.java
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

package myra;

import static myra.Config.CONFIG;

import java.util.Arrays;

import myra.Config.ConfigKey;

/**
 * This interface represents a solution archive. The archive holds the solutions
 * ordered by quality; if adding a new solution exceeds its capacity, the new
 * solution is only added if it is better than the lowest solution in the
 * archive.
 * <p>
 * The order of solutions follows the natual ordering, where the worst solution
 * of the archive returned by {@link #lowest()} and the best is returned by
 * {@link #highest()}.
 * </p>
 * 
 * @author Fernando Esteban Barril Otero
 *
 * @param <E>
 *            the elements' type in the solution archive.
 */
public interface Archive<E extends Weighable<E>> {

    /**
     * The config key for the archive size.
     */
    public static final ConfigKey<Integer> ARCHIVE_SIZE = new ConfigKey<>();

    /**
     * The config key for the weight calculation parameter <i>q</i>.
     */
    public static final ConfigKey<Double> Q = new ConfigKey<>();

    /**
     * Default value for the weight calculation parameter <i>q</i>.
     */
    public static final double DEFAULT_Q = 0.05099;

    /**
     * Adds a new solution to the archive.
     * 
     * @param e
     *            the solution to add.
     * 
     * @return <code>true</code> if the solution is added to the archive;
     *         <code>false</code> otherwise.
     */
    public boolean add(E e);

    /**
     * Returns the capacity of this archive.
     * 
     * @return the capacity of this archive.
     */
    public int capacity();

    /**
     * Returns the number of solutions in the archive.
     * 
     * @return the number of solutions in the archive.
     */
    public int size();

    /**
     * Clears the solution archive.
     */
    public void clear();

    /**
     * Returns the lowest solution in the archive.
     * 
     * @return the lowest solution in the archive.
     */
    public E lowest();

    /**
     * Returns the highest solution in the archive.
     * 
     * @return the highest solution in the archive.
     */
    public E highest();

    /**
     * Sorts the solution archive. It is only needed in cases where the
     * solutions are modified after they have been added to the archive.
     */
    public void sort();

    /**
     * Returns the <code>n</code> higher solutions in the archive. Note that
     * <code>n</code> must be smaller than the size of the archive.
     * 
     * @param n
     *            the number of solutions to return.
     * 
     * @return the <code>n</code> higher solutions in the archive.
     */
    public E[] topN(int n);

    /**
     * Returns <code>true</code> if the archive reached its capacity.
     * 
     * @return <code>true</code> if the archive reached its capacity;
     *         <code>false</code> otherwise.
     */
    public boolean isFull();

    /**
     * Updates the archive weights.
     */
    public void update();

    /**
     * Default archive implementation. Solutions are stored in decreasing order.
     * 
     * @author Fernando Esteban Barril Otero
     */
    public class DefaultArchive<E extends Weighable<E>> implements Archive<E> {
        /**
         * The solutions in the archive.
         */
        private E[] solutions;

        /**
         * The number of solutions in the archive.
         */
        private int size;

        /**
         * Creates a new <code>Archive</code>.
         * 
         * @param capacity
         *            the size of the solution archive.
         */
        @SuppressWarnings("unchecked")
        public DefaultArchive(int capacity) {
            if (capacity == 0) {
                throw new IllegalArgumentException("Invalid capacity for archive: "
                        + capacity);
            }

            solutions = (E[]) new Weighable[capacity];
            size = 0;
        }

        @Override
        public boolean add(E e) {
            if (size == 0 || (size < solutions.length
                    && e.compareTo(solutions[size - 1]) <= 0)) {
                solutions[size] = e;
                size++;
            } else if (e.compareTo(solutions[size - 1]) > 0) {
                E previous = null;
                int position = -1;

                // finds the position to insert the new element (insertion sort)
                for (int i = 0; i < solutions.length; i++) {
                    if (solutions[i] == null || e.compareTo(solutions[i]) > 0) {
                        previous = solutions[i];
                        position = i + 1;
                        // adds the new solution
                        solutions[i] = e;
                        break;
                    }
                }
                // shift the remaining solutions
                for (int i = position; i < solutions.length
                        && previous != null; i++) {
                    E element = solutions[i];
                    solutions[i] = previous;
                    previous = element;
                }

                if (size < solutions.length) {
                    size++;
                }
            } else {
                // the solutions will not be added to the archive
                return false;
            }

            return true;
        }

        @Override
        public int capacity() {
            return solutions.length;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public void clear() {
            Arrays.fill(solutions, null);
            size = 0;
        }

        @Override
        public E lowest() {
            return solutions[size - 1];
        }

        @Override
        public E highest() {
            return solutions[0];
        }

        @Override
        public void sort() {
            Arrays.sort(solutions, (o1, o2) -> -o1.compareTo(o2));
        }

        @Override
        @SuppressWarnings("unchecked")
        public E[] topN(int n) {
            if (size < n) {
                throw new IllegalArgumentException("Invalid number of solutions: "
                        + "expected " + size + ", actual " + n);
            }

            E[] top = (E[]) new Comparable[n];
            System.arraycopy(solutions, 0, top, 0, n);
            return top;
        }

        @Override
        public boolean isFull() {
            return size() == capacity();
        }

        /**
         * Returns the sorted solutions in the archive.
         * 
         * @return the sorted solutions in the archive.
         */
        public E[] solutions() {
            return solutions;
        }

        /**
         * Updates the archive weights.
         */
        public void update() {
            double q = CONFIG.get(Q);
            double k = size();

            for (int i = 0; i < size(); i++) {
                E c = solutions[i];

                double exp = -Math.pow((i + 1) - (double) 1, 2)
                        / (2 * q * q * k * k);
                c.setWeight((1 / (q * k * Math.sqrt(2 * Math.PI)))
                        * Math.pow(Math.E, exp));
            }
        }

    }

    /**
     * Synchonize wrapper for a solution archive.
     * 
     * @author Fernando Esteban Barril Otero
     */
    public class SynchronizedArchive<E extends Weighable<E>>
            implements Archive<E> {
        /**
         * The backing archive.
         */
        private final Archive<E> archive;

        /**
         * Default constructor.
         * 
         * @param archive
         *            the wrapped archive instance.
         */
        public SynchronizedArchive(Archive<E> archive) {
            this.archive = archive;
        }

        @Override
        public synchronized boolean add(E e) {
            return this.archive.add(e);
        }

        @Override
        public synchronized int capacity() {
            return this.archive.capacity();
        }

        @Override
        public synchronized int size() {
            return this.archive.size();
        }

        @Override
        public synchronized void clear() {
            this.archive.clear();
        }

        @Override
        public synchronized E lowest() {
            return this.archive.lowest();
        }

        @Override
        public synchronized E highest() {
            return this.archive.highest();
        }

        @Override
        public synchronized void sort() {
            this.archive.sort();
        }

        @Override
        public synchronized E[] topN(int n) {
            return this.archive.topN(n);
        }

        @Override
        public synchronized boolean isFull() {
            return size() == capacity();
        }

        @Override
        public synchronized void update() {
            this.archive.update();
        }
    }
}