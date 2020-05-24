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
package org.apache.commons.geometry.euclidean.threed;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

import org.apache.commons.geometry.core.partitioning.HyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.line.Line3D;
import org.apache.commons.geometry.euclidean.threed.line.LineConvexSubset3D;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.Lines;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

/** Class containing factory methods for constructing {@link Plane} and {@link PlaneSubset} instances.
 */
public final class Planes {

    /** Utility class; no instantiation. */
    private Planes() {
    }

    /** Build a plane from a point and two (on plane) vectors.
     * @param p the provided point (on plane)
     * @param u u vector (on plane)
     * @param v v vector (on plane)
     * @param precision precision context used to compare floating point values
     * @return a new plane
     * @throws IllegalArgumentException if the norm of the given values is zero, NaN, or infinite.
     */
    public static EmbeddingPlane fromPointAndPlaneVectors(final Vector3D p, final Vector3D u, final Vector3D v,
            final DoublePrecisionContext precision) {
        final Vector3D.Unit uNorm = u.normalize();
        final Vector3D.Unit vNorm = uNorm.orthogonal(v);
        final Vector3D.Unit wNorm = uNorm.cross(vNorm).normalize();
        final double originOffset = -p.dot(wNorm);

        return new EmbeddingPlane(uNorm, vNorm, wNorm, originOffset, precision);
    }

    /** Build a plane from a normal.
     * Chooses origin as point on plane.
     * @param normal normal direction to the plane
     * @param precision precision context used to compare floating point values
     * @return a new plane
     * @throws IllegalArgumentException if the norm of the given values is zero, NaN, or infinite.
     */
    public static Plane fromNormal(final Vector3D normal, final DoublePrecisionContext precision) {
        return fromPointAndNormal(Vector3D.ZERO, normal, precision);
    }

    /** Build a plane from a point and a normal.
     *
     * @param p point belonging to the plane
     * @param normal normal direction to the plane
     * @param precision precision context used to compare floating point values
     * @return a new plane
     * @throws IllegalArgumentException if the norm of the given values is zero, NaN, or infinite.
     */
    public static Plane fromPointAndNormal(final Vector3D p, final Vector3D normal,
            final DoublePrecisionContext precision) {
        final Vector3D.Unit unitNormal = normal.normalize();
        final double originOffset = -p.dot(unitNormal);

        return new Plane(unitNormal, originOffset, precision);
    }

    /** Build a plane from three points.
     * <p>
     * The plane is oriented in the direction of {@code (p2-p1) ^ (p3-p1)}
     * </p>
     *
     * @param p1 first point belonging to the plane
     * @param p2 second point belonging to the plane
     * @param p3 third point belonging to the plane
     * @param precision precision context used to compare floating point values
     * @return a new plane
     * @throws IllegalArgumentException if the points do not define a unique plane
     */
    public static Plane fromPoints(final Vector3D p1, final Vector3D p2, final Vector3D p3,
            final DoublePrecisionContext precision) {
        return fromPoints(Arrays.asList(p1, p2, p3), precision);
    }

    /** Construct a plane from a collection of points lying on the plane. The plane orientation is
     * determined by the overall orientation of the point sequence. For example, if the points wind
     * around the z-axis in a counter-clockwise direction, then the plane normal will point up the
     * +z axis. If the points wind in the opposite direction, then the plane normal will point down
     * the -z axis. The {@code u} vector for the plane is set to the first non-zero vector between
     * points in the sequence (ie, the first direction in the path).
     *
     * @param pts collection of sequenced points lying on the plane
     * @param precision precision context used to compare floating point values
     * @return a new plane containing the given points
     * @throws IllegalArgumentException if the given collection does not contain at least 3 points or the
     *      points do not define a unique plane
     */
    public static Plane fromPoints(final Collection<Vector3D> pts, final DoublePrecisionContext precision) {
        return new PlaneBuilder(pts, precision).build();
    }

    /** Create a new plane subset from a plane and an embedded convex subspace area.
     * @param plane embedding plane for the area
     * @param area area embedded in the plane
     * @return a new convex sub plane instance
     */
    public static PlaneConvexSubset subsetFromConvexArea(final EmbeddingPlane plane, final ConvexArea area) {
        if (area.isFinite()) {
            // prefer a vertex-based representation for finite areas
            final List<Vector3D> vertices = plane.toSpace(area.getVertices());

            return fromConvexPlanarVertices(plane, vertices);
        }

        return new EmbeddedAreaPlaneConvexSubset(plane, area);
    }

    /** Create a new convex polygon from the given sequence of vertices. The vertices must define a unique
     * plane, meaning that at least 3 unique vertices must be given. The given sequence is assumed to be closed,
     * ie that an edge exists between the last vertex and the first.
     * @param pts collection of points defining the convex polygon
     * @param precision precision context used to compare floating point values
     * @return a new convex polygon defined by the given sequence of vertices
     * @throws IllegalArgumentException if fewer than 3 vertices are given or the vertices do not define a
     *       unique plane
     * @see #fromPoints(Collection, DoublePrecisionContext)
     */
    public static ConvexPolygon3D convexPolygonFromVertices(final Collection<Vector3D> pts,
            final DoublePrecisionContext precision) {
        final List<Vector3D> vertices = new ArrayList<>(pts.size());
        final Plane plane = new PlaneBuilder(pts, precision).buildForConvexPolygon(vertices);

        // make sure that the first point is not repeated at the end
        final Vector3D firstPt = vertices.get(0);
        final Vector3D lastPt = vertices.get(vertices.size() - 1);
        if (firstPt.eq(lastPt, precision)) {
            vertices.remove(vertices.size() - 1);
        }

        if (vertices.size() == 3) {
            return new SimpleTriangle3D(plane, vertices.get(0), vertices.get(1), vertices.get(2));
        }
        return new VertexListConvexPolygon3D(plane, vertices);
    }

    /** Construct a triangle from three vertices. The triangle plane is oriented such that the points
     * are arranged in a counter-clockwise order when looking down the plane normal.
     * @param p1 first vertex
     * @param p2 second vertex
     * @param p3 third vertex
     * @param precision precision context used for floating point comparisons
     * @return a triangle constructed from the three vertices
     * @throws IllegalArgumentException if the points do not define a unique plane
     */
    public static Triangle3D triangleFromVertices(final Vector3D p1, final Vector3D p2, final Vector3D p3,
            final DoublePrecisionContext precision) {
        final Plane plane = fromPoints(p1, p2, p3, precision);
        return new SimpleTriangle3D(plane, p1, p2, p3);
    }

    /** Construct a list of {@link Triangle3D} instances from a set of vertices and arrays of face indices.
     * For example, the following code constructs a list of triangles forming a square pyramid.
     * <pre>
     * DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-10);
     *
     * Vector3D[] vertices = {
     *      Vector3D.ZERO,
     *      Vector3D.of(1, 0, 0),
     *      Vector3D.of(1, 1, 0),
     *      Vector3D.of(0, 1, 0),
     *      Vector3D.of(0.5, 0.5, 4)
     * };
     *
     * int[][] faceIndices = {
     *      {0, 2, 1},
     *      {0, 3, 2},
     *      {0, 1, 4},
     *      {1, 2, 4},
     *      {2, 3, 4},
     *      {3, 0, 4}
     * };
     *
     * List&lt;Triangle3D&gt; triangles = Planes.indexedTriangles(vertices, faceIndices, TEST_PRECISION);
     * </pre>
     * @param vertices vertices available for use in triangle construction
     * @param faceIndices array of indices for each triangular face; each entry in the array is an array of
     *      3 index values into {@code vertices}, defining the 3 vertices that will be used to construct the
     *      triangle
     * @param precision precision context used for floating point comparisons
     * @return a list of triangles constructed from the set of vertices and face indices
     * @throws IllegalArgumentException if any face index array does not contain exactly 3 elements or a set
     *      of 3 vertices do not define a plane
     * @throws IndexOutOfBoundsException if any index into {@code vertices} is out of bounds
     */
    public static List<Triangle3D> indexedTriangles(final Vector3D[] vertices, final int[][] faceIndices,
            final DoublePrecisionContext precision) {
        return indexedTriangles(Arrays.asList(vertices), faceIndices, precision);
    }

    /** Construct a list of {@link Triangle3D} instances from a set of vertices and arrays of face indices.
     * @param vertices vertices available for use in triangle construction
     * @param faceIndices array of indices for each triangular face; each entry in the array is an array of
     *      3 index values into {@code vertices}, defining the 3 vertices that will be used to construct the
     *      triangle
     * @param precision precision context used for floating point comparisons
     * @return a list of triangles constructed from the set of vertices and face indices
     * @throws IllegalArgumentException if any face index array does not contain exactly 3 elements or a set
     *      of 3 vertices do not define a plane
     * @throws IndexOutOfBoundsException if any index into {@code vertices} is out of bounds
     * @see #indexedTriangles(Vector3D[], int[][], DoublePrecisionContext)
     */
    public static List<Triangle3D> indexedTriangles(final List<Vector3D> vertices, final int[][] faceIndices,
            final DoublePrecisionContext precision) {

        final int numFaces = faceIndices.length;
        final List<Triangle3D> triangles = new ArrayList<>(numFaces);

        int[] face;
        for (int i = 0; i < numFaces; ++i) {
            face = faceIndices[i];
            if (face.length != 3) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Invalid number of vertex indices for face at index {0}: expected 3 but found {1}",
                        i, face.length));
            }

            triangles.add(triangleFromVertices(
                        vertices.get(face[0]),
                        vertices.get(face[1]),
                        vertices.get(face[2]),
                        precision
                    ));
        }

        return triangles;
    }

    /** Construct a list of {@link ConvexPolygon3D} instances from a set of vertices and arrays of face indices. Each
     * face must contain at least 3 vertices but the number of vertices per face does not need to be constant.
     * For example, the following code constructs a list of convex polygons forming a square pyramid.
     * Note that the first face (the pyramid base) uses a different number of vertices than the other faces.
     * <pre>
     * DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-10);
     *
     * Vector3D[] vertices = {
     *      Vector3D.ZERO,
     *      Vector3D.of(1, 0, 0),
     *      Vector3D.of(1, 1, 0),
     *      Vector3D.of(0, 1, 0),
     *      Vector3D.of(0.5, 0.5, 4)
     * };
     *
     * int[][] faceIndices = {
     *      {0, 3, 2, 1}, // square base
     *      {0, 1, 4},
     *      {1, 2, 4},
     *      {2, 3, 4},
     *      {3, 0, 4}
     * };
     *
     * List&lt;ConvexPolygon3D&gt; polygons = Planes.indexedConvexPolygons(vertices, faceIndices, precision);
     * </pre>
     * @param vertices vertices available for use in convex polygon construction
     * @param faceIndices array of indices for each triangular face; each entry in the array is an array of
     *      at least 3 index values into {@code vertices}, defining the vertices that will be used to construct the
     *      convex polygon
     * @param precision precision context used for floating point comparisons
     * @return a list of convex polygons constructed from the set of vertices and face indices
     * @throws IllegalArgumentException if any face index array does not contain at least 3 elements or a set
     *      of vertices do not define a planar convex polygon
     * @throws IndexOutOfBoundsException if any index into {@code vertices} is out of bounds
     */
    public static List<ConvexPolygon3D> indexedConvexPolygons(final Vector3D[] vertices, final int[][] faceIndices,
            final DoublePrecisionContext precision) {
        return indexedConvexPolygons(Arrays.asList(vertices), faceIndices, precision);
    }

    /** Construct a list of {@link ConvexPolygon3D} instances from a set of vertices and arrays of face indices. Each
     * face must contain at least 3 vertices but the number of vertices per face does not need to be constant.
     * @param vertices vertices available for use in convex polygon construction
     * @param faceIndices array of indices for each triangular face; each entry in the array is an array of
     *      at least 3 index values into {@code vertices}, defining the vertices that will be used to construct the
     *      convex polygon
     * @param precision precision context used for floating point comparisons
     * @return a list of convex polygons constructed from the set of vertices and face indices
     * @throws IllegalArgumentException if any face index array does not contain at least 3 elements or a set
     *      of vertices do not define a planar convex polygon
     * @throws IndexOutOfBoundsException if any index into {@code vertices} is out of bounds
     * @see #indexedConvexPolygons(Vector3D[], int[][], DoublePrecisionContext)
     */
    public static List<ConvexPolygon3D> indexedConvexPolygons(final List<Vector3D> vertices, final int[][] faceIndices,
            final DoublePrecisionContext precision) {
        final int numFaces = faceIndices.length;
        final List<ConvexPolygon3D> polygons = new ArrayList<>(numFaces);
        final List<Vector3D> faceVertices = new ArrayList<>();

        int[] face;
        for (int i = 0; i < numFaces; ++i) {
            face = faceIndices[i];
            if (face.length < 3) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Invalid number of vertex indices for face at index {0}: required at least 3 but found {1}",
                        i, face.length));
            }

            for (int j = 0; j < face.length; ++j) {
                faceVertices.add(vertices.get(face[j]));
            }

            polygons.add(convexPolygonFromVertices(
                        faceVertices,
                        precision
                    ));

            faceVertices.clear();
        }

        return polygons;
    }

    /** Get the unique intersection of the plane subset with the given line. Null is
     * returned if no unique intersection point exists (ie, the line and plane are
     * parallel or coincident) or the line does not intersect the plane subset.
     * @param planeSubset plane subset to intersect with
     * @param line line to intersect with this plane subset
     * @return the unique intersection point between the line and this plane subset
     *      or null if no such point exists.
     */
    static Vector3D intersection(final PlaneSubset planeSubset, final Line3D line) {
        final Vector3D pt = planeSubset.getPlane().intersection(line);
        return (pt != null && planeSubset.contains(pt)) ? pt : null;
    }

    /** Get the unique intersection of the plane subset with the given line subset. Null
     * is returned if the underlying line and plane do not have a unique intersection
     * point (ie, they are parallel or coincident) or the intersection point is unique
     * but is not contained in both the line subset and plane subset.
     * @param planeSubset plane subset to intersect with
     * @param lineSubset line subset to intersect with
     * @return the unique intersection point between this plane subset and the argument or
     *      null if no such point exists.
     */
    static Vector3D intersection(final PlaneSubset planeSubset, final LineConvexSubset3D lineSubset) {
        final Vector3D pt = intersection(planeSubset, lineSubset.getLine());
        return (pt != null && lineSubset.contains(pt)) ? pt : null;
    }

    /** Validate that the actual plane contains the same points as the expected plane, throwing an exception if not.
     * The subspace orientations of embedding planes are not considered.
     * @param expected the expected plane
     * @param actual the actual plane
     * @throws IllegalArgumentException if the actual plane is not equivalent to the expected plane
     */
    static void validatePlanesEquivalent(final Plane expected, final Plane actual) {
        if (!expected.eq(actual, expected.getPrecision())) {
            throw new IllegalArgumentException("Arguments do not represent the same plane. Expected " +
                    expected + " but was " + actual + ".");
        }
    }

    /** Generic split method that uses performs the split using the subspace region of the plane subset.
     * @param splitter splitting hyperplane
     * @param subset the plane subset being split
     * @param factory function used to create new plane subset instances
     * @param <T> Plane subset implementation type
     * @return the result of the split operation
     */
    static <T extends PlaneSubset> Split<T> subspaceSplit(final Plane splitter, final T subset,
            final BiFunction<EmbeddingPlane, HyperplaneBoundedRegion<Vector2D>, T> factory) {

        final EmbeddingPlane thisPlane = subset.getPlane().getEmbedding();

        final Line3D intersection = thisPlane.intersection(splitter);
        if (intersection == null) {
            return getNonIntersectingSplitResult(splitter, subset);
        } else {
            final EmbeddingPlane embeddingPlane = subset.getPlane().getEmbedding();

            // the lines intersect; split the subregion
            final Vector3D intersectionOrigin = intersection.getOrigin();
            final Vector2D subspaceP1 = embeddingPlane.toSubspace(intersectionOrigin);
            final Vector2D subspaceP2 = embeddingPlane.toSubspace(intersectionOrigin.add(intersection.getDirection()));

            final Line subspaceSplitter = Lines.fromPoints(subspaceP1, subspaceP2, thisPlane.getPrecision());

            final Split<? extends HyperplaneBoundedRegion<Vector2D>> split =
                    subset.getEmbedded().getSubspaceRegion().split(subspaceSplitter);
            final SplitLocation subspaceSplitLoc = split.getLocation();

            if (SplitLocation.MINUS == subspaceSplitLoc) {
                return new Split<>(subset, null);
            } else if (SplitLocation.PLUS == subspaceSplitLoc) {
                return new Split<>(null, subset);
            }

            final T minus = (split.getMinus() != null) ? factory.apply(thisPlane, split.getMinus()) : null;
            final T plus = (split.getPlus() != null) ? factory.apply(thisPlane, split.getPlus()) : null;

            return new Split<>(minus, plus);
        }
    }

    /** Get a split result for cases where the splitting plane and the plane containing the subset being split
     * do not intersect. Callers are responsible for ensuring that the planes involved do not actually intersect.
     * @param <T> Plane subset implementation type
     * @param splitter plane performing the splitting
     * @param subset subset being split
     * @return the split result for the non-intersecting split
     */
    private static <T extends PlaneSubset> Split<T> getNonIntersectingSplitResult(
            final Plane splitter, final T subset) {
        final Plane plane = subset.getPlane();

        final double offset = splitter.offset(plane);
        final int comp = plane.getPrecision().compare(offset, 0.0);

        if (comp < 0) {
            return new Split<>(subset, null);
        } else if (comp > 0) {
            return new Split<>(null, subset);
        } else {
            return new Split<>(null, null);
        }
    }

    /** Construct a convex polygon 3D from a plane and a list of vertices lying in the plane. Callers are
     * responsible for ensuring that the vertices lie in the plane and define a convex polygon.
     * @param plane the plane containing the convex polygon
     * @param vertices vertices defining the closed, convex polygon. The must must contain at least 3 unique
     *      vertices and should not include the start vertex at the end of the list.
     * @return a new convex polygon instance
     * @throws IllegalArgumentException if the size of {@code vertices} if less than 3
     */
    static ConvexPolygon3D fromConvexPlanarVertices(final Plane plane, final List<Vector3D> vertices) {
        final int size = vertices.size();

        if (size == 3) {
            return new SimpleTriangle3D(plane, vertices.get(0), vertices.get(1), vertices.get(2));
        }

        return new VertexListConvexPolygon3D(plane, vertices);
    }

    /** Convert a convex polygon defined by a plane and list of points into a triangle fan.
     * @param plane plane containing the convex polygon
     * @param vertices vertices defining the convex polygon
     * @return a triangle fan representing the same area as the convex polygon
     * @throws IllegalArgumentException if fewer than 3 vertices are given
     */
    static List<Triangle3D> convexPolygonToTriangleFan(final Plane plane, final List<Vector3D> vertices) {
        final int size = vertices.size();
        if (size < 3) {
            throw new IllegalArgumentException("Cannot create triangle fan: 3 or more vertices are required " +
                    "but found only " + vertices.size());
        }

        final List<Triangle3D> triangles = new ArrayList<>(size - 2);

        int fanIdx = findBestTriangleFanIndex(vertices);
        int vertexIdx = (fanIdx + 1) % size;

        Vector3D fanBase = vertices.get(fanIdx);
        Vector3D vertexA = vertices.get(vertexIdx);
        Vector3D vertexB;

        vertexIdx = (vertexIdx + 1) % size;
        while (vertexIdx != fanIdx) {
            vertexB = vertices.get(vertexIdx);

            // add directly as a triangle instance to avoid computation of the plane again
            triangles.add(new SimpleTriangle3D(plane, fanBase, vertexA, vertexB));

            vertexA = vertexB;
            vertexIdx = (vertexIdx + 1) % size;
        }

        return triangles;
    }

    /** Find the index of the best vertex to use as the base for a triangle fan split of the convex polygon
     * defined by the given vertices. The best vertex is the one that forms the largest interior angle in the
     * polygon since a split at that point will help prevent the creation of very thin triangles.
     * @param vertices vertices defining the convex polygon; must not be empty
     * @return the index of the best vertex to use as the base for a triangle fan split of the convex polygon
     */
    private static int findBestTriangleFanIndex(final List<Vector3D> vertices) {
        final Iterator<Vector3D> it = vertices.iterator();

        Vector3D curPt = it.next();
        Vector3D nextPt;

        Vector3D lastVec = vertices.get(vertices.size() - 1).directionTo(curPt);
        Vector3D incomingVec = lastVec;
        Vector3D outgoingVec;

        int bestIdx = 0;
        double bestDot = -1.0;

        int idx = 0;
        double dot;
        while (it.hasNext()) {
            nextPt = it.next();
            outgoingVec = curPt.directionTo(nextPt);

            dot = incomingVec.dot(outgoingVec);
            if (dot > bestDot) {
                bestIdx = idx;
                bestDot = dot;
            }

            curPt = nextPt;
            incomingVec = outgoingVec;

            ++idx;
        }

        // handle the last vertex on its own
        dot = incomingVec.dot(lastVec);
        if (dot > bestDot) {
            bestIdx = idx;
        }

        return bestIdx;
    }

    /** Internal helper class used to construct planes from sequences of points. Instances can be also be
     * configured to collect lists of unique points found during plane construction and validate that the
     * defined region is convex.
     */
    private static final class PlaneBuilder {

        /** The point sequence to build a plane for. */
        private final Collection<Vector3D> pts;

        /** Precision context used for floating point comparisons. */
        private final DoublePrecisionContext precision;

        /** The start point from the point sequence. */
        private Vector3D startPt;

        /** The previous point from the point sequence. */
        private Vector3D prevPt;

        /** The previous vector from the point sequence, preceeding from the {@code startPt} to {@code prevPt}. */
        private Vector3D prevVector;

        /** The computed {@code normal} vector for the plane. */
        private Vector3D.Unit normal;

        /** The x component of the sum of all cross products from adjacent vectors in the point sequence. */
        private double crossSumX;

        /** The y component of the sum of all cross products from adjacent vectors in the point sequence. */
        private double crossSumY;

        /** The z component of the sum of all cross products from adjacent vectors in the point sequence. */
        private double crossSumZ;

        /** If true, an exception will be thrown if the point sequence is discovered to be non-convex. */
        private boolean requireConvex = false;

        /** List that unique vertices discovered in the input sequence will be added to. */
        private List<Vector3D> uniqueVertexOutput;

        /** Construct a new build instance for the given point sequence and precision context.
         * @param pts point sequence
         * @param precision precision context used to perform floating point comparisons
         */
        PlaneBuilder(final Collection<Vector3D> pts, final DoublePrecisionContext precision) {
            this.pts = pts;
            this.precision = precision;
        }

        /** Build a plane from the configured point sequence.
         * @return a plane built from the configured point sequence
         * @throw IllegalArgumentException if the points do not define a plane
         */
        Plane build() {
            if (pts.size() < 3) {
                throw nonPlanar();
            }

            pts.forEach(this::processPoint);

            return createPlane();
        }

        /** Build a plane from the configured point sequence, validating that the points form a convex region
         * and adding all discovered unique points to the given list.
         * @param vertexOutput list that unique points discovered in the point sequence will be added to
         * @return a plane created from the configured point sequence
         * @throw IllegalArgumentException if the points do not define a plane or the {@code requireConvex}
         *      flag is true and the points do not define a convex area
         */
        Plane buildForConvexPolygon(final List<Vector3D> vertexOutput) {
            this.requireConvex = true;
            this.uniqueVertexOutput = vertexOutput;

            Plane plane = build();

            return plane;
        }

        /** Process a point from the point sequence.
         * @param pt
         * @throw IllegalArgumentException if the points do not define a plane or the {@code requireConvex}
         *      flag is true and the points do not define a convex area
         */
        private void processPoint(final Vector3D pt) {
            if (prevPt == null) {
                startPt = pt;
                prevPt = pt;

                if (uniqueVertexOutput != null) {
                    uniqueVertexOutput.add(pt);
                }

            } else if (!prevPt.eq(pt, precision)) { // skip duplicate points
                final Vector3D vec = startPt.vectorTo(pt);

                if (prevVector != null) {
                    processCrossProduct(prevVector.cross(vec));
                }

                if (uniqueVertexOutput != null) {
                    uniqueVertexOutput.add(pt);
                }

                prevPt = pt;
                prevVector = vec;
            }
        }

        /** Process the computed cross product of two vectors from the input point sequence. The vectors
         * start at the first point in the sequence and point to adjacent points later in the sequence.
         * @param cross the cross product of two vectors from the input point sequence
         * @throw IllegalArgumentException if the points do not define a plane or the {@code requireConvex}
         *      flag is true and the points do not define a convex area
         */
        private void processCrossProduct(final Vector3D cross) {
            crossSumX += cross.getX();
            crossSumY += cross.getY();
            crossSumZ += cross.getZ();

            final double crossNorm = cross.norm();

            if (!precision.eqZero(crossNorm)) {
                // the cross product has non-zero magnitude
                if (normal == null) {
                    // save the first non-zero cross product as our normal
                    normal = cross.normalize();
                } else {
                    final double crossDot = normal.dot(cross) / crossNorm;

                    // check non-planar before non-convex since the former is a more general type
                    // of issue
                    if (!precision.eq(1.0, Math.abs(crossDot))) {
                        throw nonPlanar();
                    } else if (requireConvex && crossDot < 0) {
                        throw nonConvex();
                    }
                }
            }
        }

        /** Construct the plane instance using the value gathered during point processing.
         * @return the created plane instance
         * @throw IllegalArgumentException if the point do not define a plane
         */
        private Plane createPlane() {
            if (normal == null) {
                throw nonPlanar();
            }

            // flip the normal if needed to match the overall orientation of the points
            if (normal.dot(Vector3D.of(crossSumX, crossSumY, crossSumZ)) < 0) {
                normal = normal.negate();
            }

            // construct the plane
            final double originOffset = -startPt.dot(normal);

            return new Plane(normal, originOffset, precision);
        }

        /** Return an exception with a message stating that the points given to this builder do not
         * define a plane.
         * @return an exception stating that the points do not define a plane
         */
        private IllegalArgumentException nonPlanar() {
            return new IllegalArgumentException("Points do not define a plane: " + pts);
        }

        /** Return an exception with a message stating that the points given to this builder do not
         * define a convex region.
         * @return an exception stating that the points do not define a plane
         */
        private IllegalArgumentException nonConvex() {
            return new IllegalArgumentException("Points do not define a convex region: " + pts);
        }
    }
}
