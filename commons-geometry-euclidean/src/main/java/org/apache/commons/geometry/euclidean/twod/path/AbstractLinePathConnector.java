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
package org.apache.commons.geometry.euclidean.twod.path;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.geometry.core.internal.GeometryInternalUtils;
import org.apache.commons.geometry.euclidean.internal.AbstractPathConnector;
import org.apache.commons.geometry.euclidean.twod.LineConvexSubset;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.angle.Angle;

/** Abstract class for joining collections of line subsets into connected
 * paths. This class is not thread-safe.
 */
public abstract class AbstractLinePathConnector
    extends AbstractPathConnector<AbstractLinePathConnector.ConnectableLineSubset> {
    /** Add a line subset to the connector, leaving it unconnected until a later call to
     * to {@link #connect(Iterable)} or {@link #connectAll()}.
     * @param subset line subset to add
     * @see #connect(Iterable)
     * @see #connectAll()
     */
    public void add(final LineConvexSubset subset) {
        addPathElement(new ConnectableLineSubset(subset));
    }

    /** Add a collection of line subsets to the connector, leaving them unconnected
     * until a later call to {@link #connect(Iterable)} or
     * {@link #connectAll()}.
     * @param subsets line subsets to add
     * @see #connect(Iterable)
     * @see #connectAll()
     * @see #add(LineConvexSubset)
     */
    public void add(final Iterable<? extends LineConvexSubset> subsets) {
        for (final LineConvexSubset subset : subsets) {
            add(subset);
        }
    }

    /** Add a collection of line subsets to the connector and attempt to connect each new
     * line subset with existing subsets. Connections made at this time will not be
     * overwritten by subsequent calls to this or other connection methods.
     * (eg, {@link #connectAll()}).
     *
     * <p>The connector is not reset by this call. Additional line subsets can still be added
     * to the current set of paths.</p>
     * @param subsets line subsets to connect
     * @see #connectAll()
     */
    public void connect(final Iterable<? extends LineConvexSubset> subsets) {
        final List<ConnectableLineSubset> newEntries = new ArrayList<>();

        for (final LineConvexSubset subset : subsets) {
            newEntries.add(new ConnectableLineSubset(subset));
        }

        connectPathElements(newEntries);
    }

    /** Add the given line subsets to this instance and connect all current
     * subsets into connected paths. This call is equivalent to
     * <pre>
     *      connector.add(subsets);
     *      List&lt;LinePath&gt; result = connector.connectAll();
     * </pre>
     *
     * <p>The connector is reset after this call. Further calls to
     * add or connect line subsets will result in new paths being generated.</p>
     * @param subsets line subsets to add
     * @return the connected 2D paths
     * @see #add(Iterable)
     * @see #connectAll()
     */
    public List<LinePath> connectAll(final Iterable<LineConvexSubset> subsets) {
        add(subsets);
        return connectAll();
    }

    /** Connect all current line subsets into connected paths, returning the result as a
     * list of line paths.
     *
     * <p>The connector is reset after this call. Further calls to
     * add or connect line subsets will result in new paths being generated.</p>
     * @return the connected 2D paths
     */
    public List<LinePath> connectAll() {
        final List<ConnectableLineSubset> roots = computePathRoots();
        final List<LinePath> paths = new ArrayList<>(roots.size());

        for (final ConnectableLineSubset root : roots) {
            paths.add(toPath(root));
        }

        return paths;
    }

    /** Convert the linked list of path elements starting at the argument
     * into a {@link LinePath}.
     * @param root root of a connected path linked list
     * @return a line path representing the linked list path
     */
    private LinePath toPath(final ConnectableLineSubset root) {
        final LinePath.Builder builder = LinePath.builder(null);

        builder.append(root.getLineSubset());

        ConnectableLineSubset current = root.getNext();

        while (current != null && !GeometryInternalUtils.sameInstance(current, root)) {
            builder.append(current.getLineSubset());
            current = current.getNext();
        }

        return builder.build();
    }

    /** Internal class used to connect line subsets together.
     */
    protected static class ConnectableLineSubset
        extends AbstractPathConnector.ConnectableElement<ConnectableLineSubset> {
        /** Line subset start point. This will be used to connect to other path elements. */
        private final Vector2D start;

        /** Line subset for the entry. */
        private final LineConvexSubset subset;

        /** Create a new instance with the given start point. This constructor is
         * intended only for performing searches for other path elements.
         * @param start start point
         */
        public ConnectableLineSubset(final Vector2D start) {
            this(start, null);
        }

        /** Create a new instance from the given line subset.
         * @param subset subset instance
         */
        public ConnectableLineSubset(final LineConvexSubset subset) {
            this(subset.getStartPoint(), subset);
        }

        /** Create a new instance with the given start point and line subset.
         * @param start start point
         * @param subset line subset instance
         */
        private ConnectableLineSubset(final Vector2D start, final LineConvexSubset subset) {
            this.start = start;
            this.subset = subset;
        }

        /** Get the line subset for this instance.
         * @return the line subset for this instance
         */
        public LineConvexSubset getLineSubset() {
            return subset;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasStart() {
            return start != null;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasEnd() {
            return subset != null && subset.getEndPoint() != null;
        }

        /** Return true if this instance has a size equivalent to zero.
         * @return true if this instance has a size equivalent to zero.
         */
        public boolean hasZeroSize() {
            return subset != null && subset.getPrecision().eqZero(subset.getSize());
        }

        /** {@inheritDoc} */
        @Override
        public boolean endPointsEq(final ConnectableLineSubset other) {
            if (hasEnd() && other.hasEnd()) {
                return subset.getEndPoint()
                        .eq(other.subset.getEndPoint(), subset.getPrecision());
            }

            return false;
        }

        /** {@inheritDoc} */
        @Override
        public boolean canConnectTo(final ConnectableLineSubset next) {
            final Vector2D end = subset.getEndPoint();
            final Vector2D nextStart = next.start;

            return end != null && nextStart != null &&
                    end.eq(nextStart, subset.getPrecision());
        }

        /** {@inheritDoc} */
        @Override
        public double getRelativeAngle(final ConnectableLineSubset next) {
            return subset.getLine().angle(next.getLineSubset().getLine());
        }

        /** {@inheritDoc} */
        @Override
        public ConnectableLineSubset getConnectionSearchKey() {
            return new ConnectableLineSubset(subset.getEndPoint());
        }

        /** {@inheritDoc} */
        @Override
        public boolean shouldContinueConnectionSearch(final ConnectableLineSubset candidate, final boolean ascending) {

            if (candidate.hasStart()) {
                final double candidateX = candidate.getLineSubset().getStartPoint().getX();
                final double thisX = subset.getEndPoint().getX();
                final int cmp = subset.getPrecision().compare(candidateX, thisX);

                return ascending ? cmp <= 0 : cmp >= 0;
            }

            return true;
        }

        /** {@inheritDoc} */
        @Override
        public int compareTo(final ConnectableLineSubset other) {
            // sort by coordinates
            int cmp = Vector2D.COORDINATE_ASCENDING_ORDER.compare(start, other.start);
            if (cmp == 0) {
                // sort entries without line subsets before ones with
                final boolean thisHasSubset = subset != null;
                final boolean otherHasSubset = other.subset != null;

                cmp = Boolean.compare(thisHasSubset, otherHasSubset);

                if (cmp == 0 && thisHasSubset) {
                    // place point-like line subsets before ones with non-zero length
                    cmp = Boolean.compare(this.hasZeroSize(), other.hasZeroSize());

                    if (cmp == 0) {
                        // sort by line angle
                        final double aAngle = Angle.Rad.WITHIN_MINUS_PI_AND_PI.applyAsDouble(
                                this.getLineSubset().getLine().getAngle());
                        final double bAngle = Angle.Rad.WITHIN_MINUS_PI_AND_PI.applyAsDouble(
                                other.getLineSubset().getLine().getAngle());

                        cmp = Double.compare(aAngle, bAngle);
                    }
                }
            }
            return cmp;
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return Objects.hash(start, subset);
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

            final ConnectableLineSubset other = (ConnectableLineSubset) obj;
            return Objects.equals(this.start, other.start) &&
                    Objects.equals(this.subset, other.subset);
        }

        /** {@inheritDoc} */
        @Override
        protected ConnectableLineSubset getSelf() {
            return this;
        }
    }
}
