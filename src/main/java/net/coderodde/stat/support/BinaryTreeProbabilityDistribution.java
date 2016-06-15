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
 * @param <E> the actual type of the elements stored in this distribution.
 */
public class BinaryTreeProbabilityDistribution<E>
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
            this.weight  = weight;
            this.numberOfLeafNodes = 1;
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
    private Node<E> root;
    
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
        insert(newnode);
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
        delete(node);
        updateMetadata(node.getParent(), -node.getWeight(), -1);
        this.size--;
        this.totalWeight -= node.getWeight();
        return true;
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
        
        relayNode.setNumberOfLeaves(1);
        relayNode.setWeight(leafNodeToBypass.getWeight());
        relayNode.setLeftChild(leafNodeToBypass);
        relayNode.setRightChild(newNode);
        
        leafNodeToBypass.setParent(relayNode);
        newNode.setParent(relayNode);

        if (parentOfCurrentNode == null) {
            this.root = relayNode;
        } else if (parentOfCurrentNode.getLeftChild() == leafNodeToBypass) {
            relayNode.setParent(parentOfCurrentNode);
            parentOfCurrentNode.setLeftChild(relayNode);
        } else {
            relayNode.setParent(parentOfCurrentNode);
            parentOfCurrentNode.setRightChild(relayNode);
        }
        
        updateMetadata(relayNode, newNode.getWeight(), 1);
    }
    
    private void insert(final Node<E> node) {
        if (root == null) {
            root = node;
            return;
        }
        
        Node<E> currentNode = root;
        
        while (currentNode.isRelayNode()) {
            if (currentNode.getLeftChild().getNumberOfLeaves() < 
                    currentNode.getRightChild().getNumberOfLeaves()) {
                currentNode = currentNode.getLeftChild();
            } else {
                currentNode = currentNode.getRightChild();
            }
        }
        
        bypassLeafNode(currentNode, node);
        
//        if (root.getLeftChild() == null) {
//            root.setLeftChild(node);
//            node.setParent(root);
//            root.setNumberOfLeaves(root.getNumberOfLeaves() + 1);
//            root.setWeight(root.getWeight() + node.getWeight());
//            return;
//        }
//        
//        if (root.getRightChild() == null) {
//            root.setRightChild(node);
//            node.setParent(root);
//            root.setNumberOfLeaves(root.getNumberOfLeaves() + 1);
//            root.setWeight(root.getWeight() + node.getWeight());
//            return;
//        }
//        
//        Node<E> currentNode = root;
//        
//        while (true) {
//            if (currentNode.isLeafNode()) {
//                break;
//            }
//            
//            // Once here, 'currentNode' has both children. Choose the one that
//            // contains the smaller number of nodes in order to keep the tree
//            // balanced:
//            if (currentNode.leftChild.getNumberOfLeaves() 
//                    < currentNode.rightChild.getNumberOfLeaves()) {
//                currentNode = currentNode.getLeftChild();
//            } else {
//                currentNode = currentNode.getRightChild();
//            }
//        }
//        
//        bypassLeafNode(currentNode, node);
    }
    
    private void delete(final Node<E> leafToDelete) {
        final Node<E> relayNode = leafToDelete.getParent();
        
        if (relayNode == null) {
            this.root = null;
            return;
        } 
        
        final Node<E> parentOfRelayNode = relayNode.getParent();
        final Node<E> siblingLeaf = relayNode.getLeftChild() == leafToDelete ?
                                    relayNode.getRightChild() :
                                    relayNode.getLeftChild();
        
        if (parentOfRelayNode == null) {
            this.root = siblingLeaf;
            siblingLeaf.setParent(null);
            return;
        }
        
        if (parentOfRelayNode.getLeftChild() == relayNode) {
            parentOfRelayNode.setLeftChild(siblingLeaf);
        } else {
            parentOfRelayNode.setRightChild(siblingLeaf);
        }
        
        siblingLeaf.setParent(parentOfRelayNode);
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
                                final double weightDelta, 
                                final int nodeDelta) {
        while (node != null) {
            node.setNumberOfLeaves(node.getNumberOfLeaves() + nodeDelta);
            node.setWeight(node.getWeight() + weightDelta);
            node = node.getParent();
        }
    }
    
    private Node<E> removeLast(final List<Node<E>> list) {
        return list.remove(list.size() - 1);
    }
    
    @Override
    public E sampleElement() {
        checkNotEmpty();
        
        double value = this.totalWeight * this.random.nextDouble();
        Node<E> node = root;
        
        while (node.isRelayNode()) {
            if (value < node.getLeftChild().getWeight()) {
                node = node.getLeftChild();
            } else {
                node = node.getRightChild();
                value -= node.getLeftChild().getWeight();
            }
        }
        
        return node.getElement();
    }

    @Override
    public void clear() {
        this.root.setLeftChild(null);
        this.root.setRightChild(null);
        this.size = 0;
        this.totalWeight = 0.0;
    }
}
