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
package org.apache.commons.geometry.euclidean.threed;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.junit.Test;

import org.junit.Assert;

public class SubPlaneTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final Plane XY_PLANE = Plane.fromNormal(Vector3D.PLUS_Z, TEST_PRECISION);

    @Test
    public void testCtor_plane() {
        // act
        SubPlane sp = new SubPlane(XY_PLANE);

        // assert
        Assert.assertFalse(sp.isFull());
        Assert.assertTrue(sp.isEmpty());

        Assert.assertEquals(0, sp.getSize(), TEST_EPS);
    }

    @Test
    public void testCtor_plane_booleanFalse() {
        // act
        SubPlane sp = new SubPlane(XY_PLANE, false);

        // assert
        Assert.assertFalse(sp.isFull());
        Assert.assertTrue(sp.isEmpty());

        Assert.assertEquals(0, sp.getSize(), TEST_EPS);
    }

    @Test
    public void testCtor_plane_booleanTrue() {
        // act
        SubPlane sp = new SubPlane(XY_PLANE, true);

        // assert
        Assert.assertTrue(sp.isFull());
        Assert.assertFalse(sp.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(sp.getSize());
    }
}
