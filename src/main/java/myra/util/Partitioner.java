/*
 * Partitioner.java
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

package myra.util;

import static myra.Config.CONFIG;
import static myra.datamining.Algorithm.RANDOM_GENERATOR;
import static myra.datamining.Dataset.COVERED;
import static myra.datamining.Dataset.NOT_COVERED;

import java.util.ArrayList;
import java.util.HashMap;

import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;

/**
 * This class can be used to partition de data in-memory.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class Partitioner {
    /**
     * The number of partitions to generate.
     */
    private int size;

    /**
     * The dataset to partition.
     */
    private Dataset dataset;

    /**
     * The dataset partitions.
     */
    private Instance[][] partitions;

    /**
     * Default constructor.
     * 
     * @param dataset
     *            the dataset to partition.
     * @param slices
     *            the number of partitions.
     */
    public Partitioner(Dataset dataset, int slices) {
	this.size = slices;
	this.dataset = dataset;
	partitions = new Instance[slices][];
    }

    /**
     * Partitions the dataset respecting the class attribute values' frequency.
     */
    public void stratified() {
	HashMap<Integer, ArrayList<Integer>> instances =
		new HashMap<Integer, ArrayList<Integer>>();

	for (int i = 0; i < dataset.size(); i++) {
	    int klass = (int) dataset.value(i, dataset.classIndex());
	    ArrayList<Integer> indexes = instances.get(klass);

	    if (indexes == null) {
		indexes = new ArrayList<Integer>();
		instances.put(klass, indexes);
	    }

	    indexes.add(i);
	}

	for (int i = 0; i < partitions.length; i++) {
	    partitions[i] = Instance.newArray(dataset.size());
	    Instance.markAll(partitions[i], COVERED);
	}

	int current = 0;

	for (int i = 0; i < instances.size(); i++) {
	    ArrayList<Integer> indexes = instances.get(i);

	    while (indexes != null && !indexes.isEmpty()) {
		for (int j = current; j < partitions.length; j++) {
		    int index = CONFIG.get(RANDOM_GENERATOR)
			    .nextInt(indexes.size());
		    partitions[j][indexes.get(index)].flag = NOT_COVERED;

		    indexes.remove(index);
		    current = j;

		    if (indexes.isEmpty()) {
			break;
		    }
		}

		current = ((current + 1) < partitions.length) ? current + 1 : 0;
	    }
	}

	// sanity check

	// for (int i = current; i < partitions.length; i++)
	// {
	// System.out.print("Partition " + i + ": ");
	// printFrequencies(dataset, partitions[i]);
	// }
    }

    /**
     * Returns training and validation datasets according to the partitions.
     * 
     * @param training
     *            number of training partitions.
     * @param validation
     *            number of validation partitions.
     * 
     * @return training and validation datasets according to the partitions.
     */
    public Dataset[] split(int training, int validation) {
	if ((training + validation) > size) {
	    throw new IllegalArgumentException("Invalid number of partitions: "
		    + (training + validation) + " found; " + size
		    + " expected");
	}

	Dataset[] split = new Dataset[2];
	Instance[] instances = Instance.newArray(dataset.size());
	Instance.markAll(instances, COVERED);

	for (int i = 0; i < training; i++) {
	    for (int j = 0; j < partitions[i].length; j++) {
		if (partitions[i][j].flag == NOT_COVERED) {
		    instances[j].flag = NOT_COVERED;
		}
	    }
	}

	split[0] = Dataset.filter(dataset, instances, NOT_COVERED);
	Instance.markAll(instances, COVERED);

	for (int i = training; i < (training + validation); i++) {
	    for (int j = 0; j < partitions[i].length; j++) {
		if (partitions[i][j].flag == NOT_COVERED) {
		    instances[j].flag = NOT_COVERED;
		}
	    }
	}

	split[1] = Dataset.filter(dataset, instances, NOT_COVERED);

	return split;
    }

    /*
     * void printFrequencies(Dataset dataset, Instance[] instances) { int[]
     * frequencies = new int[dataset.classLength()];
     * 
     * for (int i = 0; i < instances.length; i++) { if (instances[i].flag ==
     * NOT_COVERED) { frequencies[(int) dataset.value(i,
     * dataset.classIndex())]++; } }
     * 
     * System.out.print("{");
     * 
     * for (int i = 0; i < frequencies.length; i++) { if (i > 0) {
     * System.out.print(", "); }
     * 
     * System.out.print(frequencies[i]); }
     * 
     * System.out.println("}"); }
     */
}