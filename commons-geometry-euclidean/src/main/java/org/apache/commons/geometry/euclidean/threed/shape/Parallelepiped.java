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
package org.apache.commons.geometry.euclidean.threed.shape;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.AffineTransformMatrix3D;
import org.apache.commons.geometry.euclidean.threed.ConvexVolume;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;

/** Class representing parallelepipeds, i.e. 3 dimensional figures formed by six
 * parallelograms. For example, cubes and rectangular prisms are parallelepipeds.
 * @see <a href="https://en.wikipedia.org/wiki/Parallelepiped">Parallelepiped</a>
 */
public final class Parallelepiped extends ConvexVolume {

    /** Vertices defining a cube with sides of length 1 centered at the origin. */
    private static final List<Vector3D> UNIT_CUBE_VERTICES = Arrays.asList(
                Vector3D.of(-0.5, -0.5, -0.5),
                Vector3D.of(0.5, -0.5, -0.5),
                Vector3D.of(0.5, 0.5, -0.5),
                Vector3D.of(-0.5, 0.5, -0.5),

                Vector3D.of(-0.5, -0.5, 0.5),
                Vector3D.of(0.5, -0.5, 0.5),
                Vector3D.of(0.5, 0.5, 0.5),
                Vector3D.of(-0.5, 0.5, 0.5)
            );

    /** Simple constructor. Callers are responsible for ensuring that the given boundaries
     * represent a parallelepiped. No validation is performed.
     * @param boundaries the boundaries of the parallelepiped; this must be a list
     *      with 6 elements
     */
    private Parallelepiped(final List<PlaneConvexSubset> boundaries) {
        super(boundaries);
    }

    /** Construct a new instance representing a unit cube centered at the origin. The vertices of this
     * cube are:
     * <pre>
     * [
     *      (-0.5, -0.5, -0.5),
     *      (0.5, -0.5, -0.5),
     *      (0.5, 0.5, -0.5),
     *      (-0.5, 0.5, -0.5),
     *
     *      (-0.5, -0.5, 0.5),
     *      (0.5, -0.5, 0.5),
     *      (0.5, 0.5, 0.5),
     *      (-0.5, 0.5, 0.5)
     * ]
     * </pre>
     * @param precision precision context used to construct boundaries
     * @return a new instance representing a unit cube centered at the origin
     */
    public static Parallelepiped unitCube(final DoublePrecisionContext precision) {
        return fromTransformedUnitCube(AffineTransformMatrix3D.identity(), precision);
    }

    /** Return a new instance representing an axis-aligned parallelepiped, ie, a rectangular prism.
     * The points {@code a} and {@code b} are taken to represent opposite corner points in the prism and may be
     * specified in any order.
     * @param a first corner point in the prism (opposite of {@code b})
     * @param b second corner point in the prism (opposite of {@code a})
     * @param precision precision context used to construct boundaries
     * @return a new instance representing an axis-aligned rectangular prism
     * @throws IllegalArgumentException if the width, height, or depth of the defined prism is zero
     *      as evaluated by the precision context.
     */
    public static Parallelepiped axisAligned(final Vector3D a, final Vector3D b,
            final DoublePrecisionContext precision) {

        final double minX = Math.min(a.getX(), b.getX());
        final double maxX = Math.max(a.getX(), b.getX());

        final double minY = Math.min(a.getY(), b.getY());
        final double maxY = Math.max(a.getY(), b.getY());

        final double minZ = Math.min(a.getZ(), b.getZ());
        final double maxZ = Math.max(a.getZ(), b.getZ());

        final double xDelta = maxX - minX;
        final double yDelta = maxY - minY;
        final double zDelta = maxZ - minZ;

        final Vector3D scale = Vector3D.of(xDelta, yDelta, zDelta);
        final Vector3D position = Vector3D.of(
                    (0.5 * xDelta) + minX,
                    (0.5 * yDelta) + minY,
                    (0.5 * zDelta) + minZ
                );

        return builder(precision)
                .setScale(scale)
                .setPosition(position)
                .build();
    }

    /** Construct a new instance by transforming a unit cube centered at the origin. The vertices of
     * this input cube are:
     * <pre>
     * [
     *      (-0.5, -0.5, -0.5),
     *      (0.5, -0.5, -0.5),
     *      (0.5, 0.5, -0.5),
     *      (-0.5, 0.5, -0.5),
     *
     *      (-0.5, -0.5, 0.5),
     *      (0.5, -0.5, 0.5),
     *      (0.5, 0.5, 0.5),
     *      (-0.5, 0.5, 0.5)
     * ]
     * </pre>
     * @param transform transform to apply to the vertices of the unit cube
     * @param precision precision context used to construct boundaries
     * @return a new instance created by transforming the vertices of a unit cube centered at the origin
     * @throws IllegalArgumentException if the width, height, or depth of the defined shape is zero
     *      as evaluated by the precision context.
     */
    public static Parallelepiped fromTransformedUnitCube(final Transform<Vector3D> transform,
            final DoublePrecisionContext precision) {

        final List<Vector3D> vertices = UNIT_CUBE_VERTICES.stream()
                .map(transform)
                .collect(Collectors.toList());
        final boolean reverse = !transform.preservesOrientation();

        // check lengths in each dimension
        ensureNonZeroSideLength(vertices.get(0), vertices.get(1), precision);
        ensureNonZeroSideLength(vertices.get(1), vertices.get(2), precision);
        ensureNonZeroSideLength(vertices.get(0), vertices.get(4), precision);

        final List<PlaneConvexSubset> boundaries = Arrays.asList(
                    // planes orthogonal to x
                    createFace(0, 4, 7, 3, vertices, reverse, precision),
                    createFace(1, 2, 6, 5, vertices, reverse, precision),

                    // planes orthogonal to y
                    createFace(0, 1, 5, 4, vertices, reverse, precision),
                    createFace(3, 7, 6, 2, vertices, reverse, precision),

                    // planes orthogonal to z
                    createFace(0, 3, 2, 1, vertices, reverse, precision),
                    createFace(4, 5, 6, 7, vertices, reverse, precision)
                );

        return new Parallelepiped(boundaries);
    }

    /** Return a new {@link Builder} instance to use for constructing parallelepipeds.
     * @param precision precision context used to create boundaries
     * @return a new {@link Builder} instance
     */
    public static Builder builder(final DoublePrecisionContext precision) {
        return new Builder(precision);
    }

    /** Create a single face of a parallelepiped using the indices of elements in the given vertex list.
     * @param a first vertex index
     * @param b second vertex index
     * @param c third vertex index
     * @param d fourth vertex index
     * @param vertices list of vertices for the parallelepiped
     * @param reverse if true, reverse the orientation of the face
     * @param precision precision context used to create the face
     * @return a parallelepiped face created from the indexed vertices
     */
    private static PlaneConvexSubset createFace(final int a, final int b, final int c, final int d,
            final List<Vector3D> vertices, final boolean reverse, final DoublePrecisionContext precision) {

        final Vector3D pa = vertices.get(a);
        final Vector3D pb = vertices.get(b);
        final Vector3D pc = vertices.get(c);
        final Vector3D pd = vertices.get(d);

        final List<Vector3D> loop = reverse ?
                Arrays.asList(pd, pc, pb, pa) :
                Arrays.asList(pa, pb, pc, pd);

        return Planes.subsetFromVertexLoop(loop, precision);
    }

    /** Ensure that the given points defining one side of a parallelepiped face are separated by a non-zero
     * distance, as determined by the precision context.
     * @param a first vertex
     * @param b second vertex
     * @param precision precision used to evaluate the distance between the two points
     * @throws IllegalArgumentException if the given points are equivalent according to the precision context
     */
    private static void ensureNonZeroSideLength(final Vector3D a, final Vector3D b,
            final DoublePrecisionContext precision) {
        if (precision.eqZero(a.distance(b))) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Parallelepiped has zero size: vertices {0} and {1} are equivalent", a, b));
        }
    }

    /** Class designed to aid construction of {@link Parallelepiped} instances. Parallelepipeds are constructed
     * by transforming the vertices of a unit cube centered at the origin with a transform built from
     * the values configured here. The transformations applied are <em>scaling</em>, <em>rotation</em>,
     * and <em>translation</em>, in that order. When applied in this order, the scale factors determine
     * the width, height, and depth of the parallelepiped; the rotation determines the orientation; and the
     * translation determines the position of the center point.
     */
    public static final class Builder {

        /** Amount to scale the parallelepiped. */
        private Vector3D scale = Vector3D.of(1, 1, 1);

        /** The rotation of the parallelepiped. */
        private QuaternionRotation rotation = QuaternionRotation.identity();

        /** Amount to translate the parallelepiped. */
        private Vector3D position = Vector3D.ZERO;

        /** Precision context used to construct boundaries. */
        private final DoublePrecisionContext precision;

        /** Construct a new instance configured with the given precision context.
         * @param precision precision context used to create boundaries
         */
        private Builder(final DoublePrecisionContext precision) {
            this.precision = precision;
        }

        /** Set the center position of the created parallelepiped.
         * @param pos center position of the created parallelepiped
         * @return this instance
         */
        public Builder setPosition(final Vector3D pos) {
            this.position = pos;
            return this;
        }

        /** Set the scaling for the created parallelepiped. The scale values determine
         * the lengths of the respective sides in the created parallelepiped.
         * @param scaleFactors scale factors
         * @return this instance
         */
        public Builder setScale(final Vector3D scaleFactors) {
            this.scale = scaleFactors;
            return this;
        }

        /** Set the scaling for the created parallelepiped. The scale values determine
         * the lengths of the respective sides in the created parallelepiped.
         * @param x x scale factor
         * @param y y scale factor
         * @param z z scale factor
         * @return this instance
         */
        public Builder setScale(final double x, final double y, final double z) {
            return setScale(Vector3D.of(x, y, z));
        }

        /** Set the scaling for the created parallelepiped. The given scale factor is applied
         * to the x, y, and z directions.
         * @param scaleFactor scale factor for the x, y, and z directions
         * @return this instance
         */
        public Builder setScale(final double scaleFactor) {
            return setScale(scaleFactor, scaleFactor, scaleFactor);
        }

        /** Set the rotation of the created parallelepiped.
         * @param rot the rotation of the created parallelepiped
         * @return this instance
         */
        public Builder setRotation(final QuaternionRotation rot) {
            this.rotation = rot;
            return this;
        }

        /** Build a new parallelepiped instance with the values configured in this builder.
         * @return a new parallelepiped instance
         * @throws IllegalArgumentException if the length of any side of the parallelepiped is zero,
         *      as determined by the configured precision context
         * @see Parallelepiped#fromTransformedUnitCube(Transform, DoublePrecisionContext)
         */
        public Parallelepiped build() {
            final AffineTransformMatrix3D transform = AffineTransformMatrix3D.createScale(scale)
                    .rotate(rotation)
                    .translate(position);

            return fromTransformedUnitCube(transform, precision);
        }
    }
}
