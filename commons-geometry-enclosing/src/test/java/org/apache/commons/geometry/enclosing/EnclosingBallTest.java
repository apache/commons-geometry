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
package org.apache.commons.geometry.enclosing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.junit.Assert;
import org.junit.Test;

public class EnclosingBallTest {

    private static final double TEST_EPS = 1e-10;

    @Test
    public void testProperties_emptySupport() {
        // arrange
        Vector2D center = Vector2D.of(1.2, 3.4);
        double radius = 10;
        List<Vector2D> support = new ArrayList<>();

        // act
        EnclosingBall<Vector2D> ball = new EnclosingBall<>(center, radius, support);

        // assert
        Assert.assertSame(center, ball.getCenter());
        Assert.assertEquals(radius, ball.getRadius(), TEST_EPS);
        Assert.assertEquals(0, ball.getSupportSize());

        List<Vector2D> resultSupport = ball.getSupport();
        Assert.assertEquals(0, resultSupport.size());
    }

    @Test
    public void testProperties_nonEmptySupport() {
        // arrange
        Vector2D center = Vector2D.of(1.2, 3.4);
        double radius = 10;
        List<Vector2D> support = new ArrayList<>(Arrays.asList(
                Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.Unit.PLUS_Y));

        // act
        EnclosingBall<Vector2D> ball = new EnclosingBall<>(center, radius, support);

        // assert
        Assert.assertSame(center, ball.getCenter());
        Assert.assertEquals(radius, ball.getRadius(), TEST_EPS);
        Assert.assertEquals(3, ball.getSupportSize());

        List<Vector2D> resultSupport = ball.getSupport();
        Assert.assertNotSame(support, resultSupport);
        Assert.assertEquals(support, resultSupport);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetSupport_listCannotBeModified() {
        // arrange
        List<Vector2D> support = new ArrayList<>(Arrays.asList(Vector2D.ZERO));

        EnclosingBall<Vector2D> ball = new EnclosingBall<>(Vector2D.of(1, 1), 4, support);

        // act/assert
        ball.getSupport().add(Vector2D.Unit.PLUS_X);
    }

    @Test
    public void testContains_strict() {
        // arrange
        Vector2D center = Vector2D.of(1, 2);
        double radius = 2;
        EnclosingBall<Vector2D> ball = new EnclosingBall<>(center, radius, Collections.emptyList());

        // act/assert
        Assert.assertTrue(ball.contains(center));

        Assert.assertTrue(ball.contains(Vector2D.of(2, 3)));
        Assert.assertTrue(ball.contains(Vector2D.of(0, 1)));

        Assert.assertTrue(ball.contains(Vector2D.of(0, 2)));
        Assert.assertTrue(ball.contains(Vector2D.of(1, 4)));

        Assert.assertFalse(ball.contains(Vector2D.of(3.00001, 2)));
        Assert.assertFalse(ball.contains(Vector2D.of(1, -1e-12)));

        Assert.assertFalse(ball.contains(Vector2D.of(1, 5)));
        Assert.assertFalse(ball.contains(Vector2D.of(1, -1)));
        Assert.assertFalse(ball.contains(Vector2D.of(-2, 2)));
        Assert.assertFalse(ball.contains(Vector2D.of(4, 2)));
    }

    @Test
    public void testContains_precision() {
        // arrange
        DoublePrecisionContext lowerPrecision = new EpsilonDoublePrecisionContext(1e-4);
        DoublePrecisionContext higherPrecision = new EpsilonDoublePrecisionContext(1e-10);

        Vector2D center = Vector2D.of(1, 2);
        double radius = 2;
        EnclosingBall<Vector2D> ball = new EnclosingBall<>(center, radius, Collections.emptyList());

        // act/assert
        Assert.assertTrue(ball.contains(center, higherPrecision));

        Assert.assertTrue(ball.contains(Vector2D.of(2, 3), higherPrecision));
        Assert.assertTrue(ball.contains(Vector2D.of(0, 1), higherPrecision));

        Assert.assertTrue(ball.contains(Vector2D.of(0, 2), higherPrecision));
        Assert.assertTrue(ball.contains(Vector2D.of(1, 4), higherPrecision));

        Assert.assertFalse(ball.contains(Vector2D.of(3.00001, 2), higherPrecision));
        Assert.assertTrue(ball.contains(Vector2D.of(1, -1e-12), higherPrecision));

        Assert.assertTrue(ball.contains(Vector2D.of(3.00001, 2), lowerPrecision));
        Assert.assertTrue(ball.contains(Vector2D.of(1, -1e-12), lowerPrecision));

        Assert.assertFalse(ball.contains(Vector2D.of(1, 5), higherPrecision));
        Assert.assertFalse(ball.contains(Vector2D.of(1, -1), higherPrecision));
        Assert.assertFalse(ball.contains(Vector2D.of(-2, 2), higherPrecision));
        Assert.assertFalse(ball.contains(Vector2D.of(4, 2), higherPrecision));
    }

    @Test
    public void testToString() {
        // arrange
        EnclosingBall<Vector2D> ball = new EnclosingBall<>(Vector2D.ZERO, 1, Arrays.asList(Vector2D.Unit.PLUS_X));

        // act
        String str = ball.toString();

        // assert
        Assert.assertTrue(str.startsWith("EnclosingBall[center= (0"));
        Assert.assertTrue(str.contains("radius= 1"));
    }
}
