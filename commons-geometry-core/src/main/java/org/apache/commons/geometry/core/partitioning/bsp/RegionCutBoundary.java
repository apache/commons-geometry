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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.Sized;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;

/** Class representing the portion of an
 * {@link AbstractRegionBSPTree.AbstractRegionNode AbstractRegionNode}'s cut that
 * lies on the boundary of the region. Portions of the node cut may be oriented so
 * that the plus side of the cut points toward the outside of the region
 * ({@link #getOutsideFacing()}) and other portions toward the inside of the
 * region ({@link #getInsideFacing()}). The inside-facing and outside-facing portions
 * of the region boundary are represented as lists of disjoint hyperplane convex subsets,
 * all originating from the same hyperplane convex subset forming the node cut.
 *
 * @param <P> Point implementation type
 */
public final class RegionCutBoundary<P extends Point<P>> implements Sized {

    /** Portion of the cut oriented such that the plus side of the cut points to the inside of the region. */
    private final List<HyperplaneConvexSubset<P>> insideFacing;

    /** Portion of the cut oriented such that the plus side of the cut points to the outside of the region. */
    private final List<HyperplaneConvexSubset<P>> outsideFacing;

    /** Construct a new instance from the inside-facing and outside-facing portions of a node cut. The
     * given lists are expected to be disjoint regions originating from the same hyperplane convex subset.
     * No validation is performed.
     * @param insideFacing the inside-facing portion of the node cut
     * @param outsideFacing the outside-facing portion of the node cut
     */
    RegionCutBoundary(final List<HyperplaneConvexSubset<P>> insideFacing,
            final List<HyperplaneConvexSubset<P>> outsideFacing) {
        this.insideFacing = insideFacing != null ?
                Collections.unmodifiableList(insideFacing) :
                Collections.emptyList();

        this.outsideFacing = outsideFacing != null ?
                Collections.unmodifiableList(outsideFacing) :
                Collections.emptyList();
    }

    /** Get the portion of the cut with its plus side facing the inside of the region.
     * @return the portion of the cut with its plus side facing the
     *      inside of the region
     */
    public List<HyperplaneConvexSubset<P>> getInsideFacing() {
        return insideFacing;
    }

    /** Get the portion of the cut with its plus side facing the outside of the region.
     * @return the portion of the cut with its plus side facing the
     *      outside of the region
     */
    public List<HyperplaneConvexSubset<P>> getOutsideFacing() {
        return outsideFacing;
    }

    /** Get the total size of the cut boundary, including inside and outside facing components.
     * @return the total size of the cut boundary, including inside and outside facing components
     */
    @Override
    public double getSize() {
        return getTotalSize(insideFacing) + getTotalSize(outsideFacing);
    }

    /** Get the total size of all boundaries in the given list.
     * @param boundaries boundaries to compute the size for
     * @return the total size of all boundaries in the given list
     */
    private double getTotalSize(final List<? extends HyperplaneConvexSubset<P>> boundaries) {
        double total = 0.0;
        for (final HyperplaneConvexSubset<P> boundary : boundaries) {
            total += boundary.getSize();

            if (Double.isInfinite(total)) {
                return total;
            }
        }

        return total;
    }

    /** Return the closest point to the argument in the inside and outside facing
     * portions of the cut boundary.
     * @param pt the reference point
     * @return the point in the cut boundary closest to the reference point
     * @see HyperplaneConvexSubset#closest(Point)
     */
    public P closest(final P pt) {
        P closest = null;
        double closestDist = Double.POSITIVE_INFINITY;

        final Iterator<HyperplaneConvexSubset<P>> insideIt = insideFacing.iterator();
        final Iterator<HyperplaneConvexSubset<P>> outsideIt = outsideFacing.iterator();

        HyperplaneConvexSubset<P> boundary;
        P testPt;
        double dist;

        while (insideIt.hasNext() || outsideIt.hasNext()) {
            boundary = insideIt.hasNext() ?
                    insideIt.next() :
                    outsideIt.next();

            testPt = boundary.closest(pt);
            dist = pt.distance(testPt);

            if (closest == null || dist < closestDist) {
                closest = testPt;
                closestDist = dist;
            }
        }

        return closest;
    }

    /** Return true if the given point is contained in the boundary, in either the
     * inside facing portion or the outside facing portion.
     * @param pt point to test
     * @return true if the point is contained in the boundary
     * @see HyperplaneConvexSubset#contains(Point)
     */
    public boolean contains(final P pt) {
        return containsInsideFacing(pt) || containsOutsideFacing(pt);
    }

    /** Return true if the given point is contained in the inside-facing portion of
     * the region boundary.
     * @param pt point to test
     * @return true if the point is contained in the inside-facing portion of the region
     *      boundary
     */
    public boolean containsInsideFacing(final P pt) {
        return anyContains(pt, insideFacing);
    }

    /** Return true if the given point is contained in the outside-facing portion of the
     * region boundary.
     * @param pt point to test
     * @return true if the point is contained in the outside-facing portion of the region
     *      boundary
     */
    public boolean containsOutsideFacing(final P pt) {
        return anyContains(pt, outsideFacing);
    }

    /** Return true if the point is contained in any of the given boundaries.
     * @param pt point to test
     * @param boundaries
     * @return true if the point is contained in any of the given boundaries
     */
    private boolean anyContains(final P pt, final List<? extends HyperplaneConvexSubset<P>> boundaries) {
        for (final HyperplaneConvexSubset<P> boundary : boundaries) {
            if (boundary.contains(pt)) {
                return true;
            }
        }

        return false;
    }
}
