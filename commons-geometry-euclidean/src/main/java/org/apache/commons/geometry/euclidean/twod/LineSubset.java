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
package org.apache.commons.geometry.euclidean.twod;

import java.util.List;
import java.util.Objects;

import org.apache.commons.geometry.core.RegionEmbedding;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.HyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.HyperplaneSubset;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.Vector1D;

/** Class representing a subset of points on a line in 2D Euclidean space. For example, line segments
 * and rays are line subsets. Line subsets may be finite or infinite.
 */
public abstract class LineSubset implements HyperplaneSubset<Vector2D>, RegionEmbedding<Vector2D, Vector1D> {
    /** The line containing this instance. */
    private final Line line;

    /** Construct a new instance based on the given line.
     * @param line line forming the base of the instance
     */
    LineSubset(final Line line) {
        this.line = line;
    }

    /** Get the line containing this subset. This method is an alias
     * for {@link #getHyperplane()}.
     * @return the line containing this subset
     * @see #getHyperplane()
     */
    public Line getLine() {
        return getHyperplane();
    }

    /** {@inheritDoc} */
    @Override
    public Line getHyperplane() {
        return line;
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D toSubspace(final Vector2D pt) {
        return line.toSubspace(pt);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D toSpace(final Vector1D pt) {
        return line.toSpace(pt);
    }

    /** {@inheritDoc} */
    @Override
    public abstract List<LineConvexSubset> toConvex();

    /** Get a {@link Bounds2D} object defining an axis-aligned bounding box containing all
     * vertices for this subset. Null is returned if the subset is infinite or does not
     * contain any vertices.
     * @return the bounding box for this instance or null if no valid bounds could be determined
     */
    public abstract Bounds2D getBounds();

    /** {@inheritDoc} */
    @Override
    public abstract HyperplaneBoundedRegion<Vector1D> getSubspaceRegion();

    /** {@inheritDoc} */
    @Override
    public HyperplaneSubset.Builder<Vector2D> builder() {
        return new Builder(line);
    }

    /** {@inheritDoc} */
    @Override
    public RegionLocation classify(final Vector2D pt) {
        if (line.contains(pt)) {
            return classifyAbscissa(line.abscissa(pt));
        }

        return RegionLocation.OUTSIDE;
    }

    /** Get the unique intersection of this subset with the given line. Null is
     * returned if no unique intersection point exists (ie, the lines are
     * parallel or coincident) or the line does not intersect this instance.
     * @param inputLine line to intersect with this line subset
     * @return the unique intersection point between the line and this line subset
     *      or null if no such point exists.
     * @see Line#intersection(Line)
     */
    public Vector2D intersection(final Line inputLine) {
        final Vector2D pt = line.intersection(inputLine);
        return (pt != null && contains(pt)) ? pt : null;
    }

    /** Get the unique intersection of this instance with the given line subset. Null
     * is returned if the lines containing the line subsets do not have a unique intersection
     * point (ie, they are parallel or coincident) or the intersection point is unique
     * but is not contained in both line subsets.
     * @param subset line subset to intersect with
     * @return the unique intersection point between this line subset and the argument or
     *      null if no such point exists.
     * @see Line#intersection(Line)
     */
    public Vector2D intersection(final LineSubset subset) {
        final Vector2D pt = intersection(subset.getLine());
        return (pt != null && subset.contains(pt)) ? pt : null;
    }

    /** Return the object used to perform floating point comparisons, which is the
     * same object used by the underlying {@link Line}).
     * @return precision object used to perform floating point comparisons.
     */
    public DoublePrecisionContext getPrecision() {
        return line.getPrecision();
    }

    /** Classify the given line abscissa value with respect to the subspace region.
     * @param abscissa the abscissa value to classify
     * @return the region location of the line abscissa value
     */
    abstract RegionLocation classifyAbscissa(double abscissa);

    /** Get a split result for cases where no intersection exists between the splitting line and the
     * line underlying the given line subset. This occurs when the two lines are parallel or coincident.
     * @param <T> Line subset type
     * @param splitter splitting line
     * @param subset line subset instance being split
     * @return return result of the non-intersecting split operation
     */
    <T extends LineSubset> Split<T> getNonIntersectingSplitResult(final Line splitter, final T subset) {
        // check which side of the splitter we lie on
        final double offset = splitter.offset(subset.getLine());
        final int comp = getPrecision().compare(offset, 0.0);

        if (comp < 0) {
            return new Split<>(subset, null);
        } else if (comp > 0) {
            return new Split<>(null, subset);
        } else {
            return new Split<>(null, null);
        }
    }

    /** Return true if the plus side of the given splitter line is facing in the positive direction
     * of this line.
     * @param splitterLine line splitting this instance
     * @return true if the plus side of the given line is facing in the positive direction of this
     *      line
     */
    boolean splitterPlusIsPositiveFacing(final Line splitterLine) {
        return line.getOffsetDirection().dot(splitterLine.getDirection()) <= 0;
    }

    /** Create a split result for the given splitter line, given the low and high split portion of this
     * instance. The arguments are assigned to the split result's minus and plus properties based on the
     * relative orientation of the splitter line.
     * @param <T> Line subset type
     * @param splitter splitter line
     * @param low portion of the split result closest to negative infinity on this line
     * @param high portion of th split result closest to positive infinity on this line
     * @return a split result for the given splitter line.
     */
    <T extends LineSubset> Split<T> createSplitResult(final Line splitter, final T low, final T high) {
        return splitterPlusIsPositiveFacing(splitter) ?
                new Split<>(low, high) :
                new Split<>(high, low);
    }

    /** Internal implementation of the {@link HyperplaneSubset.Builder} interface. In cases where only a single
     * convex subset is given to the builder, this class returns the convex subset instance directly. In all other
     * cases, an {@link EmbeddedTreeLineSubset} is used to construct the final subset.
     */
    private static final class Builder implements HyperplaneSubset.Builder<Vector2D> {
        /** Line that a subset is being constructed for. */
        private final Line line;

        /** Embedded tree subset. */
        private EmbeddedTreeLineSubset treeSubset;

        /** Convex subset added as the first subset to the builder. This is returned directly if
         * no other subsets are added.
         */
        private LineConvexSubset convexSubset;

        /** Create a new subset builder for the given line.
         * @param line line to build a subset for
         */
        Builder(final Line line) {
            this.line = line;
        }

        /** {@inheritDoc} */
        @Override
        public void add(final HyperplaneSubset<Vector2D> sub) {
            addInternal(sub);
        }

        /** {@inheritDoc} */
        @Override
        public void add(final HyperplaneConvexSubset<Vector2D> sub) {
            addInternal(sub);
        }

        /** {@inheritDoc} */
        @Override
        public LineSubset build() {
            // return the convex subset directly if that was all we were given
            if (convexSubset != null) {
                return convexSubset;
            }
            return getTreeSubset();
        }

        /** Internal method for adding hyperplane subsets to this builder.
         * @param sub the hyperplane subset to add; may be either convex or non-convex
         */
        private void addInternal(final HyperplaneSubset<Vector2D> sub) {
            Objects.requireNonNull(sub, "Hyperplane subset must not be null");

            if (sub instanceof LineConvexSubset) {
                addConvexSubset((LineConvexSubset) sub);
            } else if (sub instanceof EmbeddedTreeLineSubset) {
                addTreeSubset((EmbeddedTreeLineSubset) sub);
            } else {
                throw new IllegalArgumentException("Unsupported hyperplane subset type: " + sub.getClass().getName());
            }
        }

        /** Add a convex subset to the builder.
         * @param convex convex subset to add
         */
        private void addConvexSubset(final LineConvexSubset convex) {
            Lines.validateLinesEquivalent(line, convex.getLine());

            if (treeSubset == null && convexSubset == null) {
                convexSubset = convex;
            } else {
                getTreeSubset().add(convex);
            }
        }

        /** Add an embedded tree subset to the builder.
         * @param tree embedded tree subset to add
         */
        private void addTreeSubset(final EmbeddedTreeLineSubset tree) {
            // no need to validate the line here since the add() method does that for us
            getTreeSubset().add(tree);
        }

        /** Get the tree subset for the builder, creating it if needed.
         * @return the tree subset for the builder
         */
        private EmbeddedTreeLineSubset getTreeSubset() {
            if (treeSubset == null) {
                treeSubset = new EmbeddedTreeLineSubset(line);

                if (convexSubset != null) {
                    treeSubset.add(convexSubset);

                    convexSubset = null;
                }
            }

            return treeSubset;
        }
    }
}
