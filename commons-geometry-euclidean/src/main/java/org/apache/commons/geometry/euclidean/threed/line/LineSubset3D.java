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
package org.apache.commons.geometry.euclidean.threed.line;

import org.apache.commons.geometry.core.RegionEmbedding;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Sized;
import org.apache.commons.geometry.core.partitioning.HyperplaneBoundedRegion;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.threed.Bounds3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

/** Class representing a subset of a line in 3D Euclidean space. For example, line segments,
 * rays, and disjoint combinations of the two are line subsets. Line subsets may be finite or infinite.
 */
public abstract class LineSubset3D implements RegionEmbedding<Vector3D, Vector1D>, Sized {
    /** The line containing this instance. */
    private final Line3D line;

    /** Construct a new instance based on the given line.
     * @param line line containing the instance
     */
    LineSubset3D(final Line3D line) {
        this.line = line;
    }

    /** Get the line containing this subset.
     * @return the line containing this subset
     */
    public Line3D getLine() {
        return line;
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D toSpace(final Vector1D pt) {
        return line.toSpace(pt);
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D toSubspace(final Vector3D pt) {
        return line.toSubspace(pt);
    }

    /** Get the centroid, or geometric center, of the line subset or {@code null} if
     * the subset is empty or infinite.
     * @return the centroid of the line subset, or {@code null} if the subset is empty or
     *      infinite
     */
    public abstract Vector3D getCentroid();

    /** Get the 3D bounding box of the line subset or {@code null} if the subset is
     * empty or infinite.
     * @return the 3D bounding box the line subset or {@code null} if the subset is
     *      empty or infinite
     */
    public abstract Bounds3D getBounds();

    /** Get the subspace region for the instance.
     * @return the subspace region for the instance
     */
    @Override
    public abstract HyperplaneBoundedRegion<Vector1D> getSubspaceRegion();

    /** Classify the given line abscissa value with respect to the subspace region.
     * @param abscissa the abscissa value to classify
     * @return the region location of the line abscissa value
     */
    public abstract RegionLocation classifyAbscissa(double abscissa);
}
