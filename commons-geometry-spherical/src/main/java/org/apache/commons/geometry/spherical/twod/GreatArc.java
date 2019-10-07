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
package org.apache.commons.geometry.spherical.twod;

import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.spherical.oned.AngularInterval;

/** Class representing a single angular interval in a {@link GreatCircle}.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public final class GreatArc extends AbstractSubGreatCircle implements ConvexSubHyperplane<Point2S> {

    /** Serializable UID */
    private static final long serialVersionUID = 20191005L;

    /** The interval representing the region of the great circle contained in
     * the arc.
     */
    private final AngularInterval interval;

    /** Create a new instance from a great circle and the interval embedded in it.
     * @param greatCircle defining great circle instance
     * @param interval angular interval embedded in the great circle
     */
    private GreatArc(final GreatCircle greatCircle, final AngularInterval interval) {
        super(greatCircle);

        this.interval = interval;
    }

    /** Get the angular interval for the arc.
     * @return the angular interval for the arc
     * @see #getSubspaceRegion()
     */
    public AngularInterval getInterval() {
        return interval;
    }

    /** {@inheritDoc} */
    @Override
    public AngularInterval getSubspaceRegion() {
        return getInterval();
    }

    /** {@inheritDoc} */
    @Override
    public List<GreatArc> toConvex() {
        return Collections.singletonList(this);
    }

    /** {@inheritDoc} */
    @Override
    public GreatArc reverse() {
        return new GreatArc(getCircle().reverse(), interval);
    }

    /** {@inheritDoc} */
    @Override
    public Split<GreatArc> split(final Hyperplane<Point2S> splitter) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public GreatArc transform(final Transform<Point2S> transform) {
        return new GreatArc(getCircle().transform(transform), interval);
    }

    /** Construct an arc from a great circle and an angular interval.
     * @param circle circle defining the arc
     * @param interval interval representing the portion of the circle contained
     *      in the arc
     * @return an arc created from the given great circle and interval
     */
    public static GreatArc fromInterval(final GreatCircle circle, final AngularInterval interval) {
        return new GreatArc(circle, interval);
    }
}
