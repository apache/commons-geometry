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
package org.apache.commons.geometry.euclidean.threed.mesh;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.euclidean.threed.Triangle3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

/** Interface representing a mesh composed entirely of triangular faces.
 */
public interface TriangleMesh extends Mesh<TriangleMesh.Face> {

    /** {@inheritDoc} */
    @Override
    TriangleMesh transform(Transform<Vector3D> transform);

    /** Interface representing a single triangular face in a mesh.
     */
    interface Face extends Mesh.Face {

        /** Get the first vertex in the face.
         * @return the first vertex in the face
         */
        Vector3D getPoint1();

        /** Get the second vertex in the face.
         * @return the second vertex in the face
         */
        Vector3D getPoint2();

        /** Get the third vertex in the face.
         * @return the third vertex in the face
         */
        Vector3D getPoint3();

        /** {@inheritDoc} */
        @Override
        Triangle3D getPolygon();
    }
}
