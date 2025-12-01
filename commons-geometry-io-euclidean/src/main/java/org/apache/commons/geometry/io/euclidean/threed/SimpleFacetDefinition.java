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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.geometry.euclidean.internal.EuclideanUtils;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

/** Simple {@link FacetDefinition} implementation that stores a list of vertices and
 * optional normal.
 */
public class SimpleFacetDefinition implements FacetDefinition {

    /** Facet vertices. */
    private final List<Vector3D> vertices;

    /** Facet normal; may be null. */
    private final Vector3D normal;

    /** Construct a new instance with the given vertices and no defined normal.
     * @param vertices facet vertices
     * @throws IllegalArgumentException if {@code vertices} contains fewer than 3 elements
     */
    public SimpleFacetDefinition(final List<Vector3D> vertices) {
        this(checkVertices(vertices), null, false);
    }

    /** Construct a new instance with the given vertices and normal.
     * @param vertices facet vertices
     * @param normal facet normal; may be null
     * @throws IllegalArgumentException if {@code vertices} contains fewer than 3 elements
     */
    public SimpleFacetDefinition(final List<Vector3D> vertices, final Vector3D normal) {
        this(checkVertices(vertices), normal, false);
    }

    /** Private constructor.
     * @param vertices facet vertices
     * @param normal facet normal; may be null
     * @param ignored Ignored parameter.
     */
    private SimpleFacetDefinition(final List<Vector3D> vertices, final Vector3D normal, boolean ignored) {
        this.vertices = Collections.unmodifiableList(vertices);
        this.normal = normal;
    }

    /**
     * Check the vertices.
     *
     * @param vertices the vertices
     * @return the vertices
     * @throws IllegalArgumentException if {@code vertices} contains fewer than 3 elements
     */
    private static List<Vector3D> checkVertices(final List<Vector3D> vertices) {
        Objects.requireNonNull(vertices, "Facet vertex list cannot be null");
        if (vertices.size() < EuclideanUtils.TRIANGLE_VERTEX_COUNT) {
            throw new IllegalArgumentException("Facet vertex list must contain at least " +
                    EuclideanUtils.TRIANGLE_VERTEX_COUNT + " points; found " + vertices.size());
        }
        return vertices;
    }

    /** {@inheritDoc} */
    @Override
    public List<Vector3D> getVertices() {
        return vertices;
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D getNormal() {
        return normal;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(256);
        sb.append(getClass().getSimpleName())
            .append("[vertices= ")
            .append(getVertices())
            .append(", normal= ")
            .append(getNormal())
            .append(']');

        return sb.toString();
    }
}
