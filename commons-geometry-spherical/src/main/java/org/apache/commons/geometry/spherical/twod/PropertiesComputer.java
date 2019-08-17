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
import java.util.List;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.internal.GeometryInternalError;
import org.apache.commons.geometry.core.partitioning.BSPTree_Old;
import org.apache.commons.geometry.core.partitioning.BSPTreeVisitor_Old;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

/** Visitor computing geometrical properties.
 */
class PropertiesComputer implements BSPTreeVisitor_Old<S2Point> {

    /** Precision context used to determine floating point equality. */
    private final DoublePrecisionContext precision;

    /** Summed area. */
    private double summedArea;

    /** Summed barycenter. */
    private Vector3D summedBarycenter;

    /** List of points strictly inside convex cells. */
    private final List<Vector3D> convexCellsInsidePoints;

    /** Simple constructor.
     * @param precision precision context used to compare floating point values
     */
    PropertiesComputer(final DoublePrecisionContext precision) {
        this.precision              = precision;
        this.summedArea             = 0;
        this.summedBarycenter       = Vector3D.ZERO;
        this.convexCellsInsidePoints = new ArrayList<>();
    }

    /** {@inheritDoc} */
    @Override
    public Order visitOrder(final BSPTree_Old<S2Point> node) {
        return Order.MINUS_SUB_PLUS;
    }

    /** {@inheritDoc} */
    @Override
    public void visitInternalNode(final BSPTree_Old<S2Point> node) {
        // nothing to do here
    }

    /** {@inheritDoc} */
    @Override
    public void visitLeafNode(final BSPTree_Old<S2Point> node) {
        if ((Boolean) node.getAttribute()) {

            // transform this inside leaf cell into a simple convex polygon
            final SphericalPolygonsSet convex =
                    new SphericalPolygonsSet(node.pruneAroundConvexCell(Boolean.TRUE,
                                                                        Boolean.FALSE,
                                                                        null),
                            precision);

            // extract the start of the single loop boundary of the convex cell
            final List<Vertex> boundary = convex.getBoundaryLoops();
            if (boundary.size() != 1) {
                // this should never happen
                throw new GeometryInternalError();
            }

            // compute the geometrical properties of the convex cell
            final double area  = convexCellArea(boundary.get(0));
            final Vector3D barycenter = convexCellBarycenter(boundary.get(0));
            convexCellsInsidePoints.add(barycenter);

            // add the cell contribution to the global properties
            summedArea      += area;
            summedBarycenter = Vector3D.linearCombination(1, summedBarycenter, area, barycenter);

        }
    }

    /** Compute convex cell area.
     * @param start start vertex of the convex cell boundary
     * @return area
     */
    private double convexCellArea(final Vertex start) {

        int n = 0;
        double sum = 0;

        // loop around the cell
        for (Edge e = start.getOutgoing(); n == 0 || e.getStart() != start; e = e.getEnd().getOutgoing()) {

            // find path interior angle at vertex
            final Vector3D previousPole = e.getCircle().getPole();
            final Vector3D nextPole     = e.getEnd().getOutgoing().getCircle().getPole();
            final Vector3D point        = e.getEnd().getLocation().getVector();
            double alpha = Math.atan2(nextPole.dot(point.cross(previousPole)),
                                          - nextPole.dot(previousPole));
            if (alpha < 0) {
                alpha += Geometry.TWO_PI;
            }
            sum += alpha;
            n++;
        }

        // compute area using extended Girard theorem
        // see Spherical Trigonometry: For the Use of Colleges and Schools by I. Todhunter
        // article 99 in chapter VIII Area Of a Spherical Triangle. Spherical Excess.
        // book available from project Gutenberg at http://www.gutenberg.org/ebooks/19770
        return sum - (n - 2) * Math.PI;

    }

    /** Compute convex cell barycenter.
     * @param start start vertex of the convex cell boundary
     * @return barycenter
     */
    private Vector3D convexCellBarycenter(final Vertex start) {

        int n = 0;
        Vector3D sumB = Vector3D.ZERO;

        // loop around the cell
        for (Edge e = start.getOutgoing(); n == 0 || e.getStart() != start; e = e.getEnd().getOutgoing()) {
            sumB = Vector3D.linearCombination(1, sumB, e.getLength(), e.getCircle().getPole());
            n++;
        }

        return sumB.normalize();

    }

    /** Get the area.
     * @return area
     */
    public double getArea() {
        return summedArea;
    }

    /** Get the barycenter.
     * @return barycenter
     */
    public S2Point getBarycenter() {
        if (summedBarycenter.normSq() == 0) {
            return S2Point.NaN;
        } else {
            return S2Point.ofVector(summedBarycenter);
        }
    }

    /** Get the points strictly inside convex cells.
     * @return points strictly inside convex cells
     */
    public List<Vector3D> getConvexCellsInsidePoints() {
        return convexCellsInsidePoints;
    }

}
