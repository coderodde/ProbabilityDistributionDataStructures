package net.coderodde.stat.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.coderodde.stat.AbstractProbabilityDistribution;

/**
 * This class implements a probability distribution relying on a linked list.
 * The running times of the main methods are as follows:
 * 
 * <table>
 * <tr><td>Method</td>  <td>Complexity</td></tr>
 * <tr><td><tt>addElement   </tt></td>  
 *     <td><tt>amortized constant time</tt>,</td></tr>
 * <tr><td><tt>sampleElement</tt> </td>  <td><tt>O(n)</tt>,</td></tr>
 * <tr><td><tt>removeElement</tt> </td>  <td><tt>O(1)</tt>.</td></tr>
 * </table>
 * 
 * This probability distribution class is best used whenever it is modified
 * frequently compared to the number of queries made.
 * 
 * @param <E> the actual type of the elements stored in this probability 
 *            distribution.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Jun 11, 2016)
 */
public class LinkedListProbabilityDistribution<E>
extends AbstractProbabilityDistribution<E> {

    private static final class LinkedListNode<E> {

        private final E element;
        private final double weight;
        private LinkedListNode<E> prev;
        private LinkedListNode<E> next;

        LinkedListNode(E element, double weight) {
            this.element = element;
            this.weight  = weight;
        }

        E getElement() {
            return element;
        }

        double getWeight() {
            return weight;
        }

        LinkedListNode<E> getPreviousLinkedListNode() {
            return prev;
        }

        LinkedListNode<E> getNextLinkedListNode() {
            return next;
        }

        void setPreviousLinkedListNode(LinkedListNode<E> node) {
            prev = node;
        }

        void setNextLinkedListNode(LinkedListNode<E> node) {
            next = node;
        }
    }

    /**
     * This map maps the elements to their respective linked list nodes.
     */
    private final Map<E, LinkedListNode<E>> map = new HashMap<>();

    /**
     * Stores the very first linked list node in this probability distribution.
     */
    private LinkedListNode<E> linkedListHead;

    /**
     * Stores the very last linked list node in this probability distribution.
     */
    private LinkedListNode<E> linkedListTail;

    /**
     * Construct a new probability distribution. 
     */
    public LinkedListProbabilityDistribution() {
        super();
    }

    /**
     * Constructs a new probability distribution using the input random number
     * generator.
     * 
     * @param random the random number generator to use.
     */
    public LinkedListProbabilityDistribution(Random random) {
        super(random);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean addElement(E element, double weight) {
        checkWeight(weight);

        if (map.containsKey(element)) {
            return false;
        }

        LinkedListNode<E> newnode = new LinkedListNode<>(element, weight);

        if (linkedListHead == null) {
            linkedListHead = newnode;
            linkedListTail = newnode;
        } else {
            linkedListTail.setNextLinkedListNode(newnode);
            newnode.setPreviousLinkedListNode(linkedListTail);
            linkedListTail = newnode;
        }

        map.put(element, newnode);
        size++;
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

        for (LinkedListNode<E> node = linkedListHead;
                node != null;
                node = node.getNextLinkedListNode()) {
            if (value < node.getWeight()) {
                return node.getElement();
            }

            value -= node.getWeight();
        }

        throw new IllegalStateException("Should not get here.");
    }

    /**
     * {@inheritDoc } 
     */
    @Override
    public boolean contains(E element) {
        return map.containsKey(element);
    }

    /**
     * {@inheritDoc } 
     */
    @Override
    public boolean removeElement(E element) {
        LinkedListNode<E> node = map.get(element);

        if (node == null) {
            return false;
        }

        map.remove(element);
        size--;
        totalWeight -= node.getWeight();
        unlink(node);
        return true;
    }

    /**
     * {@inheritDoc } 
     */
    @Override
    public void clear() {
        size = 0;
        totalWeight = 0.0;
        map.clear();
        linkedListHead = null;
        linkedListTail = null;
    }

    private void unlink(LinkedListNode<E> node) {
        LinkedListNode<E> left  = node.getPreviousLinkedListNode();
        LinkedListNode<E> right = node.getNextLinkedListNode();

        if (left != null) {
            left.setNextLinkedListNode(node.getNextLinkedListNode());
        } else {
            linkedListHead = node.getNextLinkedListNode();
        }

        if (right != null) {
            right.setPreviousLinkedListNode(node.getPreviousLinkedListNode());
        } else {
            linkedListTail = node.getPreviousLinkedListNode();
        }
    }
}
