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

import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.euclidean.threed.Plane.SubspaceTransform;
import org.apache.commons.geometry.euclidean.threed.lines.Line3D;
import org.apache.commons.geometry.euclidean.threed.lines.LineConvexSubset3D;
import org.apache.commons.geometry.euclidean.twod.AffineTransformMatrix2D;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

/** Class representing a convex subset of points in a plane. The subset may be finite
 * or infinite.
 * @see Planes
 */
public final class PlaneConvexSubset extends PlaneSubset
    implements HyperplaneConvexSubset<Vector3D>  {
    /** The embedded 2D area. */
    private final ConvexArea area;

    /** Create a new instance from its component parts.
     * @param plane plane the the convex area is embedded in
     * @param area the embedded convex area
     */
    PlaneConvexSubset(final Plane plane, final ConvexArea area) {
        super(plane);

        this.area = area;
    }

    /** {@inheritDoc} */
    @Override
    public List<PlaneConvexSubset> toConvex() {
        return Collections.singletonList(this);
    }

    /** {@inheritDoc} */
    @Override
    public PlaneConvexSubset reverse() {
        final Plane plane = getPlane();
        final Plane rPlane = plane.reverse();

        final Vector2D rU = rPlane.toSubspace(plane.toSpace(Vector2D.Unit.PLUS_X));
        final Vector2D rV = rPlane.toSubspace(plane.toSpace(Vector2D.Unit.PLUS_Y));

        final AffineTransformMatrix2D transform =
                AffineTransformMatrix2D.fromColumnVectors(rU, rV);

        return new PlaneConvexSubset(rPlane, area.transform(transform));
    }

    /** {@inheritDoc} */
    @Override
    public PlaneConvexSubset transform(final Transform<Vector3D> transform) {
        final SubspaceTransform st = getPlane().subspaceTransform(transform);
        final ConvexArea tArea = area.transform(st.getTransform());

        return Planes.subsetFromConvexArea(st.getPlane(), tArea);
    }

    /** {@inheritDoc} */
    @Override
    public ConvexArea getSubspaceRegion() {
        return area;
    }

    /** {@inheritDoc} */
    @Override
    public Split<PlaneConvexSubset> split(final Hyperplane<Vector3D> splitter) {
        return splitInternal(splitter, this, (p, r) -> new PlaneConvexSubset(p, (ConvexArea) r));
    }

    /** Get the unique intersection of this plane subset with the given line. Null is
     * returned if no unique intersection point exists (ie, the line and plane are
     * parallel or coincident) or the line does not intersect the plane subset.
     * @param line line to intersect with this plane subset
     * @return the unique intersection point between the line and this plane subset
     *      or null if no such point exists.
     * @see Plane#intersection(Line3D)
     */
    public Vector3D intersection(final Line3D line) {
        final Vector3D pt = getPlane().intersection(line);
        return (pt != null && contains(pt)) ? pt : null;
    }

    /** Get the unique intersection of this plane subset with the given line subset. Null
     * is returned if the underlying line and plane do not have a unique intersection
     * point (ie, they are parallel or coincident) or the intersection point is unique
     * but is not contained in both the line subset and plane subset.
     * @param lineSubset line subset to intersect with
     * @return the unique intersection point between this plane subset and the argument or
     *      null if no such point exists.
     * @see Plane#intersection(Line3D)
     */
    public Vector3D intersection(final LineConvexSubset3D lineSubset) {
        final Vector3D pt = intersection(lineSubset.getLine());
        return (pt != null && lineSubset.contains(pt)) ? pt : null;
    }

    /** Get the vertices for the plane subset. The vertices lie at the intersections of the
     * 2D area bounding lines.
     * @return the vertices for the plane subset
     */
    public List<Vector3D> getVertices() {
        return getPlane().toSpace(area.getVertices());
    }
}
