package net.coderodde.stat.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.coderodde.stat.AbstractProbabilityDistribution;

/**
 * This class implements a hierarchical probability distribution. It uses a data
 * structure similar to a binary tree.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Jun 11, 2016)
 */
public class HierarchicalProbabilityDistribution<E>
extends AbstractProbabilityDistribution<E> {

    private static final class Node<E> {
        
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
        
        private boolean isRelayNode;
        
        /**
         * The left child node.
         */
        private Node<E> leftChild;
        
        /**
         * The right child node.
         */
        private Node<E> rightChild;
        
        /**
         * The parent node.
         */
        private Node<E> parent;
        
        /**
         * Caches the number of leaf nodes in the subtree starting from this
         * node.
         */
        private int numberOfLeafNodes;
        
        Node(final E element, final double weight) {
            this.element = element;
        }   
        
        Node() {
            this.element = null;
            this.isRelayNode = true;
        }
        
        E getElement() {
            return this.element;
        }
        
        double getWeight() {
            return this.weight;
        }
        
        void setWeight(final double weight) {
            this.weight = weight;
        }
        
        int getNumberOfLeaves() {
            return this.numberOfLeafNodes;
        }
        
        void setNumberOfLeaves(final int numberOfLeaves) {
            this.numberOfLeafNodes = numberOfLeaves;
        }
        
        Node<E> getLeftChild() {
            return this.leftChild;
        }
        
        void setLeftChild(final Node<E> block) {
            this.leftChild = block;
        }
        
        Node<E> getRightChild() {
            return this.rightChild;
        }
        
        void setRightChild(final Node<E> block) {
            this.rightChild = block;
        }
        
        Node<E> getParent() {
            return this.parent;
        }
        
        void setParent(final Node<E> block) {
            this.parent = block;
        }
        
        boolean isRelayNode() {
            return isRelayNode;
        }
        
        boolean isLeafNode() {
            return !isRelayNode;
        }
    }
    
    /**
     * Maps each element to the list of nodes representing the element.
     */
    private final Map<E, List<Node<E>>> map = new HashMap<>();
    
    /**
     * The root node of this distribution tree.
     */
    private final Node<E> root = new Node<>();
    
    /**
     * {@inheritDoc }
     */
    @Override
    public void addElement(E element, double weight) {
        checkWeight(weight);
        final Node<E> newnode = new Node<>(element, weight);
        List<Node<E>> nodeList = this.map.get(element);
        
        if (nodeList == null) {
            nodeList = new ArrayList<>();
            this.map.put(element, nodeList);
        } 
            
        nodeList.add(newnode);
        insert(newnode, root);
        this.size++;
        this.totalWeight += weight;
    }

    @Override
    public boolean removeElement(final E element) {
        final List<Node<E>> nodeList = this.map.get(element);
        
        if (nodeList == null) {
            return false;
        }
        
        final Node<E> node = removeLast(nodeList);
        
        this.size--;
        this.totalWeight -= node.getWeight();
        return true;
    }

    private void insert(final Node<E> node, final Node<E> root) {
        if (root.getLeftChild() == null) {
            root.setLeftChild(node);
            node.setParent(root);
            root.setNumberOfLeaves(root.getNumberOfLeaves() + 1);
            root.setWeight(root.getWeight() + node.getWeight());
            return;
        }
        
        if (root.getRightChild() == null) {
            root.setRightChild(node);
            node.setParent(root);
            root.setNumberOfLeaves(root.getNumberOfLeaves() + 1);
            root.setWeight(root.getWeight() + node.getWeight());
            return;
        }
        
        Node<E> currentNode = root;
        
        while (true) {
            if (currentNode.isLeafNode()) {
                break;
            }
            
            // Once here, 'currentNode' has both children. Choose the one that
            // contains the smaller number of nodes in order to keep the tree
            // balanced:
            if (currentNode.leftChild.getNumberOfLeaves() 
                    < currentNode.rightChild.getNumberOfLeaves()) {
                currentNode = currentNode.getLeftChild();
            } else {
                currentNode = currentNode.getRightChild();
            }
        }
        
        bypassLeafNode(currentNode, node);
    }
    
    /**
     * Assuming that {@code leafNodeToBypass} is a leaf node, this procedure 
     * attaches a relay node instead of it, and assigns {@code leafNodeToBypass}
     * and {@code newnode} as children of the new relay node.
     * 
     * @param leafNodeToBypass the leaf node to bypass.
     * @param newNode          the new node to add.
     */
    private void bypassLeafNode(final Node<E> leafNodeToBypass, 
                                final Node<E> newNode) {
        final Node<E> relayNode = new Node<>();
        final Node<E> parentOfCurrentNode = leafNodeToBypass.getParent();
        
        relayNode.setLeftChild(leafNodeToBypass);
        relayNode.setRightChild(newNode);
        
        leafNodeToBypass.setParent(relayNode);
        newNode.setParent(relayNode);
        relayNode.setParent(parentOfCurrentNode);
        
        if (parentOfCurrentNode.getLeftChild() == leafNodeToBypass) {
            parentOfCurrentNode.setLeftChild(relayNode);
        } else {
            parentOfCurrentNode.setRightChild(relayNode);
        }
    }
    
    /**
     * This method is responsible for updating the metadata of this data 
     * structure.
     * 
     * @param node      the node from which to start the metadata update. The 
     *                  updating routine updates also the metadata of all the 
     *                  predecessors of this node in the tree.
     * @param weight    the weight delta to add to each predecessor node.
     * @param nodeDelta the node count delta to add to each predecessor node.
     */
    private void updateMetadata(Node<E> node, 
                                final double weight, 
                                final int nodeDelta) {
        while (node != null) {
            node.setNumberOfLeaves(node.getNumberOfLeaves() + 1);
            node.setWeight(node.getWeight() + weight);
            node = node.getParent();
        }
    }
    
    private Node<E> removeLast(final List<Node<E>> list) {
        return list.remove(list.size() - 1);
    }
    
    @Override
    public E sampleElement() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clear() {
        this.root.setLeftChild(null);
        this.root.setRightChild(null);
        this.size = 0;
        this.totalWeight = 0.0;
    }
}
