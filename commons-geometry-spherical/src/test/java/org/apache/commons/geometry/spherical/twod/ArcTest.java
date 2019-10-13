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
package org.apache.commons.geometry.spherical.twod;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.spherical.oned.AngularInterval;
import org.junit.Assert;
import org.junit.Test;

public class ArcTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromInterval_full() {
        // act
        Arc arc = Arc.fromInterval(
                GreatCircle.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION),
                AngularInterval.full());

        // assert
        Assert.assertTrue(arc.isFull());
        Assert.assertFalse(arc.isEmpty());
        Assert.assertTrue(arc.isFinite());
        Assert.assertFalse(arc.isInfinite());

        for (double az = 0; az < Geometry.TWO_PI; az += 0.1) {
            checkClassify(arc, RegionLocation.INSIDE, Point2S.of(az, Geometry.HALF_PI));
        }

        checkClassify(arc, RegionLocation.OUTSIDE,
                Point2S.PLUS_K, Point2S.of(0, Geometry.HALF_PI + 0.1),
                Point2S.MINUS_K, Point2S.of(0, Geometry.HALF_PI - 0.1));
    }

    private static void checkClassify(Arc arc, RegionLocation loc, Point2S ... pts) {
        for (Point2S pt : pts) {
            Assert.assertEquals("Unexpected location for point " + pt, loc, arc.classify(pt));
        }
    }
}
