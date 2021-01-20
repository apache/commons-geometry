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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.AffineTransformMatrix3D;
import org.apache.commons.geometry.euclidean.threed.BoundaryList3D;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D;
import org.apache.commons.geometry.euclidean.threed.Triangle3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.shape.Parallelepiped;
import org.apache.commons.geometry.io.core.test.CloseCountInputStream;
import org.apache.commons.geometry.io.core.test.CloseCountOutputStream;
import org.apache.commons.geometry.io.euclidean.EuclideanIOTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class IO3DTest {

    private static final double TEST_EPS = 1e-4;

    private static final double MODEL_EPS = 1e-8;

    private static final DoublePrecisionContext MODEL_PRECISION = new EpsilonDoublePrecisionContext(MODEL_EPS);

    private static final List<String> SUPPORTED_FORMATS = Arrays.asList(
                IO3D.TXT,
                IO3D.CSV,
                IO3D.OBJ
            );

    @TempDir
    public Path tempDir;

    @Test
    public void testStreamExample() throws IOException {
        final Path origFile = tempDir.resolve("orig.obj");
        final Path scaledFile = tempDir.resolve("scaled.csv");

        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-10);
        final BoundarySource3D src = Parallelepiped.unitCube(precision);

        IO3D.write(src, origFile);

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.createScale(2);

        try (Stream<Triangle3D> stream = IO3D.triangles(origFile, precision)) {
            IO3D.write(stream.map(t -> t.transform(transform)), scaledFile);
        }

        final RegionBSPTree3D result = IO3D.read(scaledFile, precision).toTree();

        // assert
        Assertions.assertEquals(8, result.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, result.getCentroid(), TEST_EPS);
    }

    @Test
    public void testReadWriteFacets_facetDefinitionReader() throws Exception {
        // act/assert
        testReadWriteWithPath(
                (fmt, path) -> readerToBoundaryList(IO3D.facetDefinitionReader(path)),
                (src, fmt, path) -> IO3D.writeFacets(boundarySourceToFacets(src), path));
        testReadWriteWithUrl(
                (fmt, url) -> readerToBoundaryList(IO3D.facetDefinitionReader(url)),
                (src, fmt, path) -> IO3D.writeFacets(boundarySourceToFacets(src), path));
        testReadWriteWithInputOutputStreams(
                (fmt, in) -> readerToBoundaryList(IO3D.facetDefinitionReader(in, fmt)),
                (src, fmt, out) -> IO3D.writeFacets(boundarySourceToFacets(src), out, fmt));
    }

    @Test
    public void testReadWriteFacets_facetStream() throws Exception {
        // act/assert
        testReadWriteWithPath(
                (fmt, path) -> facetsToBoundaryList(IO3D.facets(path)),
                (src, fmt, path) -> IO3D.writeFacets(boundarySourceToFacets(src), path));
        testReadWriteWithUrl(
                (fmt, url) -> facetsToBoundaryList(IO3D.facets(url)),
                (src, fmt, path) -> IO3D.writeFacets(boundarySourceToFacets(src), path));
        testReadWriteWithInputOutputStreams(
                (fmt, in) -> facetsToBoundaryList(IO3D.facets(in, fmt)),
                (src, fmt, out) -> IO3D.writeFacets(boundarySourceToFacets(src), out, fmt));
    }

    @Test
    public void testReadWriteBoundarySource() throws Exception {
        // act/assert
        testReadWriteWithPath(
                (fmt, path) -> IO3D.read(path, MODEL_PRECISION),
                (src, fmt, path) -> IO3D.write(src, path));
        testReadWriteWithUrl(
                (fmt, url) -> IO3D.read(url, MODEL_PRECISION),
                (src, fmt, path) -> IO3D.write(src, path));
        testReadWriteWithInputOutputStreams(
                (fmt, in) -> IO3D.read(in, fmt, MODEL_PRECISION),
                (src, fmt, out) -> IO3D.write(src, out, fmt));
    }

    @Test
    public void testReadWriteBoundarySource_triangleMesh() throws Exception {
        // act/assert
        testReadWriteWithPath(
                (fmt, path) -> IO3D.readTriangleMesh(path, MODEL_PRECISION),
                (src, fmt, path) -> IO3D.write(src.toTriangleMesh(MODEL_PRECISION), path));
        testReadWriteWithUrl(
                (fmt, url) -> IO3D.readTriangleMesh(url, MODEL_PRECISION),
                (src, fmt, path) -> IO3D.write(src.toTriangleMesh(MODEL_PRECISION), path));
        testReadWriteWithInputOutputStreams(
                (fmt, in) -> IO3D.readTriangleMesh(in, fmt, MODEL_PRECISION),
                (src, fmt, out) -> IO3D.write(src.toTriangleMesh(MODEL_PRECISION), out, fmt));
    }

    @Test
    public void testReadWriteBoundarySource_boundaryStream() throws Exception {
        // act/assert
        testReadWriteWithPath(
                (fmt, path) -> boundariesToBoundaryList(IO3D.boundaries(path, MODEL_PRECISION)),
                (src, fmt, path) -> IO3D.write(src, path));
        testReadWriteWithUrl(
                (fmt, url) -> boundariesToBoundaryList(IO3D.boundaries(url, MODEL_PRECISION)),
                (src, fmt, path) -> IO3D.write(src, path));
        testReadWriteWithInputOutputStreams(
                (fmt, in) -> boundariesToBoundaryList(IO3D.boundaries(in, fmt, MODEL_PRECISION)),
                (src, fmt, out) -> IO3D.write(src, out, fmt));
    }

    @Test
    public void testReadWriteBoundarySource_triangleStream() throws Exception {
        // act/assert
        testReadWriteWithPath(
                (fmt, path) -> boundariesToBoundaryList(IO3D.triangles(path, MODEL_PRECISION)),
                (src, fmt, path) -> IO3D.write(src, path));
        testReadWriteWithUrl(
                (fmt, url) -> boundariesToBoundaryList(IO3D.triangles(url, MODEL_PRECISION)),
                (src, fmt, path) -> IO3D.write(src, path));
        testReadWriteWithInputOutputStreams(
                (fmt, in) -> boundariesToBoundaryList(IO3D.triangles(in, fmt, MODEL_PRECISION)),
                (src, fmt, out) -> IO3D.write(src, out, fmt));
    }

    @Test
    public void testWriteBoundaryStream() throws Exception {
        // act/assert
        testReadWriteWithPath(
                (fmt, path) -> boundariesToBoundaryList(IO3D.triangles(path, MODEL_PRECISION)),
                (src, fmt, path) -> IO3D.write(src.boundaryStream(), path));
        testReadWriteWithUrl(
                (fmt, url) -> boundariesToBoundaryList(IO3D.triangles(url, MODEL_PRECISION)),
                (src, fmt, path) -> IO3D.write(src.boundaryStream(), path));
        testReadWriteWithInputOutputStreams(
                (fmt, in) -> boundariesToBoundaryList(IO3D.triangles(in, fmt, MODEL_PRECISION)),
                (src, fmt, out) -> IO3D.write(src.boundaryStream(), out, fmt));
    }

    @Test
    public void testWriteFacetStream() throws Exception {
        // act/assert
        testReadWriteWithPath(
                (fmt, path) -> boundariesToBoundaryList(IO3D.triangles(path, MODEL_PRECISION)),
                (src, fmt, path) -> IO3D.writeFacets(boundarySourceToFacets(src).stream(), path));
        testReadWriteWithUrl(
                (fmt, url) -> boundariesToBoundaryList(IO3D.triangles(url, MODEL_PRECISION)),
                (src, fmt, path) -> IO3D.writeFacets(boundarySourceToFacets(src).stream(), path));
        testReadWriteWithInputOutputStreams(
                (fmt, in) -> boundariesToBoundaryList(IO3D.triangles(in, fmt, MODEL_PRECISION)),
                (src, fmt, out) -> IO3D.writeFacets(boundarySourceToFacets(src).stream(), out, fmt));
    }

    private void testReadWriteWithPath(final ReadFn<Path> readFn, final WriteFn<Path> writeFn)
            throws Exception {
        String baseName;
        RegionBSPTree3D expected;
        String location;
        Path path;
        for (final Map.Entry<String, RegionBSPTree3D> entry : getTestInputs().entrySet()) {
            baseName = entry.getKey();
            expected = entry.getValue();

            for (final String fmt : SUPPORTED_FORMATS) {
                location = getModelLocation(baseName, fmt);
                path = Paths.get(EuclideanIOTestUtils.resource(location).toURI());

                testReadWriteWithPath(fmt, path, readFn, writeFn, expected, TEST_EPS);
            }
        }
    }

    private void testReadWriteWithPath(final String fmt, final Path path,
            final ReadFn<Path> readFn, final WriteFn<Path> writeFn,
            final RegionBSPTree3D expected, final double eps) throws IOException {

        final Path tmp = Files.createTempFile("tmp", "." + fmt);

        final BoundarySource3D orig = readFn.read(fmt, path);
        assertRegion(expected, orig, eps);

        writeFn.write(orig, fmt, tmp);

        final BoundarySource3D result = readFn.read(fmt, tmp);
        assertRegion(expected, result, eps);
    }

    private void testReadWriteWithUrl(final ReadFn<URL> readFn, final WriteFn<Path> writeFn) throws Exception {
        String baseName;
        RegionBSPTree3D expected;
        String location;
        URL url;
        for (final Map.Entry<String, RegionBSPTree3D> entry : getTestInputs().entrySet()) {
            baseName = entry.getKey();
            expected = entry.getValue();

            for (String fmt : SUPPORTED_FORMATS) {
                location = getModelLocation(baseName, fmt);
                url = EuclideanIOTestUtils.resource(location);

                testReadWriteWithUrl(fmt, url, readFn, writeFn, expected, TEST_EPS);
            }
        }
    }

    private void testReadWriteWithUrl(final String fmt, final URL url,
            final ReadFn<URL> readFn, final WriteFn<Path> writeFn,
            final RegionBSPTree3D expected, final double eps) throws IOException {

        final Path tmp = Files.createTempFile("tmp", "." + fmt);

        final BoundarySource3D orig = readFn.read(fmt, url);
        assertRegion(expected, orig, eps);

        writeFn.write(orig, fmt, tmp);

        final BoundarySource3D result = readFn.read(fmt, tmp.toUri().toURL());
        assertRegion(expected, result, eps);
    }

    private void testReadWriteWithInputOutputStreams(final ReadFn<InputStream> readFn, final WriteFn<OutputStream> writeFn)
            throws Exception {
        String baseName;
        RegionBSPTree3D expected;
        String location;
        Path path;
        for (final Map.Entry<String, RegionBSPTree3D> entry : getTestInputs().entrySet()) {
            baseName = entry.getKey();
            expected = entry.getValue();

            for (String fmt : SUPPORTED_FORMATS) {
                location = getModelLocation(baseName, fmt);
                path = Paths.get(EuclideanIOTestUtils.resource(location).toURI());

                testReadWriteWithStreams(fmt, path, readFn, writeFn, expected, TEST_EPS);
            }
        }
    }

    private void testReadWriteWithStreams(final String fmt, final Path path,
            final ReadFn<InputStream> readFn, final WriteFn<OutputStream> writeFn,
            final RegionBSPTree3D expected, final double eps) throws IOException {

        final Path tmp = Files.createTempFile("tmp", "." + fmt);

        final BoundarySource3D orig;
        try (CloseCountInputStream in = new CloseCountInputStream(Files.newInputStream(path))) {
            orig = readFn.read(fmt, in);

            Assertions.assertEquals(0, in.getCloseCount());
        }
        assertRegion(expected, orig, eps);

        try (CloseCountOutputStream out = new CloseCountOutputStream(Files.newOutputStream(tmp))) {
            writeFn.write(orig, fmt, out);

            Assertions.assertEquals(0, out.getCloseCount());
        }

        final BoundarySource3D result;
        try (CloseCountInputStream in = new CloseCountInputStream(Files.newInputStream(tmp))) {
            result = readFn.read(fmt, in);
        }
        assertRegion(expected, result, eps);
    }

    private static void assertRegion(final RegionBSPTree3D expected, final BoundarySource3D actual, final double eps) {
        final RegionBSPTree3D actualRegion = actual.toTree();

        Assertions.assertEquals(expected.getSize(), actualRegion.getSize(), eps);
        Assertions.assertEquals(expected.getBoundarySize(), actualRegion.getBoundarySize(), eps);

        if (expected.isEmpty()) {
            Assertions.assertTrue(actualRegion.isEmpty());
        } else {
            EuclideanTestUtils.assertCoordinatesEqual(expected.getCentroid(), actualRegion.getCentroid(), eps);
        }

        final RegionBSPTree3D diff = RegionBSPTree3D.empty();
        diff.difference(expected, actualRegion);

        Assertions.assertEquals(0, diff.getSize(), eps);
    }

    private static String getModelLocation(final String baseName, final String fmt) {
        return "/models/" + baseName + "." + fmt;
    }

    private static Map<String, RegionBSPTree3D> getTestInputs() {
        final Map<String, RegionBSPTree3D> inputs = new HashMap<>();

        inputs.put("empty", RegionBSPTree3D.empty());
        inputs.put("cube", EuclideanIOTestUtils.cube(MODEL_PRECISION).toTree());
        inputs.put("cube-minus-sphere", EuclideanIOTestUtils.cubeMinusSphere(MODEL_PRECISION).toTree());

        return inputs;
    }

    private static BoundaryList3D readerToBoundaryList(final FacetDefinitionReader reader) throws IOException {
        final List<PlaneConvexSubset> list = new ArrayList<>();
        FacetDefinition f;
        while ((f = reader.readFacet()) != null) {
            list.add(FacetDefinitions.toPolygon(f, MODEL_PRECISION));
        }

        return new BoundaryList3D(list);
    }

    private static BoundaryList3D facetsToBoundaryList(final Stream<FacetDefinition> stream) throws IOException {
        try (Stream<FacetDefinition> facetStream = stream) {
            final List<PlaneConvexSubset> list = facetStream
                    .map(f -> FacetDefinitions.toPolygon(f, MODEL_PRECISION))
                    .collect(Collectors.toList());

            return new BoundaryList3D(list);
        }
    }

    private static <T extends PlaneConvexSubset> BoundaryList3D boundariesToBoundaryList(final Stream<T> stream)
            throws IOException {
        try (Stream<T> boundaryStream = stream) {
            final List<PlaneConvexSubset> list = boundaryStream.collect(Collectors.toList());

            return new BoundaryList3D(list);
        }
    }

    private static List<FacetDefinition> boundarySourceToFacets(final BoundarySource3D src) {
        return src.boundaryStream()
                .map(b -> new SimpleFacetDefinition(b.getVertices()))
                .collect(Collectors.toList());
    }

    @FunctionalInterface
    interface ReadFn<T> {
        BoundarySource3D read(String format, T t) throws IOException;
    }

    @FunctionalInterface
    interface WriteFn<D> {
        void write(BoundarySource3D src, String fmt, D dst) throws IOException;
    }
}
