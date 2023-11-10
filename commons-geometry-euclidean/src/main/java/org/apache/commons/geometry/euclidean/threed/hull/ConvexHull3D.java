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

package org.apache.commons.geometry.euclidean.threed.hull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.ConvexHull;
import org.apache.commons.geometry.core.collection.PointSet;
import org.apache.commons.geometry.euclidean.EuclideanCollections;
import org.apache.commons.geometry.euclidean.threed.ConvexPolygon3D;
import org.apache.commons.geometry.euclidean.threed.ConvexVolume;
import org.apache.commons.geometry.euclidean.threed.Plane;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.line.Line3D;
import org.apache.commons.geometry.euclidean.threed.line.Lines3D;
import org.apache.commons.numbers.core.Precision.DoubleEquivalence;

/**
 * This class represents a convex hull in three-dimensional Euclidean space.
 */
public class ConvexHull3D implements ConvexHull<Vector3D> {

    /** The vertices of the convex hull. */
    private final List<Vector3D> vertices;

    /** The region defined by the hull. */
    private final ConvexVolume region;

    /** A collection of all facets that form the convex volume of the hull. */
    private final List<ConvexPolygon3D> facets;

    /** Flag for when the hull is degenerate. */
    private final boolean isDegenerate;

    /**
     * Simple constructor no validation performed. This constructor is called if the
     * hull is well-formed and non-degenerative.
     *
     * @param facets the facets of the hull.
     */
    ConvexHull3D(Collection<? extends ConvexPolygon3D> facets) {
        vertices = Collections.unmodifiableList(
                new ArrayList<>(facets.stream().flatMap(f -> f.getVertices().stream()).collect(Collectors.toSet())));
        region = ConvexVolume.fromBounds(() -> facets.stream().map(ConvexPolygon3D::getPlane).iterator());
        this.facets = new ArrayList<>(facets);
        this.isDegenerate = false;
    }

    /**
     * Simple constructor no validation performed. No Region is formed as it is
     * assumed that the hull is degenerate.
     *
     * @param points       the given vertices of the hull.
     * @param isDegenerate boolean flag
     */
    ConvexHull3D(Collection<Vector3D> points, boolean isDegenerate) {
        vertices = Collections.unmodifiableList(new ArrayList<>(points));
        region = null;
        this.facets = Collections.emptyList();
        this.isDegenerate = isDegenerate;
    }

    /** {@inheritDoc} */
    @Override
    public List<Vector3D> getVertices() {
        return vertices;
    }

    /** {@inheritDoc} */
    @Override
    public ConvexVolume getRegion() {
        return region;
    }

    /**
     * Return a collection of all two-dimensional faces (called facets) of the
     * convex hull.
     *
     * @return a collection of all two-dimensional faces.
     */
    public List<? extends ConvexPolygon3D> getFacets() {
        return Collections.unmodifiableList(facets);
    }

    /**
     * Return {@code true} if the hull is degenerate.
     *
     * @return the isDegenerate
     */
    public boolean isDegenerate() {
        return isDegenerate;
    }

    /**
     * Implementation of quick-hull algorithm by Barber, Dobkin and Huhdanpaa. The
     * algorithm constructs the convex hull for a given finite set of points.
     * Empirically, the number of points processed by Quickhull is proportional to
     * the number of vertices in the output. The algorithm runs on an input of size
     * n with r processed points in time O(n log r). We define a point of the given
     * set to be extreme, if and only if the point is part of the final hull. The
     * algorithm runs in multiple stages:
     * <ol>
     * <li>First we construct a simplex with extreme properties from the given point
     * set to maximize the possibility of choosing extreme points as initial simplex
     * vertices.</li>
     * <li>We partition all the remaining points into outside sets. Each polygon
     * face of the simplex defines a positive and negative half-space. A point can
     * be assigned to the outside set of the polygon if it is an element of the
     * positive half space.</li>
     * <li>For each polygon-face (facet) with a non empty outside set we choose a
     * point with maximal distance to the given facet.</li>
     * <li>We determine all the visible facets from the given outside point and find
     * a path around the horizon.</li>
     * <li>We construct a new cone of polygons from the edges of the horizon to the
     * outside point. All visible facets are removed and the points in the outside
     * sets of the visible facets are redistributed.</li>
     * <li>We repeat step 3-5 until each outside set is empty.</li>
     * </ol>
     */
    public static class Builder {

        /** Set of possible candidates. */
        private final PointSet<Vector3D> candidates;

        /** Precision context used to compare floating point numbers. */
        private final DoubleEquivalence precision;

        /** Simplex for testing new points and starting the algorithm. */
        private Simplex simplex;

        /** The minX, maxX, minY, maxY, minZ, maxZ points. */
        private final Vector3D[] box;

        /**
         * A map which contains all the vertices of the current hull as keys and the
         * associated facets as values.
         */
        private Map<Vector3D, Set<Facet>> vertexToFacetMap;

        /**
         * Constructor for a builder with the given precision.
         *
         * @param precision the given precision.
         */
        public Builder(DoubleEquivalence precision) {
            candidates = EuclideanCollections.pointSet3D(precision);
            this.precision = precision;
            vertexToFacetMap = EuclideanCollections.pointMap3D(precision);
            box = new Vector3D[6];
            simplex = new Simplex(Collections.emptySet());
        }

        /**
         * Appends to the point to the set of possible candidates.
         *
         * @param point the given point.
         * @return this instance.
         */
        public Builder append(Vector3D point) {
            if (box[0] == null) {
                box[0] = box[1] = box[2] = box[3] = box[4] = box[5] = point;
                candidates.add(point);
                return this;
            }
            boolean hasBeenModified = false;
            if (box[0].getX() > point.getX()) {
                box[0] = point;
                hasBeenModified = true;
            }
            if (box[1].getX() < point.getX()) {
                box[1] = point;
                hasBeenModified = true;
            }
            if (box[2].getY() > point.getY()) {
                box[2] = point;
                hasBeenModified = true;
            }
            if (box[3].getY() < point.getY()) {
                box[3] = point;
                hasBeenModified = true;
            }
            if (box[4].getZ() > point.getZ()) {
                box[4] = point;
                hasBeenModified = true;
            }
            if (box[5].getZ() < point.getZ()) {
                box[5] = point;
                hasBeenModified = true;
            }
            candidates.add(point);
            if (hasBeenModified) {
                // Remove all outside Points and add all vertices again.
                removeFacets(simplex.facets());
                simplex.facets().stream().map(Facet::getPolygon).forEach(p -> candidates.addAll(p.getVertices()));
                simplex = createSimplex(candidates);
            }
            distributePoints(simplex.facets());
            return this;
        }

        /**
         * Appends the given collection of points to the set of possible candidates.
         *
         * @param points the given collection of points.
         * @return this instance.
         */
        public Builder append(Collection<Vector3D> points) {
            if (simplex != null) {
                // Remove all outside Points and add all vertices again.
                removeFacets(simplex.facets());
                simplex.facets().stream().map(Facet::getPolygon).forEach(p -> candidates.addAll(p.getVertices()));
            }
            candidates.addAll(points);
            simplex = createSimplex(candidates);
            distributePoints(simplex.facets());
            return this;
        }

        /**
         * Builds a convex hull containing all appended points.
         *
         * @return a convex hull containing all appended points.
         */
        public ConvexHull3D build() {
            if (simplex == null) {
                return new ConvexHull3D(candidates, true);
            }

            // The simplex is degenerate.
            if (simplex.isDegenerate()) {
                return new ConvexHull3D(candidates, true);
            }

            vertexToFacetMap = new HashMap<>();
            simplex.facets().forEach(this::addFacet);
            distributePoints(simplex.facets());
            while (isInconflict()) {
                Facet conflictFacet = getConflictFacet();
                Vector3D conflictPoint = conflictFacet.getConflictPoint();
                Set<Facet> visibleFacets = new HashSet<>();
                getVisibleFacets(conflictFacet, conflictPoint, visibleFacets);
                Set<Vector3D[]> horizon = getHorizon(visibleFacets);
                Vector3D referencePoint = conflictFacet.getPolygon().getCentroid();
                Set<Facet> cone = constructCone(conflictPoint, horizon, referencePoint);
                removeFacets(visibleFacets);
                cone.forEach(this::addFacet);
                distributePoints(cone);
            }
            Collection<ConvexPolygon3D> hull = vertexToFacetMap.values().stream().flatMap(Collection::stream)
                    .map(Facet::getPolygon).collect(Collectors.toSet());
            return new ConvexHull3D(hull);
        }

        /**
         * Constructs a new cone with conflict point and the given edges. The reference
         * point is used for orientation in such a way, that the reference point lies in
         * the negative half-space of all newly constructed facets.
         *
         * @param conflictPoint  the given conflict point.
         * @param horizon        the given set of edges.
         * @param referencePoint a reference point for orientation.
         * @return a set of newly constructed facets.
         */
        private Set<Facet> constructCone(Vector3D conflictPoint, Set<Vector3D[]> horizon, Vector3D referencePoint) {
            Set<Facet> newFacets = new HashSet<>();
            for (Vector3D[] edge : horizon) {
                ConvexPolygon3D newFacet = Planes
                        .convexPolygonFromVertices(Arrays.asList(edge[0], edge[1], conflictPoint), precision);
                if (!isInside(newFacet, referencePoint, precision)) {
                    newFacet = newFacet.reverse();
                }
                newFacets.add(new Facet(newFacet, precision));
            }
            return newFacets;
        }

        /**
         * Create an initial simplex for the given point set. If no non-zero simplex can
         * be formed the point set is degenerate and an empty Collection is returned.
         * Each vertex of the simplex must be inside the given point set.
         *
         * @param points the given point set.
         * @return an initial simplex.
         */
        private Simplex createSimplex(Collection<Vector3D> points) {

            // First vertex of the simplex
            Vector3D vertex1 = points.stream().min(Vector3D.COORDINATE_ASCENDING_ORDER).get();

            // Find a point with maximal distance to the second.
            Vector3D vertex2 = points.stream().max((u, v) -> Double.compare(vertex1.distance(u), vertex1.distance(v)))
                    .get();

            // The point is degenerate if all points are equivalent.
            if (vertex1.eq(vertex2, precision)) {
                return new Simplex(Collections.emptyList());
            }

            // First and second vertex form a line.
            Line3D line = Lines3D.fromPoints(vertex1, vertex2, precision);

            // Find a point with maximal distance from the line.
            Vector3D vertex3 = points.stream().max((u, v) -> Double.compare(line.distance(u), line.distance(v))).get();

            // The point set is degenerate because all points are colinear.
            if (line.contains(vertex3)) {
                return new Simplex(Collections.emptyList());
            }

            // Form a triangle with the first three vertices.
            ConvexPolygon3D facet1 = Planes.triangleFromVertices(vertex1, vertex2, vertex3, precision);

            // Find a point with maximal distance to the plane formed by the triangle.
            Plane plane = facet1.getPlane();
            Vector3D vertex4 = points.stream()
                    .max((u, v) -> Double.compare(Math.abs(plane.offset(u)), Math.abs(plane.offset(v)))).get();

            // The point set is degenerate, because all points are coplanar.
            if (plane.contains(vertex4)) {
                return new Simplex(Collections.emptyList());
            }

            // Construct the other three facets.
            ConvexPolygon3D facet2 = Planes.convexPolygonFromVertices(Arrays.asList(vertex1, vertex2, vertex4),
                    precision);
            ConvexPolygon3D facet3 = Planes.convexPolygonFromVertices(Arrays.asList(vertex1, vertex3, vertex4),
                    precision);
            ConvexPolygon3D facet4 = Planes.convexPolygonFromVertices(Arrays.asList(vertex2, vertex3, vertex4),
                    precision);

            List<Facet> facets = new ArrayList<>();

            // Choose the right orientation for all facets.
            facets.add(isInside(facet1, vertex4, precision) ? new Facet(facet1, precision) :
                new Facet(facet1.reverse(), precision));
            facets.add(isInside(facet2, vertex3, precision) ? new Facet(facet2, precision) :
                new Facet(facet2.reverse(), precision));
            facets.add(isInside(facet3, vertex2, precision) ? new Facet(facet3, precision) :
                new Facet(facet3.reverse(), precision));
            facets.add(isInside(facet4, vertex1, precision) ? new Facet(facet4, precision) :
                new Facet(facet4.reverse(), precision));

            return new Simplex(facets);
        }

        /**
         * Returns {@code true} if the given point resides inside the negative
         * half-space of the oriented facet. Points which are coplanar are also assumed
         * to be inside. Mathematically a point is inside if the calculated oriented
         * offset, of the point to the hyperplane is less than or equal to zero.
         *
         * @param facet     the given facet.
         * @param point     a reference point.
         * @param precision the given precision.
         * @return {@code true} if the given point resides inside the negative
         *         half-space.
         */
        private static boolean isInside(ConvexPolygon3D facet, Vector3D point, DoubleEquivalence precision) {
            return precision.lte(facet.getPlane().offset(point), 0);
        }

        /**
         * Returns {@code true} if any of the facets is in conflict.
         *
         * @return {@code true} if any of the facets is in conflict.
         */
        private boolean isInconflict() {
            return vertexToFacetMap.values().stream().flatMap(Collection::stream).anyMatch(Facet::hasOutsidePoints);
        }

        /**
         * Adds the facet for the quickhull algorithm.
         *
         * @param facet the given facet.
         */
        private void addFacet(Facet facet) {
            for (Vector3D p : facet.getPolygon().getVertices()) {
                if (vertexToFacetMap.containsKey(p)) {
                    Set<Facet> set = vertexToFacetMap.get(p);
                    set.add(facet);
                } else {
                    Set<Facet> set = new HashSet<>(3);
                    set.add(facet);
                    vertexToFacetMap.put(p, set);
                }
            }
        }

        /**
         * Associates each point of the candidates set with an outside set of the given
         * facets. Afterwards the candidates set is cleared.
         *
         * @param facets the facets to check against.
         */
        private void distributePoints(Collection<Facet> facets) {
            if (!facets.isEmpty()) {
                candidates.forEach(p -> distributePoint(p, facets));
                candidates.clear();
            }
        }

        /**
         * Associates the given point with an outside set if possible.
         *
         * @param p the given point.
         * @param facets the facets to check against.
         */
        private static void distributePoint(Vector3D p, Iterable<Facet> facets) {
            for (Facet facet : facets) {
                if (facet.addPoint(p)) {
                    return;
                }
            }
        }

        /**
         * Returns any facet, which is currently in conflict e.g has a non empty outside
         * set.
         *
         * @return any facet, which is currently in conflict e.g has a non empty outside
         *         set.
         */
        private Facet getConflictFacet() {
            return vertexToFacetMap.values().stream().flatMap(Collection::stream).filter(Facet::hasOutsidePoints)
                    .findFirst().get();
        }

        /**
         * Adds all visible facets to the provided set.
         *
         * @param facet the given conflictFacet.
         * @param conflictPoint the given conflict point.
         * @param collector     visible facets are collected in this set.
         */
        private void getVisibleFacets(Facet facet, Vector3D conflictPoint, Set<Facet> collector) {
            if (collector.contains(facet)) {
                return;
            }

            // Check the facet and all neighbors.
            if (!Builder.isInside(facet.getPolygon(), conflictPoint, precision)) {
                collector.add(facet);
                findNeighbors(facet).stream().forEach(f -> getVisibleFacets(f, conflictPoint, collector));
            }
        }

        /**
         * Returns a set of all neighbors for the given facet.
         *
         * @param facet the given facet.
         * @return a set of all neighbors.
         */
        private Set<Facet> findNeighbors(Facet facet) {
            List<Vector3D> vertices = facet.getPolygon().getVertices();
            Set<Facet> neighbors = new HashSet<>();
            for (int i = 0; i < vertices.size(); i++) {
                for (int j = i + 1; j < vertices.size(); j++) {
                    neighbors.addAll(getFacets(vertices.get(i), vertices.get(j)));
                }
            }
            neighbors.remove(facet);
            return neighbors;
        }

        /**
         * Gets all the facets, which have the given point as vertex or {@code null}.
         *
         * @param vertex the given point.
         * @return a set containing all facets with the given vertex.
         */
        private Set<Facet> getFacets(Vector3D vertex) {
            Set<Facet> set = vertexToFacetMap.get(vertex);
            return set == null ? Collections.emptySet() : Collections.unmodifiableSet(set);
        }

        /**
         * Returns a set of all facets that have the given points as vertices.
         *
         * @param first  the first vertex.
         * @param second the second vertex.
         * @return a set of all facets that have the given points as vertices.
         */
        private Set<Facet> getFacets(Vector3D first, Vector3D second) {
            Set<Facet> set = new HashSet<>(getFacets(first));
            set.retainAll(getFacets(second));
            return Collections.unmodifiableSet(set);
        }

        /**
         * Finds the horizon of the given set of facets as a set of arrays.
         *
         * @param visibleFacets the given set of facets.
         * @return a set of arrays with size 2.
         */
        private Set<Vector3D[]> getHorizon(Set<Facet> visibleFacets) {
            Set<Vector3D[]> edges = new HashSet<>();
            for (Facet facet : visibleFacets) {
                for (Facet neighbor : findNeighbors(facet)) {
                    if (!visibleFacets.contains(neighbor)) {
                        edges.add(findEdge(facet, neighbor));
                    }
                }
            }
            return edges;
        }

        /**
         * Finds the two vertices that form the edge between the facet and neighbor
         * facet..
         *
         * @param facet    the given facet.
         * @param neighbor the neighboring facet.
         * @return the edge between the two polygons as array.
         */
        private Vector3D[] findEdge(Facet facet, Facet neighbor) {
            List<Vector3D> vertices = new ArrayList<>(facet.getPolygon().getVertices());
            vertices.retainAll(neighbor.getPolygon().getVertices());
            // Only two vertices can remain.
            Vector3D[] edge = {vertices.get(0), vertices.get(1)};
            return edge;
        }

        /**
         * Removes the facets from vertexToFacets map and returns a set of all
         * associated outside points. All outside set associated with the visible facets
         * are added to the possible candidates again.
         *
         * @param visibleFacets a set of facets.
         */
        private void removeFacets(Set<Facet> visibleFacets) {
            visibleFacets.forEach(f -> candidates.addAll(f.outsideSet()));
            if (!vertexToFacetMap.isEmpty()) {
                removeFacetsFromVertexMap(visibleFacets);
            }
        }

        /**
         * Removes the given facets from the vertexToFacetMap.
         *
         * @param visibleFacets the facets to be removed.
         */
        private void removeFacetsFromVertexMap(Set<Facet> visibleFacets) {
            // Remove facets from vertxToFacetMap
            for (Facet facet : visibleFacets) {
                for (Vector3D vertex : facet.getPolygon().getVertices()) {
                    Set<Facet> facets = vertexToFacetMap.get(vertex);
                    facets.remove(facet);
                    if (facets.isEmpty()) {
                        vertexToFacetMap.remove(vertex);
                    }
                }
            }
        }
    }

    /**
     * A facet is a convex polygon with an associated outside set.
     */
    private static class Facet {

        /** The polygon of the facet. */
        private final ConvexPolygon3D polygon;

        /** The outside set of the facet. */
        private final Set<Vector3D> outsideSet;

        /** Precision context used to compare floating point numbers. */
        private final DoubleEquivalence precision;

        /**
         * Constructs a new facet with a the given polygon and an associated empty
         * outside set.
         *
         * @param polygon   the given polygon.
         * @param precision context used to compare floating point numbers.
         */
        Facet(ConvexPolygon3D polygon, DoubleEquivalence precision) {
            this.polygon = polygon;
            outsideSet = EuclideanCollections.pointSet3D(precision);
            this.precision = precision;
        }

        /**
         * Return {@code true} if the facet is in conflict e.g the outside set is
         * non-empty.
         *
         * @return {@code true} if the facet is in conflict e.g the outside set is
         *         non-empty.
         */
        boolean hasOutsidePoints() {
            return !outsideSet.isEmpty();
        }

        /**
         * Returns the associated polygon.
         *
         * @return the associated polygon.
         */
        public ConvexPolygon3D getPolygon() {
            return polygon;
        }

        /**
         * Returns an unmodifiable view of the associated outside set.
         *
         * @return an unmodifiable view of the associated outside set.
         */
        public Set<Vector3D> outsideSet() {
            return outsideSet;
        }

        /**
         * Returns {@code true} if the point resides in the positive half-space defined
         * by the associated hyperplane of the polygon and {@code false} otherwise. If
         * {@code true} the point is added to the associated outside set.
         *
         * @param p the given point.
         * @return {@code true} if the point is added to the outside set, {@code false}
         *         otherwise.
         */
        public boolean addPoint(Vector3D p) {
            return !Builder.isInside(polygon, p, precision) ? outsideSet.add(p) : false;
        }

        /**
         * Returns the outside point with the greatest offset distance to the hyperplane
         * defined by the associated polygon.
         *
         * @return the outside point with the greatest offset distance to the hyperplane
         *         defined by the associated polygon.
         */
        public Vector3D getConflictPoint() {
            Plane plane = polygon.getPlane();
            return outsideSet.stream().max((u, v) -> Double.compare(plane.offset(u), plane.offset(v))).get();
        }
    }

    /**
     * This class represents a simple simplex with four facets.
     */
    private static class Simplex {

        /** The facets of the simplex. */
        private final Set<Facet> facets;

        /**
         * Constructs a new simplex with the given facets.
         * @param facets the given facets.
         */
        Simplex(Collection<Facet> facets) {
            this.facets = new HashSet<>(facets);
        }

        /**
         * Returns {@code true} if the collection of facets is empty.
         *
         * @return {@code true} if the collection of facets is empty.
         */
        public boolean isDegenerate() {
            return facets.isEmpty();
        }

        /**
         * Returns the facets of the simplex as set.
         *
         * @return the facets of the simplex as set.
         */
        public Set<Facet> facets() {
            return facets;
        }
    }
}
