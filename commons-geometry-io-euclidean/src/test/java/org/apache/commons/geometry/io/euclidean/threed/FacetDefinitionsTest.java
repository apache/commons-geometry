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
package org.apache.commons.geometry.io.euclidean.threed;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.ConvexPolygon3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FacetDefinitionsTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION = new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final List<Vector3D> FACET_PTS = Arrays.asList(
            Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(1, 1, 0), Vector3D.of(0, 1, 0));

    @Test
    public void testToPolygon_noNormal() {
        // arrange
        final SimpleFacetDefinition f = new SimpleFacetDefinition(FACET_PTS);

        // act
        final ConvexPolygon3D p = FacetDefinitions.toPolygon(f, TEST_PRECISION);

        // assert
        Assertions.assertSame(p.getPlane().getPrecision(), TEST_PRECISION);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Z, p.getPlane().getNormal(), TEST_EPS);
        Assertions.assertEquals(4, p.getVertices().size());
        Assertions.assertEquals(1.0, p.getSize(), TEST_EPS);
    }

    @Test
    public void testToPolygon_withNormal_similarDirection() {
        // arrange
        final Vector3D normal = Vector3D.of(0.1, 0.2, 0.3);
        final SimpleFacetDefinition f = new SimpleFacetDefinition(FACET_PTS, normal);

        // act
        final ConvexPolygon3D p = FacetDefinitions.toPolygon(f, TEST_PRECISION);

        // assert
        Assertions.assertSame(p.getPlane().getPrecision(), TEST_PRECISION);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Z, p.getPlane().getNormal(), TEST_EPS);
        Assertions.assertEquals(4, p.getVertices().size());
        Assertions.assertEquals(1.0, p.getSize(), TEST_EPS);
    }

    @Test
    public void testToPolygon_withNormal_differentDirection() {
        // arrange
        final Vector3D normal = Vector3D.of(0.1, 0.2, -0.3);
        final SimpleFacetDefinition f = new SimpleFacetDefinition(FACET_PTS, normal);

        // act
        final ConvexPolygon3D p = FacetDefinitions.toPolygon(f, TEST_PRECISION);

        // assert
        Assertions.assertSame(p.getPlane().getPrecision(), TEST_PRECISION);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_Z, p.getPlane().getNormal(), TEST_EPS);
        Assertions.assertEquals(4, p.getVertices().size());
        Assertions.assertEquals(1.0, p.getSize(), TEST_EPS);
    }

    @Test
    public void testToPolygon_failure() {
        // arrange
        final SimpleFacetDefinition f = new SimpleFacetDefinition(Arrays.asList(
                Vector3D.ZERO, Vector3D.ZERO, Vector3D.ZERO));

        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> FacetDefinitions.toPolygon(f, TEST_PRECISION));
    }

    @Test
    public void testToPolygon_invalidArgs() {
        // arrange
        final SimpleFacetDefinition f = new SimpleFacetDefinition(Arrays.asList(
                Vector3D.ZERO, Vector3D.ZERO, Vector3D.ZERO));

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> FacetDefinitions.toPolygon(null, TEST_PRECISION),
                NullPointerException.class, "Facet cannot be null");

        GeometryTestUtils.assertThrowsWithMessage(
                () -> FacetDefinitions.toPolygon(f, null),
                NullPointerException.class, "Precision context cannot be null");
    }
}
