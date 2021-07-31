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
package org.apache.commons.geometry.io.euclidean.threed.obj;

import java.io.StringReader;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.apache.commons.geometry.io.core.test.CloseCountReader;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ObjTriangleMeshReaderTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    @Test
    void testDefaults() {
        // arrange
        final ObjTriangleMeshReader reader = reader("");

        // act/assert
        Assertions.assertFalse(reader.getFailOnNonPolygonKeywords());
    }

    @Test
    void testClose() {
        // arrange
        final CloseCountReader closeReader = new CloseCountReader(new StringReader(""));

        // act/assert
        try (ObjTriangleMeshReader reader = new ObjTriangleMeshReader(closeReader, TEST_PRECISION)) {
            Assertions.assertEquals(0, closeReader.getCloseCount());
        }

        Assertions.assertEquals(1, closeReader.getCloseCount());
    }

    @Test
    void testReadTriangleMesh_withNormal() {
        // arrange
        final ObjTriangleMeshReader reader = reader(
                "o test\n\n" +
                "v 0 0 0\r\n" +
                "v 0.5 0 0\n" +
                "v 1 1 0\r" +
                "v 0 1 0\n" +
                "vn 0 0 -1\n" +
                "f 1//1 2//1 3//1 4//1\n" +
                "curv non-polygon data\n");

        // act
        final TriangleMesh mesh = reader.readTriangleMesh();

        // assert
        Assertions.assertEquals(4, mesh.getVertexCount());
        Assertions.assertEquals(2, mesh.getFaceCount());

        final TriangleMesh.Face face1 = mesh.getFace(0);

        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(
                Vector3D.of(0.5, 0, 0), Vector3D.of(0, 1, 0), Vector3D.of(1, 1, 0)
            ), face1.getVertices(), TEST_PRECISION);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_Z,
                face1.getPolygon().getPlane().getNormal(), TEST_EPS);

        final TriangleMesh.Face face2 = mesh.getFace(1);

        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(
                Vector3D.ZERO, Vector3D.of(0, 1, 0), Vector3D.of(0.5, 0, 0)
            ), face2.getVertices(), TEST_PRECISION);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_Z,
                face2.getPolygon().getPlane().getNormal(), TEST_EPS);
    }

    @Test
    void testReadTriangleMesh_withoutNormal() {
        // arrange
        final ObjTriangleMeshReader reader = reader(
                "o test\n\n" +
                "v -1 0 0\n" +
                "v 0 0 0\r\n" +
                "v 1 0 0\n" +
                "v 1 1 0\r" +
                "v -2 0 0\n" +
                "f 2 3 4\n");

        // act
        final TriangleMesh mesh = reader.readTriangleMesh();

        // assert
        Assertions.assertEquals(5, mesh.getVertexCount());
        Assertions.assertEquals(1, mesh.getFaceCount());

        final TriangleMesh.Face face = mesh.getFace(0);

        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(
                    Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(1, 1, 0)
                ), face.getVertices(), TEST_PRECISION);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1),
                face.getPolygon().getPlane().getNormal(), TEST_EPS);
    }

    @Test
    void testReadTriangleMesh_failOnNonPolygon() {
        // arrange
        final ObjTriangleMeshReader reader = reader(
                "o test\n\n" +
                "v 0 0 0\r\n" +
                "v 1 0 0\n" +
                "v 1 1 0\r" +
                "v 0 1 0\n" +
                "vn 0 0 1\n" +
                "f 1//1 2//1 3//1\n" +
                "curv non-polygon data\n");

        reader.setFailOnNonPolygonKeywords(true);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> reader.readTriangleMesh(),
                IllegalStateException.class, Pattern.compile("^Parsing failed.*"));
    }

    private static ObjTriangleMeshReader reader(final String str) {
        return new ObjTriangleMeshReader(new StringReader(str), TEST_PRECISION);
    }
}
