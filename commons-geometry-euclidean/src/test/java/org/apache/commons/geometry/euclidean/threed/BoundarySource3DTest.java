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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.junit.Assert;
import org.junit.Test;

public class BoundarySource3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testToTree() {
        // act
        PlaneConvexSubset a = Planes.subsetFromVertexLoop(
                Arrays.asList(Vector3D.ZERO, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y), TEST_PRECISION);
        PlaneConvexSubset b = Planes.subsetFromVertexLoop(
                Arrays.asList(Vector3D.ZERO, Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_Z), TEST_PRECISION);

        BoundarySource3D src = BoundarySource3D.from(a, b);

        // act
        RegionBSPTree3D tree = src.toTree();

        // assert
        Assert.assertEquals(5, tree.count());
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());
    }

    @Test
    public void testToTree_noBoundaries() {
        // act
        BoundarySource3D src = BoundarySource3D.from();

        // act
        RegionBSPTree3D tree = src.toTree();

        // assert
        Assert.assertEquals(1, tree.count());
        Assert.assertFalse(tree.isFull());
        Assert.assertTrue(tree.isEmpty());
    }

    @Test
    public void testFrom_varargs_empty() {
        // act
        BoundarySource3D src = BoundarySource3D.from();

        // assert
        List<PlaneConvexSubset> segments = src.boundaryStream().collect(Collectors.toList());
        Assert.assertEquals(0, segments.size());
    }

    @Test
    public void testFrom_varargs() {
        // act
        PlaneConvexSubset a = Planes.subsetFromVertexLoop(
                Arrays.asList(Vector3D.ZERO, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y), TEST_PRECISION);
        PlaneConvexSubset b = Planes.subsetFromVertexLoop(
                Arrays.asList(Vector3D.ZERO, Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_Z), TEST_PRECISION);

        BoundarySource3D src = BoundarySource3D.from(a, b);

        // assert
        List<PlaneConvexSubset> boundaries = src.boundaryStream().collect(Collectors.toList());
        Assert.assertEquals(2, boundaries.size());

        Assert.assertSame(a, boundaries.get(0));
        Assert.assertSame(b, boundaries.get(1));
    }

    @Test
    public void testFrom_list_empty() {
        // arrange
        List<PlaneConvexSubset> input = new ArrayList<>();

        // act
        BoundarySource3D src = BoundarySource3D.from(input);

        // assert
        List<PlaneConvexSubset> segments = src.boundaryStream().collect(Collectors.toList());
        Assert.assertEquals(0, segments.size());
    }

    @Test
    public void testFrom_list() {
        // act
        PlaneConvexSubset a = Planes.subsetFromVertexLoop(
                Arrays.asList(Vector3D.ZERO, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y), TEST_PRECISION);
        PlaneConvexSubset b = Planes.subsetFromVertexLoop(
                Arrays.asList(Vector3D.ZERO, Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_Z), TEST_PRECISION);

        List<PlaneConvexSubset> input = new ArrayList<>();
        input.add(a);
        input.add(b);

        BoundarySource3D src = BoundarySource3D.from(input);

        // assert
        List<PlaneConvexSubset> segments = src.boundaryStream().collect(Collectors.toList());
        Assert.assertEquals(2, segments.size());

        Assert.assertSame(a, segments.get(0));
        Assert.assertSame(b, segments.get(1));
    }
}
