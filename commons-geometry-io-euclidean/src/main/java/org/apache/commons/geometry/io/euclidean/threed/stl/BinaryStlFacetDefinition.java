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
package org.apache.commons.geometry.io.euclidean.threed.stl;

import java.util.List;

import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.io.euclidean.threed.SimpleFacetDefinition;

/** Facet definition class that provides access to the 2-byte attribute value
 * stored with each triangle in the binary STL format.
 */
public class BinaryStlFacetDefinition extends SimpleFacetDefinition {

    /** Attribute value for the facet (2 bytes). */
    private final int attributeValue;

    /** Construct a new instance.
     * @param vertices facet vertices
     * @param normal facet normal
     * @param attributeValue 2-byte attribute value
     */
    public BinaryStlFacetDefinition(final List<Vector3D> vertices, final Vector3D normal,
            final int attributeValue) {
        super(vertices, normal);
        this.attributeValue = attributeValue;
    }

    /** Get the 2-byte attribute value (known as the "attribute byte count") stored at the end of the STL
     * facet definition binary representation. This value is typically set to zero but non-standard implementations
     * may choose to store other values here.
     *
     * <p>The bytes are stored with the first byte in the upper portion (bits 8-15) of the int and the second byte
     * in the lower portion (bits 0-7).</p>
     * @return 2-byte attribute value for the facet
     */
    public int getAttributeValue() {
        return attributeValue;
    }
}
