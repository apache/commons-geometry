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
package org.apache.commons.geometry.core.internal;

import java.util.Map;

import org.apache.commons.geometry.core.partitioning.test.TestPoint1D;
import org.apache.commons.numbers.core.Precision;

/** {@link AbstractBucketPointMap} implementation for use in tests.
 * @param <V> Value type
 */
public class TestBucketPointMap1D<V> extends AbstractBucketPointMap<TestPoint1D, V> {

    static final int MAX_ENTRY_COUNT = 16;

    static final int NODE_CHILD_COUNT = 2;

    TestBucketPointMap1D(final Precision.DoubleEquivalence precision) {
        super(TestNode1D::new,
                MAX_ENTRY_COUNT,
                NODE_CHILD_COUNT,
                precision);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean pointsEq(final TestPoint1D a, final TestPoint1D b) {
        return getPrecision().eq(a.getX(), b.getX());
    }

    /** {@inheritDoc} */
    @Override
    protected int disambiguatePointComparison(final TestPoint1D a, final TestPoint1D b) {
        return Double.compare(a.getX(), b.getX());
    }

    private static final class TestNode1D<V>
        extends AbstractBucketPointMap.BucketNode<TestPoint1D, V> {

        /** Negative half-space flag. */
        private static final int NEG = 1 << 1;

        /** Positve half-space flag. */
        private static final int POS = 1;

        /** Location flags for child nodes. */
        private static final int[] CHILD_LOCATIONS = {
            NEG,
            POS
        };

        private double split;

        TestNode1D(
                final AbstractBucketPointMap<TestPoint1D, V> map,
                final BucketNode<TestPoint1D, V> parent,
                final int childIndex) {
            super(map, parent, childIndex);
        }

        /** {@inheritDoc} */
        @Override
        protected void computeSplit() {
            double sum = 0;
            for (Map.Entry<TestPoint1D, V> entry : this) {
                sum += entry.getKey().getX();
            }

            split = sum / TestBucketPointMap1D.MAX_ENTRY_COUNT;
        }

        /** {@inheritDoc} */
        @Override
        protected int getSearchLocation(final TestPoint1D pt) {
            return getSearchLocationValue(getPrecision().compare(pt.getX(), split), NEG, POS);
        }

        /** {@inheritDoc} */
        @Override
        protected int getInsertLocation(final TestPoint1D pt) {
            return getInsertLocationValue(Double.compare(pt.getX(), split), NEG, POS);
        }

        /** {@inheritDoc} */
        @Override
        protected boolean testChildLocation(final int childIdx, final int loc) {
            final int childLoc = CHILD_LOCATIONS[childIdx];
            return (childLoc & loc) == childLoc;
        }

        /** {@inheritDoc} */
        @Override
        protected double getMinChildDistance(final int childIdx, final TestPoint1D pt, final int ptLoc) {
            return ptLoc == CHILD_LOCATIONS[childIdx] ?
                    0d :
                    Math.abs(pt.getX() - split);
        }

        /** {@inheritDoc} */
        @Override
        protected double getMaxChildDistance(final int childIdx, final TestPoint1D pt, final int ptLoc) {
            final TestNode1D<V> parent = (TestNode1D<V>) getParent();
            if (parent != null &&
                childIdx != getChildIndex()) {

                return getMaxDistance(pt.getX(), parent.split, split);
            }

            return Double.POSITIVE_INFINITY;
        }
    }
}
