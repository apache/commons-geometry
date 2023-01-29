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
package org.apache.commons.geometry.hull.euclidean.threed;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.geometry.euclidean.EuclideanCollections;
import org.apache.commons.geometry.euclidean.threed.ConvexPolygon3D;
import org.apache.commons.geometry.euclidean.threed.ConvexVolume;
import org.apache.commons.geometry.euclidean.threed.Plane;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.line.Line3D;
import org.apache.commons.geometry.euclidean.threed.line.Lines3D;
import org.apache.commons.geometry.hull.ConvexHullGenerator;
import org.apache.commons.numbers.core.Precision.DoubleEquivalence;

/**
 * Implementation of quick-hull algorithm by Barber, Dobkin and Huhdanpaa. The
 * algorithm constructs the convex hull of a given finite set of points.
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
public class QuickHull3D implements ConvexHullGenerator<Vector3D> {

    /**
     * Precision context used to compare floating point numbers.
     */
    private final DoubleEquivalence precision;

    /**
     * Constructor for a quick hull generator with the given precision.
     *
     * @param precision context used to compare floating point numbers.
     */
    public QuickHull3D(DoubleEquivalence precision) {
        this.precision = precision;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConvexHull3D generate(Collection<Vector3D> points) {
        if (points.size() < 4) {
            return degenerateHull(points);
        }

        // Construct an initial simplex with extreme properties.
        Collection<ConvexPolygon3D> simplex = createSimplex(points);

        // If the collection is empty the hull is degenerate.
        if (simplex.isEmpty()) {
            return degenerateHull(points);
        }

        // The hull builder. Append facets and points
        Builder builder = new Builder();
        simplex.forEach(builder::addFacet);
        points.forEach(builder::appendPoint);

        while (builder.isInConflict()) {
            Entry<ConvexPolygon3D, Vector3D> conflictEntry = builder.getConflictEntry();
            ConvexPolygon3D conflictFace = conflictEntry.getKey();
            Vector3D conflictPoint = conflictEntry.getValue();

            Collection<ConvexPolygon3D> visibleFacets = builder.findVisibleFacets(conflictFace, conflictPoint);
            List<Vector3D[]> horizon = builder.findHorizon(visibleFacets);

            // Construct a cone from the horizon and conflict point, The conflict face is
            // used for
            List<ConvexPolygon3D> cone = constructCone(conflictFace.getCentroid(), conflictPoint, horizon);

            // Remove all visible facets and redistribute the associated outside points.
            Collection<Vector3D> outsidePoints = builder.remove(visibleFacets);
            cone.forEach(builder::addFacet);
            outsidePoints.forEach(builder::appendPoint);
        }

        return new SimpleConvexHull3D(builder.getFacets());
    }

    /**
     * Constructs a new cone of polygons from the horizon to the given point. All
     * constructed facets are oriented in such a way, that the reference points
     * resides in the negative half-space of all newly constructed polygons.
     *
     * @param referencePoint reference point for orientation.
     * @param point          the point that is to be appended to the hull.
     * @param horizon        the horizon which form the other edges of the cone.
     * @return a list of newly constructed polygons which form a cone.
     */
    private List<ConvexPolygon3D> constructCone(Vector3D referencePoint, Vector3D point, List<Vector3D[]> horizon) {
        List<ConvexPolygon3D> newFacets = new ArrayList<>();
        for (Vector3D[] edge : horizon) {
            ConvexPolygon3D newFacet = Planes.convexPolygonFromVertices(Arrays.asList(edge[0], edge[1], point),
                    precision);
            if (!isInside(newFacet, referencePoint)) {
                newFacet = newFacet.reverse();
            }
            newFacets.add(newFacet);
        }
        return newFacets;
    }

    /**
     * Create an initial simplex for the given point set. If no non-zero simplex can
     * be formed the point set is degenerate and an empty Collection is returned.
     * Each vertex of the simplex is contained in the given point set.
     *
     * @param points the given point set.
     * @return an initial simplex.
     */
    private Collection<ConvexPolygon3D> createSimplex(Collection<Vector3D> points) {

        // First vertex of the simplex
        Vector3D vertex1 = points.stream().min(Vector3D.COORDINATE_ASCENDING_ORDER).get();

        // Find a point with maximal distance to the second.
        Vector3D vertex2 = points.stream().max((u, v) -> Double.compare(vertex1.distance(u), vertex1.distance(v)))
                .get();

        // The point is degenerate if all points are equivalent.
        if (vertex1.eq(vertex2, precision)) {
            return Collections.emptyList();
        }

        // First and second vertex form a line.
        Line3D line = Lines3D.fromPoints(vertex1, vertex2, precision);

        // Find a point with maximal distance from the line.
        Vector3D vertex3 = points.stream().max((u, v) -> Double.compare(line.distance(u), line.distance(v))).get();

        // The point set is degenerate because all points are colinear.
        if (line.contains(vertex3)) {
            return Collections.emptyList();
        }

        // Form a triangle with the first three vertices.
        ConvexPolygon3D facet1 = Planes.triangleFromVertices(vertex1, vertex2, vertex3, precision);

        // Find a point with maximal distance to the plane formed by the triangle.
        Plane plane = facet1.getPlane();
        Vector3D vertex4 = points.stream()
                .max((u, v) -> Double.compare(Math.abs(plane.offset(u)), Math.abs(plane.offset(v)))).get();

        // The point set is degenerate, because all points are coplanar.
        if (plane.contains(vertex4)) {
            return Collections.emptyList();
        }

        // Construct the other three facets.
        ConvexPolygon3D facet2 = Planes.convexPolygonFromVertices(Arrays.asList(vertex1, vertex2, vertex4), precision);
        ConvexPolygon3D facet3 = Planes.convexPolygonFromVertices(Arrays.asList(vertex1, vertex3, vertex4), precision);
        ConvexPolygon3D facet4 = Planes.convexPolygonFromVertices(Arrays.asList(vertex2, vertex3, vertex4), precision);

        facet1 = isInside(facet1, vertex4) ? facet1 : facet1.reverse();
        facet2 = isInside(facet2, vertex3) ? facet2 : facet2.reverse();
        facet3 = isInside(facet3, vertex2) ? facet3 : facet3.reverse();
        facet4 = isInside(facet4, vertex1) ? facet4 : facet4.reverse();

        return Arrays.asList(facet1, facet2, facet3, facet4);
    }

    /**
     * Returns a facet which is oriented in such a way, that the given point lies in
     * the negative half-space of the given facet.
     *
     * @param facet the given facet.
     * @param point a reference point.
     * @return an oriented facet.
     */
    private boolean isInside(ConvexPolygon3D facet, Vector3D point) {
        return precision.lte(facet.getPlane().offset(point), 0);
    }

    /**
     * Returns a degenerate hull. A degenerate hull cannot form a finite non-zero
     * region.
     *
     * @param points the points of the hull.
     * @return a degenerate hull.
     */
    private static ConvexHull3D degenerateHull(Collection<Vector3D> points) {
        return new ConvexHull3D() {

            private List<Vector3D> vertices = Collections.unmodifiableList(new ArrayList<>(points));

            @Override
            public List<Vector3D> getVertices() {
                return vertices;
            }

            @Override
            public ConvexVolume getRegion() {
                return null;
            }

            @Override
            public Collection<? extends ConvexPolygon3D> getFacets() {
                return Collections.emptyList();
            }
        };
    }

    /**
     * Builder class keeps track of all facets and their neighbors. Furthermore the
     * class contains contains outside sets facets.
     */
    private class Builder {

        /**
         * A map which contains all the vertices of the current hull as keys and the
         * associated facets as values.
         */
        private final Map<Vector3D, Set<ConvexPolygon3D>> vertexToFacetMap;

        /**
         * A map containing all outside set.
         */
        private final Map<ConvexPolygon3D, Set<Vector3D>> outsideSets;

        /**
         * Constructor for a quick hull builder with the given precision.
         */
        Builder() {
            this.vertexToFacetMap = EuclideanCollections.pointMap3D(precision);
            this.outsideSets = new HashMap<>();
        }

        /**
         * Return a list of all facets which are currently added to the builder.
         *
         * @return a list of all facets which are currently added to the builder.
         */
        Collection<? extends ConvexPolygon3D> getFacets() {
            return outsideSets.keySet();
        }

        /**
         * Removes all the facets in the collection from the builder and returns all
         * previously associated outside points.
         *
         * @param visibleFacets the collection of facets.
         * @return a collection of all previously associated outside points.
         */
        Collection<Vector3D> remove(Collection<ConvexPolygon3D> visibleFacets) {
            Set<Vector3D> outsidePoints = EuclideanCollections.pointSet3D(precision);
            visibleFacets.forEach(f -> outsidePoints.addAll(remove(f)));
            return outsidePoints;
        }

        /**
         * Removes the polygon from the vertex to facet map and returns the set off
         * outside points. Previously associated with the facet.
         *
         * @param facet the facet that will be removed.
         * @return the collection of outside points previously associated with the
         *         facet.
         */
        Collection<Vector3D> remove(ConvexPolygon3D facet) {
            for (Vector3D vertex : facet.getVertices()) {
                Set<ConvexPolygon3D> facets = vertexToFacetMap.get(vertex);
                facets.remove(facet);
                if (facets.isEmpty()) {
                    vertexToFacetMap.remove(vertex);
                }
            }

            // Get the associated outside set
            return outsideSets.remove(facet);
        }

        /**
         * Adds a vertex to facet mapping for each vertex of the facet.
         *
         * @param facet the given facet.
         */
        void addFacet(ConvexPolygon3D facet) {
            if (outsideSets.containsKey(facet)) {
                return;
            }
            // Add the facet as key for outside sets.
            outsideSets.put(facet, Collections.emptySet());

            // Add facet to vertex map.
            for (Vector3D vertex : facet.getVertices()) {
                Set<ConvexPolygon3D> value = vertexToFacetMap.get(vertex);
                if (Objects.isNull(value)) {
                    value = new HashSet<>(3);
                    value.add(facet);
                    vertexToFacetMap.put(vertex, value);
                } else {
                    value.add(facet);
                }
            }
        }

        /**
         * Appends the point to the hull and assigns it to a outside set if possible. If
         * the point is on the inside of each hull it is discarded.
         *
         * @param point the point that is to be appended to the hull.
         */
        void appendPoint(Vector3D point) {
            for (Entry<ConvexPolygon3D, Set<Vector3D>> entry : outsideSets.entrySet()) {
                ConvexPolygon3D facet = entry.getKey();
                if (!isInside(facet, point)) {
                    Set<Vector3D> outsideSet = entry.getValue();
                    outsideSet = outsideSet.isEmpty() ? EuclideanCollections.pointSet3D(precision) : outsideSet;
                    outsideSet.add(point);
                    outsideSets.put(facet, outsideSet);
                    return;
                }
            }
        }

        /**
         * Returns {@code true} if any facet as a non-empty outside set, {@code false}
         * otherwise.
         *
         * @return {@code true} if any facet as a non-empty outside set, {@code false}
         *         otherwise.
         */
        boolean isInConflict() {
            for (Set<Vector3D> outsideSet : outsideSets.values()) {
                if (!outsideSet.isEmpty()) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Returns any facet with a non-empty outside set as key and the farthest point
         * of said outside set as value.
         *
         * @return any facet with a non-empty outside set as key and the farthest point
         *         of said outside set as value.
         */
        Entry<ConvexPolygon3D, Vector3D> getConflictEntry() {
            for (Entry<ConvexPolygon3D, Set<Vector3D>> entry : outsideSets.entrySet()) {
                Set<Vector3D> outsideSet = entry.getValue();
                ConvexPolygon3D polygon = entry.getKey();
                Plane plane = polygon.getPlane();
                Optional<Vector3D> point = outsideSet.stream()
                        .max((u, v) -> Double.compare(plane.offset(u), plane.offset(v)));
                if (point.isPresent()) {
                    return new SimpleEntry<>(polygon, point.get());
                }
            }
            // No conflict entry can be found.
            return null;
        }

        /**
         * Returns a collection of all polygons that have the given point as vertex.
         *
         * @param vertex the given vertex.
         * @return a collection of all polygons that have the given points as vertices.
         */
        Collection<ConvexPolygon3D> get(Vector3D vertex) {
            return vertexToFacetMap.containsKey(vertex) ? Collections.unmodifiableSet(vertexToFacetMap.get(vertex)) :
                Collections.emptySet();
        }

        /**
         * Returns a collection of all polygons that have the given points as vertices.
         *
         * @param first  the first vertex.
         * @param second the second vertex.
         * @return a collection of all polygons that have the given points as vertices.
         */
        Collection<ConvexPolygon3D> get(Vector3D first, Vector3D second) {
            Set<ConvexPolygon3D> set = new HashSet<>(get(first));
            set.retainAll(get(second));
            return Collections.unmodifiableCollection(set);
        }

        /**
         * Returns a collection of all the neighbors of this facet.
         *
         * @param polygon the given facet.
         * @return a collection of all neighbors.
         */
        Collection<ConvexPolygon3D> findNeighbors(ConvexPolygon3D polygon) {
            List<Vector3D> vertices = polygon.getVertices();
            HashSet<ConvexPolygon3D> neighbors = new HashSet<>();
            for (int i = 0; i < vertices.size(); i++) {
                for (int j = 0; j < vertices.size(); j++) {
                    if (i == j) {
                        continue;
                    }
                    neighbors.addAll(get(vertices.get(i), vertices.get(j)));
                }
            }
            neighbors.remove(polygon);
            return neighbors;
        }

        /**
         * Returns a collection of all polygons which are visible from the given outside
         * point.
         *
         * @param conflictFace the starting point of the recursive seach for all visible facets.
         * @param outsidePoint the outside point which is visible from all returned facets.
         * @return a collection of all visible facets from the outside point.
         */
        Collection<ConvexPolygon3D> findVisibleFacets(ConvexPolygon3D conflictFace, Vector3D outsidePoint) {
            Collection<ConvexPolygon3D> visibleFacets = new HashSet<>();
            addIfVisible(outsidePoint, conflictFace, visibleFacets);
            return visibleFacets;
        }

        /**
         * Checks if the given facet is visible from the outside point. If visible the
         * facets is added to the collection and checks recursively all neighbors.
         *
         * @param outsidePoint  the outside point.
         * @param facet         the facet to be checked.
         * @param visibleFacets a collection of all visible facets.
         */
        void addIfVisible(Vector3D outsidePoint, ConvexPolygon3D facet, Collection<ConvexPolygon3D> visibleFacets) {
            if (visibleFacets.contains(facet)) {
                return;
            }
            if (precision.gt(facet.getPlane().offset(outsidePoint), 0)) {
                visibleFacets.add(facet);
                for (ConvexPolygon3D neighbor : findNeighbors(facet)) {
                    addIfVisible(outsidePoint, neighbor, visibleFacets);
                }
            }
        }

        /**
         * Find all segments that make up the horizon.
         *
         * @param visibleFacets the visible facets.
         * @return an ordered list of edges that make up the horizon. The order defines
         *         a path around the horizon.
         */
        List<Vector3D[]> findHorizon(Collection<ConvexPolygon3D> visibleFacets) {
            List<Vector3D[]> edges = new ArrayList<>();
            for (ConvexPolygon3D facet : visibleFacets) {
                for (ConvexPolygon3D neighbor : findNeighbors(facet)) {
                    if (!visibleFacets.contains(neighbor)) {
                        edges.add(findEdge(facet, neighbor));
                    }
                }
            }
            return order(edges);
        }

        /**
         * Orders the list of edges in such a way that it forms a closed path.
         *
         * @param edges the list of edges.
         * @return an ordered list of edges.
         */
        List<Vector3D[]> order(List<Vector3D[]> edges) {
            for (int i = 0; i < edges.size(); i++) {
                Vector3D[] current = edges.get(i);
                for (int j = i + 1; j < edges.size(); j++) {
                    Vector3D[] next = edges.get(j);
                    if (next[0].eq(current[1], precision)) {
                        edges.add(i + 1, edges.remove(j));
                        break;
                    } else if (next[1].eq(current[1], precision)) {
                        next = edges.remove(j);
                        Vector3D[] nextReversed = {next[1], next[0]};
                        edges.add(i + 1, nextReversed);
                        break;
                    }
                    // If no segment can be found until the last one an error occured
                    if (j == edges.size() - 1) {
                        throw new RuntimeException("No link can be found");
                    }
                }
            }
            return edges;
        }

        /**
         * Finds the two vertices that form the edge between the first and second
         * polygon.
         *
         * @param first  the first polygon.
         * @param second the second polygon.
         * @return the edge between the two polygons as array.
         */
        Vector3D[] findEdge(ConvexPolygon3D first, ConvexPolygon3D second) {
            List<Vector3D> vertices = new ArrayList<>(first.getVertices());
            vertices.retainAll(second.getVertices());
            // Only two vertices can remain.
            Vector3D[] edge = {vertices.get(0), vertices.get(1)};
            return edge;
        }
    }
}
