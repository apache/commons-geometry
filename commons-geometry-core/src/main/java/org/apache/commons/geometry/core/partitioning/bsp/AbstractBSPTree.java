/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.core.partitioning.bsp;

import java.io.Serializable;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;
import org.apache.commons.geometry.core.partitioning.bsp.BSPTreeVisitor.Order;

/** Abstract class for Binary Space Partitioning (BSP) tree implementations.
 * @param <P> Point implementation type
 * @param <T> Node implementation type
 */
public abstract class AbstractBSPTree<P extends Point<P>, N extends AbstractBSPTree.AbstractNode<P, N>>
    implements BSPTree<P, N>, Serializable {

    /** Serializable UID */
    private static final long serialVersionUID = 20190330L;

    /** The default number of levels to print when creating a string representation of the tree */
    private static final int DEFAULT_TREE_STRING_MAX_DEPTH = 8;

    /** Integer value set on various node fields when a value is unknown. */
    private static final int UNKNOWN_VALUE = -1;

    /** The root node for the tree. */
    private N root;

    /** The current modification version for the tree structure. This is incremented each time
     * a structural change occurs in the tree and is used to determine when cached values
     * must be recomputed.
     */
    private int version = 0;

    /** {@inheritDoc} */
    @Override
    public N getRoot() {
        if (root == null) {
            setRoot(createNode());
        }
        return root;
    }

    /** Set the root node for the tree. Cached tree properties are invalidated
     * with {@link #invalidate()}.
     */
    protected void setRoot(final N root) {
        this.root = root;

        this.root.makeRoot();

        invalidate();
    }

    /** {@inheritDoc} */
    @Override
    public int count() {
        return getRoot().count();
    }

    /** {@inheritDoc} */
    @Override
    public int height() {
        return getRoot().height();
    }

    /** {@inheritDoc} */
    @Override
    public void accept(final BSPTreeVisitor<P, N> visitor) {
        acceptVisitor(getRoot(), visitor);
    }

    /** {@inheritDoc} */
    @Override
    public N findNode(final P pt, final NodeCutRule cutBehavior) {
        return findNode(getRoot(), pt, cutBehavior);
    }

    /** {@inheritDoc} */
    @Override
    public void insert(final SubHyperplane<P> sub) {
        insert(sub.toConvex());
    }

    /** {@inheritDoc} */
    @Override
    public void insert(final ConvexSubHyperplane<P> convexSub) {
        insertRecursive(getRoot(), convexSub,
                convexSub.getHyperplane().span());
    }

    /** {@inheritDoc} */
    @Override
    public void insert(final Iterable<? extends ConvexSubHyperplane<P>> convexSubs) {
        for (ConvexSubHyperplane<P> convexSub : convexSubs) {
            insert(convexSub);
        }
    }

    /** Return an iterator over the nodes in the tree */
    @Override
    public Iterator<N> iterator() {
        return new NodeIterator<>(getRoot());
    }

    /** {@inheritDoc} */
    @Override
    public void copy(final BSPTree<P, N> src) {
        copySubtree(src.getRoot(), getRoot());
    }

    /** {@inheritDoc} */
    @Override
    public void extract(final N node) {
        // copy downward
        final N extracted = importSubtree(node);

        // extract upward
        final N newRoot = extractParentPath(node, extracted);

        // set the root of this tree
        setRoot(newRoot);
    }

    /** {@inheritDoc} */
    @Override
    public void transform(final Transform<P> transform) {
        final boolean swapChildren = swapsInsideOutside(transform);
        transformRecursive(getRoot(), transform, swapChildren);

        invalidate();
    }

    /** Get a simple string representation of the tree structure. The returned string contains
     * the tree structure down to the default max depth of {@value #DEFAULT_TREE_STRING_MAX_DEPTH}.
     * @return a string representation of the tree
     */
    public String treeString() {
        return treeString(DEFAULT_TREE_STRING_MAX_DEPTH);
    }

    /** Get a simple string representation of the tree structure. The returned string contains
     * the tree structure down to {@code maxDepth}.
     * @return a string representation of the tree
     */
    public String treeString(final int maxDepth) {
        BSPTreePrinter<P, N> printer = new BSPTreePrinter<>(maxDepth);
        accept(printer);

        return printer.toString();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return new StringBuilder()
                .append(getClass().getSimpleName())
                .append("[count= ")
                .append(count())
                .append(", height= ")
                .append(height())
                .append("]")
                .toString();
    }

    /** Create a new node for this tree
     * @return a new node for this tree
     */
    protected abstract N createNode();

    /** Copy non-structural node properties from {@code src} to {@code dst}.
     * Non-structural properties are those properties not directly related
     * to the structure of the BSP tree, i.e. properties other than parent/child
     * connections and cut subhyperplanes. Subclasses should override this method
     * when additional properties are stored on nodes.
     * @param src source node
     * @param dst destination node
     */
    protected void copyNodeProperties(final N src, final N dst) {
    }

    /** Method called to initialize a new child node. Subclasses can use this method to
     * set initial attributes on the node.
     * @param parent the parent node
     * @param child the new child node
     * @param isPlus true if the child will be assigned as the parent's plus child;
     *      false if it will be the parent's minus child
     */
    protected void initChildNode(final N parent, final N child, final boolean isPlus) {
    }

    /** Create a non-structural copy of the given node. Properties such as parent/child
     * connections and cut subhyperplanes are <em>not</em> copied.
     * @param src the node to copy; does not need to belong to the current tree
     * @return the copied node
     * @see AbstractBSPTree#copyNodeProperties(AbstractNode, AbstractNode)
     */
    protected N copyNode(final N src) {
        final N copy = createNode();
        copyNodeProperties(src, copy);

        return copy;
    }

    /** Recursively copy a subtree. The returned node is not attached to the current tree.
     * Structural <em>and</em> non-structural properties are copied from the source subtree
     * to the destination subtree. This method does nothing if {@code src} and {@code dst}
     * reference the same node.
     * @param src the node representing the source subtree; does not need to belong to the
     *      current tree
     * @param dst the node representing the destination subtree
     * @return the copied node, ie {@code dst}
     */
    protected N copySubtree(final N src, final N dst) {
        // only copy if we're actually switching nodes
        if (src != dst) {
            // copy non-structural properties
            copyNodeProperties(src, dst);

            // copy the subtree structure
            ConvexSubHyperplane<P> cut = null;
            N minus = null;
            N plus = null;

            if (!src.isLeaf()) {
                final AbstractBSPTree<P, N> dstTree = dst.getTree();

                cut = src.getCut();
                minus = copySubtree(src.getMinus(), dstTree.createNode());
                plus = copySubtree(src.getPlus(), dstTree.createNode());
            }

            dst.setSubtree(cut, minus, plus);
        }

        return dst;
    }

    /** Import the subtree represented by the given node into this tree. If the given node
     * already belongs to this tree, then the node is returned directly without modification.
     * If the node does <em>not</em> belong to this tree, a new node is created and the src node
     * subtree is copied into it.
     *
     * <p>This method does not modify the current structure of the tree.</p>
     * @param src node to import
     * @return the given node if it belongs to this tree, otherwise a new node containing
     *      a copy of the given node's subtree
     * @see #copySubtree(AbstractNode, AbstractNode)
     */
    protected N importSubtree(final N src) {
        // create a copy of the node if it's not already in this tree
        if (src.getTree() != this) {
            return copySubtree(src, createNode());
        }

        return src;
    }

    /** Extract the path from {@code src} to the root of its tree and
     * set it as the parent path of {@code dst}. Leaf nodes created during
     * the extraction are given the same node properties as their counterparts
     * in the source tree but without the cuts and child nodes. The properties
     * of {@code dst} are not modified, with the exception of its parent node
     * reference.
     * @param src the source node to copy the parent path from
     * @param dst the destination node to place under the extracted path
     * @return the root node of the extracted path
     */
    protected N extractParentPath(final N src, final N dst) {
        N dstParent = dst;
        N dstChild;

        N srcChild = src;
        N srcParent = srcChild.getParent();

        while (srcParent != null) {
            dstChild = dstParent;
            dstParent = copyNode(srcParent);

            if (srcChild.isMinus()) {
                dstParent.setSubtree(
                        srcParent.getCut(),
                        dstChild,
                        copyNode(srcParent.getPlus()));
            }
            else {
                dstParent.setSubtree(
                        srcParent.getCut(),
                        copyNode(srcParent.getMinus()),
                        dstChild);
            }

            srcChild = srcParent;
            srcParent = srcChild.getParent();
        }

        return dstParent;
    }

    /** Find the smallest node in the tree containing the point, starting
     * at the given node.
     * @param start the node to begin the search with
     * @param pt the point to check
     * @param cutBehavior value determining the search behavior when the test point
     *      lies directly on the cut subhyperplane of an internal node
     * @return the smallest node in the tree containing the point
     */
    protected N findNode(final N start, final P pt, final NodeCutRule cutBehavior) {
        Hyperplane<P> cutHyper = start.getCutHyperplane();
        if (cutHyper != null) {
            HyperplaneLocation cutLoc = cutHyper.classify(pt);

            final boolean onPlusSide = cutLoc == HyperplaneLocation.PLUS;
            final boolean onMinusSide = cutLoc == HyperplaneLocation.MINUS;
            final boolean onCut = !onPlusSide && !onMinusSide;

            if (onMinusSide || (onCut && cutBehavior == NodeCutRule.MINUS)) {
                return findNode(start.getMinus(), pt, cutBehavior);
            }
            else if (onPlusSide || (onCut && cutBehavior == NodeCutRule.PLUS)) {
                return findNode(start.getPlus(), pt, cutBehavior);
            }
        }
        return start;
    }

    /** Visit the nodes in a subtree.
     * @param node the node to begin the visit process
     * @param visitor the visitor to pass nodes to
     */
    protected void acceptVisitor(final N node, BSPTreeVisitor<P, N> visitor) {
        if (node.isLeaf()){
            visitor.visit(node);
        }
        else {
            final Order order = visitor.visitOrder(node);

            if (order != null) {

                switch (order) {
                    case PLUS_MINUS_NODE:
                        acceptVisitor(node.getPlus(), visitor);
                        acceptVisitor(node.getMinus(), visitor);
                        visitor.visit(node);
                        break;
                    case PLUS_NODE_MINUS:
                        acceptVisitor(node.getPlus(), visitor);
                        visitor.visit(node);
                        acceptVisitor(node.getMinus(), visitor);
                        break;
                    case MINUS_PLUS_NODE:
                        acceptVisitor(node.getMinus(), visitor);
                        acceptVisitor(node.getPlus(), visitor);
                        visitor.visit(node);
                        break;
                    case MINUS_NODE_PLUS:
                        acceptVisitor(node.getMinus(), visitor);
                        visitor.visit(node);
                        acceptVisitor(node.getPlus(), visitor);
                        break;
                    case NODE_PLUS_MINUS:
                        visitor.visit(node);
                        acceptVisitor(node.getPlus(), visitor);
                        acceptVisitor(node.getMinus(), visitor);
                        break;
                    case NODE_MINUS_PLUS:
                        visitor.visit(node);
                        acceptVisitor(node.getMinus(), visitor);
                        acceptVisitor(node.getPlus(), visitor);
                        break;
                }
            }
        }
    }

    /** Cut a node with a hyperplane. The algorithm proceeds are follows:
     * <ol>
     *      <li>The hyperplane is trimmed by splitting it with each cut hyperplane on the
     *      path from the given node to the root of the tree.</li>
     *      <li>If the remaining portion of the hyperplane is <em>not</em> empty, then
     *          <ul>
     *              <li>the remaining portion becomes the cut subhyperplane for the node,</li>
     *              <li>two new child nodes are created and initialized with
     *              {@link #initChildNode(AbstractNode, AbstractNode, boolean)}, and</li>
     *              <li>true is returned.</li>
     *          </ul>
 *          </li>
     *      <li>If the remaining portion of the hyperplane <em>is</em> empty (ie, the
     *      cutting hyperplane does not intersect the node's region), then
     *          <ul>
     *              <li>the node is converted to a leaf node (meaning that previous
     *              child nodes are lost), and</li>
     *              <li>false is returned.</li>
     *          </ul>
     *      </li>
     * </ol>
     *
     * <p>It is important to note that since this method uses the path from given node
     * to the tree root, it must only be used on nodes that are already inserted into
     * the tree.</p>
     *
     * <p>This method always calls {@link #invalidate()} to invalidate cached tree properties.</p>
     *
     * @param node the node to cut
     * @param cutter the hyperplane to cut the node with
     * @return true if the node was cut and two new child nodes were created;
     *      otherwise false
     * @see #trimToNode(AbstractNode, ConvexSubHyperplane)
     * @see #cutNode(AbstractNode, ConvexSubHyperplane)
     * @see #invalidate()
     */
    protected boolean insertNodeCut(final N node, final Hyperplane<P> cutter) {
        // cut the hyperplane using all hyperplanes from this node up
        // to the root
        ConvexSubHyperplane<P> cut = trimToNode(node, cutter.span());
        if (cut == null || cut.isEmpty()) {
            // insertion failed; the node was not cut
            cutNode(node, null);
            return false;
        }

        cutNode(node, cut);
        return true;
    }

    /** Trim the given subhyperplane to the region defined by the given node. This method cuts the
     * subhyperplane with the cut hyperplanes (binary partitioners) of all parent nodes up to
     * the root and returns the trimmed subhyperplane or {@code null} if the subhyperplane lies
     * outside of the region defined by the node.
     *
     * <p>If the subhyperplane is directly coincident with a binary partitioner of a parent node,
     * then the relative orientations of the associated hyperplanes are used to determine the behavior,
     * as described below.
     * <ul>
     *      <li>If the orientations are <strong>similar</strong>, then the subhyperplane is determined to
     *      lie <em>outside</em> of the node's region and {@code null} is returned.</li>
     *      <li>If the orientations are <strong>different</strong> (ie, opposite), then the subhyperplane
     *      is determined to lie <em>inside</em> of the node's region and the fit operation continues
     *      with the remaining parent nodes.</li>
     * </ul>
     * These rules are designed to allow the creation of trees with node regions that are the thickness
     * of a single hyperplane. For example, in two dimensions, a tree could be constructed with an internal
     * node containing a cut along the x-axis in the positive direction and with a child node containing a
     * cut along the x-axis in the opposite direction. If the nodes in the tree are given inside and outside
     * attributes, then this tree could be used to represent a region consisting of a single line or a region
     * consisting of the entire space except for the single line. This would not be possible if nodes were not
     * able to have cut hyperplanes that were coincident with parent cuts but in opposite directions.
     * </p>
     *
     * <p>
     * Another way of looking at the rules above is that inserting a hyperplane into the tree that exactly
     * matches the hyperplane of a parent node does not add any information to the tree. However, adding a
     * hyperplane to the tree that is coincident with a parent node but with the opposite orientation,
     * <em>does</em> add information to the tree.
     * </p>
     *
     * @param node the node representing the region to fit the subhyperplane to
     * @param sub the subhyperplane to trim to the node's region
     * @return the trimmed subhyperplane or null if the given subhyperplane does not intersect
     *      the node's region
     */
    protected ConvexSubHyperplane<P> trimToNode(final N node, final ConvexSubHyperplane<P> sub) {

        ConvexSubHyperplane<P> result = sub;

        N parentNode = node.getParent();
        N currentNode = node;

        while (parentNode != null && result != null) {
            final Split<? extends ConvexSubHyperplane<P>> split = result.split(parentNode.getCutHyperplane());

            if (split.getLocation() == SplitLocation.NEITHER) {
                // if we're directly on the splitter and have the same orientation, then
                // we say the subhyperplane does not lie in the node's region (no new information
                // is added to the tree in this case)
                if (result.getHyperplane().similarOrientation(parentNode.getCutHyperplane())) {
                    result = null;
                }
            }
            else {
                result = currentNode.isPlus() ? split.getPlus() : split.getMinus();
            }

            currentNode = parentNode;
            parentNode = parentNode.getParent();
        }

        return result;
    }

    /** Remove the cut from the given node. Returns true if the node had a cut before
     * the call to this method. Any previous child nodes are lost.
     * @param node the node to remove the cut from
     * @return true if the node previously had a cut
     */
    protected boolean removeNodeCut(final N node) {
        boolean hadCut = node.getCut() != null;
        cutNode(node, null);

        return hadCut;
    }

    /** Set the cut subhyperplane for the given node. If {@code cut} is {@code null} then any
     * existing child nodes are removed. If {@code cut} is not {@code null}, two new child
     * nodes are created and initialized with
     * {@link AbstractBSPTree#initChildNode(AbstractNode, AbstractNode, boolean)}.
     *
     * <p>This method performs absolutely <em>no</em> validation on the given cut
     * subhyperplane. It is the responsibility of the caller to ensure that the
     * subhyperplane fits the region represented by the node.</p>
     *
     * <p>This method always calls {@link #invalidate()} to invalidate cached tree properties.</p>
     * @param node
     * @param cut
     */
    protected void cutNode(final N node, final ConvexSubHyperplane<P> cut) {
        N plus = null;;
        N minus = null;

        if (cut != null) {
            minus = createNode();
            initChildNode(node, minus, false);

            plus = createNode();
            initChildNode(node, plus, true);
        }

        node.setSubtree(cut, minus, plus);

        invalidate();
    }

    /** Return true if the given transform swaps the inside and outside of
     * the region.
     *
     * <p>The default behavior of this method is to return true if the transform
     * does not preserve spatial orientation (ie, {@link Transform#preservesOrientation()}
     * is false). Subclasses may need to override this method to implement the correct
     * behavior for their space and dimension.</p>
     * @param transform transform to check
     * @return true if the given transform swaps the interior and exterior of
     *      the region
     */
    protected boolean swapsInsideOutside(final Transform<P> transform) {
        return !transform.preservesOrientation();
    }

    /** Recursively insert a subhyperplane into the tree at the given node.
     * @param node the node to begin insertion with
     * @param insert the subhyperplane to insert
     * @param trimmed subhyperplane containing the result of splitting the entire
     *      space with each hyperplane from this node to the root
     */
    private void insertRecursive(final N node, final ConvexSubHyperplane<P> insert,
            final ConvexSubHyperplane<P> trimmed) {
        if (node.isLeaf()) {
            cutNode(node, trimmed);
        }
        else {
            Split<? extends ConvexSubHyperplane<P>> insertSplit = insert.split(node.getCutHyperplane());

            final ConvexSubHyperplane<P> minus = insertSplit.getMinus();
            final ConvexSubHyperplane<P> plus = insertSplit.getPlus();

            if (minus != null || plus != null) {
                final Split<? extends ConvexSubHyperplane<P>> trimmedSplit = trimmed.split(node.getCutHyperplane());

                if (minus != null) {
                    insertRecursive(node.getMinus(), minus, trimmedSplit.getMinus());
                }
                if (plus != null) {
                    insertRecursive(node.getPlus(), plus, trimmedSplit.getPlus());
                }
            }
        }
    }

    /** Transform the subtree rooted as {@code node} recursively.
     * @param node the root node of the subtree to transform
     * @param t the transform to apply
     * @param swapChildren if true, the plus and minus child nodes of each internal node
     *      will be swapped; this should be the case when the transform is a reflection
     */
    private void transformRecursive(final N node, final Transform<P> t, final boolean swapChildren) {
        if (node.isInternal()) {
            // transform our cut
            final ConvexSubHyperplane<P> transformedCut = node.getCut().transform(t);

            // transform our children
            transformRecursive(node.getMinus(), t, swapChildren);
            transformRecursive(node.getPlus(), t, swapChildren);

            final N transformedMinus = swapChildren ? node.getPlus() : node.getMinus();
            final N transformedPlus = swapChildren ? node.getMinus() : node.getPlus();

            // set our new state
            node.setSubtree(transformedCut, transformedMinus, transformedPlus);
        }
    }

    /** Split this tree with the given hyperplane, placing the split contents into the given
     * target trees. One of the given trees may be null, in which case that portion of the split
     * will not be exported. The current tree is not modified.
     * @param splitter splitting hyperplane
     * @param minus tree that will contain the portion of the tree on the minus side of the splitter
     * @param plus tree that will contain the portion of the tree on the plus side of the splitter
     */
    protected void splitIntoTrees(final Hyperplane<P> splitter,
            final AbstractBSPTree<P, N> minus, final AbstractBSPTree<P, N> plus) {

        AbstractBSPTree<P, N> temp = (minus != null) ? minus : plus;

        N splitRoot = temp.splitSubtree(this.getRoot(), splitter.span());

        if (minus != null) {
            if (plus != null) {
                plus.extract(splitRoot.getPlus());
            }
            minus.extract(splitRoot.getMinus());
        }
        else {
            plus.extract(splitRoot.getPlus());
        }
    }

    /** Split the subtree rooted at the given node by a partitioning convex subhyperplane defined
     * on the same region as the node. The subtree rooted at {@code node} is imported into
     * this tree, meaning that if it comes from a different tree, the other tree is not
     * modified.
     * @param node the root node of the subtree to split; may come from a different tree,
     *      in which case the other tree is not modified
     * @param partitioner partitioning convex subhyperplane
     * @return node containing the split subtree
     */
    protected N splitSubtree(final N node, final ConvexSubHyperplane<P> partitioner) {
        if (node.isLeaf()) {
            return splitLeafNode(node, partitioner);
        }
        return splitInternalNode(node, partitioner);
    }

    /** Split the given leaf node by a partitioning convex subhyperplane defined on the
     * same region and import it into this tree.
     * @param node the leaf node to split
     * @param partitioner partitioning convex subhyperplane
     * @return node containing the split subtree
     */
    private N splitLeafNode(final N node, final ConvexSubHyperplane<P> partitioner) {
        // in this case, we just create a new parent node with the partitioner as its
        // cut and two copies of the original node as children
        final N parent = createNode();
        parent.setSubtree(partitioner, copyNode(node), copyNode(node));

        return parent;
    }

    /** Split the given internal node by a partitioning convex subhyperplane defined on the same region
     * as the node and import it into this tree.
     * @param node the internal node to split
     * @param partitioner partitioning convex subhyperplane
     * @return node containing the split subtree
     */
    private N splitInternalNode(final N node, final ConvexSubHyperplane<P> partitioner) {
        // split the partitioner and node cut with each other's hyperplanes to determine their relative positions
        final Split<? extends ConvexSubHyperplane<P>> partitionerSplit = partitioner.split(node.getCutHyperplane());
        final Split<? extends ConvexSubHyperplane<P>> nodeCutSplit = node.getCut().split(partitioner.getHyperplane());

        final SplitLocation partitionerSplitSide = partitionerSplit.getLocation();
        final SplitLocation nodeCutSplitSide = nodeCutSplit.getLocation();

        final N result = createNode();

        N resultMinus;
        N resultPlus;

        if (partitionerSplitSide == SplitLocation.PLUS) {
            if (nodeCutSplitSide == SplitLocation.PLUS) {
                // partitioner is on node cut plus side, node cut is on partitioner plus side
                final N nodePlusSplit = splitSubtree(node.getPlus(), partitioner);

                resultMinus = nodePlusSplit.getMinus();

                resultPlus = copyNode(node);
                resultPlus.setSubtree(node.getCut(), importSubtree(node.getMinus()), nodePlusSplit.getPlus());
            }
            else {
                // partitioner is on node cut plus side, node cut is on partitioner minus side
                final N nodePlusSplit = splitSubtree(node.getPlus(), partitioner);

                resultMinus = copyNode(node);
                resultMinus.setSubtree(node.getCut(), importSubtree(node.getMinus()), nodePlusSplit.getMinus());

                resultPlus = nodePlusSplit.getPlus();
            }
        }
        else if (partitionerSplitSide == SplitLocation.MINUS) {
            if (nodeCutSplitSide == SplitLocation.MINUS) {
                // partitioner is on node cut minus side, node cut is on partitioner minus side
                final N nodeMinusSplit = splitSubtree(node.getMinus(), partitioner);

                resultMinus = copyNode(node);
                resultMinus.setSubtree(node.getCut(), nodeMinusSplit.getMinus(), importSubtree(node.getPlus()));

                resultPlus = nodeMinusSplit.getPlus();
            }
            else {
                // partitioner is on node cut minus side, node cut is on partitioner plus side
                final N nodeMinusSplit = splitSubtree(node.getMinus(), partitioner);

                resultMinus = nodeMinusSplit.getMinus();

                resultPlus = copyNode(node);
                resultPlus.setSubtree(node.getCut(), nodeMinusSplit.getPlus(), importSubtree(node.getPlus()));
            }
        }
        else if (partitionerSplitSide == SplitLocation.BOTH) {
            // partitioner and node cut split each other
            final N nodeMinusSplit = splitSubtree(node.getMinus(), partitionerSplit.getMinus());
            final N nodePlusSplit = splitSubtree(node.getPlus(), partitionerSplit.getPlus());

            resultMinus = copyNode(node);
            resultMinus.setSubtree(nodeCutSplit.getMinus(), nodeMinusSplit.getMinus(), nodePlusSplit.getMinus());

            resultPlus = copyNode(node);
            resultPlus.setSubtree(nodeCutSplit.getPlus(), nodeMinusSplit.getPlus(), nodePlusSplit.getPlus());
        }
        else {
            // partitioner and node cut are parallel or anti-parallel
            final boolean sameOrientation = partitioner.getHyperplane().similarOrientation(node.getCutHyperplane());

            resultMinus = importSubtree(sameOrientation ? node.getMinus() : node.getPlus());
            resultPlus = importSubtree(sameOrientation ? node.getPlus() : node.getMinus());
        }

        result.setSubtree(partitioner, resultMinus, resultPlus);

        return result;
    }

    /** Invalidate any previously computed properties that rely on the internal structure of the tree.
     * This method must be called any time the tree's internal structure changes in order to force cacheable
     * tree and node properties to be recomputed the next time they are requested.
     *
     * <p>This method increments the tree's {@link #version} property.</p>
     * @see #getVersion()
     */
    protected void invalidate() {
        version = Math.max(0, version + 1); // positive values only
    }

    /** Get the current structural version of the tree. This is incremented each time the
     * tree structure is changes and can be used by nodes to allow caching of computed values.
     * @return the current version of the tree structure
     * @see #invalidate()
     */
    protected int getVersion() {
        return version;
    }

    /** Abstract implementation of {@link BSPTree.Node}. This class is intended for use with
     * {@link AbstractBSPTree} and delegates tree mutation methods back to the parent tree object.
     * @param <P> Point implementation type
     * @param <T> Node implementation type
     */
    public static abstract class AbstractNode<P extends Point<P>, N extends AbstractNode<P, N>> implements BSPTree.Node<P, N>, Serializable {

        /** Serializable UID */
        private static final long serialVersionUID = 20190225L;

        /** The owning tree instance */
        private final AbstractBSPTree<P, N> tree;

        /** The parent node; this will be null for the tree root node */
        private N parent;

        /** The subhyperplane cutting the node's region; this will be null for leaf nodes */
        private ConvexSubHyperplane<P> cut;

        /** The node lying on the minus side of the cut subhyperplane; this will be null
         * for leaf nodes.
         */
        private N minus;

        /** The node lying on the plus side of the cut subhyperplane; this will be null
         * for leaf nodes.
         */
        private N plus;

        /** The current version of the node. This is set to track the tree's version
         * and is used to detect when certain values need to be recomputed due to
         * structural changes in the tree.
         */
        private int nodeVersion = -1;

        /** The depth of this node in the tree. This will be zero for the root node and
         * {@link AbstractBSPTree#UNKNOWN_VALUE} when the value needs to be computed.
         */
        private int depth = UNKNOWN_VALUE;

        /** The total number of nodes in the subtree rooted at this node. This will be
         * set to {@link AbstractBSPTree#UNKNOWN_VALUE} when the value needs
         * to be computed.
         */
        private int count = UNKNOWN_VALUE;

        /** The height of the subtree rooted at this node. This will
         * be set to {@link AbstractBSPTree#UNKNOWN_VALUE} when the value needs
         * to be computed.
         */
        private int height = UNKNOWN_VALUE;

        /** Simple constructor.
         * @param tree the tree instance that owns this node
         */
        protected AbstractNode(final AbstractBSPTree<P, N> tree) {
            this.tree = tree;
        }

        /** {@inheritDoc} */
        @Override
        public AbstractBSPTree<P, N> getTree() {
            return tree;
        }

        /** {@inheritDoc} */
        @Override
        public int depth() {
            if (depth == UNKNOWN_VALUE) {
                // calculate our depth based on our parent's depth, if
                // possible
                if (parent != null) {
                    int parentDepth = parent.depth();
                    if (parentDepth != UNKNOWN_VALUE) {
                        depth = parentDepth + 1;
                    }
                }
            }
            return depth;
        }

        /** {@inheritDoc} */
        @Override
        public int height() {
            checkValid();

            if (height == UNKNOWN_VALUE) {
                if (isLeaf()) {
                    height = 0;
                }
                else {
                    height = Math.max(getMinus().height(), getPlus().height()) + 1;
                }
            }

            return height;
        }

        /** {@inheritDoc} */
        @Override
        public int count() {
            checkValid();

            if (count == UNKNOWN_VALUE) {
                count = 1;

                if (!isLeaf()) {
                    count += minus.count() + plus.count();
                }
            }

            return count;
        }

        /** {@inheritDoc} */
        @Override
        public Iterator<N> iterator() {
            return new NodeIterator<P, N>(getSelf());
        }

        /** {@inheritDoc} */
        @Override
        public void accept(final BSPTreeVisitor<P, N> visitor) {
            tree.acceptVisitor(getSelf(), visitor);
        }

        /** {@inheritDoc} */
        @Override
        public N getParent() {
            return parent;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isLeaf() {
            return cut == null;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isInternal() {
            return cut != null;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isPlus() {
            return parent != null && parent.getPlus() == this;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isMinus() {
            return parent != null && parent.getMinus() == this;
        }

        /** {@inheritDoc} */
        @Override
        public ConvexSubHyperplane<P> getCut() {
            return cut;
        }

        /** {@inheritDoc} */
        @Override
        public Hyperplane<P> getCutHyperplane() {
            return (cut != null) ? cut.getHyperplane() : null;
        }

        /** {@inheritDoc} */
        @Override
        public N getPlus() {
            return plus;
        }

        /** {@inheritDoc} */
        @Override
        public N getMinus() {
            return minus;
        }

        /** {@inheritDoc} */
        @Override
        public boolean insertCut(final Hyperplane<P> cutter) {
            return tree.insertNodeCut(getSelf(), cutter);
        }

        /** {@inheritDoc} */
        @Override
        public boolean clearCut() {
            return tree.removeNodeCut(getSelf());
        }

        /** {@inheritDoc} */
        @Override
        public N cut(final Hyperplane<P> cutter) {
            this.insertCut(cutter);

            return getSelf();
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append(this.getClass().getSimpleName())
                .append("[cut= ")
                .append(getCut())
                .append("]");

            return sb.toString();
        }

        /** Set the parameters for the subtree rooted at this node. The arguments should either be
         * all null (representing a leaf node) or all non-null (representing an internal node).
         *
         * <p>Absolutely no validation is performed on the arguments. Callers are responsible for
         * ensuring that any given subhyperplane fits the region defined by the node and that
         * any child nodes belong to this tree and are correctly initialized.</p>
         *
         * @param cut the new cut subhyperplane for the node
         * @param minus the new minus child for the node
         * @param plus the new plus child for the node
         */
        protected void setSubtree(final ConvexSubHyperplane<P> cut, final N minus, final N plus) {
            this.cut = cut;

            final N self = getSelf();

            // cast for access to private members
            AbstractNode<P, N> minusNode = minus;
            AbstractNode<P, N> plusNode = plus;

            // get the child depth now if we know it offhand, otherwise set it to the unknown value
            // and have the child pull it when needed
            final int childDepth = (depth != UNKNOWN_VALUE) ? depth + 1 : UNKNOWN_VALUE;

            if (minus != null) {
                minusNode.parent = self;
                minusNode.depth = childDepth;
            }
            this.minus = minus;

            if (plus != null) {
                plusNode.parent = self;
                plusNode.depth = childDepth;
            }
            this.plus = plus;
        }

        /**
         * Make this node a root node, detaching it from its parent and settings its depth to zero.
         * Any previous parent node will be left in an invalid state since one of its children now
         * does not have a reference back to it.
         */
        protected void makeRoot() {
            parent = null;
            depth = 0;
        }

        /** Check if cached node properties are valid, meaning that no structural updates have
         * occurred in the tree since the last call to this method. If updates have occurred, the
         * {@link #nodeInvalidated()} method is called to clear the cached properties. This method
         * should be called at the beginning of any method that fetches cacheable properties
         * to ensure that no stale values are returned.
         */
        protected void checkValid() {
            final int treeVersion = tree.getVersion();

            if (nodeVersion != treeVersion) {
                // the tree structure changed somewhere
                nodeInvalidated();

                // store the current version
                nodeVersion = treeVersion;
            }
        }

        /** Method called from {@link #checkValid()} when updates
         * are detected in the tree. This method should clear out any
         * computed properties that rely on the structure of the tree
         * and prepare them for recalculation.
         */
        protected void nodeInvalidated() {
            count = UNKNOWN_VALUE;
            height = UNKNOWN_VALUE;
        }

        /** Get a reference to the current instance, cast to type N.
         * @return a reference to the current instance, as type N.
         */
        protected abstract N getSelf();
    }

    /** Class for iterating through the nodes in a BSP subtree.
     * @param <P> Point implementation type
     * @param <N> Node implementation type
     */
    public static class NodeIterator<P extends Point<P>, N extends AbstractNode<P, N>> implements Iterator<N> {

        /** The current node stack */
        private final Deque<N> stack = new LinkedList<>();

        /** Create a new instance for iterating over the nodes in the given subtree.
         * @param subtreeRoot the root node of the subtree to iterate
         */
        public NodeIterator(final N subtreeRoot) {
            stack.push(subtreeRoot);
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        /** {@inheritDoc} */
        @Override
        public N next() {
            if (stack.isEmpty()) {
                throw new NoSuchElementException();
            }

            final N result = stack.pop();

            if (result != null && !result.isLeaf()) {
                stack.push(result.getPlus());
                stack.push(result.getMinus());
            }

            return result;
        }
    }
}
