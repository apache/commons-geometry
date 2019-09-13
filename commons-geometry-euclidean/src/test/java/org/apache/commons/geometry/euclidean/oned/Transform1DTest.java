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

import java.util.function.Function;

import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class Transform1DTest {

    private static final double TEST_EPS = 1e-15;

    @Test
    public void testIdentity() {
        // arrange
        Vector1D p0 = Vector1D.of(0);
        Vector1D p1 = Vector1D.of(1);
        Vector1D p2 = Vector1D.of(-1);

        // act
        Transform1D t = Transform1D.identity();

        // assert
        Assert.assertTrue(t.preservesOrientation());

        EuclideanTestUtils.assertCoordinatesEqual(p0, t.apply(p0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, t.apply(p1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p2, t.apply(p2), TEST_EPS);
    }

    @Test
    public void testFrom_identity() {
        // arrange
        Vector1D p0 = Vector1D.of(0);
        Vector1D p1 = Vector1D.of(1);
        Vector1D p2 = Vector1D.of(-1);

        // act
        Transform1D t = Transform1D.from(Function.identity());

        // assert
        Assert.assertTrue(t.preservesOrientation());

        EuclideanTestUtils.assertCoordinatesEqual(p0, t.apply(p0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, t.apply(p1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p2, t.apply(p2), TEST_EPS);
    }

    @Test
    public void testFrom_scaleAndTranslate() {
        // arrange
        Vector1D p0 = Vector1D.of(0);
        Vector1D p1 = Vector1D.of(1);
        Vector1D p2 = Vector1D.of(-1);

        // act
        Transform1D t = Transform1D.from(v -> Vector1D.of((v.getX() + 2) * 3));

        // assert
        Assert.assertTrue(t.preservesOrientation());

        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(6), t.apply(p0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(9), t.apply(p1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(3), t.apply(p2), TEST_EPS);
    }

    @Test
    public void testFrom_reflection() {
        // arrange
        Vector1D p0 = Vector1D.of(0);
        Vector1D p1 = Vector1D.of(1);
        Vector1D p2 = Vector1D.of(-1);

        // act
        Transform1D t = Transform1D.from(Vector1D::negate);

        // assert
        Assert.assertFalse(t.preservesOrientation());

        EuclideanTestUtils.assertCoordinatesEqual(p0, t.apply(p0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p2, t.apply(p1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, t.apply(p2), TEST_EPS);
    }
}
