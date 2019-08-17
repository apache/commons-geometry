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
package org.apache.commons.geometry.euclidean.twod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.geometry.core.partitioning.AbstractRegion_Old;
import org.apache.commons.geometry.core.partitioning.AbstractSubHyperplane_Old;
import org.apache.commons.geometry.core.partitioning.BSPTree_Old;
import org.apache.commons.geometry.core.partitioning.BSPTreeVisitor_Old;
import org.apache.commons.geometry.core.partitioning.BoundaryAttribute_Old;
import org.apache.commons.geometry.core.partitioning.Hyperplane_Old;
import org.apache.commons.geometry.core.partitioning.Side_Old;
import org.apache.commons.geometry.core.partitioning.SubHyperplane_Old;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.Interval_Old;
import org.apache.commons.geometry.euclidean.oned.IntervalsSet;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.numbers.core.Precision;

/** This class represents a 2D region: a set of polygons.
 */
public class PolygonsSet extends AbstractRegion_Old<Vector2D, Vector1D> {

    /** Vertices organized as boundary loops. */
    private Vector2D[][] vertices;

    /** Build a polygons set representing the whole plane.
     * @param precision precision context used to compare floating point values
     */
    public PolygonsSet(final DoublePrecisionContext precision) {
        super(precision);
    }

    /** Build a polygons set from a BSP tree.
     * <p>The leaf nodes of the BSP tree <em>must</em> have a
     * {@code Boolean} attribute representing the inside status of
     * the corresponding cell (true for inside cells, false for outside
     * cells). In order to avoid building too many small objects, it is
     * recommended to use the predefined constants
     * {@code Boolean.TRUE} and {@code Boolean.FALSE}</p>
     * <p>
     * This constructor is aimed at expert use, as building the tree may
     * be a difficult task. It is not intended for general use and for
     * performances reasons does not check thoroughly its input, as this would
     * require walking the full tree each time. Failing to provide a tree with
     * the proper attributes, <em>will</em> therefore generate problems like
     * {@link NullPointerException} or {@link ClassCastException} only later on.
     * This limitation is known and explains why this constructor is for expert
     * use only. The caller does have the responsibility to provided correct arguments.
     * </p>
     * @param tree inside/outside BSP tree representing the region
     * @param precision precision context used to compare floating point values
     */
    public PolygonsSet(final BSPTree_Old<Vector2D> tree, final DoublePrecisionContext precision) {
        super(tree, precision);
    }

    /** Build a polygons set from a Boundary REPresentation (B-rep).
     * <p>The boundary is provided as a collection of {@link
     * SubHyperplane_Old sub-hyperplanes}. Each sub-hyperplane has the
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
     * org.apache.commons.geometry.core.partitioning.Region_Old#checkPoint(org.apache.commons.geometry.core.Point)
     * checkPoint} method will not be meaningful anymore.</p>
     * <p>If the boundary is empty, the region will represent the whole
     * space.</p>
     * @param boundary collection of boundary elements, as a
     * collection of {@link SubHyperplane_Old SubHyperplane} objects
     * @param precision precision context used to compare floating point values
     */
    public PolygonsSet(final Collection<SubHyperplane_Old<Vector2D>> boundary, final DoublePrecisionContext precision) {
        super(boundary, precision);
    }

    /** Build a parallellepipedic box.
     * @param xMin low bound along the x direction
     * @param xMax high bound along the x direction
     * @param yMin low bound along the y direction
     * @param yMax high bound along the y direction
     * @param precision precision context used to compare floating point values
     */
    public PolygonsSet(final double xMin, final double xMax,
                       final double yMin, final double yMax,
                       final DoublePrecisionContext precision) {
        super(boxBoundary(xMin, xMax, yMin, yMax, precision), precision);
    }

    /** Build a polygon from a simple list of vertices.
     * <p>The boundary is provided as a list of points considering to
     * represent the vertices of a simple loop. The interior part of the
     * region is on the left side of this path and the exterior is on its
     * right side.</p>
     * <p>This constructor does not handle polygons with a boundary
     * forming several disconnected paths (such as polygons with holes).</p>
     * <p>For cases where this simple constructor applies, it is expected to
     * be numerically more robust than the {@link #PolygonsSet(Collection, DoublePrecisionContext) general
     * constructor} using {@link SubHyperplane_Old subhyperplanes}.</p>
     * <p>If the list is empty, the region will represent the whole
     * space.</p>
     * <p>
     * Polygons with thin pikes or dents are inherently difficult to handle because
     * they involve lines with almost opposite directions at some vertices. Polygons
     * whose vertices come from some physical measurement with noise are also
     * difficult because an edge that should be straight may be broken in lots of
     * different pieces with almost equal directions. In both cases, computing the
     * lines intersections is not numerically robust due to the almost 0 or almost
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
    public PolygonsSet(final DoublePrecisionContext precision, final Vector2D ... vertices) {
        super(verticesToTree(precision, vertices), precision);
    }

    /** Create a list of hyperplanes representing the boundary of a box.
     * @param xMin low bound along the x direction
     * @param xMax high bound along the x direction
     * @param yMin low bound along the y direction
     * @param yMax high bound along the y direction
     * @param precision precision context used to compare floating point values
     * @return boundary of the box
     */
    private static Line[] boxBoundary(final double xMin, final double xMax,
                                      final double yMin, final double yMax,
                                      final DoublePrecisionContext precision) {
        if (precision.eq(xMin, xMax) || precision.eq(yMin, yMax)) {
            // too thin box, build an empty polygons set
            return null;
        }
        final Vector2D minMin = Vector2D.of(xMin, yMin);
        final Vector2D minMax = Vector2D.of(xMin, yMax);
        final Vector2D maxMin = Vector2D.of(xMax, yMin);
        final Vector2D maxMax = Vector2D.of(xMax, yMax);
        return new Line[] {
            Line.fromPoints(minMin, maxMin, precision),
            Line.fromPoints(maxMin, maxMax, precision),
            Line.fromPoints(maxMax, minMax, precision),
            Line.fromPoints(minMax, minMin, precision)
        };
    }

    /** Build the BSP tree of a polygons set from a simple list of vertices.
     * <p>The boundary is provided as a list of points considering to
     * represent the vertices of a simple loop. The interior part of the
     * region is on the left side of this path and the exterior is on its
     * right side.</p>
     * <p>This constructor does not handle polygons with a boundary
     * forming several disconnected paths (such as polygons with holes).</p>
     * <p>For cases where this simple constructor applies, it is expected to
     * be numerically more robust than the {@link #PolygonsSet(Collection,double) general
     * constructor} using {@link SubHyperplane_Old subhyperplanes}.</p>
     * @param precision precision context used to compare floating point values
     * @param vertices vertices of the simple loop boundary
     * @return the BSP tree of the input vertices
     */
    private static BSPTree_Old<Vector2D> verticesToTree(final DoublePrecisionContext precision,
                                                       final Vector2D ... vertices) {

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
        for (int i = 0; i < n; ++i) {

            // get the endpoints of the edge
            final Vertex start = vArray[i];
            final Vertex end   = vArray[(i + 1) % n];

            // get the line supporting the edge, taking care not to recreate it
            // if it was already created earlier due to another edge being aligned
            // with the current one
            Line line = start.sharedLineWith(end);
            if (line == null) {
                line = Line.fromPoints(start.getLocation(), end.getLocation(), precision);
            }

            // create the edge and store it
            edges.add(new Edge(start, end, line));

            // check if another vertex also happens to be on this line
            for (final Vertex vertex : vArray) {
                if (vertex != start && vertex != end &&
                    precision.eqZero(line.getOffset(vertex.getLocation()))) {
                    vertex.bindWith(line);
                }
            }

        }

        // build the tree top-down
        final BSPTree_Old<Vector2D> tree = new BSPTree_Old<>();
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
                                    final BSPTree_Old<Vector2D> node,
                                    final List<Edge> edges) {

        // find an edge with an hyperplane that can be inserted in the node
        int index = 0;
        Edge inserted =null;
        while (inserted == null && index < edges.size()) {
            inserted = edges.get(index++);
            if (inserted.getNode() == null) {
                if (node.insertCut(inserted.getLine())) {
                    inserted.setNode(node);
                } else {
                    inserted = null;
                }
            } else {
                inserted = null;
            }
        }

        if (inserted == null) {
            // no suitable edge was found, the node remains a leaf node
            // we need to set its inside/outside boolean indicator
            final BSPTree_Old<Vector2D> parent = node.getParent();
            if (parent == null || node == parent.getMinus()) {
                node.setAttribute(Boolean.TRUE);
            } else {
                node.setAttribute(Boolean.FALSE);
            }
            return;
        }

        // we have split the node by inserting an edge as a cut sub-hyperplane
        // distribute the remaining edges in the two sub-trees
        final List<Edge> plusList  = new ArrayList<>();
        final List<Edge> minusList = new ArrayList<>();
        for (final Edge edge : edges) {
            if (edge != inserted) {
                final double startOffset = inserted.getLine().getOffset(edge.getStart().getLocation());
                final double endOffset   = inserted.getLine().getOffset(edge.getEnd().getLocation());
                Side_Old startSide = precision.eqZero(Math.abs(startOffset)) ?
                                 Side_Old.HYPER : ((startOffset < 0) ? Side_Old.MINUS : Side_Old.PLUS);
                Side_Old endSide   = precision.eqZero(endOffset) ?
                                 Side_Old.HYPER : ((endOffset < 0) ? Side_Old.MINUS : Side_Old.PLUS);
                switch (startSide) {
                    case PLUS:
                        if (endSide == Side_Old.MINUS) {
                            // we need to insert a split point on the hyperplane
                            final Vertex splitPoint = edge.split(inserted.getLine());
                            minusList.add(splitPoint.getOutgoing());
                            plusList.add(splitPoint.getIncoming());
                        } else {
                            plusList.add(edge);
                        }
                        break;
                    case MINUS:
                        if (endSide == Side_Old.PLUS) {
                            // we need to insert a split point on the hyperplane
                            final Vertex splitPoint = edge.split(inserted.getLine());
                            minusList.add(splitPoint.getIncoming());
                            plusList.add(splitPoint.getOutgoing());
                        } else {
                            minusList.add(edge);
                        }
                        break;
                    default:
                        if (endSide == Side_Old.PLUS) {
                            plusList.add(edge);
                        } else if (endSide == Side_Old.MINUS) {
                            minusList.add(edge);
                        }
                        break;
                }
            }
        }

        // recurse through lower levels
        if (!plusList.isEmpty()) {
            insertEdges(precision, node.getPlus(),  plusList);
        } else {
            node.getPlus().setAttribute(Boolean.FALSE);
        }
        if (!minusList.isEmpty()) {
            insertEdges(precision, node.getMinus(), minusList);
        } else {
            node.getMinus().setAttribute(Boolean.TRUE);
        }

    }

    /** Internal class for holding vertices while they are processed to build a BSP tree. */
    private static class Vertex {

        /** Vertex location. */
        private final Vector2D location;

        /** Incoming edge. */
        private Edge incoming;

        /** Outgoing edge. */
        private Edge outgoing;

        /** Lines bound with this vertex. */
        private final List<Line> lines;

        /** Build a non-processed vertex not owned by any node yet.
         * @param location vertex location
         */
        Vertex(final Vector2D location) {
            this.location = location;
            this.incoming = null;
            this.outgoing = null;
            this.lines    = new ArrayList<>();
        }

        /** Get Vertex location.
         * @return vertex location
         */
        public Vector2D getLocation() {
            return location;
        }

        /** Bind a line considered to contain this vertex.
         * @param line line to bind with this vertex
         */
        public void bindWith(final Line line) {
            lines.add(line);
        }

        /** Get the common line bound with both the instance and another vertex, if any.
         * <p>
         * When two vertices are both bound to the same line, this means they are
         * already handled by node associated with this line, so there is no need
         * to create a cut hyperplane for them.
         * </p>
         * @param vertex other vertex to check instance against
         * @return line bound with both the instance and another vertex, or null if the
         * two vertices do not share a line yet
         */
        public Line sharedLineWith(final Vertex vertex) {
            for (final Line line1 : lines) {
                for (final Line line2 : vertex.lines) {
                    if (line1 == line2) {
                        return line1;
                    }
                }
            }
            return null;
        }

        /** Set incoming edge.
         * <p>
         * The line supporting the incoming edge is automatically bound
         * with the instance.
         * </p>
         * @param incoming incoming edge
         */
        public void setIncoming(final Edge incoming) {
            this.incoming = incoming;
            bindWith(incoming.getLine());
        }

        /** Get incoming edge.
         * @return incoming edge
         */
        public Edge getIncoming() {
            return incoming;
        }

        /** Set outgoing edge.
         * <p>
         * The line supporting the outgoing edge is automatically bound
         * with the instance.
         * </p>
         * @param outgoing outgoing edge
         */
        public void setOutgoing(final Edge outgoing) {
            this.outgoing = outgoing;
            bindWith(outgoing.getLine());
        }

        /** Get outgoing edge.
         * @return outgoing edge
         */
        public Edge getOutgoing() {
            return outgoing;
        }

    }

    /** Internal class for holding edges while they are processed to build a BSP tree. */
    private static class Edge {

        /** Start vertex. */
        private final Vertex start;

        /** End vertex. */
        private final Vertex end;

        /** Line supporting the edge. */
        private final Line line;

        /** Node whose cut hyperplane contains this edge. */
        private BSPTree_Old<Vector2D> node;

        /** Build an edge not contained in any node yet.
         * @param start start vertex
         * @param end end vertex
         * @param line line supporting the edge
         */
        Edge(final Vertex start, final Vertex end, final Line line) {

            this.start = start;
            this.end   = end;
            this.line  = line;
            this.node  = null;

            // connect the vertices back to the edge
            start.setOutgoing(this);
            end.setIncoming(this);

        }

        /** Get start vertex.
         * @return start vertex
         */
        public Vertex getStart() {
            return start;
        }

        /** Get end vertex.
         * @return end vertex
         */
        public Vertex getEnd() {
            return end;
        }

        /** Get the line supporting this edge.
         * @return line supporting this edge
         */
        public Line getLine() {
            return line;
        }

        /** Set the node whose cut hyperplane contains this edge.
         * @param node node whose cut hyperplane contains this edge
         */
        public void setNode(final BSPTree_Old<Vector2D> node) {
            this.node = node;
        }

        /** Get the node whose cut hyperplane contains this edge.
         * @return node whose cut hyperplane contains this edge
         * (null if edge has not yet been inserted into the BSP tree)
         */
        public BSPTree_Old<Vector2D> getNode() {
            return node;
        }

        /** Split the edge.
         * <p>
         * Once split, this edge is not referenced anymore by the vertices,
         * it is replaced by the two half-edges and an intermediate splitting
         * vertex is introduced to connect these two halves.
         * </p>
         * @param splitLine line splitting the edge in two halves
         * @return split vertex (its incoming and outgoing edges are the two halves)
         */
        public Vertex split(final Line splitLine) {
            final Vertex splitVertex = new Vertex(line.intersection(splitLine));
            splitVertex.bindWith(splitLine);
            final Edge startHalf = new Edge(start, splitVertex, line);
            final Edge endHalf   = new Edge(splitVertex, end, line);
            startHalf.node = node;
            endHalf.node   = node;
            return splitVertex;
        }

    }

    /** {@inheritDoc} */
    @Override
    public PolygonsSet buildNew(final BSPTree_Old<Vector2D> tree) {
        return new PolygonsSet(tree, getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    protected void computeGeometricalProperties() {

        final Vector2D[][] v = getVertices();

        if (v.length == 0) {
            final BSPTree_Old<Vector2D> tree = getTree(false);
            if (tree.getCut() == null && (Boolean) tree.getAttribute()) {
                // the instance covers the whole space
                setSize(Double.POSITIVE_INFINITY);
                setBarycenter(Vector2D.NaN);
            } else {
                setSize(0);
                setBarycenter(Vector2D.NaN);
            }
        } else if (v[0][0] == null) {
            // there is at least one open-loop: the polygon is infinite
            setSize(Double.POSITIVE_INFINITY);
            setBarycenter(Vector2D.NaN);
        } else {
            // all loops are closed, we compute some integrals around the shape

            double sum  = 0;
            double sumX = 0;
            double sumY = 0;

            for (Vector2D[] loop : v) {
                double x1 = loop[loop.length - 1].getX();
                double y1 = loop[loop.length - 1].getY();
                for (final Vector2D point : loop) {
                    final double x0 = x1;
                    final double y0 = y1;
                    x1 = point.getX();
                    y1 = point.getY();
                    final double factor = x0 * y1 - y0 * x1;
                    sum  += factor;
                    sumX += factor * (x0 + x1);
                    sumY += factor * (y0 + y1);
                }
            }

            if (sum < 0) {
                // the polygon as a finite outside surrounded by an infinite inside
                setSize(Double.POSITIVE_INFINITY);
                setBarycenter(Vector2D.NaN);
            } else {
                setSize(sum / 2);
                setBarycenter(Vector2D.of(sumX / (3 * sum), sumY / (3 * sum)));
            }

        }

    }

    /** Get the vertices of the polygon.
     * <p>The polygon boundary can be represented as an array of loops,
     * each loop being itself an array of vertices.</p>
     * <p>In order to identify open loops which start and end by
     * infinite edges, the open loops arrays start with a null point. In
     * this case, the first non null point and the last point of the
     * array do not represent real vertices, they are dummy points
     * intended only to get the direction of the first and last edge. An
     * open loop consisting of a single infinite line will therefore be
     * represented by a three elements array with one null point
     * followed by two dummy points. The open loops are always the first
     * ones in the loops array.</p>
     * <p>If the polygon has no boundary at all, a zero length loop
     * array will be returned.</p>
     * <p>All line segments in the various loops have the inside of the
     * region on their left side and the outside on their right side
     * when moving in the underlying line direction. This means that
     * closed loops surrounding finite areas obey the direct
     * trigonometric orientation.</p>
     * @return vertices of the polygon, organized as oriented boundary
     * loops with the open loops first (the returned value is guaranteed
     * to be non-null)
     */
    public Vector2D[][] getVertices() {
        if (vertices == null) {
            if (getTree(false).getCut() == null) {
                vertices = new Vector2D[0][];
            } else {

                // build the unconnected segments
                final SegmentsBuilder visitor = new SegmentsBuilder(getPrecision());
                getTree(true).visit(visitor);
                final List<ConnectableSegment> segments = visitor.getSegments();

                // connect all segments, using topological criteria first
                // and using Euclidean distance only as a last resort
                int pending = segments.size();
                pending -= naturalFollowerConnections(segments);
                if (pending > 0) {
                    pending -= splitEdgeConnections(segments);
                }
                if (pending > 0) {
                    pending -= closeVerticesConnections(segments);
                }

                // create the segment loops
                final ArrayList<List<Segment>> loops = new ArrayList<>();
                for (ConnectableSegment s = getUnprocessed(segments); s != null; s = getUnprocessed(segments)) {
                    final List<Segment> loop = followLoop(s);
                    if (loop != null) {
                        // an open loop is one that has fewer than two segments or has a null
                        // start point; the case where we have two segments in a closed loop
                        // (ie, an infinitely thin, degenerate loop) will result in null being
                        // returned from the followLoops method
                        if (loop.size() < 2 || loop.get(0).getStart() == null) {
                            // this is an open loop, we put it on the front
                            loops.add(0, loop);
                        } else {
                            // this is a closed loop, we put it on the back
                            loops.add(loop);
                        }
                    }
                }

                // transform the loops in an array of arrays of points
                vertices = new Vector2D[loops.size()][];
                int i = 0;

                for (final List<Segment> loop : loops) {
                    if (loop.size() < 2 ||
                        (loop.size() == 2 && loop.get(0).getStart() == null && loop.get(1).getEnd() == null)) {
                        // single infinite line
                        final Line line = loop.get(0).getLine();
                        vertices[i++] = new Vector2D[] {
                            null,
                            line.toSpace(Vector1D.of(-Float.MAX_VALUE)),
                            line.toSpace(Vector1D.of(+Float.MAX_VALUE))
                        };
                    } else if (loop.get(0).getStart() == null) {
                        // open loop with at least one real point
                        final Vector2D[] array = new Vector2D[loop.size() + 2];
                        int j = 0;
                        for (Segment segment : loop) {

                            if (j == 0) {
                                // null point and first dummy point
                                double x = segment.getLine().toSubSpace(segment.getEnd()).getX();
                                x -= Math.max(1.0, Math.abs(x / 2));
                                array[j++] = null;
                                array[j++] = segment.getLine().toSpace(Vector1D.of(x));
                            }

                            if (j < (array.length - 1)) {
                                // current point
                                array[j++] = segment.getEnd();
                            } else if (j == (array.length - 1)) {
                                // last dummy point
                                double x = segment.getLine().toSubSpace(segment.getStart()).getX();
                                x += Math.max(1.0, Math.abs(x / 2));
                                array[j++] = segment.getLine().toSpace(Vector1D.of(x));
                            }

                        }
                        vertices[i++] = array;
                    } else {
                        final Vector2D[] array = new Vector2D[loop.size()];
                        int j = 0;
                        for (Segment segment : loop) {
                            array[j++] = segment.getStart();
                        }
                        vertices[i++] = array;
                    }
                }

            }
        }

        return vertices.clone();

    }

    /** Connect the segments using only natural follower information.
     * @param segments segments complete segments list
     * @return number of connections performed
     */
    private int naturalFollowerConnections(final List<ConnectableSegment> segments) {
        int connected = 0;
        for (final ConnectableSegment segment : segments) {
            if (segment.getNext() == null) {
                final BSPTree_Old<Vector2D> node = segment.getNode();
                final BSPTree_Old<Vector2D> end  = segment.getEndNode();
                for (final ConnectableSegment candidateNext : segments) {
                    if (candidateNext.getPrevious()  == null &&
                        candidateNext.getNode()      == end &&
                        candidateNext.getStartNode() == node) {
                        // connect the two segments
                        segment.setNext(candidateNext);
                        candidateNext.setPrevious(segment);
                        ++connected;
                        break;
                    }
                }
            }
        }
        return connected;
    }

    /** Connect the segments resulting from a line splitting a straight edge.
     * @param segments segments complete segments list
     * @return number of connections performed
     */
    private int splitEdgeConnections(final List<ConnectableSegment> segments) {
        int connected = 0;
        for (final ConnectableSegment segment : segments) {
            if (segment.getNext() == null && segment.getEndNode() != null) {
                final Hyperplane_Old<Vector2D> hyperplane = segment.getNode().getCut().getHyperplane();
                final BSPTree_Old<Vector2D> end  = segment.getEndNode();
                for (final ConnectableSegment candidateNext : segments) {
                    if (candidateNext.getPrevious()                      == null &&
                        candidateNext.getNode().getCut().getHyperplane().equals(hyperplane) &&
                        candidateNext.getStartNode()                     == end) {
                        // connect the two segments
                        segment.setNext(candidateNext);
                        candidateNext.setPrevious(segment);
                        ++connected;
                        break;
                    }
                }
            }
        }
        return connected;
    }

    /** Connect the segments using Euclidean distance.
     * <p>
     * This connection heuristic should be used last, as it relies
     * only on a fuzzy distance criterion.
     * </p>
     * @param segments segments complete segments list
     * @return number of connections performed
     */
    private int closeVerticesConnections(final List<ConnectableSegment> segments) {
        int connected = 0;
        for (final ConnectableSegment segment : segments) {
            if (segment.getNext() == null && segment.getEnd() != null) {
                final Vector2D end = segment.getEnd();
                ConnectableSegment selectedNext = null;
                double min = Double.POSITIVE_INFINITY;
                for (final ConnectableSegment candidateNext : segments) {
                    if (candidateNext.getPrevious() == null && candidateNext.getStart() != null) {
                        final double distance = end.distance(candidateNext.getStart());
                        if (distance < min) {
                            selectedNext = candidateNext;
                            min          = distance;
                        }
                    }
                }
                if (getPrecision().eqZero(min)) {
                    // connect the two segments
                    segment.setNext(selectedNext);
                    selectedNext.setPrevious(segment);
                    ++connected;
                }
            }
        }
        return connected;
    }

    /** Get first unprocessed segment from a list.
     * @param segments segments list
     * @return first segment that has not been processed yet
     * or null if all segments have been processed
     */
    private ConnectableSegment getUnprocessed(final List<ConnectableSegment> segments) {
        for (final ConnectableSegment segment : segments) {
            if (!segment.isProcessed()) {
                return segment;
            }
        }
        return null;
    }

    /** Build the loop containing a segment.
     * <p>
     * The segment put in the loop will be marked as processed.
     * </p>
     * @param defining segment used to define the loop
     * @return loop containing the segment (may be null if the loop is a
     * degenerated infinitely thin 2 points loop
     */
    private List<Segment> followLoop(final ConnectableSegment defining) {

        final List<Segment> loop = new ArrayList<>();
        loop.add(defining);
        defining.setProcessed(true);

        // add segments in connection order
        ConnectableSegment next = defining.getNext();
        while (next != defining && next != null) {
            loop.add(next);
            next.setProcessed(true);
            next = next.getNext();
        }

        if (next == null) {
            // the loop is open and we have found its end,
            // we need to find its start too
            ConnectableSegment previous = defining.getPrevious();
            while (previous != null) {
                loop.add(0, previous);
                previous.setProcessed(true);
                previous = previous.getPrevious();
            }
        }

        // filter out spurious vertices
        filterSpuriousVertices(loop);

        if (loop.size() == 2 && loop.get(0).getStart() != null) {
            // this is a degenerated infinitely thin closed loop, we simply ignore it
            return null;
        } else {
            return loop;
        }

    }

    /** Filter out spurious vertices on straight lines (at machine precision).
     * @param loop segments loop to filter (will be modified in-place)
     */
    private void filterSpuriousVertices(final List<Segment> loop) {
        // we need at least 2 segments in order for one of the contained vertices
        // to be unnecessary
        if (loop.size() > 1) {
            // Go through the list and compare each segment with the next
            // one in line. We can remove the shared vertex if the segments
            // are not infinite and they lie on the same line.
            for (int i = 0; i < loop.size(); ++i) {
                final Segment previous = loop.get(i);
                int j = (i + 1) % loop.size();
                final Segment next = loop.get(j);
                if (next != null &&
                    previous.getStart() != null && next.getEnd() != null &&
                    Precision.equals(previous.getLine().getAngle(), next.getLine().getAngle(), Precision.EPSILON)) {
                    // the vertex between the two edges is a spurious one
                    // replace the two segments by a single one
                    loop.set(j, new Segment(previous.getStart(), next.getEnd(), previous.getLine()));
                    loop.remove(i--);
                }
            }
        }
    }

    /** Private extension of Segment allowing connection. */
    private static class ConnectableSegment extends Segment {

        /** Node containing segment. */
        private final BSPTree_Old<Vector2D> node;

        /** Node whose intersection with current node defines start point. */
        private final BSPTree_Old<Vector2D> startNode;

        /** Node whose intersection with current node defines end point. */
        private final BSPTree_Old<Vector2D> endNode;

        /** Previous segment. */
        private ConnectableSegment previous;

        /** Next segment. */
        private ConnectableSegment next;

        /** Indicator for completely processed segments. */
        private boolean processed;

        /** Build a segment.
         * @param start start point of the segment
         * @param end end point of the segment
         * @param line line containing the segment
         * @param node node containing the segment
         * @param startNode node whose intersection with current node defines start point
         * @param endNode node whose intersection with current node defines end point
         */
        ConnectableSegment(final Vector2D start, final Vector2D end, final Line line,
                           final BSPTree_Old<Vector2D> node,
                           final BSPTree_Old<Vector2D> startNode,
                           final BSPTree_Old<Vector2D> endNode) {
            super(start, end, line);
            this.node      = node;
            this.startNode = startNode;
            this.endNode   = endNode;
            this.previous  = null;
            this.next      = null;
            this.processed = false;
        }

        /** Get the node containing segment.
         * @return node containing segment
         */
        public BSPTree_Old<Vector2D> getNode() {
            return node;
        }

        /** Get the node whose intersection with current node defines start point.
         * @return node whose intersection with current node defines start point
         */
        public BSPTree_Old<Vector2D> getStartNode() {
            return startNode;
        }

        /** Get the node whose intersection with current node defines end point.
         * @return node whose intersection with current node defines end point
         */
        public BSPTree_Old<Vector2D> getEndNode() {
            return endNode;
        }

        /** Get the previous segment.
         * @return previous segment
         */
        public ConnectableSegment getPrevious() {
            return previous;
        }

        /** Set the previous segment.
         * @param previous previous segment
         */
        public void setPrevious(final ConnectableSegment previous) {
            this.previous = previous;
        }

        /** Get the next segment.
         * @return next segment
         */
        public ConnectableSegment getNext() {
            return next;
        }

        /** Set the next segment.
         * @param next previous segment
         */
        public void setNext(final ConnectableSegment next) {
            this.next = next;
        }

        /** Set the processed flag.
         * @param processed processed flag to set
         */
        public void setProcessed(final boolean processed) {
            this.processed = processed;
        }

        /** Check if the segment has been processed.
         * @return true if the segment has been processed
         */
        public boolean isProcessed() {
            return processed;
        }

    }

    /** Visitor building segments. */
    private static class SegmentsBuilder implements BSPTreeVisitor_Old<Vector2D> {

        /** Object used to determine floating point equality */
        private final DoublePrecisionContext precision;

        /** Built segments. */
        private final List<ConnectableSegment> segments;

        /** Simple constructor.
         * @param precision precision context used to compare floating point values
         */
        SegmentsBuilder(final DoublePrecisionContext precision) {
            this.precision = precision;
            this.segments  = new ArrayList<>();
        }

        /** {@inheritDoc} */
        @Override
        public Order visitOrder(final BSPTree_Old<Vector2D> node) {
            return Order.MINUS_SUB_PLUS;
        }

        /** {@inheritDoc} */
        @Override
        public void visitInternalNode(final BSPTree_Old<Vector2D> node) {
            @SuppressWarnings("unchecked")
            final BoundaryAttribute_Old<Vector2D> attribute = (BoundaryAttribute_Old<Vector2D>) node.getAttribute();
            final Iterable<BSPTree_Old<Vector2D>> splitters = attribute.getSplitters();
            if (attribute.getPlusOutside() != null) {
                addContribution(attribute.getPlusOutside(), node, splitters, false);
            }
            if (attribute.getPlusInside() != null) {
                addContribution(attribute.getPlusInside(), node, splitters, true);
            }
        }

        /** {@inheritDoc} */
        @Override
        public void visitLeafNode(final BSPTree_Old<Vector2D> node) {
        }

        /** Add the contribution of a boundary facet.
         * @param sub boundary facet
         * @param node node containing segment
         * @param splitters splitters for the boundary facet
         * @param reversed if true, the facet has the inside on its plus side
         */
        private void addContribution(final SubHyperplane_Old<Vector2D> sub,
                                     final BSPTree_Old<Vector2D> node,
                                     final Iterable<BSPTree_Old<Vector2D>> splitters,
                                     final boolean reversed) {
            @SuppressWarnings("unchecked")
            final AbstractSubHyperplane_Old<Vector2D, Vector1D> absSub =
                (AbstractSubHyperplane_Old<Vector2D, Vector1D>) sub;
            final Line line      = (Line) sub.getHyperplane();
            final List<Interval_Old> intervals = ((IntervalsSet) absSub.getRemainingRegion()).asList();
            for (final Interval_Old i : intervals) {

                // find the 2D points
                final Vector2D startV = Double.isInfinite(i.getInf()) ?
                                        null : line.toSpace(Vector1D.of(i.getInf()));
                final Vector2D endV   = Double.isInfinite(i.getSup()) ?
                                        null : line.toSpace(Vector1D.of(i.getSup()));

                // recover the connectivity information
                final BSPTree_Old<Vector2D> startN = selectClosest(startV, splitters);
                final BSPTree_Old<Vector2D> endN   = selectClosest(endV, splitters);

                if (reversed) {
                    segments.add(new ConnectableSegment(endV, startV, line.reverse(),
                                                        node, endN, startN));
                } else {
                    segments.add(new ConnectableSegment(startV, endV, line,
                                                        node, startN, endN));
                }

            }
        }

        /** Select the node whose cut sub-hyperplane is closest to specified point.
         * @param point reference point
         * @param candidates candidate nodes
         * @return node closest to point, or null if point is null or no node is closer than tolerance
         */
        private BSPTree_Old<Vector2D> selectClosest(final Vector2D point, final Iterable<BSPTree_Old<Vector2D>> candidates) {
            if (point != null) {
                BSPTree_Old<Vector2D> selected = null;
                double min = Double.POSITIVE_INFINITY;

                for (final BSPTree_Old<Vector2D> node : candidates) {
                    final double distance = Math.abs(node.getCut().getHyperplane().getOffset(point));
                    if (distance < min) {
                        selected = node;
                        min      = distance;
                    }
                }

                if (precision.eqZero(min)) {
                    return selected;
                }
            }
            return null;
        }

        /** Get the segments.
         * @return built segments
         */
        public List<ConnectableSegment> getSegments() {
            return segments;
        }

    }

}
