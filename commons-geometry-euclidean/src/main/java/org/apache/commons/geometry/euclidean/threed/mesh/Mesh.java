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

import java.util.List;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.ConvexPolygon3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

/** Interface representing a 3D mesh data structure.
 * @param <F> Mesh face implementation type
 * @see <a href="https://en.wikipedia.org/wiki/Polygon_mesh">Polygon Mesh</a>
 */
public interface Mesh<F extends Mesh.Face> extends BoundarySource3D {

    /** Get an iterable containing the vertices in the mesh.
     * @return an iterable containing the vertices in the mesh
     */
    Iterable<Vector3D> vertices();

    /** Get a list containing all vertices in the mesh.
     * @return a list containing all vertices in the mesh
     */
    List<Vector3D> getVertices();

    /** Get the number of vertices in the mesh.
     * @return the number of vertices in the mesh
     */
    int getVertexCount();

    /** Get an iterable containing all faces in the mesh.
     * @return an iterable containing all faces in the mesh
     */
    Iterable<F> faces();

    /** Get a list containing all faces in the mesh.
     * @return a list containing all faces in the mesh
     */
    List<F> getFaces();

    /** Get the number of faces in the mesh.
     * @return the number of faces in the mesh
     */
    int getFaceCount();

    /** Get a face from the mesh by its index.
     * @param index the index of the mesh to retrieve
     * @return the face at the given index
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    F getFace(int index);

    /** Return a new, transformed mesh by applying the given transform to
     * all vertices. Faces and vertex ordering are not affected.
     * @param transform transform to apply
     * @return a new, transformed mesh
     */
    Mesh<F> transform(Transform<Vector3D> transform);

    /** Interface representing a single face in a mesh.
     */
    interface Face {

        /** Get the 0-based index of the face in the mesh.
         * @return the 0-based index of the face in the mesh
         */
        int getIndex();

        /** Get an array containing the 0-based indices of the vertices defining
         * this face. The indices are references to the vertex positions in
         * the mesh vertex list.
         * @return an array containing the indices of the vertices defining
         *      this face
         * @see Mesh#getVertices()
         */
        int[] getVertexIndices();

        /** Get the vertices for the face.
         * @return the vertices for the face
         */
        List<Vector3D> getVertices();

        /** Return true if the vertices for this face define a convex polygon
         * with non-zero size.
         * @return true if the vertices for this face define a convex polygon
         *      with non-zero size
         */
        boolean definesPolygon();

        /** Get the 3D polygon defined by this face.
         * @return the 3D polygon defined by this face
         * @throws IllegalArgumentException if the vertices for the face do not
         *      define a polygon
         * @see #definesPolygon()
         */
        ConvexPolygon3D getPolygon();
    }
}
