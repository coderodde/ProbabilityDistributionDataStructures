package net.coderodde.stat.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
 * @version 1.61 (Sep 30, 2016)
 */
public class BinarySearchProbabilityDistribution<E> 
extends AbstractProbabilityDistribution<E> {

    /**
     * This class implements the actual entry in the distribution.
     * 
     * @param <E> the actual element type.
     */
    private static final class Entry<E> {
        
        private final E element;
        
        private final double weight;
        
        private double accumulatedWeight;
        
        Entry(E element, double weight, double accumulatedWeight) {
            this.element = element;
            this.weight = weight; 
            this.accumulatedWeight = accumulatedWeight;
        }
        
        E getElement() {
            return element;
        }
        
        double getWeight() {
            return weight;
        }
        
        double getAccumulatedWeight() {
            return accumulatedWeight;
        }
        
        void addAccumulatedWeight(double delta) {
            accumulatedWeight += delta;
        }
    }
    
    /**
     * Holds all the elements currently stored in this probability distribution.
     */
    private final Set<E> filterSet = new HashSet<>();
    
    /**
     * Holds the actual distribution entries.
     */
    private final List<Entry<E>> storage = new ArrayList<>();
    
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
        
        Entry<E> e = new Entry<>(element, weight, totalWeight);
        storage.add(e);
        totalWeight += weight;
        filterSet.add(element);
        return true;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public E sampleElement() {
        checkNotEmpty();
        double value = totalWeight * random.nextDouble();
        
        int left = 0;
        int right = storage.size() - 1;
        
        while (left < right) {
            int middle = left + ((right - left) >> 1);
            Entry<E> middleEntry = storage.get(middle);
            double lowerBound = middleEntry.getAccumulatedWeight();
            double upperBound = lowerBound + middleEntry.getWeight();
            
            if (lowerBound <= value && value < upperBound) {
                return middleEntry.getElement();
            }
            
            if (value < lowerBound) {
                right = middle - 1;
            } else {
                left = middle + 1;
            }
        }
        
        return storage.get(left).getElement();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean contains(E element) {
        return filterSet.contains(element);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean removeElement(E element) {
        if (!filterSet.contains(element)) {
            return false;
        }

        int index = indexOf(element);
        double weight = storage.get(index).getWeight();
        totalWeight -= weight;
        int storageLength = storage.size();
        
        for (int i = index + 1; i < storageLength; ++i) {
            storage.get(i).addAccumulatedWeight(-weight);
        }
        
        storage.remove(index);
        filterSet.remove(element);
        return true;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void clear() {
        storage.clear();
        totalWeight = 0.0;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isEmpty() {
        return storage.isEmpty();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int size() {
        return storage.size();
    }

    private int indexOf(E element) {
        int storageLength = storage.size();
        
        for (int i = 0; i < storageLength; ++i) {
            if (Objects.equals(element, storage.get(i).getElement())) {
                return i;
            }
        }

        return -1;
    }
    
    private void checkNotEmpty() {
        checkNotEmpty(storage.size());
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
