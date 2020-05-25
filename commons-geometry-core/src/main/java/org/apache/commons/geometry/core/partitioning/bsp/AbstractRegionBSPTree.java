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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.internal.IteratorTransform;
import org.apache.commons.geometry.core.partitioning.BoundarySource;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.HyperplaneLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.partitioning.HyperplaneSubset;
import org.apache.commons.geometry.core.partitioning.bsp.BSPTreeVisitor.ClosestFirstVisitor;

/** Abstract {@link BSPTree} specialized for representing regions of space. For example,
 * this class can be used to represent polygons in Euclidean 2D space and polyhedrons
 * in Euclidean 3D space.
 *
 * <p>This class is not thread safe.</p>
 * @param <P> Point implementation type
 * @param <N> BSP tree node implementation type
 * @see HyperplaneBoundedRegion
 */
public abstract class AbstractRegionBSPTree<
        P extends Point<P>,
        N extends AbstractRegionBSPTree.AbstractRegionNode<P, N>>
    extends AbstractBSPTree<P, N> implements HyperplaneBoundedRegion<P> {

    /** The default {@link RegionCutRule}. */
    private static final RegionCutRule DEFAULT_REGION_CUT_RULE = RegionCutRule.MINUS_INSIDE;

    /** Value used to indicate an unknown size. */
    private static final double UNKNOWN_SIZE = -1.0;

    /** The region boundary size; this is computed when requested and then cached. */
    private double boundarySize = UNKNOWN_SIZE;

    /** The current size properties for the region. */
    private RegionSizeProperties<P> regionSizeProperties;

    /** Construct a new region will the given boolean determining whether or not the
     * region will be full (including the entire space) or empty (excluding the entire
     * space).
     * @param full if true, the region will cover the entire space, otherwise it will
     *      be empty
     */
    protected AbstractRegionBSPTree(final boolean full) {
        getRoot().setLocationValue(full ? RegionLocation.INSIDE : RegionLocation.OUTSIDE);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return !hasNodeWithLocationRecursive(getRoot(), RegionLocation.INSIDE);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFull() {
        return !hasNodeWithLocationRecursive(getRoot(), RegionLocation.OUTSIDE);
    }

    /** Return true if any node in the subtree rooted at the given node has a location with the
     * given value.
     * @param node the node at the root of the subtree to search
     * @param location the location to find
     * @return true if any node in the subtree has the given location
     */
    private boolean hasNodeWithLocationRecursive(final AbstractRegionNode<P, N> node, final RegionLocation location) {
        if (node == null) {
            return false;
        }

        return node.getLocation() == location ||
                hasNodeWithLocationRecursive(node.getMinus(), location) ||
                hasNodeWithLocationRecursive(node.getPlus(), location);
    }

    /** Modify this instance so that it contains the entire space.
     * @see #isFull()
     */
    public void setFull() {
        final N root = getRoot();

        root.clearCut();
        root.setLocationValue(RegionLocation.INSIDE);
    }

    /** Modify this instance so that is is completely empty.
     * @see #isEmpty()
     */
    public void setEmpty() {
        final N root = getRoot();

        root.clearCut();
        root.setLocationValue(RegionLocation.OUTSIDE);
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        return getRegionSizeProperties().getSize();
    }

    /** {@inheritDoc} */
    @Override
    public double getBoundarySize() {
        if (boundarySize < 0.0) {
            double sum = 0.0;

            RegionCutBoundary<P> boundary;
            for (final AbstractRegionNode<P, N> node : nodes()) {
                boundary = node.getCutBoundary();
                if (boundary != null) {
                    sum += boundary.getInsideFacing().getSize();
                    sum += boundary.getOutsideFacing().getSize();
                }
            }

            boundarySize = sum;
        }

        return boundarySize;
    }

    /** Insert a hyperplane subset into the tree, using the default {@link RegionCutRule} of
     * {@link RegionCutRule#MINUS_INSIDE MINUS_INSIDE}.
     * @param sub the hyperplane subset to insert into the tree
     */
    public void insert(final HyperplaneSubset<P> sub) {
        insert(sub, DEFAULT_REGION_CUT_RULE);
    }

    /** Insert a hyperplane subset into the tree.
     * @param sub the hyperplane subset to insert into the tree
     * @param cutRule rule used to determine the region locations of new child nodes
     */
    public void insert(final HyperplaneSubset<P> sub, final RegionCutRule cutRule) {
        insert(sub.toConvex(), cutRule);
    }

    /** Insert a hyperplane convex subset into the tree, using the default {@link RegionCutRule} of
     * {@link RegionCutRule#MINUS_INSIDE MINUS_INSIDE}.
     * @param convexSub the hyperplane convex subset to insert into the tree
     */
    public void insert(final HyperplaneConvexSubset<P> convexSub) {
        insert(convexSub, DEFAULT_REGION_CUT_RULE);
    }

    /** Insert a hyperplane convex subset into the tree.
     * @param convexSub the hyperplane convex subset to insert into the tree
     * @param cutRule rule used to determine the region locations of new child nodes
     */
    public void insert(final HyperplaneConvexSubset<P> convexSub, final RegionCutRule cutRule) {
        insert(convexSub, getSubtreeInitializer(cutRule));
    }

    /** Insert a set of hyperplane convex subsets into the tree, using the default {@link RegionCutRule} of
     * {@link RegionCutRule#MINUS_INSIDE MINUS_INSIDE}.
     * @param convexSubs iterable containing a collection of hyperplane convex subsets
     *      to insert into the tree
     */
    public void insert(final Iterable<? extends HyperplaneConvexSubset<P>> convexSubs) {
        insert(convexSubs, DEFAULT_REGION_CUT_RULE);
    }

    /** Insert a set of hyperplane convex subsets into the tree.
     * @param convexSubs iterable containing a collection of hyperplane convex subsets
     *      to insert into the tree
     * @param cutRule rule used to determine the region locations of new child nodes
     */
    public void insert(final Iterable<? extends HyperplaneConvexSubset<P>> convexSubs, final RegionCutRule cutRule) {
        for (final HyperplaneConvexSubset<P> convexSub : convexSubs) {
            insert(convexSub, cutRule);
        }
    }

    /** Insert all hyperplane convex subsets from the given source into the tree, using the default
     * {@link RegionCutRule} of {@link RegionCutRule#MINUS_INSIDE MINUS_INSIDE}.
     * @param boundarySrc source of boundary hyperplane subsets to insert
     *      into the tree
     */
    public void insert(final BoundarySource<? extends HyperplaneConvexSubset<P>> boundarySrc) {
        insert(boundarySrc, DEFAULT_REGION_CUT_RULE);
    }

    /** Insert all hyperplane convex subsets from the given source into the tree.
     * @param boundarySrc source of boundary hyperplane subsets to insert
     *      into the tree
     * @param cutRule rule used to determine the region locations of new child nodes
     */
    public void insert(final BoundarySource<? extends HyperplaneConvexSubset<P>> boundarySrc,
            final RegionCutRule cutRule) {
        try (Stream<? extends HyperplaneConvexSubset<P>> stream = boundarySrc.boundaryStream()) {
            stream.forEach(c -> insert(c, cutRule));
        }
    }

    /** Get the subtree initializer to use for the given region cut rule.
     * @param cutRule the cut rule to get an initializer for
     * @return the subtree initializer for the given region cut rule
     */
    protected SubtreeInitializer<N> getSubtreeInitializer(final RegionCutRule cutRule) {
        switch (cutRule) {
        case INHERIT:
            return root -> {
                final RegionLocation rootLoc = root.getLocation();

                root.getMinus().setLocationValue(rootLoc);
                root.getPlus().setLocationValue(rootLoc);
            };
        case PLUS_INSIDE:
            return root -> {
                root.getMinus().setLocationValue(RegionLocation.OUTSIDE);
                root.getPlus().setLocationValue(RegionLocation.INSIDE);
            };
        default:
            return root -> {
                root.getMinus().setLocationValue(RegionLocation.INSIDE);
                root.getPlus().setLocationValue(RegionLocation.OUTSIDE);
            };
        }
    }

    /** Return an {@link Iterable} for iterating over the boundaries of the region.
     * Each boundary is oriented such that its plus side points to the outside of the
     * region. The exact ordering of the boundaries is determined by the internal structure
     * of the tree.
     * @return an {@link Iterable} for iterating over the boundaries of the region
     * @see #getBoundaries()
     */
    public Iterable<? extends HyperplaneConvexSubset<P>> boundaries() {
        return createBoundaryIterable(Function.identity());
    }

    /** Internal method for creating the iterable instances used to iterate the region boundaries.
     * @param typeConverter function to convert the generic hyperplane subset type into
     *      the type specific for this tree
     * @param <C> HyperplaneConvexSubset implementation type
     * @return an iterable to iterating the region boundaries
     */
    protected <C extends HyperplaneConvexSubset<P>> Iterable<C> createBoundaryIterable(
            final Function<HyperplaneConvexSubset<P>, C> typeConverter) {

        return () -> new RegionBoundaryIterator<>(
                getRoot().nodes().iterator(),
                typeConverter);
    }

    /** Return a list containing the boundaries of the region. Each boundary is oriented such
     * that its plus side points to the outside of the region. The exact ordering of
     * the boundaries is determined by the internal structure of the tree.
     * @return a list of the boundaries of the region
     */
    public List<? extends HyperplaneConvexSubset<P>> getBoundaries() {
        return createBoundaryList(Function.identity());
    }

    /** Internal method for creating a list of the region boundaries.
     * @param typeConverter function to convert the generic convex subset type into
     *      the type specific for this tree
     * @param <C> HyperplaneConvexSubset implementation type
     * @return a list of the region boundaries
     */
    protected <C extends HyperplaneConvexSubset<P>> List<C> createBoundaryList(
            final Function<HyperplaneConvexSubset<P>, C> typeConverter) {

        final List<C> result = new ArrayList<>();

        final RegionBoundaryIterator<P, C, N> it = new RegionBoundaryIterator<>(nodes().iterator(), typeConverter);
        it.forEachRemaining(result::add);

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public P project(P pt) {
        final BoundaryProjector<P, N> projector = new BoundaryProjector<>(pt);
        accept(projector);

        return projector.getProjected();
    }

    /** {@inheritDoc} */
    @Override
    public P getCentroid() {
        return getRegionSizeProperties().getCentroid();
    }

    /** Helper method implementing the algorithm for splitting a tree by a hyperplane. Subclasses
     * should call this method with two instantiated trees of the correct type.
     * @param splitter splitting hyperplane
     * @param minus tree that will contain the minus side of the split result
     * @param plus tree that will contain the plus side of the split result
     * @param <T> Tree implementation type
     * @return result of splitting this tree with the given hyperplane
     */
    protected <T extends AbstractRegionBSPTree<P, N>> Split<T> split(final Hyperplane<P> splitter,
            final T minus, final T plus) {

        splitIntoTrees(splitter, minus, plus);

        T splitMinus = null;
        T splitPlus = null;

        if (minus != null) {
            minus.getRoot().getPlus().setLocationValue(RegionLocation.OUTSIDE);
            minus.condense();

            splitMinus = minus.isEmpty() ? null : minus;
        }
        if (plus != null) {
            plus.getRoot().getMinus().setLocationValue(RegionLocation.OUTSIDE);
            plus.condense();

            splitPlus = plus.isEmpty() ? null : plus;
        }

        return new Split<>(splitMinus, splitPlus);
    }

    /** Get the size-related properties for the region. The value is computed
     * lazily and cached.
     * @return the size-related properties for the region
     */
    protected RegionSizeProperties<P> getRegionSizeProperties() {
        if (regionSizeProperties == null) {
            regionSizeProperties = computeRegionSizeProperties();
        }

        return regionSizeProperties;
    }

    /** Compute the size-related properties of the region.
     * @return object containing size properties for the region
     */
    protected abstract RegionSizeProperties<P> computeRegionSizeProperties();

    /** {@inheritDoc}
     *
     * <p>If the point is {@link org.apache.commons.geometry.core.Spatial#isNaN() NaN}, then
     * {@link RegionLocation#OUTSIDE} is returned.</p>
     */
    @Override
    public RegionLocation classify(final P point) {
        if (point.isNaN()) {
            return RegionLocation.OUTSIDE;
        }

        return classifyRecursive(getRoot(), point);
    }

    /** Recursively classify a point with respect to the region.
     * @param node the node to classify against
     * @param point the point to classify
     * @return the classification of the point with respect to the region rooted
     *      at the given node
     */
    private RegionLocation classifyRecursive(final AbstractRegionNode<P, N> node, final P point) {
        if (node.isLeaf()) {
            // the point is in a leaf, so the classification is just the leaf location
            return node.getLocation();
        } else {
            final HyperplaneLocation cutLoc = node.getCutHyperplane().classify(point);

            if (cutLoc == HyperplaneLocation.MINUS) {
                return classifyRecursive(node.getMinus(), point);
            } else if (cutLoc == HyperplaneLocation.PLUS) {
                return classifyRecursive(node.getPlus(), point);
            } else {
                // the point is on the cut boundary; classify against both child
                // subtrees and see if we end up with the same result or not
                RegionLocation minusLoc = classifyRecursive(node.getMinus(), point);
                RegionLocation plusLoc = classifyRecursive(node.getPlus(), point);

                if (minusLoc == plusLoc) {
                    return minusLoc;
                }
                return RegionLocation.BOUNDARY;
            }
        }
    }

    /** Change this region into its complement. All inside nodes become outside
     * nodes and vice versa. The orientations of the node cuts are not modified.
     */
    public void complement() {
        complementRecursive(getRoot());
    }

    /** Set this instance to be the complement of the given tree. The argument
     * is not modified.
     * @param tree the tree to become the complement of
     */
    public void complement(final AbstractRegionBSPTree<P, N> tree) {
        copySubtree(tree.getRoot(), getRoot());
        complementRecursive(getRoot());
    }

    /** Recursively switch all inside nodes to outside nodes and vice versa.
     * @param node the node at the root of the subtree to switch
     */
    private void complementRecursive(final AbstractRegionNode<P, N> node) {
        if (node != null) {
            final RegionLocation newLoc = (node.getLocation() == RegionLocation.INSIDE) ?
                    RegionLocation.OUTSIDE :
                    RegionLocation.INSIDE;

            node.setLocationValue(newLoc);

            complementRecursive(node.getMinus());
            complementRecursive(node.getPlus());
        }
    }

    /** Compute the union of this instance and the given region, storing the result back in
     * this instance. The argument is not modified.
     * @param other the tree to compute the union with
     */
    public void union(final AbstractRegionBSPTree<P, N> other) {
        new UnionOperator<P, N>().apply(this, other, this);
    }

    /** Compute the union of the two regions passed as arguments and store the result in
     * this instance. Any nodes currently existing in this instance are removed.
     * @param a first argument to the union operation
     * @param b second argument to the union operation
     */
    public void union(final AbstractRegionBSPTree<P, N> a, final AbstractRegionBSPTree<P, N> b) {
        new UnionOperator<P, N>().apply(a, b, this);
    }

    /** Compute the intersection of this instance and the given region, storing the result back in
     * this instance. The argument is not modified.
     * @param other the tree to compute the intersection with
     */
    public void intersection(final AbstractRegionBSPTree<P, N> other) {
        new IntersectionOperator<P, N>().apply(this, other, this);
    }

    /** Compute the intersection of the two regions passed as arguments and store the result in
     * this instance. Any nodes currently existing in this instance are removed.
     * @param a first argument to the intersection operation
     * @param b second argument to the intersection operation
     */
    public void intersection(final AbstractRegionBSPTree<P, N> a, final AbstractRegionBSPTree<P, N> b) {
        new IntersectionOperator<P, N>().apply(a, b, this);
    }

    /** Compute the difference of this instance and the given region, storing the result back in
     * this instance. The argument is not modified.
     * @param other the tree to compute the difference with
     */
    public void difference(final AbstractRegionBSPTree<P, N> other) {
        new DifferenceOperator<P, N>().apply(this, other, this);
    }

    /** Compute the difference of the two regions passed as arguments and store the result in
     * this instance. Any nodes currently existing in this instance are removed.
     * @param a first argument to the difference operation
     * @param b second argument to the difference operation
     */
    public void difference(final AbstractRegionBSPTree<P, N> a, final AbstractRegionBSPTree<P, N> b) {
        new DifferenceOperator<P, N>().apply(a, b, this);
    }

    /** Compute the symmetric difference (xor) of this instance and the given region, storing the result back in
     * this instance. The argument is not modified.
     * @param other the tree to compute the symmetric difference with
     */
    public void xor(final AbstractRegionBSPTree<P, N> other) {
        new XorOperator<P, N>().apply(this, other, this);
    }

    /** Compute the symmetric difference (xor) of the two regions passed as arguments and store the result in
     * this instance. Any nodes currently existing in this instance are removed.
     * @param a first argument to the symmetric difference operation
     * @param b second argument to the symmetric difference operation
     */
    public void xor(final AbstractRegionBSPTree<P, N> a, final AbstractRegionBSPTree<P, N> b) {
        new XorOperator<P, N>().apply(a, b, this);
    }

    /** Condense this tree by removing redundant subtrees, returning true if the
     * tree structure was modified.
     *
     * <p>This operation can be used to reduce the total number of nodes in the
     * tree after performing node manipulations. For example, if two sibling leaf
     * nodes both represent the same {@link RegionLocation}, then there is no reason
     * from the perspective of the geometric region to retain both nodes. They are
     * therefore both merged into their parent node. This method performs this
     * simplification process.
     * </p>
     * @return true if the tree structure was modified, otherwise false
     */
    public boolean condense() {
        return new Condenser<P, N>().condense(getRoot());
    }

    /** {@inheritDoc} */
    @Override
    protected void copyNodeProperties(final N src, final N dst) {
        dst.setLocationValue(src.getLocation());
    }

    /** {@inheritDoc} */
    @Override
    protected void invalidate() {
        super.invalidate();

        // clear cached region properties
        boundarySize = UNKNOWN_SIZE;
        regionSizeProperties = null;
    }

    /** {@link BSPTree.Node} implementation for use with {@link AbstractRegionBSPTree}s.
     * @param <P> Point implementation type
     * @param <N> BSP tree node implementation type
     */
    public abstract static class AbstractRegionNode<P extends Point<P>, N extends AbstractRegionNode<P, N>>
        extends AbstractBSPTree.AbstractNode<P, N> {
        /** The location for the node. This will only be set on leaf nodes. */
        private RegionLocation location;

        /** Object representing the part of the node cut hyperplane subset that lies on the
         * region boundary. This is calculated lazily and is only present on internal nodes.
         */
        private RegionCutBoundary<P> cutBoundary;

        /** Simple constructor.
         * @param tree owning tree instance
         */
        protected AbstractRegionNode(AbstractBSPTree<P, N> tree) {
            super(tree);
        }

        /** {@inheritDoc} */
        @Override
        public AbstractRegionBSPTree<P, N> getTree() {
            // cast to our parent tree type
            return (AbstractRegionBSPTree<P, N>) super.getTree();
        }

        /** Get the location property of the node. Only the locations of leaf nodes are meaningful
         * as they relate to the region represented by the BSP tree. For example, changing
         * the location of an internal node will only affect the geometric properties
         * of the region if the node later becomes a leaf node.
         * @return the location of the node
         */
        public RegionLocation getLocation() {
            return location;
        }

        /** Set the location property for the node. If the location is changed, the tree is
         * invalidated.
         *
         * <p>Only the locations of leaf nodes are meaningful
         * as they relate to the region represented by the BSP tree. For example, changing
         * the location of an internal node will only affect the geometric properties
         * of the region if the node later becomes a leaf node.</p>
         * @param location the location for the node
         * @throws IllegalArgumentException if {@code location} is not one of
         *      {@link RegionLocation#INSIDE INSIDE} or {@link RegionLocation#OUTSIDE OUTSIDE}
         */
        public void setLocation(final RegionLocation location) {
            if (location != RegionLocation.INSIDE && location != RegionLocation.OUTSIDE) {
                throw new IllegalArgumentException("Invalid node location: " + location);
            }
            if (this.location != location) {
                this.location = location;

                getTree().invalidate();
            }
        }

        /** True if the node is a leaf node and has a location of {@link RegionLocation#INSIDE}.
         * @return true if the node is a leaf node and has a location of
         *      {@link RegionLocation#INSIDE}
         */
        public boolean isInside() {
            return isLeaf() && getLocation() == RegionLocation.INSIDE;
        }

        /** True if the node is a leaf node and has a location of {@link RegionLocation#OUTSIDE}.
         * @return true if the node is a leaf node and has a location of
         *      {@link RegionLocation#OUTSIDE}
         */
        public boolean isOutside() {
            return isLeaf() && getLocation() == RegionLocation.OUTSIDE;
        }

        /** Insert a cut into this node, using the default region cut rule of
         * {@link RegionCutRule#MINUS_INSIDE}.
         * @param cutter the hyperplane to cut the node's region with
         * @return true if the cutting hyperplane intersected the node's region, resulting
         *      in the creation of new child nodes
         * @see #insertCut(Hyperplane, RegionCutRule)
         */
        public boolean insertCut(final Hyperplane<P> cutter) {
            return insertCut(cutter, DEFAULT_REGION_CUT_RULE);
        }

        /** Insert a cut into this node. If the given hyperplane intersects
         * this node's region, then the node's cut is set to the {@link HyperplaneConvexSubset}
         * representing the intersection, new plus and minus child leaf nodes
         * are assigned, and true is returned. If the hyperplane does not intersect
         * the node's region, then the node's cut and plus and minus child references
         * are all set to null (ie, it becomes a leaf node) and false is returned. In
         * either case, any existing cut and/or child nodes are removed by this method.
         * @param cutter the hyperplane to cut the node's region with
         * @param cutRule rule used to determine the region locations of newly created
         *      child nodes
         * @return true if the cutting hyperplane intersected the node's region, resulting
         *      in the creation of new child nodes
         */
        public boolean insertCut(final Hyperplane<P> cutter, final RegionCutRule cutRule) {
            final AbstractRegionBSPTree<P, N> tree = getTree();
            return tree.cutNode(getSelf(), cutter, tree.getSubtreeInitializer(cutRule));
        }

        /** Remove the cut from this node. Returns true if the node previously had a cut.
         * @return true if the node had a cut before the call to this method
         */
        public boolean clearCut() {
            return getTree().removeNodeCut(getSelf());
        }

        /** Cut this node with the given hyperplane. The same node is returned, regardless of
         * the outcome of the cut operation. If the operation succeeded, then the node will
         * have plus and minus child nodes.
         * @param cutter the hyperplane to cut the node's region with
         * @return this node
         * @see #insertCut(Hyperplane)
         */
        public N cut(final Hyperplane<P> cutter) {
            return cut(cutter, DEFAULT_REGION_CUT_RULE);
        }

        /** Cut this node with the given hyperplane, using {@code cutRule} to determine the region
         * locations of any new child nodes. The same node is returned, regardless of
         * the outcome of the cut operation. If the operation succeeded, then the node will
         * have plus and minus child nodes.
         * @param cutter the hyperplane to cut the node's region with
         * @param cutRule rule used to determine the region locations of newly created
         *      child nodes
         * @return this node
         * @see #insertCut(Hyperplane, RegionCutRule)
         */
        public N cut(final Hyperplane<P> cutter, final RegionCutRule cutRule) {
            this.insertCut(cutter, cutRule);

            return getSelf();
        }

        /** Get the portion of the node's cut that lies on the boundary of the region.
         * @return the portion of the node's cut that lies on the boundary of
         *      the region
         */
        public RegionCutBoundary<P> getCutBoundary() {
            if (!isLeaf()) {
                checkValid();

                if (cutBoundary == null) {
                    cutBoundary = computeBoundary();
                }
            }

            return cutBoundary;
        }

        /** Compute the portion of the node's cut that lies on the boundary of the region.
         * This method must only be called on internal nodes.
         * @return object representing the portions of the node's cut that lie on the region's boundary
         */
        private RegionCutBoundary<P> computeBoundary() {
            HyperplaneConvexSubset<P> sub = getCut();

            // find the portions of the node cut sub-hyperplane that touch inside and
            // outside cells in the minus sub-tree
            HyperplaneSubset.Builder<P> minusInBuilder = sub.builder();
            HyperplaneSubset.Builder<P> minusOutBuilder = sub.builder();

            characterizeHyperplaneSubset(sub, getMinus(), minusInBuilder, minusOutBuilder);

            List<? extends HyperplaneConvexSubset<P>> minusIn = minusInBuilder.build().toConvex();
            List<? extends HyperplaneConvexSubset<P>> minusOut = minusOutBuilder.build().toConvex();

            // create the result boundary builders
            HyperplaneSubset.Builder<P> insideFacing = sub.builder();
            HyperplaneSubset.Builder<P> outsideFacing = sub.builder();

            if (!minusIn.isEmpty()) {
                // Add to the boundary anything that touches an inside cell in the minus sub-tree
                // and an outside cell in the plus sub-tree. These portions are oriented with their
                // plus side pointing to the outside of the region.
                for (HyperplaneConvexSubset<P> minusInFragment : minusIn) {
                    characterizeHyperplaneSubset(minusInFragment, getPlus(), null, outsideFacing);
                }
            }

            if (!minusOut.isEmpty()) {
                // Add to the boundary anything that touches an outside cell in the minus sub-tree
                // and an inside cell in the plus sub-tree. These portions are oriented with their
                // plus side pointing to the inside of the region.
                for (HyperplaneConvexSubset<P> minusOutFragment : minusOut) {
                    characterizeHyperplaneSubset(minusOutFragment, getPlus(), insideFacing, null);
                }
            }

            return new RegionCutBoundary<>(insideFacing.build(), outsideFacing.build());
        }

        /** Recursive method to characterize a hyperplane convex subset with respect to the region's
         * boundaries.
         * @param sub the hyperplane convex subset to characterize
         * @param node the node to characterize the hyperplane convex subset against
         * @param in the builder that will receive the portions of the subset that lie in the inside
         *      of the region; may be null
         * @param out the builder that will receive the portions of the subset that lie on the outside
         *      of the region; may be null
         */
        private void characterizeHyperplaneSubset(final HyperplaneConvexSubset<P> sub,
                final AbstractRegionNode<P, N> node, final HyperplaneSubset.Builder<P> in,
                final HyperplaneSubset.Builder<P> out) {

            if (sub != null) {
                if (node.isLeaf()) {
                    if (node.isInside() && in != null) {
                        in.add(sub);
                    } else if (node.isOutside() && out != null) {
                        out.add(sub);
                    }
                } else {
                    final Split<? extends HyperplaneConvexSubset<P>> split = sub.split(node.getCutHyperplane());

                    // Continue further on down the subtree with the same subset if the
                    // subset lies directly on the current node's cut
                    if (split.getLocation() == SplitLocation.NEITHER) {
                        characterizeHyperplaneSubset(sub, node.getPlus(), in, out);
                        characterizeHyperplaneSubset(sub, node.getMinus(), in, out);
                    } else {
                        characterizeHyperplaneSubset(split.getPlus(), node.getPlus(), in, out);
                        characterizeHyperplaneSubset(split.getMinus(), node.getMinus(), in, out);
                    }
                }
            }
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append(this.getClass().getSimpleName())
                .append("[cut= ")
                .append(getCut())
                .append(", location= ")
                .append(getLocation())
                .append("]");

            return sb.toString();
        }

        /** {@inheritDoc} */
        @Override
        protected void nodeInvalidated() {
            super.nodeInvalidated();

            // null any computed boundary value since it is no longer valid
            cutBoundary = null;
        }

        /** Directly set the value of the location property for the node. No input validation
         * is performed and the tree is not invalidated.
         * @param locationValue the new location value for the node
         * @see #setLocation(RegionLocation)
         */
        protected void setLocationValue(final RegionLocation locationValue) {
            this.location = locationValue;
        }
    }

    /** Class used to compute the point on the region's boundary that is closest to a target point.
     * @param <P> Point implementation type
     * @param <N> BSP tree node implementation type
     */
    protected static class BoundaryProjector<P extends Point<P>, N extends AbstractRegionNode<P, N>>
        extends ClosestFirstVisitor<P, N> {
        /** The projected point. */
        private P projected;

        /** The current closest distance to the boundary found. */
        private double minDist = -1.0;

        /** Simple constructor.
         * @param point the point to project onto the region's boundary
         */
        public BoundaryProjector(final P point) {
            super(point);
        }

        /** {@inheritDoc} */
        @Override
        public Result visit(final N node) {
            final P point = getTarget();

            if (node.isInternal() && (minDist < 0.0 || isPossibleClosestCut(node.getCut(), point, minDist))) {
                final RegionCutBoundary<P> boundary = node.getCutBoundary();
                final P boundaryPt = boundary.closest(point);

                final double dist = boundaryPt.distance(point);
                final int cmp = Double.compare(dist, minDist);

                if (minDist < 0.0 || cmp < 0) {
                    projected = boundaryPt;
                    minDist = dist;
                } else if (cmp == 0) {
                    // the two points are the _exact_ same distance from the reference point, so use
                    // a separate method to disambiguate them
                    projected = disambiguateClosestPoint(point, projected, boundaryPt);
                }
            }

            return Result.CONTINUE;
        }

        /** Return true if the given node cut is a possible candidate for containing the closest region
         * boundary point to the target.
         * @param cut the node cut to test
         * @param target the target point being projected
         * @param currentMinDist the smallest distance found so far to a region boundary; this value is guaranteed
         *      to be non-negative
         * @return true if the cut is a possible candidate for containing the closest region
         *      boundary point to the target
         */
        protected boolean isPossibleClosestCut(final HyperplaneSubset<P> cut, final P target,
                final double currentMinDist) {
            return Math.abs(cut.getHyperplane().offset(target)) <= currentMinDist;
        }

        /** Method used to determine which of points {@code a} and {@code b} should be considered
         * as the "closest" point to {@code target} when the points are exactly equidistant.
         * @param target the target point
         * @param a first point to consider
         * @param b second point to consider
         * @return which of {@code a} or {@code b} should be considered as the one closest to
         *      {@code target}
         */
        protected P disambiguateClosestPoint(final P target, final P a, final P b) {
            return a;
        }

        /** Get the projected point on the region's boundary, or null if no point could be found.
         * @return the projected point on the region's boundary
         */
        public P getProjected() {
            return projected;
        }
    }

    /** Class containing the primary size-related properties of a region. These properties
     * are typically computed at the same time, so this class serves to encapsulate the result
     * of the combined computation.
     * @param <P> Point implementation type
     */
    protected static class RegionSizeProperties<P extends Point<P>> {
        /** The size of the region. */
        private final double size;

        /** The centroid of the region. */
        private final P centroid;

        /** Simple constructor.
         * @param size the region size
         * @param centroid the region centroid
         */
        public RegionSizeProperties(final double size, final P centroid) {
            this.size = size;
            this.centroid = centroid;
        }

        /** Get the size of the region.
         * @return the size of the region
         */
        public double getSize() {
            return size;
        }

        /** Get the centroid of the region.
         * @return the centroid of the region
         */
        public P getCentroid() {
            return centroid;
        }
    }

    /** Class containing the basic algorithm for merging region BSP trees.
     * @param <P> Point implementation type
     * @param <N> BSP tree node implementation type
     */
    private abstract static class RegionMergeOperator<P extends Point<P>, N extends AbstractRegionNode<P, N>>
        extends AbstractBSPTreeMergeOperator<P, N> {

        /** Merge two input trees, storing the output in the third. The output tree can be one of the
         * input trees. The output tree is condensed before the method returns.
         * @param inputTree1 first input tree
         * @param inputTree2 second input tree
         * @param outputTree the tree that will contain the result of the merge; may be one
         *      of the input trees
         */
        public void apply(final AbstractRegionBSPTree<P, N> inputTree1, final AbstractRegionBSPTree<P, N> inputTree2,
                final AbstractRegionBSPTree<P, N> outputTree) {

            this.performMerge(inputTree1, inputTree2, outputTree);

            outputTree.condense();
        }
    }

    /** Class for performing boolean union operations on region trees.
     * @param <P> Point implementation type
     * @param <N> BSP tree node implementation type
     */
    private static final class UnionOperator<P extends Point<P>, N extends AbstractRegionNode<P, N>>
        extends RegionMergeOperator<P, N> {

        /** {@inheritDoc} */
        @Override
        protected N mergeLeaf(final N node1, final N node2) {
            if (node1.isLeaf()) {
                return node1.isInside() ? node1 : node2;
            }

            // call again with flipped arguments
            return mergeLeaf(node2, node1);
        }
    }

    /** Class for performing boolean intersection operations on region trees.
     * @param <P> Point implementation type
     * @param <N> BSP tree node implementation type
     */
    private static final class IntersectionOperator<P extends Point<P>, N extends AbstractRegionNode<P, N>>
        extends RegionMergeOperator<P, N> {

        /** {@inheritDoc} */
        @Override
        protected N mergeLeaf(final N node1, final N node2) {
            if (node1.isLeaf()) {
                return node1.isInside() ? node2 : node1;
            }

            // call again with flipped arguments
            return mergeLeaf(node2, node1);
        }
    }

    /** Class for performing boolean difference operations on region trees.
     * @param <P> Point implementation type
     * @param <N> BSP tree node implementation type
     */
    private static final class DifferenceOperator<P extends Point<P>, N extends AbstractRegionNode<P, N>>
        extends RegionMergeOperator<P, N> {

        /** {@inheritDoc} */
        @Override
        protected N mergeLeaf(final N node1, final N node2) {
            // a region is included if it belongs in tree1 and is not in tree2

            if (node1.isInside()) {
                // this region is inside of tree1, so only include subregions that are
                // not in tree2, ie include everything in node2's complement
                final N output = outputSubtree(node2);
                output.getTree().complementRecursive(output);

                return output;
            } else if (node2.isInside()) {
                // this region is inside of tree2 and so cannot be in the result region
                final N output = outputNode();
                output.setLocationValue(RegionLocation.OUTSIDE);

                return output;
            }

            // this region is not in tree2, so we can include everything in tree1
            return node1;
        }
    }

    /** Class for performing boolean symmetric difference (xor) operations on region trees.
     * @param <P> Point implementation type
     * @param <N> BSP tree node implementation type
     */
    private static final class XorOperator<P extends Point<P>, N extends AbstractRegionNode<P, N>>
        extends RegionMergeOperator<P, N> {

        /** {@inheritDoc} */
        @Override
        protected N mergeLeaf(final N node1, final N node2) {
            // a region is included if it belongs in tree1 and is not in tree2 OR
            // it belongs in tree2 and is not in tree1

            if (node1.isLeaf()) {
                if (node1.isInside()) {
                    // this region is inside node1, so only include subregions that are
                    // not in node2, ie include everything in node2's complement
                    final N output = outputSubtree(node2);
                    output.getTree().complementRecursive(output);

                    return output;
                } else {
                    // this region is not in node1, so only include subregions that
                    // in node2
                    return node2;
                }
            }

            // the operation is symmetric, so perform the same operation but with the
            // nodes flipped
            return mergeLeaf(node2, node1);
        }
    }

    /** Internal class used to perform tree condense operations.
     * @param <P> Point implementation type
     * @param <N> BSP tree node implementation type
     */
    private static final class Condenser<P extends Point<P>, N extends AbstractRegionNode<P, N>> {
        /** Flag set to true if the tree was modified during the operation. */
        private boolean modifiedTree;

        /** Condense the nodes in the subtree rooted at the given node. Redundant child nodes are
         * removed. The tree is invalidated if the tree structure was modified.
         * @param node the root node of the subtree to condense
         * @return true if the tree was modified.
         */
        boolean condense(final N node) {
            modifiedTree = false;

            condenseRecursive(node);

            return modifiedTree;
        }

        /** Recursively condense nodes that have children with homogenous location attributes
         * (eg, both inside, both outside) into single nodes.
         * @param node the root of the subtree to condense
         * @return the location of the successfully condensed subtree or null if no condensing was
         *      able to be performed
         */
        private RegionLocation condenseRecursive(final N node) {
            if (node.isLeaf()) {
                return node.getLocation();
            }

            final RegionLocation minusLocation = condenseRecursive(node.getMinus());
            final RegionLocation plusLocation = condenseRecursive(node.getPlus());

            if (minusLocation != null && plusLocation != null && minusLocation == plusLocation) {
                node.setLocationValue(minusLocation);
                node.clearCut();

                modifiedTree = true;

                return minusLocation;
            }

            return null;
        }
    }

    /** Class that iterates over the boundary hyperplane convex subsets from a set of region nodes.
     * @param <P> Point implementation type
     * @param <C> Boundary hyperplane convex subset implementation type
     * @param <N> BSP tree node implementation type
     */
    private static final class RegionBoundaryIterator<
            P extends Point<P>,
            C extends HyperplaneConvexSubset<P>,
            N extends AbstractRegionNode<P, N>>
        extends IteratorTransform<N, C> {

        /** Function that converts from the convex subset type to the output type. */
        private final Function<HyperplaneConvexSubset<P>, C> typeConverter;

        /** Simple constructor.
         * @param inputIterator iterator that will provide all nodes in the tree
         * @param typeConverter function that converts from the convex subset type to the output type
         */
        RegionBoundaryIterator(final Iterator<N> inputIterator,
                final Function<HyperplaneConvexSubset<P>, C> typeConverter) {
            super(inputIterator);

            this.typeConverter = typeConverter;
        }

        /** {@inheritDoc} */
        @Override
        protected void acceptInput(final N input) {
            if (input.isInternal()) {
                final RegionCutBoundary<P> cutBoundary = input.getCutBoundary();

                final HyperplaneSubset<P> outsideFacing = cutBoundary.getOutsideFacing();
                final HyperplaneSubset<P> insideFacing = cutBoundary.getInsideFacing();

                if (outsideFacing != null && !outsideFacing.isEmpty()) {
                    for (HyperplaneConvexSubset<P> boundary : outsideFacing.toConvex()) {

                        addOutput(typeConverter.apply(boundary));
                    }
                }
                if (insideFacing != null && !insideFacing.isEmpty()) {
                    for (HyperplaneConvexSubset<P> boundary : insideFacing.toConvex()) {
                        HyperplaneConvexSubset<P> reversed = boundary.reverse();

                        addOutput(typeConverter.apply(reversed));
                    }
                }
            }
        }
    }
}
