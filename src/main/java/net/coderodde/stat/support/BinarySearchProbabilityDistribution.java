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
    public BinarySearchProbabilityDistribution(final Random random) {
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
    public boolean addElement(final E element, final double weight) {
        checkWeight(weight);
        
        if (this.filterSet.contains(element)) {
            return false;
        }
        
        ensureCapacity(this.size + 1);
        
        this.objectStorageArray[this.size] = element;
        this.weightStorageArray[this.size] = weight;
        this.accumulatedWeightArray[this.size] = this.totalWeight;
        this.totalWeight += weight;
        this.size++;
        this.filterSet.add(element);
        
        return true;
    }

    @Override
    public E sampleElement() {
        final double value = this.totalWeight * this.random.nextDouble();
        
        int left = 0;
        int right = this.size - 1;
        
        while (left < right) {
            final int middle = left + ((right - left) >> 1);
            
            final double lowerBound = this.accumulatedWeightArray[middle];
            final double upperBound = lowerBound + 
                                      this.weightStorageArray[middle];
            
            if (lowerBound <= value && value < upperBound) {
                return (E) this.objectStorageArray[middle];
            }
            
            if (value < lowerBound) {
                right = middle - 1;
            } else {
                left = middle + 1;
            }
        }
        
        return (E) this.objectStorageArray[left];
    }

    @Override
    public boolean contains(final E element) {
        return this.filterSet.contains(element);
    }

    @Override
    public boolean removeElement(final E element) {
        if (!this.filterSet.contains(element)) {
            return false;
        }

        final int index = indexOf(element);
        final double weight = this.weightStorageArray[index];
        this.totalWeight -= weight;

        for (int j = index + 1; j < this.size; ++j) {
            this.objectStorageArray[j - 1]     = this.objectStorageArray[j];
            this.weightStorageArray[j - 1]     = this.weightStorageArray[j];
            this.accumulatedWeightArray[j - 1] = this.accumulatedWeightArray[j] 
                                                 - weight;
        }
        
        this.objectStorageArray[--this.size] = null;
        
        return true;    
    }

    @Override
    public void clear() {
        for (int i = 0; i < this.size; ++i) {
            this.objectStorageArray[i] = null;
        }
        
        this.size = 0;
        this.totalWeight = 0.0;
    }
    
    private void ensureCapacity(final int requestedCapacity) {
        if (requestedCapacity > objectStorageArray.length) {
            final int newCapacity = Math.max(requestedCapacity, 
                                             2 * objectStorageArray.length);
            final Object[] newObjectStorageArray = new Object[newCapacity];
            final double[] newWeightStorageArray = new double[newCapacity];
            final double[] newAccumulatedWeightArray = new double[newCapacity];

            System.arraycopy(this.objectStorageArray, 
                             0, 
                             newObjectStorageArray, 
                             0, 
                             this.size);

            System.arraycopy(this.weightStorageArray,
                             0,
                             newWeightStorageArray, 
                             0,
                             this.size);
            
            System.arraycopy(this.accumulatedWeightArray,
                             0, 
                             newAccumulatedWeightArray, 
                             0, 
                             this.size);

            this.objectStorageArray = newObjectStorageArray;
            this.weightStorageArray = newWeightStorageArray;
            this.accumulatedWeightArray = newAccumulatedWeightArray;
        }
    }

    private int indexOf(final E element) {
        for (int i = 0; i < this.size; ++i) {
            if (Objects.equals(element, this.objectStorageArray[i])) {
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
