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
package org.apache.commons.geometry.euclidean.threed.line;

import java.util.List;

/** Interface for objects that support linecast operations in Euclidean 3D space.
 *
 * <p>
 * Linecasting is a process that takes a line or line convex subset and intersects it with
 * the boundaries of a region. This is similar to
 * <a href="https://en.wikipedia.org/wiki/Ray_casting">raycasting</a> used
 * for collision detection with the exception that the intersecting element can be a
 * line or line convex subset and not just a ray.
 * </p>
 */
public interface Linecastable3D {

    /** Intersect the given line against the boundaries in this instance, returning a
     * list of all intersections in order of increasing distance along the line. An empty
     * list is returned if no intersections are discovered.
     * @param line line the line to intersect
     * @return a list of computed intersections in order of increasing distance
     *      along the line
     */
    default List<LinecastPoint3D> linecast(final Line3D line) {
        return linecast(line.span());
    }

    /** Intersect the given line convex subset against the boundaries in this instance, returning
     * a list of all intersections in order of increasing distance along the line. An empty list is
     * returned if no intersections are discovered.
     * @param subset line subset to intersect
     * @return a list of computed intersections in order of increasing distance
     *      along the line
     */
    List<LinecastPoint3D> linecast(LineConvexSubset3D subset);

    /** Intersect the given line against the boundaries in this instance, returning the first
     * intersection found when traveling in the direction of the line from infinity.
     * @param line the line to intersect
     * @return the first intersection found or {@code null} if no intersection
     *      is found
     */
    default LinecastPoint3D linecastFirst(final Line3D line) {
        return linecastFirst(line.span());
    }

    /** Intersect the given line convex subset against the boundaries in this instance, returning
     * the first intersection found when traveling in the direction of the line subset from its
     * start point.
     * @param subset line subset to intersect
     * @return the first intersection found or {@code null} if no intersection
     *      is found
     */
    LinecastPoint3D linecastFirst(LineConvexSubset3D subset);
}
