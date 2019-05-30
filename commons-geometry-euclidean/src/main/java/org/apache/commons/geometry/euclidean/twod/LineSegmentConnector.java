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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.twod.LineSegmentPath.LineSegmentPathBuilder;
import org.apache.commons.numbers.angle.PlaneAngle;

/** Abstract class for joining collections of line segments into connected
 * paths. Two default implementations are available through the static factory
 * methods {@link #maximizeAngles()} and {@link #minimizAngles()}, with the
 * implementations differing in the way they select connections when more than
 * one segment can be connected to a particular vertex. The object returned
 * by {@link #maximizeAngles()} attempts to maximize the angle measured from
 * the incoming line segment to the outgoing line segment, with positive angles
 * moving counterclockwise and negative angles moving clockwise. This results
 * in behavior favoring small convex paths instead of larger concave ones.
 * The {@link #minimizAngles()} method returns an object that does the opposite,
 * attempting to minimize the angle between the incoming and outgoing segments.
 * This implementation favors large concave paths as opposed to convex ones.
 */
public abstract class LineSegmentConnector implements Serializable {

    /** Serializable UID */
    private static final long serialVersionUID = 20190528L;

    /** The constructed line segment paths */
    private List<LineSegmentPath> paths;

    /** Line segments that have not yet been connected. */
    private List<LineSegment> unusedSegments;

    /** Builder instance used to join segments together into a path. */
    private LineSegmentPathBuilder builder = new LineSegmentPathBuilder();

    /** List of potential connections for the current segment. */
    private List<LineSegment> potentialConnections = new ArrayList<>();

    public List<LineSegmentPath> connect(final Collection<LineSegment> segments) {
        beginConnect(segments);

        doConnect();

        return endConnect();
    }

    protected abstract LineSegment selectConnection(final LineSegment segment, final List<LineSegment> connections,
            final boolean forward);

    private void beginConnect(final Collection<LineSegment> segments) {
        paths = new ArrayList<>();

        unusedSegments = new ArrayList<>(segments);
        Collections.sort(unusedSegments, getSegmentComparator());

        potentialConnections.clear();
    }

    private List<LineSegmentPath> endConnect() {
        List<LineSegmentPath> result = paths;

        paths = null;
        unusedSegments = null;
        potentialConnections.clear();

        return result;
    }

    private void doConnect() {
        while (unusedSegments.size() > 0) {

            final LineSegment current = unusedSegments.remove(unusedSegments.size() - 1);
            builder.append(current);

            followForwardConnections(current);
            followReverseConnections(current);

            LineSegmentPath path = builder.build();
            if (!path.isEmpty()) {
                paths.add(path);
            }
        }
    }

    private void followForwardConnections(final LineSegment segment) {

        LineSegment current = segment;

        while (current != null) {
            Vector2D end = current.getEnd();
            DoublePrecisionContext precision = current.getPrecision();

            potentialConnections.clear();

            if (end != null) {
                Vector2D start;
                for (LineSegment unusedSegment : unusedSegments) {
                    start = unusedSegment.getStart();
                    if (start != null && end.eq(start, precision)) {
                        potentialConnections.add(unusedSegment);
                    }
                }
            }

            // advance to the next segment if possible
            LineSegment next = potentialConnections.isEmpty() ?
                    null :
                    selectConnection(current, potentialConnections, true);

            if (next != null) {
                unusedSegments.remove(next);
                builder.append(next);
            }

            current = next;
        }
    }

    private void followReverseConnections(final LineSegment segment) {

        LineSegment current = segment;

        while (current != null) {
            Vector2D start = current.getStart();

            potentialConnections.clear();

            if (start != null) {
                Vector2D end;
                for (LineSegment unusedSegment : unusedSegments) {
                    end = unusedSegment.getEnd();
                    if (end != null && end.eq(start, unusedSegment.getPrecision())) {
                        potentialConnections.add(unusedSegment);
                    }
                }
            }

            // advance to the next segment if possible
            LineSegment next = potentialConnections.isEmpty() ?
                    null :
                    selectConnection(current, potentialConnections, false);

            if (next != null) {
                unusedSegments.remove(next);
                builder.prepend(next);
            }

            current = next;
        }
    }

    private Comparator<LineSegment> getSegmentComparator() {
        return (a, b) -> {
            // Sort by descending start point coordinates. The segment with the start point
            // with the smallest coordinates will be at the end of the sorted list
            return Vector2D.COORDINATE_ASCENDING_ORDER.compare(b.getStart(), a.getStart());
        };
    }

    public static LineSegmentConnector minimizeAngles() {
        return new MinAngleConnector();
    }

    public static LineSegmentConnector maximizeAngles() {
        return new MaxAngleConnector();
    }

    private static class AngleBasedConnector extends LineSegmentConnector {

        /** Serializable UID */
        private static final long serialVersionUID = 20190528L;

        private final BiPredicate<Double, Double> anglePredicate;

        AngleBasedConnector(final BiPredicate<Double, Double> preferAnglePredicate) {
            this.anglePredicate = preferAnglePredicate;
        }

        /** {@inheritDoc} */
        @Override
        protected LineSegment selectConnection(final LineSegment segment, final List<LineSegment> connections,
                final boolean forward) {

            // simple case first
            if (connections.size() == 1) {
                return connections.get(0);
            }

            // search for the best connection
            final double segmentAngle = segment.getLine().getAngle();

            double selectedDiffAngle = Double.POSITIVE_INFINITY;
            LineSegment selected = null;

            for (LineSegment connection : connections) {
                double connectionAngle = connection.getLine().getAngle();
                double diffAngle = forward ?
                        connectionAngle - segmentAngle :
                        segmentAngle - connectionAngle;

                if (selected == null || anglePredicate.test(selectedDiffAngle, diffAngle)) {
                    selectedDiffAngle = diffAngle;
                    selected = connection;
                }
            }

            return selected;
        }
    }

    private static class MaxAngleConnector extends AngleBasedConnector {

        /** Serializable UID */
        private static final long serialVersionUID = 20190528L;

        MaxAngleConnector() {
            super((prev, current) -> current > prev);
        }
    }

    private static class MinAngleConnector extends AngleBasedConnector {

        /** Serializable UID */
        private static final long serialVersionUID = 20190528L;

        MinAngleConnector() {
            super((prev, current) -> current < prev);
        }
    }
}
