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
package org.apache.commons.geometry.euclidean.threed;

import java.util.List;

import org.apache.commons.geometry.euclidean.internal.AbstractMultiDimensionalPointMap;
import org.apache.commons.numbers.core.Precision;

final class PointMap3DImpl<V>
    extends AbstractMultiDimensionalPointMap<Vector3D, V>
    implements PointMap3D<V> {

    /** Number of children per node. */
    private static final int NODE_CHILD_COUNT = 8;

    /** X negative octant flag. */
    private static final int XNEG = 1 << 5;

    /** X postive octant flag. */
    private static final int XPOS = 1 << 4;

    /** Y negative octant flag. */
    private static final int YNEG = 1 << 3;

    /** Y positive octant flag. */
    private static final int YPOS = 1 << 2;

    /** Z negative octant flag. */
    private static final int ZNEG = 1 << 1;

    /** Z positive octant flag. */
    private static final int ZPOS = 1;

    /** Octant location flags for child nodes. */
    private static final int[] CHILD_LOCATIONS = {
        XNEG | YNEG | ZNEG,
        XNEG | YNEG | ZPOS,
        XNEG | YPOS | ZNEG,
        XNEG | YPOS | ZPOS,

        XPOS | YNEG | ZNEG,
        XPOS | YNEG | ZPOS,
        XPOS | YPOS | ZNEG,
        XPOS | YPOS | ZPOS
    };

    PointMap3DImpl(final Precision.DoubleEquivalence precision) {
        super(Node3D::new, precision);
    }

    private static final class Node3D<V> extends Node<Vector3D, V> {

        Node3D(final AbstractMultiDimensionalPointMap<Vector3D, V> map, final Node<Vector3D, V> parent) {
            super(map, parent);
        }

        /** {@inheritDoc} */
        @Override
        protected int getNodeChildCount() {
            return NODE_CHILD_COUNT;
        }

        /** {@inheritDoc} */
        @Override
        protected Vector3D computeSplitPoint(final List<Entry<Vector3D, V>> entries) {
            final Vector3D.Sum sum = Vector3D.Sum.create();
            for (Entry<Vector3D, V> entry : entries) {
                sum.add(entry.getKey());
            }

            return sum.get().multiply(1.0 / entries.size());
        }

        /** {@inheritDoc} */
        @Override
        protected int getLocation(final Vector3D pt, final Vector3D split) {
            final Precision.DoubleEquivalence precision = getPrecision();

            int loc = getLocationValue(
                    precision.compare(pt.getX(), split.getX()),
                    XNEG,
                    XPOS);
            loc |= getLocationValue(
                    precision.compare(pt.getY(), split.getY()),
                    YNEG,
                    YPOS);
            loc |= getLocationValue(
                    precision.compare(pt.getZ(), split.getZ()),
                    ZNEG,
                    ZPOS);

            return loc;
        }

        /** {@inheritDoc} */
        @Override
        protected boolean testChildLocation(final int childIdx, final int loc) {
            final int childLoc = CHILD_LOCATIONS[childIdx];
            return (childLoc & loc) == childLoc;
        }
    }
}