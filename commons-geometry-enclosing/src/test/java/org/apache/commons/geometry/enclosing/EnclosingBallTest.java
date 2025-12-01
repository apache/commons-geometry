/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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

import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EnclosingBallTest {

    private static final double TEST_EPS = 1e-10;

    @Test
    void testProperties_emptySupport() {
        // arrange
        final Vector2D center = Vector2D.of(1.2, 3.4);
        final double radius = 10;
        final List<Vector2D> support = new ArrayList<>();

        // act
        final EnclosingBall<Vector2D> ball = new EnclosingBall<>(center, radius, support);

        // assert
        Assertions.assertSame(center, ball.getCenter());
        Assertions.assertEquals(radius, ball.getRadius(), TEST_EPS);
        Assertions.assertEquals(0, ball.getSupportSize());

        final List<Vector2D> resultSupport = ball.getSupport();
        Assertions.assertEquals(0, resultSupport.size());
    }

    @Test
    void testProperties_nonEmptySupport() {
        // arrange
        final Vector2D center = Vector2D.of(1.2, 3.4);
        final double radius = 10;
        final List<Vector2D> support = new ArrayList<>(Arrays.asList(
                Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.Unit.PLUS_Y));

        // act
        final EnclosingBall<Vector2D> ball = new EnclosingBall<>(center, radius, support);

        // assert
        Assertions.assertSame(center, ball.getCenter());
        Assertions.assertEquals(radius, ball.getRadius(), TEST_EPS);
        Assertions.assertEquals(3, ball.getSupportSize());

        final List<Vector2D> resultSupport = ball.getSupport();
        Assertions.assertNotSame(support, resultSupport);
        Assertions.assertEquals(support, resultSupport);
    }

    @Test
    void testGetSupport_listCannotBeModified() {
        // arrange
        final List<Vector2D> support = new ArrayList<>(Collections.singletonList(Vector2D.ZERO));

        final EnclosingBall<Vector2D> ball = new EnclosingBall<>(Vector2D.of(1, 1), 4, support);
        final List<Vector2D> ballSupport = ball.getSupport();

        // act/assert
        Assertions.assertThrows(UnsupportedOperationException.class, () ->  ballSupport.add(Vector2D.Unit.PLUS_X));
    }

    @Test
    void testContains_strict() {
        // arrange
        final Vector2D center = Vector2D.of(1, 2);
        final double radius = 2;
        final EnclosingBall<Vector2D> ball = new EnclosingBall<>(center, radius, Collections.emptyList());

        // act/assert
        Assertions.assertTrue(ball.contains(center));

        Assertions.assertTrue(ball.contains(Vector2D.of(2, 3)));
        Assertions.assertTrue(ball.contains(Vector2D.of(0, 1)));

        Assertions.assertTrue(ball.contains(Vector2D.of(0, 2)));
        Assertions.assertTrue(ball.contains(Vector2D.of(1, 4)));

        Assertions.assertFalse(ball.contains(Vector2D.of(3.00001, 2)));
        Assertions.assertFalse(ball.contains(Vector2D.of(1, -1e-12)));

        Assertions.assertFalse(ball.contains(Vector2D.of(1, 5)));
        Assertions.assertFalse(ball.contains(Vector2D.of(1, -1)));
        Assertions.assertFalse(ball.contains(Vector2D.of(-2, 2)));
        Assertions.assertFalse(ball.contains(Vector2D.of(4, 2)));
    }

    @Test
    void testContains_precision() {
        // arrange
        final Precision.DoubleEquivalence lowerPrecision = Precision.doubleEquivalenceOfEpsilon(1e-4);
        final Precision.DoubleEquivalence higherPrecision = Precision.doubleEquivalenceOfEpsilon(1e-10);

        final Vector2D center = Vector2D.of(1, 2);
        final double radius = 2;
        final EnclosingBall<Vector2D> ball = new EnclosingBall<>(center, radius, Collections.emptyList());

        // act/assert
        Assertions.assertTrue(ball.contains(center, higherPrecision));

        Assertions.assertTrue(ball.contains(Vector2D.of(2, 3), higherPrecision));
        Assertions.assertTrue(ball.contains(Vector2D.of(0, 1), higherPrecision));

        Assertions.assertTrue(ball.contains(Vector2D.of(0, 2), higherPrecision));
        Assertions.assertTrue(ball.contains(Vector2D.of(1, 4), higherPrecision));

        Assertions.assertFalse(ball.contains(Vector2D.of(3.00001, 2), higherPrecision));
        Assertions.assertTrue(ball.contains(Vector2D.of(1, -1e-12), higherPrecision));

        Assertions.assertTrue(ball.contains(Vector2D.of(3.00001, 2), lowerPrecision));
        Assertions.assertTrue(ball.contains(Vector2D.of(1, -1e-12), lowerPrecision));

        Assertions.assertFalse(ball.contains(Vector2D.of(1, 5), higherPrecision));
        Assertions.assertFalse(ball.contains(Vector2D.of(1, -1), higherPrecision));
        Assertions.assertFalse(ball.contains(Vector2D.of(-2, 2), higherPrecision));
        Assertions.assertFalse(ball.contains(Vector2D.of(4, 2), higherPrecision));
    }

    @Test
    void testToString() {
        // arrange
        final EnclosingBall<Vector2D> ball = new EnclosingBall<>(Vector2D.ZERO, 1, Collections.singletonList(Vector2D.Unit.PLUS_X));

        // act
        final String str = ball.toString();

        // assert
        Assertions.assertTrue(str.startsWith("EnclosingBall[center= (0"));
        Assertions.assertTrue(str.contains("radius= 1"));
    }
}
