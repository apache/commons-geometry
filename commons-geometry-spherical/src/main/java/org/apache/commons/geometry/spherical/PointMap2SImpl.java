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
package org.apache.commons.geometry.spherical;

import java.util.List;

import org.apache.commons.geometry.core.internal.AbstractBucketPointMap;
import org.apache.commons.geometry.core.partitioning.HyperplaneLocation;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.twod.GreatCircle;
import org.apache.commons.geometry.spherical.twod.GreatCircles;
import org.apache.commons.geometry.spherical.twod.Point2S;
import org.apache.commons.geometry.spherical.twod.PointMap2S;
import org.apache.commons.numbers.core.Precision;

/** Internal implementation of {@link PointMap2S}.
 * @param <V> Map value type
 */
final class PointMap2SImpl<V>
    extends AbstractBucketPointMap<Point2S, V>
    implements PointMap2S<V> {

    /** Number of children per node. */
    private static final int NODE_CHILD_COUNT = 4;

    /** Max entries per node. */
    private static final int MAX_ENTRIES_PER_NODE = 16;

    /** First negative quadrant flag. */
    private static final int NEG1 = 1 << 3;

    /** First positive quadrant flag. */
    private static final int POS1 = 1 << 2;

    /** Second negative quadrant flag. */
    private static final int NEG2 = 1 << 1;

    /** Second positive quadrant flag. */
    private static final int POS2 = 1;

    /** Location flags for child nodes. */
    private static final int[] CHILD_LOCATIONS = {
        NEG1 | NEG2,
        NEG1 | POS2,
        POS1 | NEG2,
        POS1 | POS2
    };

    PointMap2SImpl(final Precision.DoubleEquivalence precision) {
        super(MapNode2S::new,
                MAX_ENTRIES_PER_NODE,
                NODE_CHILD_COUNT,
                precision);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean pointsEq(final Point2S a, final Point2S b) {
        return a.eq(b, getPrecision());
    }

    /** Tree node class for {@link PointMap2SImpl}.
     * @param <V> Map value type
     */
    private static final class MapNode2S<V>
        extends BucketNode<Point2S, V> {

        /** First hyperplane split. */
        private GreatCircle firstSplit;

        /** Second hyperplane split. */
        private GreatCircle secondSplit;

        MapNode2S(
                final AbstractBucketPointMap<Point2S, V> map,
                final BucketNode<Point2S, V> parent) {
            super(map, parent);
        }

        /** {@inheritDoc} */
        @Override
        protected void computeSplit() {
            final Vector3D.Sum sum = Vector3D.Sum.create();

            for (Entry<Point2S, V> entry : this) {
                sum.add(entry.getKey().getVector());
            }

            // construct an orthonormal basis
            Vector3D.Unit u = sum.get().multiply(1.0 / MAX_ENTRIES_PER_NODE)
                    .normalizeOrNull();
            if (u == null) {
                u = Vector3D.Unit.PLUS_X;
            }

            Vector3D.Unit v =  Vector3D.Unit.PLUS_Z.cross(u)
                    .normalizeOrNull();
            if (v == null) {
                v = Vector3D.Unit.PLUS_Y.cross(u)
                        .normalizeOrNull();
            }
            final Vector3D.Unit w = u.cross(v).normalize();

            // construct the two great circles
            firstSplit = GreatCircles.fromPole(v.add(w), getPrecision());
            secondSplit = GreatCircles.fromPole(v.negate().add(w), getPrecision());
        }

        /** {@inheritDoc} */
        @Override
        protected int getSearchLocation(final Point2S pt) {
            int loc = getSearchLocationValue(firstSplit.classify(pt), NEG1, POS1);

            loc |= getSearchLocationValue(secondSplit.classify(pt), NEG2, POS2);

            return loc;
        }

        /** {@inheritDoc} */
        @Override
        protected int getInsertLocation(final Point2S pt) {
            int loc = firstSplit.offset(pt) > 0.0 ?
                    POS1 :
                    NEG1;

            loc |= secondSplit.offset(pt) > 0.0 ?
                    POS2 :
                    NEG2;

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
        protected void makeLeaf(final List<Entry<Point2S, V>> leafEntries) {
            super.makeLeaf(leafEntries);

            firstSplit = null;
            secondSplit = null;
        }

        /** Get an encoded search location for the given hyperplane location.
         * @param loc point location
         * @param neg negative flag
         * @param pos positive flag
         * @return encoded search location
         */
        private static int getSearchLocationValue(final HyperplaneLocation loc,
                final int neg, final int pos) {
            switch (loc) {
            case MINUS:
                return neg;
            case PLUS:
                return pos;
            default:
                return neg | pos;
            }
        }
    }
}
