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
package org.apache.commons.geometry.euclidean.threed;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.RegionBSPTree1D;
import org.apache.commons.geometry.euclidean.threed.Line3D.SubspaceTransform;

/** Class representing an arbitrary region of a 3 dimensional line. This class can represent
 * both convex and non-convex regions of its underlying line.
 *
 * <p>This class is mutable and <em>not</em> thread safe.</p>
 */
public final class SubLine3D extends AbstractSubLine3D<RegionBSPTree1D> {

    /** Serializable UID */
    private static final long serialVersionUID = 20190702L;

    /** The 1D region representing the area on the line */
    private final RegionBSPTree1D region;

    /** Construct a new, empty subline for the given line.
     * @param line line defining the subline
     */
    public SubLine3D(final Line3D line) {
        this(line, false);
    }

    /** Construct a new subline for the given line. If {@code full}
     * is true, then the subline will cover the entire line; otherwise,
     * it will be empty.
     * @param line line defining the subline
     * @param full if true, the subline will cover the entire space;
     *      otherwise it will be empty
     */
    public SubLine3D(final Line3D line, boolean full) {
        this(line, new RegionBSPTree1D(full));
    }

    /** Construct a new instance from its defining line and subspace region.
     * @param line line defining the subline
     * @param region subspace region for the subline
     */
    public SubLine3D(final Line3D line, final RegionBSPTree1D region) {
        super(line);

        this.region = region;
    }

    /** Transform this instance.
     * @param transform the transform to apply
     * @return a new, transformed instance
     */
    public SubLine3D transform(final Transform<Vector3D> transform) {
        final SubspaceTransform st = getLine().subspaceTransform(transform);

        final RegionBSPTree1D tRegion = RegionBSPTree1D.empty();
        tRegion.copy(region);
        tRegion.transform(st.getTransform());

        return new SubLine3D(st.getLine(), tRegion);
    }

    /** Return a list of {@link Segment3D} instances representing the same region
     * as this subline.
     * @return a list of {@link Segment3D} instances representing the same region
     *      as this instance.
     */
    public List<Segment3D> toConvex() {
        final List<Interval> intervals = region.toIntervals();

        final Line3D line = getLine();
        final List<Segment3D> segments = new ArrayList<>(intervals.size());

        for (Interval interval : intervals) {
            segments.add(Segment3D.fromInterval(line, interval));
        }

        return segments;
    }

    /** {@inheritDoc} */
    @Override
    public RegionBSPTree1D getSubspaceRegion() {
        return region;
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
