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
package org.apache.commons.geometry.euclidean.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.geometry.euclidean.threed.Vector3D;

/** Class containing utilities and algorithms intended to be internal to the library.
 * Absolutely no guarantees are made regarding the stability of this API.
 */
public final class EuclideanInternals {

    /** Utility class; no instantiation. */
    private EuclideanInternals() { }

    /** Convert a convex polygon defined by a list of vertices into a triangle fan. The vertex forming the largest
     * interior angle in the polygon is selected as the base of the triangle fan. Callers are responsible for
     * ensuring that the given list of vertices define a geometrically valid convex polygon; no validation (except
     * for a check on the minimum number of vertices) is performed.
     * @param <T> triangle result type
     * @param vertices vertices defining a convex polygon
     * @param fn function accepting the vertices of each triangle as a list and returning the object used
     *      to represent that triangle in the result; each argument to this function is guaranteed to
     *      contain 3 vertices
     * @return a list containing the return results of the function when passed the vertices for each
     *      triangle in order
     * @throws IllegalArgumentException if fewer than 3 vertices are given
     */
    public static <T> List<T> convexPolygonToTriangleFan(final List<Vector3D> vertices,
           final Function<List<Vector3D>, T> fn) {
        final int size = vertices.size();
        if (size < 3) {
            throw new IllegalArgumentException("Cannot create triangle fan: 3 or more vertices are required " +
                    "but found only " + vertices.size());
        } else if (size == 3) {
            return Collections.singletonList(fn.apply(vertices));
        }

        final List<T> triangles = new ArrayList<>(size - 2);

        final int fanIdx = findBestTriangleFanIndex(vertices);
        int vertexIdx = (fanIdx + 1) % size;

        final Vector3D fanBase = vertices.get(fanIdx);
        Vector3D vertexA = vertices.get(vertexIdx);
        Vector3D vertexB;

        vertexIdx = (vertexIdx + 1) % size;
        while (vertexIdx != fanIdx) {
            vertexB = vertices.get(vertexIdx);

            triangles.add(fn.apply(Arrays.asList(fanBase, vertexA, vertexB)));

            vertexA = vertexB;
            vertexIdx = (vertexIdx + 1) % size;
        }

        return triangles;
    }

    /** Find the index of the best vertex to use as the base for a triangle fan split of the convex polygon
     * defined by the given vertices. The best vertex is the one that forms the largest interior angle in the
     * polygon since a split at that point will help prevent the creation of very thin triangles.
     * @param vertices vertices defining the convex polygon; must not be empty; no validation is performed
     *      to ensure that the vertices actually define a convex polygon
     * @return the index of the best vertex to use as the base for a triangle fan split of the convex polygon
     */
    private static int findBestTriangleFanIndex(final List<Vector3D> vertices) {
        final Iterator<Vector3D> it = vertices.iterator();

        Vector3D curPt = it.next();
        Vector3D nextPt;

        final Vector3D lastVec = vertices.get(vertices.size() - 1).directionTo(curPt);
        Vector3D incomingVec = lastVec;
        Vector3D outgoingVec;

        int bestIdx = 0;
        double bestDot = -1.0;

        int idx = 0;
        double dot;
        while (it.hasNext()) {
            nextPt = it.next();
            outgoingVec = curPt.directionTo(nextPt);

            dot = incomingVec.dot(outgoingVec);
            if (dot > bestDot) {
                bestIdx = idx;
                bestDot = dot;
            }

            curPt = nextPt;
            incomingVec = outgoingVec;

            ++idx;
        }

        // handle the last vertex on its own
        dot = incomingVec.dot(lastVec);
        if (dot > bestDot) {
            bestIdx = idx;
        }

        return bestIdx;
    }
}
