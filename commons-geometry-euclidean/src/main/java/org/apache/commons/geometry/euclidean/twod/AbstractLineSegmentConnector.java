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
import java.util.List;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.twod.LineSegmentPath.PathBuilder;
import org.apache.commons.numbers.angle.PlaneAngleRadians;

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
 *
 * <p>This class is not thread-safe.</p>
 */
public abstract class AbstractLineSegmentConnector implements Serializable {

    /** Serializable UID */
    private static final long serialVersionUID = 20190528L;

    /** List of connector entries sorted by ascending start point coordinates. */
    private List<ConnectorEntry> entries;

    /** List used to store possible connections for the current line segment entry. */
    private List<ConnectorEntry> possibleConnections = new ArrayList<>();

    private List<ConnectorEntry> possiblePointConnections = new ArrayList<>();

    /** Convert a collection of line segments into a set of connected line segment paths.
     * @param segments the segments to connect
     * @return a list of connected paths
     */
    public List<LineSegmentPath> connect(final Collection<LineSegment> segments) {

        entries = createSortedEntries(segments);

        // follow loops starting from each entry
        for (ConnectorEntry entry : entries) {
            followForwardConnections(entry);
        }

        // export the paths
        List<LineSegmentPath> paths = new ArrayList<>();
        LineSegmentPath path;
        for (ConnectorEntry entry : entries) {
            path = entry.exportPath();
            if (path != null) {
                paths.add(path);
            }
        }

        // tear down
        entries = null;
        possibleConnections.clear();

        return paths;
    }

    /** Create a list of sorted connector entries for the given line segments.
     * @param segments line segments
     * @return a list of sorted connector entries
     */
    private List<ConnectorEntry> createSortedEntries(final Collection<LineSegment> segments) {
        List<ConnectorEntry> result = new ArrayList<>(segments.size());

        for (LineSegment segment : segments) {
            result.add(new ConnectorEntry(segment));
        }

        // Sort the list in the standard ordering of ConnectorEntry but also adding the
        // conditions that
        //  1) point-like segments come after ones with non-zero length and
        //  2) lines pointing down and to the right come first.
        // We add these extra conditions here and not in the ConnectorEntry compareTo method since
        // that method is used when comparing ConnectorEntry instances that don't have a line
        // segment when performing binary searches. When sorting here, we know that all instances
        // have a line segment.
        Collections.sort(result, (a, b) -> {
            int cmp = a.compareTo(b);
            if (cmp == 0) {
                cmp = Boolean.compare(a.isPointLike(), b.isPointLike());

                if (cmp == 0) {
                    final double aAngle = PlaneAngleRadians.normalizeBetweenMinusPiAndPi(
                            a.getSegment().getLine().getAngle());
                    final double bAngle = PlaneAngleRadians.normalizeBetweenMinusPiAndPi(
                            b.getSegment().getLine().getAngle());

                    return Double.compare(aAngle, bAngle);
                }
            }
            return cmp;
        });

        return result;
    }

    /** Find and follow line segment forward connections from the given start entry.
     * @param start entry to begin the connection operation with
     */
    private void followForwardConnections(final ConnectorEntry start) {
        ConnectorEntry current = start;
        ConnectorEntry next = null;

        while (current != null && current.hasConnectableEndPoint() && !current.hasNext()) {

            findPossibleConnections(current);

            // select from all available connections, handling point-like segments first
            if (!possiblePointConnections.isEmpty()) {
                next = (possiblePointConnections.size() == 1) ?
                        possiblePointConnections.get(0) :
                        selectPointConnection(current, possiblePointConnections);
            }
            else if (!possibleConnections.isEmpty()) {

                next = (possibleConnections.size() == 1) ?
                        possibleConnections.get(0) :
                        selectConnection(current, possibleConnections);
            }

            if (next != null) {
                current.connectTo(next);
            }

            current = next;
            next = null;
        }
    }

    /** Find possible connections for the given entry and place them in the
     * {@link #possibleConnections} list.
     * @param entry the entry to find connections for
     */
    private void findPossibleConnections(final ConnectorEntry entry) {
        possibleConnections.clear();
        possiblePointConnections.clear();

        final Vector2D end = entry.getSegment().getEndPoint();
        if (end != null) {
            // find entries that have a start point equal to our end point;
            // use a binary search to determine where to start looking
            final ConnectorEntry startKey = new ConnectorEntry(end);
            final int search = Collections.binarySearch(entries, startKey);
            final int startIdx = (search >= 0) ? search : Math.abs(search + 1);

            final int size = entries.size();
            final DoublePrecisionContext precision = entry.getSegment().getPrecision();

            ConnectorEntry candidate;
            Vector2D candidateStart;

            // search up
            for (int i=0; i<size; ++i) {
                candidate = entries.get(i);

                if (!addPossibleConnection(entry, candidate)) {
                    // Break out of the loop if the candidate's start point is null or
                    // its x coordinate is greater than the x coordinate of the end point.
                    // Either of these cases indicate that no further sorted points will
                    // match this entry.
                    candidateStart = candidate.getStart();
                    if (candidateStart == null || precision.gt(candidateStart.getX(), end.getX())) {
                        break;
                    }
                }
            }

            // search down
            for (int i=startIdx-1; i>=0; --i) {
                candidate = entries.get(i);

                if (!addPossibleConnection(entry, candidate)) {
                    // Break out of the loop if the candidate's start point is null or
                    // its x coordinate is less than the x coordinate of the end point.
                    candidateStart = candidate.getStart();
                    if (candidateStart == null || precision.lt(candidateStart.getX(), end.getX())) {
                        break;
                    }
                }
            }
        }
    }

    /** Add the candidate to the connections lists if it represents a possible connection. Returns
     * true the candidate has added, otherwise false.
     * @param entry entry to check for connections with
     * @param candidate candidate connection entry
     * @return true if the candidate is a possible connection
     */
    private boolean addPossibleConnection(final ConnectorEntry entry, final ConnectorEntry candidate) {
        if (entry != candidate && entry.canConnectTo(candidate)) {
            if (entry.endPointsEq(candidate)) {
                possiblePointConnections.add(candidate);
            }
            else {
                possibleConnections.add(candidate);
            }

            return true;
        }

        return false;
    }

    /** Method called to select a connection to use for a given entry when multiple zero-length connections are available.
     * The algorithm here attempts to choose the point most likely to produce a logical path by selecting the outgoing segment
     * with the line angle closest to the incoming segment, with unconnected segments preferred over ones that are already
     * connected (thereby allowing other connections to occur in the path).
     * @param incoming the incoming entry
     * @param outgoing list of available outgoing point-like connections
     * @return the connection to use
     */
    protected ConnectorEntry selectPointConnection(final ConnectorEntry incoming, final List<ConnectorEntry> outgoing) {

        final double incomingLineAngle = incoming.getSegment().getLine().getAngle();

        double angleDiff;
        boolean isUnconnected;

        double smallestAngleDiff = 0.0;
        ConnectorEntry bestEntry = null;
        boolean bestIsUnconnected = false;

        for (ConnectorEntry entry : outgoing) {
            angleDiff = Math.abs(PlaneAngleRadians.normalizeBetweenMinusPiAndPi(
                    incomingLineAngle - entry.getSegment().getLine().getAngle()));
            isUnconnected = !entry.hasNext();

            if (bestEntry == null || (!bestIsUnconnected && isUnconnected) ||
                    (bestIsUnconnected == isUnconnected && angleDiff < smallestAngleDiff)) {

                smallestAngleDiff = angleDiff;
                bestEntry = entry;
                bestIsUnconnected = isUnconnected;
            }
        }

        return bestEntry;
    }

    /** Method called to select a connection to use for a given entry when multiple non-length-zero connections are available.
     * @param incoming the incoming entry
     * @param outgoing list of available outgoing connections
     * @return the connection to use
     */
    protected abstract ConnectorEntry selectConnection(final ConnectorEntry incoming, final List<ConnectorEntry> outgoing);


    /** Internal class used to connect line segments together.
     */
    protected static class ConnectorEntry implements Comparable<ConnectorEntry> {

        /** Entry start point. Other entries will connect to this instance at this point. */
        private final Vector2D start;

        /** Line segment for the entry. */
        private final LineSegment segment;

        /** Next connected entry. */
        private ConnectorEntry next;

        /** Previous connected entry. */
        private ConnectorEntry previous;

        /** Flag set to true when this entry has exported its line segment to a path. */
        private boolean exported = false;

        /** Create a new instance with the given start point. This constructor is
         * intended only for performing comparable-based searches for other entries.
         * @param start start point
         */
        public ConnectorEntry(final Vector2D start) {
            this(start, null);
        }

        /** Create a new instance from the given line segment
         * @param segment line segment
         */
        public ConnectorEntry(final LineSegment segment) {
            this(segment.getStartPoint(), segment);
        }

        /** Create a new instance with the given start point and line segment.
         * @param start start point
         * @param segment line segment
         */
        private ConnectorEntry(final Vector2D start, final LineSegment segment) {
            this.start = start;
            this.segment = segment;
        }

        /** Get the start point for this entry.
         * @return the start point for this entry
         */
        public Vector2D getStart() {
            return start;
        }

        /** Get the line segment for this instance.
         * @return the line segment for this instance
         */
        public LineSegment getSegment() {
            return segment;
        }

        /** Return true if the instance is connected to another entry's start point.
         * @return true if the instance has a next entry
         */
        public boolean hasNext() {
            return next != null;
        }

        /** Return true if another entry is connected to this instance's start point.
         * @return true if the instance has a previous entry
         */
        public boolean hasPrevious() {
            return previous != null;
        }

        /**
         * Return true if the line segment has an end point that can be connected
         * to other instances.
         * @return true if the instance has an end point that can be connected to
         *      other instances
         */
        public boolean hasConnectableEndPoint() {
            return segment.getEndPoint() != null;
        }

        /** Return true if the end point for this instance and the given argument exist
         * and are equivalent as evaluated by the precision context for this instance.
         * @param other entry to compare end points with
         * @return true if both end points exist and are equivalent
         */
        public boolean endPointsEq(final ConnectorEntry other) {
            final Vector2D thisEnd = segment.getEndPoint();
            final Vector2D otherEnd = other.segment.getEndPoint();

            return thisEnd != null && otherEnd != null &&
                    thisEnd.eq(otherEnd, segment.getPrecision());
        }

        /** Return true if the line segment has a length of zero according to the
         * instance's precision.
         * @return true if this instance is point-like
         */
        public boolean isPointLike() {
            return segment.getPrecision().eqZero(segment.getSize());
        }

        /** Return true if this instance can connect to the given entry.
         * @param next the potential next entry
         * @return true if this instance can connect to {@code next}
         */
        public boolean canConnectTo(final ConnectorEntry next) {
            if (!next.hasPrevious()) {
                final Vector2D end = segment.getEndPoint();
                final Vector2D start = next.getStart();

                return end != null && start != null &&
                        end.eq(start, segment.getPrecision());
            }

            return false;
        }

        /** Connect this instance's end point to the given entry's start point. No validation
         * is performed in this method. The {@link #canConnectTo(ConnectorEntry)} method must
         * have been called previously.
         * @param next
         */
        public void connectTo(final ConnectorEntry next) {
            this.next = next;
            this.next.previous = this;
        }

        /**
         *  Export the path that this entry belongs to. Returns null if the
         *  path has already been exported.
         *  @return the path that this entry belong to or null if the path has
         *      already been exported
         */
        public LineSegmentPath exportPath() {
            if (!exported) {
                PathBuilder builder = LineSegmentPath.builder(null);

                // add ourselves
                exportPathInternal(builder, true);

                // export the other portions of the path, moving both
                // forward and backward
                ConnectorEntry current;

                // forward
                current = next;
                while (current != null && !current.exported) {
                    current.exportPathInternal(builder, true);

                    current = current.next;
                }

                // backward
                current = previous;
                while (current != null && !current.exported) {
                    current.exportPathInternal(builder, false);

                    current = current.previous;
                }

                return builder.build();
            }

            return null;
        }

        /** Internal method for exporting the path that this entry belongs to.
         * @param builder builder instance for constructing the line segment path
         * @param append if true, the entry's segment should be appended to the builder;
         *      if false, it should be prepended
         */
        private void exportPathInternal(final PathBuilder builder, final boolean append) {
            if (append) {
                builder.append(segment);
            }
            else {
                builder.prepend(segment);
            }

            exported = true;
        }

        /** {@inheritDoc} */
        @Override
        public int compareTo(ConnectorEntry other) {
            return Vector2D.COORDINATE_ASCENDING_ORDER.compare(start, other.start);
        }
    }
}
