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

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.junit.Assert;
import org.junit.Test;

public class SegmentTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testDistance() {
        Vector2D start = Vector2D.of(2, 2);
        Vector2D end = Vector2D.of(-2, -2);
        Segment segment = new Segment(start, end, new Line(start, end, TEST_PRECISION));

        // distance to center of segment
        Assert.assertEquals(Math.sqrt(2), segment.distance(Vector2D.of(1, -1)), TEST_EPS);

        // distance a point on segment
        Assert.assertEquals(Math.sin(Math.PI / 4.0), segment.distance(Vector2D.of(0, -1)), TEST_EPS);

        // distance to end point
        Assert.assertEquals(Math.sqrt(8), segment.distance(Vector2D.of(0, 4)), TEST_EPS);

        // distance to start point
        Assert.assertEquals(Math.sqrt(8), segment.distance(Vector2D.of(0, -4)), TEST_EPS);
    }
}
