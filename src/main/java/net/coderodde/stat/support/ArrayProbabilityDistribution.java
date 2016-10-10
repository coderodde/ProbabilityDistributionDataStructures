package net.coderodde.stat.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.coderodde.stat.AbstractProbabilityDistribution;

/**
 * This class implements a probability distribution relying on an array of 
 * elements. The running times are as follows:
 * 
 * <table style="border: 1px solid black;">
 * <tr style="border: 1px solid black;"><td>Method</td>  <td>Complexity</td></tr>
 * <tr><td><tt>addElement   </tt> </td>  <td>amortized constant time,</td></tr>
 * <tr><td><tt>sampleElement</tt> </td>  <td><tt>worst case O(n)</tt>,</td></tr>
 * <tr><td><tt>removeElement</tt> </td>  <td><tt>worst case O(n)</tt>.</td></tr>
 * </table>
 * 
 * @param <E> the actual type of the elements stored in this probability 
 *            distribution.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.61 (Sep 30, 2016)
 */
public class ArrayProbabilityDistribution<E> 
extends AbstractProbabilityDistribution<E> {

    /**
     * Couples the actual element with its respective weight.
     * 
     * @param <E> the actual type of the element.
     */
    private static final class Entry<E> {
        
        /**
         * The actual element.
         */
        private final E element;
        
        /**
         * The weight assigned to the {@code element}.
         */
        private double weight;
        
        Entry(E element, double weight) {
            this.element = element;
            this.weight = weight;
        }
        
        E getElement() {
            return element;
        }
        
        double getWeight() {
            return weight;
        }
        
        void setWeight(double weight) {
            this.weight = weight;
        }
    }
    
    /**
     * The actual storage array holding the entries.
     */
    private final List<Entry<E>> storage = new ArrayList<>();
    
    /**
     * This map maps each element in this probability distribution to its 
     * respective entry object.
     */
    private final Map<E, Entry<E>> map = new HashMap<>();
    
    public ArrayProbabilityDistribution() {
        this(new Random());
    }

    public ArrayProbabilityDistribution(Random random) {
        super(random);
    }

    /**
     * {@inheritDoc } 
     */
    @Override
    public boolean addElement(E element, double weight) {
        checkWeight(weight);
        Entry<E> entry = map.get(element);
        
        if (entry != null) {
            entry.setWeight(entry.getWeight() + weight);
        } else {
            entry = new Entry<>(element, weight);
            map.put(element, entry);
            storage.add(entry);
        }
        
        totalWeight += weight;
        return true;
    }

    /**
     * {@inheritDoc } 
     */
    @Override
    public E sampleElement() {
        checkNotEmpty();
        double value = random.nextDouble() * totalWeight;
        int distributionSize = storage.size();
        
        for (int i = 0; i < distributionSize; ++i) {
            Entry<E> entry = storage.get(i);
            double currentWeight = entry.getWeight();
            
            if (value < currentWeight) {
                return entry.getElement();
            }
            
            value -= currentWeight;
        }

        throw new IllegalStateException("Should not get here.");
    }

    /**
     * {@inheritDoc } 
     */
    @Override
    public boolean removeElement(E element) {
        Entry<E> entry = map.remove(element);
        
        if (entry == null) {
            return false;
        }
        
        totalWeight -= entry.getWeight();
        storage.remove(entry);
        return true;
    }

    /**
     * {@inheritDoc } 
     */
    @Override
    public void clear() {
        map.clear();
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
    
    /**
     * {@inheritDoc } 
     */
    @Override
    public boolean contains(E element) {
        return map.containsKey(element);
    }
    
    protected void checkNotEmpty() {
        checkNotEmpty(storage.size());
    }
    
    public static void main(String[] args) {
        AbstractProbabilityDistribution<Integer> pd = 
                new ArrayProbabilityDistribution<>();
        
        pd.addElement(1, 1.0);
        pd.addElement(2, 2.0);
        pd.addElement(3, 3.0);
        
        int[] count = new int[4];
        
        for (int i = 0; i < 1000; ++i) {
            count[pd.sampleElement()]++;
        }
        
        System.out.println(Arrays.toString(count));
    }
}
