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
package org.apache.commons.geometry.spherical.oned;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.junit.Assert;
import org.junit.Test;

public class LimitAngleTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testReversedLimit() {
        for (int k = -2; k < 3; ++k) {
            LimitAngle l  = new LimitAngle(S1Point.of(1.0 + k * Geometry.TWO_PI), false, TEST_PRECISION);
            Assert.assertEquals(l.getLocation().getAzimuth(), l.getReverse().getLocation().getAzimuth(), TEST_EPS);
            Assert.assertSame(l.getPrecision(), l.getReverse().getPrecision());
            Assert.assertTrue(l.sameOrientationAs(l));
            Assert.assertFalse(l.sameOrientationAs(l.getReverse()));
            Assert.assertEquals(Geometry.TWO_PI, l.wholeSpace().getSize(), TEST_EPS);
            Assert.assertEquals(Geometry.TWO_PI, l.getReverse().wholeSpace().getSize(), TEST_EPS);
        }
    }

}
