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
package org.apache.commons.geometry.spherical.twod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.junit.Assert;
import org.junit.Test;

public class BoundarySource2STest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testToTree() {
        // act
        List<GreatArc> arcs = Arrays.asList(GreatCircles.arcFromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION));
        BoundarySource2S src = () -> arcs.stream();

        // act
        RegionBSPTree2S tree = src.toTree();

        // assert
        Assert.assertEquals(3, tree.count());
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());
    }

    @Test
    public void testToTree_noBoundaries() {
        // act
        BoundarySource2S src = () -> new ArrayList<GreatArc>().stream();

        // act
        RegionBSPTree2S tree = src.toTree();

        // assert
        Assert.assertEquals(1, tree.count());
        Assert.assertFalse(tree.isFull());
        Assert.assertTrue(tree.isEmpty());
    }
}
