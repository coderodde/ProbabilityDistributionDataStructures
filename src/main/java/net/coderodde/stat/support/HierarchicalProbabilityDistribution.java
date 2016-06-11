package net.coderodde.stat.support;

import net.coderodde.stat.AbstractProbabilityDistribution;

/**
 * This class implements a hierarchical probability distribution. It uses a 
 * data structure similar to skip list: we have a set of linked list of blocks
 * describing the weight ranges.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Jun 11, 2016)
 */
public class HierarchicalProbabilityDistribution<E>
extends AbstractProbabilityDistribution<E> {

    private static final class Block<E> {
        
        /**
         * Holds the element if this block is a leaf. Internal blocks have 
         * {@code null} assigned to this field.
         */
        private final E element;
        
        /**
         * If this block is a leaf, specifies the weight of the {@code element}.
         * Otherwise, this field caches the sum of all weights over all
         * descendant leaves.
         */
        private double weight;
        
        /**
         * Caches the height of this block. The height of a leaf block is 0.
         */
        private int height;
        
        /**
         * Immediate left child block.
         */
        private Block<E> leftChild;
        
        /**
         * Immediate right child block.
         */
        private Block<E> rightChild;
        
        /**
         * Immediate parent block.
         */
        private Block<E> parent;
        
        /**
         * The total number of descendants that this block has.
         */
        private int numberOfDescendants;
        
        /**
         * The maximum possible number of descendants that this block can have.
         */
        private int capacity;
        
        Block(final E element) {
            this.element = element;
        }   
        
        double getWeight() {
            return this.weight;
        }
        
        void setWeight(final double weight) {
            this.weight = weight;
        }
        
        int getHeight() {
            return this.height;
        }
        
        void setHeight(final int height) {
            this.height = height;
        }
        
        int getNumberOfMissingBlock() {
            return 0;
        }
        
        void setLeftChild(final Block<E> block) {
            this.leftChild = block;
        }
        
        void setRightChild(final Block<E> block) {
            this.rightChild = block;
        }
        
        void setParent(final Block<E> block) {
            this.parent = block;
        }
        
        boolean isFull() {
            return false;
        }
    }
    
    private Block<E> leftRoot;
    private Block<E> rightRoot;
    
    @Override
    public void addElement(E element, double weight) {
        checkWeight(weight);
        
        if (leftRoot == null) {
            leftRoot = new Block<>(element);
            leftRoot.setWeight(weight);
            this.size++;
            this.totalWeight += weight;
            return;
        }
        
        if (rightRoot == null) {
            rightRoot = new Block<>(element);
            rightRoot.setWeight(weight);
            this.size++;
            this.totalWeight += weight;
            return;
        }
        
        if (leftRoot.isFull() && rightRoot.isFull()) {
            final Block<E> newroot = new Block<>(null);
            newroot.setWeight(leftRoot.getWeight() + rightRoot.getWeight());
            newroot.setHeight(leftRoot.getHeight() + 1);
            newroot.setLeftChild(leftRoot);
            newroot.setRightChild(rightRoot);
            leftRoot.setParent(newroot);
            rightRoot.setParent(newroot);
            leftRoot = newroot;
            this.size++;
            this.totalWeight += weight;
            return;
        }
    }

    @Override
    public E sampleElement() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean removeElement(E element) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
