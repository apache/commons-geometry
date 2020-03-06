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
package org.apache.commons.geometry.euclidean.threed.rotation;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class SlerpFunctionTest {

    private static final double TEST_EPS = 1e-12;

    @Test
    public void testProperties() {
        // arrange
        QuaternionRotation start = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_X, 0.0);
        QuaternionRotation end = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_X, PlaneAngleRadians.PI_OVER_TWO);

        // act
        SlerpFunction fn = new SlerpFunction(start, end);

        // assert
        Assert.assertSame(start, fn.getStart());
        Assert.assertSame(end, fn.getEnd());
    }

    @Test
    public void testApply() {
        // arrange
        QuaternionRotation start = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, 0.0);
        QuaternionRotation end = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, PlaneAngleRadians.PI_OVER_TWO);

        // act
        SlerpFunction fn = new SlerpFunction(start, end);

        // assert
        Vector3D pt = Vector3D.Unit.PLUS_X;

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, -1, 0).normalize(), fn.apply(-0.5).apply(pt), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_X, fn.apply(0).apply(pt), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 0).normalize(), fn.apply(0.5).apply(pt), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Y, fn.apply(1).apply(pt), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 1, 0).normalize(), fn.apply(1.5).apply(pt), TEST_EPS);
    }

    @Test
    public void testToString() {
        // arrange
        QuaternionRotation start = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_X, 0.0);
        QuaternionRotation end = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_X, PlaneAngleRadians.PI_OVER_TWO);

        SlerpFunction fn = new SlerpFunction(start, end);

        // act
        String str = fn.toString();

        // assert
        GeometryTestUtils.assertContains("SlerpFunction[", str);
        GeometryTestUtils.assertContains("start= [1", str);
        GeometryTestUtils.assertContains("end= [0.7", str);
    }
}
