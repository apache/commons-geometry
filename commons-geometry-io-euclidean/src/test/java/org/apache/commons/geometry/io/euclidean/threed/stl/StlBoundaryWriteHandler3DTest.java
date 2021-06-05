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
package org.apache.commons.geometry.io.euclidean.threed.stl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.BoundaryList3D;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.mesh.SimpleTriangleMesh;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.apache.commons.geometry.euclidean.threed.shape.Parallelepiped;
import org.apache.commons.geometry.io.core.input.GeometryInput;
import org.apache.commons.geometry.io.core.input.StreamGeometryInput;
import org.apache.commons.geometry.io.core.output.StreamGeometryOutput;
import org.apache.commons.geometry.io.euclidean.EuclideanIOTestUtils;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinition;
import org.apache.commons.geometry.io.euclidean.threed.GeometryFormat3D;
import org.apache.commons.geometry.io.euclidean.threed.SimpleFacetDefinition;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StlBoundaryWriteHandler3DTest {

    private static final double TEST_EPS = 1e-10;

    /** Lower test epsilon accounting for the use of floats in the binary output. */
    private static final double MODEL_TEST_EPS = 1e-7;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    private final StlBoundaryWriteHandler3D handler = new StlBoundaryWriteHandler3D();

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    @Test
    public void testProperties() {
        // assert
        Assertions.assertEquals(GeometryFormat3D.STL, handler.getFormat());
        Assertions.assertEquals(51200, handler.getinitialBufferSize());
    }

    @Test
    public void testSetInitialBufferSize() {
        // act
        handler.setInitialBufferSize(10);

        // assert
        Assertions.assertEquals(10, handler.getinitialBufferSize());
    }

    @Test
    public void setInitialBufferSize_invalidArg() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> handler.setInitialBufferSize(0),
                IllegalArgumentException.class, "Buffer size must be greater than 0");
    }

    @Test
    public void testWrite_boundarySource_empty() throws IOException {
        // arrange
        final BoundarySource3D src = BoundarySource3D.of();

        // act
        handler.write(src, new StreamGeometryOutput(out));

        // assert
        Assertions.assertEquals(0, readOutput().count());
    }

    @Test
    public void testWrite_boundaryList() throws IOException {
        // arrange
        final BoundarySource3D src = EuclideanIOTestUtils.cubeMinusSphere(TEST_PRECISION);

        // act
        handler.write(src, new StreamGeometryOutput(out));

        // assert
        EuclideanIOTestUtils.assertCubeMinusSphere(readOutput(), MODEL_TEST_EPS);
    }

    @Test
    public void testWrite_triangleMesh() throws IOException {
        // arrange
        final TriangleMesh mesh = EuclideanIOTestUtils.cubeMinusSphere(TEST_PRECISION)
                .toTriangleMesh(TEST_PRECISION);

        // act
        handler.write(mesh, new StreamGeometryOutput(out));

        // assert
        EuclideanIOTestUtils.assertCubeMinusSphere(readOutput(), MODEL_TEST_EPS);
    }

    @Test
    public void testWrite_triangleMesh_empty() throws IOException {
        // arrange
        final TriangleMesh mesh = SimpleTriangleMesh.builder(TEST_PRECISION)
                .build();

        // act
        handler.write(mesh, new StreamGeometryOutput(out));

        // assert
        Assertions.assertEquals(0, readOutput().count());
    }

    @Test
    public void testWriteFacets_list() throws IOException {
        // arrange
        final List<FacetDefinition> facets = cubeFacets();

        // act
        handler.writeFacets(facets, new StreamGeometryOutput(out));

        // assert
        EuclideanIOTestUtils.assertCube(readOutput(), MODEL_TEST_EPS);
    }

    @Test
    public void testWriteFacets_list_empty() throws IOException {
        // act
        handler.writeFacets(Collections.emptyList(), new StreamGeometryOutput(out));

        // assert
        Assertions.assertEquals(0, readOutput().count());
    }

    @Test
    public void testWriteFacets_includesStlFacetAttribute() throws IOException {
        // arrange
        final List<Vector3D> vertices = Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0));
        final Vector3D normal = Vector3D.Unit.PLUS_Z;
        final int attr = 12;

        final BinaryStlFacetDefinition facet = new BinaryStlFacetDefinition(vertices, normal, attr);

        // act
        handler.writeFacets(Collections.singletonList(facet), new StreamGeometryOutput(out));

        // assert
        BinaryStlFacetDefinitionReader reader =
                new BinaryStlFacetDefinitionReader(new ByteArrayInputStream(out.toByteArray()));
        BinaryStlFacetDefinition result = reader.readFacet();

        EuclideanIOTestUtils.assertFacetVertices(result, vertices, MODEL_TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(normal, result.getNormal(), MODEL_TEST_EPS);
        Assertions.assertEquals(attr, result.getAttributeValue());
    }

    private BoundaryList3D readOutput() throws IOException {
        final GeometryInput input = new StreamGeometryInput(new ByteArrayInputStream(out.toByteArray()));

        final StlBoundaryReadHandler3D readHandler = new StlBoundaryReadHandler3D();
        return readHandler.read(input, TEST_PRECISION).toList();
    }

    private static List<FacetDefinition> cubeFacets() {
        final BoundarySource3D cube = Parallelepiped.unitCube(TEST_PRECISION);
        return cube.triangleStream()
            .map(t -> new SimpleFacetDefinition(t.getVertices(), t.getPlane().getNormal()))
            .collect(Collectors.toList());
    }
}
