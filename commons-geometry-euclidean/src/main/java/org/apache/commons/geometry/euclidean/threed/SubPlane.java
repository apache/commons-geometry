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

import org.apache.commons.geometry.core.partitioning.AbstractSubHyperplane_Old;
import org.apache.commons.geometry.core.partitioning.BSPTree_Old;
import org.apache.commons.geometry.core.partitioning.Hyperplane_Old;
import org.apache.commons.geometry.core.partitioning.Region_Old;
import org.apache.commons.geometry.core.partitioning.SubHyperplane_Old;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.twod.PolygonsSet;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

/** This class represents a sub-hyperplane for {@link Plane}.
 */
public class SubPlane extends AbstractSubHyperplane_Old<Vector3D, Vector2D> {

    /** Simple constructor.
     * @param hyperplane underlying hyperplane
     * @param remainingRegion remaining region of the hyperplane
     */
    public SubPlane(final Hyperplane_Old<Vector3D> hyperplane,
                    final Region_Old<Vector2D> remainingRegion) {
        super(hyperplane, remainingRegion);
    }

    /** {@inheritDoc} */
    @Override
    protected AbstractSubHyperplane_Old<Vector3D, Vector2D> buildNew(final Hyperplane_Old<Vector3D> hyperplane,
                                                                       final Region_Old<Vector2D> remainingRegion) {
        return new SubPlane(hyperplane, remainingRegion);
    }

    /** Split the instance in two parts by an hyperplane.
     * @param hyperplane splitting hyperplane
     * @return an object containing both the part of the instance
     * on the plus side of the instance and the part of the
     * instance on the minus side of the instance
     */
    @Override
    public SplitSubHyperplane<Vector3D> split(Hyperplane_Old<Vector3D> hyperplane) {

        final Plane otherPlane = (Plane) hyperplane;
        final Plane thisPlane  = (Plane) getHyperplane();
        final Line  inter      = otherPlane.intersection(thisPlane);
        final DoublePrecisionContext precision = thisPlane.getPrecision();

        if (inter == null) {
            // the hyperplanes are parallel
            final double global = otherPlane.getOffset(thisPlane);
            final int comparison = precision.compare(global, 0.0);

            if (comparison < 0) {
                return new SplitSubHyperplane<>(null, this);
            } else if (comparison > 0) {
                return new SplitSubHyperplane<>(this, null);
            } else {
                return new SplitSubHyperplane<>(null, null);
            }
        }

        // the hyperplanes do intersect
        Vector2D p = thisPlane.toSubSpace(inter.toSpace(Vector1D.ZERO));
        Vector2D q = thisPlane.toSubSpace(inter.toSpace(Vector1D.ONE));
        Vector3D crossP = inter.getDirection().cross(thisPlane.getNormal());
        if (crossP.dot(otherPlane.getNormal()) < 0) {
            final Vector2D tmp = p;
            p           = q;
            q           = tmp;
        }
        final SubHyperplane_Old<Vector2D> l2DMinus =
            org.apache.commons.geometry.euclidean.twod.Line_Old.fromPoints(p, q, precision).wholeHyperplane();
        final SubHyperplane_Old<Vector2D> l2DPlus =
            org.apache.commons.geometry.euclidean.twod.Line_Old.fromPoints(q, p, precision).wholeHyperplane();

        final BSPTree_Old<Vector2D> splitTree = getRemainingRegion().getTree(false).split(l2DMinus);
        final BSPTree_Old<Vector2D> plusTree  = getRemainingRegion().isEmpty(splitTree.getPlus()) ?
                                               new BSPTree_Old<Vector2D>(Boolean.FALSE) :
                                               new BSPTree_Old<>(l2DPlus, new BSPTree_Old<Vector2D>(Boolean.FALSE),
                                                                        splitTree.getPlus(), null);

        final BSPTree_Old<Vector2D> minusTree = getRemainingRegion().isEmpty(splitTree.getMinus()) ?
                                               new BSPTree_Old<Vector2D>(Boolean.FALSE) :
                                                   new BSPTree_Old<>(l2DMinus, new BSPTree_Old<Vector2D>(Boolean.FALSE),
                                                                            splitTree.getMinus(), null);

        return new SplitSubHyperplane<>(new SubPlane(thisPlane.copySelf(), new PolygonsSet(plusTree, precision)),
                                                   new SubPlane(thisPlane.copySelf(), new PolygonsSet(minusTree, precision)));

    }

}
