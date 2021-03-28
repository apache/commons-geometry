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
package org.apache.commons.geometry.io.euclidean.threed;

import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.io.core.GeometryFormat;

/** Enum containing 3D geometry formats supported internally by Apache Commons Geometry.
 */
public enum GeometryFormat3D implements GeometryFormat {

    /** Value representing the OBJ file format.
     * @see <a href="https://en.wikipedia.org/wiki/Wavefront_.obj_file">Wavefront .obj file</a>
     */
    OBJ("obj"),

    /** Value representing the STL file format in both the text (i.e. "ASCII") and binary forms.
     * @see <a href="https://en.wikipedia.org/wiki/STL_(file_format)">STL</a>
     */
    STL("stl"),

    /** Value representing a simple, <em>non-standard</em> text geometry format that defines facets one per line
     * by listing the coordinates of the facet vertices in order, separated by non-numeric characters (e.g. whitespace,
     * commas, semicolons, etc). Each line follows the pattern
     * <p>
     * <code>
     *      p1<sub>x</sub> p1<sub>y</sub> p1<sub>z</sub> p2<sub>x</sub> p2<sub>y</sub> p2<sub>z</sub> p3<sub>x</sub> p3<sub>y</sub> p3<sub>z</sub> ...
     * </code>
     * </p>
     * <p>where the <em>p1</em> elements contain the coordinates of the first facet vertex,
     * <em>p2</em> those of the second, and so on. Facets may have 3 or more vertices and do not need to all have
     * the same number of vertices.
     *
     * <p>This format is non-standard and no guarantees are made regarding its compatibility with other systems.
     * It is intended primarily to provide a convenient, human-readable format for data input and analysis.</p>
     * @see org.apache.commons.geometry.io.euclidean.threed.txt.TextFacetDefinitionReader
     * @see org.apache.commons.geometry.io.euclidean.threed.txt.TextFacetDefinitionWriter
     */
    TXT("txt"),

    /** Value representing a simple, <em>non-standard</em> CSV geometry format that defines triangular facets
     * one per line by listing the facet vertex coordinates in order, separated by commas. This format is a subset
     * of the {@link #TXT} format with commas as separators and facets written as triangles (to ensure that
     * all rows have the same number of columns).
     *
     * <p>This format is non-standard and no guarantees are made regarding its compatibility with other systems.
     * It is intended primarily to provide a convenient, human-readable format for data input and analysis.</p>
     * @see org.apache.commons.geometry.io.euclidean.threed.txt.TextFacetDefinitionWriter#csvFormat(java.io.Writer)
     */
    CSV("csv");

    /** List of file extensions associated with the format. The first file extension
     * listed is taken as the default.
     */
    private final List<String> fileExtensions;

    /** Construct a new instance with the given file extension.
     * @param fileExt file extension
     */
    GeometryFormat3D(final String fileExt) {
        this.fileExtensions = Collections.singletonList(fileExt);
    }

    /** {@inheritDoc} */
    @Override
    public String getFormatName() {
        return name();
    }

    /** {@inheritDoc} */
    @Override
    public String getDefaultFileExtension() {
        return fileExtensions.get(0);
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getFileExtensions() {
        return fileExtensions;
    }
}
