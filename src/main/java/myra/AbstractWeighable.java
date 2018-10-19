/*
 * AbstractWeighable.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2018 Fernando Esteban Barril Otero
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
 * Skeleton implementation for weighable archive solutions.
 * 
 * @author Fernando Esteban Barril Otero
 * 
 * @see Weighable
 */
public abstract class AbstractWeighable<T> implements Weighable<T> {
    /**
     * The solution weight. The weight based on the rank of the solution
     * in the archive.
     */
    protected double weight;
    
    @Override
    public double getWeight() {
	return weight;
    }

    @Override
    public void setWeight(double weight) {
	this.weight = weight;
    }
}