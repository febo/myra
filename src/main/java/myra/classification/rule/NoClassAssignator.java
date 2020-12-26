/*
 * NoClassAssignator.java
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

package myra.classification.rule;

import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.rule.Assignator;
import myra.rule.Rule;

/**
 * A "pseudo" class assignator that does not change the class value in the
 * consequent of the rule.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class NoClassAssignator implements Assignator {
    /**
     * Returns the number of uncovered instances of the class value predicted by
     * the rule.
     * 
     * @return the number of uncovered instances of the class value predicted by
     *         the rule.
     */
    @Override
    public int assign(Dataset dataset, Rule rule, Instance[] instances) {
        if (!(rule instanceof ClassificationRule)) {
            throw new IllegalArgumentException("Expecting a classification rule: "
                    + rule.getClass().getName());
        }

        ClassificationRule cRule = (ClassificationRule) rule;

        return cRule.uncovered()[cRule.getConsequent().value()];
    }
}