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

import org.apache.commons.geometry.euclidean.threed.ConvexPolygon3D;
import org.apache.commons.geometry.euclidean.threed.ConvexVolume;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.hull.ConvexHull;

/**
 * This class represents a convex hull in three-dimensional Euclidean space.
 */
public interface ConvexHull3D extends ConvexHull<Vector3D> {

    /**
     * {@inheritDoc}
     */
    @Override
    ConvexVolume getRegion();

    /**
     * Return a collection of all two-dimensional faces (called facets) of the
     * convex hull.
     *
     * @return a collection of all two-dimensional faces.
     */
    Collection<? extends ConvexPolygon3D> getFacets();

}
