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

import java.io.Closeable;
import java.io.IOException;

/** Interface for reading {@link FacetDefinition facet definitions} from an input source.
 * @see FacetDefinition
 */
public interface FacetDefinitionReader extends Closeable {

    /** Return the next facet definition from the input source or null if no more
     * facets are available.
     * @return the next facet definition or null if no more facets
     *      are available
     * @throws IOException if an I/O or data format error occurs
     */
    FacetDefinition readFacet() throws IOException;
}
