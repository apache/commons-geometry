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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.exception.GeometryException;
import org.apache.commons.geometry.core.partition.ConvexHyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partition.Hyperplane;
import org.apache.commons.geometry.core.partition.HyperplaneLocation;
import org.apache.commons.geometry.core.partition.Split;
import org.apache.commons.geometry.core.partition.SplitLocation;
import org.apache.commons.geometry.euclidean.twod.LineSegmentPath.PathBuilder;

/** Class representing a finite or infinite convex area in Euclidean 2D space.
 * The boundaries of this area, if any, are composed of line segments.
 */
public final class ConvexArea implements ConvexHyperplaneBoundedRegion<Vector2D>, Serializable {

    /** Serializable UID */
    private static final long serialVersionUID = 20190619L;

    /** Instance representing the full 2D plane. */
    private static final ConvexArea FULL = new ConvexArea(LineSegmentPath.empty());

    /** The boundary of the convex area; this will be empty if the area
     * covers the entire space.
     */
    private final LineSegmentPath boundaryPath;

    /** Simple constructor. Callers are responsible for ensuring that the given path
     * represents the boundary of a convex area. No validation is performed.
     * @param boundaryPath the boundary of the convex area
     */
    private ConvexArea(final LineSegmentPath boundaryPath) {
        this.boundaryPath = boundaryPath;
    }

    /** Get the line segment path comprising the boundary of the area. The segments
     * are oriented so that their minus sides point toward the interior of the region.
     * The path will be empty if the area does not have any boundaries, in other words,
     * if it is full.
     * @return the line segment path comprising the boundary of the area.
     */
    public LineSegmentPath getBoundaryPath() {
        return boundaryPath;
    }

    /** {@inheritDoc} */
    @Override
    public List<ConvexArea> toConvex() {
        return Arrays.asList(this);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFull() {
        // no segments => no boundaries => no outside
        return boundaryPath.isEmpty();
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
        if (boundaryPath.isClosed()) {
            double quadrilateralAreaSum = 0.0;

            for (LineSegment segment : boundaryPath) {
                quadrilateralAreaSum += segment.getStartPoint().signedArea(segment.getEndPoint());
            }

            return 0.5 * quadrilateralAreaSum;
        }

        // not closed; size is infinite
        return Double.POSITIVE_INFINITY;
    }

    /** {@inheritDoc} */
    @Override
    public double getBoundarySize() {
        double sum = 0.0;
        for (LineSegment seg : boundaryPath) {
            sum += seg.getSize();
        }

        return sum;
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D getBarycenter() {
        if (boundaryPath.isClosed()) {

            double quadrilateralAreaSum = 0.0;
            double scaledSumX = 0.0;
            double scaledSumY = 0.0;

            double signedArea;
            Vector2D startPoint;
            Vector2D endPoint;

            for (LineSegment seg : boundaryPath) {
                startPoint = seg.getStartPoint();
                endPoint = seg.getEndPoint();

                signedArea = startPoint.signedArea(endPoint);

                quadrilateralAreaSum += signedArea;

                scaledSumX += signedArea * (startPoint.getX() + endPoint.getX());
                scaledSumY += signedArea * (startPoint.getY() + endPoint.getY());
            }

            return Vector2D.of(scaledSumX, scaledSumY).multiply(1.0 / (3.0 * quadrilateralAreaSum));
        }

        // not closed; no barycenter
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public RegionLocation classify(Vector2D pt) {
        boolean isOn = false;

        HyperplaneLocation loc;
        for (LineSegment seg : boundaryPath) {
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

        for (LineSegment seg : boundaryPath) {
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
    public LineSegment trim(final LineSegment segment) {
        LineSegment remaining = segment;
        for (LineSegment boundary : boundaryPath) {
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
        final ConvexArea minus = new ConvexArea(LineSegmentPath.fromSegments(splitter.span()));
        final ConvexArea plus = new ConvexArea(LineSegmentPath.fromSegments(splitter.reverse().span()));

        return new Split<>(minus, plus);
    }

    /** Method called to split the current instance when the instance contains at least
     * one boundary.
     * @param splitter line to split the instance with
     * @return the split instance
     */
    private Split<ConvexArea> splitBounded(final Line splitter) {
        final LineSegment trimmedSplitter = trim(splitter.span());

        if (trimmedSplitter == null) {
            // The splitter lies entirely outside of the region; we need
            // to determine whether we lie on the plus or minus side of the splitter.
            // We can use the first segment to determine this. If the segment is entirely
            // on the minus side of the splitter or lies directly on the splitter and has
            // the same orientation, then the area lies on the minus side of the splitter.
            // Otherwise, it lies on the plus side.
            LineSegment testSegment = boundaryPath.getStartSegment();
            SplitLocation testLocation = testSegment.split(splitter).getLocation();

            if (SplitLocation.MINUS == testLocation ||
                    (SplitLocation.NEITHER == testLocation && splitter.similarOrientation(testSegment.getLine()))) {
                return new Split<>(this, null);
            }

            return new Split<>(null, this);
        }

        return new ConvexAreaPathSplitHelper(splitter, trimmedSplitter).split();
    }

    /** Return an instance representing the full 2D area.
     * @return an instance representing the full 2D area.
     */
    public static ConvexArea full() {
        return FULL;
    }

    /** Create a convex area formed by the intersection of the negative half-spaces of the
     * given bounding lines. The returned instance represents the area that is not on the
     * plus side of any of the lines.
     * @param boundingLines lines used to define the convex area
     * @return a new convex area instance representing the area on the minus side of all
     *      of the bounding lines or an instance representing the full area if no lines are
     *      given
     * @throws GeometryException if the given set of bounding lines do not form a convex area,
     *      meaning that there is no region that is on the minus side of all of the bounding
     *      lines.
     */
    public static ConvexArea fromBoundingLines(final Line ... boundingLines) {
        return fromBoundingLines(Arrays.asList(boundingLines));
    }

    /** Create a convex area formed by the intersection of the negative half-spaces of the
     * given bounding lines. The returned instance represents the area that is not on the
     * plus side of any other of the lines.
     * @param boundingLines lines used to define the convex area
     * @return a new convex area instance representing the area on the minus side of all
     *      of the bounding lines or an instance representing the full area if the collection
     *      is empty
     * @throws GeometryException if the given set of bounding lines do not form a convex area,
     *      meaning that there is no region that is on the minus side of all of the bounding
     *      lines.
     */
    public static ConvexArea fromBoundingLines(final Collection<Line> boundingLines) {
        if (boundingLines.isEmpty()) {
            return full();
        }

        final InteriorAngleLineSegmentConnector connector = new InteriorAngleLineSegmentConnector.Minimize();

        // cut each line by every other line in order to get the line segment boundaries
        for (Line line : boundingLines) {
            LineSegment segment = line.span();

            for (Line splitter : boundingLines) {
                if (!line.eq(splitter)) {
                    segment = segment.split(splitter).getMinus();
                    if (segment == null) {
                        break;
                    }
                }
            }

            if (segment != null) {
                connector.add(segment);
            }
        }

        final List<LineSegmentPath> paths = connector.getPaths();

        if (paths.isEmpty()) {
            throw new GeometryException("Bounding lines did not produce a convex region.");
        }

        return new ConvexArea(paths.get(0));
    }

    /** Helper class for splitting the convex area when the splitter cuts through the
     * interior of the area. This class handles the construction of the boundary paths for
     * each side of the split.
     */
    private final class ConvexAreaPathSplitHelper {

        /** Path builder for the minus side of the split. */
        private final PathBuilder minusBuilder = LineSegmentPath.builder(null);

        /** Path builder for the plus side of the split. */
        private final PathBuilder plusBuilder = LineSegmentPath.builder(null);

        /** Splitting line. */
        private final Line splitter;

        /** The new boundary on the minus side of the split. */
        private LineSegment newMinusBoundary;

        /** The new boundary on the plus side of the split. */
        private LineSegment newPlusBoundary;

        /** The side that the current segment is on relative to the splitter. */
        private HyperplaneLocation currentSide;

        /** The side that the previous segment was on relative to the splitter. */
        private HyperplaneLocation prevSide;

        /** Create a new instance for building the plus and minus sides of a split operation. The
         * splitter must pass through the interior of the convex area.
         * @param splitter The splitting line; this must pass through the interior of the area.
         * @param trimmedSplitter The line segment containing the portion of the splitter that
         *      lies entirely on the inside of the area.
         */
        public ConvexAreaPathSplitHelper(final Line splitter, final LineSegment trimmedSplitter) {
            this.splitter = splitter;

            this.newMinusBoundary = trimmedSplitter;
            this.newPlusBoundary = trimmedSplitter.reverse();
        }

        /** Perform the split operation.
         * @return the result of the split
         */
        public Split<ConvexArea> split() {

            for (LineSegment boundary : boundaryPath) {
                // split the existing boundary
                Split<LineSegment> split = boundary.split(splitter);
                SplitLocation splitLocation = split.getLocation();

                if (SplitLocation.MINUS == splitLocation) {
                    currentSide = HyperplaneLocation.MINUS;

                    if (hasChangedSide()) {
                        appendNewBoundaries();
                    }

                    minusBuilder.append(split.getMinus());
                }
                else if (SplitLocation.PLUS == splitLocation) {
                    currentSide = HyperplaneLocation.PLUS;

                    if (hasChangedSide()) {
                        appendNewBoundaries();
                    }

                    plusBuilder.append(split.getPlus());
                }
                else if (SplitLocation.BOTH == splitLocation) {
                    // determine which side comes first in the path sequence
                    double angle = splitter.angle(boundary.getLine());
                    if (angle >= 0) {
                        // the boundary crosses the splitter from the plus side to
                        // the minus side
                        plusBuilder.append(split.getPlus());
                        appendNewBoundaries();
                        minusBuilder.append(split.getMinus());

                        currentSide = HyperplaneLocation.MINUS;
                    }
                    else {
                        // the boundary crosses the splitter from the minus side to
                        // the plus side
                        minusBuilder.append(split.getMinus());
                        appendNewBoundaries();
                        plusBuilder.append(split.getPlus());

                        currentSide = HyperplaneLocation.PLUS;
                    }
                }

                prevSide = currentSide;
            }

            appendNewBoundaries();

            final ConvexArea minus = new ConvexArea(minusBuilder.build());
            final ConvexArea plus = new ConvexArea(plusBuilder.build());

            return new Split<>(minus, plus);
        }

        /** Return true if the split operation has switched sides relative to the
         * splitting line since the last boundary segment was processed.
         * @return true if the operation has switched sides
         */
        private boolean hasChangedSide() {
            return prevSide != null && prevSide != currentSide;
        }

        /** Append the new boundaries formed by the split operation to the plus and
         * minus paths. The boundaries are only appended once. Subsequent calls to
         * this method do nothing.
         */
        private void appendNewBoundaries() {
            if (newMinusBoundary != null) {
                minusBuilder.append(newMinusBoundary);
                plusBuilder.append(newPlusBoundary);

                newMinusBoundary = null;
                newPlusBoundary = null;
            }
        }
    }
}
