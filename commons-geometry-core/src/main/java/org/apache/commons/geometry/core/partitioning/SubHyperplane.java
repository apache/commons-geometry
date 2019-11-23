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
package org.apache.commons.geometry.core.partitioning;

import java.util.List;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;

/** Interface representing subhyperplanes, which are regions
 * embedded in a hyperplane.

 * @param <P> Point implementation type
 */
public interface SubHyperplane<P extends Point<P>> extends Splittable<P, SubHyperplane<P>> {

    /** Get the hyperplane that this instance is embedded in.
     * @return the hyperplane that this instance is embedded in.
     */
    Hyperplane<P> getHyperplane();

    /** Return true if this instance contains all points in the
     * hyperplane.
     * @return true if this instance contains all points in the
     *      hyperplane
     */
    boolean isFull();

    /** Return true if this instance does not contain any points.
     * @return true if this instance does not contain any points
     */
    boolean isEmpty();

    /** Return true if this instance has infinite size.
     * @return true if this instance has infinite size
     */
    boolean isInfinite();

    /** Return true if this instance has finite size.
     * @return true if this instance has finite size
     */
    boolean isFinite();

    /** Return the size of this instance. This will have different
     * meanings in different spaces and dimensions. For example, in
     * Euclidean space, this will be length in 2D and area in 3D.
     * @return the size of this instance
     */
    double getSize();

    /** Classify a point with respect to the subhyperplane's region. The point is
     * classified as follows:
     * <ul>
     *  <li>{@link RegionLocation#INSIDE INSIDE} - The point lies on the hyperplane
     *      and inside of the subhyperplane's region.</li>
     *  <li>{@link RegionLocation#BOUNDARY BOUNDARY} - The point lies on the hyperplane
     *      and is on the boundary of the subhyperplane's region.</li>
     *  <li>{@link RegionLocation#OUTSIDE OUTSIDE} - The point does not lie on
     *      the hyperplane or it does lie on the hyperplane but is outside of the
     *      subhyperplane's region.</li>
     * </ul>
     * @param point the point to classify
     * @return classification of the point with respect to the subhyperplane's hyperplane
     *      and region
     */
    RegionLocation classify(P point);

    /** Return true if the subhyperplane contains the given point, meaning that the point
     * lies on the hyperplane and is not on the outside of the subhyperplane's region.
     * @param point the point to check
     * @return true if the point is contained in the subhyperplane
     */
    default boolean contains(P point) {
        final RegionLocation loc = classify(point);
        return loc != null && loc != RegionLocation.OUTSIDE;
    }

    /** Return the closest point to the argument that is contained in the subhyperplane
     * (ie, not classified as {@link RegionLocation#OUTSIDE outside}), or null if no
     * such point exists.
     * @param point the reference point
     * @return the closest point to the reference point that is contained in the subhyperplane,
     *      or null if no such point exists
     */
    P closest(P point);

    /** Return a {@link Builder} instance for joining multiple
     * subhyperplanes together.
     * @return a new builder instance
     */
    Builder<P> builder();

    /** Return a new subhyperplane instance resulting from the application
     * of the given transform. The current instance is not modified.
     * @param transform the transform instance to apply
     * @return new transformed subhyperplane instance
     */
    SubHyperplane<P> transform(Transform<P> transform);

    /** Convert this instance into a list of convex child subhyperplanes.
     * @return a list of convex subhyperplanes representing the same subspace
     *      region as this instance
     */
    List<? extends ConvexSubHyperplane<P>> toConvex();

    /** Interface for joining multiple {@link SubHyperplane}s into a single
     * instance.
     * @param <P> Point implementation type
     */
    interface Builder<P extends Point<P>> {

        /** Add a {@link SubHyperplane} instance to the builder.
         * @param sub subhyperplane to add to this instance
         */
        void add(SubHyperplane<P> sub);

        /** Add a {@link ConvexSubHyperplane} instance to the builder.
         * @param sub convex subhyperplane to add to this instance
         */
        void add(ConvexSubHyperplane<P> sub);

        /** Get a {@link SubHyperplane} representing the union
         * of all input subhyperplanes.
         * @return subhyperplane representing the union of all input
         *      subhyperplanes
         */
        SubHyperplane<P> build();
    }
}
