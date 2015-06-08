/*
 * Dataset.java
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

import static myra.Attribute.Type.CONTINUOUS;
import static myra.Attribute.Type.NOMINAL;

import java.util.Arrays;

/**
 * This class represents the data.
 * 
 * @author Fernando Esteban Barril Otero
 */
public final class Dataset
{
    /**
     * The index representing a missing value.
     */
    public static final int MISSING_VALUE_INDEX = -1;

    /**
     * The string representing a missing value.
     */
    public static final String MISSING_VALUE = "?";

    /**
     * Flag indicating an uncovered instance.
     */
    public static final byte NOT_COVERED = 0;

    /**
     * Flag indicating an instance covered by a rule.
     */
    public static final byte RULE_COVERED = 1;

    /**
     * Flag indicating a previously covered instance.
     */
    public static final byte COVERED = 3;

    /**
     * The name of the dataset.
     */
    private String name;

    /**
     * The attributes of the dataset.
     */
    private Attribute[] attributes;

    /**
     * The instances of the dataset, represented as an array. Each instance has
     * the length of <code>attributes.length</code>.
     */
    private double[] instances;

    /**
     * Default constructor.
     */
    public Dataset()
    {
        attributes = new Attribute[0];
        instances = new double[0];
    }

    /**
     * Returns the attribute of the dataset at the specified index.
     * 
     * @param index the attribute index.
     * 
     * @return the attribute of the dataset at the specified index.
     */
    public Attribute getAttribute(int index)
    {
        return attributes[index];
    }

    /**
     * Returns the attributes of the dataset.
     * 
     * @return the attributes of the dataset.
     */
    public Attribute[] attributes()
    {
        return attributes;
    }

    /**
     * Returns the attribute with the specified name.
     * 
     * @param name the attribute name.
     * 
     * @return the attribute with the specified name.
     */
    public Attribute findAttribute(String name)
    {
        for (Attribute attribute : attributes)
        {
            if (attribute.getName().equals(name))
            {
                return attribute;
            }
        }

        throw new IllegalArgumentException("Attribute not found: " + name);
    }

    /**
     * Returns the dataset name.
     * 
     * @return the dataset name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the name of the dataset.
     * 
     * @param name the name to set.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Returns the number of instances in the dataset.
     * 
     * @return the number of instances in the dataset.
     */
    public final int size()
    {
        return (instances.length / attributes.length);
    }

    /**
     * Returns the number of instances in the dataset associated with the
     * specified class value.
     * 
     * @param value the class value.
     * 
     * @return the number of instances in the dataset associated with the
     *         specified class value.
     */
    public final int size(int value)
    {
        int count = 0;

        for (int i = 0; i < size(); i++)
        {
            if (instances[(i * attributes.length) + classIndex()] == value)
            {
                count++;
            }
        }

        return count;
    }

    /**
     * Adds an attribute to the dataset.
     * 
     * @param attribute the attribute to add.
     */
    public void add(Attribute attribute)
    {
        int index = attributes.length;
        attributes = Arrays.copyOf(attributes, attributes.length + 1);

        attributes[index] = attribute;
        attribute.setIndex(index);
    }

    /**
     * Adds an instance to the dataset.
     * 
     * @param values the values of the instance to add.
     */
    public void add(String[] values)
    {
        if (values.length != attributes.length)
        {
            throw new IllegalArgumentException("Invalid instance length: "
                + values.length + " (expected " + attributes.length + ")");
        }

        int offset = instances.length;
        instances = Arrays.copyOf(
            instances,
            instances.length + attributes.length);

        for (int i = 0; i < attributes.length; i++)
        {
            if (values[i].equals(MISSING_VALUE))
            {
                switch (attributes[i].getType())
                {
                    case NOMINAL:
                        instances[offset + i] = MISSING_VALUE_INDEX;
                        break;

                    case CONTINUOUS:
                        instances[offset + i] = Double.NaN;
                        break;
                }
            }
            else if (attributes[i].getType() == CONTINUOUS)
            {
                instances[offset + i] = Double.parseDouble(values[i]);
            }
            else if (attributes[i].getType() == NOMINAL)
            {
                int index = MISSING_VALUE_INDEX;

                for (int j = 0; j < attributes[i].values().length; j++)
                {
                    if (values[i].equals(attributes[i].value(j)))
                    {
                        index = j;
                        break;
                    }
                }

                instances[offset + i] = index;
            }
        }
    }

    /**
     * Adds an instance to the dataset.
     * 
     * @param values the values of the instance to add.
     */
    public void add(double[] values)
    {
        if (values.length != attributes.length)
        {
            throw new IllegalArgumentException("Invalid instance length: "
                + values.length + " (expected " + attributes.length + ")");
        }

        int offset = instances.length;

        instances = Arrays.copyOf(
            instances,
            instances.length + attributes.length);

        System.arraycopy(values, 0, instances, offset, values.length);
    }

    /**
     * Returns the values of the specified instance.
     * 
     * @param index the index of the instance.
     * 
     * @return the values of the specified instance.
     */
    public double[] get(int index)
    {
        double[] values = new double[attributes.length];
        System.arraycopy(instances,
            index * values.length,
            values,
            0,
            values.length);

        return values;
    }

    /**
     * Removes the specified instances from the dataset.
     * 
     * @param indexes the array of indexes to remove.
     * 
     * @return the size of the dataset after the removal of the instances.
     */
    public int remove(int ... indexes)
    {
        double[] resized =
            new double[instances.length - (indexes.length * attributes.length)];

        int current = 0;
        int target = 0;
        
        for (int removed : indexes)
        {
            if (current == removed)
            {
                current++;
            }
            else
            {
                int length = (removed - current) * attributes.length;
                int source =  current * attributes.length;

                System.arraycopy(instances, source, resized, target, length);

                target += length;
                current = removed + 1;
            }
        }
        
        if (target != resized.length)
        {
            int length = resized.length - target;
            int source =  current * attributes.length;

            System.arraycopy(instances, source, resized, target, length);            
        }
        
        this.instances = resized;
        
        return size();
    }

    /**
     * Returns the index of the majority class.
     * 
     * @param instances the instances array.
     * @param flag      the type of instances to be considered (valid values
     *                  are {@link #COVERED}, {@link #NOT_COVERED} and
     *                  {@link #RULE_COVERED}).
     * 
     * @return the index of the majority class.
     */
    public int findMajority(Instance[] instances, byte flag)
    {
        int classIndex = attributes.length - 1;
        double[] frequencies =
            new double[attributes[classIndex].values().length];

        for (int i = 0; i < size(); i++)
        {
            if (instances[i].flag == flag)
            {
                int index = (i * attributes.length) + classIndex;
                frequencies[(int) this.instances[index]] += instances[i].weight;
            }
        }

        int majority = -1;

        for (int i = 0; i < frequencies.length; i++)
        {
            if (majority == -1 || frequencies[majority] < frequencies[i])
            {
                majority = i;
            }
        }

        return majority;
    }

    /**
     * Returns the index of the class attribute.
     * 
     * @return the index of the class attribute.
     */
    public final int classIndex()
    {
        return attributes.length - 1;
    }

    /**
     * Returns the number of class values.
     * 
     * @return the number of class values.
     */
    public final int classLength()
    {
        return attributes[attributes.length - 1].values().length;
    }

    /**
     * Returns <code>true</code> if the specified value represents a missing
     * value.
     * 
     * @param attribute the attribute object.
     * @param value     the value to check.
     * 
     * @return <code>true</code> if the specified value represents a missing
     *         value; <code>false</code> otherwise.
     */
    public boolean isMissing(Attribute attribute, double value)
    {
        boolean missing = false;

        switch (attribute.getType())
        {
            case CONTINUOUS:
                missing = Double.isNaN(value);
                break;

            case NOMINAL:
                missing = (value == MISSING_VALUE_INDEX);
                break;
        }

        return missing;
    }

    /**
     * Returns the attribute value of a given instance.
     * 
     * @param instance  the instance index.
     * @param attribute the attribute index.
     * 
     * @return the attribute value of a given instance.
     */
    public double value(int instance, int attribute)
    {
        return instances[(instance * attributes.length) + attribute];
    }

    /**
     * Marks the current <code>RULE_COVERED</code> instances as
     * <code>COVERED</code>.
     * 
     * @param covered the instances array.
     * 
     * @return the number of available instances (<code>NOT_COVERED</code>
     *         instances).
     */
    public static int markCovered(Instance[] covered)
    {
        int available = 0;

        for (int j = 0; j < covered.length; j++)
        {
            if (covered[j].flag == RULE_COVERED)
            {
                covered[j].flag = COVERED;
            }
            else if (covered[j].flag == NOT_COVERED)
            {
                available++;
            }
        }

        return available;
    }

    /**
     * Marks the current <code>RULE_COVERED</code> instances as
     * <code>COVERED</code> associated with the specified class value. Instances
     * that are covered but associated with a different class value are set as
     * <code>NOT_COVERED</code>.
     * 
     * @param dataset   the current dataset.
     * @param covered   the instances array.
     * @param predicted the predicted class value.
     * 
     * @return the number of correctly covered instances.
     */
    public static int markCorrect(Dataset dataset, Instance[] covered,
                                  int predicted)
    {
        int marked = 0;

        for (int j = 0; j < covered.length; j++)
        {
            if (covered[j].flag == RULE_COVERED)
            {
                if (dataset.value(j, dataset.classIndex()) == predicted)
                {
                    covered[j].flag = COVERED;
                    marked++;
                }
                else
                {
                    covered[j].flag = NOT_COVERED;
                }
            }
        }

        return marked;
    }

    /**
     * Returns a copy of the dataset including only the instances associated
     * with the specified flag.
     * 
     * @param dataset the dataset to copy.
     * @param covered the instance array information.
     * @param flag    the instances flag.
     * 
     * @return a copy of the dataset.
     */
    public static Dataset filter(Dataset dataset, Instance[] covered, int flag)
    {
        Dataset clone = new Dataset();
        clone.attributes = dataset.attributes.clone();
        clone.name = dataset.name;

        for (int i = 0; i < dataset.size(); i++)
        {
            if (covered[i].flag == flag)
            {
                int start = clone.instances.length;
                int length = dataset.attributes.length;

                clone.instances = Arrays.copyOf(
                    clone.instances,
                    start + clone.attributes.length);

                System.arraycopy(
                    dataset.instances,
                    (i * length),
                    clone.instances,
                    start,
                    length);
            }
        }

        return clone;
    }

    /**
     * Struct-like class to hold the information about an instance.
     */
    public static final class Instance
        implements Cloneable
    {
        /**
         * The weight of the instance.
         */
        public double weight = 1.0;

        /**
         * The coverage flag of the instance.
         */
        public byte flag = NOT_COVERED;

        /**
         * Default constructor.
         */
        public Instance()
        {
            this(1.0, NOT_COVERED);
        }

        /**
         * Create a new <code>Instance</code>.
         * 
         * @param weight the weight of the instance.
         * @param flag   the covered flag of the instance.
         */
        public Instance(double weight, byte flag)
        {
            this.weight = weight;
            this.flag = flag;
        }

        /**
         * Returns a new instance array.
         * 
         * @param size the size of the array.
         * 
         * @return a new instance array.
         */
        public static Instance[] newArray(int size)
        {
            Instance[] instances = new Instance[size];

            for (int i = 0; i < size; i++)
            {
                instances[i] = new Instance();
            }

            return instances;
        }

        /**
         * Returns a copy of the specified instance array.
         * 
         * @param instances the instance array.
         * 
         * @return a copy of the specified instance array.
         */
        public static Instance[] copyOf(Instance[] instances)
        {
            Instance[] copy = new Instance[instances.length];

            for (int i = 0; i < copy.length; i++)
            {
                copy[i] = new Instance(instances[i].weight, instances[i].flag);
            }

            return copy;
        }

        /**
         * Sets the instances' flags to the specified flag.
         * 
         * @param instances the instance array.
         * @param flag      the flag to set.
         */
        public static void markAll(Instance[] instances, byte flag)
        {
            for (int i = 0; i < instances.length; i++)
            {
                instances[i].flag = flag;
            }
        }

        /**
         * Sets the <code>from</code> flags to the specified <code>to</code>
         * flag.
         * 
         * @param instances the instance array.
         * @param from      the original flag.
         * @param to        the flag to set.
         */
        public static void mark(Instance[] instances, byte from, byte to)
        {
            for (int i = 0; i < instances.length; i++)
            {
                if (instances[i].flag == from)
                {
                    instances[i].flag = to;
                }
            }
        }
    }
}