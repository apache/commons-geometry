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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.junit.Assert;
import org.junit.Test;

public class NestedLoopsTest_Old {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @SuppressWarnings("unchecked")
    @Test
    public void testNestedLoops() throws Exception {
        Vector2D oneOne = Vector2D.of(1.0, 1.0);
        Vector2D oneNegativeOne = Vector2D.of(1.0, -1.0);
        Vector2D negativeOneNegativeOne = Vector2D.of(-1.0, -1.0);
        Vector2D negativeOneOne = Vector2D.of(-1.0, 1.0);
        Vector2D origin = Vector2D.of(0, 0);

        Vector2D [] vertices = new Vector2D[]{
                oneOne,
                oneNegativeOne,
                negativeOneNegativeOne,
                negativeOneOne,
                origin
        };

        NestedLoops nestedLoops = new NestedLoops(TEST_PRECISION);
        nestedLoops.add(vertices);
        nestedLoops.correctOrientation();

        Field surroundedField = nestedLoops.getClass().getDeclaredField("surrounded");
        Field loopField = nestedLoops.getClass().getDeclaredField("loop");
        surroundedField.setAccessible(Boolean.TRUE);
        loopField.setAccessible(Boolean.TRUE);
        List<NestedLoops> surrounded = (List<NestedLoops>) surroundedField.get(nestedLoops);
        Vector2D[] loop = (Vector2D []) loopField.get(surrounded.get(0));
        Set<Vector2D> vertexSet = new HashSet<>(Arrays.asList(loop));
        Assert.assertTrue(vertexSet.contains(oneOne));
        Assert.assertTrue(vertexSet.contains(oneNegativeOne));
        Assert.assertTrue(vertexSet.contains(negativeOneNegativeOne));
        Assert.assertTrue(vertexSet.contains(negativeOneOne));
        Assert.assertTrue(vertexSet.contains(origin));
    }

}
