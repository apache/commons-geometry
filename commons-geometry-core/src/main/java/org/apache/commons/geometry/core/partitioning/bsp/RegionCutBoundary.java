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

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.partitioning.HyperplaneSubset;

/** Class representing the portion of an
 * {@link AbstractRegionBSPTree.AbstractRegionNode AbstractRegionNode}'s cut that
 * lies on the boundary of the region. Portions of this hyperplane subset
 * may be oriented so that the plus side of the hyperplane subset points toward
 * the outside of the region ({@link #getOutsideFacing()}) and other portions
 * of the same hyperplane subset may be oriented so that the plus side points
 * toward the inside of the region ({@link #getInsideFacing()}).
 *
 * @param <P> Point implementation type
 */
public final class RegionCutBoundary<P extends Point<P>> {
    /** Portion of the cut with its plus side facing the inside of the region.
     */
    private final HyperplaneSubset<P> insideFacing;

    /** Portion of the cut with its plus side facing the outside of the region.
     */
    private final HyperplaneSubset<P> outsideFacing;

    /** Simple constructor.
     * @param insideFacing portion of the cut with its plus side facing the
     *      inside of the region
     * @param outsideFacing portion of the cut with its plus side facing the
     *      outside of the region
     */
    public RegionCutBoundary(final HyperplaneSubset<P> insideFacing, final HyperplaneSubset<P> outsideFacing) {
        this.insideFacing = insideFacing;
        this.outsideFacing = outsideFacing;
    }

    /** Get the portion of the cut with its plus side facing the inside of the region.
     * @return the portion of the cut with its plus side facing the
     *      inside of the region
     */
    public HyperplaneSubset<P> getInsideFacing() {
        return insideFacing;
    }

    /** Get the portion of the cut with its plus side facing the outside of the region.
     * @return the portion of the cut with its plus side facing the
     *      outside of the region
     */
    public HyperplaneSubset<P> getOutsideFacing() {
        return outsideFacing;
    }

    /** Return the closest point to the argument in the inside and outside facing
     * portions of the cut boundary.
     * @param pt the reference point
     * @return the point in the cut boundary closest to the reference point
     * @see HyperplaneSubset#closest(Point)
     */
    public P closest(final P pt) {
        final P insideFacingPt = (insideFacing != null) ? insideFacing.closest(pt) : null;
        final P outsideFacingPt = (outsideFacing != null) ? outsideFacing.closest(pt) : null;

        if (insideFacingPt != null && outsideFacingPt != null) {
            if (pt.distance(insideFacingPt) < pt.distance(outsideFacingPt)) {
                return insideFacingPt;
            }
            return outsideFacingPt;
        } else if (insideFacingPt != null) {
            return insideFacingPt;
        }
        return outsideFacingPt;
    }

    /** Return true if the given point is contained in the boundary, in either the
     * inside facing portion or the outside facing portion.
     * @param pt point to test
     * @return true if the point is contained in the boundary
     * @see HyperplaneSubset#contains(Point)
     */
    public boolean contains(final P pt) {
        return (insideFacing != null && insideFacing.contains(pt)) ||
                (outsideFacing != null && outsideFacing.contains(pt));
    }
}
