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

import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.Transform;

/** Interface representing a triangle in Euclidean 3D space.
 */
public interface Triangle3D extends ConvexPolygon3D {

    /** The first point in the triangle.
     * @return the first point in the triangle
     */
    Vector3D getPoint1();

    /** The second point in the triangle.
     * @return the second point in the triangle
     */
    Vector3D getPoint2();

    /** The third point in the triangle.
     * @return the third point in the triangle
     */
    Vector3D getPoint3();

    /** {@inheritDoc} */
    @Override
    Triangle3D reverse();

    /** {@inheritDoc} */
    @Override
    Triangle3D transform(Transform<Vector3D> transform);

    /** {@inheritDoc}
    *
    * <p>This method simply returns a singleton list containing this object.</p>
    */
    @Override
    default List<Triangle3D> toTriangles() {
        return Collections.singletonList(this);
    }
}
