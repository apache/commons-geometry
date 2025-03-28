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
package org.apache.commons.geometry.io.euclidean.threed.txt;

import org.apache.commons.geometry.io.core.GeometryFormat;
import org.apache.commons.geometry.io.core.output.GeometryOutput;
import org.apache.commons.geometry.io.euclidean.threed.GeometryFormat3D;

/** {@link org.apache.commons.geometry.io.euclidean.threed.BoundaryWriteHandler3D BoundaryWriteHandler3D}
 * implementation for the non-standard {@link GeometryFormat3D#CSV CSV} format.
 */
public class CsvBoundaryWriteHandler3D extends AbstractTextBoundaryWriteHandler3D {

    /** Create an instance. */
    public CsvBoundaryWriteHandler3D() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public GeometryFormat getFormat() {
        return GeometryFormat3D.CSV;
    }

    /** {@inheritDoc} */
    @Override
    protected TextFacetDefinitionWriter getFacetDefinitionWriter(final GeometryOutput out) {
        final TextFacetDefinitionWriter facetWriter = super.getFacetDefinitionWriter(out);

        facetWriter.setVertexComponentSeparator(TextFacetDefinitionWriter.CSV_SEPARATOR);
        facetWriter.setVertexSeparator(TextFacetDefinitionWriter.CSV_SEPARATOR);
        facetWriter.setFacetVertexCount(TextFacetDefinitionWriter.CSV_FACET_VERTEX_COUNT);

        return facetWriter;
    }
}
