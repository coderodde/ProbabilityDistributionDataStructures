package net.coderodde.stat;

import java.util.Objects;
import java.util.Random;

/**
 * This class implements an abstract base class for probability distributions. 
 * Elements are added with strictly positive weights and whenever asking this 
 * data structure for a random element, their respective weights are taken into 
 * account. For example, if this data structure contains three different 
 * elements (<tt>a</tt>, <tt>b</tt>, <tt>c</tt> with respective weights 
 * <tt>1.0</tt>, <tt>1.0</tt>, <tt>3.0</tt>), whenever asking for a random 
 * element, there is 20 percent chance of obtaining <tt>a</tt>, 20 percent 
 * chance of obtaining <tt>b</tt>, and 60 percent chance of obtaining 
 * <tt>c</tt>.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.61 (Oct 10, 2016)
 */
public abstract class AbstractProbabilityDistribution<E> {

    /**
     * The sum of all weights.
     */
    protected double totalWeight; 

    /**
     * The random number generator of this probability distribution.
     */
    protected final Random random;

    /**
     * Constructs this probability distribution.
     */
    protected AbstractProbabilityDistribution() {
        this(new Random());
    }

    /**
     * Constructs this probability distribution using the input random number 
     * generator.
     * 
     * @param random the random number generator.
     */
    protected AbstractProbabilityDistribution(Random random) {
        this.random = 
                Objects.requireNonNull(random, 
                                       "The random number generator is null.");
    }

    /**
     * Returns {@code true} if this probability distribution is empty. Otherwise
     * {@code false} is returned.
     * 
     * @return {@code true} if this probability distribution is empty.
     */
    public abstract boolean isEmpty();
    
    /**
     * Returns the number of elements in this probability distribution.
     * 
     * @return the size of this probability distribution.
     */
    public abstract int size();

    /**
     * Adds the element {@code element} to this probability distribution, and
     * assigns {@code weight} as its weight.
     * 
     * @param element the element to add.
     * @param weight  the weight of the new element.
     * 
     * @return {@code true} only if the input element did not reside in this
     *         structure and was successfully added.
     */
    public abstract boolean addElement(E element, double weight);

    /**
     * Returns a randomly chosen element from this probability distribution 
     * taking the weights into account.
     * 
     * @return a randomly chosen element.
     */
    public abstract E sampleElement();

    /**
     * Returns {@code true} if this probability distribution contains the
     * element {@code element}.
     * 
     * @param element the element to query.
     * @return {@code true} if the input element is in this probability 
     *         distribution; {@code false} otherwise.
     */
    public abstract boolean contains(E element);

    /**
     * Removes the element {@code element} from this probability distribution.
     * 
     * @param element the element to remove.
     * @return {@code true} if the element was present in this probability 
     *         distribution and was successfully removed.
     */
    public abstract boolean removeElement(E element);

    /**
     * Removes all elements from this probability distribution.
     */
    public abstract void clear();

    /**
     * Checks that the element weight is valid. The weight must not be a 
     * <tt>NaN</tt> and must be positive, but not a positive infinity.
     * 
     * @param weight the weight to validate.
     */
    protected void checkWeight(double weight) {
        if (Double.isNaN(weight)) {
            throw new IllegalArgumentException("The element weight is NaN.");
        }

        if (weight <= 0.0) {
            throw new IllegalArgumentException(
                    "The element weight must be positive. Received " + weight);
        }

        if (Double.isInfinite(weight)) {
            // Once here, 'weight' is positive infinity.
            throw new IllegalArgumentException(
                    "The element weight is infinite.");
        }
    }

    /**
     * Checks that this probability distribution contains at least one element.
     */
    protected void checkNotEmpty(int size) {
        if (size == 0) {
            throw new IllegalStateException(
                    "This probability distribution is empty.");
        }
    }
}
