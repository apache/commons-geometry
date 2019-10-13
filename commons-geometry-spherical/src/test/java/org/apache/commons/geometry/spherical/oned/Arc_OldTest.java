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
import org.apache.commons.geometry.spherical.partitioning.Region_Old;
import org.apache.commons.numbers.core.Precision;
import org.junit.Assert;
import org.junit.Test;

public class Arc_OldTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testArc() {
        Arc_Old arc = new Arc_Old(2.3, 5.7, TEST_PRECISION);
        Assert.assertEquals(3.4, arc.getSize(), TEST_EPS);
        Assert.assertEquals(4.0, arc.getBarycenter(), TEST_EPS);
        Assert.assertEquals(Region_Old.Location.BOUNDARY, arc.checkPoint(2.3));
        Assert.assertEquals(Region_Old.Location.BOUNDARY, arc.checkPoint(5.7));
        Assert.assertEquals(Region_Old.Location.OUTSIDE,  arc.checkPoint(1.2));
        Assert.assertEquals(Region_Old.Location.OUTSIDE,  arc.checkPoint(8.5));
        Assert.assertEquals(Region_Old.Location.INSIDE,   arc.checkPoint(8.7));
        Assert.assertEquals(Region_Old.Location.INSIDE,   arc.checkPoint(3.0));
        Assert.assertEquals(2.3, arc.getInf(), TEST_EPS);
        Assert.assertEquals(5.7, arc.getSup(), TEST_EPS);
        Assert.assertEquals(4.0, arc.getBarycenter(), TEST_EPS);
        Assert.assertEquals(3.4, arc.getSize(), TEST_EPS);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testWrongInterval() {
        new Arc_Old(1.2, 0.0, TEST_PRECISION);
    }

    @Test
    public void testTolerance() {
        Assert.assertEquals(Region_Old.Location.OUTSIDE,  new Arc_Old(2.3, 5.7, createPrecision(1.0)).checkPoint(1.2));
        Assert.assertEquals(Region_Old.Location.BOUNDARY, new Arc_Old(2.3, 5.7, createPrecision(1.2)).checkPoint(1.2));
        Assert.assertEquals(Region_Old.Location.OUTSIDE,  new Arc_Old(2.3, 5.7, createPrecision(0.7)).checkPoint(6.5));
        Assert.assertEquals(Region_Old.Location.BOUNDARY, new Arc_Old(2.3, 5.7, createPrecision(0.9)).checkPoint(6.5));
        Assert.assertEquals(Region_Old.Location.INSIDE,   new Arc_Old(2.3, 5.7, createPrecision(0.6)).checkPoint(3.0));
        Assert.assertEquals(Region_Old.Location.BOUNDARY, new Arc_Old(2.3, 5.7, createPrecision(0.8)).checkPoint(3.0));
    }

    @Test
    public void testFullCircle() {
        Arc_Old arc = new Arc_Old(9.0, 9.0, TEST_PRECISION);
        // no boundaries on a full circle
        Assert.assertEquals(Region_Old.Location.INSIDE, arc.checkPoint(9.0));
        Assert.assertEquals(.0, arc.getInf(), TEST_EPS);
        Assert.assertEquals(Geometry.TWO_PI, arc.getSup(), TEST_EPS);
        Assert.assertEquals(2.0 * Math.PI, arc.getSize(), TEST_EPS);
        for (double alpha = -20.0; alpha <= 20.0; alpha += 0.1) {
            Assert.assertEquals(Region_Old.Location.INSIDE, arc.checkPoint(alpha));
        }
    }

    @Test
    public void testSmall() {
        Arc_Old arc = new Arc_Old(1.0, Math.nextAfter(1.0, Double.POSITIVE_INFINITY), createPrecision(Precision.EPSILON));
        Assert.assertEquals(2 * Precision.EPSILON, arc.getSize(), Precision.SAFE_MIN);
        Assert.assertEquals(1.0, arc.getBarycenter(), Precision.EPSILON);
    }

    /** Create a {@link DoublePrecisionContext} with the given epsilon value.
     * @param eps epsilon value
     * @return new precision context
     */
    private static DoublePrecisionContext createPrecision(final double eps) {
        return new EpsilonDoublePrecisionContext(eps);
    }
}
