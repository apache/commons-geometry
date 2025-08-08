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

import org.apache.commons.geometry.core.ConvexHull;
import org.apache.commons.geometry.core.collection.PointSet;
import org.apache.commons.geometry.euclidean.EuclideanCollections;
import org.apache.commons.geometry.euclidean.threed.Bounds3D;
import org.apache.commons.geometry.euclidean.threed.ConvexPolygon3D;
import org.apache.commons.geometry.euclidean.threed.ConvexVolume;
import org.apache.commons.geometry.euclidean.threed.Plane;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.line.Line3D;
import org.apache.commons.geometry.euclidean.threed.line.Lines3D;
import org.apache.commons.numbers.core.Precision.DoubleEquivalence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class represents a convex hull in three-dimensional Euclidean space.
 */
public class ConvexHull3D implements ConvexHull<Vector3D> {

    /**
     * The vertices of the convex hull.
     */
    private final List<Vector3D> vertices;

    /**
     * The region defined by the hull.
     */
    private final ConvexVolume region;

    /**
     * A collection of all facets that form the convex volume of the hull.
     */
    private final List<ConvexPolygon3D> facets;

    /**
     * Flag for when the hull is degenerate.
     */
    private final boolean isDegenerate;

    /**
     * Simple constructor no validation performed. This constructor is called if the
     * hull is well-formed and non-degenerative.
     *
     * @param facets the facets of the hull.
     */
    ConvexHull3D(Collection<? extends ConvexPolygon3D> facets) {
        vertices = Collections.unmodifiableList(new ArrayList<>(facets.stream().flatMap(f -> f.getVertices().stream())
                .collect(Collectors.toSet())));
        region = ConvexVolume.fromBounds(() -> facets.stream().map(ConvexPolygon3D::getPlane).iterator());
        this.facets = Collections.unmodifiableList(new ArrayList<>(facets));
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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Vector3D> getVertices() {
        return vertices;
    }

    /**
     * {@inheritDoc}
     */
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
    public List<ConvexPolygon3D> getFacets() {
        return facets;
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
     * Empirically, the number of points processed by quickhull is proportional to
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
     * <li>For each polygon-face (facet) with a non-empty outside set we choose a
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

        /**
         * Set of possible candidates.
         */
        private final PointSet<Vector3D> candidates;

        /**
         * Precision context used to compare floating point numbers.
         */
        private final DoubleEquivalence precision;
        /**
         * Map containing all edges as keys and the associated facets as values.
         */
        private final Map<Edge, Facet> edgeMap;
        /**
         * Simplex for testing new points and starting the algorithm.
         */
        private Simplex simplex;
        /**
         * The minX, maxX, minY, maxY, minZ, maxZ points.
         */
        private Bounds3D box;

        /**
         * Constructor for a builder with the given precision.
         *
         * @param precision the given precision.
         */
        public Builder(DoubleEquivalence precision) {
            candidates = EuclideanCollections.pointSet3D(precision);
            this.precision = precision;
            simplex = new Simplex(Collections.emptySet());
            edgeMap = new HashMap<>();
        }

        /**
         * Associates the given point with an outside set if possible.
         *
         * @param p      the given point.
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
         * Appends to the point to the set of possible candidates.
         *
         * @param point the given point.
         * @return this instance.
         */
        public Builder append(Vector3D point) {
            boolean recomputeSimplex = false;
            if (box == null) {
                box = Bounds3D.from(point);
                recomputeSimplex = true;
            } else if (!box.contains(point)) {
                box = Bounds3D.from(box.getMin(), box.getMax(), point);
                recomputeSimplex = true;
            }
            candidates.add(point);
            if (recomputeSimplex) {
                // Remove all outside Points and add all vertices again.
                removeFacets(simplex.getFacets());
                simplex.getFacets().stream().map(Facet::getPolygon).forEach(p -> candidates.addAll(p.getVertices()));
                simplex = createSimplex(candidates);
            }
            distributePoints(simplex.getFacets());
            return this;
        }

        /**
         * Appends the given collection of points to the set of possible candidates.
         *
         * @param points the given collection of points.
         * @return this instance.
         */
        public Builder append(Collection<Vector3D> points) {
            boolean recomputeSimplex = false;
            if (box == null) {
                box = Bounds3D.from(points);
                recomputeSimplex = true;
            } else if (points.stream().anyMatch(p -> !box.contains(p))) {
                box = Bounds3D.builder().add(box).addAll(points).build();
                recomputeSimplex = true;
            }

            candidates.addAll(points);
            if (recomputeSimplex) {
                // Remove all outside Points and add all vertices again.
                removeFacets(simplex.getFacets());
                simplex.getFacets().stream().map(Facet::getPolygon).forEach(p -> candidates.addAll(p.getVertices()));
                simplex = createSimplex(candidates);
            }
            distributePoints(simplex.getFacets());
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


            simplex.getFacets().forEach(this::addFacet);
            distributePoints(simplex.getFacets());
            Facet conflictFacet = getConflictFacet();
            while (conflictFacet != null) {
                Vector3D conflictPoint = conflictFacet.getOutsidePoint();
                Set<Facet> visibleFacets = new HashSet<>();
                visibleFacets.add(conflictFacet);
                Set<Edge> horizon = new HashSet<>();
                getVisibleFacets(conflictFacet, conflictPoint, visibleFacets, horizon);
                Vector3D referencePoint = conflictFacet.getPolygon().getCentroid();
                Set<Facet> cone = constructCone(conflictPoint, horizon, referencePoint);
                removeFacets(visibleFacets);
                cone.forEach(this::addFacet);
                distributePoints(cone);
                conflictFacet = getConflictFacet();
            }
            Collection<ConvexPolygon3D> hull = edgeMap.values().stream()
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
        private Set<Facet> constructCone(Vector3D conflictPoint, Set<Edge> horizon, Vector3D referencePoint) {
            Set<Facet> newFacets = new HashSet<>();
            for (Edge edge : horizon) {
                ConvexPolygon3D newPolygon = Planes.convexPolygonFromVertices(Arrays.asList(edge.getStart(),
                        edge.getEnd(), conflictPoint), precision);
                newFacets.add(new Facet(newPolygon, referencePoint, precision));
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
            Vector3D vertex2 = points.stream().max(Comparator.comparingDouble(vertex1::distance)).get();

            // The point is degenerate if all points are equivalent.
            if (vertex1.eq(vertex2, precision)) {
                return new Simplex(Collections.emptyList());
            }

            // First and second vertex form a line.
            Line3D line = Lines3D.fromPoints(vertex1, vertex2, precision);

            // Find a point with maximal distance from the line.
            Vector3D vertex3 = points.stream().max(Comparator.comparingDouble(line::distance)).get();

            // The point set is degenerate because all points are collinear.
            if (line.contains(vertex3)) {
                return new Simplex(Collections.emptyList());
            }

            // Form a triangle with the first three vertices.
            ConvexPolygon3D facet1 = Planes.triangleFromVertices(vertex1, vertex2, vertex3, precision);

            // Find a point with maximal distance to the plane formed by the triangle.
            Plane plane = facet1.getPlane();
            Vector3D vertex4 = points.stream().max(Comparator.comparingDouble(d -> Math.abs(plane.offset(d)))).get();

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
            facets.add(new Facet(facet1, vertex4, precision));
            facets.add(new Facet(facet2, vertex3, precision));
            facets.add(new Facet(facet3, vertex2, precision));
            facets.add(new Facet(facet4, vertex1, precision));

            return new Simplex(facets);
        }

        /**
         * Adds the facet for the quickhull algorithm.
         *
         * @param facet the given facet.
         */
        private void addFacet(Facet facet) {
            for (Edge e : facet.getEdges()) {
                edgeMap.put(e, facet);
            }
        }

        /**
         * Associates each point of the candidates set with an outside set of the given
         * facets. Afterward the candidates set is cleared.
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
         * Returns any facet, which is currently in conflict e.g. has a non-empty outside
         * set.
         *
         * @return any facet, which is currently in conflict e.g. has a non-empty outside
         * set.
         */
        private Facet getConflictFacet() {
            return edgeMap.values().stream().filter(Facet::hasOutsidePoints).findFirst()
                    .orElse(null);
        }

        /**
         * Adds all visible facets to the provided set.
         *
         * @param facet         the given conflictFacet.
         * @param outsidePoint  the given outside point.
         * @param visibleFacets visible facets are collected in this set.
         * @param horizon       horizon edges.
         */
        private void getVisibleFacets(Facet facet, Vector3D outsidePoint, Set<Facet> visibleFacets, Set<Edge> horizon) {
            for (Edge e : facet.getEdges()) {
                Facet neighbor = edgeMap.get(e.getInverse());
                if (precision.gt(neighbor.offset(outsidePoint), 0.0)) {
                    if (visibleFacets.add(neighbor)) {
                        getVisibleFacets(neighbor, outsidePoint, visibleFacets, horizon);
                    }
                } else {
                    horizon.add(e);
                }
            }
        }

        /**
         * Removes the facets from vertexToFacets map and returns a set of all
         * associated outside points. All outside set associated with the visible facets
         * are added to the possible candidates again.
         *
         * @param visibleFacets a set of facets.
         */
        private void removeFacets(Set<Facet> visibleFacets) {
            visibleFacets.forEach(f -> candidates.addAll(f.getOutsideSet()));
            if (!edgeMap.isEmpty()) {
                removeFacetsFromVertexMap(visibleFacets);
            }
        }

        /**
         * Removes the given facets from the vertexToFacetMap.
         *
         * @param visibleFacets the facets to be removed.
         */
        private void removeFacetsFromVertexMap(Set<Facet> visibleFacets) {
            // Remove facets from edgeMap
            for (Facet facet : visibleFacets) {
                for (Edge e : facet.getEdges()) {
                    edgeMap.remove(e);
                }
            }
        }

    }

    /**
     * A facet is a convex polygon with an associated outside set.
     */
    static class Facet {

        /**
         * The polygon of the facet.
         */
        private final ConvexPolygon3D polygon;

        /**
         * The edges of the facet.
         */
        private final List<Edge> edges;

        /**
         * The outside set of the facet.
         */
        private final Set<Vector3D> outsideSet;

        /**
         * Precision context used to compare floating point numbers.
         */
        private final DoubleEquivalence precision;

        /**
         * Store the offset with the biggest distance to the plane.
         */
        private double maximumOffset;

        /**
         * Store the point with the biggest distance.
         */
        private Vector3D maximumPoint;

        /**
         * Constructs a new facet with the given polygon and an associated empty
         * outside set in such a way, that the reference point is in the negative half-space of the associated oriented
         * polygon.
         *
         * @param polygon        the given polygon.
         * @param referencePoint reference point for construction.
         * @param precision      context used to compare floating point numbers.
         */
        Facet(ConvexPolygon3D polygon, Vector3D referencePoint, DoubleEquivalence precision) {
            this.polygon = precision.lte(polygon.getPlane().offset(referencePoint), 0) ? polygon : polygon.reverse();
            outsideSet = EuclideanCollections.pointSet3D(precision);
            this.precision = precision;
            List<Edge> edgesCol = new ArrayList<>();
            List<Vector3D> vertices = this.polygon.getVertices();
            maximumOffset = 0.0;

            for (int i = 0; i < vertices.size(); i++) {
                Vector3D start = vertices.get(i);
                Vector3D end = vertices.get(i + 1 == vertices.size() ? 0 : i + 1);
                edgesCol.add(new Edge(start, end));
            }
            edges = Collections.unmodifiableList(edgesCol);
        }

        /**
         * Return {@code true} if the facet is in conflict e.g. the outside set is
         * non-empty.
         *
         * @return {@code true} if the facet is in conflict e.g. the outside set is
         * non-empty.
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
        public Set<Vector3D> getOutsideSet() {
            return outsideSet;
        }

        /**
         * Returns a list of all edges.
         *
         * @return a list of all edges.
         */
        public List<Edge> getEdges() {
            return edges;
        }

        /**
         * Returns {@code true} if the point resides in the positive half-space defined
         * by the associated hyperplane of the polygon and {@code false} otherwise. If
         * {@code true} the point is added to the associated outside set.
         *
         * @param p the given point.
         * @return {@code true} if the point is added to the outside set, {@code false}
         * otherwise.
         */
        public boolean addPoint(Vector3D p) {
            double offset = offset(p);
            if (precision.gt(offset, 0.0)) {
                outsideSet.add(p);
                if (precision.gt(offset, maximumOffset)) {
                    maximumOffset = offset;
                    maximumPoint = p;
                }
                return true;
            }
            return false;
        }

        /**
         * Returns the offset of the given point to the plane defined by this instance.
         *
         * @param point a reference point.
         * @return the offset of the given point to the plane defined by this instance.
         */
        public double offset(Vector3D point) {
            return polygon.getPlane().offset(point);
        }

        /**
         * Returns the outside point with the greatest offset distance to the hyperplane
         * defined by the associated polygon.
         *
         * @return the outside point with the greatest offset distance to the hyperplane
         * defined by the associated polygon.
         */
        public Vector3D getOutsidePoint() {
            return maximumPoint;
        }
    }

    /**
     * This class represents an edge consisting of two vertices. The order of the vertices is not relevant so two edges
     * are equivalent if the edges are equivalent irrespectively of the order.
     */
    static class Edge {

        /**
         * The first vertex.
         */
        private final Vector3D start;

        /**
         * The second vertex.
         */
        private final Vector3D end;

        /**
         * Simple Constructor.
         *
         * @param start the start of the edge.
         * @param end   the end of the edge.
         */
        Edge(Vector3D start, Vector3D end) {
            this.start = start;
            this.end = end;
        }

        /**
         * Getter for the start vertex.
         *
         * @return the start vertex.
         */
        public Vector3D getStart() {
            return start;
        }

        /**
         * Getter for the end vertex.
         *
         * @return the end vertex.
         */
        public Vector3D getEnd() {
            return end;
        }

        /**
         * Returns the inverse of this given edge.
         *
         * @return the inverse of this given edge.
         */
        public Edge getInverse() {
            return new Edge(end, start);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Edge edge = (Edge) o;
            return Objects.equals(start, edge.start) && Objects.equals(end, edge.end);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return Objects.hash(start, end);
        }
    }

    /**
     * This class represents a simple simplex with four facets.
     */
    private static class Simplex {


        /**
         * The facets of the simplex.
         */
        private final Set<Facet> facets;

        /**
         * Constructs a new simplex with the given facets.
         *
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
        public Set<Facet> getFacets() {
            return facets;
        }
    }
}
