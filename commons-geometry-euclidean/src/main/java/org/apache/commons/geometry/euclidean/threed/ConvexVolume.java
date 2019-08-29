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
import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.exception.GeometryException;
import org.apache.commons.geometry.core.partitioning.AbstractConvexHyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;

/** Class representing a finite or infinite convex volume in Euclidean 3D space.
 * The boundaries of this area, if any, are composed of convex subplanes.
 */
public final class ConvexVolume extends AbstractConvexHyperplaneBoundedRegion<Vector3D, ConvexSubPlane> {

    /** Serializable UID */
    private static final long serialVersionUID = 20190811L;

    /** Instance representing the full 3D volume. */
    private static ConvexVolume FULL = new ConvexVolume(Collections.emptyList());

    /** Simple constructor. Callers are responsible for ensuring that the given path
     * represents the boundary of a convex area. No validation is performed.
     * @param boundaries the boundaries of the convex area
     */
    private ConvexVolume(final List<ConvexSubPlane> boundaries) {
        super(boundaries);
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        if (isFull()) {
            return Double.POSITIVE_INFINITY;
        }

        double volumeSum = 0.0;

        for (ConvexSubPlane subplane : getBoundaries()) {
            if (subplane.isInfinite()) {
                return Double.POSITIVE_INFINITY;
            }

            final Plane plane = subplane.getPlane();
            final ConvexArea subarea = subplane.getSubspaceRegion();

            final Vector3D facetBarycenter = subplane.getHyperplane().toSpace(
                    subarea.getBarycenter());


            volumeSum += subarea.getSize() * facetBarycenter.dot(plane.getNormal());
        }

        return volumeSum / 3.0;
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D getBarycenter() {
        if (isFull()) {
            return null;
        }

        double volumeSum = 0.0;

        double sumX = 0.0;
        double sumY = 0.0;
        double sumZ = 0.0;

        for (ConvexSubPlane subplane : getBoundaries()) {
            if (subplane.isInfinite()) {
                return null;
            }

            final Plane plane = subplane.getPlane();
            final ConvexArea subarea = subplane.getSubspaceRegion();

            final Vector3D facetBarycenter = subplane.getHyperplane().toSpace(
                    subarea.getBarycenter());

            double scaledVolume = subarea.getSize() * facetBarycenter.dot(plane.getNormal());

            volumeSum += scaledVolume;

            sumX += scaledVolume * facetBarycenter.getX();
            sumY += scaledVolume * facetBarycenter.getY();
            sumZ += scaledVolume * facetBarycenter.getZ();
        }

        double size = volumeSum / 3.0;

        // Since the volume we used when adding together the facet contributions
        // was 3x the actual pyramid size, we'll multiply by 1/4 here instead
        // of 3/4 to adjust for the actual barycenter position in each pyramid.
        final double barycenterScale = 1.0 / (4 * size);
        return Vector3D.of(
                sumX * barycenterScale,
                sumY * barycenterScale,
                sumZ * barycenterScale);
    }

    /** {@inheritDoc} */
    @Override
    public Split<ConvexVolume> split(final Hyperplane<Vector3D> splitter) {
        return splitInternal(splitter, this, ConvexSubPlane.class, ConvexVolume::new);
    }

    /** {@inheritDoc} */
    @Override
    public ConvexSubPlane trim(final ConvexSubHyperplane<Vector3D> convexSubHyperplane) {
        return (ConvexSubPlane) super.trim(convexSubHyperplane);
    }

    /** Return a new instance transformed by the argument.
     * @param transform transform to apply
     * @return a new instance transformed by the argument
     */
    public ConvexVolume transform(final Transform<Vector3D> transform) {
        return transformInternal(transform, this, ConvexSubPlane.class, ConvexVolume::new);
    }

    /** Return a BSP tree instance representing the same region as the current instance.
     * @return a BSP tree instance representing the same region as the current instance
     */
    public RegionBSPTree3D toTree() {
        return RegionBSPTree3D.fromConvexVolume(this);
    }

    /** Return an instance representing the full 3D volume.
     * @return an instance representing the full 3D volume.
     */
    public static ConvexVolume full() {
        return FULL;
    }

    /** Create a convex volume formed by the intersection of the negative half-spaces of the
     * given bounding planes. The returned instance represents the volume that is on the
     * minus side of all of the given plane. Note that this method does not support volumes
     * of zero size (ie, infinitely thin volumes or points.)
     * @param boundingPlanes planes used to define the convex area
     * @return a new convex volume instance representing the volume on the minus side of all
     *      of the bounding plane or an instance representing the full space if the collection
     *      is empty
     * @throws GeometryException if the given set of bounding planes do not form a convex vplume,
     *      meaning that there is no region that is on the minus side of all of the bounding
     *      planes.
     */
    public static ConvexVolume fromBounds(final Plane ... planes) {
        return fromBounds(Arrays.asList(planes));
    }

    /** Create a convex volume formed by the intersection of the negative half-spaces of the
     * given bounding planes. The returned instance represents the volume that is on the
     * minus side of all of the given plane. Note that this method does not support volumes
     * of zero size (ie, infinitely thin volumes or points.)
     * @param boundingPlanes planes used to define the convex area
     * @return a new convex volume instance representing the volume on the minus side of all
     *      of the bounding plane or an instance representing the full space if the collection
     *      is empty
     * @throws GeometryException if the given set of bounding planes do not form a convex vplume,
     *      meaning that there is no region that is on the minus side of all of the bounding
     *      planes.
     */
    public static ConvexVolume fromBounds(final Iterable<Plane> boundingPlanes) {
        final List<ConvexSubPlane> subplanes = new ConvexRegionBoundaryBuilder<>(ConvexSubPlane.class).build(boundingPlanes);
        return subplanes.isEmpty() ? full() : new ConvexVolume(subplanes);
    }
}
