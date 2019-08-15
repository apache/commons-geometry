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
package org.apache.commons.geometry.spherical.twod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.partition.SubHyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.enclosing.EnclosingBall;
import org.apache.commons.geometry.enclosing.WelzlEncloser;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.enclosing.SphereGenerator;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.spherical.oned.S1Point;
import org.apache.commons.geometry.spherical.partitioning.AbstractRegion_Old;
import org.apache.commons.geometry.spherical.partitioning.BSPTree_Old;
import org.apache.commons.geometry.spherical.partitioning.BoundaryProjection_Old;
import org.apache.commons.geometry.spherical.partitioning.RegionFactory_Old;
import org.apache.commons.geometry.spherical.partitioning.SubHyperplane_Old;

/** This class represents a region on the 2-sphere: a set of spherical polygons.
 */
public class SphericalPolygonsSet extends AbstractRegion_Old<S2Point, S1Point> {

    /** Boundary defined as an array of closed loops start vertices. */
    private List<Vertex> loops;

    /** Build a polygons set representing the whole real 2-sphere.
     * @param precision precision context used to compare floating point values
     */
    public SphericalPolygonsSet(final DoublePrecisionContext precision) {
        super(precision);
    }

    /** Build a polygons set representing a hemisphere.
     * @param pole pole of the hemisphere (the pole is in the inside half)
     * @param precision precision context used to compare floating point values
     */
    public SphericalPolygonsSet(final Vector3D pole, final DoublePrecisionContext precision) {
        super(new BSPTree_Old<>(new Circle(pole, precision).wholeHyperplane(),
                                    new BSPTree_Old<S2Point>(Boolean.FALSE),
                                    new BSPTree_Old<S2Point>(Boolean.TRUE),
                                    null),
              precision);
    }

    /** Build a polygons set representing a regular polygon.
     * @param center center of the polygon (the center is in the inside half)
     * @param meridian point defining the reference meridian for first polygon vertex
     * @param outsideRadius distance of the vertices to the center
     * @param n number of sides of the polygon
     * @param precision precision context used to compare floating point values
     */
    public SphericalPolygonsSet(final Vector3D center, final Vector3D meridian,
                                final double outsideRadius, final int n,
                                final DoublePrecisionContext precision) {
        this(precision, createRegularPolygonVertices(center, meridian, outsideRadius, n));
    }

    /** Build a polygons set from a BSP tree.
     * <p>The leaf nodes of the BSP tree <em>must</em> have a
     * {@code Boolean} attribute representing the inside status of
     * the corresponding cell (true for inside cells, false for outside
     * cells). In order to avoid building too many small objects, it is
     * recommended to use the predefined constants
     * {@code Boolean.TRUE} and {@code Boolean.FALSE}</p>
     * @param tree inside/outside BSP tree representing the region
     * @param precision precision context used to compare floating point values
     */
    public SphericalPolygonsSet(final BSPTree_Old<S2Point> tree, final DoublePrecisionContext precision) {
        super(tree, precision);
    }

    /** Build a polygons set from a Boundary REPresentation (B-rep).
     * <p>The boundary is provided as a collection of {@link
     * SubHyperplane sub-hyperplanes}. Each sub-hyperplane has the
     * interior part of the region on its minus side and the exterior on
     * its plus side.</p>
     * <p>The boundary elements can be in any order, and can form
     * several non-connected sets (like for example polygons with holes
     * or a set of disjoint polygons considered as a whole). In
     * fact, the elements do not even need to be connected together
     * (their topological connections are not used here). However, if the
     * boundary does not really separate an inside open from an outside
     * open (open having here its topological meaning), then subsequent
     * calls to the {@link
     * org.apache.commons.geometry.core.partitioning.Region#checkPoint(org.apache.commons.geometry.core.Point)
     * checkPoint} method will not be meaningful anymore.</p>
     * <p>If the boundary is empty, the region will represent the whole
     * space.</p>
     * @param boundary collection of boundary elements, as a
     * collection of {@link SubHyperplane SubHyperplane} objects
     * @param precision precision context used to compare floating point values
     */
    public SphericalPolygonsSet(final Collection<SubHyperplane_Old<S2Point>> boundary, final DoublePrecisionContext precision) {
        super(boundary, precision);
    }

    /** Build a polygon from a simple list of vertices.
     * <p>The boundary is provided as a list of points considering to
     * represent the vertices of a simple loop. The interior part of the
     * region is on the left side of this path and the exterior is on its
     * right side.</p>
     * <p>This constructor does not handle polygons with a boundary
     * forming several disconnected paths (such as polygons with holes).</p>
     * <p>For cases where this simple constructor applies, it is expected to
     * be numerically more robust than the {@link #SphericalPolygonsSet(Collection, DoublePrecisionContext)
     * general constructor} using {@link SubHyperplane subhyperplanes}.</p>
     * <p>If the list is empty, the region will represent the whole
     * space.</p>
     * <p>
     * Polygons with thin pikes or dents are inherently difficult to handle because
     * they involve circles with almost opposite directions at some vertices. Polygons
     * whose vertices come from some physical measurement with noise are also
     * difficult because an edge that should be straight may be broken in lots of
     * different pieces with almost equal directions. In both cases, computing the
     * circles intersections is not numerically robust due to the almost 0 or almost
     * &pi; angle. Such cases need to carefully adjust the {@code hyperplaneThickness}
     * parameter. A too small value would often lead to completely wrong polygons
     * with large area wrongly identified as inside or outside. Large values are
     * often much safer. As a rule of thumb, a value slightly below the size of the
     * most accurate detail needed is a good value for the {@code hyperplaneThickness}
     * parameter.
     * </p>
     * @param precision precision context used to compare floating point values
     * @param vertices vertices of the simple loop boundary
     */
    public SphericalPolygonsSet(final DoublePrecisionContext precision, final S2Point ... vertices) {
        super(verticesToTree(precision, vertices), precision);
    }

    /** Build the vertices representing a regular polygon.
     * @param center center of the polygon (the center is in the inside half)
     * @param meridian point defining the reference meridian for first polygon vertex
     * @param outsideRadius distance of the vertices to the center
     * @param n number of sides of the polygon
     * @return vertices array
     */
    private static S2Point[] createRegularPolygonVertices(final Vector3D center, final Vector3D meridian,
                                                          final double outsideRadius, final int n) {
        final S2Point[] array = new S2Point[n];
        final QuaternionRotation r0 = QuaternionRotation.fromAxisAngle(center.cross(meridian),
                                         outsideRadius);
        array[0] = S2Point.ofVector(r0.apply(center));

        final QuaternionRotation r = QuaternionRotation.fromAxisAngle(center, Geometry.TWO_PI / n);
        for (int i = 1; i < n; ++i) {
            array[i] = S2Point.ofVector(r.apply(array[i - 1].getVector()));
        }

        return array;
    }

    /** Build the BSP tree of a polygons set from a simple list of vertices.
     * <p>The boundary is provided as a list of points considering to
     * represent the vertices of a simple loop. The interior part of the
     * region is on the left side of this path and the exterior is on its
     * right side.</p>
     * <p>This constructor does not handle polygons with a boundary
     * forming several disconnected paths (such as polygons with holes).</p>
     * <p>This constructor handles only polygons with edges strictly shorter
     * than \( \pi \). If longer edges are needed, they need to be broken up
     * in smaller sub-edges so this constraint holds.</p>
     * <p>For cases where this simple constructor applies, it is expected to
     * be numerically more robust than the {@link #PolygonsSet(Collection) general
     * constructor} using {@link SubHyperplane subhyperplanes}.</p>
     * @param precision precision context used to compare floating point values
     * @param vertices vertices of the simple loop boundary
     * @return the BSP tree of the input vertices
     */
    private static BSPTree_Old<S2Point> verticesToTree(final DoublePrecisionContext precision,
                                                    final S2Point ... vertices) {

        final int n = vertices.length;
        if (n == 0) {
            // the tree represents the whole space
            return new BSPTree_Old<>(Boolean.TRUE);
        }

        // build the vertices
        final Vertex[] vArray = new Vertex[n];
        for (int i = 0; i < n; ++i) {
            vArray[i] = new Vertex(vertices[i]);
        }

        // build the edges
        List<Edge> edges = new ArrayList<>(n);
        Vertex end = vArray[n - 1];
        for (int i = 0; i < n; ++i) {

            // get the endpoints of the edge
            final Vertex start = end;
            end = vArray[i];

            // get the circle supporting the edge, taking care not to recreate it
            // if it was already created earlier due to another edge being aligned
            // with the current one
            Circle circle = start.sharedCircleWith(end);
            if (circle == null) {
                circle = new Circle(start.getLocation(), end.getLocation(), precision);
            }

            // create the edge and store it
            edges.add(new Edge(start, end,
                               start.getLocation().getVector().angle(
                                              end.getLocation().getVector()),
                               circle));

            // check if another vertex also happens to be on this circle
            for (final Vertex vertex : vArray) {
                if (vertex != start && vertex != end &&
                    precision.eqZero(circle.getOffset(vertex.getLocation()))) {
                    vertex.bindWith(circle);
                }
            }

        }

        // build the tree top-down
        final BSPTree_Old<S2Point> tree = new BSPTree_Old<>();
        insertEdges(precision, tree, edges);

        return tree;

    }

    /** Recursively build a tree by inserting cut sub-hyperplanes.
     * @param precision precision context used to compare floating point values
     * @param node current tree node (it is a leaf node at the beginning
     * of the call)
     * @param edges list of edges to insert in the cell defined by this node
     * (excluding edges not belonging to the cell defined by this node)
     */
    private static void insertEdges(final DoublePrecisionContext precision,
                                    final BSPTree_Old<S2Point> node,
                                    final List<Edge> edges) {

        // find an edge with an hyperplane that can be inserted in the node
        int index = 0;
        Edge inserted = null;
        while (inserted == null && index < edges.size()) {
            inserted = edges.get(index++);
            if (!node.insertCut(inserted.getCircle())) {
                inserted = null;
            }
        }

        if (inserted == null) {
            // no suitable edge was found, the node remains a leaf node
            // we need to set its inside/outside boolean indicator
            final BSPTree_Old<S2Point> parent = node.getParent();
            if (parent == null || node == parent.getMinus()) {
                node.setAttribute(Boolean.TRUE);
            } else {
                node.setAttribute(Boolean.FALSE);
            }
            return;
        }

        // we have split the node by inserting an edge as a cut sub-hyperplane
        // distribute the remaining edges in the two sub-trees
        final List<Edge> outsideList = new ArrayList<>();
        final List<Edge> insideList  = new ArrayList<>();
        for (final Edge edge : edges) {
            if (edge != inserted) {
                edge.split(inserted.getCircle(), outsideList, insideList);
            }
        }

        // recurse through lower levels
        if (!outsideList.isEmpty()) {
            insertEdges(precision, node.getPlus(), outsideList);
        } else {
            node.getPlus().setAttribute(Boolean.FALSE);
        }
        if (!insideList.isEmpty()) {
            insertEdges(precision, node.getMinus(),  insideList);
        } else {
            node.getMinus().setAttribute(Boolean.TRUE);
        }

    }

    /** {@inheritDoc} */
    @Override
    public SphericalPolygonsSet buildNew(final BSPTree_Old<S2Point> tree) {
        return new SphericalPolygonsSet(tree, getPrecision());
    }

    /** {@inheritDoc}
     * @exception IllegalStateException if the tolerance setting does not allow to build
     * a clean non-ambiguous boundary
     */
    @Override
    protected void computeGeometricalProperties() {

        final BSPTree_Old<S2Point> tree = getTree(true);

        if (tree.getCut() == null) {

            // the instance has a single cell without any boundaries

            if (tree.getCut() == null && (Boolean) tree.getAttribute()) {
                // the instance covers the whole space
                setSize(4 * Math.PI);
                setBarycenter(S2Point.of(0, 0));
            } else {
                setSize(0);
                setBarycenter(S2Point.NaN);
            }

        } else {

            // the instance has a boundary
            final PropertiesComputer pc = new PropertiesComputer(getPrecision());
            tree.visit(pc);
            setSize(pc.getArea());
            setBarycenter(pc.getBarycenter());

        }

    }

    /** Get the boundary loops of the polygon.
     * <p>The polygon boundary can be represented as a list of closed loops,
     * each loop being given by exactly one of its vertices. From each loop
     * start vertex, one can follow the loop by finding the outgoing edge,
     * then the end vertex, then the next outgoing edge ... until the start
     * vertex of the loop (exactly the same instance) is found again once
     * the full loop has been visited.</p>
     * <p>If the polygon has no boundary at all, a zero length loop
     * array will be returned.</p>
     * <p>If the polygon is a simple one-piece polygon, then the returned
     * array will contain a single vertex.
     * </p>
     * <p>All edges in the various loops have the inside of the region on
     * their left side (i.e. toward their pole) and the outside on their
     * right side (i.e. away from their pole) when moving in the underlying
     * circle direction. This means that the closed loops obey the direct
     * trigonometric orientation.</p>
     * @return boundary of the polygon, organized as an unmodifiable list of loops start vertices.
     * @exception IllegalStateException if the tolerance setting does not allow to build
     * a clean non-ambiguous boundary
     * @see Vertex
     * @see Edge
     */
    public List<Vertex> getBoundaryLoops() {

        if (loops == null) {
            if (getTree(false).getCut() == null) {
                loops = Collections.emptyList();
            } else {

                // sort the arcs according to their start point
                final BSPTree_Old<S2Point> root = getTree(true);
                final EdgesBuilder visitor = new EdgesBuilder(root, getPrecision());
                root.visit(visitor);
                final List<Edge> edges = visitor.getEdges();


                // convert the list of all edges into a list of start vertices
                loops = new ArrayList<>();
                while (!edges.isEmpty()) {

                    // this is an edge belonging to a new loop, store it
                    Edge edge = edges.get(0);
                    final Vertex startVertex = edge.getStart();
                    loops.add(startVertex);

                    // remove all remaining edges in the same loop
                    do {

                        // remove one edge
                        for (final Iterator<Edge> iterator = edges.iterator(); iterator.hasNext();) {
                            if (iterator.next() == edge) {
                                iterator.remove();
                                break;
                            }
                        }

                        // go to next edge following the boundary loop
                        edge = edge.getEnd().getOutgoing();

                    } while (edge.getStart() != startVertex);

                }

            }
        }

        return Collections.unmodifiableList(loops);

    }

    /** Get a spherical cap enclosing the polygon.
     * <p>
     * This method is intended as a first test to quickly identify points
     * that are guaranteed to be outside of the region, hence performing a full
     * {@link #checkPoint(org.apache.commons.geometry.core.Point) checkPoint}
     * only if the point status remains undecided after the quick check. It is
     * is therefore mostly useful to speed up computation for small polygons with
     * complex shapes (say a country boundary on Earth), as the spherical cap will
     * be small and hence will reliably identify a large part of the sphere as outside,
     * whereas the full check can be more computing intensive. A typical use case is
     * therefore:
     * </p>
     * <pre>{@code
     *   // compute region, plus an enclosing spherical cap
     *   SphericalPolygonsSet complexShape = ...;
     *   EnclosingBall<S2Point, S2Point> cap = complexShape.getEnclosingCap();
     *
     *   // check lots of points
     *   for (Vector3D p : points) {
     *
     *     final Location l;
     *     if (cap.contains(p)) {
     *       // we cannot be sure where the point is
     *       // we need to perform the full computation
     *       l = complexShape.checkPoint(v);
     *     } else {
     *       // no need to do further computation,
     *       // we already know the point is outside
     *       l = Location.OUTSIDE;
     *     }
     *
     *     // use l ...
     *
     *   }
     * }</pre>
     * <p>
     * In the special cases of empty or whole sphere polygons, special
     * spherical caps are returned, with angular radius set to negative
     * or positive infinity so the {@link
     * EnclosingBall#contains(org.apache.commons.geometry.core.Point) ball.contains(point)}
     * method return always false or true.
     * </p>
     * <p>
     * This method is <em>not</em> guaranteed to return the smallest enclosing cap.
     * </p>
     * @return a spherical cap enclosing the polygon
     */
    public EnclosingBall<S2Point> getEnclosingCap() {

        // handle special cases first
        if (isEmpty()) {
            return new EnclosingBall<>(S2Point.PLUS_K, Double.NEGATIVE_INFINITY);
        }
        if (isFull()) {
            return new EnclosingBall<>(S2Point.PLUS_K, Double.POSITIVE_INFINITY);
        }

        // as the polygons is neither empty nor full, it has some boundaries and cut hyperplanes
        final BSPTree_Old<S2Point> root = getTree(false);
        if (isEmpty(root.getMinus()) && isFull(root.getPlus())) {
            // the polygon covers an hemisphere, and its boundary is one 2π long edge
            final Circle circle = (Circle) root.getCut().getHyperplane();
            return new EnclosingBall<>(S2Point.ofVector(circle.getPole()).negate(),
                                                        0.5 * Math.PI);
        }
        if (isFull(root.getMinus()) && isEmpty(root.getPlus())) {
            // the polygon covers an hemisphere, and its boundary is one 2π long edge
            final Circle circle = (Circle) root.getCut().getHyperplane();
            return new EnclosingBall<>(S2Point.ofVector(circle.getPole()),
                                                        0.5 * Math.PI);
        }

        // gather some inside points, to be used by the encloser
        final List<Vector3D> points = getInsidePoints();

        // extract points from the boundary loops, to be used by the encloser as well
        final List<Vertex> boundary = getBoundaryLoops();
        for (final Vertex loopStart : boundary) {
            int count = 0;
            for (Vertex v = loopStart; count == 0 || v != loopStart; v = v.getOutgoing().getEnd()) {
                ++count;
                points.add(v.getLocation().getVector());
            }
        }

        // find the smallest enclosing 3D sphere
        final SphereGenerator generator = new SphereGenerator();
        final WelzlEncloser<Vector3D> encloser =
                new WelzlEncloser<>(getPrecision(), generator);
        EnclosingBall<Vector3D> enclosing3D = encloser.enclose(points);
        final Vector3D[] support3D = enclosing3D.getSupport();

        // convert to 3D sphere to spherical cap
        final double r = enclosing3D.getRadius();
        final double h = enclosing3D.getCenter().norm();
        if (getPrecision().eqZero(h)) {
            // the 3D sphere is centered on the unit sphere and covers it
            // fall back to a crude approximation, based only on outside convex cells
            EnclosingBall<S2Point> enclosingS2 =
                    new EnclosingBall<>(S2Point.PLUS_K, Double.POSITIVE_INFINITY);
            for (Vector3D outsidePoint : getOutsidePoints()) {
                final S2Point outsideS2 = S2Point.ofVector(outsidePoint);
                final BoundaryProjection_Old<S2Point> projection = projectToBoundary(outsideS2);
                if (Math.PI - projection.getOffset() < enclosingS2.getRadius()) {
                    enclosingS2 = new EnclosingBall<>(outsideS2.negate(),
                                                                       Math.PI - projection.getOffset(),
                                                                       projection.getProjected());
                }
            }
            return enclosingS2;
        }
        final S2Point[] support = new S2Point[support3D.length];
        for (int i = 0; i < support3D.length; ++i) {
            support[i] = S2Point.ofVector(support3D[i]);
        }

        final EnclosingBall<S2Point> enclosingS2 =
                new EnclosingBall<>(S2Point.ofVector(enclosing3D.getCenter()),
                                                     Math.acos((1 + h * h - r * r) / (2 * h)),
                                                     support);

        return enclosingS2;

    }

    /** Gather some inside points.
     * @return list of points known to be strictly in all inside convex cells
     */
    private List<Vector3D> getInsidePoints() {
        final PropertiesComputer pc = new PropertiesComputer(getPrecision());
        getTree(true).visit(pc);
        return pc.getConvexCellsInsidePoints();
    }

    /** Gather some outside points.
     * @return list of points known to be strictly in all outside convex cells
     */
    private List<Vector3D> getOutsidePoints() {
        final SphericalPolygonsSet complement =
                (SphericalPolygonsSet) new RegionFactory_Old<S2Point>().getComplement(this);
        final PropertiesComputer pc = new PropertiesComputer(getPrecision());
        complement.getTree(true).visit(pc);
        return pc.getConvexCellsInsidePoints();
    }

}
