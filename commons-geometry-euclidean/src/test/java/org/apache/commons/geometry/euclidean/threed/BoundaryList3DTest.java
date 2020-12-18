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

import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BoundaryList3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testCtor() {
        // arrange
        final List<PlaneConvexSubset> boundaries = Collections.singletonList(
                Planes.fromNormal(Vector3D.Unit.PLUS_X, TEST_PRECISION).span()
        );

        // act
        final BoundaryList3D list = new BoundaryList3D(boundaries);

        // assert
        Assertions.assertNotSame(boundaries, list.getBoundaries());
        Assertions.assertEquals(boundaries, list.getBoundaries());
        Assertions.assertEquals(1, list.count());
    }

    @Test
    public void testToList() {
        // arrange
        final BoundaryList3D list = new BoundaryList3D(Collections.emptyList());

        // act/assert
        Assertions.assertSame(list, list.toList());
    }

    @Test
    public void testToString() {
        // arrange
        final BoundaryList3D list = new BoundaryList3D(Collections.emptyList());

        // act
        Assertions.assertEquals("BoundaryList3D[count= 0]", list.toString());
    }
}
