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
public abstract class IterativeActivity<T extends Comparable<T>>
	implements Activity<T> {
    /**
     * The config key for the maximum number of iterations.
     */
    public final static ConfigKey<Integer> MAX_ITERATIONS = new ConfigKey<>();

    /**
     * The iteration number;
     */
    protected int iteration;

    /**
     * Initialises the iteration number to <code>0</code>.
     */
    @Override
    public void initialise() {
	iteration = 0;
    }

    /**
     * Increaments the iteration number.
     */
    @Override
    public void daemon() {
	iteration++;
    }

    /**
     * Checks whether the maximum number of iterations has been reached.
     */
    @Override
    public boolean terminate() {
	return iteration >= CONFIG.get(MAX_ITERATIONS);
    }
}