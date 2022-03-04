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

import org.apache.commons.geometry.core.internal.AbstractBucketPointMap;
import org.apache.commons.geometry.euclidean.twod.PointMap2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.core.Precision;

/** Internal {@link PointMap2D} implementation.
 * @param <V> Map value type
 */
final class PointMap2DImpl<V>
    extends AbstractBucketPointMap<Vector2D, V>
    implements PointMap2D<V> {

    /** Number of children per node. */
    private static final int NODE_CHILD_COUNT = 4;

    /** Max entries per node. */
    private static final int MAX_ENTRIES_PER_NODE = 16;

    /** X negative quadrant flag. */
    private static final int XNEG = 1 << 3;

    /** X postive quadrant flag. */
    private static final int XPOS = 1 << 2;

    /** Y negative quadrant flag. */
    private static final int YNEG = 1 << 1;

    /** Y positive quadrant flag. */
    private static final int YPOS = 1;

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

    /** Tree node class for {@link PointMap2DImpl}.
     * @param <V> Map value type
     */
    private static final class MapNode2D<V> extends BucketNode<Vector2D, V> {

        /** Point to split child spaces; will be null for leaf nodes. */
        private Vector2D split;

        /** Construct a new instance.
         * @param map owning map
         * @param parent parent node; set to null for the root node
         */
        MapNode2D(final AbstractBucketPointMap<Vector2D, V> map,
                final BucketNode<Vector2D, V> parent) {
            super(map, parent);
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
    }
}
