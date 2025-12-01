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
package org.apache.commons.geometry.io.euclidean.threed;

import java.util.List;

import org.apache.commons.geometry.euclidean.threed.Vector3D;

/** Interface containing values (vertices and optional normal) used to define a convex,
 * finite polygon in 3D space. In contrast to the similar
 * {@link org.apache.commons.geometry.euclidean.threed.ConvexPolygon3D ConvexPolygon3D}
 * class, no guarantees are made regarding the geometric validity of the data.
 * For example, instances may contain vertices that do not lie in the same plane
 * or have normals that are not unit length or point in an unexpected direction.
 * This is lack of validation is intentional, since a primary purpose of this
 * interface is to allow access to raw, possibly invalid, geometric data from input
 * sources.
 * @see org.apache.commons.geometry.euclidean.threed.ConvexPolygon3D
 * @see FacetDefinitions
 */
public interface FacetDefinition {

    /** Get the facet vertices.
     * @return facet vertices
     */
    List<Vector3D> getVertices();

    /** Get the normal defined for the facet or {@code null} if one has not been explicitly
     * specified. No guarantees are made regarding the properties of the normal
     * or its relationship to the vertices.
     * @return the defined normal for the facet or {@code null} if one has not been explicitly
     *      specified
     */
    Vector3D getNormal();
}
