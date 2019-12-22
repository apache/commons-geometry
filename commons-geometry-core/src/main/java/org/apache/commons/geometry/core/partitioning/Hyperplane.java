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

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.Transform;

/** Interface representing a hyperplane, which is a subspace of dimension
 * one less than its surrounding space. (A hyperplane in Euclidean 3D space,
 * for example, is a 2 dimensional plane.)
 *
 * <p>
 * Hyperplanes partition their surrounding space into 3 distinct sets: (1) points
 * lying on one side of the hyperplane, (2) points lying on the opposite side, and
 * (3) points lying on the hyperplane itself. One side of the hyperplane is labeled
 * as the <em>plus</em> side and the other as the <em>minus</em> side. The
 * {@link #offset(Point) offset} of a point in relation to a hyperplane is the distance
 * from the point to the hyperplane combined with the sign of the side that the point
 * lies on: points lying on the plus side of the hyperplane have a positive offsets,
 * those on the minus side have a negative offset, and those lying directly on the
 * hyperplane have an offset of zero.
 *
 * @param <P> Point implementation type
 * @see HyperplaneLocation
 * @see SubHyperplane
 */
public interface Hyperplane<P extends Point<P>> {

    /** Get the offset (oriented distance) of a point with respect
     * to this instance. Points with an offset of zero lie on the
     * hyperplane itself.
     * @param point the point to compute the offset for
     * @return the offset of the point
     */
    double offset(P point);

    /** Classify a point with respect to this hyperplane.
     * @param point the point to classify
     * @return the relative location of the point with
     *      respect to this instance
     */
    HyperplaneLocation classify(P point);

    /** Return true if the given point lies on the hyperplane.
     * @param point the point to test
     * @return true if the point lies on the hyperplane
     */
    boolean contains(P point);

    /** Project a point onto this instance.
     * @param point the point to project
     * @return the projection of the point onto this instance. The returned
     *      point lies on the hyperplane.
     */
    P project(P point);

    /** Return a hyperplane that has the opposite orientation as this instance.
     * That is, the plus side of this instance is the minus side of the returned
     * instance and vice versa.
     * @return a hyperplane with the opposite orientation
     */
    Hyperplane<P> reverse();

    /** Transform this instance using the given {@link Transform}.
     * @param transform object to transform this instance with
     * @return a new, transformed hyperplane
     */
    Hyperplane<P> transform(Transform<P> transform);

    /** Return true if this instance has a similar orientation to the given hyperplane,
     * meaning that they point in generally the same direction. This method is not
     * used to determine exact equality of hyperplanes, but rather to determine whether
     * two hyperplanes that contain the same points are parallel (point in the same direction)
     * or anti-parallel (point in opposite directions).
     * @param other the hyperplane to compare with
     * @return true if the hyperplanes point in generally the same direction and could
     *      possibly be parallel
     */
    boolean similarOrientation(Hyperplane<P> other);

    /** Return a {@link ConvexSubHyperplane} spanning this entire hyperplane. The returned
     * subhyperplane contains all points lying in this hyperplane and no more.
     * @return a {@link ConvexSubHyperplane} containing all points lying in this hyperplane
     */
    ConvexSubHyperplane<P> span();
}
