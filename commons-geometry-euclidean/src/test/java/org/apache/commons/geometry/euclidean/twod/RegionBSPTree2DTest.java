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

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class RegionBSPTree2DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testCtor_booleanArg_true() {
        // act
        RegionBSPTree2D tree = new RegionBSPTree2D(true);

        // assert
        Assert.assertTrue(tree.isFull());
        Assert.assertFalse(tree.isEmpty());
        Assert.assertEquals(1, tree.count());

        Assert.assertNull(tree.getBarycenter());

        GeometryTestUtils.assertPositiveInfinity(tree.getBoundarySize());
        GeometryTestUtils.assertPositiveInfinity(tree.getSize());
    }

    @Test
    public void testCtor_booleanArg_false() {
        // act
        RegionBSPTree2D tree = new RegionBSPTree2D(false);

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertTrue(tree.isEmpty());
        Assert.assertEquals(1, tree.count());

        Assert.assertNull(tree.getBarycenter());

        Assert.assertEquals(0.0, tree.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(0.0, tree.getSize(), TEST_EPS);
    }

    @Test
    public void testCtor_default() {
        // act
        RegionBSPTree2D tree = new RegionBSPTree2D();

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertTrue(tree.isEmpty());
        Assert.assertEquals(1, tree.count());

        Assert.assertNull(tree.getBarycenter());

        Assert.assertEquals(0.0, tree.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(0.0, tree.getSize(), TEST_EPS);
    }

    @Test
    public void testFull_factoryMethod() {
        // act
        RegionBSPTree2D tree = RegionBSPTree2D.full();

        // assert
        Assert.assertTrue(tree.isFull());
        Assert.assertFalse(tree.isEmpty());
        Assert.assertEquals(1, tree.count());

        Assert.assertNull(tree.getBarycenter());

        GeometryTestUtils.assertPositiveInfinity(tree.getBoundarySize());
        GeometryTestUtils.assertPositiveInfinity(tree.getSize());
    }

    @Test
    public void testEmpty_factoryMethod() {
        // act
        RegionBSPTree2D tree = RegionBSPTree2D.empty();

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertTrue(tree.isEmpty());
        Assert.assertEquals(1, tree.count());

        Assert.assertNull(tree.getBarycenter());

        Assert.assertEquals(0.0, tree.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(0.0, tree.getSize(), TEST_EPS);
    }

    private static void checkClassify(RegionBSPTree2D tree, RegionLocation loc, Vector2D ... points) {
        for (Vector2D point : points) {
            String msg = "Unexpected location for point " + point;

            Assert.assertEquals(msg, loc, tree.classify(point));
        }
    }

    private static void checkContains(RegionBSPTree2D tree, boolean contains, Vector2D ... points) {
        for (Vector2D point : points) {
            String msg = "Unexpected contains status for point " + point;

            Assert.assertEquals(msg, contains, tree.contains(point));
        }
    }
}
