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
package org.apache.commons.geometry.euclidean.threed.line;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

/** Class representing a convex subset of a line in 3D Euclidean space. Instances
 * need not be finite, in which case the start or end point (or both) will be null.
 * @see Lines3D
 */
public abstract class LineConvexSubset3D extends LineSubset3D {

    /** Construct a new instance for the given line.
     * @param line line containing this convex subset
     */
    LineConvexSubset3D(final Line3D line) {
        super(line);
    }

    /** Get the start point for the line subset.
     * @return the start point for the line subset, or null if no start point exists
     */
    public abstract Vector3D getStartPoint();

    /** Get the 1D start location of the line subset or {@link Double#NEGATIVE_INFINITY} if
     * no start location exists.
     * @return the 1D start location of the line subset or {@link Double#NEGATIVE_INFINITY} if
     *      no start location exists.
     */
    public abstract double getSubspaceStart();

    /** Get the end point for the line subset.
     * @return the end point for the line subset, or null if no end point exists.
     */
    public abstract Vector3D getEndPoint();

    /** Get the 1D end location of the line subset or {@link Double#POSITIVE_INFINITY} if
     * no end location exists.
     * @return the 1D end location of the line subset or {@link Double#POSITIVE_INFINITY} if
     *      no end location exists
     */
    public abstract double getSubspaceEnd();

    /** {@inheritDoc} */
    @Override
    public Interval getSubspaceRegion() {
        final double start = getSubspaceStart();
        final double end = getSubspaceEnd();

        return Interval.of(start, end, getLine().getPrecision());
    }

    /** Get the 1D interval for the line subset. This method is an alias for {@link #getSubspaceRegion()}.
     * @return the 1D interval for the line subset.
     */
    public Interval getInterval() {
        return getSubspaceRegion();
    }

    /** Return true if the given point lies in the line subset.
     * @param pt point to check
     * @return true if the point lies in the line subset
     */
    public boolean contains(final Vector3D pt) {
        final Line3D line = getLine();
        return line.contains(pt) && classifyAbscissa(line.abscissa(pt)) != RegionLocation.OUTSIDE;
    }

    /** Transform this instance.
     * @param transform the transform to apply
     * @return a new, transformed instance
     */
    public abstract LineConvexSubset3D transform(Transform<Vector3D> transform);
}
