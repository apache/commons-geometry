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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.euclidean.internal.Vectors;
import org.apache.commons.geometry.euclidean.threed.line.Lines3D;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.core.Precision;

/** Abstract base class for {@link ConvexPolygon3D} implementations.
 */
abstract class AbstractConvexPolygon3D extends AbstractPlaneSubset implements ConvexPolygon3D {

    /** Plane containing the convex polygon. */
    private final Plane plane;

    /** Simple constructor.
     * @param plane the plane containing the convex polygon
     */
    AbstractConvexPolygon3D(final Plane plane) {
        this.plane = plane;
    }

    /** {@inheritDoc} */
    @Override
    public Plane getPlane() {
        return plane;
    }

    /** {@inheritDoc}
     *
     *  <p>This method always returns {@code false}.</p>
     */
    @Override
    public boolean isFull() {
        return false;
    }

    /** {@inheritDoc}
     *
     *  <p>This method always returns {@code false}.</p>
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        // see https://geomalgorithms.com/a01-_area.html#3D-Planar-Polygons
        final List<Vector3D> vertices = getVertices();

        double crossSumX = 0.0;
        double crossSumY = 0.0;
        double crossSumZ = 0.0;

        Vector3D prevPt = vertices.get(vertices.size() - 1);
        Vector3D cross;
        for (final Vector3D curPt : vertices) {
            cross = prevPt.cross(curPt);

            crossSumX += cross.getX();
            crossSumY += cross.getY();
            crossSumZ += cross.getZ();

            prevPt = curPt;
        }

        return 0.5 * plane.getNormal().dot(Vector3D.of(crossSumX, crossSumY, crossSumZ));
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D getCentroid() {
        final List<Vector3D> vertices = getVertices();

        double areaSum = 0.0;
        double scaledCentroidSumX = 0.0;
        double scaledCentroidSumY = 0.0;
        double scaledCentroidSumZ = 0.0;

        final Iterator<Vector3D> it = vertices.iterator();

        final Vector3D startPt = it.next();

        Vector3D prevPt = it.next();
        Vector3D curPt;

        Vector3D prevVec = startPt.vectorTo(prevPt);
        Vector3D curVec;

        double triArea;
        Vector3D triCentroid;
        while (it.hasNext()) {
            curPt = it.next();
            curVec = startPt.vectorTo(curPt);

            triArea = 0.5 * prevVec.cross(curVec).norm();
            triCentroid = Vector3D.centroid(startPt, prevPt, curPt);

            areaSum += triArea;

            scaledCentroidSumX += triArea * triCentroid.getX();
            scaledCentroidSumY += triArea * triCentroid.getY();
            scaledCentroidSumZ += triArea * triCentroid.getZ();

            prevPt = curPt;
            prevVec = curVec;
        }

        if (areaSum > 0) {
            final double scale = 1 / areaSum;
            return Vector3D.of(
                        scale * scaledCentroidSumX,
                        scale * scaledCentroidSumY,
                        scale * scaledCentroidSumZ
                    );
        }

        // zero area, which means that the points are all linear; return the point midway between the
        // min and max points
        final Vector3D min = Vector3D.min(vertices);
        final Vector3D max = Vector3D.max(vertices);

        return min.lerp(max, 0.5);
    }

    /** {@inheritDoc} */
    @Override
    public Bounds3D getBounds() {
        return Bounds3D.from(getVertices());
    }

    /** {@inheritDoc} */
    @Override
    public RegionLocation classify(final Vector3D pt) {
        if (plane.contains(pt)) {
            final List<Vector3D> vertices = getVertices();
            final Precision.DoubleEquivalence precision = plane.getPrecision();

            final Vector3D normal = plane.getNormal();
            Vector3D edgeVec;
            Vector3D edgePlusVec;
            Vector3D testVec;

            Vector3D offsetVec;
            double offsetSign;
            double offset;
            int cmp;

            boolean onBoundary = false;

            Vector3D startVertex = vertices.get(vertices.size() - 1);
            for (final Vector3D nextVertex : vertices) {

                edgeVec = startVertex.vectorTo(nextVertex);
                edgePlusVec = edgeVec.cross(normal);

                testVec = startVertex.vectorTo(pt);

                offsetVec = testVec.reject(edgeVec);
                offsetSign = Math.signum(offsetVec.dot(edgePlusVec));
                offset = offsetSign * offsetVec.norm();

                cmp = precision.compare(offset, 0.0);
                if (cmp > 0) {
                    // the point is on the plus side (outside) of a boundary
                    return RegionLocation.OUTSIDE;
                } else if (cmp == 0) {
                    onBoundary = true;
                }

                startVertex = nextVertex;
            }

            if (onBoundary) {
                // the point is not on the outside of any boundaries and is directly on at least one
                return RegionLocation.BOUNDARY;
            }

            // the point is on the inside of all boundaries
            return RegionLocation.INSIDE;
        }

        // the point is not on the plane
        return RegionLocation.OUTSIDE;
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D closest(final Vector3D pt) {
        final Vector3D normal = plane.getNormal();
        final Precision.DoubleEquivalence precision = plane.getPrecision();

        final List<Vector3D> vertices = getVertices();

        final Vector3D projPt = plane.project(pt);

        Vector3D edgeVec;
        Vector3D edgePlusVec;
        Vector3D testVec;

        Vector3D offsetVec;
        double offsetSign;
        double offset;
        int cmp;

        Vector3D boundaryVec;
        double boundaryPointT;
        Vector3D boundaryPoint;
        double boundaryPointDistSq;

        double closestBoundaryPointDistSq = Double.POSITIVE_INFINITY;
        Vector3D closestBoundaryPoint = null;

        Vector3D startVertex = vertices.get(vertices.size() - 1);
        for (final Vector3D nextVertex : vertices) {

            edgeVec = startVertex.vectorTo(nextVertex);
            edgePlusVec = edgeVec.cross(normal);

            testVec = startVertex.vectorTo(projPt);

            offsetVec = testVec.reject(edgeVec);
            offsetSign = Math.signum(offsetVec.dot(edgePlusVec));
            offset = offsetSign * offsetVec.norm();

            cmp = precision.compare(offset, 0.0);
            if (cmp >= 0) {
                // the point is directly on the boundary or on its plus side; project the point onto the
                // boundary, taking care to restrict the point to the actual extent of the boundary,
                // and select the point with the shortest distance
                boundaryVec = testVec.subtract(offsetVec);
                boundaryPointT =
                        Math.signum(boundaryVec.dot(edgeVec)) * (boundaryVec.norm() / Vectors.checkedNorm(edgeVec));
                boundaryPointT = Math.max(0, Math.min(1, boundaryPointT));

                boundaryPoint = startVertex.lerp(nextVertex, boundaryPointT);

                boundaryPointDistSq = boundaryPoint.distanceSq(projPt);
                if (boundaryPointDistSq < closestBoundaryPointDistSq) {
                    closestBoundaryPointDistSq = boundaryPointDistSq;
                    closestBoundaryPoint = boundaryPoint;
                }
            }

            startVertex = nextVertex;
        }

        if (closestBoundaryPoint != null) {
            // the point is on the outside of the polygon; return the closest point on the boundary
            return closestBoundaryPoint;
        }

        // the projected point is on the inside of all boundaries and therefore on the inside of the subset
        return projPt;
    }

    /** {@inheritDoc} */
    @Override
    public PlaneConvexSubset.Embedded getEmbedded() {
        final EmbeddingPlane embeddingPlane = plane.getEmbedding();
        final List<Vector2D> subspaceVertices = embeddingPlane.toSubspace(getVertices());
        final ConvexArea area = ConvexArea.convexPolygonFromVertices(subspaceVertices,
                embeddingPlane.getPrecision());

        return new EmbeddedAreaPlaneConvexSubset(embeddingPlane, area);
    }

    /** {@inheritDoc} */
    @Override
    public Split<PlaneConvexSubset> split(final Hyperplane<Vector3D> splitter) {
        final Plane splitterPlane = (Plane) splitter;
        final List<Vector3D> vertices = getVertices();

        final int size = vertices.size();

        int minusPlusTransitionIdx = -1;
        Vector3D minusPlusInsertVertex = null;

        int plusMinusTransitionIdx = -1;
        Vector3D plusMinusInsertVertex = null;

        int transitionCount = 0;

        Vector3D curVertex;
        HyperplaneLocation curLoc;

        int lastSideIdx = -1;
        Vector3D lastSideVertex = null;
        HyperplaneLocation lastSideLoc = null;

        int lastBoundaryIdx = -1;

        for (int i = 0; i <= size || transitionCount == 1; ++i) {

            curVertex = vertices.get(i % size);
            curLoc = splitter.classify(curVertex);

            if (lastSideLoc == HyperplaneLocation.MINUS && curLoc == HyperplaneLocation.PLUS) {
                // transitioned from minus side to plus side
                minusPlusTransitionIdx = Math.max(lastSideIdx, lastBoundaryIdx);
                ++transitionCount;

                if (lastBoundaryIdx < 0) {
                    // no shared boundary point; compute a new vertex
                    minusPlusInsertVertex = splitterPlane.intersection(
                            Lines3D.fromPoints(lastSideVertex, curVertex, splitterPlane.getPrecision()));
                }
            } else if (lastSideLoc == HyperplaneLocation.PLUS && curLoc == HyperplaneLocation.MINUS) {
                // transitioned from plus side to minus side
                plusMinusTransitionIdx = Math.max(lastSideIdx, lastBoundaryIdx);
                ++transitionCount;

                if (lastBoundaryIdx < 0) {
                    // no shared boundary point; compute a new vertex
                    plusMinusInsertVertex = splitterPlane.intersection(
                            Lines3D.fromPoints(lastSideVertex, curVertex, splitterPlane.getPrecision()));
                }
            }

            if (curLoc == HyperplaneLocation.ON) {
                lastBoundaryIdx = i;
            } else {
                lastBoundaryIdx = -1;

                lastSideIdx = i;
                lastSideVertex = curVertex;
                lastSideLoc = curLoc;
            }
        }

        if (minusPlusTransitionIdx > -1 && plusMinusTransitionIdx > -1) {
            // we've split; compute the vertex list for each side
            final List<Vector3D> minusVertices =  buildPolygonSplitVertexList(
                    plusMinusTransitionIdx, plusMinusInsertVertex,
                    minusPlusTransitionIdx, minusPlusInsertVertex, vertices);
            final List<Vector3D> plusVertices = buildPolygonSplitVertexList(
                    minusPlusTransitionIdx, minusPlusInsertVertex,
                    plusMinusTransitionIdx, plusMinusInsertVertex, vertices);

            // delegate back to the Planes factory methods to determine the concrete types
            // for each side of the split
            return new Split<>(
                    Planes.fromConvexPlanarVertices(plane, minusVertices),
                    Planes.fromConvexPlanarVertices(plane, plusVertices));

        } else if (lastSideLoc == HyperplaneLocation.PLUS) {
            // we lie entirely on the plus side of the splitter
            return new Split<>(null, this);
        } else if (lastSideLoc == HyperplaneLocation.MINUS) {
            // we lie entirely on the minus side of the splitter
            return new Split<>(this, null);
        }

        // we lie entirely on the splitter
        return new Split<>(null, null);
    }

    /** Internal method for building a vertex list for one side of a split result. The method is
     * designed to make the fewest allocations possible.
     * @param enterIdx the index of the vertex from {@code vertices} immediately before the polygon transitioned
     *      to being fully entered into this side of the split result. If no point from {@code vertices} lay
     *      directly on the splitting plane while entering this side and a new vertex had to be computed for the
     *      split result, then this index will be the last vertex on the opposite side of the split. If a vertex
     *      did lie directly on the splitting plane, then this index will point to that vertex.
     * @param newEnterPt the newly-computed point to be added as the first vertex in the split result; may
     *      be null if no such point exists
     * @param exitIdx the index of the vertex from {@code vertices} immediately before the polygon transitioned
     *      to being fully exited from this side of the split result. If no point from {@code vertices} lay
     *      directly on the splitting plane while exiting this side and a new vertex had to be computed for the
     *      split result, then this index will be the last vertex on this side of the split. If a vertex did
     *      lie directly on the splitting plane, then this index will point to that vertex.
     * @param newExitPt the newly-computed point to be added as the last vertex in the split result; may
     *      be null if no such point exists
     * @param vertices the original list of vertices that this split result originated from; this list is
     *      not modified by this operation
     * @return the list of vertices for the split result
     */
    private List<Vector3D> buildPolygonSplitVertexList(final int enterIdx, final Vector3D newEnterPt,
            final int exitIdx, final Vector3D newExitPt, final List<? extends Vector3D> vertices) {

        final int size = vertices.size();

        final boolean hasNewEnterPt = newEnterPt != null;
        final boolean hasNewExitPt = newExitPt != null;

        final int startIdx = (hasNewEnterPt ? enterIdx + 1 : enterIdx) % size;
        final int endIdx = exitIdx % size;

        final boolean hasWrappedIndices = endIdx < startIdx;

        final int resultSize = (hasWrappedIndices ? endIdx + size : endIdx) - startIdx + 1;
        final List<Vector3D> result = new ArrayList<>(resultSize);

        if (hasNewEnterPt) {
            result.add(newEnterPt);
        }

        if (hasWrappedIndices) {
            result.addAll(vertices.subList(startIdx, size));
            result.addAll(vertices.subList(0, endIdx + 1));
        } else {
            result.addAll(vertices.subList(startIdx, endIdx + 1));
        }

        if (hasNewExitPt) {
            result.add(newExitPt);
        }

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(256);
        sb.append(getClass().getSimpleName())
            .append("[normal= ")
            .append(getPlane().getNormal())
            .append(", vertices= ")
            .append(getVertices())
            .append(']');

        return sb.toString();
    }
}
