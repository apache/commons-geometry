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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

/** Class containing factory methods for constructing {@link Plane} and {@link PlaneSubset} instances.
 */
public final class Planes {

    /** Utility class; no instantiation. */
    private Planes() {
    }

    /**
     * Build a plane from a point and two (on plane) vectors.
     * @param p the provided point (on plane)
     * @param u u vector (on plane)
     * @param v v vector (on plane)
     * @param precision precision context used to compare floating point values
     * @return a new plane
     * @throws IllegalArgumentException if the norm of the given values is zero, NaN, or infinite.
     */
    public static Plane fromPointAndPlaneVectors(final Vector3D p, final Vector3D u, final Vector3D v,
            final DoublePrecisionContext precision) {
        final Vector3D uNorm = u.normalize();
        final Vector3D vNorm = uNorm.orthogonal(v);
        final Vector3D wNorm = uNorm.cross(vNorm).normalize();
        final double originOffset = -p.dot(wNorm);

        return new Plane(uNorm, vNorm, wNorm, originOffset, precision);
    }

    /**
     * Build a plane from a normal.
     * Chooses origin as point on plane.
     * @param normal normal direction to the plane
     * @param precision precision context used to compare floating point values
     * @return a new plane
     * @throws IllegalArgumentException if the norm of the given values is zero, NaN, or infinite.
     */
    public static Plane fromNormal(final Vector3D normal, final DoublePrecisionContext precision) {
        return fromPointAndNormal(Vector3D.ZERO, normal, precision);
    }

    /**
     * Build a plane from a point and a normal.
     *
     * @param p point belonging to the plane
     * @param normal normal direction to the plane
     * @param precision precision context used to compare floating point values
     * @return a new plane
     * @throws IllegalArgumentException if the norm of the given values is zero, NaN, or infinite.
     */
    public static Plane fromPointAndNormal(final Vector3D p, final Vector3D normal,
            final DoublePrecisionContext precision) {
        final Vector3D w = normal.normalize();
        final double originOffset = -p.dot(w);

        final Vector3D u = w.orthogonal();
        final Vector3D v = w.cross(u);

        return new Plane(u, v, w, originOffset, precision);
    }

    /**
     * Build a plane from three points.
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

        if (pts.size() < 3) {
            throw new IllegalArgumentException("At least 3 points are required to define a plane; " +
                    "argument contains only " + pts.size() + ".");
        }

        final Iterator<Vector3D> it = pts.iterator();

        final Vector3D startPt = it.next();

        Vector3D u = null;
        Vector3D w = null;

        Vector3D currentPt;
        Vector3D prevPt = startPt;

        Vector3D currentVector = null;
        Vector3D prevVector = null;

        Vector3D cross = null;
        double crossNorm;
        double crossSumX = 0.0;
        double crossSumY = 0.0;
        double crossSumZ = 0.0;

        boolean nonPlanar = false;

        while (it.hasNext()) {
            currentPt = it.next();

            if (!currentPt.eq(prevPt, precision)) {
                currentVector = startPt.vectorTo(currentPt);

                if (u == null) {
                    // save the first non-zero vector as our u vector
                    u = currentVector.normalize();
                }
                if (prevVector != null) {
                    cross = prevVector.cross(currentVector);

                    crossSumX += cross.getX();
                    crossSumY += cross.getY();
                    crossSumZ += cross.getZ();

                    crossNorm = cross.norm();

                    if (!precision.eqZero(crossNorm)) {
                        // the cross product has non-zero magnitude
                        if (w == null) {
                            // save the first non-zero cross product as our normal
                            w = cross.normalize();
                        } else if (!precision.eq(1.0, Math.abs(w.dot(cross) / crossNorm))) {
                            // if the normalized dot product is not either +1 or -1, then
                            // the points are not coplanar
                            nonPlanar = true;
                            break;
                        }
                    }
                }

                prevVector = currentVector;
                prevPt = currentPt;
            }
        }

        if (u == null || w == null || nonPlanar) {
            throw new IllegalArgumentException("Points do not define a plane: " + pts);
        }

        if (w.dot(Vector3D.of(crossSumX, crossSumY, crossSumZ)) < 0) {
            w = w.negate();
        }

        final Vector3D v = w.cross(u);
        final double originOffset = -startPt.dot(w);

        return new Plane(u, v, w, originOffset, precision);
    }

    /** Create a new plane subset from a plane and an embedded convex subspace area.
     * @param plane embedding plane for the area
     * @param area area embedded in the plane
     * @return a new convex sub plane instance
     */
    public static PlaneConvexSubset subsetFromConvexArea(final Plane plane, final ConvexArea area) {
        return new PlaneConvexSubset(plane, area);
    }

    /** Create a new plane subset from the given sequence of points. The points must define a unique plane,
     * meaning that at least 3 unique vertices must be given. In contrast with the
     * {@link #fromVertices(Collection, DoublePrecisionContext)} method, the first point in the sequence is included
     * at the end if needed, in order to form a closed loop.
     * @param pts collection of points defining the plane subset
     * @param precision precision context used to compare floating point values
     * @return a new plane subset defined by the given sequence of vertices
     * @throws IllegalArgumentException if fewer than 3 vertices are given or the vertices do not define a
     *       unique plane
     * @see #fromVertices(Collection, DoublePrecisionContext)
     * @see #fromVertices(Collection, boolean, DoublePrecisionContext)
     * @see Plane#fromPoints(Collection, DoublePrecisionContext)
     */
    public static PlaneConvexSubset subsetFromVertexLoop(final Collection<Vector3D> pts,
            final DoublePrecisionContext precision) {
        return subsetFromVertices(pts, true, precision);
    }

    /** Create a new plane subset from the given sequence of points. The points must define a unique plane,
     * meaning that at least 3 unique vertices must be given.
     * @param pts collection of points defining the plane subset
     * @param precision precision context used to compare floating point values
     * @return a new plane subset defined by the given sequence of vertices
     * @throws IllegalArgumentException if fewer than 3 vertices are given or the vertices do not define a
     *      unique plane
     * @see #fromVertexLoop(Collection, DoublePrecisionContext)
     * @see #fromVertices(Collection, boolean, DoublePrecisionContext)
     * @see Plane#fromPoints(Collection, DoublePrecisionContext)
     */
    public static PlaneConvexSubset subsetFromVertices(final Collection<Vector3D> pts,
            final DoublePrecisionContext precision) {
        return subsetFromVertices(pts, false, precision);
    }

    /** Create a new plane subset from the given sequence of points. The points must define a unique plane,
     * meaning that at least 3 unique vertices must be given. If {@code close} is true, the vertices are made
     * into a closed loop by including the start point at the end if needed.
     * @param pts collection of points
     * @param close if true, the point sequence will implicitly include the start point again at the end; otherwise
     *      the vertex sequence is taken as-is
     * @param precision precision context used to compare floating point values
     * @return a new plane subset instance
     * @throws IllegalArgumentException if fewer than 3 vertices are given or the vertices do not define a
     *      unique plane
     * @see #fromVertexLoop(Collection, DoublePrecisionContext)
     * @see #fromVertices(Collection, DoublePrecisionContext)
     * @see Plane#fromPoints(Collection, DoublePrecisionContext)
     */
    public static PlaneConvexSubset subsetFromVertices(final Collection<Vector3D> pts, final boolean close,
            final DoublePrecisionContext precision) {

        final Plane plane = Planes.fromPoints(pts, precision);

        final List<Vector2D> subspacePts = plane.toSubspace(pts);
        final ConvexArea area = ConvexArea.fromVertices(subspacePts, close, precision);

        return new PlaneConvexSubset(plane, area);
    }
}
