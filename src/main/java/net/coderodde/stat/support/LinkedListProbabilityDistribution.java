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
 * <tr><td><tt>addElement   </tt> </td>  
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
        
        LinkedListNode(final E element, final double weight) {
            this.element = element;
            this.weight  = weight;
        }
        
        E getElement() {
            return this.element;
        }
        
        double getWeight() {
            return this.weight;
        }
        
        LinkedListNode<E> getPreviousLinkedListNode() {
            return this.prev;
        }
        
        LinkedListNode<E> getNextLinkedListNode() {
            return this.next;
        }
        
        void setPreviousLinkedListNode(final LinkedListNode<E> node) {
            this.prev = node;
        }
        
        void setNextLinkedListNode(final LinkedListNode<E> node) {
            this.next = node;
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
    public LinkedListProbabilityDistribution(final Random random) {
        super(random);
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public void addElement(final E element, final double weight) {
        checkWeight(weight);
        final LinkedListNode<E> newnode = new LinkedListNode<>(element, weight);
        
        if (linkedListHead == null) {
            linkedListHead = newnode;
            linkedListTail = newnode;
        } else {
            linkedListTail.setNextLinkedListNode(newnode);
            newnode.setPreviousLinkedListNode(linkedListTail);
            linkedListTail = newnode;
        }
        
        this.map.put(element, newnode);
        this.size++;
        this.totalWeight += weight;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public E sampleElement() {
        checkNotEmpty();
        double value = this.random.nextDouble() * this.totalWeight;
        
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

    @Override
    public boolean removeElement(E element) {
        final LinkedListNode<E> node = map.get(element);
        
        if (node == null) {
            return false;
        }
        
        this.map.remove(element);
        this.size--;
        this.totalWeight -= node.getWeight();
        unlink(node);
        return true;
    }
    
    @Override
    public void clear() {
        this.size = 0;
        this.totalWeight = 0.0;
        this.map.clear();
        this.linkedListHead = null;
        this.linkedListTail = null;
    }
    
    private void unlink(final LinkedListNode<E> node) {
        final LinkedListNode<E> left  = node.getPreviousLinkedListNode();
        final LinkedListNode<E> right = node.getNextLinkedListNode();
        
        if (left != null) {
            left.setNextLinkedListNode(node.getNextLinkedListNode());
        } else {
            this.linkedListHead = node.getNextLinkedListNode();
        }
        
        if (right != null) {
            right.setPreviousLinkedListNode(node.getPreviousLinkedListNode());
        } else {
            this.linkedListTail = node.getPreviousLinkedListNode();
        }
    }
}
