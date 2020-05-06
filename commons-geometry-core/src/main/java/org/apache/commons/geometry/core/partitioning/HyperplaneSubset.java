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
import org.apache.commons.geometry.core.Sized;
import org.apache.commons.geometry.core.Transform;

/** Interface representing a subset of the points lying on a hyperplane. Examples include
 * rays and line segments in Euclidean 2D space and triangular facets in Euclidean 3D space.
 * Hyperplane subsets can have finite or infinite size and can represent contiguous regions
 * of the hyperplane (as in the examples aboves); multiple, disjoint regions; or the
 * {@link Hyperplane#span() entire hyperplane}.
 *
 * @param <P> Point implementation type
 * @see Hyperplane
 */
public interface HyperplaneSubset<P extends Point<P>> extends Splittable<P, HyperplaneSubset<P>>, Sized {

    /** Get the hyperplane containing this instance.
     * @return the hyperplane containing this instance
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

    /** Classify a point with respect to the subset region. The point is classified as follows:
     * <ul>
     *  <li>{@link RegionLocation#INSIDE INSIDE} - The point lies on the hyperplane
     *      and inside of the subset region.</li>
     *  <li>{@link RegionLocation#BOUNDARY BOUNDARY} - The point lies on the hyperplane
     *      and is on the boundary of the subset region.</li>
     *  <li>{@link RegionLocation#OUTSIDE OUTSIDE} - The point does not lie on
     *      the hyperplane or it does lie on the hyperplane but is outside of the
     *      subset region.</li>
     * </ul>
     * @param pt the point to classify
     * @return classification of the point with respect to the hyperplane
     *      and subspace region
     */
    RegionLocation classify(P pt);

    /** Return true if the hyperplane subset contains the given point, meaning that the point
     * lies on the hyperplane and is not on the outside of the subset region.
     * @param pt the point to check
     * @return true if the point is contained in the hyperplane subset
     */
    default boolean contains(P pt) {
        final RegionLocation loc = classify(pt);
        return loc != null && loc != RegionLocation.OUTSIDE;
    }

    /** Return the closest point to the argument that is contained in the subset
     * (ie, not classified as {@link RegionLocation#OUTSIDE outside}), or null if no
     * such point exists.
     * @param pt the reference point
     * @return the closest point to the reference point that is contained in the subset,
     *      or null if no such point exists
     */
    P closest(P pt);

    /** Return a {@link Builder} instance for joining multiple
     * hyperplane subsets together.
     * @return a new builder instance
     */
    Builder<P> builder();

    /** Return a new hyperplane subset resulting from the application of the given transform.
     * The current instance is not modified.
     * @param transform the transform instance to apply
     * @return new transformed hyperplane subset
     */
    HyperplaneSubset<P> transform(Transform<P> transform);

    /** Convert this instance into a list of convex child subsets.
     * @return a list of hyperplane convex subsets representing the same subspace
     *      region as this instance
     */
    List<? extends HyperplaneConvexSubset<P>> toConvex();

    /** Interface for joining multiple {@link HyperplaneSubset}s into a single
     * instance.
     * @param <P> Point implementation type
     */
    interface Builder<P extends Point<P>> {

        /** Add a {@link HyperplaneSubset} instance to the builder.
         * @param sub subset to add to this instance
         */
        void add(HyperplaneSubset<P> sub);

        /** Add a {@link HyperplaneConvexSubset} instance to the builder.
         * @param sub convex subset to add to this instance
         */
        void add(HyperplaneConvexSubset<P> sub);

        /** Get a {@link HyperplaneSubset} representing the union
         * of all input subsets.
         * @return subset representing the union of all input subsets
         */
        HyperplaneSubset<P> build();
    }
}
