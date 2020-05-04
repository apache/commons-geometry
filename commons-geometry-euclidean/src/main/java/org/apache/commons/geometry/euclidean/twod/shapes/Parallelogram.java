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
package org.apache.commons.geometry.euclidean.twod.shapes;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.twod.AffineTransformMatrix2D;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.LineConvexSubset;
import org.apache.commons.geometry.euclidean.twod.Lines;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.geometry.euclidean.twod.rotation.Rotation2D;

/** Class representing parallelograms, i.e. quadrilaterals with two pairs of parallel sides.
 * @see <a href="https://en.wikipedia.org/wiki/Parallelogram">Parallelogram</a>
 */
public final class Parallelogram extends ConvexArea {

    /** Vertices defining a square with sides of length 1 centered on the origin. */
    private static final List<Vector2D> UNIT_SQUARE_VERTICES = Arrays.asList(
                Vector2D.of(-0.5, -0.5),
                Vector2D.of(0.5, -0.5),
                Vector2D.of(0.5, 0.5),
                Vector2D.of(-0.5, 0.5)
            );

    /** Simple constructor. Callers are responsible for ensuring that the given path
     * represents a parallelogram. No validation is performed.
     * @param boundaries the boundaries of the parallelogram; this must be a list
     *      with 4 elements
     */
    private Parallelogram(final List<LineConvexSubset> boundaries) {
        super(boundaries);
    }

    /** Return a new instance representing a unit square centered on the origin.
     * The vertices of this square are:
     * <pre>
     * [
     *      (-0.5 -0.5),
     *      (0.5, -0.5),
     *      (0.5, 0.5),
     *      (-0.5, 0.5)
     * ]
     * </pre>
     * @param precision precision context used to construct boundaries
     * @return a new instance representing a unit square centered on the origin
     */
    public static Parallelogram unitSquare(final DoublePrecisionContext precision) {
        return fromTransformedUnitSquare(AffineTransformMatrix2D.identity(), precision);
    }

    /** Return a new instance representing an axis-aligned rectangle. The points {@code a}
     * and {@code b} are taken to represent opposite corner points in the rectangle and may be specified in
     * any order.
     * @param a first corner point in the rectangle (opposite of {@code b})
     * @param b second corner point in the rectangle (opposite of {@code a})
     * @param precision precision context used to construct boundaries
     * @return a new instance representing an axis-aligned rectangle
     * @throws IllegalArgumentException if the length of any side of the parallelogram is zero,
     *      as determined by the given precision context
     */
    public static Parallelogram axisAligned(final Vector2D a, final Vector2D b,
            final DoublePrecisionContext precision) {

        final double minX = Math.min(a.getX(), b.getX());
        final double maxX = Math.max(a.getX(), b.getX());

        final double minY = Math.min(a.getY(), b.getY());
        final double maxY = Math.max(a.getY(), b.getY());

        final double xDelta = maxX - minX;
        final double yDelta = maxY - minY;

        final Vector2D scale = Vector2D.of(xDelta, yDelta);
        final Vector2D position = Vector2D.of(
                    (0.5 * xDelta) + minX,
                    (0.5 * yDelta) + minY
                );

        return builder(precision)
                .setScale(scale)
                .setPosition(position)
                .build();
    }

    /** Create a new instance by transforming a unit square centered at the origin. The vertices
     * of this input square are:
     * <pre>
     * [
     *      (-0.5 -0.5),
     *      (0.5, -0.5),
     *      (0.5, 0.5),
     *      (-0.5, 0.5)
     * ]
     * </pre>
     * @param transform the transform to apply to the unit square
     * @param precision precision context used to construct boundaries
     * @return a new instance constructed by transforming the unit square
     * @throws IllegalArgumentException if the length of any side of the parallelogram is zero,
     *      as determined by the given precision context
     */
    public static Parallelogram fromTransformedUnitSquare(final Transform<Vector2D> transform,
            final DoublePrecisionContext precision) {

        final List<Vector2D> vertices = UNIT_SQUARE_VERTICES.stream()
                .map(transform).collect(Collectors.toList());

        final int len = vertices.size();
        final boolean preservesOrientation = transform.preservesOrientation();

        final List<LineConvexSubset> boundaries = new ArrayList<>(UNIT_SQUARE_VERTICES.size());

        Vector2D p0;
        Vector2D p1;
        LineConvexSubset boundary;
        for (int i = 0; i < len; ++i) {
            p0 = vertices.get(i);
            p1 = vertices.get((i + 1) % len);

            if (precision.eqZero(p0.distance(p1))) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Parallelogram has zero size: vertices {0} and {1} are equivalent", p0, p1));
            }

            boundary = preservesOrientation ?
                    Lines.segmentFromPoints(p0, p1, precision) :
                    Lines.segmentFromPoints(p1, p0, precision);

            boundaries.add(boundary);
        }

        return new Parallelogram(boundaries);
    }

    /** Return a new {@link Builder} instance to use for constructing parallelograms.
     * @param precision precision context used to create boundaries
     * @return a new {@link Builder} instance
     */
    public static Builder builder(final DoublePrecisionContext precision) {
        return new Builder(precision);
    }

    /** Class designed to aid construction of {@link Parallelogram} instances. Parallelograms are constructed
     * by transforming the vertices of a unit square centered at the origin with a transform built from
     * the values configured here. The transformations applied are <em>scaling</em>, <em>rotation</em>,
     * and <em>translation</em>, in that order. When applied in this order, the scale factors determine
     * the width and height of the parallelogram, the rotation determines the orientation, and the translation
     * determines the position of the center point.
     */
    public static final class Builder {

        /** Amount to scale the parallelogram. */
        private Vector2D scale = Vector2D.of(1, 1);

        /** The rotation of the parallelogram. */
        private Rotation2D rotation = Rotation2D.identity();

        /** Amount to translate the parallelogram. */
        private Vector2D position = Vector2D.ZERO;

        /** Precision context used to construct boundaries. */
        private final DoublePrecisionContext precision;

        /** Construct a new instance configured with the given precision context.
         * @param precision precision context used to create boundaries
         */
        private Builder(final DoublePrecisionContext precision) {
            this.precision = precision;
        }

        /** Set the center position of the created parallelogram.
         * @param pos center position of the created parallelogram
         * @return this instance
         */
        public Builder setPosition(final Vector2D pos) {
            this.position = pos;
            return this;
        }

        /** Set the scaling for the created parallelogram. The scale
         * values determine the lengths of the respective sides in the
         * created parallelogram.
         * @param scaleFactors scale factors
         * @return this instance
         */
        public Builder setScale(final Vector2D scaleFactors) {
            this.scale = scaleFactors;
            return this;
        }

        /** Set the scaling for the created parallelogram. The scale
         * values determine the lengths of the respective sides in the
         * created parallelogram.
         * @param x x scale factor
         * @param y y scale factor
         * @return this instance
         */
        public Builder setScale(final double x, final double y) {
            return setScale(Vector2D.of(x, y));
        }

        /** Set the scaling for the created parallelogram. The given scale
         * factor is applied to both the x and y directions.
         * @param scaleFactor scale factor for x and y directions
         * @return this instance
         */
        public Builder setScale(final double scaleFactor) {
            return setScale(scaleFactor, scaleFactor);
        }

        /** Set the rotation of the created parallelogram.
         * @param rot the rotation of the created parallelogram
         * @return this instance
         */
        public Builder setRotation(final Rotation2D rot) {
            this.rotation = rot;
            return this;
        }

        /** Set the rotation of the created parallelogram such that the
         * relative x-axis of the shape points in the given direction.
         * @param xDirection the direction of the relative x-axis
         * @return this instance
         * @throws IllegalArgumentException if the given vector cannot be normalized
         * @see #setRotation(Rotation2D)
         */
        public Builder setXDirection(final Vector2D xDirection) {
            return setRotation(
                    Rotation2D.createVectorRotation(Vector2D.Unit.PLUS_X, xDirection));
        }

        /** Set the rotation of the created parallelogram such that the
         * relative y-axis of the shape points in the given direction.
         * @param yDirection the direction of the relative y-axis
         * @return this instance
         * @throws IllegalArgumentException if the given vector cannot be normalized
         * @see #setRotation(Rotation2D)
         */
        public Builder setYDirection(final Vector2D yDirection) {
            return setRotation(
                    Rotation2D.createVectorRotation(Vector2D.Unit.PLUS_Y, yDirection));
        }

        /** Build a new parallelogram instance with the values configured in this builder.
         * @return a new parallelogram instance
         * @throws IllegalArgumentException if the length of any side of the parallelogram is zero,
         *      as determined by the configured precision context
         * @see Parallelogram#fromTransformedUnitSquare(Transform, DoublePrecisionContext)
         */
        public Parallelogram build() {
            final AffineTransformMatrix2D transform = AffineTransformMatrix2D.createScale(scale)
                    .rotate(rotation)
                    .translate(position);

            return fromTransformedUnitSquare(transform, precision);
        }
    }
}
