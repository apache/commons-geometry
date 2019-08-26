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
package org.apache.commons.geometry.spherical.oned;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.ConvexHyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** Class representing an angular interval. The interval is defined by two azimuth angles: a min and
 * a max. The interval starts at the min azimuth angle and contains all points in the direction of
 * increasing azimuth angles up to max.
 *
 * <p>This class is guaranteed to be immutable.</p>
 */
public class AngularInterval implements ConvexHyperplaneBoundedRegion<Point1S>, Serializable {

    /** Serializable UID */
    private static final long serialVersionUID = 20190817L;

    /** Interval instance representing the full space. */
    private static final AngularInterval FULL = new AngularInterval(null, null);

    /** The minimum boundary of the interval. */
    private final OrientedPoint1S minBoundary;

    /** The maximum boundary of the interval. */
    private final OrientedPoint1S maxBoundary;

    /** Construct a new instance representing the angular region between the given
     * min and max azimuth boundaries. Both boundaries must either be finite or null
     * (to indicate the full space). No validation is performed.
     * @param minBoundary minimum boundary for the interval
     * @param maxBoundary maximum boundary for the interval
     */
    private AngularInterval(final OrientedPoint1S minBoundary, final OrientedPoint1S maxBoundary) {
        this.minBoundary = minBoundary;
        this.maxBoundary = maxBoundary;
    }

    /** Get the minimum azimuth angle for the interval. This value will
     * be {@link Double#NEGATIVE_INFINITY} if the interval represents the
     * full space.
     * @return the minimum azimuth angle for the interval or {@link Double#NEGATIVE_INFINITY}
     *      if the interval represents the full space.
     */
    public double getMin() {
        return minBoundary != null ?
                minBoundary.getAzimuth() :
                Double.NEGATIVE_INFINITY;
    }

    /** Get the minimum boundary for the interval. This will be null if the
     * interval represents the full space.
     * @return the minimum point for the interval or null if
     *      the interval represents the full space
     */
    public OrientedPoint1S getMinBoundary() {
        return minBoundary;
    }

    /** Get the maximum azimuth angle for the interval. This value will
     * be {@link Double#POSITIVE_INFINITY} if the interval represents the
     * full space.
     * @return the maximum azimuth angle for the interval or {@link Double#POSITIVE_INFINITY}
     *      if the interval represents the full space.
     */
    public double getMax() {
        return maxBoundary != null ?
                maxBoundary.getAzimuth() :
                Double.POSITIVE_INFINITY;
    }

    /** Get the maximum point for the interval. This will be null if the
     * interval represents the full space.
     * @return the maximum point for the interval or null if
     *      the interval represents the full space
     */
    public OrientedPoint1S getMaxBoundary() {
        return maxBoundary;
    }

    /** {@inheritDoc} */
    @Override
    public List<AngularInterval> toConvex() {
        return Arrays.asList(this);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFull() {
        return minBoundary == null;
    }

    /** {@inheritDoc}
     *
     * <p>This method always returns false.</p>
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        return getMax() - getMin();
    }

    /** {@inheritDoc}
     *
     * <p>This method simply returns 0 because boundaries in one dimension do not
     *  have any size.</p>
     */
    @Override
    public double getBoundarySize() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public Point1S getBarycenter() {
        if (!isFull()) {
            return Point1S.of(maxBoundary.getAzimuth() - minBoundary.getAzimuth());
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public RegionLocation classify(final Point1S pt) {
        if (!isFull()) {
            // TODO
        }
        return RegionLocation.INSIDE;
    }

    /** {@inheritDoc} */
    @Override
    public Point1S project(final Point1S pt) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public AngularInterval transform(final Transform<Point1S> transform) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Split<AngularInterval> split(final Hyperplane<Point1S> splitter) {
        // TODO Auto-generated method stub
        return null;
    }

    /** Return an instance representing the full space. The returned instance contains all
     * possible azimuth angles.
     * @return an interval representing the full space
     */
    public static AngularInterval full() {
        return FULL;
    }

    public static AngularInterval of(final double min, final double max, final DoublePrecisionContext precision) {
        return null;
    }
}
