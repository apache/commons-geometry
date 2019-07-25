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

import java.util.function.BiFunction;

import org.apache.commons.geometry.core.partition.AbstractEmbeddingSubHyperplane;
import org.apache.commons.geometry.core.partition.Hyperplane;
import org.apache.commons.geometry.core.partition.HyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partition.Split;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.SubPlane.SubPlaneBuilder;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

/** Internal base class for subplane implementations.
 * @param <R> Subspace region type
 */
abstract class AbstractSubPlane<R extends HyperplaneBoundedRegion<Vector2D>>
    extends AbstractEmbeddingSubHyperplane<Vector3D, Vector2D, Plane> {

    /** The plane defining this instance. */
    private final Plane plane;

    /** Construct a new instance based on the given plane.
     * @param plane the plane defining the subplane
     */
    AbstractSubPlane(final Plane plane) {
        this.plane = plane;
    }

    /** Get the plane that this subplane lies on. This method is an alias
     * for {@link getHyperplane()}.
     * @return the plane that this subplane lies on
     * @see #getHyperplane()
     */
    public Plane getPlane() {
        return getHyperplane();
    }

    /** {@inheritDoc} */
    @Override
    public Plane getHyperplane() {
        return plane;
    }

    /** {@inheritDoc} */
    @Override
    public SubPlaneBuilder builder() {
        return new SubPlaneBuilder(plane);
    }

    /** Return the object used to perform floating point comparisons, which is the
     * same object used by the underlying {@link Plane).
     * @return precision object used to perform floating point comparisons.
     */
    public DoublePrecisionContext getPrecision() {
        return plane.getPrecision();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
            .append("[plane= ")
            .append(getPlane())
            .append(", subspaceRegion= ")
            .append(getSubspaceRegion());


        return sb.toString();
    }

    /** Generic, internal split method. Subclasses should call this from their
     * {@link #split(Hyperplane)} methods.
     * @param splitter splitting hyperplane
     * @param thisInstance a reference to the current instance; this is passed as
     *      an argument in order to allow it to be a generic type
     * @param factory function used to create new subhyperplane instances
     * @return the result of the split operation
     */
    protected <T extends AbstractSubPlane<R>> Split<T> splitInternal(final Hyperplane<Vector3D> splitter,
            final T thisInstance, final BiFunction<Plane, HyperplaneBoundedRegion<Vector2D>, T> factory) {

        final Plane thisPlane = thisInstance.getPlane();
        final Plane splitterPlane = (Plane) splitter;
        final DoublePrecisionContext precision = thisInstance.getPrecision();

        final Line3D intersection = splitterPlane.intersection(thisPlane);
        if (intersection == null) {
            // the lines are parallel or coincident; check which side of
            // the splitter we lie on
            final double offset = splitterPlane.offset(thisPlane);
            final int comp = precision.compare(offset, 0.0);

            if (comp < 0) {
                return new Split<>(thisInstance, null);
            }
            else if (comp > 0) {
                return new Split<>(null, thisInstance);
            }
            else {
                return new Split<>(null, null);
            }
        }
        else {
            // the lines intersect; split the subregion
            final Vector3D intersectionOrigin = intersection.getOrigin();
            final Vector2D subspaceP1 = thisPlane.toSubspace(intersectionOrigin);
            final Vector2D subspaceP2 = thisPlane.toSubspace(intersectionOrigin.add(intersection.getDirection()));

            final Line subspaceSplitter = Line.fromPoints(subspaceP1, subspaceP2, getPrecision());

            Split<? extends HyperplaneBoundedRegion<Vector2D>> split = thisInstance.getSubspaceRegion().split(subspaceSplitter);

            final T minus = (split.getMinus() != null) ? factory.apply(getPlane(), split.getMinus()) : null;
            final T plus = (split.getPlus() != null) ? factory.apply(getPlane(), split.getPlus()) : null;

            return new Split<>(minus, plus);
        }
    }
}
