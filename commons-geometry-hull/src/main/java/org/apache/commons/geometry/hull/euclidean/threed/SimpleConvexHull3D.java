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
package org.apache.commons.geometry.hull.euclidean.threed;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.euclidean.threed.ConvexPolygon3D;
import org.apache.commons.geometry.euclidean.threed.ConvexVolume;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

/**
 * This class represents a convex hull in three dimensions.
 */
public class SimpleConvexHull3D implements ConvexHull3D {

    /**
     * The vertices of the convex hull.
     */
    private List<Vector3D> vertices;

    /**
     * The region defined by the hull.
     */
    private ConvexVolume region;

    /**
     * A collection of all facets that form the convex volume of the hull.
     */
    private Collection<ConvexPolygon3D> facets;

    /**
     * Simple constructor. No validation is performed.
     *
     * @param facets the facets of the hull.s
     */
    SimpleConvexHull3D(Collection<? extends ConvexPolygon3D> facets) {
        vertices = Collections
                .unmodifiableList(facets.stream().flatMap(f -> f.getVertices().stream()).collect(Collectors.toList()));
        region = ConvexVolume.fromBounds(() -> facets.stream().map(ConvexPolygon3D::getPlane).iterator());
        this.facets = Collections.unmodifiableSet(new HashSet<>(facets));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Vector3D> getVertices() {
        return vertices;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConvexVolume getRegion() {
        return region;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<? extends ConvexPolygon3D> getFacets() {
        return facets;
    }

}
