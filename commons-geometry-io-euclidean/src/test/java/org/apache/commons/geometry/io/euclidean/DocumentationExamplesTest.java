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
package org.apache.commons.geometry.io.euclidean;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.apache.commons.geometry.euclidean.threed.shape.Parallelepiped;
import org.apache.commons.geometry.euclidean.threed.shape.Sphere;
import org.apache.commons.geometry.io.euclidean.threed.IO3D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DocumentationExamplesTest {

    private static final double TEST_EPS = 1e-12;

    @Test
    public void testIndexPageExample() throws IOException {
        // construct a precision context to handle floating-point comparisons
        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-6);

        // create a BSP tree representing the unit cube
        final RegionBSPTree3D tree = Parallelepiped.unitCube(precision).toTree();

        // create a sphere centered on the origin
        final Sphere sphere = Sphere.from(Vector3D.ZERO, 0.65, precision);

        // subtract a BSP tree approximation of the sphere containing 512 facets
        // from the cube, modifying the cube tree in place
        tree.difference(sphere.toTree(3));

        // compute some properties of the resulting region
        final double size = tree.getSize(); // 0.11509505362599505
        final Vector3D centroid = tree.getCentroid(); // (0, 0, 0)

        // convert to a triangle mesh
        final TriangleMesh mesh = tree.toTriangleMesh(precision);

        // save as an OBJ file
        IO3D.write(mesh, Paths.get("target/cube-minus-sphere.obj"));

        // -----------
        Assertions.assertEquals(0.11509505362599505, size, TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, centroid, TEST_EPS);
    }
}
