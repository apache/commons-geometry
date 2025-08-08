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
package org.apache.commons.geometry.spherical.twod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.geometry.core.internal.GeometryInternalUtils;
import org.apache.commons.geometry.euclidean.internal.AbstractPathConnector;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

/** Abstract class for joining collections of great arcs into connected
 * paths. This class is not thread-safe.
 */
public abstract class AbstractGreatArcConnector
    extends AbstractPathConnector<AbstractGreatArcConnector.ConnectableGreatArc> {

    /** Create an instance. */
    public AbstractGreatArcConnector() {
        // Do nothing
    }

    /** Add an arc to the connector, leaving it unconnected until a later call
     * to {@link #connect(Iterable)} or {@link #connectAll()}.
     * @param arc arc to add
     * @see #connect(Iterable)
     * @see #connectAll()
     */
    public void add(final GreatArc arc) {
        addPathElement(new ConnectableGreatArc(arc));
    }

    /** Add a collection of arcs to the connector, leaving them unconnected
     * until a later call to {@link #connect(Iterable)} or
     * {@link #connectAll()}.
     * @param arcs arcs to add
     * @see #connect(Iterable)
     * @see #connectAll()
     * @see #add(GreatArc)
     */
    public void add(final Iterable<GreatArc> arcs) {
        for (final GreatArc segment : arcs) {
            add(segment);
        }
    }

    /** Add a collection of arcs to the connector and attempt to connect each new
     * arc with existing ones. Connections made at this time will not be
     * overwritten by subsequent calls to this or other connection methods,
     * (eg, {@link #connectAll()}).
     *
     * <p>The connector is not reset by this call. Additional arc can still be added
     * to the current set of paths.</p>
     * @param arcs arcs to connect
     * @see #connectAll()
     */
    public void connect(final Iterable<GreatArc> arcs) {
        final List<ConnectableGreatArc> newEntries = new ArrayList<>();

        for (final GreatArc segment : arcs) {
            newEntries.add(new ConnectableGreatArc(segment));
        }

        connectPathElements(newEntries);
    }

    /** Add the given arcs to this instance and connect all current
     * arc into paths. This call is equivalent to
     * <pre>
     *      connector.add(arcs);
     *      List&lt;GreatArcPath&gt; result = connector.connectAll();
     * </pre>
     *
     * <p>The connector is reset after this call. Further calls to
     * add or connect arcs will result in new paths being generated.</p>
     * @param arcs arcs to add
     * @return the connected arc paths
     * @see #add(Iterable)
     * @see #connectAll()
     */
    public List<GreatArcPath> connectAll(final Iterable<GreatArc> arcs) {
        add(arcs);
        return connectAll();
    }

    /** Connect all current arcs into connected paths, returning the result as a
     * list of arc paths.
     *
     * <p>The connector is reset after this call. Further calls to
     * add or connect arcs will result in new paths being generated.</p>
     * @return the connected line segments paths
     */
    public List<GreatArcPath> connectAll() {
        final List<ConnectableGreatArc> roots = computePathRoots();
        final List<GreatArcPath> paths = new ArrayList<>(roots.size());

        for (final ConnectableGreatArc root : roots) {
            paths.add(toPath(root));
        }

        return paths;
    }

    /** Convert the linked list of path elements starting at the argument
     * into a {@link GreatArcPath}.
     * @param root root of a connected path linked list
     * @return a great arc path representing the linked list path
     */
    private GreatArcPath toPath(final ConnectableGreatArc root) {
        final GreatArcPath.Builder builder = GreatArcPath.builder(null);

        builder.append(root.getArc());

        ConnectableGreatArc current = root.getNext();

        while (current != null && !GeometryInternalUtils.sameInstance(current, root)) {
            builder.append(current.getArc());
            current = current.getNext();
        }

        return builder.build();
    }

    /** Internal class for connecting {@link GreatArc}s into {@link GreatArcPath}s.
     */
    protected static class ConnectableGreatArc extends AbstractPathConnector.ConnectableElement<ConnectableGreatArc> {
        /** Segment start point. This will be used to connect to other path elements. */
        private final Point2S start;

        /** Great arc for this instance. */
        private final GreatArc arc;

        /** Create a new instance with the given start point. This constructor is
         * intended only for performing searches for other path elements.
         * @param start start point
         */
        public ConnectableGreatArc(final Point2S start) {
            this(start, null);
        }

        /** Create a new instance from the given arc.
         * @param arc arc for the instance
         */
        public ConnectableGreatArc(final GreatArc arc) {
            this(arc.getStartPoint(), arc);
        }

        /** Create a new instance with the given start point and arc.
         * @param start start point
         * @param arc arc for the instance
         */
        private ConnectableGreatArc(final Point2S start, final GreatArc arc) {
            this.start = start;
            this.arc = arc;
        }

        /** Get the arc for the instance.
         * @return the arc for the instance
         */
        public GreatArc getArc() {
            return arc;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasStart() {
            return start != null;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasEnd() {
            return arc.getEndPoint() != null;
        }

        /** {@inheritDoc} */
        @Override
        public boolean endPointsEq(final ConnectableGreatArc other) {
            if (hasEnd() && other.hasEnd()) {
                return arc.getEndPoint()
                        .eq(other.arc.getEndPoint(), arc.getCircle().getPrecision());
            }

            return false;
        }

        /** Return true if this instance has a size equivalent to zero.
         * @return true if this instance has a size equivalent to zero.
         */
        public boolean hasZeroSize() {
            return arc != null && arc.getCircle().getPrecision().eqZero(arc.getSize());
        }

        /** {@inheritDoc} */
        @Override
        public boolean canConnectTo(final ConnectableGreatArc next) {
            final Point2S end = arc.getEndPoint();
            final Point2S nextStart = next.start;

            return end != null && nextStart != null &&
                    end.eq(nextStart, arc.getCircle().getPrecision());
        }

        /** {@inheritDoc} */
        @Override
        public double getRelativeAngle(final ConnectableGreatArc other) {
            return arc.getCircle().angle(other.getArc().getCircle());
        }

        /** {@inheritDoc} */
        @Override
        public ConnectableGreatArc getConnectionSearchKey() {
            return new ConnectableGreatArc(arc.getEndPoint());
        }

        /** {@inheritDoc} */
        @Override
        public boolean shouldContinueConnectionSearch(final ConnectableGreatArc candidate,
                final boolean ascending) {

            if (candidate.hasStart()) {
                final double candidatePolar = candidate.getArc().getStartPoint().getPolar();
                final double thisPolar = arc.getEndPoint().getPolar();
                final int cmp = arc.getCircle().getPrecision().compare(candidatePolar, thisPolar);

                return ascending ? cmp <= 0 : cmp >= 0;
            }

            return true;
        }

        /** {@inheritDoc} */
        @Override
        public int compareTo(final ConnectableGreatArc other) {
            int cmp = Point2S.POLAR_AZIMUTH_ASCENDING_ORDER.compare(start, other.start);

            if (cmp == 0) {
                // sort entries without arcs before ones with arcs
                final boolean thisHasArc = arc != null;
                final boolean otherHasArc = other.arc != null;

                cmp = Boolean.compare(thisHasArc, otherHasArc);

                if (cmp == 0 && thisHasArc) {
                    // place point-like segments before ones with non-zero length
                    cmp = Boolean.compare(this.hasZeroSize(), other.hasZeroSize());

                    if (cmp == 0) {
                        // sort by circle pole
                        cmp = Vector3D.COORDINATE_ASCENDING_ORDER.compare(
                                arc.getCircle().getPole(),
                                other.arc.getCircle().getPole());
                    }
                }
            }

            return cmp;
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return Objects.hash(start, arc);
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

            final ConnectableGreatArc other = (ConnectableGreatArc) obj;
            return Objects.equals(this.start, other.start) &&
                    Objects.equals(this.arc, other.arc);
        }

        /** {@inheritDoc} */
        @Override
        protected ConnectableGreatArc getSelf() {
            return this;
        }
    }
}
