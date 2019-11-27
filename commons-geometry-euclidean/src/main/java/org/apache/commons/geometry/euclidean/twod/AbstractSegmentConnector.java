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
import java.util.Objects;

import org.apache.commons.geometry.euclidean.internal.AbstractPathConnector;
import org.apache.commons.numbers.angle.PlaneAngleRadians;

/** Abstract class for joining collections of line segments into connected
 * paths. This class is not thread-safe.
 */
public abstract class AbstractSegmentConnector
    extends AbstractPathConnector<AbstractSegmentConnector.ConnectableSegment> {
    /** Add a line segment to the connector, leaving it unconnected until a later call to
     * to {@link #connect(Iterable)} or {@link #connectAll()}.
     * @param segment line segment to add
     * @see #connect(Iterable)
     * @see #connectAll()
     */
    public void add(final Segment segment) {
        addPathElement(new ConnectableSegment(segment));
    }

    /** Add a collection of line segments to the connector, leaving them unconnected
     * until a later call to {@link #connect(Iterable)} or
     * {@link #connectAll()}.
     * @param segments line segments to add
     * @see #connect(Iterable)
     * @see #connectAll()
     * @see #add(Segment)
     */
    public void add(final Iterable<Segment> segments) {
        for (Segment segment : segments) {
            add(segment);
        }
    }

    /** Add a collection of line segments to the connector and attempt to connect each new
     * segment with existing segments. Connections made at this time will not be
     * overwritten by subsequent calls to this or other connection methods.
     * (eg, {@link #connectAll()}).
     *
     * <p>The connector is not reset by this call. Additional segments can still be added
     * to the current set of paths.</p>
     * @param segments line segments to connect
     * @see #connectAll()
     */
    public void connect(final Iterable<Segment> segments) {
        List<ConnectableSegment> newEntries = new ArrayList<>();

        for (Segment segment : segments) {
            newEntries.add(new ConnectableSegment(segment));
        }

        connectPathElements(newEntries);
    }

    /** Add the given line segments to this instance and connect all current
     * segments into polylines (ie, line segment paths). This call is equivalent to
     * <pre>
     *      connector.add(segments);
     *      List&lt;Polyline&gt; result = connector.connectAll();
     * </pre>
     *
     * <p>The connector is reset after this call. Further calls to
     * add or connect line segments will result in new paths being
     * generated.</p>
     * @param segments line segments to add
     * @return the connected line segment paths
     * @see #add(Iterable)
     * @see #connectAll()
     */
    public List<Polyline> connectAll(final Iterable<Segment> segments) {
        add(segments);
        return connectAll();
    }

    /** Connect all current segments into connected paths, returning the result as a
     * list of polylines.
     *
     * <p>The connector is reset after this call. Further calls to
     * add or connect line segments will result in new paths being
     * generated.</p>
     * @return the connected line segments paths
     */
    public List<Polyline> connectAll() {
        final List<ConnectableSegment> roots = computePathRoots();
        final List<Polyline> paths = new ArrayList<>(roots.size());

        for (ConnectableSegment root : roots) {
            paths.add(toPolyline(root));
        }

        return paths;
    }

    /** Convert the linked list of path elements starting at the argument
     * into a {@link Polyline}.
     * @param root root of a connected path linked list
     * @return a polyline representing the linked list path
     */
    private Polyline toPolyline(final ConnectableSegment root) {
        final Polyline.Builder builder = Polyline.builder(null);

        builder.append(root.getSegment());

        ConnectableSegment current = root.getNext();

        while (current != null && current != root) {
            builder.append(current.getSegment());
            current = current.getNext();
        }

        return builder.build();
    }

    /** Internal class used to connect line segments together.
     */
    protected static class ConnectableSegment extends AbstractPathConnector.ConnectableElement<ConnectableSegment> {
        /** Segment start point. This will be used to connect to other path elements. */
        private final Vector2D start;

        /** Line segment for the entry. */
        private final Segment segment;

        /** Create a new instance with the given start point. This constructor is
         * intended only for performing searches for other path elements.
         * @param start start point
         */
        public ConnectableSegment(final Vector2D start) {
            this(start, null);
        }

        /** Create a new instance from the given line segment.
         * @param segment line segment
         */
        public ConnectableSegment(final Segment segment) {
            this(segment.getStartPoint(), segment);
        }

        /** Create a new instance with the given start point and line segment.
         * @param start start point
         * @param segment line segment
         */
        private ConnectableSegment(final Vector2D start, final Segment segment) {
            this.start = start;
            this.segment = segment;
        }

        /** Get the line segment for this instance.
         * @return the line segment for this instance
         */
        public Segment getSegment() {
            return segment;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasStart() {
            return start != null;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasEnd() {
            return segment != null && segment.getEndPoint() != null;
        }

        /** Return true if this instance has a size equivalent to zero.
         * @return true if this instance has a size equivalent to zero.
         */
        public boolean hasZeroSize() {
            return segment != null && segment.getPrecision().eqZero(segment.getSize());
        }

        /** {@inheritDoc} */
        @Override
        public boolean endPointsEq(final ConnectableSegment other) {
            if (hasEnd() && other.hasEnd()) {
                return segment.getEndPoint()
                        .eq(other.segment.getEndPoint(), segment.getPrecision());
            }

            return false;
        }

        /** {@inheritDoc} */
        @Override
        public boolean canConnectTo(final ConnectableSegment next) {
            final Vector2D end = segment.getEndPoint();
            final Vector2D nextStart = next.start;

            return end != null && nextStart != null &&
                    end.eq(nextStart, segment.getPrecision());
        }

        /** {@inheritDoc} */
        @Override
        public double getRelativeAngle(final ConnectableSegment next) {
            return segment.getLine().angle(next.getSegment().getLine());
        }

        /** {@inheritDoc} */
        @Override
        public ConnectableSegment getConnectionSearchKey() {
            return new ConnectableSegment(segment.getEndPoint());
        }

        /** {@inheritDoc} */
        @Override
        public boolean shouldContinueConnectionSearch(final ConnectableSegment candidate, final boolean ascending) {

            if (candidate.hasStart()) {
                final double candidateX = candidate.getSegment().getStartPoint().getX();
                final double thisX = segment.getEndPoint().getX();
                final int cmp = segment.getPrecision().compare(candidateX, thisX);

                return ascending ? cmp <= 0 : cmp >= 0;
            }

            return true;
        }

        /** {@inheritDoc} */
        @Override
        public int compareTo(ConnectableSegment other) {
            // sort by coordinates
            int cmp = Vector2D.COORDINATE_ASCENDING_ORDER.compare(start, other.start);
            if (cmp == 0) {
                // sort entries without segments before ones with segments
                final boolean thisHasSegment = segment != null;
                final boolean otherHasSegment = other.segment != null;

                cmp = Boolean.compare(thisHasSegment, otherHasSegment);

                if (cmp == 0 && thisHasSegment) {
                    // place point-like segments before ones with non-zero length
                    cmp = Boolean.compare(this.hasZeroSize(), other.hasZeroSize());

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

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return Objects.hash(start, segment);
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || !this.getClass().equals(obj.getClass())) {
                return false;
            }

            final ConnectableSegment other = (ConnectableSegment) obj;
            return Objects.equals(this.start, other.start) &&
                    Objects.equals(this.segment, other.segment);
        }

        /** {@inheritDoc} */
        @Override
        protected ConnectableSegment getSelf() {
            return this;
        }
    }
}
