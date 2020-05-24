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
import java.util.Objects;

import org.apache.commons.geometry.core.RegionEmbedding;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.internal.HyperplaneSubsets;
import org.apache.commons.geometry.core.partitioning.HyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.HyperplaneSubset;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.spherical.oned.Point1S;

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
    public Point2S getBarycenter() {
        final Point1S subspaceBarycenter = getSubspaceRegion().getBarycenter();
        if (subspaceBarycenter != null) {
            return getCircle().toSpace(subspaceBarycenter);
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

    /** {@inheritDoc} */
    @Override
    public HyperplaneSubset.Builder<Point2S> builder() {
        return new Builder(circle);
    }

    /** Return the object used to perform floating point comparisons, which is the
     * same object used by the underlying {@link GreatCircle}.
     * @return precision object used to perform floating point comparisons.
     */
    public DoublePrecisionContext getPrecision() {
        return circle.getPrecision();
    }

    /** Internal implementation of the {@link HyperplaneSubset.Builder} interface. In cases where only a single
     * convex subset is given to the builder, this class returns the convex subset instance directly. In all other
     * cases, an {@link EmbeddedTreeGreatCircleSubset} is used to construct the final subset.
     */
    private static final class Builder implements HyperplaneSubset.Builder<Point2S> {
        /** Great circle that a subset is being constructed for. */
        private final GreatCircle circle;

        /** Embedded tree subset. */
        private EmbeddedTreeGreatCircleSubset treeSubset;

        /** Convex subset added as the first subset to the builder. This is returned directly if
         * no other subsets are added.
         */
        private GreatArc convexSubset;

        /** Create a new subset builder for the given great circle.
         * @param circle great circle to build a subset for
         */
        Builder(final GreatCircle circle) {
            this.circle = circle;
        }

        /** {@inheritDoc} */
        @Override
        public void add(final HyperplaneSubset<Point2S> sub) {
            addInternal(sub);
        }

        /** {@inheritDoc} */
        @Override
        public void add(final HyperplaneConvexSubset<Point2S> sub) {
            addInternal(sub);
        }

        /** {@inheritDoc} */
        @Override
        public GreatCircleSubset build() {
            // return the convex subset directly if that was all we were given
            if (convexSubset != null) {
                return convexSubset;
            }
            return getTreeSubset();
        }

        /** Internal method for adding hyperplane subsets to this builder.
         * @param sub the hyperplane subset to add; may be either convex or non-convex
         */
        private void addInternal(final HyperplaneSubset<Point2S> sub) {
            Objects.requireNonNull(sub, "Hyperplane subset must not be null");

            if (sub instanceof GreatArc) {
                addConvexSubset((GreatArc) sub);
            } else if (sub instanceof EmbeddedTreeGreatCircleSubset) {
                addTreeSubset((EmbeddedTreeGreatCircleSubset) sub);
            } else {
                throw new IllegalArgumentException("Unsupported hyperplane subset type: " + sub.getClass().getName());
            }
        }

        /** Add a convex subset to the builder.
         * @param convex convex subset to add
         */
        private void addConvexSubset(final GreatArc convex) {
            GreatCircles.validateGreatCirclesEquivalent(circle, convex.getCircle());

            if (treeSubset == null && convexSubset == null) {
                convexSubset = convex;
            } else {
                getTreeSubset().add(convex);
            }
        }

        /** Add an embedded tree subset to the builder.
         * @param tree embedded tree subset to add
         */
        private void addTreeSubset(final EmbeddedTreeGreatCircleSubset tree) {
            // no need to validate the line here since the add() method does that for us
            getTreeSubset().add(tree);
        }

        /** Get the tree subset for the builder, creating it if needed.
         * @return the tree subset for the builder
         */
        private EmbeddedTreeGreatCircleSubset getTreeSubset() {
            if (treeSubset == null) {
                treeSubset = new EmbeddedTreeGreatCircleSubset(circle);

                if (convexSubset != null) {
                    treeSubset.add(convexSubset);

                    convexSubset = null;
                }
            }

            return treeSubset;
        }
    }
}
