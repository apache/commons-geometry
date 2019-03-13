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
package org.apache.commons.geometry.core.partition;

import java.util.List;

import org.apache.commons.geometry.core.Point;

/** {@link BSPTree} specialized for representing regions of space. For example, this
 * class can be used to represent polygons in Euclidean 2D space and polyhedrons
 * in Euclidean 3D space.
 * @param <P> Point implementation type
 */
public class RegionBSPTree<P extends Point<P>> extends AbstractBSPTree<P, RegionBSPTree.RegionNode<P>> {

    /** Serializable UID */
    private static final long serialVersionUID = 1L;

    public RegionBSPTree() {
        super(RegionNode<P>::new);

        getRoot().setLocation(RegionLocation.INSIDE);
    }

    /** Classify a point with respect to the region.
     * @param point the point to classify
     * @return the classification of the point with respect to the region
     */
    public RegionLocation classify(final P point) {
        return classifyRecursive(getRoot(), point);
    }

    /** Recursively classify a point with respect to the region.
     * @param node the node to classify against
     * @param point the point to classify
     * @return the classification of the point with respect to the region rooted
     *      at the given node
     */
    protected RegionLocation classifyRecursive(final RegionNode<P> node, final P point) {
        if (node.isLeaf()) {
            // the point is in a leaf, so the classification is just the leaf location
            return node.getLocation();
        }
        else {
            final Side side = node.getCutHyperplane().classify(point);

            if (side == Side.MINUS) {
                return classifyRecursive(node.getMinus(), point);
            }
            else if (side == Side.PLUS) {
                return classifyRecursive(node.getPlus(), point);
            }
            else {
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

    /** {@inheritDoc} */
    @Override
    protected RegionBSPTree<P> createTree() {
        return new RegionBSPTree<P>();
    }

    /** Compute the portion of the node's cut subhyperplane that lies on the boundary of
     * the region.
     * @param node the node to compute the cut subhyperplane boundary of
     * @return object representing the portions of the node's cut subhyperplane that lie
     *      on the region's boundary
     */
    protected RegionCutBoundary<P> computeBoundary(final RegionNode<P> node) {
        if (node.isLeaf()) {
            // no boundary for leaf nodes; they are either entirely in or
            // entirely out
            return null;
        }

        ConvexSubHyperplane<P> sub = node.getCut();

        // find the portions of the node cut sub-hyperplane that touch inside and
        // outside cells in the minus sub-tree
        SubHyperplane.Builder<P> minusInBuilder = sub.builder();
        SubHyperplane.Builder<P> minusOutBuilder = sub.builder();

        characterizeSubHyperplane(sub, node.getMinus(), minusInBuilder, minusOutBuilder);

        List<ConvexSubHyperplane<P>> minusIn = minusInBuilder.build().toConvex();
        List<ConvexSubHyperplane<P>> minusOut = minusOutBuilder.build().toConvex();

        // create the result boundary builders
        SubHyperplane.Builder<P> insideFacing = sub.builder();
        SubHyperplane.Builder<P> outsideFacing = sub.builder();

        if (!minusIn.isEmpty()) {
            // Add to the boundary anything that touches an inside cell in the minus sub-tree
            // and an outside cell in the plus sub-tree. These portions are oriented with their
            // plus side pointing to the outside of the region.
            for (ConvexSubHyperplane<P> minusInFragment : minusIn) {
                characterizeSubHyperplane(minusInFragment, node.getPlus(), null, outsideFacing);
            }
        }

        if (!minusOut.isEmpty()) {
            // Add to the boundary anything that touches an outside cell in the minus sub-tree
            // and an inside cell in the plus sub-tree. These portions are oriented with their
            // plus side pointing to the inside of the region.
            for (ConvexSubHyperplane<P> minusOutFragment : minusOut) {
                characterizeSubHyperplane(minusOutFragment, node.getPlus(), insideFacing, null);
            }
        }

        return new RegionCutBoundary<P>(insideFacing.build(), outsideFacing.build());
    }

    /** Recursive method to characterize a convex subhyperplane with respect to the region's
     * boundaries.
     * @param sub the subhyperplane to characterize
     * @param node the node to characterize the subhyperplane against
     * @param in the builder that will receive the portions of the subhyperplane that lie in the inside
     *      of the region; may be null
     * @param out the builder that will receive the portions of the subhyperplane that lie on the outside
     *      of the region; may be null
     */
    protected void characterizeSubHyperplane(final ConvexSubHyperplane<P> sub, final RegionNode<P> node,
            final SubHyperplane.Builder<P> in, final SubHyperplane.Builder<P> out) {

        if (sub != null) {
            if (node.isLeaf()) {
                if (node.isInside() && in != null) {
                    in.add(sub);
                }
                else if (node.isOutside() && out != null) {
                    out.add(sub);
                }
            }
            else {
                ConvexSubHyperplane.Split<P> split = sub.split(node.getCutHyperplane());

                characterizeSubHyperplane(split.getPlus(), node.getPlus(), in, out);
                characterizeSubHyperplane(split.getMinus(), node.getMinus(), in, out);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void initChildNode(final RegionNode<P> parent, final RegionNode<P> child, final boolean isPlus) {
        super.initChildNode(parent, child, isPlus);

        child.setLocation(isPlus ? RegionLocation.OUTSIDE : RegionLocation.INSIDE);
    }

    /** {@link BSPTree.Node} implementation for use with {@link RegionBSPTree}s.
     * @param <P> Point implementation type
     */
    public static class RegionNode<P extends Point<P>> extends AbstractBSPTree.AbstractNode<P, RegionNode<P>> {

        /** Serializable UID */
        private static final long serialVersionUID = 1L;

        /** The location for the node. This will only be set on leaf nodes. */
        private RegionLocation location;

        /** Object representing the part of the node cut subhyperplane that lies on the
         * region boundary. This is calculated lazily and is only present on internal nodes.
         */
        private RegionCutBoundary<P> cutBoundary;

        /** Simple constructor.
         * @param tree owning tree instance
         */
        protected RegionNode(AbstractBSPTree<P, RegionNode<P>> tree) {
            super(tree);
        }

        /** {@inheritDoc} */
        @Override
        public RegionBSPTree<P> getTree() {
            // cast to our parent tree type
            return (RegionBSPTree<P>) super.getTree();
        }

        /** Get the location of the node. This value will only be non-null for
         * leaf nodes.
         * @return the location of the node; will be null for internal nodes
         */
        public RegionLocation getLocation() {
            return isLeaf() ? location : null;
        }

        /** True if the node is a leaf node and has a location of {@link RegionLocation#INSIDE}.
         * @return true if the node is a leaf node and has a location of
         *      {@link RegionLocation#INSIDE}
         */
        public boolean isInside() {
            return location == RegionLocation.INSIDE;
        }

        /** True if the node is a leaf node and has a location of {@link RegionLocation#OUTSIDE}.
         * @return true if the node is a leaf node and has a location of
         *      {@link RegionLocation#OUTSIDE}
         */
        public boolean isOutside() {
            return location == RegionLocation.OUTSIDE;
        }

        /** Get the portion of the node's cut subhyperplane that lies on the boundary of the
         * region.
         * @return the portion of the node's cut subhyperplane that lies on the boundary of
         *      the region
         */
        public RegionCutBoundary<P> getCutBoundary() {
            if (!isLeaf()) {
                checkTreeUpdates();

                if (cutBoundary == null) {
                    cutBoundary = getTree().computeBoundary(this);
                }
            }

            return cutBoundary;
        }

        /** {@inheritDoc} */
        @Override
        protected void treeUpdated() {
            super.treeUpdated();

            // null any computed boundary value since it is no longer valid
            cutBoundary = null;
        }

        /** Set the location attribute for the node.
         * @param location the location attribute for the node
         */
        protected void setLocation(final RegionLocation location) {
            this.location = location;
        }

        /** {@inheritDoc} */
        @Override
        protected RegionNode<P> getSelf() {
            return this;
        }
    }
}
