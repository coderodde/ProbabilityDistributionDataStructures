package net.coderodde.stat.support;

import java.util.HashSet;
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
 * @version 1.6 (Jun 11, 2016)
 */
public class ArrayProbabilityDistribution<E> 
extends AbstractProbabilityDistribution<E> {

    private static final int DEFAULT_STORAGE_ARRAYS_CAPACITY = 8;

    private Object[] objectStorageArray;
    private double[] weightStorageArray;
    private final Set<E> filterSet = new HashSet<>();

    public ArrayProbabilityDistribution() {
        this(new Random());
    }

    public ArrayProbabilityDistribution(Random random) {
        super(random);
        this.objectStorageArray = new Object[DEFAULT_STORAGE_ARRAYS_CAPACITY];
        this.weightStorageArray = new double[DEFAULT_STORAGE_ARRAYS_CAPACITY];
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

        ensureCapacity(size + 1);
        objectStorageArray[size] = element;
        weightStorageArray[size] = weight; 
        totalWeight += weight;
        size++;
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

        for (int i = 0; i < size; ++i) {
            if (value < weightStorageArray[i]) {
                return (E) objectStorageArray[i];
            }

            value -= weightStorageArray[i];
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

        int index = indexOf(element);
        totalWeight -= weightStorageArray[index];

        for (int j = index + 1; j < size; ++j) {
            objectStorageArray[j - 1] = objectStorageArray[j];
            weightStorageArray[j - 1] = weightStorageArray[j];
        }

        objectStorageArray[--size] = null;
        return true;
    }

    /**
     * {@inheritDoc } 
     */
    @Override
    public void clear() {
        for (int i = 0; i < size; ++i) {
            objectStorageArray[i] = null;
        }

        size = 0; 
        totalWeight = 0.0;
    }

    /**
     * {@inheritDoc } 
     */
    @Override
    public boolean contains(E element) {
        return filterSet.contains(element);
    }

    private int indexOf(E element) {
        for (int i = 0; i < size; ++i) {
            if (Objects.equals(element, objectStorageArray[i])) {
                return i;
            }
        }

        return -1;
    }

    private void ensureCapacity(int requestedCapacity) {
        if (requestedCapacity > objectStorageArray.length) {
            int newCapacity = Math.max(requestedCapacity, 
                                             2 * objectStorageArray.length);
            Object[] newObjectStorageArray = new Object[newCapacity];
            double[] newWeightStorageArray = new double[newCapacity];

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

            objectStorageArray = newObjectStorageArray;
            weightStorageArray = newWeightStorageArray;
        }
    }
}
