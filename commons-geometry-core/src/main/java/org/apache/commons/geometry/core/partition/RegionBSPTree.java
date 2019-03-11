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
 * @param <P> Point type
 */
public class RegionBSPTree<P extends Point<P>> extends AbstractBSPTree<P, RegionBSPTree.RegionNode<P>> {

    /** Serializable UID */
    private static final long serialVersionUID = 1L;

    public RegionBSPTree() {
        super(RegionNode<P>::new);
    }

    /** {@inheritDoc} */
    @Override
    protected RegionBSPTree<P> createTree() {
        return new RegionBSPTree<P>();
    }

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

        // create the boundary builder
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

    public static class RegionNode<P extends Point<P>> extends AbstractBSPTree.AbstractNode<P, RegionNode<P>> {

        /** Serializable UID */
        private static final long serialVersionUID = 1L;

        private RegionLocation location;

        private RegionCutBoundary<P> cutBoundary;

        /** Simple constructor.
         * @param tree owning tree instance
         */
        protected RegionNode(AbstractBSPTree<P, RegionNode<P>> tree) {
            super(tree);
        }

        public RegionLocation getLocation() {
            return location;
        }

        public boolean isInside() {
            return location == RegionLocation.INSIDE;
        }

        public boolean isOutside() {
            return location == RegionLocation.OUTSIDE;
        }

        public RegionCutBoundary<P> getCutBoundary() {
            if (!isLeaf() && cutBoundary == null) {
                cutBoundary = ((RegionBSPTree<P>) getTree()).computeBoundary(this);
            }
            return cutBoundary;
        }

        /** {@inheritDoc} */
        @Override
        protected RegionNode<P> getSelf() {
            return this;
        }
    }
}
