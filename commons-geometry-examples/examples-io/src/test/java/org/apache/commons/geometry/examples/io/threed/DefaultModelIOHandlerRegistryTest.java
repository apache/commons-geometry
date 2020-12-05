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
package org.apache.commons.geometry.examples.io.threed;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.Triangle3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DefaultModelIOHandlerRegistryTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private DefaultModelIOHandlerRegistry registry = new DefaultModelIOHandlerRegistry();

    @Test
    public void testDefaultHandlers() {
        // act
        List<ModelIOHandler> handlers = registry.getHandlers();

        // assert
        Assertions.assertEquals(1, handlers.size());
    }

    @Test
    public void testSupportedTypes() {
        // act/assert
        Assertions.assertTrue(registry.handlesType("obj"));
        Assertions.assertTrue(registry.handlesType("OBJ"));
    }

    @Test
    public void testReadWrite_supportedTypes() {
        // act/assert
        checkWriteRead("obj");
    }

    private void checkWriteRead(String type) {
        // arrange
        BoundarySource3D model = BoundarySource3D.from(
                Planes.triangleFromVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0), TEST_PRECISION)
            );

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // act
        registry.write(model, type, out);
        BoundarySource3D result = registry.read(type, new ByteArrayInputStream(out.toByteArray()), TEST_PRECISION);

        // assert
        List<Triangle3D> tris = result.triangleStream().collect(Collectors.toList());
        Assertions.assertEquals(1, tris.size());

        Triangle3D tri = tris.get(0);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, tri.getPoint1(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 0, 0), tri.getPoint2(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 0), tri.getPoint3(), TEST_EPS);
    }
}
