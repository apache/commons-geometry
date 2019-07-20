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
package org.apache.commons.geometry.euclidean.twod;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.exception.GeometryException;
import org.apache.commons.geometry.core.partition.ConvexHyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partition.Hyperplane;
import org.apache.commons.geometry.core.partition.HyperplaneLocation;
import org.apache.commons.geometry.core.partition.Split;
import org.apache.commons.geometry.core.partition.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** Class representing a finite or infinite convex area in Euclidean 2D space.
 * The boundaries of this area, if any, are composed of line segments.
 */
public final class ConvexArea implements ConvexHyperplaneBoundedRegion<Vector2D>, Serializable {

    /** Serializable UID */
    private static final long serialVersionUID = 20190619L;

    /** Instance representing the full 2D plane. */
    private static final ConvexArea FULL = new ConvexArea(Collections.emptyList());

    /** Line segments forming the boundaries of the area. */
    private final List<Segment> boundarySegments;

    /** Simple constructor. Callers are responsible for ensuring that the given path
     * represents the boundary of a convex area. No validation is performed.
     * @param boundaryPath the boundary of the convex area
     */
    private ConvexArea(final List<Segment> boundarySegments) {
        this.boundarySegments = boundarySegments;
    }

    /** Get the list of line segments comprising the boundary of the area. The segments
     * are oriented so that their minus sides point toward the interior of the region.
     * The returned list will be empty if the instance represents the full area.
     * @return list of line segments comprising the boundary of the area.
     */
    public List<Segment> getBoundarySegments() {
        return boundarySegments;
    }

    /** Get the connected line segment paths comprising the boundary of the area. The
     * segments are oriented so that their minus sides point toward the interior of the
     * region. The size of the returned list is
     * <ul>
     *      <li><strong>0</strong> if the convex area is full,</li>
     *      <li><strong>1</strong> if at least one boundary is present and
     *          a single path can connect all segments (this will be the case
     *          for most instances), and</li>
     *      <li><strong>2</strong> if only two boundaries exist and they are
     *          parallel to each other (in which case they cannot be connected
     *          as a single path).</li>
     * </ul>
     * @return the line segment paths comprising the boundary of the area.
     */
    public List<SegmentPath> getBoundaryPaths() {
        return InteriorAngleSegmentConnector.connectMinimized(boundarySegments);
    }

    /** Transform this instance using the given {@link Transform}.
     * @return a new transformed convex area
     */
    public ConvexArea transform(final Transform<Vector2D> transform) {
        if (isFull()) {
            return this;
        }

        final int size = boundarySegments.size();
        final List<Segment> tSegments = new ArrayList<>(size);

        // determine if the lines should be flipped
        Segment seg = boundarySegments.get(0);
        Segment tSeg = seg.transform(transform);

        final Vector2D plusPt = seg.getLine().plusPoint();
        final boolean reverseDirection = tSeg.getLine().classify(transform.apply(plusPt)) == HyperplaneLocation.MINUS;

        // transform all of the segments
        if (reverseDirection) {
            tSeg = tSeg.reverse();
        }
        tSegments.add(tSeg);

        for (int i=1; i<boundarySegments.size(); ++i) {
            tSeg = boundarySegments.get(i).transform(transform);

            if (reverseDirection) {
                tSeg = tSeg.reverse();
            }

            tSegments.add(tSeg);
        }

        return new ConvexArea(tSegments);
    }

    /** {@inheritDoc} */
    @Override
    public List<ConvexArea> toConvex() {
        return Arrays.asList(this);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFull() {
        // no boundaries => no outside
        return boundarySegments.isEmpty();
    }

    /** {@inheritDoc}
     *
     * <p>This method always returns false.</p>
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        if (isFull()) {
            return Double.POSITIVE_INFINITY;
        }

        double quadrilateralAreaSum = 0.0;

        for (Segment segment : boundarySegments) {
            if (segment.isInfinite()) {
                return Double.POSITIVE_INFINITY;
            }

            quadrilateralAreaSum += segment.getStartPoint().signedArea(segment.getEndPoint());
        }

        return 0.5 * quadrilateralAreaSum;
    }

    /** {@inheritDoc} */
    @Override
    public double getBoundarySize() {
        double sum = 0.0;
        for (Segment seg : boundarySegments) {
            sum += seg.getSize();
        }

        return sum;
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D getBarycenter() {
        if (!boundarySegments.isEmpty()) {
            double quadrilateralAreaSum = 0.0;
            double scaledSumX = 0.0;
            double scaledSumY = 0.0;

            double signedArea;
            Vector2D startPoint;
            Vector2D endPoint;

            for (Segment seg : boundarySegments) {
                if (seg.isInfinite()) {
                    // infinite => no barycenter
                    return null;
                }

                startPoint = seg.getStartPoint();
                endPoint = seg.getEndPoint();

                signedArea = startPoint.signedArea(endPoint);

                quadrilateralAreaSum += signedArea;

                scaledSumX += signedArea * (startPoint.getX() + endPoint.getX());
                scaledSumY += signedArea * (startPoint.getY() + endPoint.getY());
            }

            return Vector2D.of(scaledSumX, scaledSumY).multiply(1.0 / (3.0 * quadrilateralAreaSum));
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public RegionLocation classify(Vector2D pt) {
        boolean isOn = false;

        HyperplaneLocation loc;
        for (Segment seg : boundarySegments) {
            loc = seg.getLine().classify(pt);

            if (loc == HyperplaneLocation.PLUS) {
                return RegionLocation.OUTSIDE;
            }
            else if (loc == HyperplaneLocation.ON) {
                isOn = true;
            }
        }

        return isOn ? RegionLocation.BOUNDARY : RegionLocation.INSIDE;
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D project(Vector2D pt) {

        Vector2D projected;
        double distSq;

        Vector2D closestPt = null;
        double closestDistSq = Double.POSITIVE_INFINITY;

        for (Segment seg : boundarySegments) {
            projected = seg.closest(pt);
            distSq = pt.distanceSq(projected);

            if (projected != null && (closestPt == null || distSq < closestDistSq)) {
                closestPt = projected;
                closestDistSq = distSq;
            }
        }

        return closestPt;
    }

    /** Trim the given line segment to the region. The returned segment lies
     * entirely inside the region. Null is returned if the segment does not
     * intersect the region.
     * @param segment segment to trim
     * @return the portion of the given segment that lies inside the region or
     *      null if the segment does not intersect the region
     */
    public Segment trim(final Segment segment) {
        Segment remaining = segment;
        for (Segment boundary : boundarySegments) {
            remaining = remaining.split(boundary.getLine()).getMinus();
            if (remaining == null) {
                break;
            }
        }

        return remaining;
    }

    @Override
    public Split<ConvexArea> split(final Hyperplane<Vector2D> splitter) {
        final Line splitterLine = (Line) splitter;

        if (isFull()) {
            return splitFull(splitterLine);
        }
        return splitBounded(splitterLine);
    }

    /** Method called to split the current instance when the instance represents
     * the full space.
     * @param splitter line to split the instance with
     * @return the split instance
     */
    private Split<ConvexArea> splitFull(final Line splitter) {
        final ConvexArea minus = new ConvexArea(Arrays.asList(splitter.span()));
        final ConvexArea plus = new ConvexArea(Arrays.asList(splitter.reverse().span()));

        return new Split<>(minus, plus);
    }

    /** Method called to split the current instance when the instance contains at least
     * one boundary.
     * @param splitter line to split the instance with
     * @return the split instance
     */
    private Split<ConvexArea> splitBounded(final Line splitter) {
        final Segment trimmedSplitter = trim(splitter.span());

        if (trimmedSplitter == null) {
            // The splitter lies entirely outside of the region; we need
            // to determine whether we lie on the plus or minus side of the splitter.
            // We can use the first segment to determine this. If the segment is entirely
            // on the minus side of the splitter or lies directly on the splitter and has
            // the same orientation, then the area lies on the minus side of the splitter.
            // Otherwise, it lies on the plus side.
            Segment testSegment = boundarySegments.get(0);
            SplitLocation testLocation = testSegment.split(splitter).getLocation();

            if (SplitLocation.MINUS == testLocation ||
                    (SplitLocation.NEITHER == testLocation && splitter.similarOrientation(testSegment.getLine()))) {
                return new Split<>(this, null);
            }

            return new Split<>(null, this);
        }

        final List<Segment> minusBoundary = new ArrayList<>();
        final List<Segment> plusBoundary = new ArrayList<>();

        Split<Segment> split;
        Segment minusSegment;
        Segment plusSegment;

        for (Segment segment : boundarySegments) {
            split = segment.split(splitter);

            minusSegment = split.getMinus();
            plusSegment = split.getPlus();

            if (minusSegment != null) {
                minusBoundary.add(minusSegment);
            }

            if (plusSegment != null) {
                plusBoundary.add(plusSegment);
            }
        }

        minusBoundary.add(trimmedSplitter);
        plusBoundary.add(trimmedSplitter.reverse());

        return new Split<>(new ConvexArea(minusBoundary), new ConvexArea(plusBoundary));
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName())
            .append("[boundarySegments= ")
            .append(boundarySegments);

        return sb.toString();
    }

    /** Return an instance representing the full 2D area.
     * @return an instance representing the full 2D area.
     */
    public static ConvexArea full() {
        return FULL;
    }

    /** Construct a convex area by creating lines between adjacent vertices. The vertices must be given in a counter-clockwise
     * around order the interior of the shape. If the area is intended to be closed, the beginning point must be repeated
     * at the end of the path.
     * @param vertices vertices to use to construct the area
     * @param precision precision context used to create new line instances
     * @return a convex area constructed using lines between adjacent vertices
     * @see #fromVertexLoop(Collection, DoublePrecisionContext)
     */
    public static ConvexArea fromVertices(final Collection<Vector2D> vertices, final DoublePrecisionContext precision) {
        return fromVertices(vertices, false, precision);
    }

    /** Construct a convex area by creating lines between adjacent vertices. An implicit line is created between the
     * last vertex given and the first one. The vertices must be given in a counter-clockwise around order the interior
     * of the shape.
     * @param vertices vertices to use to construct the area
     * @param precision precision context used to create new line instances
     * @return a convex area constructed using lines between adjacent vertices
     * @see #fromVertices(Collection, DoublePrecisionContext)
     */
    public static ConvexArea fromVertexLoop(final Collection<Vector2D> vertices, final DoublePrecisionContext precision) {
        return fromVertices(vertices, true, precision);
    }

    /** Internal method for creating a convex area from lines between adjacent vertices.
     * @param vertices vertices to use to construct the area
     * @param close if true, an additional line will be created between the last and first vertex
     * @param precision precision context used to create new line instances
     * @return a convex area constructed using lines between adjacent vertices
     */
    private static ConvexArea fromVertices(final Collection<Vector2D> vertices, boolean close, final DoublePrecisionContext precision) {
        if (vertices.isEmpty()) {
            return full();
        }

        final List<Line> lines = new ArrayList<>();

        Vector2D first = null;
        Vector2D prev = null;
        Vector2D cur = null;

        for (Vector2D vertex : vertices) {
            cur = vertex;

            if (first == null) {
                first = cur;
            }

            if (prev != null && !cur.eq(prev, precision)) {
                lines.add(Line.fromPoints(prev, cur, precision));
            }

            prev = cur;
        }

        if (close && cur != null && !cur.eq(first, precision)) {
            lines.add(Line.fromPoints(cur, first, precision));
        }

        if (!vertices.isEmpty() && lines.isEmpty()) {
            throw new IllegalStateException("Unable to create convex area: only a single unique vertex provided");
        }

        return fromBounds(lines);
    }

    /** Construct a convex area from a line segment path. The area represents the intersection of all of the negative
     * half-spaces of the lines in the path. The boundaries of the returned area may therefore not match the line
     * segments in the path.
     * @param path path to construct the area from
     * @return a convex area constructed from the lines in the given path
     */
    public static ConvexArea fromPath(final SegmentPath path) {
        final List<Line> lines = new ArrayList<>();
        for (Segment segment : path) {
            lines.add(segment.getLine());
        }

        return fromBounds(lines);
    }

    /** Create a convex area formed by the intersection of the negative half-spaces of the
     * given bounding lines. The returned instance represents the area that is on the
     * minus side of all of the given lines. Note that this method does not support areas
     * of zero size (ie, infinitely thin areas or points.)
     * @param boundingLines lines used to define the convex area
     * @return a new convex area instance representing the area on the minus side of all
     *      of the bounding lines or an instance representing the full area if no lines are
     *      given
     * @throws GeometryException if the given set of bounding lines do not form a convex area,
     *      meaning that there is no region that is on the minus side of all of the bounding
     *      lines.
     */
    public static ConvexArea fromBounds(final Line ... boundingLines) {
        return fromBounds(Arrays.asList(boundingLines));
    }

    /** Create a convex area formed by the intersection of the negative half-spaces of the
     * given bounding lines. The returned instance represents the area that is on the
     * minus side of all of the given lines. Note that this method does not support areas
     * of zero size (ie, infinitely thin areas or points.)
     * @param boundingLines lines used to define the convex area
     * @return a new convex area instance representing the area on the minus side of all
     *      of the bounding lines or an instance representing the full area if the collection
     *      is empty
     * @throws GeometryException if the given set of bounding lines do not form a convex area,
     *      meaning that there is no region that is on the minus side of all of the bounding
     *      lines.
     */
    public static ConvexArea fromBounds(final Iterable<Line> boundingLines) {
        final List<Segment> segments = new ArrayList<>();

        // cut each line by every other line in order to get the line segment boundaries
        boolean notConvex = false;
        int outerIdx = 0;
        for (Line line : boundingLines) {
            ++outerIdx;
            Segment segment = line.span();

            int innerIdx = 0;
            for (Line splitter : boundingLines) {
                ++innerIdx;

                if (line != splitter) {
                    Split<Segment> split = segment.split(splitter);

                    if (split.getLocation() == SplitLocation.NEITHER) {
                        if (line.similarOrientation(splitter)) {
                            // two or more splitters are the equivalent; only
                            // use the segment from the first one
                            if (outerIdx > innerIdx) {
                                segment = null;
                            }
                        }
                        else {
                            // two or more splitters are coincident and have opposite
                            // orientations, meaning that no area is on the minus side
                            // of both
                            notConvex = true;
                            break;
                        }
                    }
                    else {
                        segment = segment.split(splitter).getMinus();
                    }

                    if (segment == null) {
                        break;
                    }
                }
            }

            if (notConvex) {
                break;
            }

            if (segment != null) {
                segments.add(segment);
            }
        }

        if (outerIdx < 1) {
            // no lines were given
            return full();
        }

        if (segments.isEmpty() || notConvex) {
            throw new GeometryException("Bounding lines do not produce a convex region: " + boundingLines);
        }

        return new ConvexArea(segments);
    }
}
