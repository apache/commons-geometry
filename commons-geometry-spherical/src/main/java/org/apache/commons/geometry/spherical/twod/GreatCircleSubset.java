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

import java.util.List;

import org.apache.commons.geometry.core.RegionEmbedding;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.internal.HyperplaneSubsets;
import org.apache.commons.geometry.core.partitioning.HyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.HyperplaneSubset;
import org.apache.commons.geometry.spherical.oned.Point1S;
import org.apache.commons.numbers.core.Precision;

/** Class representing a subset of the points in a great circle.
 * @see GreatCircles
 */
public abstract class GreatCircleSubset implements HyperplaneSubset<Point2S>, RegionEmbedding<Point2S, Point1S> {
    /** The great circle defining this instance. */
    private final GreatCircle circle;

    /** Simple constructor.
     * @param circle great circle defining this instance
     */
    GreatCircleSubset(final GreatCircle circle) {
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
        return getCircle();
    }

    /** {@inheritDoc} */
    @Override
    public Point1S toSubspace(final Point2S pt) {
        return circle.toSubspace(pt);
    }

    /** {@inheritDoc} */
    @Override
    public Point2S toSpace(final Point1S pt) {
        return circle.toSpace(pt);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFull() {
        return getSubspaceRegion().isFull();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return getSubspaceRegion().isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        return getSubspaceRegion().getSize();
    }

    /** {@inheritDoc} */
    @Override
    public Point2S getCentroid() {
        final Point1S subspaceCentroid = getSubspaceRegion().getCentroid();
        if (subspaceCentroid != null) {
            return getCircle().toSpace(subspaceCentroid);
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public RegionLocation classify(final Point2S pt) {
        return HyperplaneSubsets.classifyAgainstEmbeddedRegion(pt, circle, getSubspaceRegion());
    }

    /** {@inheritDoc} */
    @Override
    public Point2S closest(final Point2S pt) {
        return HyperplaneSubsets.closestToEmbeddedRegion(pt, circle, getSubspaceRegion());
    }

    /** {@inheritDoc} */
    @Override
    public abstract List<GreatArc> toConvex();

    /** {@inheritDoc} */
    @Override
    public abstract HyperplaneBoundedRegion<Point1S> getSubspaceRegion();

    /** Return the object used to perform floating point comparisons, which is the
     * same object used by the underlying {@link GreatCircle}.
     * @return precision object used to perform floating point comparisons.
     */
    public Precision.DoubleEquivalence getPrecision() {
        return circle.getPrecision();
    }
}
