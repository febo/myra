/*
 * IterativeActivity.java
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

import myra.Config.ConfigKey;

/**
 * Base class for iterative activities.
 * 
 * @author Fernando Esteban Barril Otero
 */
public abstract class IterativeActivity<T extends Weighable<T>>
	extends AbstractActivity<T> {
    /**
     * The config key for the maximum number of iterations.
     */
    public final static ConfigKey<Integer> MAX_ITERATIONS = new ConfigKey<>();

    /**
     * The config key for the stagnation test. If no improvement in the global
     * best is observed in <code>STAGNATION</code> iterations, the creation
     * process is stops. This class does not include this as a termination
     * criteria, but it is available to subclasses.
     */
    public final static ConfigKey<Integer> STAGNATION =
	    new ConfigKey<Integer>();

    /**
     * The iteration number;
     */
    protected int iteration;

    /**
     * The stagnation counter. It represents the number of iterations without an
     * improvement in the global best solution.
     */
    protected int stagnation;

    /**
     * The best-so-far candidate solution.
     */
    protected T globalBest;

    /**
     * Initialises the iteration number to <code>0</code>.
     */
    @Override
    public void initialise() {
	iteration = 0;
	stagnation = 0;
	globalBest = null;
    }

    /**
     * Checks whether the maximum number of iterations has been reached.
     */
    @Override
    public boolean terminate() {
	return iteration >= CONFIG.get(MAX_ITERATIONS);
    }

    @Override
    public void update(Archive<T> archive) {
	iteration++;

	T candidate = archive.highest();

	// updates the global best

	if (globalBest == null || candidate.compareTo(globalBest) > 0) {
	    globalBest = candidate;
	    stagnation = 0;
	} else if (candidate.compareTo(globalBest) == 0) {
	    stagnation++;
	}
    }

    /**
     * Returns the best solution found over all iterations.
     * 
     * @return the best solution found over all iterations.
     */
    public T getBest() {
	return globalBest;
    }
}