/*
 * RRMSECoverage.java
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

package myra.regression.rule.function;

import static myra.Config.CONFIG;
import static myra.datamining.Dataset.RULE_COVERED;
import static myra.datamining.Dataset.COVERED;

import myra.Config.ConfigKey;
import myra.Cost;
import myra.Cost.Maximise;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.regression.rule.RegressionRule;

/**
 * Rule quality function based on SeCoReg (Janssen and Furnkranz, 2010):
 * 
 * <pre>
 * &#64;INPROCEEDINGS{Janssen10,
 *    author    = {F. Janssen and J. Furnkranz},
 *    title     = {Seperate-and-conquer regression},
 *    booktitle = {Proceedings of the German Workshop on Lernen},
 *    year      = {2010},
 *    pages     = {81–-89}
 * }
 * </pre>
 * 
 * @since 4.5
 * 
 * @author Fernando Esteban Barril Otero
 */
public class RRMSECoverage extends RegressionRuleFunction {
    /**
     * The config key for the <i>m</i> parameter.
     */
    public static final ConfigKey<Double> ALPHA = new ConfigKey<Double>();

    static {
	// default alpha value
	// see F. Janssen and J. Furnkranz, "On the quest for optimal rule
	// learning heuristics", Machine Learning 78, pp. 343-379, 2010.
	CONFIG.set(ALPHA, 0.59);
    }

    @Override
    public Cost evaluate(Dataset dataset,
			 RegressionRule rule,
			 Instance[] instances) {
	double predicted = rule.getConsequent().value();
	double mean = dataset.mean();

	double lRMSE = 0;
	double lDefault = 0;
	double coverage = 0;

	double available = 0;
	
	for (int i = 0; i < dataset.size(); i++) {
	    if(instances[i].flag != COVERED)
	    {
		    available ++;
        	    if (instances[i].flag == RULE_COVERED) {
        		double actual = dataset.value(i, dataset.classIndex());
        		lDefault += Math.pow(actual - mean, 2);
        		lRMSE += Math.pow(actual - predicted, 2);
        		coverage ++;
        	    }
	    }
	}
	
	double RRMSE = Math.sqrt(lRMSE / coverage)
		/ Math.sqrt(lDefault / coverage);
	
	RRMSE  = RRMSE * RRMSE;

	RRMSE = CONFIG.get(ALPHA) * (1 - RRMSE);

	coverage = (1 - CONFIG.get(ALPHA)) * (coverage/available);

	return new Maximise(RRMSE + coverage);
    }
	
}