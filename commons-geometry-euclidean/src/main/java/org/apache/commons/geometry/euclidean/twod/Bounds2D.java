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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.euclidean.AbstractBounds;
import org.apache.commons.geometry.euclidean.twod.shape.Parallelogram;
import org.apache.commons.numbers.core.Precision;

/** Class containing minimum and maximum points defining a 2D axis-aligned bounding box. Unless otherwise
 * noted, floating point comparisons used in this class are strict, meaning that values are considered equal
 * if and only if they match exactly.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public final class Bounds2D extends AbstractBounds<Vector2D, Bounds2D>
    implements Linecastable2D {

    /** Simple constructor. Callers are responsible for ensuring the min is not greater than max.
     * @param min minimum point
     * @param max maximum point
     */
    private Bounds2D(final Vector2D min, final Vector2D max) {
        super(min, max);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasSize(final Precision.DoubleEquivalence precision) {
        final Vector2D diag = getDiagonal();

        return !precision.eqZero(diag.getX()) &&
                !precision.eqZero(diag.getY());
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(final Vector2D pt) {
        final double x = pt.getX();
        final double y = pt.getY();

        final Vector2D min = getMin();
        final Vector2D max = getMax();

        return x >= min.getX() && x <= max.getX() &&
                y >= min.getY() && y <= max.getY();
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(final Vector2D pt, final Precision.DoubleEquivalence precision) {
        final double x = pt.getX();
        final double y = pt.getY();

        final Vector2D min = getMin();
        final Vector2D max = getMax();

        return precision.gte(x, min.getX()) && precision.lte(x, max.getX()) &&
                precision.gte(y, min.getY()) && precision.lte(y, max.getY());
    }

    /** {@inheritDoc} */
    @Override
    public boolean intersects(final Bounds2D other) {
        final Vector2D aMin = getMin();
        final Vector2D aMax = getMax();

        final Vector2D bMin = other.getMin();
        final Vector2D bMax = other.getMax();

        return aMin.getX() <= bMax.getX() && aMax.getX() >= bMin.getX() &&
                aMin.getY() <= bMax.getY() && aMax.getY() >= bMin.getY();
    }

    /** {@inheritDoc} */
    @Override
    public Bounds2D intersection(final Bounds2D other) {
        if (intersects(other)) {
            final Vector2D aMin = getMin();
            final Vector2D aMax = getMax();

            final Vector2D bMin = other.getMin();
            final Vector2D bMax = other.getMax();

            // get the max of the mins and the mins of the maxes
            final double minX = Math.max(aMin.getX(), bMin.getX());
            final double minY = Math.max(aMin.getY(), bMin.getY());

            final double maxX = Math.min(aMax.getX(), bMax.getX());
            final double maxY = Math.min(aMax.getY(), bMax.getY());

            return new Bounds2D(
                    Vector2D.of(minX, minY),
                    Vector2D.of(maxX, maxY));
        }

        return null; // no intersection
    }

    /** Return {@code true} if the region represented by this instance shares any points with
     * the given line. Floating point comparisons are made using the
     * {@link Line#getPrecision() precision} of the line.
     * @param line line to determine intersection with
     * @return {@code true} if the region represented by this instance intersects
     *      the given line
     */
    public boolean intersects(final Line line) {
        return intersects(line.span());
    }

    /** Return {@code true} if the region represented by this instance shares any points with
     * the given line convex subset. Floating point comparisons are made using the
     * {@link Line#getPrecision() precision} of the subset's line.
     * @param subset line convex subset to determine intersection with
     * @return {@code true} if the region represented by this instance intersects
     *      the given line convex subset
     */
    public boolean intersects(final LineConvexSubset subset) {
        return new BoundsLinecaster2D(subset).intersectsRegion();
    }

    /** Return a {@link Segment} representing the intersection of the region
     * represented by this instance with the given line or {@code null} if no such
     * intersection exists. Floating point comparisons are made using the
     * {@link Line#getPrecision() precision} of the line.
     * @param line line to intersect with
     * @return {@link Segment} representing the intersection of the region
     *      represented by this instance with the given line or {@code null}
     *      if no such intersection exists
     */
    public Segment intersection(final Line line) {
        return intersection(line.span());
    }

    /** Return a {@link Segment} representing the intersection of the region
     * represented by this instance with the given line convex subset or {@code null}
     * if no such intersection exists. Floating point comparisons are made using the
     * {@link Line#getPrecision() precision} of the subset's line.
     * @param subset line convex subset to intersect with
     * @return {@link Segment} representing the intersection of the region
     *      represented by this instance with the given line convex subset or {@code null}
     *      if no such intersection exists
     */
    public Segment intersection(final LineConvexSubset subset) {
        return new BoundsLinecaster2D(subset).getRegionIntersection();
    }

    /** {@inheritDoc} */
    @Override
    public List<LinecastPoint2D> linecast(final LineConvexSubset subset) {
        return new BoundsLinecaster2D(subset).getBoundaryIntersections();
    }

    /** {@inheritDoc} */
    @Override
    public LinecastPoint2D linecastFirst(final LineConvexSubset subset) {
        return new BoundsLinecaster2D(subset).getFirstBoundaryIntersection();
    }

    /** {@inheritDoc}
     *
     * @throws IllegalArgumentException if any dimension of the bounding box is zero
     *      as evaluated by the given precision context
     */
    @Override
    public Parallelogram toRegion(final Precision.DoubleEquivalence precision) {
        return Parallelogram.axisAligned(getMin(), getMax(), precision);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(getMin(), getMax());
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof Bounds2D)) {
            return false;
        }

        final Bounds2D other = (Bounds2D) obj;

        return getMin().equals(other.getMin()) &&
                getMax().equals(other.getMax());
    }

    /** Construct a new instance from the given points.
     * @param first first point
     * @param more additional points
     * @return a new instance containing the min and max coordinates values from the input points
     */
    public static Bounds2D from(final Vector2D first, final Vector2D... more) {
        final Builder builder = builder();

        builder.add(first);
        builder.addAll(Arrays.asList(more));

        return builder.build();
    }

    /** Construct a new instance from the given points.
     * @param points input points
     * @return a new instance containing the min and max coordinates values from the input points
     */
    public static Bounds2D from(final Iterable<Vector2D> points) {
        final Builder builder = builder();

        builder.addAll(points);

        return builder.build();
    }

    /** Construct a new {@link Builder} instance for creating bounds.
     * @return a new builder instance for creating bounds
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Class used to construct {@link Bounds2D} instances.
     */
    public static final class Builder {

        /** Minimum x coordinate. */
        private double minX = Double.POSITIVE_INFINITY;

        /** Minimum y coordinate. */
        private double minY = Double.POSITIVE_INFINITY;

        /** Maximum x coordinate. */
        private double maxX = Double.NEGATIVE_INFINITY;

        /** Maximum y coordinate. */
        private double maxY = Double.NEGATIVE_INFINITY;

        /** Private constructor; instantiate through factory method. */
        private Builder() { }

        /** Add a point to this instance.
         * @param pt point to add
         * @return this instance
         */
        public Builder add(final Vector2D pt) {
            final double x = pt.getX();
            final double y = pt.getY();

            minX = Math.min(x, minX);
            minY = Math.min(y, minY);

            maxX = Math.max(x, maxX);
            maxY = Math.max(y, maxY);

            return this;
        }

        /** Add a collection of points to this instance.
         * @param pts points to add
         * @return this instance
         */
        public Builder addAll(final Iterable<? extends Vector2D> pts) {
            for (final Vector2D pt : pts) {
                add(pt);
            }

            return this;
        }

        /** Add the min and max points from the given bounds to this instance.
         * @param bounds bounds containing the min and max points to add
         * @return this instance
         */
        public Builder add(final Bounds2D bounds) {
            add(bounds.getMin());
            add(bounds.getMax());

            return this;
        }

        /** Return true if this builder contains valid min and max coordinate values.
         * @return true if this builder contains valid min and max coordinate values
         */
        public boolean hasBounds() {
            return Double.isFinite(minX) &&
                    Double.isFinite(minY) &&
                    Double.isFinite(maxX) &&
                    Double.isFinite(maxY);
        }

        /** Create a new {@link Bounds2D} instance from the values in this builder.
         * The builder can continue to be used to create other instances.
         * @return a new bounds instance
         * @throws IllegalStateException if no points were given to the builder or any of the computed
         *      min and max coordinate values are NaN or infinite
         * @see #hasBounds()
         */
        public Bounds2D build() {
            final Vector2D min = Vector2D.of(minX, minY);
            final Vector2D max = Vector2D.of(maxX, maxY);

            if (!hasBounds()) {
                if (Double.isInfinite(minX) && minX > 0 &&
                        Double.isInfinite(maxX) && maxX < 0) {
                    throw new IllegalStateException("Cannot construct bounds: no points given");
                }

                throw new IllegalStateException("Invalid bounds: min= " + min + ", max= " + max);
            }

            return new Bounds2D(min, max);
        }
    }

    /** Subclass of {@link BoundsLinecaster} for 2D space.
     */
    private final class BoundsLinecaster2D extends BoundsLinecaster<Segment, LinecastPoint2D> {

        /** Line convex subset to be tested against the bounds. */
        private final LineConvexSubset subset;

        /** Line instance for the subset being tested. */
        private final Line line;

         /** Construct a new instance for computing bounds intersection information with
          * the given line convex subset.
          * @param subset line convex subset to compute intersection information for
          */
        BoundsLinecaster2D(final LineConvexSubset subset) {
            super(subset.getLine().getPrecision());

            this.subset = subset;
            this.line = subset.getLine();
        }

        /** {@inheritDoc} */
        @Override
        protected Segment createSegment(final double startAbscissa, final double endAbscissa) {
            return line.segment(startAbscissa, endAbscissa);
        }

        /** {@inheritDoc} */
        @Override
        protected LinecastPoint2D createBoundaryIntersection(final Vector2D pt, final Vector2D normal) {
            return new LinecastPoint2D(pt, normal, line);
        }

        /** {@inheritDoc} */
        @Override
        protected void addBoundaryIntersections(final double abscissa, final List<LinecastPoint2D> results) {
            if (subset.classifyAbscissa(abscissa) != RegionLocation.OUTSIDE) {
                final Vector2D pt = line.toSpace(abscissa);

                addBoundaryIntersectionIfPresent(
                        pt,
                        Vector2D.Unit.MINUS_X,
                        Vector2D.Unit.PLUS_X,
                        Vector2D::getX,
                        results);

                addBoundaryIntersectionIfPresent(
                        pt,
                        Vector2D.Unit.MINUS_Y,
                        Vector2D.Unit.PLUS_Y,
                        Vector2D::getY,
                        results);
            }
        }

        /** {@inheritDoc} */
        @Override
        protected boolean computeNearFar() {
            return updateNearFar(Vector2D::getX) &&
                    updateNearFar(Vector2D::getY);
        }

        /** {@inheritDoc} */
        @Override
        protected Comparator<LinecastPoint2D> getBoundaryIntersectionComparator() {
            return LinecastPoint2D.ABSCISSA_ORDER;
        }

        /** {@inheritDoc} */
        @Override
        protected Vector2D getLineDir() {
            return line.getDirection();
        }

        /** {@inheritDoc} */
        @Override
        protected Vector2D getLineOrigin() {
            return line.getOrigin();
        }

        /** {@inheritDoc} */
        @Override
        protected double getSubspaceStart() {
            return subset.getSubspaceStart();
        }

        /** {@inheritDoc} */
        @Override
        protected double getSubspaceEnd() {
            return subset.getSubspaceEnd();
        }
    }
}
