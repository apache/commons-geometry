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

import org.apache.commons.geometry.core.partitioning.AbstractEmbeddingSubHyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.spherical.oned.Point1S;
import org.apache.commons.geometry.spherical.twod.SubGreatCircle.SubGreatCircleBuilder;

/** Abstract base class for great circle subhyperplane implementations.
 */
abstract class AbstractSubGreatCircle
    extends AbstractEmbeddingSubHyperplane<Point2S, Point1S, GreatCircle> {

    /** Serializable UID */
    private static final long serialVersionUID = 20191005L;

    /** The great circle defining this instance. */
    private final GreatCircle circle;

    /** Simple constructor.
     * @param circle great circle defining this instance
     */
    AbstractSubGreatCircle(final GreatCircle circle) {
        this.circle = circle;
    }

    /** Get the great circle defining this instance.
     * @return the great circle defining this instance
     * @see #getHyperplane()
     */
    public GreatCircle getCircle() {
        return circle;
    }

    /** {@inheritDoc} */
    @Override
    public GreatCircle getHyperplane() {
        return circle;
    }

    /** {@inheritDoc} */
    @Override
    public SubGreatCircleBuilder builder() {
        return new SubGreatCircleBuilder(circle);
    }

    /** Return the object used to perform floating point comparisons, which is the
     * same object used by the underlying {@link GreatCircle).
     * @return precision object used to perform floating point comparisons.
     */
    public DoublePrecisionContext getPrecision() {
        return circle.getPrecision();
    }
}
