/*
 * AttributeArchive.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2017 Fernando Esteban Barril Otero
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

package myra.datamining;

import static myra.Config.CONFIG;
import static myra.datamining.Algorithm.RANDOM_GENERATOR;
import static myra.datamining.Attribute.EQUAL_TO;
import static myra.datamining.Attribute.Condition;

import myra.Archive.DefaultArchive;
import myra.Config.ConfigKey;

/**
 * This class represents a local archive of attribute conditions. It is used to
 * sample attribute conditions using an <code>ACO<sub>MV</sub></code> strategy.
 * 
 * @author Fernando Esteban Barril Otero
 */
public abstract class AttributeArchive {
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
     * The attribute values' archive.
     */
    protected DefaultArchive<Condition> archive;

    /**
     * The attribute index.
     */
    protected int index;
    
    /**
     * The weight vector
     */
    protected double[] weights;

    /**
     * Default constructor.
     */
    public AttributeArchive(int index) {
	this.index = index;
	archive = new DefaultArchive<Condition>(CONFIG.get(ARCHIVE_SIZE));
	weights = new double[CONFIG.get(ARCHIVE_SIZE)];
	initaliseweights();
    }

    /**
     * Returns an attribute condition sampled using an
     * <code>ACO<sub>MV</sub></code> strategy. Note that
     * <code>ACO<sub>MV</sub></code> sampling only occurs when the archive is
     * complete; random sampling is used.
     * 
     * @return an attribute condition.
     */
    public abstract Condition sample();

    /**
     * Adds the specified condition to the archive.
     * 
     * @param condition
     *            the condition to add.
     */
    public void add(Condition condition) {
	archive.add(condition);
    }

    /**
     * initalise the archive weights.
     */
    public void initaliseweights() {
	double q = CONFIG.get(Q);
	double k = CONFIG.get(ARCHIVE_SIZE);

	for (int i = 0; i < k; i++) {
	    double exp = -Math.pow((i + 1) - 1, 2) / (2 * q * q * k * k);
	    weights[i] = (1 / (q * k * Math.sqrt(2 * Math.PI)))
		    * Math.pow(Math.E, exp);
		}
    }
    
    
    /**
	 * Returns the weights of the solutions in the archive.
	 * 
	 * @return the weights of the solutions  in the archive.
	 */
	public double[] weights() {
	    return weights;
	}
    

    /**
     * This class represents the archive for continuous attributes.
     */
    public static class Continuous extends AttributeArchive {

	/**
	 * Default constructor.
	 * 
	 * @param index
	 *            the attribute index.
	 */
	public Continuous(int index) {
	    super(index);
	}

	@Override
	public Condition sample() {
	    return null;
	}
    }

    /**
     * This class represents the archive for nominal attributes.
     */
    public static class Nominal extends AttributeArchive {
	/**
	 * The number of values in the nominal attribute domain.
	 */
	private int size;

	/**
	 * Default constructor.
	 * 
	 * @param index
	 *            the attribute index.
	 * @param values
	 *            the number of values in the nominal attribute domain.
	 */
	public Nominal(int index, int size) {
	    super(index);
	    this.size = size;
	}

	@Override
	public Condition sample() {
	    Condition condition = new Condition();
	    condition.attribute = index;
	    condition.relation = EQUAL_TO;

	    if (!archive.isFull()) {
		// random sampling, since archive is not complete
		condition.value[0] = CONFIG.get(RANDOM_GENERATOR).nextInt(size);
	    } else {
		double[] probabilities = new double[size];
		Object[] solutions = archive.solutions();

		double[] weight =  new double[size];
		
		
		int n = 0;

		for (int i = 0; i < size; i++) {
		    for (int j = 0; j < solutions.length; j++) {
			// ugly, but necessary
			Condition c = (Condition) solutions[i];

			if (j == c.value[0]) {
			    if (weight[j] == 0) {
				// highest quality solution that uses value i
				weight[j] = weights[i];
			    }

			    // number of solutions that use value i
			    probabilities[j]++;
			}
		    }

		    // number of unused values
		    n += (probabilities[i] == 0) ? 1 : 0;
		}

		// calculates the weight of each value

		final double q = CONFIG.get(Q);
		double total = 0;

		for (int i = 0; i < size; i++) {
		    if (n > 0) {
			if (probabilities[i] > 0) {
			    probabilities[i] =
				    (weight[i] / probabilities[i]) + (q / n);
			} else {
			    probabilities[i] = (q / n);
			}
		    } else if (probabilities[i] > 0) {
			probabilities[i] = (weight[i] / probabilities[i]);
		    }

		    total += probabilities[i];
		}

		// roulette selection based on the weight of each value

		condition.value[0] = (size - 1);
		double slot = CONFIG.get(RANDOM_GENERATOR).nextDouble();
		double cumulative = 0.0;

		for (int i = 0; i < size; i++) {
		    probabilities[i] = cumulative + (probabilities[i] / total);
		    cumulative = probabilities[i];

		    if (slot <= probabilities[i]) {
			condition.value[0] = i;
			break;
		    }
		}
	    }

	    return condition;
	}
    }
}