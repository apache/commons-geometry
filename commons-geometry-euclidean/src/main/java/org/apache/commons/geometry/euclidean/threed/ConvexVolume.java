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
import java.util.stream.Stream;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.AbstractConvexHyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.Split;

/** Class representing a finite or infinite convex volume in Euclidean 3D space.
 * The boundaries of this area, if any, are composed of plane convex subsets.
 */
public class ConvexVolume extends AbstractConvexHyperplaneBoundedRegion<Vector3D, PlaneConvexSubset>
    implements BoundarySource3D {

    /** Instance representing the full 3D volume. */
    private static final ConvexVolume FULL = new ConvexVolume(Collections.emptyList());

    /** Simple constructor. Callers are responsible for ensuring that the given path
     * represents the boundary of a convex area. No validation is performed.
     * @param boundaries the boundaries of the convex area
     */
    protected ConvexVolume(final List<PlaneConvexSubset> boundaries) {
        super(boundaries);
    }

    /** {@inheritDoc} */
    @Override
    public Stream<PlaneConvexSubset> boundaryStream() {
        return getBoundaries().stream();
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        if (isFull()) {
            return Double.POSITIVE_INFINITY;
        }

        double volumeSum = 0.0;

        for (final PlaneConvexSubset boundary : getBoundaries()) {
            if (boundary.isInfinite()) {
                return Double.POSITIVE_INFINITY;
            }

            final Plane boundaryPlane = boundary.getPlane();
            final double boundaryArea = boundary.getSize();
            final Vector3D boundaryBarycenter = boundary.getBarycenter();

            volumeSum += boundaryArea * boundaryBarycenter.dot(boundaryPlane.getNormal());
        }

        return volumeSum / 3.0;
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D getBarycenter() {
        double volumeSum = 0.0;

        double sumX = 0.0;
        double sumY = 0.0;
        double sumZ = 0.0;

        for (final PlaneConvexSubset boundary : getBoundaries()) {
            if (boundary.isInfinite()) {
                return null;
            }

            final Plane boundaryPlane = boundary.getPlane();
            final double boundaryArea = boundary.getSize();
            final Vector3D boundaryBarycenter = boundary.getBarycenter();

            final double scaledVolume = boundaryArea * boundaryBarycenter.dot(boundaryPlane.getNormal());

            volumeSum += scaledVolume;

            sumX += scaledVolume * boundaryBarycenter.getX();
            sumY += scaledVolume * boundaryBarycenter.getY();
            sumZ += scaledVolume * boundaryBarycenter.getZ();
        }

        if (volumeSum > 0) {
            final double size = volumeSum / 3.0;

            // Since the volume we used when adding together the boundary contributions
            // was 3x the actual pyramid size, we'll multiply by 1/4 here instead
            // of 3/4 to adjust for the actual barycenter position in each pyramid.
            final double barycenterScale = 1.0 / (4 * size);
            return Vector3D.of(
                    sumX * barycenterScale,
                    sumY * barycenterScale,
                    sumZ * barycenterScale);
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Split<ConvexVolume> split(final Hyperplane<Vector3D> splitter) {
        return splitInternal(splitter, this, PlaneConvexSubset.class, ConvexVolume::new);
    }

    /** Return a BSP tree representing the same region as this instance.
     */
    @Override
    public RegionBSPTree3D toTree() {
        return RegionBSPTree3D.from(getBoundaries(), true);
    }

    /** {@inheritDoc} */
    @Override
    public PlaneConvexSubset trim(final HyperplaneConvexSubset<Vector3D> convexSubset) {
        return (PlaneConvexSubset) super.trim(convexSubset);
    }

    /** Return a new instance transformed by the argument.
     * @param transform transform to apply
     * @return a new instance transformed by the argument
     */
    public ConvexVolume transform(final Transform<Vector3D> transform) {
        return transformInternal(transform, this, PlaneConvexSubset.class, ConvexVolume::new);
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
     * @param planes planes used to define the convex area
     * @return a new convex volume instance representing the volume on the minus side of all
     *      of the bounding plane or an instance representing the full space if the collection
     *      is empty
     * @throws IllegalArgumentException if the given set of bounding planes do not form a convex volume,
     *      meaning that there is no region that is on the minus side of all of the bounding planes.
     */
    public static ConvexVolume fromBounds(final Plane... planes) {
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
     * @throws IllegalArgumentException if the given set of bounding planes do not form a convex volume,
     *      meaning that there is no region that is on the minus side of all of the bounding planes.
     */
    public static ConvexVolume fromBounds(final Iterable<Plane> boundingPlanes) {
        final List<PlaneConvexSubset> facets = new ConvexRegionBoundaryBuilder<>(PlaneConvexSubset.class)
                .build(boundingPlanes);
        return facets.isEmpty() ? full() : new ConvexVolume(facets);
    }
}
