/*
 * Activity.java
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

/**
 * The <code>Activity</code> interface specifies the steps of a basic ACO
 * algorithm. An implementation of this interface will usually be wrapped in a
 * <code>Scheduler</code>, which will determine the order of the steps.
 * 
 * @author Fernando Esteban Barril Otero
 * 
 * @see Scheduler
 * 
 * @param <T>
 *            the type of solution created by the activity.
 */
public interface Activity<T extends Weighable<T>> {
    /**
     * Creates a single solution to the problem. This implementation must be
     * thread-safe in order to be executed by the {@link ParallelScheduler}.
     * 
     * @return the created solution.
     */
    public T create();

    /**
     * Applies local search to the candidate solutions. More generally, local
     * search is one example of what have been called daemon actions.
     * 
     * @param archive
     *            the solution archive.
     * 
     * @return <code>true</code> if the solutions in the archive have been
     *         modified; <code>false</code> otherwise.
     */
    public boolean search(Archive<T> archive);

    /**
     * Performs the initialisation step.
     */
    public void initialise();

    /**
     * Indicates if the search should stop.
     * 
     * @return <code>true</code> if the search should stop; <code>false</code>
     *         otherwise.
     */
    public boolean terminate();

    /**
     * Updates the state of the activity. In most cases, this involves updating
     * the pheromone values using the iteration-best solution.
     * 
     * @param archive
     *            the solution archive.
     */
    public void update(Archive<T> archive);
}