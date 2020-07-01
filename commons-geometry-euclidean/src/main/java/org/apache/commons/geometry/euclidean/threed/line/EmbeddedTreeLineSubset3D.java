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
package org.apache.commons.geometry.euclidean.threed.line;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.RegionBSPTree1D;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.threed.Bounds3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.line.Line3D.SubspaceTransform;

/** Class representing an arbitrary subset of a line in 3D Euclidean space using a
 * {@link RegionBSPTree1D}. This class can represent convex, non-convex, finite,
 * infinite, and empty regions.
 *
 * <p>This class is mutable and <em>not</em> thread safe.</p>
 */
public final class EmbeddedTreeLineSubset3D extends LineSubset3D {
    /** The 1D region representing the area on the line. */
    private final RegionBSPTree1D region;

    /** Construct a new, empty subset for the given line.
     * @param line line defining the subset
     */
    public EmbeddedTreeLineSubset3D(final Line3D line) {
        this(line, false);
    }

    /** Construct a new subset for the given line. If {@code full}
     * is true, then the subset will cover the entire line; otherwise,
     * it will be empty.
     * @param line line defining the subset
     * @param full if true, the subset will cover the entire space;
     *      otherwise it will be empty
     */
    public EmbeddedTreeLineSubset3D(final Line3D line, final boolean full) {
        this(line, new RegionBSPTree1D(full));
    }

    /** Construct a new instance from its defining line and subspace region.
     * @param line line defining the subset
     * @param region subspace region for the subset
     */
    public EmbeddedTreeLineSubset3D(final Line3D line, final RegionBSPTree1D region) {
        super(line);

        this.region = region;
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        return region.getSize();
    }

    /** {@inheritDoc} */
    @Override
    public RegionBSPTree1D getSubspaceRegion() {
        return region;
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D getCentroid() {
        final Vector1D subcenter = region.getCentroid();
        return subcenter != null ?
                getLine().toSpace(subcenter) :
                null;
    }

    /** {@inheritDoc} */
    @Override
    public Bounds3D getBounds() {
        final double min = region.getMin();
        final double max = region.getMax();

        if (Double.isFinite(min) && Double.isFinite(max)) {
            final Line3D line = getLine();

            return Bounds3D.builder()
                    .add(line.toSpace(min))
                    .add(line.toSpace(max))
                    .build();
        }

        return null;
    }

    /** Transform this instance.
     * @param transform the transform to apply
     * @return a new, transformed instance
     */
    public EmbeddedTreeLineSubset3D transform(final Transform<Vector3D> transform) {
        final SubspaceTransform st = getLine().subspaceTransform(transform);

        final RegionBSPTree1D tRegion = RegionBSPTree1D.empty();
        tRegion.copy(region);
        tRegion.transform(st.getTransform());

        return new EmbeddedTreeLineSubset3D(st.getLine(), tRegion);
    }

    /** Return a list of {@link LineConvexSubset3D} instances representing the same region
     * as this instance.
     * @return a list of {@link LineConvexSubset3D} instances representing the same region
     *      as this instance.
     */
    public List<LineConvexSubset3D> toConvex() {
        final List<Interval> intervals = region.toIntervals();

        final Line3D line = getLine();
        final List<LineConvexSubset3D> convex = new ArrayList<>(intervals.size());

        for (final Interval interval : intervals) {
            convex.add(Lines3D.subsetFromInterval(line, interval));
        }

        return convex;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final Line3D line = getLine();

        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName())
            .append('[')
            .append("lineOrigin= ")
            .append(line.getOrigin())
            .append(", lineDirection= ")
            .append(line.getDirection())
            .append(", region= ")
            .append(region)
            .append(']');

        return sb.toString();
    }
}
