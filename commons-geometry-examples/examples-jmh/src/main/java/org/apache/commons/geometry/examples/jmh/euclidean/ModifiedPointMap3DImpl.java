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
package org.apache.commons.geometry.examples.jmh.euclidean;

import org.apache.commons.geometry.euclidean.threed.PointMap3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.core.Precision;

/** Internal {@link PointMap3D} implementation.
 * @param <V> Map value type
 */
final class ModifiedPointMap3DImpl<V>
    extends ModifiedAbstractBucketPointMap<Vector3D, V>
    implements PointMap3D<V> {

    /** Number of children per node. */
    private static final int NODE_CHILD_COUNT = 8;

    /** Max entries per node. */
    private static final int MAX_ENTRIES_PER_NODE = 16;

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

    /** Construct a new instance using the given precision context to determine
     * floating point equality.
     * @param precision precision context
     */
    ModifiedPointMap3DImpl(final Precision.DoubleEquivalence precision) {
        super(MapNode3D::new,
                MAX_ENTRIES_PER_NODE,
                NODE_CHILD_COUNT,
                precision);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean pointsEq(final Vector3D a, final Vector3D b) {
        return a.eq(b, getPrecision());
    }

    /** Tree node class for {@link ModifiedPointMap3DImpl}.
     * @param <V> Map value type
     */
    private static final class MapNode3D<V> extends BucketNode<Vector3D, V> {

        /** Point to split child spaces; will be null for leaf nodes. */
        private Vector3D split;

        /** Construct a new instance.
         * @param map owning map
         * @param parent parent node; set to null for the root node
         */
        MapNode3D(final ModifiedAbstractBucketPointMap<Vector3D, V> map,
                final BucketNode<Vector3D, V> parent) {
            super(map, parent);
        }

        /** {@inheritDoc} */
        @Override
        protected void computeSplit() {
            final Vector3D.Sum sum = Vector3D.Sum.create();
            for (Entry<Vector3D, V> entry : this) {
                sum.add(entry.getKey());
            }

            split = sum.get().multiply(1.0 / MAX_ENTRIES_PER_NODE);
        }

        /** {@inheritDoc} */
        @Override
        protected int getSearchLocation(final Vector3D pt) {
            final Precision.DoubleEquivalence precision = getPrecision();

            int loc = getSearchLocationValue(
                    precision.compare(pt.getX(), split.getX()),
                    XNEG,
                    XPOS);
            loc |= getSearchLocationValue(
                    precision.compare(pt.getY(), split.getY()),
                    YNEG,
                    YPOS);
            loc |= getSearchLocationValue(
                    precision.compare(pt.getZ(), split.getZ()),
                    ZNEG,
                    ZPOS);

            return loc;
        }

        /** {@inheritDoc} */
        @Override
        protected int getInsertLocation(final Vector3D pt) {
            int loc = getInsertLocationValue(
                    Double.compare(pt.getX(), split.getX()),
                    XNEG,
                    XPOS);
            loc |= getInsertLocationValue(
                    Double.compare(pt.getY(), split.getY()),
                    YNEG,
                    YPOS);
            loc |= getInsertLocationValue(
                    Double.compare(pt.getZ(), split.getZ()),
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

        /** {@inheritDoc} */
        @Override
        protected void makeLeaf() {
            super.makeLeaf();

            split = null;
        }
    }
}
