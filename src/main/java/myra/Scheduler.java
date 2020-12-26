/*
 * Scheduler.java
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

import myra.Archive.DefaultArchive;
import myra.Config.ConfigKey;

/**
 * This class is responsible to execute an <code>Activity</code>. It provides
 * the basic structure for an ACO algorithm:
 * 
 * <pre>
 * initialise();
 * 
 * while (!terminate()) {
 *     // create ant solutions
 *     create();
 * 
 *     // (optional) local search
 *     search();
 * 
 *     // update pheromones
 *     update();
 * }
 * </pre>
 * 
 * <p>
 * The {@link #create()} iterate over the colony, using the
 * {@link Activity#create()} to create a solution for each ant in the colony.
 * All solutions are created sequentially.
 * </p>
 * 
 * @param <T>
 *            type of the solution created by the <code>Activity</code>.
 * 
 * @author Fernando Esteban Barril Otero
 * 
 * @see Activity
 */
public class Scheduler<T extends Weighable<T>> {
    /**
     * The config key for the size of the colony.
     */
    public final static ConfigKey<Integer> COLONY_SIZE = new ConfigKey<>();

    /**
     * The config key for parallel execution.
     */
    public final static ConfigKey<Integer> PARALLEL = new ConfigKey<>();

    /**
     * The wrapped activity.
     */
    protected Activity<T> activity;

    /**
     * Archive of solutions used during the update.
     */
    protected Archive<T> archive;

    /**
     * Creates a new <code>Scheduler</code>.
     */
    public Scheduler() {
        this(null, CONFIG.get(COLONY_SIZE));
    }

    /**
     * Creates a new <code>Scheduler</code>.
     * 
     * @param capacity
     *            number of candidate solutions stored at each iteration.
     */
    public Scheduler(int capacity) {
        this(null, capacity);
    }

    /**
     * Creates a new <code>Scheduler</code>.
     * 
     * @param activity
     *            the (wrapped) activity.
     */
    public Scheduler(Activity<T> activity) {
        this(activity, CONFIG.get(COLONY_SIZE));
    }

    /**
     * Creates a new <code>Scheduler</code>.
     * 
     * @param activity
     *            the (wrapped) activity.
     * @param capacity
     *            number of candidate solutions stored at each iteration.
     */
    public Scheduler(Activity<T> activity, int capacity) {
        this.activity = activity;
        archive = new DefaultArchive<>(capacity);
    }

    /**
     * Returns the activity of the scheduler.
     * 
     * @return the activity of the scheduler.
     */
    public Activity<T> getActivity() {
        return activity;
    }

    /**
     * Sets the activity of the scheduler.
     * 
     * @param activity
     *            the activity to set.
     */
    public void setActivity(Activity<T> activity) {
        this.activity = activity;
    }

    /**
     * Sets the solution archive capacity. This will clear the current solutions
     * in the archive.
     * 
     * @param capacity
     *            the archive capacity.
     */
    public void setCapacity(int capacity) {
        archive = new DefaultArchive<>(capacity);
    }

    /**
     * Runs the scheduler.
     */
    public void run() {
        initialise();

        while (!terminate()) {
            create();

            search();

            update();
        }
    }

    /**
     * Performs the initialisation of the activity.
     */
    protected void initialise() {
        activity.initialise();
        archive.clear();
    }

    /**
     * Creates the candidate solutions, one per ant in the colony. The creation
     * of each individual solution is delegated to the activity. The number of
     * ants is controlled by the configuration {@link #COLONY_SIZE}.
     */
    protected void create() {
        for (int i = 0; i < CONFIG.get(COLONY_SIZE); i++) {
            archive.add(activity.create());
        }
    }

    /**
     * Checks whether the activity should stop or not.
     * 
     * @return <code>true</code> if the activity should stop; <code>false</code>
     *         otherwise.
     */
    protected boolean terminate() {
        return activity.terminate();
    }

    /**
     * Performs the update of the activity.
     */
    protected void update() {
        activity.update(archive);
        // clears the archive
        archive.clear();
    }

    /**
     * Performs the local search of the activity. If the quality of the
     * candidates solutions are updated, a new solution archive is created.
     */
    protected void search() {
        if (activity.search(archive)) {
            archive.sort();
        }
    }

    /**
     * Returns a new <code>Scheduler</code> instance. This method works as a
     * factory method, checking the {@link #PARALLEL} configuration.
     * 
     * @param <V>
     *            type of the solution created by the <code>Activity</code>.
     * 
     * @return a <code>ParallelScheduler</code> instance if the
     *         {@link #PARALLEL} configuration is set; otherwise a (sequential)
     *         <code>Scheduler</code> instance.
     */
    public static <V extends Weighable<V>> Scheduler<V> newInstance() {
        if (CONFIG.isPresent(PARALLEL)) {
            return new ParallelScheduler<V>();
        }

        return new Scheduler<V>();
    }

    /**
     * Returns a new <code>Scheduler</code> instance. This method works as a
     * factory method, checking the {@link #PARALLEL} configuration.
     * 
     * @param <V>
     *            type of the solution created by the <code>Activity</code>.
     * @param capacity
     *            the solution archive capacity.
     * 
     * @return a <code>ParallelScheduler</code> instance if the
     *         {@link #PARALLEL} configuration is set; otherwise a (sequential)
     *         <code>Scheduler</code> instance.
     */
    public static <V extends Weighable<V>> Scheduler<V> newInstance(int capacity) {
        Scheduler<V> scheduler = newInstance();
        scheduler.setCapacity(capacity);
        return scheduler;
    }
}