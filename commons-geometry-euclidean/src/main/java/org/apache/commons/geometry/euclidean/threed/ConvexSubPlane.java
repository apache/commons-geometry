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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partition.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partition.Hyperplane;
import org.apache.commons.geometry.core.partition.Split;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.Plane.SubspaceTransform;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

/** Class representing a convex subhyperplane in 3 dimensional Euclidean space, meaning
 * a 2D convex area embedded in a plane.
 */
public final class ConvexSubPlane extends AbstractSubPlane<ConvexArea>
    implements ConvexSubHyperplane<Vector3D>  {

    /** Serializable UID */
    private static final long serialVersionUID = 20190729L;

    /** The embedded 2D area. */
    private final ConvexArea area;

    /** Create a new instance from its component parts.
     * @param plane plane the the convex area is embedded in
     * @param area the embedded convex area
     */
    private ConvexSubPlane(final Plane plane, final ConvexArea area) {
        super(plane);

        this.area = area;
    }

    /** {@inheritDoc} */
    @Override
    public List<ConvexSubPlane> toConvex() {
        return Arrays.asList(this);
    }

    /** {@inheritDoc} */
    @Override
    public ConvexSubPlane transform(final Transform<Vector3D> transform) {
        final SubspaceTransform st = getPlane().subspaceTransform(transform);
        final ConvexArea tArea = area.transform(st.getTransform());

        return fromConvexArea(st.getPlane(), tArea);
    }

    /** {@inheritDoc} */
    @Override
    public ConvexArea getSubspaceRegion() {
        return area;
    }

    /** {@inheritDoc} */
    @Override
    public Split<ConvexSubPlane> split(Hyperplane<Vector3D> splitter) {
        return splitInternal(splitter, this, (p, r) -> new ConvexSubPlane(p, (ConvexArea) r));
    }

    /** Create a new instance from a plane and an embedded convex subspace area.
     * @param plane embedding plane for the area
     * @param area area embedded in the plane
     * @return a new convex sub plane instance
     */
    public static ConvexSubPlane fromConvexArea(final Plane plane, final ConvexArea area) {
        return new ConvexSubPlane(plane, area);
    }

    /** Create a new instance from the given sequence of points. The points must define a unique plane, meaning that
     * at least 3 unique vertices must be given.
     * @param pts collection of points defining the convex subplane
     * @param precision precision context used to compare floating point values
     * @return a new instance defined by the given sequence of vertices
     * @throws IllegalArgumentException if fewer than 3 vertices are given
     * @throws GeometryException if the vertices do not define a unique plane
     * @see #fromVertexLoop(Collection, DoublePrecisionContext)
     * @see Plane#fromPoints(Collection, DoublePrecisionContext)
     */
    public static ConvexSubPlane fromVertices(final Collection<Vector3D> pts, final DoublePrecisionContext precision) {
        return fromVertices(pts, precision, false);
    }

    /** Create a new instance from the given sequence of points. The points must define a unique plane, meaning that
    * at least 3 unique vertices must be given. In contrast with the {@link #fromVertices(Collection, DoublePrecisionContext)}
    * method, the first point in the sequence is included at the end if needed, in order to form a closed loop.
    * @param pts collection of points defining the convex subplane
    * @param precision precision context used to compare floating point values
    * @return a new instance defined by the given sequence of vertices
    * @throws IllegalArgumentException if fewer than 3 vertices are given
    * @throws GeometryException if the vertices do not define a unique plane
    * @see #fromVertices(Collection, DoublePrecisionContext)
    * @see Plane#fromPoints(Collection, DoublePrecisionContext)
    */
    public static ConvexSubPlane fromVertexLoop(final Collection<Vector3D> pts, final DoublePrecisionContext precision) {
        return fromVertices(pts, precision, true);
    }

    /** Internal method to create convex subplane instances from sequences of points.
     * @param pts collection of points
     * @param precision precision context used to compare floating point values
     * @param makeLoop if true, the point sequence will implicitly include the start point again at the end; otherwise
     *      the vertex sequence is taken as-is
     * @return a new convex subplane instance
     */
    private static ConvexSubPlane fromVertices(final Collection<Vector3D> pts, final DoublePrecisionContext precision,
            final boolean makeLoop) {

        final Plane plane = Plane.fromPoints(pts, precision);

        final List<Vector2D> subspacePts = plane.toSubspace(pts);
        final ConvexArea area = makeLoop ?
                ConvexArea.fromVertexLoop(subspacePts, precision) :
                ConvexArea.fromVertices(subspacePts, precision);

        return new ConvexSubPlane(plane, area);
    }
}
