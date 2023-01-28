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

import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.hull.ConvexHull;
import org.apache.commons.geometry.hull.ConvexHullGenerator;

/**
 * Implementation of quick-hull algorithm by Barber, Dobkin and Huhdanpaa. The
 * algorithm constructs the convex hull of a given finite set of points.
 * Empirically, the number of points processed by Quickhull is proportional to
 * the number of vertices in the output. The algorithm runs on an input of size
 * n with r processed points in time O(n log r). We define a point of the given
 * set to be extreme, if and only if the point is part of the final hull. The
 * algorithm runs in multiple stages:
 * <ol>
 * <li>First we construct a simplex with extreme properties from the given point
 * set to maximize the possibility of choosing extreme points as initial simplex
 * vertices.</li>
 * <li>We partition all the remaining points into outside sets. Each polygon
 * face of the simplex defines a positive and negative half-space. A point can
 * be assigned to the outside set of the polygon if it is an element of the
 * positive half space.</li>
 * <li>For each polygon-face (facet) with a non empty outside set we choose a
 * point with maximal distance to the given facet.</li>
 * <li>We determine all the visible facets from the given outside point and find
 * a path around the horizon.</li>
 * <li>We construct a new cone of polygons from the edges of the horizon to the
 * outside point. All visible facets are removed and the points in the outside
 * sets of the visible facets are redistributed.</li>
 * <li>We repeat step 3-5 until each outside set is empty.</li>
 * </ol>
 */
public class QuickHull3D implements ConvexHullGenerator<Vector3D> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ConvexHull<Vector3D> generate(Collection<Vector3D> points) {
        return null;
    }
}
