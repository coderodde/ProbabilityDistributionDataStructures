package net.coderodde.stat.support;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import net.coderodde.stat.AbstractProbabilityDistribution;

/**
 * This class implements a probability distribution data structure that 
 * maintains an accumulated sum of weights and thus allows sampling the elements
 * in worst-case logarithmic time.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Jun 17, 2016)
 */
public class BinarySearchProbabilityDistribution<E> 
extends AbstractProbabilityDistribution<E> {

    private static final int DEFAULT_STORAGE_ARRAYS_CAPACITY = 8;
    
    /**
     * Holds all the elements currently stored in this probability distribution.
     */
    private final Set<E> filterSet = new HashSet<>();
    
    /**
     * Stores all the actual elements in this probability distribution.
     */
    private Object[] objectStorageArray;
    
    /**
     * Stores all the actual elements in this probability distribution.
     */
    private double[] weightStorageArray;
    
    /**
     * Stores the accumulated weights in order to be able to perform binary 
     * search.
     */
    private double[] accumulatedWeightArray;
    
    /**
     * Constructs this probability distribution with default random number 
     * generator.
     */
    public BinarySearchProbabilityDistribution() {
        this(new Random());
    }
    
    /**
     * Constructs this probability distribution with given random number 
     * generator.
     * 
     * @param random the random number generator.
     */
    public BinarySearchProbabilityDistribution(Random random) {
        super(random);
        this.objectStorageArray = new Object[DEFAULT_STORAGE_ARRAYS_CAPACITY];
        this.weightStorageArray = new double[DEFAULT_STORAGE_ARRAYS_CAPACITY];
        this.accumulatedWeightArray = 
                new double[DEFAULT_STORAGE_ARRAYS_CAPACITY];
    }
    
    /**
     * {@inheritDoc } 
     */
    @Override
    public boolean addElement(E element, double weight) {
        checkWeight(weight);
        
        if (filterSet.contains(element)) {
            return false;
        }
        
        ensureCapacity(size + 1);
        objectStorageArray[size] = element;
        weightStorageArray[size] = weight;
        accumulatedWeightArray[size] = totalWeight;
        totalWeight += weight;
        size++;
        filterSet.add(element);
        return true;
    }

    @Override
    public E sampleElement() {
        double value = totalWeight * random.nextDouble();
        
        int left = 0;
        int right = size - 1;
        
        while (left < right) {
            int middle = left + ((right - left) >> 1);
            double lowerBound = accumulatedWeightArray[middle];
            double upperBound = lowerBound + weightStorageArray[middle];
            
            if (lowerBound <= value && value < upperBound) {
                return (E) objectStorageArray[middle];
            }
            
            if (value < lowerBound) {
                right = middle - 1;
            } else {
                left = middle + 1;
            }
        }
        
        return (E) objectStorageArray[left];
    }

    @Override
    public boolean contains(E element) {
        return filterSet.contains(element);
    }

    @Override
    public boolean removeElement(E element) {
        if (!filterSet.contains(element)) {
            return false;
        }

        int index = indexOf(element);
        double weight = weightStorageArray[index];
        totalWeight -= weight;

        for (int j = index + 1; j < size; ++j) {
            objectStorageArray[j - 1]     = objectStorageArray[j];
            weightStorageArray[j - 1]     = weightStorageArray[j];
            accumulatedWeightArray[j - 1] = accumulatedWeightArray[j] - weight;
        }
        
        objectStorageArray[size] = null;
        return true;    
    }

    @Override
    public void clear() {
        for (int i = 0; i < size; ++i) {
            objectStorageArray[i] = null;
        }
        
        size = 0;
        totalWeight = 0.0;
    }
    
    private void ensureCapacity(int requestedCapacity) {
        if (requestedCapacity > objectStorageArray.length) {
            int newCapacity = Math.max(requestedCapacity, 
                                       2 * objectStorageArray.length);
            Object[] newObjectStorageArray = new Object[newCapacity];
            double[] newWeightStorageArray = new double[newCapacity];
            double[] newAccumulatedWeightArray = new double[newCapacity];

            System.arraycopy(objectStorageArray, 
                             0, 
                             newObjectStorageArray, 
                             0, 
                             size);

            System.arraycopy(weightStorageArray,
                             0,
                             newWeightStorageArray, 
                             0,
                             size);
            
            System.arraycopy(accumulatedWeightArray,
                             0, 
                             newAccumulatedWeightArray, 
                             0, 
                             size);

            objectStorageArray = newObjectStorageArray;
            weightStorageArray = newWeightStorageArray;
            accumulatedWeightArray = newAccumulatedWeightArray;
        }
    }

    private int indexOf(E element) {
        for (int i = 0; i < size; ++i) {
            if (Objects.equals(element, objectStorageArray[i])) {
                return i;
            }
        }

        return -1;
    }
    
    public static void main(String[] args) {
        BinarySearchProbabilityDistribution<Integer> d = new BinarySearchProbabilityDistribution<>();
        
        d.addElement(1, 1.0);
        d.addElement(2, 1.5);
        d.addElement(3, 0.5);
        d.addElement(4, 2.0);
        d.addElement(5, 2.2);
        
        d.removeElement(3);
        
        System.out.println("");
        
        binarySearchProbabilityDistributionDemo();
    }
    
    private static void binarySearchProbabilityDistributionDemo() {
        BinarySearchProbabilityDistribution<Integer> pd = 
                new BinarySearchProbabilityDistribution<>();

        pd.addElement(0, 1.0);
        pd.addElement(1, 1.0);
        pd.addElement(2, 1.0);
        pd.addElement(3, 3.0);

        int[] counts = new int[4];

        for (int i = 0; i < 100; ++i) {
            Integer myint = pd.sampleElement();
            counts[myint]++;
            System.out.println(myint);
        }

        System.out.println(Arrays.toString(counts));
    }
}
