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

import org.apache.commons.geometry.euclidean.internal.AbstractMultiDimensionalPointMap;
import org.apache.commons.numbers.core.Precision;

/** Internal {@link PointMap2D} implementation.
 * @param <V> Map value type
 */
final class PointMap2DImpl<V>
    extends AbstractMultiDimensionalPointMap<Vector2D, V>
    implements PointMap2D<V> {

    /** Number of children per node. */
    private static final int NODE_CHILD_COUNT = 4;

    /** X negative quadrant flag. */
    private static final int XNEG = 1 << 3;

    /** X postive quadrant flag. */
    private static final int XPOS = 1 << 2;

    /** Y negative quadrant flag. */
    private static final int YNEG = 1 << 1;

    /** Y positive quadrant flag. */
    private static final int YPOS = 1;

    /** Octant location flags for child nodes. */
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
        super(Node2D::new, precision);
    }

    /** Tree node class for {@link PointMap2DImpl}.
     * @param <V> Map value type
     */
    private static final class Node2D<V> extends Node<Vector2D, V> {

        /** Construct a new instance.
         * @param map owning map
         * @param parent parent node; set to null for the root node
         */
        Node2D(final AbstractMultiDimensionalPointMap<Vector2D, V> map,
                final Node<Vector2D, V> parent) {
            super(map, parent);
        }

        /** {@inheritDoc} */
        @Override
        protected int getNodeChildCount() {
            return NODE_CHILD_COUNT;
        }

        /** {@inheritDoc} */
        @Override
        protected int getLocation(final Vector2D pt, final Vector2D split) {
            final Precision.DoubleEquivalence precision = getPrecision();

            int loc = getLocationValue(
                    precision.compare(pt.getX(), split.getX()),
                    XNEG,
                    XPOS);
            loc |= getLocationValue(
                    precision.compare(pt.getY(), split.getY()),
                    YNEG,
                    YPOS);

            return loc;
        }

        /** {@inheritDoc} */
        @Override
        protected Vector2D computeSplitPoint(final List<Entry<Vector2D, V>> entries) {
            final Vector2D.Sum sum = Vector2D.Sum.create();
            for (final Entry<Vector2D, V> entry : entries) {
                sum.add(entry.getKey());
            }

            return sum.get().multiply(1.0 / entries.size());
        }

        /** {@inheritDoc} */
        @Override
        protected boolean testChildLocation(final int childIdx, final int loc) {
            final int childLoc = CHILD_LOCATIONS[childIdx];
            return (childLoc & loc) == childLoc;
        }
    }
}
