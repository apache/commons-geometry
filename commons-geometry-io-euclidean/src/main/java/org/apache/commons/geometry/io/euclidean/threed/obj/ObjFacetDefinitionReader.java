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
package org.apache.commons.geometry.io.euclidean.threed.obj;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinition;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinitionReader;
import org.apache.commons.geometry.io.euclidean.threed.SimpleFacetDefinition;

/** Class for reading {@link FacetDefinition facets} from OBJ content.
 */
public class ObjFacetDefinitionReader extends AbstractObjPolygonReader
    implements FacetDefinitionReader {

    /** List of vertices encountered in the input. */
    private final List<Vector3D> vertices = new ArrayList<>();

    /** List of normals encountered in the input. */
    private final List<Vector3D> normals = new ArrayList<>();

    /** Construct a new instance that reads OBJ content from the given reader.
     * @param reader reader to read from
     */
    public ObjFacetDefinitionReader(final Reader reader) {
        super(reader);
    }

    /** {@inheritDoc} */
    @Override
    public FacetDefinition readFacet() {
        final PolygonObjParser.Face face = readFace();
        if (face != null) {
            final List<Vector3D> faceVertices = face.getVertices(vertices::get);
            final Vector3D definedNormal = face.getDefinedCompositeNormal(normals::get);

            return new SimpleFacetDefinition(faceVertices, definedNormal);
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected void handleVertex(final Vector3D vertex) {
        vertices.add(vertex);
    }

    /** {@inheritDoc} */
    @Override
    protected void handleNormal(final Vector3D normal) {
        normals.add(normal);
    }
}
