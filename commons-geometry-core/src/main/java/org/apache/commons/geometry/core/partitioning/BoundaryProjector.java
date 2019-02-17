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
package org.apache.commons.geometry.core.partitioning;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.partitioning.Region.Location;

/** Local tree visitor to compute projection on boundary.
 * @param <P> Point type defining the space
 * @param <S> Point type defining the sub-space
 */
class BoundaryProjector<P extends Point<P>, S extends Point<S>> implements BSPTreeVisitor_Old<P> {

    /** Original point. */
    private final P original;

    /** Current best projected point. */
    private P projected;

    /** Leaf node closest to the test point. */
    private BSPTree_Old<P> leaf;

    /** Current offset. */
    private double offset;

    /** Simple constructor.
     * @param original original point
     */
    BoundaryProjector(final P original) {
        this.original  = original;
        this.projected = null;
        this.leaf      = null;
        this.offset    = Double.POSITIVE_INFINITY;
    }

    /** {@inheritDoc} */
    @Override
    public Order visitOrder(final BSPTree_Old<P> node) {
        // we want to visit the tree so that the first encountered
        // leaf is the one closest to the test point
        if (node.getCut().getHyperplane().getOffset(original) <= 0) {
            return Order.MINUS_SUB_PLUS;
        } else {
            return Order.PLUS_SUB_MINUS;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void visitInternalNode(final BSPTree_Old<P> node) {

        // project the point on the cut sub-hyperplane
        final Hyperplane<P> hyperplane = node.getCut().getHyperplane();
        final double signedOffset = hyperplane.getOffset(original);
        if (Math.abs(signedOffset) < offset) {

            // project point
            final P regular = hyperplane.project(original);

            // get boundary parts
            final List<Region<S>> boundaryParts = boundaryRegions(node);

            // check if regular projection really belongs to the boundary
            boolean regularFound = false;
            for (final Region<S> part : boundaryParts) {
                if (!regularFound && belongsToPart(regular, hyperplane, part)) {
                    // the projected point lies in the boundary
                    projected    = regular;
                    offset       = Math.abs(signedOffset);
                    regularFound = true;
                }
            }

            if (!regularFound) {
                // the regular projected point is not on boundary,
                // so we have to check further if a singular point
                // (i.e. a vertex in 2D case) is a possible projection
                for (final Region<S> part : boundaryParts) {
                    final P spI = singularProjection(regular, hyperplane, part);
                    if (spI != null) {
                        final double distance = original.distance(spI);
                        if (distance < offset) {
                            projected = spI;
                            offset    = distance;
                        }
                    }
                }

            }

        }

    }

    /** {@inheritDoc} */
    @Override
    public void visitLeafNode(final BSPTree_Old<P> node) {
        if (leaf == null) {
            // this is the first leaf we visit,
            // it is the closest one to the original point
            leaf = node;
        }
    }

    /** Get the projection.
     * @return projection
     */
    public BoundaryProjection<P> getProjection() {

        // fix offset sign
        offset = Math.copySign(offset, (Boolean) leaf.getAttribute() ? -1 : +1);

        return new BoundaryProjection<>(original, projected, offset);

    }

    /** Extract the regions of the boundary on an internal node.
     * @param node internal node
     * @return regions in the node sub-hyperplane
     */
    private List<Region<S>> boundaryRegions(final BSPTree_Old<P> node) {

        final List<Region<S>> regions = new ArrayList<>(2);

        @SuppressWarnings("unchecked")
        final BoundaryAttribute<P> ba = (BoundaryAttribute<P>) node.getAttribute();
        addRegion(ba.getPlusInside(),  regions);
        addRegion(ba.getPlusOutside(), regions);

        return regions;

    }

    /** Add a boundary region to a list.
     * @param sub sub-hyperplane defining the region
     * @param list to fill up
     */
    private void addRegion(final SubHyperplane<P> sub, final List<Region<S>> list) {
        if (sub != null) {
            @SuppressWarnings("unchecked")
            final Region<S> region = ((AbstractSubHyperplane<P, S>) sub).getRemainingRegion();
            if (region != null) {
                list.add(region);
            }
        }
    }

    /** Check if a projected point lies on a boundary part.
     * @param point projected point to check
     * @param hyperplane hyperplane into which the point was projected
     * @param part boundary part
     * @return true if point lies on the boundary part
     */
    private boolean belongsToPart(final P point, final Hyperplane<P> hyperplane,
                                  final Region<S> part) {

        // there is a non-null sub-space, we can dive into smaller dimensions
        @SuppressWarnings("unchecked")
        final Embedding<P, S> embedding = (Embedding<P, S>) hyperplane;
        return part.checkPoint(embedding.toSubSpace(point)) != Location.OUTSIDE;

    }

    /** Get the projection to the closest boundary singular point.
     * @param point projected point to check
     * @param hyperplane hyperplane into which the point was projected
     * @param part boundary part
     * @return projection to a singular point of boundary part (may be null)
     */
    private P singularProjection(final P point, final Hyperplane<P> hyperplane,
                                        final Region<S> part) {

        // there is a non-null sub-space, we can dive into smaller dimensions
        @SuppressWarnings("unchecked")
        final Embedding<P, S> embedding = (Embedding<P, S>) hyperplane;
        final BoundaryProjection<S> bp = part.projectToBoundary(embedding.toSubSpace(point));

        // back to initial dimension
        return (bp.getProjected() == null) ? null : embedding.toSpace(bp.getProjected());

    }

}
