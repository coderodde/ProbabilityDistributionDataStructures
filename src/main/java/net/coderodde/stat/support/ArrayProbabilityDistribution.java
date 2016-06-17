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

    public ArrayProbabilityDistribution(final Random random) {
        super(random);
        this.objectStorageArray = new Object[DEFAULT_STORAGE_ARRAYS_CAPACITY];
        this.weightStorageArray = new double[DEFAULT_STORAGE_ARRAYS_CAPACITY];
    }

    /**
     * {@inheritDoc } 
     */
    @Override
    public boolean addElement(final E element, final double weight) {
        checkWeight(weight);

        if (filterSet.contains(element)) {
            // 'element' is already present in this probability distribution.
            return false;
        }

        ensureCapacity(this.size + 1);
        objectStorageArray[this.size] = element;
        weightStorageArray[this.size] = weight; 
        this.totalWeight += weight;
        this.size++;
        this.filterSet.add(element);
        return true;
    }

    /**
     * {@inheritDoc } 
     */
    @Override
    public E sampleElement() {
        checkNotEmpty();
        double value = this.random.nextDouble() * this.totalWeight;

        for (int i = 0; i < this.size; ++i) {
            if (value < this.weightStorageArray[i]) {
                return (E) this.objectStorageArray[i];
            }

            value -= this.weightStorageArray[i];
        }

        throw new IllegalStateException("Should not get here.");
    }

    /**
     * {@inheritDoc } 
     */
    @Override
    public boolean removeElement(final E element) {
        if (!this.filterSet.contains(element)) {
            return false;
        }

        final int index = indexOf(element);
        this.totalWeight -= this.weightStorageArray[index];

        for (int j = index + 1; j < this.size; ++j) {
            objectStorageArray[j - 1] = objectStorageArray[j];
            weightStorageArray[j - 1] = weightStorageArray[j];
        }

        objectStorageArray[--this.size] = null;
        return true;
    }

    /**
     * {@inheritDoc } 
     */
    @Override
    public void clear() {
        for (int i = 0; i < this.size; ++i) {
            objectStorageArray[i] = null;
        }

        this.size = 0; 
        this.totalWeight = 0.0;
    }

    /**
     * {@inheritDoc } 
     */
    @Override
    public boolean contains(E element) {
        return this.filterSet.contains(element);
    }

    private int indexOf(final E element) {
        for (int i = 0; i < this.size; ++i) {
            if (Objects.equals(element, this.objectStorageArray[i])) {
                return i;
            }
        }

        return -1;
    }

    private void ensureCapacity(final int requestedCapacity) {
        if (requestedCapacity > objectStorageArray.length) {
            final int newCapacity = Math.max(requestedCapacity, 
                                             2 * objectStorageArray.length);
            final Object[] newObjectStorageArray = new Object[newCapacity];
            final double[] newWeightStorageArray = new double[newCapacity];

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

            this.objectStorageArray = newObjectStorageArray;
            this.weightStorageArray = newWeightStorageArray;
        }
    }
}
