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
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.twod.LineSegmentPath.PathBuilder;
import org.apache.commons.numbers.angle.PlaneAngleRadians;

/** Abstract class for joining collections of line segments into connected
 * paths. This class is not thread-safe.
 */
public abstract class AbstractLineSegmentConnector implements Serializable {

    /** Serializable UID */
    private static final long serialVersionUID = 20190528L;

    /** List of connector entries used internally. */
    private NavigableSet<ConnectorEntry> entries = new TreeSet<ConnectorEntry>();

    /** View of the entry set in descending order. */
    private NavigableSet<ConnectorEntry> entriesDescending = entries.descendingSet();

    /** List used to store possible connections for the current line segment entry. */
    private List<ConnectorEntry> possibleConnections = new ArrayList<>();

    /** List used to store possible point-like (zero-length) connections for the current line
     * segment entry.
     */
    private List<ConnectorEntry> possiblePointConnections = new ArrayList<>();

    /** Add a collection of line segments to the connector, leaving them unconnected
     * until a later call to {@link #connect(Iterable)} or
     * {@link #getPaths()}.
     * @param segments line segments to add
     * @see #connect(Iterable)
     * @see #getPaths()
     */
    public void add(final Iterable<LineSegment> segments) {
        for (LineSegment segment : segments) {
            entries.add(new ConnectorEntry(segment));
        }
    }

    /** Add a collection of line segments to the connector and attempt to connect each new
     * segment with existing segments.
     * @param segments line segments to connect
     */
    public void connect(final Iterable<LineSegment> segments) {
        List<ConnectorEntry> newEntries = new ArrayList<>();

        for (LineSegment segment : segments) {
            newEntries.add(new ConnectorEntry(segment));
        }

        entries.addAll(newEntries);

        for (ConnectorEntry entry : newEntries) {
            makeForwardConnection(entry);
        }
    }

    /** Add the given line segments to this instance and get the connected line segment
     * paths. This call is equivalent to
     * <pre>
     *      connector.add(segments);
     *      List&lt;LineSegmentPath&gt; result = connector.getPaths();
     * </pre>
     * @param segments line segments to add
     * @return the connected line segment paths
     * @see #add(Iterable)
     * @see #getPaths()
     */
    public List<LineSegmentPath> getPaths(final Iterable<LineSegment> segments) {
        add(segments);
        return getPaths();
    }

    /** Get the connected line segment paths. The connector is reset after this call.
     * Further calls to add line segments will result in new paths being generated.
     * @return the connected line segments paths
     */
    public List<LineSegmentPath> getPaths() {
        for (ConnectorEntry entry : entries) {
            followForwardConnections(entry);
        }

        List<LineSegmentPath> paths = new ArrayList<>();
        LineSegmentPath path;
        for (ConnectorEntry entry : entries) {
            path = entry.exportPath();
            if (path != null) {
                paths.add(path);
            }
        }

        entries.clear();
        possibleConnections.clear();
        possiblePointConnections.clear();

        return paths;
    }

    /** Find and follow line segment forward connections from the given start entry.
     * @param start entry to begin the connection operation with
     */
    protected void followForwardConnections(final ConnectorEntry start) {
        ConnectorEntry current = start;

        while (current != null && current.hasConnectableEndPoint() && !current.hasNext()) {
            current = makeForwardConnection(current);
        }
    }

    /** Connect the end point of the given entry to the start point of another entry. Returns
     * the newly connected entry or null if no forward connection was made.
     * @param entry entry to connect
     * @return the next entry in the path or null if no connection was made
     */
    protected ConnectorEntry makeForwardConnection(final ConnectorEntry entry) {
        findPossibleConnections(entry);

        ConnectorEntry next = null;;

        // select from all available connections, handling point-like segments first
        if (!possiblePointConnections.isEmpty()) {
            next = (possiblePointConnections.size() == 1) ?
                    possiblePointConnections.get(0) :
                    selectPointConnection(entry, possiblePointConnections);
        }
        else if (!possibleConnections.isEmpty()) {

            next = (possibleConnections.size() == 1) ?
                    possibleConnections.get(0) :
                    selectConnection(entry, possibleConnections);
        }

        if (next != null) {
            entry.connectTo(next);
        }

        return next;
    }

    /** Find possible connections for the given entry and place them in the
     * {@link #possibleConnections} and {@link #possiblePointConnections} lists.
     * @param entry the entry to find connections for
     */
    private void findPossibleConnections(final ConnectorEntry entry) {
        possibleConnections.clear();
        possiblePointConnections.clear();

        final Vector2D end = entry.getSegment().getEndPoint();
        if (end != null) {
            final ConnectorEntry searchKey = new ConnectorEntry(end);
            final DoublePrecisionContext precision = entry.getSegment().getPrecision();

            Vector2D candidateStart;

            // search up
            for (ConnectorEntry candidate : entries.tailSet(searchKey)) {
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
            for (ConnectorEntry candidate : entriesDescending.tailSet(searchKey, false)) {
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

    /** Add the candidate to one of the connection lists if it represents a possible connection. Returns
     * true the candidate was added, otherwise false.
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
            // sort by coordinates
            int cmp = Vector2D.COORDINATE_ASCENDING_ORDER.compare(start, other.start);
            if (cmp == 0) {
                // sort entries without segments before ones with segments
                final boolean thisHasSegment = segment != null;
                final boolean otherHasSegment = other.segment != null;

                cmp = Boolean.compare(thisHasSegment, otherHasSegment);

                if (cmp == 0 && thisHasSegment) {
                    // place point-like segments before ones with non-zero length
                    cmp = Boolean.compare(this.isPointLike(), other.isPointLike());

                    if (cmp == 0) {
                        // sort by line angle
                        final double aAngle = PlaneAngleRadians.normalizeBetweenMinusPiAndPi(
                                this.getSegment().getLine().getAngle());
                        final double bAngle = PlaneAngleRadians.normalizeBetweenMinusPiAndPi(
                                other.getSegment().getLine().getAngle());

                        cmp = Double.compare(aAngle, bAngle);
                    }
                }
            }
            return cmp;
        }
    }
}
