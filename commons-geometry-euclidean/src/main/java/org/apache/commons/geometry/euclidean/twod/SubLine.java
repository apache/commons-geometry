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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.geometry.core.partitioning.AbstractSubHyperplane;
import org.apache.commons.geometry.core.partitioning.BSPTree;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Region;
import org.apache.commons.geometry.core.partitioning.Region.Location;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.IntervalsSet;
import org.apache.commons.geometry.euclidean.oned.OrientedPoint;
import org.apache.commons.geometry.euclidean.oned.Vector1D;

/** This class represents a sub-hyperplane for {@link Line}.
 */
public class SubLine extends AbstractSubHyperplane<Vector2D, Vector1D> {

    /** Simple constructor.
     * @param hyperplane underlying hyperplane
     * @param remainingRegion remaining region of the hyperplane
     */
    public SubLine(final Hyperplane<Vector2D> hyperplane,
                   final Region<Vector1D> remainingRegion) {
        super(hyperplane, remainingRegion);
    }

    /** Create a sub-line from two endpoints.
     * @param start start point
     * @param end end point
     * @param precision precision context used to compare floating point values
     */
    public SubLine(final Vector2D start, final Vector2D end, final DoublePrecisionContext precision) {
        super(Line.fromPoints(start, end, precision), buildIntervalSet(start, end, precision));
    }

    /** Create a sub-line from a segment.
     * @param segment single segment forming the sub-line
     */
    public SubLine(final Segment segment) {
        super(segment.getLine(),
              buildIntervalSet(segment.getStart(), segment.getEnd(), segment.getLine().getPrecision()));
    }

    /** Get the endpoints of the sub-line.
     * <p>
     * A subline may be any arbitrary number of disjoints segments, so the endpoints
     * are provided as a list of endpoint pairs. Each element of the list represents
     * one segment, and each segment contains a start point at index 0 and an end point
     * at index 1. If the sub-line is unbounded in the negative infinity direction,
     * the start point of the first segment will have infinite coordinates. If the
     * sub-line is unbounded in the positive infinity direction, the end point of the
     * last segment will have infinite coordinates. So a sub-line covering the whole
     * line will contain just one row and both elements of this row will have infinite
     * coordinates. If the sub-line is empty, the returned list will contain 0 segments.
     * </p>
     * @return list of segments endpoints
     */
    public List<Segment> getSegments() {

        final Line line = (Line) getHyperplane();
        final List<Interval> list = ((IntervalsSet) getRemainingRegion()).asList();
        final List<Segment> segments = new ArrayList<>(list.size());

        for (final Interval interval : list) {
            final Vector2D start = line.toSpace(Vector1D.of(interval.getInf()));
            final Vector2D end   = line.toSpace(Vector1D.of(interval.getSup()));
            segments.add(new Segment(start, end, line));
        }

        return segments;

    }

    /** Get the intersection of the instance and another sub-line.
     * <p>
     * This method is related to the {@link Line#intersection(Line)
     * intersection} method in the {@link Line Line} class, but in addition
     * to compute the point along infinite lines, it also checks the point
     * lies on both sub-line ranges.
     * </p>
     * @param subLine other sub-line which may intersect instance
     * @param includeEndPoints if true, endpoints are considered to belong to
     * instance (i.e. they are closed sets) and may be returned, otherwise endpoints
     * are considered to not belong to instance (i.e. they are open sets) and intersection
     * occurring on endpoints lead to null being returned
     * @return the intersection point if there is one, null if the sub-lines don't intersect
     */
    public Vector2D intersection(final SubLine subLine, final boolean includeEndPoints) {

        // retrieve the underlying lines
        Line line1 = (Line) getHyperplane();
        Line line2 = (Line) subLine.getHyperplane();

        // compute the intersection on infinite line
        Vector2D v2D = line1.intersection(line2);
        if (v2D == null) {
            return null;
        }

        // check location of point with respect to first sub-line
        Location loc1 = getRemainingRegion().checkPoint(line1.toSubSpace(v2D));

        // check location of point with respect to second sub-line
        Location loc2 = subLine.getRemainingRegion().checkPoint(line2.toSubSpace(v2D));

        if (includeEndPoints) {
            return ((loc1 != Location.OUTSIDE) && (loc2 != Location.OUTSIDE)) ? v2D : null;
        } else {
            return ((loc1 == Location.INSIDE) && (loc2 == Location.INSIDE)) ? v2D : null;
        }

    }

    /** Build an interval set from two points.
     * @param start start point
     * @param end end point
     * @param precision precision context used to compare floating point values
     * @return an interval set
     */
    private static IntervalsSet buildIntervalSet(final Vector2D start, final Vector2D end, final DoublePrecisionContext precision) {
        final Line line = Line.fromPoints(start, end, precision);
        return new IntervalsSet(line.toSubSpace(start).getX(),
                                line.toSubSpace(end).getX(),
                                precision);
    }

    /** {@inheritDoc} */
    @Override
    protected AbstractSubHyperplane<Vector2D, Vector1D> buildNew(final Hyperplane<Vector2D> hyperplane,
                                                                       final Region<Vector1D> remainingRegion) {
        return new SubLine(hyperplane, remainingRegion);
    }

    /** {@inheritDoc} */
    @Override
    public SplitSubHyperplane<Vector2D> split(final Hyperplane<Vector2D> hyperplane) {

        final Line    thisLine  = (Line) getHyperplane();
        final Line    otherLine = (Line) hyperplane;
        final Vector2D crossing = thisLine.intersection(otherLine);
        final DoublePrecisionContext precision = thisLine.getPrecision();

        if (crossing == null) {
            // the lines are parallel
            final double global = otherLine.getOffset(thisLine);
            final int comparison = precision.compare(global, 0.0);

            if (comparison < 0) {
                return new SplitSubHyperplane<>(null, this);
            } else if (comparison > 0) {
                return new SplitSubHyperplane<>(this, null);
            } else {
                return new SplitSubHyperplane<>(null, null);
            }
        }

        // the lines do intersect
        final boolean direct = Math.sin(thisLine.getAngle() - otherLine.getAngle()) < 0;
        final Vector1D x      = thisLine.toSubSpace(crossing);
        final SubHyperplane<Vector1D> subPlus  =
                OrientedPoint.fromPointAndDirection(x, !direct, precision).wholeHyperplane();
        final SubHyperplane<Vector1D> subMinus =
                OrientedPoint.fromPointAndDirection(x,  direct, precision).wholeHyperplane();

        final BSPTree<Vector1D> splitTree = getRemainingRegion().getTree(false).split(subMinus);
        final BSPTree<Vector1D> plusTree  = getRemainingRegion().isEmpty(splitTree.getPlus()) ?
                                               new BSPTree<Vector1D>(Boolean.FALSE) :
                                               new BSPTree<>(subPlus, new BSPTree<Vector1D>(Boolean.FALSE),
                                                                        splitTree.getPlus(), null);
        final BSPTree<Vector1D> minusTree = getRemainingRegion().isEmpty(splitTree.getMinus()) ?
                                               new BSPTree<Vector1D>(Boolean.FALSE) :
                                               new BSPTree<>(subMinus, new BSPTree<Vector1D>(Boolean.FALSE),
                                                                        splitTree.getMinus(), null);
        return new SplitSubHyperplane<>(new SubLine(thisLine.copySelf(), new IntervalsSet(plusTree, precision)),
                                                   new SubLine(thisLine.copySelf(), new IntervalsSet(minusTree, precision)));

    }

}
