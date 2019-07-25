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

import java.util.Arrays;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.junit.Test;

import org.junit.Assert;

public class ConvexSubPlaneTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromConvexArea() {
        // arrange
        Plane plane = Plane.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1),
                Vector3D.PLUS_X, Vector3D.PLUS_Y, TEST_PRECISION);
        ConvexArea area = ConvexArea.fromVertexLoop(Arrays.asList(
                    Vector2D.of(1, 0),
                    Vector2D.of(3, 0),
                    Vector2D.of(3, 1),
                    Vector2D.of(1, 1)
                ), TEST_PRECISION);

        // act
        ConvexSubPlane sp = ConvexSubPlane.fromConvexArea(plane, area);

        // assert
        Assert.assertFalse(sp.isFull());
        Assert.assertFalse(sp.isEmpty());

        Assert.assertEquals(2, sp.getSize(), TEST_EPS);

        Assert.assertSame(plane, sp.getPlane());
        Assert.assertSame(plane, sp.getHyperplane());
        Assert.assertSame(area, sp.getSubspaceRegion());
    }
}
