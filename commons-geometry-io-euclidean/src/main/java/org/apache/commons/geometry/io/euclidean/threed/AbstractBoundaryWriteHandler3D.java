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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.stream.Stream;

import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;

/** Abstract base class for {@link BoundaryWriteHandler3D} implementations.
 */
public abstract class AbstractBoundaryWriteHandler3D implements BoundaryWriteHandler3D {

    /** {@inheritDoc} */
    @Override
    public void write(final BoundarySource3D src, final OutputStream out)
            throws IOException {
        try (Stream<PlaneConvexSubset> stream = src.boundaryStream()) {
            write(stream, out);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void writeFacets(final Collection<? extends FacetDefinition> facets, final OutputStream out)
            throws IOException {
        writeFacets(facets.stream(), out);
    }
}
