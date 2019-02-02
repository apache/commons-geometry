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

import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** This class represents a 1D oriented hyperplane on the circle.
 * <p>An hyperplane on the 1-sphere is an angle with an orientation.</p>
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public class LimitAngle implements Hyperplane<S1Point> {

    /** Angle location. */
    private final S1Point location;

    /** Orientation. */
    private final boolean direct;

    /** Precision context used to compare floating point numbers. */
    private final DoublePrecisionContext precision;

    /** Simple constructor.
     * @param location location of the hyperplane
     * @param direct if true, the plus side of the hyperplane is towards
     * angles greater than {@code location}
     * @param precision precision context used to compare floating point values
     */
    public LimitAngle(final S1Point location, final boolean direct, final DoublePrecisionContext precision) {
        this.location  = location;
        this.direct    = direct;
        this.precision = precision;
    }

    /** Copy the instance.
     * <p>Since instances are immutable, this method directly returns
     * the instance.</p>
     * @return the instance itself
     */
    @Override
    public LimitAngle copySelf() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public double getOffset(final S1Point point) {
        final double delta = point.getAzimuth() - location.getAzimuth();
        return direct ? delta : -delta;
    }

    /** Check if the hyperplane orientation is direct.
     * @return true if the plus side of the hyperplane is towards
     * angles greater than hyperplane location
     */
    public boolean isDirect() {
        return direct;
    }

    /** Get the reverse of the instance.
     * <p>Get a limit angle with reversed orientation with respect to the
     * instance. A new object is built, the instance is untouched.</p>
     * @return a new limit angle, with orientation opposite to the instance orientation
     */
    public LimitAngle getReverse() {
        return new LimitAngle(location, !direct, precision);
    }

    /** Build a region covering the whole hyperplane.
     * <p>Since this class represent zero dimension spaces which does
     * not have lower dimension sub-spaces, this method returns a dummy
     * implementation of a {@link
     * org.apache.commons.geometry.core.partitioning.SubHyperplane SubHyperplane}.
     * This implementation is only used to allow the {@link
     * org.apache.commons.geometry.core.partitioning.SubHyperplane
     * SubHyperplane} class implementation to work properly, it should
     * <em>not</em> be used otherwise.</p>
     * @return a dummy sub hyperplane
     */
    @Override
    public SubLimitAngle wholeHyperplane() {
        return new SubLimitAngle(this, null);
    }

    /** Build a region covering the whole space.
     * @return a region containing the instance (really an {@link
     * ArcsSet IntervalsSet} instance)
     */
    @Override
    public ArcsSet wholeSpace() {
        return new ArcsSet(precision);
    }

    /** {@inheritDoc} */
    @Override
    public boolean sameOrientationAs(final Hyperplane<S1Point> other) {
        return !(direct ^ ((LimitAngle) other).direct);
    }

    /** Get the hyperplane location on the circle.
     * @return the hyperplane location
     */
    public S1Point getLocation() {
        return location;
    }

    /** {@inheritDoc} */
    @Override
    public S1Point project(S1Point point) {
        return location;
    }

    /** {@inheritDoc} */
    @Override
    public DoublePrecisionContext getPrecision() {
        return precision;
    }

}
