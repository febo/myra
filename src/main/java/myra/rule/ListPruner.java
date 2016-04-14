/*
 * ListPruner.java
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

package myra.rule;

import myra.Config.ConfigKey;
import myra.data.Dataset;

/**
 * The <code>ListPruner</code> should be implemented by classes that are
 * responsible for pruning list of rules.
 * 
 * @author Fernando Esteban Barril Otero
 */
public interface ListPruner {
    /**
     * The config key for the default pruning instance.
     */
    public final static ConfigKey<ListPruner> DEFAULT_LIST_PRUNER =
	    new ConfigKey<ListPruner>();

    /**
     * Removes irrelevant terms from the antecedent of rules and even entires
     * rules from the specified list of rules.
     * 
     * @param dataset
     *            the current dataset.
     * @param list
     *            the list of rules to be pruned.
     */
    public abstract void prune(Dataset dataset, RuleList list);

    public static class None implements ListPruner {
	/**
	 * This implementation leaves the list of rules unchanged.
	 */
	public void prune(Dataset dataset, RuleList list) {
	};
    }
}