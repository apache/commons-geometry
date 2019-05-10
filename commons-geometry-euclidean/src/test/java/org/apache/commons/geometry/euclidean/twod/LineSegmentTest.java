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

import org.apache.commons.geometry.core.partition.HyperplaneLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.junit.Assert;
import org.junit.Test;


public class LineSegmentTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromPoints() {
        // arrange
        Vector2D p0 = Vector2D.ZERO;
        Vector2D p1 = Vector2D.of(1, 2);
        Vector2D p2 = Vector2D.of(-3, 4);
        Vector2D p3 = Vector2D.of(-5, -6);

        // act/assert

        checkSegment(LineSegment.fromPoints(p0, p1, TEST_PRECISION), p0, p1);
    }

    private static void checkSegment(LineSegment segment, Vector2D start, Vector2D end) {
        checkSegment(segment, start, end, TEST_PRECISION);
    }

    private static void checkSegment(LineSegment segment, Vector2D start, Vector2D end, DoublePrecisionContext precision) {
        EuclideanTestUtils.assertCoordinatesEqual(start, segment.getStart(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(end, segment.getEnd(), TEST_EPS);

        Line line = segment.getLine();
        Assert.assertEquals(HyperplaneLocation.ON, line.classify(segment.getStart()));
        Assert.assertEquals(HyperplaneLocation.ON, line.classify(segment.getEnd()));

        Assert.assertEquals(line.toSubspace(segment.getStart()).getX(), segment.getSubspaceStart(), TEST_EPS);
        Assert.assertEquals(line.toSubspace(segment.getEnd()).getX(), segment.getSubspaceEnd(), TEST_EPS);

        Assert.assertSame(precision, segment.getPrecision());
        Assert.assertSame(precision, line.getPrecision());
    }
}
