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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.oned.Arc;
import org.apache.commons.geometry.spherical.oned.ArcsSet;
import org.apache.commons.geometry.spherical.oned.Point1S;
import org.apache.commons.geometry.spherical.partitioning.BSPTreeVisitor_Old;
import org.apache.commons.geometry.spherical.partitioning.BSPTree_Old;
import org.apache.commons.geometry.spherical.partitioning.BoundaryAttribute_Old;

/** Visitor building edges.
 */
class EdgesBuilder implements BSPTreeVisitor_Old<Point2S> {

    /** Root of the tree. */
    private final BSPTree_Old<Point2S> root;

    /** Precision context used to determine floating point equality. */
    private final DoublePrecisionContext precision;

    /** Built edges and their associated nodes. */
    private final Map<Edge, BSPTree_Old<Point2S>> edgeToNode;

    /** Reversed map. */
    private final Map<BSPTree_Old<Point2S>, List<Edge>> nodeToEdgesList;

    /** Simple constructor.
     * @param root tree root
     * @param precision precision context used to compare floating point values
     */
    EdgesBuilder(final BSPTree_Old<Point2S> root, final DoublePrecisionContext precision) {
        this.root            = root;
        this.precision       = precision;
        this.edgeToNode      = new IdentityHashMap<>();
        this.nodeToEdgesList = new IdentityHashMap<>();
    }

    /** {@inheritDoc} */
    @Override
    public Order visitOrder(final BSPTree_Old<Point2S> node) {
        return Order.MINUS_SUB_PLUS;
    }

    /** {@inheritDoc} */
    @Override
    public void visitInternalNode(final BSPTree_Old<Point2S> node) {
        nodeToEdgesList.put(node, new ArrayList<Edge>());
        @SuppressWarnings("unchecked")
        final BoundaryAttribute_Old<Point2S> attribute = (BoundaryAttribute_Old<Point2S>) node.getAttribute();
        if (attribute.getPlusOutside() != null) {
            addContribution((SubCircle) attribute.getPlusOutside(), false, node);
        }
        if (attribute.getPlusInside() != null) {
            addContribution((SubCircle) attribute.getPlusInside(), true, node);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void visitLeafNode(final BSPTree_Old<Point2S> node) {
    }

    /** Add the contribution of a boundary edge.
     * @param sub boundary facet
     * @param reversed if true, the facet has the inside on its plus side
     * @param node node to which the edge belongs
     */
    private void addContribution(final SubCircle sub, final boolean reversed,
                                 final BSPTree_Old<Point2S> node) {
        final Circle circle  = (Circle) sub.getHyperplane();
        final List<Arc> arcs = ((ArcsSet) sub.getRemainingRegion()).asList();
        for (final Arc a : arcs) {
            final Vertex start = new Vertex(circle.toSpace(Point1S.of(a.getInf())));
            final Vertex end   = new Vertex(circle.toSpace(Point1S.of(a.getSup())));
            start.bindWith(circle);
            end.bindWith(circle);
            final Edge edge;
            if (reversed) {
                edge = new Edge(end, start, a.getSize(), circle.getReverse());
            } else {
                edge = new Edge(start, end, a.getSize(), circle);
            }
            edgeToNode.put(edge, node);
            nodeToEdgesList.get(node).add(edge);
        }
    }

    /** Get the edge that should naturally follow another one.
     * @param previous edge to be continued
     * @return other edge, starting where the previous one ends (they
     * have not been connected yet)
     * @exception IllegalStateException if there is not a single other edge
     */
    private Edge getFollowingEdge(final Edge previous)
        throws IllegalStateException {

        // get the candidate nodes
        final Point2S point = previous.getEnd().getLocation();
        final List<BSPTree_Old<Point2S>> candidates = root.getCloseCuts(point, precision.getMaxZero());

        // the following edge we are looking for must start from one of the candidates nodes
        double closest = precision.getMaxZero();
        Edge following = null;
        for (final BSPTree_Old<Point2S> node : candidates) {
            for (final Edge edge : nodeToEdgesList.get(node)) {
                if (edge != previous && edge.getStart().getIncoming() == null) {
                    final Vector3D edgeStart = edge.getStart().getLocation().getVector();
                    final double gap         = point.getVector().angle(edgeStart);
                    if (gap <= closest) {
                        closest   = gap;
                        following = edge;
                    }
                }
            }
        }

        if (following == null) {
            final Vector3D previousStart = previous.getStart().getLocation().getVector();
            if (precision.eqZero(point.getVector().angle(previousStart))) {
                // the edge connects back to itself
                return previous;
            }

            // this should never happen
            throw new IllegalStateException("An outline boundary loop is open");

        }

        return following;

    }

    /** Get the boundary edges.
     * @return boundary edges
     * @exception IllegalStateException if there is not a single other edge
     */
    public List<Edge> getEdges() {

        // connect the edges
        for (final Edge previous : edgeToNode.keySet()) {
            previous.setNextEdge(getFollowingEdge(previous));
        }

        return new ArrayList<>(edgeToNode.keySet());

    }

}
