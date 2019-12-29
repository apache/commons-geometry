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

import java.util.Comparator;

import org.apache.commons.geometry.euclidean.AbstractLinecastPoint;

/** Class representing intersections resulting from linecast operations in Euclidean
 * 3D space. This class contains the intersection point along with the boundary normal
 * of the target at the point of intersection.
 * @see Linecastable3D
 */
public class LinecastPoint3D extends AbstractLinecastPoint<Vector3D, Vector3D.Unit, Line3D> {

    /** Comparator that sorts intersection instances by increasing abscissa order. If two abscissa
     * values are equal, the comparison uses {@link Vector3D#COORDINATE_ASCENDING_ORDER} with the
     * intersection normals.
     */
    public static final Comparator<LinecastPoint3D> ABSCISSA_ORDER = (a, b) -> {
        int cmp = Double.compare(a.getAbscissa(), b.getAbscissa());
        if (cmp == 0) {
            cmp = Vector3D.COORDINATE_ASCENDING_ORDER.compare(a.getNormal(), b.getNormal());
        }
        return cmp;
    };

    /** Construct a new instance from its components.
     * @param point intersection point
     * @param normal normal of the target boundary at the intersection point
     * @param line intersecting line
     */
    public LinecastPoint3D(final Vector3D point, final Vector3D normal, final Line3D line) {
        super(point, normal.normalize(), line);
    }
}
