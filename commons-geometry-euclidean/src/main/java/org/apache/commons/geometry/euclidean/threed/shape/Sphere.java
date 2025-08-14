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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.partitioning.bsp.RegionCutRule;
import org.apache.commons.geometry.euclidean.AbstractNSphere;
import org.apache.commons.geometry.euclidean.threed.Plane;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D.RegionNode3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.line.Line3D;
import org.apache.commons.geometry.euclidean.threed.line.LineConvexSubset3D;
import org.apache.commons.geometry.euclidean.threed.line.LinecastPoint3D;
import org.apache.commons.geometry.euclidean.threed.line.Linecastable3D;
import org.apache.commons.geometry.euclidean.threed.mesh.SimpleTriangleMesh;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.apache.commons.numbers.core.Precision;

/** Class representing a 3-dimensional sphere in Euclidean space.
 */
public final class Sphere extends AbstractNSphere<Vector3D> implements Linecastable3D {

    /** Message used when requesting a sphere approximation with an invalid subdivision number. */
    private static final String INVALID_SUBDIVISION_MESSAGE =
        "Number of sphere approximation subdivisions must be greater than or equal to zero; was {0}";

    /** Constant equal to {@code 4 * pi}. */
    private static final double FOUR_PI = 4.0 * Math.PI;

    /** Constant equal to {@code (4/3) * pi}. */
    private static final double FOUR_THIRDS_PI = FOUR_PI / 3.0;

    /** Construct a new sphere from its component parts.
     * @param center the center of the sphere
     * @param radius the sphere radius
     * @param precision precision context used to compare floating point numbers
     * @throws IllegalArgumentException if center is not finite or radius is not finite or is
     *      less than or equal to zero as evaluated by the given precision context
     */
    private Sphere(final Vector3D center, final double radius, final Precision.DoubleEquivalence precision) {
        super(center, radius, precision);
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        final double r = getRadius();
        return FOUR_THIRDS_PI * r * r * r;
    }

    /** {@inheritDoc} */
    @Override
    public double getBoundarySize() {
        final double r = getRadius();
        return FOUR_PI * r * r;
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D project(final Vector3D pt) {
        return project(pt, Vector3D.Unit.PLUS_X);
    }

    /** Build an approximation of this sphere using a {@link RegionBSPTree3D}. The approximation is constructed by
     * taking an octahedron (8-sided polyhedron with triangular faces) inscribed in the sphere and subdividing each
     * triangular face {@code subdivisions} number of times, each time projecting the newly created vertices onto the
     * sphere surface. Each triangle subdivision produces 4 triangles, meaning that the total number of triangles
     * inserted into tree is equal to \(8 \times 4^s\), where \(s\) is the number of subdivisions. For
     * example, calling this method with {@code subdivisions} equal to {@code 3} will produce a tree having
     * \(8 \times 4^3 = 512\) triangular facets inserted. See the table below for other examples. The returned BSP
     * tree also contains structural cuts to reduce the overall height of the tree.
     *
     * <table>
     *  <caption>Subdivisions to Triangle Counts</caption>
     *  <thead>
     *      <tr>
     *          <th>Subdivisions</th>
     *          <th>Triangles</th>
     *      </tr>
     *  </thead>
     *  <tbody>
     *      <tr><td>0</td><td>8</td></tr>
     *      <tr><td>1</td><td>32</td></tr>
     *      <tr><td>2</td><td>128</td></tr>
     *      <tr><td>3</td><td>512</td></tr>
     *      <tr><td>4</td><td>2048</td></tr>
     *      <tr><td>5</td><td>8192</td></tr>
     *  </tbody>
     * </table>
     *
     * <p>Care must be taken when using this method with large subdivision numbers so that floating point errors
     * do not interfere with the creation of the planes and triangles in the tree. For example, if the number of
     * subdivisions is too high, the subdivided triangle points may become equivalent according to the sphere's
     * {@link #getPrecision() precision context} and plane creation may fail. Or plane creation may succeed but
     * insertion of the plane into the tree may fail for similar reasons. In general, it is best to use the lowest
     * subdivision number practical for the intended purpose.</p>
     * @param subdivisions the number of triangle subdivisions to use when creating the tree; the total number of
     *      triangular facets inserted into the returned tree is equal to \(8 \times 4^s\), where \(s\) is the number
     *      of subdivisions
     * @return a BSP tree containing an approximation of the sphere
     * @throws IllegalArgumentException if {@code subdivisions} is less than zero
     * @throws IllegalStateException if tree creation fails for the given subdivision count
     * @see #toTriangleMesh(int)
     */
    public RegionBSPTree3D toTree(final int subdivisions) {
        if (subdivisions < 0) {
            throw new IllegalArgumentException(MessageFormat.format(INVALID_SUBDIVISION_MESSAGE, subdivisions));
        }
        return new SphereTreeApproximationBuilder(this, subdivisions).build();
    }

    /** Build an approximation of this sphere using a {@link TriangleMesh}. The approximation is constructed by
     * taking an octahedron (8-sided polyhedron with triangular faces) inscribed in the sphere and subdividing each
     * triangular face {@code subdivisions} number of times, each time projecting the newly created vertices onto the
     * sphere surface. Each triangle subdivision produces 4 triangles, meaning that the total number of triangles
     * in the returned mesh is equal to \(8 \times 4^s\), where \(s\) is the number of subdivisions. For
     * example, calling this method with {@code subdivisions} equal to {@code 3} will produce a mesh having
     * \(8 \times 4^3 = 512\) triangular facets inserted. See the table below for other examples.
     *
     * <table>
     *  <caption>Subdivisions to Triangle Counts</caption>
     *  <thead>
     *      <tr>
     *          <th>Subdivisions</th>
     *          <th>Triangles</th>
     *      </tr>
     *  </thead>
     *  <tbody>
     *      <tr><td>0</td><td>8</td></tr>
     *      <tr><td>1</td><td>32</td></tr>
     *      <tr><td>2</td><td>128</td></tr>
     *      <tr><td>3</td><td>512</td></tr>
     *      <tr><td>4</td><td>2048</td></tr>
     *      <tr><td>5</td><td>8192</td></tr>
     *  </tbody>
     * </table>
     *
     * <p><strong>BSP Tree Conversion</strong></p>
     * <p>Inserting the boundaries of a sphere mesh approximation directly into a BSP tree will invariably result
     * in poor performance: since the region is convex the constructed BSP tree degenerates into a simple linked
     * list of nodes. If a BSP tree is needed, users should prefer the {@link #toTree(int)} method, which creates
     * balanced tree approximations directly, or the {@link RegionBSPTree3D.PartitionedRegionBuilder3D} class,
     * which can be used to insert the mesh faces into a pre-partitioned tree.
     * </p>
     * @param subdivisions the number of triangle subdivisions to use when creating the mesh; the total number of
     *      triangular faces in the returned mesh is equal to \(8 \times 4^s\), where \(s\) is the number
     *      of subdivisions
     * @return a triangle mesh approximation of the sphere
     * @throws IllegalArgumentException if {@code subdivisions} is less than zero
     * @see #toTree(int)
     */
    public TriangleMesh toTriangleMesh(final int subdivisions) {
        if (subdivisions < 0) {
            throw new IllegalArgumentException(MessageFormat.format(INVALID_SUBDIVISION_MESSAGE, subdivisions));
        }
        return new SphereMeshApproximationBuilder(this, subdivisions).build();
    }

    /** Get the intersections of the given line with this sphere. The returned list will
     * contain either 0, 1, or 2 points.
     * <ul>
     *      <li><strong>2 points</strong> - The line is a secant line and intersects the sphere at two
     *      distinct points. The points are ordered such that the first point in the list is the first point
     *      encountered when traveling in the direction of the line. (In other words, the points are ordered
     *      by increasing abscissa value.)
     *      </li>
     *      <li><strong>1 point</strong> - The line is a tangent line and only intersects the sphere at a
     *      single point (as evaluated by the sphere's precision context).
     *      </li>
     *      <li><strong>0 points</strong> - The line does not intersect the sphere.</li>
     * </ul>
     * @param line line to intersect with the sphere
     * @return a list of intersection points between the given line and this sphere
     */
    public List<Vector3D> intersections(final Line3D line) {
        return intersections(line, Line3D::abscissa, Line3D::distance);
    }

    /** Get the first intersection point between the given line and this sphere, or {@code null}
     * if no such point exists. The "first" intersection point is the first such point
     * encountered when traveling in the direction of the line from infinity.
     * @param line line to intersect with the sphere
     * @return the first intersection point between the given line and this instance or
     *      null if no such point exists
     */
    public Vector3D firstIntersection(final Line3D line) {
        return firstIntersection(line, Line3D::abscissa, Line3D::distance);
    }

    /** {@inheritDoc} */
    @Override
    public List<LinecastPoint3D> linecast(final LineConvexSubset3D subset) {
        return getLinecastStream(subset)
                .collect(Collectors.toList());
    }

    /** {@inheritDoc} */
    @Override
    public LinecastPoint3D linecastFirst(final LineConvexSubset3D subset) {
        return getLinecastStream(subset)
                .findFirst()
                .orElse(null);
    }

    /** Get a stream containing the linecast intersection points of the given
     * line subset with this instance.
     * @param subset line subset to intersect against this instance
     * @return a stream containing linecast intersection points
     */
    private Stream<LinecastPoint3D> getLinecastStream(final LineConvexSubset3D subset) {
        return intersections(subset.getLine()).stream()
            .filter(subset::contains)
            .map(pt -> new LinecastPoint3D(pt, getCenter().directionTo(pt), subset.getLine()));
    }

    /** Construct a sphere from a center point and radius.
     * @param center the center of the sphere
     * @param radius the sphere radius
     * @param precision precision context used to compare floating point numbers
     * @return a sphere constructed from the given center point and radius
     * @throws IllegalArgumentException if center is not finite or radius is not finite or is
     *      less than or equal to zero as evaluated by the given precision context
     */
    public static Sphere from(final Vector3D center, final double radius, final Precision.DoubleEquivalence precision) {
        return new Sphere(center, radius, precision);
    }

    /** Internal class used to construct hyperplane-bounded approximations of spheres as BSP trees. The class
     * begins with an octahedron inscribed in the sphere and then subdivides each triangular face a specified
     * number of times.
     */
    private static final class SphereTreeApproximationBuilder {

        /** Threshold used to determine when to stop inserting structural cuts and begin adding facets. */
        private static final int PARTITION_THRESHOLD = 2;

        /** The sphere that an approximation is being created for. */
        private final Sphere sphere;

        /** The number of triangular subdivisions to use. */
        private final int subdivisions;

        /** Construct a new builder for creating a BSP tree approximation of the given sphere.
         * @param sphere the sphere to create an approximation of
         * @param subdivisions the number of triangle subdivisions to use in tree creation
         */
        SphereTreeApproximationBuilder(final Sphere sphere, final int subdivisions) {
            this.sphere = sphere;
            this.subdivisions = subdivisions;
        }

        /** Build the sphere approximation BSP tree.
         * @return the sphere approximation BSP tree
         * @throws IllegalStateException if tree creation fails for the configured subdivision count
         */
        RegionBSPTree3D build() {
            final RegionBSPTree3D tree = RegionBSPTree3D.empty();

            final Vector3D center = sphere.getCenter();
            final double radius = sphere.getRadius();
            final Precision.DoubleEquivalence precision = sphere.getPrecision();

            // insert the primary split planes
            final Plane plusXPlane = Planes.fromPointAndNormal(center, Vector3D.Unit.PLUS_X, precision);
            final Plane plusYPlane = Planes.fromPointAndNormal(center, Vector3D.Unit.PLUS_Y, precision);
            final Plane plusZPlane = Planes.fromPointAndNormal(center, Vector3D.Unit.PLUS_Z, precision);

            tree.insert(plusXPlane.span(), RegionCutRule.INHERIT);
            tree.insert(plusYPlane.span(), RegionCutRule.INHERIT);
            tree.insert(plusZPlane.span(), RegionCutRule.INHERIT);

            // create the vertices for the octahedron
            final double cx = center.getX();
            final double cy = center.getY();
            final double cz = center.getZ();

            final Vector3D maxX = Vector3D.of(cx + radius, cy, cz);
            final Vector3D minX = Vector3D.of(cx - radius, cy, cz);

            final Vector3D maxY = Vector3D.of(cx, cy + radius, cz);
            final Vector3D minY = Vector3D.of(cx, cy - radius, cz);

            final Vector3D maxZ = Vector3D.of(cx, cy, cz + radius);
            final Vector3D minZ = Vector3D.of(cx, cy, cz - radius);

            // partition and subdivide the face triangles and insert them into the tree
            final RegionNode3D root = tree.getRoot();

            try {
                partitionAndInsert(root.getMinus().getMinus().getMinus(), minX, minZ, minY, 0);
                partitionAndInsert(root.getMinus().getMinus().getPlus(), minX, minY, maxZ, 0);

                partitionAndInsert(root.getMinus().getPlus().getMinus(), minX, maxY, minZ, 0);
                partitionAndInsert(root.getMinus().getPlus().getPlus(), minX, maxZ, maxY, 0);

                partitionAndInsert(root.getPlus().getMinus().getMinus(), maxX, minY, minZ, 0);
                partitionAndInsert(root.getPlus().getMinus().getPlus(), maxX, maxZ, minY, 0);

                partitionAndInsert(root.getPlus().getPlus().getMinus(), maxX, minZ, maxY, 0);
                partitionAndInsert(root.getPlus().getPlus().getPlus(), maxX, maxY, maxZ, 0);
            } catch (final IllegalStateException | IllegalArgumentException exc) {
                // standardize any tree construction failure as an IllegalStateException
                throw new IllegalStateException("Failed to construct sphere approximation with subdivision count " +
                        subdivisions + ": " + exc.getMessage(), exc);
            }

            return tree;
        }

        /** Recursively insert structural BSP tree cuts into the given node and then insert subdivided triangles
         * when a target subdivision level is reached. The structural BSP tree cuts are used to help reduce the
         * overall depth of the BSP tree.
         * @param node the node to insert into
         * @param p1 first triangle point
         * @param p2 second triangle point
         * @param p3 third triangle point
         * @param level current subdivision level
         */
        private void partitionAndInsert(final RegionNode3D node,
                                        final Vector3D p1, final Vector3D p2, final Vector3D p3, final int level) {

            if (subdivisions - level > PARTITION_THRESHOLD) {
                final int nextLevel = level + 1;

                final Vector3D center = sphere.getCenter();

                final Vector3D m1 = sphere.project(p1.lerp(p2, 0.5));
                final Vector3D m2 = sphere.project(p2.lerp(p3, 0.5));
                final Vector3D m3 = sphere.project(p3.lerp(p1, 0.5));

                RegionNode3D curNode = node;

                checkedCut(curNode, createPlane(m3, m2, center), RegionCutRule.INHERIT);
                partitionAndInsert(curNode.getPlus(), m3, m2, p3, nextLevel);

                curNode = curNode.getMinus();
                checkedCut(curNode, createPlane(m2, m1, center), RegionCutRule.INHERIT);
                partitionAndInsert(curNode.getPlus(), m1, p2, m2, nextLevel);

                curNode = curNode.getMinus();
                checkedCut(curNode, createPlane(m1, m3, center), RegionCutRule.INHERIT);
                partitionAndInsert(curNode.getPlus(), p1, m1, m3, nextLevel);

                partitionAndInsert(curNode.getMinus(), m1, m2, m3, nextLevel);
            } else {
                insertSubdividedTriangles(node, p1, p2, p3, level);
            }
        }

        /** Recursively insert subdivided triangles into the given node. Each triangle is inserted into the minus
         * side of the previous triangle.
         * @param node the node to insert into
         * @param p1 first triangle point
         * @param p2 second triangle point
         * @param p3 third triangle point
         * @param level the current subdivision level
         * @return the node representing the inside of the region after insertion of all triangles
         */
        private RegionNode3D insertSubdividedTriangles(final RegionNode3D node,
                                                       final Vector3D p1, final Vector3D p2, final Vector3D p3,
                                                       final int level) {

            if (level >= subdivisions) {
                // base case
                checkedCut(node, createPlane(p1, p2, p3), RegionCutRule.MINUS_INSIDE);
                return node.getMinus();
            } else {
                final int nextLevel = level + 1;

                final Vector3D m1 = sphere.project(p1.lerp(p2, 0.5));
                final Vector3D m2 = sphere.project(p2.lerp(p3, 0.5));
                final Vector3D m3 = sphere.project(p3.lerp(p1, 0.5));

                RegionNode3D curNode = node;
                curNode = insertSubdividedTriangles(curNode, p1, m1, m3, nextLevel);
                curNode = insertSubdividedTriangles(curNode, m1, p2, m2, nextLevel);
                curNode = insertSubdividedTriangles(curNode, m3, m2, p3, nextLevel);
                curNode = insertSubdividedTriangles(curNode, m1, m2, m3, nextLevel);

                return curNode;
            }
        }

        /** Create a plane from the given points, using the precision context of the sphere.
         * @param p1 first point
         * @param p2 second point
         * @param p3 third point
         * @return a plane defined by the given points
         */
        private Plane createPlane(final Vector3D p1, final Vector3D p2, final Vector3D p3) {
            return Planes.fromPoints(p1, p2, p3, sphere.getPrecision());
        }

        /** Insert the cut into the given node, throwing an exception if no portion of the cutter intersects
         * the node.
         * @param node node to cut
         * @param cutter plane to use to cut the node
         * @param cutRule cut rule to apply
         * @throws IllegalStateException if no portion of the cutter plane intersects the node
         */
        private void checkedCut(final RegionNode3D node, final Plane cutter, final RegionCutRule cutRule) {
            if (!node.insertCut(cutter, cutRule)) {
                throw new IllegalStateException("Failed to cut BSP tree node with plane: " + cutter);
            }
        }
    }

    /** Internal class used to construct geodesic mesh sphere approximations. The class begins with an octahedron
     * inscribed in the sphere and then subdivides each triangular face a specified number of times.
     */
    private static final class SphereMeshApproximationBuilder {

        /** The sphere that an approximation is being created for. */
        private final Sphere sphere;

        /** The number of triangular subdivisions to use. */
        private final int subdivisions;

        /** Mesh builder object. */
        private final SimpleTriangleMesh.Builder builder;

        /** Construct a new builder for creating a mesh approximation of the given sphere.
         * @param sphere the sphere to create an approximation of
         * @param subdivisions the number of triangle subdivisions to use in mesh creation
         */
        SphereMeshApproximationBuilder(final Sphere sphere, final int subdivisions) {
            this.sphere = sphere;
            this.subdivisions = subdivisions;
            this.builder = SimpleTriangleMesh.builder(sphere.getPrecision());
        }

        /** Build the mesh approximation of the configured sphere.
         * @return the mesh approximation of the configured sphere
         */
        public SimpleTriangleMesh build() {
            final Vector3D center = sphere.getCenter();
            final double radius = sphere.getRadius();

            // create the vertices for the octahedron
            final double cx = center.getX();
            final double cy = center.getY();
            final double cz = center.getZ();

            final Vector3D maxX = Vector3D.of(cx + radius, cy, cz);
            final Vector3D minX = Vector3D.of(cx - radius, cy, cz);

            final Vector3D maxY = Vector3D.of(cx, cy + radius, cz);
            final Vector3D minY = Vector3D.of(cx, cy - radius, cz);

            final Vector3D maxZ = Vector3D.of(cx, cy, cz + radius);
            final Vector3D minZ = Vector3D.of(cx, cy, cz - radius);

            addSubdivided(minX, minZ, minY, 0);
            addSubdivided(minX, minY, maxZ, 0);

            addSubdivided(minX, maxY, minZ, 0);
            addSubdivided(minX, maxZ, maxY, 0);

            addSubdivided(maxX, minY, minZ, 0);
            addSubdivided(maxX, maxZ, minY, 0);

            addSubdivided(maxX, minZ, maxY, 0);
            addSubdivided(maxX, maxY, maxZ, 0);

            return builder.build();
        }

        /** Recursively subdivide and add triangular faces between the given outer boundary points.
         * @param p1 first point
         * @param p2 second point
         * @param p3 third point
         * @param level recursion level; counts up
         */
        private void addSubdivided(final Vector3D p1, final Vector3D p2, final Vector3D p3, final int level) {
            if (level >= subdivisions) {
                // base case
                builder.addFaceUsingVertices(p1, p2, p3);
            } else {
                // subdivide
                final int nextLevel = level + 1;

                final Vector3D m1 = sphere.project(p1.lerp(p2, 0.5));
                final Vector3D m2 = sphere.project(p2.lerp(p3, 0.5));
                final Vector3D m3 = sphere.project(p3.lerp(p1, 0.5));

                addSubdivided(p1, m1, m3, nextLevel);
                addSubdivided(m1, p2, m2, nextLevel);
                addSubdivided(m3, m2, p3, nextLevel);
                addSubdivided(m1, m2, m3, nextLevel);
            }
        }
    }
}
