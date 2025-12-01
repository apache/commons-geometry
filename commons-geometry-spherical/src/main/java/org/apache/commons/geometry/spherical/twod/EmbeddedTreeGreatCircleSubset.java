/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.spherical.twod;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.spherical.oned.CutAngle;
import org.apache.commons.geometry.spherical.oned.CutAngles;
import org.apache.commons.geometry.spherical.oned.RegionBSPTree1S;

/** Class representing an arbitrary subset of the points on a great circle using a
 * {@link RegionBSPTree1S}. This class can represent convex, non-convex, and empty regions.
 *
 * <p>This class is mutable and <em>not</em> thread safe.</p>
 */
public final class EmbeddedTreeGreatCircleSubset extends GreatCircleSubset {
    /** The 1D region on the great circle. */
    private final RegionBSPTree1S region;

    /** Construct a new, empty hyperplane subset for the given great circle.
     * @param greatCircle great circle defining this instance
     */
    public EmbeddedTreeGreatCircleSubset(final GreatCircle greatCircle) {
        this(greatCircle, false);
    }

    /** Construct a new sub-region for the given great circle. If {@code full}
     * is true, then the region will cover the entire circle; otherwise,
     * it will be empty.
     * @param circle great circle that the sub-region will belong to
     * @param full if true, the sub-region will cover the entire circle;
     *      otherwise it will be empty
     */
    public EmbeddedTreeGreatCircleSubset(final GreatCircle circle, final boolean full) {
        this(circle, new RegionBSPTree1S(full));
    }

    /** Construct a new instance from its defining great circle and subspace region.
     * @param circle great circle that the sub-region will belong to
     * @param region subspace region
     */
    public EmbeddedTreeGreatCircleSubset(final GreatCircle circle, final RegionBSPTree1S region) {
        super(circle);

        this.region = region;
    }

    /** {@inheritDoc} */
    @Override
    public RegionBSPTree1S getSubspaceRegion() {
        return region;
    }

    /** {@inheritDoc} */
    @Override
    public EmbeddedTreeGreatCircleSubset transform(final Transform<Point2S> transform) {
        final GreatCircle circle = getCircle().transform(transform);

        return new EmbeddedTreeGreatCircleSubset(circle, region.copy());
    }

    /** {@inheritDoc} */
    @Override
    public List<GreatArc> toConvex() {
        return region.toIntervals().stream()
                .flatMap(i -> i.toConvex().stream())
                .map(i -> GreatCircles.arcFromInterval(getCircle(), i))
                .collect(Collectors.toList());
    }

    /** {@inheritDoc}
     *
     * <p>In all cases, the current instance is not modified. However, In order to avoid
     * unnecessary copying, this method will use the current instance as the split value when
     * the instance lies entirely on the plus or minus side of the splitter. For example, if
     * this instance lies entirely on the minus side of the splitter, the sub great circle
     * returned by {@link Split#getMinus()} will be this instance. Similarly, {@link Split#getPlus()}
     * will return the current instance if it lies entirely on the plus side. Callers need to make
     * special note of this, since this class is mutable.</p>
     */
    @Override
    public Split<EmbeddedTreeGreatCircleSubset> split(final Hyperplane<Point2S> splitter) {

        final GreatCircle splitterCircle = (GreatCircle) splitter;
        final GreatCircle thisCircle = getCircle();

        final Point2S intersection = splitterCircle.intersection(thisCircle);

        EmbeddedTreeGreatCircleSubset minus = null;
        EmbeddedTreeGreatCircleSubset plus = null;

        if (intersection != null) {
            final CutAngle subSplitter = CutAngles.createPositiveFacing(
                    thisCircle.toSubspace(intersection), splitterCircle.getPrecision());

            final Split<RegionBSPTree1S> subSplit = region.splitDiameter(subSplitter);
            final SplitLocation subLoc = subSplit.getLocation();

            if (subLoc == SplitLocation.MINUS) {
                minus = this;
            } else if (subLoc == SplitLocation.PLUS) {
                plus = this;
            } else if (subLoc == SplitLocation.BOTH) {
                minus = new EmbeddedTreeGreatCircleSubset(thisCircle, subSplit.getMinus());
                plus =  new EmbeddedTreeGreatCircleSubset(thisCircle, subSplit.getPlus());
            }
        }

        return new Split<>(minus, plus);
    }

    /** Add an arc to this instance.
     * @param arc arc to add
     * @throws IllegalArgumentException if the given arc is not from
     *      a great circle equivalent to this instance
     */
    public void add(final GreatArc arc) {
        GreatCircles.validateGreatCirclesEquivalent(getCircle(), arc.getCircle());

        region.add(arc.getSubspaceRegion());
    }

    /** Add the region represented by the given subcircle to this instance.
     * The argument is not modified.
     * @param subcircle subcircle to add
     * @throws IllegalArgumentException if the given subcircle is not from
     *      a great circle equivalent to this instance
     */
    public void add(final EmbeddedTreeGreatCircleSubset subcircle) {
        GreatCircles.validateGreatCirclesEquivalent(getCircle(), subcircle.getCircle());

        region.union(subcircle.getSubspaceRegion());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(256);
        sb.append(this.getClass().getSimpleName())
            .append("[circle= ")
            .append(getCircle())
            .append(", region= ")
            .append(region)
            .append(']');

        return sb.toString();
    }
}
