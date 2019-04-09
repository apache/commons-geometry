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
package org.apache.commons.geometry.euclidean.oned;

import java.util.Arrays;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.junit.Assert;
import org.junit.Test;


public class RegionBSPTree1DTest {

    private static final double TEST_EPS = 1e-15;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testRegion_singleClosedInterval() {
        // arrange
        RegionBSPTree1D tree = new RegionBSPTree1D();
        tree.insert(Arrays.asList(
                    OrientedPoint.createNegativeFacing(Vector1D.of(-1), TEST_PRECISION),
                    OrientedPoint.createPositiveFacing(Vector1D.of(9), TEST_PRECISION)
                ));

        // act/assert
        assertLocation(RegionLocation.OUTSIDE, tree, Double.NEGATIVE_INFINITY);
        assertLocation(RegionLocation.OUTSIDE, tree, -2.0);
        assertLocation(RegionLocation.INSIDE, tree, 0.0);
        assertLocation(RegionLocation.BOUNDARY, tree, 9.0 - 1e-16);
        assertLocation(RegionLocation.BOUNDARY, tree, 9.0 + 1e-16);
        assertLocation(RegionLocation.OUTSIDE, tree, 10.0);
        assertLocation(RegionLocation.OUTSIDE, tree, Double.POSITIVE_INFINITY);
    }

    private static void assertLocation(RegionLocation location, RegionBSPTree1D tree, double pt) {
        Assert.assertEquals(location, tree.classify(Vector1D.of(pt)));
    }
}
