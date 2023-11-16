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
package org.apache.commons.geometry.euclidean;

import java.util.List;

import org.apache.commons.geometry.core.collection.PointMap;
import org.apache.commons.geometry.core.internal.AbstractBucketPointMap;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.core.Precision;

/** Internal {@link PointMap} implementation for Euclidean 2D space.
 * @param <V> Map value type
 */
final class PointMap2DImpl<V>
    extends AbstractBucketPointMap<Vector2D, V>
    implements PointMap<Vector2D, V> {

    /** Number of children per node. */
    private static final int NODE_CHILD_COUNT = 4;

    /** Max entries per node. */
    private static final int MAX_ENTRIES_PER_NODE = 16;

    /** X negative quadrant flag. */
    private static final int XNEG = 1 << 3;

    /** X positive quadrant flag. */
    private static final int XPOS = 1 << 2;

    /** Y negative quadrant flag. */
    private static final int YNEG = 1 << 1;

    /** Y positive quadrant flag. */
    private static final int YPOS = 1;

    /** Bit mask for x location. */
    private static final int XMASK = XNEG | XPOS;

    /** Bit mask for y location. */
    private static final int YMASK = YNEG | YPOS;

    /** Quadtree location flags for child nodes. */
    private static final int[] CHILD_LOCATIONS = {
        XNEG | YNEG,
        XNEG | YPOS,
        XPOS | YNEG,
        XPOS | YPOS
    };

    /** Construct a new instance using the given precision context to determine
     * floating point equality.
     * @param precision precision context
     */
    PointMap2DImpl(final Precision.DoubleEquivalence precision) {
        super(MapNode2D::new,
                MAX_ENTRIES_PER_NODE,
                NODE_CHILD_COUNT,
                precision);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean pointsEq(final Vector2D a, final Vector2D b) {
        return a.eq(b, getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    protected int disambiguatePointComparison(final Vector2D a, final Vector2D b) {
        return Vector2D.COORDINATE_ASCENDING_ORDER.compare(a, b);
    }

    /** Tree node class for {@link PointMap2DImpl}.
     * @param <V> Map value type
     */
    private static final class MapNode2D<V> extends BucketNode<Vector2D, V> {

        /** Point to split child spaces; will be null for leaf nodes. */
        private Vector2D split;

        /** Construct a new instance.
         * @param map owning map
         * @param parent parent node; set to null for the root node
         * @param childIndex index of this node in its parent's child list;
         *      set to {@code -1} for the root node
         */
        MapNode2D(final AbstractBucketPointMap<Vector2D, V> map,
                final BucketNode<Vector2D, V> parent,
                final int childIndex) {
            super(map, parent, childIndex);
        }

        /** {@inheritDoc} */
        @Override
        protected int getSearchLocation(final Vector2D pt) {
            final Precision.DoubleEquivalence precision = getPrecision();

            int loc = getSearchLocationValue(
                    precision.compare(pt.getX(), split.getX()),
                    XNEG,
                    XPOS);
            loc |= getSearchLocationValue(
                    precision.compare(pt.getY(), split.getY()),
                    YNEG,
                    YPOS);

            return loc;
        }

        /** {@inheritDoc} */
        @Override
        protected int getInsertLocation(final Vector2D pt) {
            int loc = getInsertLocationValue(
                    Double.compare(pt.getX(), split.getX()),
                    XNEG,
                    XPOS);
            loc |= getInsertLocationValue(
                    Double.compare(pt.getY(), split.getY()),
                    YNEG,
                    YPOS);

            return loc;
        }

        /** {@inheritDoc} */
        @Override
        protected void computeSplit() {
            final Vector2D.Sum sum = Vector2D.Sum.create();
            for (final Entry<Vector2D, V> entry : this) {
                sum.add(entry.getKey());
            }

            split = sum.get().multiply(1.0 / MAX_ENTRIES_PER_NODE);
        }

        /** {@inheritDoc} */
        @Override
        protected boolean testChildLocation(final int childIdx, final int loc) {
            final int childLoc = CHILD_LOCATIONS[childIdx];
            return (childLoc & loc) == childLoc;
        }

        /** {@inheritDoc} */
        @Override
        protected void makeLeaf(final List<Entry<Vector2D, V>> leafEntries) {
            super.makeLeaf(leafEntries);

            split = null;
        }

        /** {@inheritDoc} */
        @Override
        protected double getMinChildDistance(final int childIdx, final Vector2D pt, final int ptLoc) {
            final int childLoc = CHILD_LOCATIONS[childIdx];

            final boolean sameX = (ptLoc & XMASK) == (childLoc & XMASK);
            final boolean sameY = (ptLoc & YMASK) == (childLoc & YMASK);

            final Vector2D diff = pt.subtract(split);

            if (sameX) {
                return sameY ?
                        0d :
                        Math.abs(diff.getY());
            } else if (sameY) {
                return Math.abs(diff.getX());
            }

            return diff.norm();
        }

        /** {@inheritDoc} */
        @Override
        protected double getMaxChildDistance(final int childIdx, final Vector2D pt, final int ptLoc) {
            final MapNode2D<V> grandParent = (MapNode2D<V>) getParent();
            if (grandParent != null) {
                final int nodeLoc = CHILD_LOCATIONS[getChildIndex()];
                final int childLoc = CHILD_LOCATIONS[childIdx];

                final boolean oppositeX = (nodeLoc & XMASK) != (childLoc & XMASK);
                final boolean oppositeY = (nodeLoc & YMASK) != (childLoc & YMASK);

                if (oppositeX && oppositeY) {
                    // the grandparent and parent splits form a completely enclosed region,
                    // meaning that we can determine a max distance
                    final Vector2D diff = Vector2D.of(
                                getMaxDistance(pt.getX(), grandParent.split.getX(), split.getX()),
                                getMaxDistance(pt.getY(), grandParent.split.getY(), split.getY())
                            );

                    return diff.norm();
                }
            }

            return Double.POSITIVE_INFINITY;
        }
    }
}
