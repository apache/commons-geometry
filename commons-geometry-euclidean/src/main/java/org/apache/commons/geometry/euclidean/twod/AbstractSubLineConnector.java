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

/** Abstract class for joining collections of sublines into connected
 * paths. This class is not thread-safe.
 */
public abstract class AbstractSubLineConnector
    extends AbstractPathConnector<AbstractSubLineConnector.ConnectableSubLine> {
    /** Add a subline to the connector, leaving it unconnected until a later call to
     * to {@link #connect(Iterable)} or {@link #connectAll()}.
     * @param subline subline to add
     * @see #connect(Iterable)
     * @see #connectAll()
     */
    public void add(final ConvexSubLine subline) {
        addPathElement(new ConnectableSubLine(subline));
    }

    /** Add a collection of sublines to the connector, leaving them unconnected
     * until a later call to {@link #connect(Iterable)} or
     * {@link #connectAll()}.
     * @param sublines sublines to add
     * @see #connect(Iterable)
     * @see #connectAll()
     * @see #add(ConvexSubLine)
     */
    public void add(final Iterable<ConvexSubLine> sublines) {
        for (final ConvexSubLine subline : sublines) {
            add(subline);
        }
    }

    /** Add a collection of sublines to the connector and attempt to connect each new
     * subline with existing sublines. Connections made at this time will not be
     * overwritten by subsequent calls to this or other connection methods.
     * (eg, {@link #connectAll()}).
     *
     * <p>The connector is not reset by this call. Additional sublines can still be added
     * to the current set of paths.</p>
     * @param sublines sublines to connect
     * @see #connectAll()
     */
    public void connect(final Iterable<ConvexSubLine> sublines) {
        final List<ConnectableSubLine> newEntries = new ArrayList<>();

        for (final ConvexSubLine subline : sublines) {
            newEntries.add(new ConnectableSubLine(subline));
        }

        connectPathElements(newEntries);
    }

    /** Add the given sublines to this instance and connect all current
     * sublines into polylines (ie, 2D paths). This call is equivalent to
     * <pre>
     *      connector.add(sublines);
     *      List&lt;Polyline&gt; result = connector.connectAll();
     * </pre>
     *
     * <p>The connector is reset after this call. Further calls to
     * add or connect sublines will result in new paths being generated.</p>
     * @param sublines sublines to add
     * @return the connected 2D paths
     * @see #add(Iterable)
     * @see #connectAll()
     */
    public List<Polyline> connectAll(final Iterable<ConvexSubLine> sublines) {
        add(sublines);
        return connectAll();
    }

    /** Connect all current sublines into connected paths, returning the result as a
     * list of polylines.
     *
     * <p>The connector is reset after this call. Further calls to
     * add or connect sublines will result in new paths being generated.</p>
     * @return the connected 2D paths
     */
    public List<Polyline> connectAll() {
        final List<ConnectableSubLine> roots = computePathRoots();
        final List<Polyline> paths = new ArrayList<>(roots.size());

        for (final ConnectableSubLine root : roots) {
            paths.add(toPolyline(root));
        }

        return paths;
    }

    /** Convert the linked list of path elements starting at the argument
     * into a {@link Polyline}.
     * @param root root of a connected path linked list
     * @return a polyline representing the linked list path
     */
    private Polyline toPolyline(final ConnectableSubLine root) {
        final Polyline.Builder builder = Polyline.builder(null);

        builder.append(root.getSubLine());

        ConnectableSubLine current = root.getNext();

        while (current != null && current != root) {
            builder.append(current.getSubLine());
            current = current.getNext();
        }

        return builder.build();
    }

    /** Internal class used to connect sublines together.
     */
    protected static class ConnectableSubLine extends AbstractPathConnector.ConnectableElement<ConnectableSubLine> {
        /** Subline start point. This will be used to connect to other path elements. */
        private final Vector2D start;

        /** Subline for the entry. */
        private final ConvexSubLine subline;

        /** Create a new instance with the given start point. This constructor is
         * intended only for performing searches for other path elements.
         * @param start start point
         */
        public ConnectableSubLine(final Vector2D start) {
            this(start, null);
        }

        /** Create a new instance from the given subline.
         * @param subline subline instance
         */
        public ConnectableSubLine(final ConvexSubLine subline) {
            this(subline.getStartPoint(), subline);
        }

        /** Create a new instance with the given start point and subline.
         * @param start start point
         * @param subline subline instance
         */
        private ConnectableSubLine(final Vector2D start, final ConvexSubLine subline) {
            this.start = start;
            this.subline = subline;
        }

        /** Get the subline for this instance.
         * @return the subline for this instance
         */
        public ConvexSubLine getSubLine() {
            return subline;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasStart() {
            return start != null;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasEnd() {
            return subline != null && subline.getEndPoint() != null;
        }

        /** Return true if this instance has a size equivalent to zero.
         * @return true if this instance has a size equivalent to zero.
         */
        public boolean hasZeroSize() {
            return subline != null && subline.getPrecision().eqZero(subline.getSize());
        }

        /** {@inheritDoc} */
        @Override
        public boolean endPointsEq(final ConnectableSubLine other) {
            if (hasEnd() && other.hasEnd()) {
                return subline.getEndPoint()
                        .eq(other.subline.getEndPoint(), subline.getPrecision());
            }

            return false;
        }

        /** {@inheritDoc} */
        @Override
        public boolean canConnectTo(final ConnectableSubLine next) {
            final Vector2D end = subline.getEndPoint();
            final Vector2D nextStart = next.start;

            return end != null && nextStart != null &&
                    end.eq(nextStart, subline.getPrecision());
        }

        /** {@inheritDoc} */
        @Override
        public double getRelativeAngle(final ConnectableSubLine next) {
            return subline.getLine().angle(next.getSubLine().getLine());
        }

        /** {@inheritDoc} */
        @Override
        public ConnectableSubLine getConnectionSearchKey() {
            return new ConnectableSubLine(subline.getEndPoint());
        }

        /** {@inheritDoc} */
        @Override
        public boolean shouldContinueConnectionSearch(final ConnectableSubLine candidate, final boolean ascending) {

            if (candidate.hasStart()) {
                final double candidateX = candidate.getSubLine().getStartPoint().getX();
                final double thisX = subline.getEndPoint().getX();
                final int cmp = subline.getPrecision().compare(candidateX, thisX);

                return ascending ? cmp <= 0 : cmp >= 0;
            }

            return true;
        }

        /** {@inheritDoc} */
        @Override
        public int compareTo(ConnectableSubLine other) {
            // sort by coordinates
            int cmp = Vector2D.COORDINATE_ASCENDING_ORDER.compare(start, other.start);
            if (cmp == 0) {
                // sort entries without sublines before ones with sublines
                final boolean thisHasSubline = subline != null;
                final boolean otherHasSubline = other.subline != null;

                cmp = Boolean.compare(thisHasSubline, otherHasSubline);

                if (cmp == 0 && thisHasSubline) {
                    // place point-like sublines before ones with non-zero length
                    cmp = Boolean.compare(this.hasZeroSize(), other.hasZeroSize());

                    if (cmp == 0) {
                        // sort by line angle
                        final double aAngle = PlaneAngleRadians.normalizeBetweenMinusPiAndPi(
                                this.getSubLine().getLine().getAngle());
                        final double bAngle = PlaneAngleRadians.normalizeBetweenMinusPiAndPi(
                                other.getSubLine().getLine().getAngle());

                        cmp = Double.compare(aAngle, bAngle);
                    }
                }
            }
            return cmp;
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return Objects.hash(start, subline);
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

            final ConnectableSubLine other = (ConnectableSubLine) obj;
            return Objects.equals(this.start, other.start) &&
                    Objects.equals(this.subline, other.subline);
        }

        /** {@inheritDoc} */
        @Override
        protected ConnectableSubLine getSelf() {
            return this;
        }
    }
}
