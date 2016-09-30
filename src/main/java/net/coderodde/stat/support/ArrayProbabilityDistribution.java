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
 * This class implements a probability distribution relying on an array of 
 * elements. The running times are as follows:
 * 
 * <table>
 * <tr><td>Method</td>  <td>Complexity</td></tr>
 * <tr><td><tt>addElement   </tt> </td>  <td>amortized constant time,</td></tr>
 * <tr><td><tt>sampleElement</tt> </td>  <td><tt>O(n)</tt>,</td></tr>
 * <tr><td><tt>removeElement</tt> </td>  <td><tt>O(n)</tt>.</td></tr>
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
        private final double weight;
        
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
    }
    
    /**
     * The actual storage array holding the entries.
     */
    private final List<Entry<E>> storage = new ArrayList<>();
    
    /**
     * The set keeping track of all entries currently in this distribution.
     */
    private final Set<E> filterSet = new HashSet<>();

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

        if (filterSet.contains(element)) {
            // 'element' is already present in this probability distribution.
            return false;
        }

        storage.add(new Entry<>(element, weight));
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
        if (!filterSet.contains(element)) {
            return false;
        }

        totalWeight -= storage.remove(indexOf(element)).getWeight();
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
    
    /**
     * {@inheritDoc } 
     */
    @Override
    public boolean contains(E element) {
        return filterSet.contains(element);
    }

    private int indexOf(E element) {
        for (int i = 0; i < storage.size(); ++i) {
            E currentElement = storage.get(i).getElement();
            
            if (Objects.equals(currentElement, element)) {
                return i;
            }
        }
        
        return -1;
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
