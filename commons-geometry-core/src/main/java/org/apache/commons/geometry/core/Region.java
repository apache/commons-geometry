/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.core;

/** Interface representing a region in a space. A region partitions a space
 * into sets of points lying on the inside, outside, and boundary.
 * @param <P> Point implementation type
 */
public interface Region<P extends Point<P>> extends Sized {

    /** Return true if the region spans the entire space. In other words,
     * a region is full if no points in the space are classified as
     * {@link RegionLocation#OUTSIDE outside}.
     * @return true if the region spans the entire space
     */
    boolean isFull();

    /** Return true if the region is completely empty, ie all points in
     * the space are classified as {@link RegionLocation#OUTSIDE outside}.
     * @return true if the region is empty
     */
    boolean isEmpty();

    /** Get the size of the boundary of the region. The size is a value in
     * the {@code d-1} dimension space. For example, in Euclidean space,
     * this will be a length in 2D and an area in 3D.
     * @return the size of the boundary of the region
     */
    double getBoundarySize();

    /** Get the centroid, or geometric center, of the region or {@code null} if no centroid
     * exists or one exists but is not unique. A centroid will not exist for empty or
     * infinite regions.
     *
     * <p>The centroid of a geometric object is defined as the mean position of
     * all points in the object, including interior points, vertices, and other points
     * lying on the boundary. If a physical object has a uniform density, then its center
     * of mass is the same as its geometric centroid.
     * </p>
     * @return the centroid of the region or {@code null} if no unique centroid exists
     * @see <a href="https://en.wikipedia.org/wiki/Centroid">Centroid</a>
     */
    P getCentroid();

    /** Classify the given point with respect to the region.
     * @param pt the point to classify
     * @return the location of the point with respect to the region
     */
    RegionLocation classify(P pt);

    /** Return true if the given point is on the inside or boundary
     * of the region.
     * @param pt the point to test
     * @return true if the point is on the inside or boundary of the region
     */
    default boolean contains(final P pt) {
        final RegionLocation location = classify(pt);
        return location != null && location != RegionLocation.OUTSIDE;
    }

    /** Project a point onto the boundary of the region. {@code null} is returned if
     * the region contains no boundaries (ie, is either {@link #isFull() full}
     * or {@link #isEmpty() empty}).
     * @param pt pt to project
     * @return projection of the point on the boundary of the region or {@code null}
     *      if the region does not contain any boundaries
     */
    P project(P pt);
}
