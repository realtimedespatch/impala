package org.impalaframework.radixtree;

/*
The MIT License

Copyright (c) 2008 Tahseen Ur Rehman

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation for Radix tree {@link RadixTree}
 * 
 * Taken from the original source from the radixtree project (http://radixtree.googlecode.com/).
 * Licence has been left unchanged as the MIT licence for this source file.
 * @author Tahseen Ur Rehman 
 * email: tahseen.ur.rehman {at.spam.me.not} gmail.com 
 */
public class RadixTreeImpl<T> implements RadixTree<T> {
    
    private final RadixTreeNode<T> root;

    private final AtomicLong size;

    /**
     * Create a Radix Tree with only the default node root.
     */
    public RadixTreeImpl() {
        root = new RadixTreeNode<T>();
        root.setKey("");
        size = new AtomicLong(0);
    }
    
    /*
     * (non-Javadoc)
     * @see uk.co.rtd.radixtree.RadixTree#find(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public T find(String key) {
        Visitor<T> visitor = new Visitor<T>() {
            T result = null;

            public void visit(String key, RadixTreeNode<T> parent,
                    RadixTreeNode<T> node) {
                if (node.isReal())
                    result = node.getValue();
            }

            public Object getResult() {
                return result;
            }
        };

        visit(key, visitor);

        return (T) visitor.getResult();
    }
    
    /*
     * (non-Javadoc)
     * @see uk.co.rtd.radixtree.RadixTree#findContainedValue(java.lang.String)
     */
    public T findContainedValue(final String searchKey) {
        
        TreeNode<T> node = findContainedNode(searchKey);
        return node != null ? node.getValue() : null;
    }    
    
    /*
     * (non-Javadoc)
     * @see uk.co.rtd.radixtree.RadixTree#findContainedNode(java.lang.String)
     */
    public TreeNode<T> findContainedNode(final String searchKey) {
        
        NodeAwareVisitor<T> visitor = new NodeAwareVisitor<T>() {
            RadixTreeNode<T> result = null;

            public void visit(
                    String key, 
                    RadixTreeNode<T> parent,
                    RadixTreeNode<T> node) {
                result = node;
            }

            public Object getResult() {
                return result;
            }
            
            private Stack<RadixTreeNode<T>> node = new Stack<RadixTreeNode<T>>();

            public void setCurrentRealNode(RadixTreeNode<T> node) {
                this.node.push(node);
            }

            public RadixTreeNode<T> getCurrentRealNode() {
                return node.isEmpty() ? null : node.peek();
            }
        };

        visit(searchKey, visitor);
        
        return (RadixTreeNode<T>) visitor.getCurrentRealNode();
    }


    /*
     * (non-Javadoc)
     * @see uk.co.rtd.radixtree.RadixTree#delete(java.lang.String)
     */
    public boolean delete(String key) {
        Visitor<T> visitor = new Visitor<T>() {
            boolean delete = false;

            public void visit(String key, RadixTreeNode<T> parent,
                    RadixTreeNode<T> node) {
                delete = node.isReal();

                // if it is a real node
                if (delete) {
                    // If there no children of the node we need to
                    // delete it from the its parent children list
                    if (node.getChildren().size() == 0) {
                        Iterator<RadixTreeNode<T>> it = parent.getChildren()
                                .iterator();
                        while (it.hasNext()) {
                            if (it.next().getKey().equals(node.getKey())) {
                                it.remove();
                                break;
                            }
                        }

                        // if parent is not real node and has only one child
                        // then they need to be merged.
                        if (parent.getChildren().size() == 1
                                && parent.isReal() == false) {
                            mergeNodes(parent, parent.getChildren().get(0));
                        }
                    } else if (node.getChildren().size() == 1) {
                        // we need to merge the only child of this node with
                        // itself
                        mergeNodes(node, node.getChildren().get(0));
                    } else { // we jus need to mark the node as non real.
                        node.setReal(false);
                    }
                }
            }

            /**
             * Merge a child into its parent node. Opertaion only valid if it is
             * only child of the parent node and parent node is not a real node.
             * 
             * @param parent
             *            The parent Node
             * @param child
             *            The child Node
             */
            private void mergeNodes(RadixTreeNode<T> parent,
                    RadixTreeNode<T> child) {
                parent.setKey(parent.getKey() + child.getKey());
                parent.setReal(child.isReal());
                parent.setValue(child.getValue());
                parent.setChildren(child.getChildren());
            }

            public Object getResult() {
                return Boolean.valueOf(delete);
            }
        };

        visit(key, visitor);

        if(((Boolean) visitor.getResult()).booleanValue()) {
            size.getAndDecrement();   
        }
        
        return ((Boolean) visitor.getResult()).booleanValue();
    }

    /*
     * (non-Javadoc)
     * @see uk.co.rtd.radixtree.RadixTree#insert(java.lang.String, java.lang.Object)
     */
    public void insert(String key, T value) throws DuplicateKeyException {
        try {
            insert(key, root, value);
        } catch (DuplicateKeyException e) {
            // re-throw the exception with 'key' in the message
            throw new DuplicateKeyException("Duplicate key: '" + key + "'");
        }
        size.getAndIncrement();
    }

    /**
     * Recursively insert the key in the radix tree.
     * 
     * @param key The key to be inserted
     * @param node The current node
     * @param value The value associated with the key 
     * @throws DuplicateKeyException If the key already exists in the database.
     */
    private void insert(String key, RadixTreeNode<T> node, T value)
            throws DuplicateKeyException {
        int i = 0;
        int keylen = key.length();
        int nodelen = node.getKey().length();

        while (i < keylen && i < nodelen) {
            if (key.charAt(i) != node.getKey().charAt(i)) {
                break;
            }
            i++;
        }

        // we are either at the root node
        // or we need to go down the tree
        if (node.getKey().equals("") == true || i == 0 || (i < keylen && i >= nodelen)) {
            boolean flag = false;
            String newText = key.substring(i, keylen);
            for (RadixTreeNode<T> child : node.getChildren()) {
                if (child.getKey().startsWith(newText.charAt(0) + "")) {
                    flag = true;
                    insert(newText, child, value);
                    break;
                }
            }

            // just add the node as the child of the current node
            if (flag == false) {
                RadixTreeNode<T> n = new RadixTreeNode<T>();
                n.setKey(newText);
                n.setReal(true);
                n.setValue(value);

                node.getChildren().add(n);
            }
        }
        // there is a exact match just make the current node as data node
        else if (i == keylen && i == nodelen) {
            if (node.isReal() == true) {
                throw new DuplicateKeyException("Duplicate key");
            }

            node.setReal(true);
            node.setValue(value);
        }
        // This node need to be splitted as the key to be inserted
        // is a prefix of the current node key
        else if (i > 0 && i < nodelen) {
            RadixTreeNode<T> n1 = new RadixTreeNode<T>();
            n1.setKey(node.getKey().substring(i, nodelen));
            n1.setReal(node.isReal());
            n1.setValue(node.getValue());
            n1.setChildren(node.getChildren());

            node.setKey(key.substring(0, i));
            node.setReal(false);
            node.setChildren(new ArrayList<RadixTreeNode<T>>());
            node.getChildren().add(n1);
            
            if(i < keylen) {
                RadixTreeNode<T> n2 = new RadixTreeNode<T>();
                n2.setKey(key.substring(i, keylen));
                n2.setReal(true);
                n2.setValue(value);
                
                node.getChildren().add(n2);
            } else {
                node.setValue(value);
                node.setReal(true);
            }
        }        
        // this key need to be added as the child of the current node
        else {
            RadixTreeNode<T> n = new RadixTreeNode<T>();
            n.setKey(node.getKey().substring(i, nodelen));
            n.setChildren(node.getChildren());
            n.setReal(node.isReal());
            n.setValue(node.getValue());

            node.setKey(key);
            node.setReal(true);
            node.setValue(value);

            node.getChildren().add(n);
        }
    }

    /*
     * (non-Javadoc)
     * @see uk.co.rtd.radixtree.RadixTree#searchPrefix(java.lang.String, int)
     */
    public ArrayList<T> searchPrefix(String key, int recordLimit) {
        ArrayList<T> keys = new ArrayList<T>();

        RadixTreeNode<T> node = searchPefix(key, root);

        if (node != null) {
            if (node.isReal()) {
                keys.add(node.getValue());
            }
            getNodes(node, keys, recordLimit);
        }

        return keys;
    }

    private void getNodes(RadixTreeNode<T> parent, ArrayList<T> keys, int limit) {
        Queue<RadixTreeNode<T>> queue = new LinkedList<RadixTreeNode<T>>();

        queue.addAll(parent.getChildren());

        while (!queue.isEmpty()) {
            RadixTreeNode<T> node = queue.remove();
            if (node.isReal() == true) {
                keys.add(node.getValue());
            }

            if (keys.size() == limit) {
                break;
            }

            queue.addAll(node.getChildren());
        }
    }

    private RadixTreeNode<T> searchPefix(String key, RadixTreeNode<T> node) {
        RadixTreeNode<T> result = null;
        int i = 0;
        int keylen = key.length();
        int nodelen = node.getKey().length();

        while (i < keylen && i < nodelen) {
            if (key.charAt(i) != node.getKey().charAt(i)) {
                break;
            }
            i++;
        }

        if (i == keylen && i <= nodelen) {
            result = node;
        } else if (node.getKey().equals("") == true
                || (i < keylen && i >= nodelen)) {
            String newText = key.substring(i, keylen);
            for (RadixTreeNode<T> child : node.getChildren()) {
                if (child.getKey().startsWith(newText.charAt(0) + "")) {
                    result = searchPefix(newText, child);
                    break;
                }
            }
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * @see uk.co.rtd.radixtree.RadixTree#contains(java.lang.String)
     */
    public boolean contains(String key) {
        Visitor<T> visitor = new Visitor<T>() {
            boolean result = false;

            public void visit(String key, RadixTreeNode<T> parent,
                    RadixTreeNode<T> node) {
                result = node.isReal();
            }

            public Object getResult() {
                return Boolean.valueOf(result);
            }
        };

        visit(key, visitor);

        return ((Boolean) visitor.getResult()).booleanValue();
    }
    
    /**
     * visit the node those key matches the given key
     * @param key The key that need to be visited
     * @param visitor The visitor object
     */
    public void visit(String key, Visitor<T> visitor) {
        if (root != null)
            visit(key, visitor, null, root);
    }

    /**
     * recursively visit the tree based on the supplied "key". calls the Visitor
     * for the node those key matches the given prefix
     * 
     * @param prefix
     *            The key o prefix to search in the tree
     * @param visitor
     *            The Visitor that will be called if a node with "key" as its
     *            key is found
     * @param node
     *            The Node from where onward to search
     */
    void visit(String prefix, 
            Visitor<T> visitor,
            RadixTreeNode<T> parent, 
            RadixTreeNode<T> node) {
        
        int i = 0;
        int keylen = prefix.length();
        int nodelen = node.getKey().length();

        // match the prefix with node key
        while (i < keylen && i < nodelen) {
            
            final String key = node.getKey();
            if (prefix.charAt(i) != key.charAt(i)) {
                break;
            }
            i++;
        }

        // if the node key and prefix match, we found a match!
        if (i == keylen && i == nodelen) {
            visitor.visit(prefix, parent, node);
            
        }
        else {
            final String key = node.getKey();
            
            if (key.equals("") == true // either we are at the
                    // root
                    || (i < keylen && i >= nodelen)) {
                
                // OR we need to
                // traverse the children
                String newText = prefix.substring(i, keylen);
                
                final List<RadixTreeNode<T>> children = node.getChildren();
                for (RadixTreeNode<T> child : children) {
                    // recursively search the child nodes
                    final String childKey = child.getKey();
                    if (childKey.startsWith(newText.charAt(0) + "")) {
                        
                        if (visitor instanceof NodeAwareVisitor<?>) {
                        if (child.isReal()) {
                            if (newText.startsWith(childKey)) {
                                ((NodeAwareVisitor<T>) visitor).setCurrentRealNode(child);
                            }
                        }
                        }
                        
                        visit(newText, visitor, node, child);
                        break;
                    }
                }
            }
        }
    }
    

    /**
     * Display the Trie on console. WARNING! Do not use for large Trie. For
     * testing purpose only.
     */
    @Deprecated
    public void display() {
        display(0, root);
    }

    @Deprecated
    private void display(int level, RadixTreeNode<T> node) {
        for (int i = 0; i < level; i++) {
            System.out.print(" ");
        }
        System.out.print("|");
        for (int i = 0; i < level; i++) {
            System.out.print("-");
        }

        if (node.isReal() == true)
            System.out.println(node.getKey() + "[" + node.getValue() + "]*");
        else
            System.out.println(node.getKey());

        for (RadixTreeNode<T> child : node.getChildren()) {
            display(level + 1, child);
        }
    }

    /*
     * (non-Javadoc)
     * @see uk.co.rtd.radixtree.RadixTree#getSize()
     */
    public long getSize() {
        return size.longValue();
    }
}
