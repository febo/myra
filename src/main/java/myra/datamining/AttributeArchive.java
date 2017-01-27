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
import static myra.datamining.Attribute.IN_RANGE;
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
     * The config key for the weight calculation parameter <i>q</i>.
     */
    public static final ConfigKey<Double> CONVERGENCE = new ConfigKey<>();

    /**
     * Default value for the weight calculation parameter <i>q</i>.
     */
    public static final double DEFAULT_Q = 0.05099;
    
    /**
     * Default value for the weight calculation parameter <i>q</i>.
     */
    public static final double DEFAULT_CONVERGENCE = 0.6795;

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
    	 * The number of values in the nominal attribute domain.
    	 */
    	private double min;
    	
    	/**
    	 * The number of values in the nominal attribute domain.
    	 */
    	private double max;
    	
	/**
	 * Default constructor.
	 * 
	 * @param index
	 *            the attribute index.
	 */
    	
	public Continuous(int index, double min, double max) {
		super(index);
		this.min = min;
		this.max = max;  
	}

	@Override
	public Condition sample() {
		 // Creating a condition for continuous attribute
		 Condition condition = new Condition();
		 condition.attribute = index;
		 // sampling the operator using categorical sampling from the archive
		 condition = relationsample(condition);
		 // if the archive is not full
		 if (!archive.isFull()) {
			// random sampling, since archive is not complete for the continuous attribute
			condition.value[0]= (CONFIG.get(RANDOM_GENERATOR).nextDouble() * (max - min)) + min;
			// if the relation  is In range we need to sample another value
			if(condition.relation == IN_RANGE){
				condition.value[1]= (CONFIG.get(RANDOM_GENERATOR).nextDouble() * (max - condition.value[0])) + condition.value[0];
			}	
		} 
		else {
			// Sampling from the archive the first value of the attribute
			condition = sampleConditionValue(condition,0);
			// if the relation is in range we need to sample another value
			if(condition.relation == IN_RANGE){
				condition = sampleConditionValue(condition,1);
			}
		}
		return condition;
	}
	
	public Condition sampleConditionValue(Condition condition,int index){
		// selecting the index of the random solution with same condition.
		int selectedindex = selectRandomSolution(condition);
		
		Object[] solutions = archive.solutions();
		double std = 0;
		int count = 0;
		final double convergence = CONFIG.get(CONVERGENCE);
		// if the relation choosen was found in the archive
		if(selectedindex != -1)
		{
			// ugly, but necessary
			Condition c1 = (Condition) solutions[selectedindex];
			
			for (int j = 0; j < solutions.length; j++) {
				// ugly, but necessary
				Condition c = (Condition) solutions[j];
				// if this is the relation, we get the std and count 
				if(c.relation == condition.relation){
					std += Math.abs(c.value[index]- c1.value[index]);
					count++;
				}
			}
			
			if (count != 0) {	
				std = std / count;
				std *= convergence;
				// calculating an gussian using the std and mean
				condition.value[index] = (CONFIG.get(RANDOM_GENERATOR).nextGaussian() * std ) + c1.value[index];
			}
			else
			{
				// if there is not values in archive using a random generated value
				condition.value[index]= (CONFIG.get(RANDOM_GENERATOR).nextDouble() * (max - min)) + min;
			}
		} 
		else // if the relation operator was not found in archive
		{	
			// a random value for the limits will be used
			condition.value[index]= (CONFIG.get(RANDOM_GENERATOR).nextDouble() * (max - min)) + min;
		}
		return condition;
	}
	
	
	/**
	 * Randomly selecting a solution in the archive with the same condition
	 * @param condition
	 * @return the index of selected solution
	 */
	public int selectRandomSolution(Condition condition){
		// archive size
		int size = archive.size();
		Object[] solutions = archive.solutions();
		double[] probabilities = new double[size];
		double total =0;
		// applying roulette wheel selection
		for (int i = 0; i < size; i++) 
		{
			Condition c = (Condition) solutions[i];
			if(c.relation == condition.relation)
			{
				probabilities[i]= weights[i];
				total += probabilities[i];	
			}
			else
				probabilities[i] = 0;
			
		}
		double cumulative = 0.0;
		double slot = CONFIG.get(RANDOM_GENERATOR).nextDouble();
		int selected = - 1;
		
		for (int i = 0; i < 2; i++) {
		    probabilities[i] = cumulative + (probabilities[i] / total);
		    cumulative = probabilities[i];

		    if (slot <= probabilities[i]) {
		    	selected = i;
			break;
		    }
		}
		return selected;
		
	}
	
	/**
	 * sampling a condition using the categorical sampling process
	 * @param condition
	 * @return condition 
	 */
	public Condition relationsample(Condition condition){
		
		if (!archive.isFull()) {
			// random sampling, since archive is not complete
			condition.relation = (short) (CONFIG.get(RANDOM_GENERATOR).nextInt(3) + 1);
			
		    } else {
			double[] probabilities = new double[3];
			Object[] solutions = archive.solutions();

			double[] weight =  new double[3];
			
			
			int n = 0;

			for (int i = 0; i < 3; i++) {
			    for (int j = 0; j < solutions.length; j++) {
				// ugly, but necessary
				Condition c = (Condition) solutions[j];

				if (i == c.relation) {
				    if (weight[i] == 0) {
					// highest quality solution that uses value i
					weight[i] = weights[j];
				    }

				    // number of solutions that use value i
				    probabilities[i]++;
				}
			    }

			    // number of unused values
			    n += (probabilities[i] == 0) ? 1 : 0;
			}

			// calculates the weight of each value

			final double q = CONFIG.get(Q);
			double total = 0;

			for (int i = 0; i < 3; i++) {
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

			condition.relation = 3;
			double slot = CONFIG.get(RANDOM_GENERATOR).nextDouble();
			double cumulative = 0.0;

			for (int i = 0; i < 2; i++) {
			    probabilities[i] = cumulative + (probabilities[i] / total);
			    cumulative = probabilities[i];

			    if (slot <= probabilities[i]) {
				condition.relation = (short) i;
				break;
			    }
			}
		    }

		    return condition;
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
			Condition c = (Condition) solutions[j];

			if (i == c.value[0]) {
			    if (weight[i] == 0) {
				// highest quality solution that uses value i
				weight[i] = weights[j];
			    }

			    // number of solutions that use value i
			    probabilities[i]++;
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