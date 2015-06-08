/*
 * Heuristic.java
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

package myra.tree;

import java.util.Arrays;

import myra.Config.ConfigKey;
import myra.Dataset;
import myra.Dataset.Instance;

public interface Heuristic {
    /**
     *
     * Config key for the default <code>Heuristic</code> instance.
     */
    public static final ConfigKey<Heuristic> DEFAULT_HEURISTIC =
	    new ConfigKey<>();

    /**
     * Computes the heuristic value of each attribute.
     * 
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the covered instances flags.
     * @param used
     *            indicates if an attribute has been used or not; attributes
     *            already used are not considered in the heuristic calculation.
     * 
     * @return the heuristic value of each attribute of the dataset.
     */
    public double[] compute(Dataset dataset,
			    Instance[] instances,
			    boolean[] used);

    /**
     * No heuristic information implementation - e.g., heuristic value is set to
     * 1 for all unused attributes.
     * 
     * @author Fernando Esteban Barril Otero
     */
    public static class None implements Heuristic {
	@Override
	public double[] compute(Dataset dataset,
				Instance[] instances,
				boolean[] used) {
	    double[] heuristic = new double[dataset.attributes().length - 1];
	    Arrays.fill(heuristic, 1.0);

	    for (int i = 0; i < heuristic.length; i++) {
		if (used[i]) {
		    heuristic[i] = 0.0;
		}
	    }

	    return heuristic;
	}
    }
}