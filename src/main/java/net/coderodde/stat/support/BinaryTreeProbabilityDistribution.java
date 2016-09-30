package net.coderodde.stat.support;

import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import net.coderodde.stat.AbstractProbabilityDistribution;

/**
 * This class implements a probability distribution relying on a binary tree
 * structure. It allows <tt>O(log n)</tt> worst case time for adding, removing
 * and sampling an element.
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

        /**
         * Tells whether this node is a relay node. If not, this node is a leaf
         * node.
         */
        private final boolean isRelayNode;

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

        /**
         * Constructs a leaf node holding the element {@code element}.
         * 
         * @param element the element to store.
         * @param weight  the weight of the element.
         */
        Node(E element, double weight) {
            this.element           = element;
            this.weight            = weight;
            this.numberOfLeafNodes = 1;
            this.isRelayNode       = false;
        }   

        /**
         * Constructs a relay node.
         * 
         * @param weight the sum of the weights of all the leaf nodes reachable
         *               downwards from this node.
         */
        Node(double weight) {
            this.element           = null;
            this.weight            = weight;
            this.numberOfLeafNodes = 1;
            this.isRelayNode       = true;
        }

        /**
         * Returns a string representation of this node.
         * 
         * @return a string representation.
         */
        @Override
        public String toString() {
            if (isRelayNode) {
                return "[" + String.format("%.3f", getWeight()) + " : "
                           + numberOfLeafNodes + "]";
            }

            return "(" + String.format("%.3f", getWeight()) + " : " 
                       + element + ")";
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

        int getNumberOfLeaves() {
            return numberOfLeafNodes;
        }

        void setNumberOfLeaves(int numberOfLeaves) {
            this.numberOfLeafNodes = numberOfLeaves;
        }

        Node<E> getLeftChild() {
            return leftChild;
        }

        void setLeftChild(Node<E> block) {
            this.leftChild = block;
        }

        Node<E> getRightChild() {
            return rightChild;
        }

        void setRightChild(Node<E> block) {
            this.rightChild = block;
        }

        Node<E> getParent() {
            return parent;
        }

        void setParent(Node<E> block) {
            this.parent = block;
        }

        boolean isRelayNode() {
            return isRelayNode;
        }
    }

    /**
     * Maps each element to the list of nodes representing the element.
     */
    private final Map<E, Node<E>> map = new HashMap<>();

    /**
     * The root node of this distribution tree.
     */
    private Node<E> root;
    
    /**
     * Caches the number of elements stored in this probability distribution.
     */
    private int size;

    /**
     * Constructs this probability distribution using a default random number
     * generator.
     */
    public BinaryTreeProbabilityDistribution() {
        this(new Random());
    }

    /**
     * Constructs this probability distribution using the input random number
     * generator.
     * 
     * @param random the random number generator to use. 
     */
    public BinaryTreeProbabilityDistribution(Random random) {
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

        Node<E> newnode = new Node<>(element, weight);
        insert(newnode);
        size++;
        totalWeight += weight;
        map.put(element, newnode);
        return true;
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
    public E sampleElement() {
        checkNotEmpty(size);

        double value = totalWeight * random.nextDouble();
        Node<E> node = root;

        while (node.isRelayNode()) {
            if (value < node.getLeftChild().getWeight()) {
                node = node.getLeftChild();
            } else {
                value -= node.getLeftChild().getWeight();
                node = node.getRightChild();
            }
        }

        return node.getElement();
    }

    /**
     * {@inheritDoc } 
     */
    @Override
    public boolean removeElement(E element) {
        Node<E> node = map.get(element);

        if (node == null) {
            return false;
        }

        delete(node);
        size--;
        totalWeight -= node.getWeight();
        return true;
    }

    /**
     * {@inheritDoc } 
     */
    @Override
    public void clear() {
        root = null;
        size = 0;
        totalWeight = 0.0;
    }
     
    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Assuming that {@code leafNodeToBypass} is a leaf node, this procedure 
     * attaches a relay node instead of it, and assigns {@code leafNodeToBypass}
     * and {@code newnode} as children of the new relay node.
     * 
     * @param leafNodeToBypass the leaf node to bypass.
     * @param newNode          the new node to add.
     */
    private void bypassLeafNode(Node<E> leafNodeToBypass, 
                                Node<E> newNode) {
        Node<E> relayNode = new Node<>(leafNodeToBypass.getWeight());
        Node<E> parentOfCurrentNode = leafNodeToBypass.getParent();

        relayNode.setLeftChild(leafNodeToBypass);
        relayNode.setRightChild(newNode);

        leafNodeToBypass.setParent(relayNode);
        newNode.setParent(relayNode);

        if (parentOfCurrentNode == null) {
            root = relayNode;
        } else if (parentOfCurrentNode.getLeftChild() == leafNodeToBypass) {
            relayNode.setParent(parentOfCurrentNode);
            parentOfCurrentNode.setLeftChild(relayNode);
        } else {
            relayNode.setParent(parentOfCurrentNode);
            parentOfCurrentNode.setRightChild(relayNode);
        }

        updateMetadata(relayNode, newNode.getWeight(), 1);
    }

    private void insert(Node<E> node) {
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
    }

    private void delete(Node<E> leafToDelete) {
        Node<E> relayNode = leafToDelete.getParent();

        if (relayNode == null) {
            root = null;
            return;
        } 

        Node<E> parentOfRelayNode = relayNode.getParent();
        Node<E> siblingLeaf = relayNode.getLeftChild() == leafToDelete ?
                                    relayNode.getRightChild() :
                                    relayNode.getLeftChild();

        if (parentOfRelayNode == null) {
            root = siblingLeaf;
            siblingLeaf.setParent(null);
            return;
        }

        if (parentOfRelayNode.getLeftChild() == relayNode) {
            parentOfRelayNode.setLeftChild(siblingLeaf);
        } else {
            parentOfRelayNode.setRightChild(siblingLeaf);
        }

        siblingLeaf.setParent(parentOfRelayNode);
        updateMetadata(leafToDelete.getParent(), -leafToDelete.getWeight(), -1);
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
                                double weightDelta, 
                                int nodeDelta) {
        while (node != null) {
            node.setNumberOfLeaves(node.getNumberOfLeaves() + nodeDelta);
            node.setWeight(node.getWeight() + weightDelta);
            node = node.getParent();
        }
    }

    public String debugToString() {
        if (root == null) {
            return "empty";
        }

        StringBuilder sb = new StringBuilder();
        int treeHeight = getTreeHeight(root);
        Deque<Node<E>> queue = new LinkedList<>();
        queue.addLast(root);

        for (int i = 0; i < treeHeight + 1; ++i) {
            int currentQueueLength = queue.size();

            for (int j = 0; j < currentQueueLength; ++j) {
                Node<E> node = queue.removeFirst();
                addChildren(node, queue);
                sb.append(node == null ? "null" : node.toString()).append(" ");
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    private void addChildren(Node<E> node, Deque<Node<E>> queue) {
        if (node == null) {
            queue.addLast(null);
            queue.addLast(null);
            return;
        }

        queue.addLast(node.getLeftChild());
        queue.addLast(node.getRightChild());
    }

    private int getTreeHeight(Node<E> node) {
        if (node == null) {
            return -1;
        }

        return 1 + Math.max(getTreeHeight(node.getLeftChild()),
                            getTreeHeight(node.getRightChild()));
    }
    
    public static void main(String[] args) {
         BinaryTreeProbabilityDistribution<Integer> pd = 
                new BinaryTreeProbabilityDistribution<>();

        pd.addElement(0, 1.0);
        pd.addElement(1, 1.0);
        pd.addElement(2, 1.0);
        pd.addElement(3, 3.0);

        int[] counts = new int[4];

        for (int i = 0; i < 100; ++i) {
            Integer myint = pd.sampleElement();
            counts[myint]++;
            System.out.println(myint);
        }

        System.out.println(Arrays.toString(counts));
    }
}
