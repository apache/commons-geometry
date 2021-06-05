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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.shape.Parallelepiped;
import org.apache.commons.geometry.euclidean.threed.shape.Sphere;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinition;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinitionReader;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;

/** Class containing utility methods for IO tests.
 */
public final class EuclideanIOTestUtils {

    /** Utility class; no instantiation. */
    private EuclideanIOTestUtils() {}

    /** Return a test cube.
     * @param precision precision context used for floating point comparisons
     * @return a test cube
     */
    public static Parallelepiped cube(final Precision.DoubleEquivalence precision) {
        return Parallelepiped.unitCube(precision);
    }

    /** Assert that the given boundary source defines a cube equivalent to that returned by
     * {@link #cube(Precision.DoubleEquivalence)}.
     * @param src boundary source to test
     * @param eps floating point comparison epsilon
     */
    public static void assertCube(final BoundarySource3D src, final double eps) {
        final RegionBSPTree3D tree = src.toTree();

        Assertions.assertEquals(1, tree.getSize(), eps);
        Assertions.assertEquals(6, tree.getBoundarySize(), eps);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, tree.getCentroid(), eps);

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE,
                Vector3D.ZERO,
                Vector3D.of(0.25, 0, 0), Vector3D.of(-0.25, 0, 0),
                Vector3D.of(0, 0.25, 0), Vector3D.of(0, -0.25, 0),
                Vector3D.of(0, 0, 0.25), Vector3D.of(0, 0, -0.25));

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.BOUNDARY,
                Vector3D.of(-0.5, -0.5, -0.5), Vector3D.of(-0.5, -0.5, +0.5),
                Vector3D.of(-0.5, +0.5, -0.5), Vector3D.of(-0.5, +0.5, +0.5),
                Vector3D.of(+0.5, -0.5, -0.5), Vector3D.of(+0.5, -0.5, +0.5),
                Vector3D.of(+0.5, +0.5, -0.5), Vector3D.of(+0.5, +0.5, +0.5));

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE,
                Vector3D.of(0.5, 0.5, 1), Vector3D.of(0.5, 0.5, -1),
                Vector3D.of(0.5, 1, 0.5), Vector3D.of(0.5, -1, 0.5),
                Vector3D.of(1, 0.5, 0.5), Vector3D.of(-1, 0.5, 0.5));
    }

    /** Return a test cube with a sphere removed from the center.
     * @param precision precision context used for floating point comparisons
     * @return a test cube with a sphere removed from the center
     */
    public static RegionBSPTree3D cubeMinusSphere(final Precision.DoubleEquivalence precision) {
        final RegionBSPTree3D tree = Parallelepiped.unitCube(precision).toTree();
        final Sphere sphere = Sphere.from(Vector3D.ZERO, 0.65, precision);

        tree.difference(sphere.toTree(3));

        return tree;
    }

    /** Assert that the given boundary source defines a region equivalent to that returned by
     * {@link #cubeMinusSphere(Precision.DoubleEquivalence)}.
     * @param src boundary source to test
     * @param eps floating point comparison epsilon
     */
    public static void assertCubeMinusSphere(final BoundarySource3D src, final double eps) {
        final RegionBSPTree3D tree = src.toTree();

        Assertions.assertEquals(0.11509505362599505, tree.getSize(), eps);
        Assertions.assertEquals(4.585561662505128, tree.getBoundarySize(), eps);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, tree.getCentroid(), eps);

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE,
                Vector3D.of(0.45, 0.45, 0.45), Vector3D.of(0.45, 0.45, -0.45),
                Vector3D.of(0.45, -0.45, 0.45), Vector3D.of(0.45, -0.45, -0.45),
                Vector3D.of(-0.45, 0.45, 0.45), Vector3D.of(-0.45, 0.45, -0.45),
                Vector3D.of(-0.45, -0.45, 0.45), Vector3D.of(-0.45, -0.45, -0.45));

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.BOUNDARY,
                Vector3D.of(-0.5, -0.5, -0.5), Vector3D.of(-0.5, -0.5, +0.5),
                Vector3D.of(-0.5, +0.5, -0.5), Vector3D.of(-0.5, +0.5, +0.5),
                Vector3D.of(+0.5, -0.5, -0.5), Vector3D.of(+0.5, -0.5, +0.5),
                Vector3D.of(+0.5, +0.5, -0.5), Vector3D.of(+0.5, +0.5, +0.5));

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE,
                Vector3D.ZERO,
                Vector3D.of(0.5, 0.5, 1), Vector3D.of(0.5, 0.5, -1),
                Vector3D.of(0.5, 1, 0.5), Vector3D.of(0.5, -1, 0.5),
                Vector3D.of(1, 0.5, 0.5), Vector3D.of(-1, 0.5, 0.5));
    }

    /** Read all facets available from the given facet reader.
     * @param reader instance to read facets from
     * @return list containing all facets available from the given facet reader
     * @throws IOException if an I/O or data format error occurs
     */
    public static List<FacetDefinition> readAll(final FacetDefinitionReader reader) throws IOException {
        final List<FacetDefinition> facets = new ArrayList<>();

        FacetDefinition f;
        while ((f = reader.readFacet()) != null) {
            facets.add(f);
        }

        return facets;
    }

    /** Get the classpath resource at the given location, throwing an exception if it could not be found.
     * @param location classpath location
     * @return classpath resource at the given location
     * @throws IOException if the resource cannot be found
     */
    public static URL resource(final String location) throws IOException {
        final URL url = EuclideanIOTestUtils.class.getResource(location);
        if (url == null) {
            throw new FileNotFoundException("Unable to find classpath resource: " + location);
        }

        return url;
    }

    /** Assert that the facet definition contains the given vertices in order.
     * @param facet facet to test
     * @param expectedVertices expected vertices
     * @param eps floating point comparison epsilon
     */
    public static void assertFacetVertices(final FacetDefinition facet, final List<Vector3D> expectedVertices,
            final double eps) {
        List<Vector3D> vertices = facet.getVertices();
        Assertions.assertEquals(expectedVertices.size(), vertices.size());

        for (int i = 0; i < expectedVertices.size(); ++i) {
            EuclideanTestUtils.assertCoordinatesEqual(expectedVertices.get(i), facet.getVertices().get(i), eps);
        }
    }

    /** Assert that the facet definition contains the given vertices (in order) and normal.
     * @param facet facet to test
     * @param expectedVertices expected vertices
     * @param expectedNormal expected normal; may be null
     * @param eps floating point comparison epsilon
     */
    public static void assertFacetVerticesAndNormal(final FacetDefinition facet, final List<Vector3D> expectedVertices,
            final Vector3D expectedNormal, final double eps) {
        assertFacetVertices(facet, expectedVertices, eps);

        if (expectedNormal == null) {
            Assertions.assertNull(facet.getNormal(), "Expected facet normal to be null");
        } else {
            EuclideanTestUtils.assertCoordinatesEqual(expectedNormal, facet.getNormal(), eps);
        }
    }
}
