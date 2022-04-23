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
import org.apache.commons.geometry.euclidean.internal.Vectors;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.core.Precision;

/** Internal {@link PointMap} implementation for Euclidean 3D space.
 * @param <V> Map value type
 */
final class PointMap3DImpl<V>
    extends AbstractBucketPointMap<Vector3D, V>
    implements PointMap<Vector3D, V> {

    /** Number of children per node. */
    private static final int NODE_CHILD_COUNT = 8;

    /** Max entries per node. This value was determined empirically and was chosen to
     * provide a balance between having a small number of entries in each node when
     * searching and having a large number of samples to provide a good split point
     * during insertion. See the {@code org.apache.commons.geometry.examples.jmh.euclidean.PointMap3DPerformance}
     * class in the {@code examples-jmh} module for details on the performance tests used.
     */
    private static final int MAX_ENTRIES_PER_NODE = 32;

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

    /** Bit mask for x location. */
    private static final int XMASK = XNEG | XPOS;

    /** Bit mask for y location. */
    private static final int YMASK = YNEG | YPOS;

    /** Bit mask for z location. */
    private static final int ZMASK = ZNEG | ZPOS;

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
    PointMap3DImpl(final Precision.DoubleEquivalence precision) {
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

    /** {@inheritDoc} */
    @Override
    protected int disambiguatePointComparison(final Vector3D a, final Vector3D b) {
        return Vector3D.COORDINATE_ASCENDING_ORDER.compare(a, b);
    }

    /** Tree node class for {@link PointMap3DImpl}.
     * @param <V> Map value type
     */
    private static final class MapNode3D<V> extends BucketNode<Vector3D, V> {

        /** Point to split child spaces; will be null for leaf nodes. */
        private Vector3D split;

        /** Construct a new instance.
         * @param map owning map
         * @param parent parent node; set to null for the root node
         * @param childIndex index of this node in its parent's child list;
         *      set to {@code -1} for the root node
         */
        MapNode3D(final AbstractBucketPointMap<Vector3D, V> map,
                final BucketNode<Vector3D, V> parent,
                final int childIndex) {
            super(map, parent, childIndex);
        }

        /** {@inheritDoc} */
        @Override
        protected void computeSplit() {
            final Vector3D.Sum sum = Vector3D.Sum.create();
            for (final Entry<Vector3D, V> entry : this) {
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
        protected void makeLeaf(final List<Entry<Vector3D, V>> leafEntries) {
            super.makeLeaf(leafEntries);

            split = null;
        }

        /** {@inheritDoc} */
        @Override
        protected double getMinChildDistance(final int childIdx, final Vector3D pt, final int ptLoc) {
            final int childLoc = CHILD_LOCATIONS[childIdx];

            final boolean sameX = (ptLoc & XMASK) == (childLoc & XMASK);
            final boolean sameY = (ptLoc & YMASK) == (childLoc & YMASK);
            final boolean sameZ = (ptLoc & ZMASK) == (childLoc & ZMASK);

            final Vector3D diff = pt.subtract(split);

            if (sameX) {
                if (sameY) {
                    return sameZ ?
                            0d :
                            Math.abs(diff.getZ());
                }
                return sameZ ?
                        Math.abs(diff.getY()) :
                        Vectors.norm(diff.getY(), diff.getZ());
            } else if (sameY) {
                return sameZ ?
                        Math.abs(diff.getX()) :
                        Vectors.norm(diff.getX(), diff.getZ());
            } else if (sameZ) {
                return Vectors.norm(diff.getX(), diff.getY());
            }

            return diff.norm();
        }

        /** {@inheritDoc} */
        @Override
        protected double getMaxChildDistance(final int childIdx, final Vector3D pt, final int ptLoc) {
            final MapNode3D<V> grandParent = (MapNode3D<V>) getParent();
            if (grandParent != null) {
                final int nodeLoc = CHILD_LOCATIONS[getChildIndex()];
                final int childLoc = CHILD_LOCATIONS[childIdx];

                final boolean oppositeX = (nodeLoc & XMASK) != (childLoc & XMASK);
                final boolean oppositeY = (nodeLoc & YMASK) != (childLoc & YMASK);
                final boolean oppositeZ = (nodeLoc & ZMASK) != (childLoc & ZMASK);

                if (oppositeX && oppositeY && oppositeZ) {
                    // the grandparent and parent splits form a completely enclosed region,
                    // meaning that we can determine a max distance
                    final Vector3D diff = Vector3D.of(
                                getMaxDistance(pt.getX(), grandParent.split.getX(), split.getX()),
                                getMaxDistance(pt.getY(), grandParent.split.getY(), split.getY()),
                                getMaxDistance(pt.getZ(), grandParent.split.getZ(), split.getZ())
                            );

                    return diff.norm();
                }
            }

            return Double.POSITIVE_INFINITY;
        }
    }
}
