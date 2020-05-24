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
package org.apache.commons.geometry.examples.io;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Triangle3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

/**
 * Utility class for writing out 3D scenes in various file formats.
 */
public final class Format3D {
    /** Utility class. */
    private Format3D() {}

    /** Output format. */
    public enum Output {
        /** <a href="https://en.wikipedia.org/wiki/Wavefront_.obj_file">OBJ</a> format. */
        OBJ
    }

    /**
     * Saves the representation to the given {@code file}.
     *
     * @param type Output format.
     * @param file File.
     * @param precision Precision.
     * @param src Shape boundaries.
     */
    public static void save(Output type,
                            String file,
                            DoublePrecisionContext precision,
                            BoundarySource3D src)
        throws IOException {
        save(type, new File(file), precision, src);
    }

    /**
     * Saves the representation to the given {@code file}.
     *
     * @param type Output format.
     * @param file File.
     * @param precision Precision.
     * @param src Shape boundaries.
     */
    public static void save(Output type,
                            File file,
                            DoublePrecisionContext precision,
                            BoundarySource3D src)
        throws IOException {
        try (Writer w = Files.newBufferedWriter(file.toPath())) {
            w.write(create(type, precision, src));
        }
    }

    /**
     * Creates a string representation.
     *
     * @param type Output format.
     * @param precision Precision.
     * @param src Shape boundaries.
     * @return a string in the given {@code format}.
     */
    public static String create(Output type,
                                DoublePrecisionContext precision,
                                BoundarySource3D src) {
        // Create mesh data (vertices and facets).
        final Mesh mesh = new Mesh(precision);
        try (Stream<PlaneConvexSubset> stream = src.boundaryStream()) {
            stream.forEach(mesh::add);
        }

        // Create output.
        final StringBuilder out = new StringBuilder();

        switch (type) {
        case OBJ:
            ObjWriter.write(mesh.getVertices(), mesh.getFacets(), out);
            break;
        default:
            throw new UnsupportedOperationException("Not implemented");
        }

        return out.toString();
    }

    /**
     * Imposes a strict sorting on 3D vertices.
     * If all of the components of two vertices are within tolerance of each
     * other, then the vertices are considered equal (in order to avoid
     * writing duplicate vertices).
     */
    private static class VertexComparator implements Comparator<Vector3D> {
        /** Precision context to deteremine floating-point equality. */
        private final DoublePrecisionContext precision;

        /**
         * @param precision Precision.
         */
        VertexComparator(final DoublePrecisionContext precision) {
            this.precision = precision;
        }

        /** {@inheritDoc} */
        @Override
        public int compare(final Vector3D a,
                           final Vector3D b) {
            int result = precision.compare(a.getX(), b.getX());
            if (result == 0) {
                result = precision.compare(a.getY(), b.getY());
                if (result == 0) {
                    result = precision.compare(a.getZ(), b.getZ());
                }
            }

            return result;
        }
    }

    /**
     * Extract vertices and facets from {@link BoundarySource3D} instances.
     */
    private static class Mesh {
        /** Map of vertices to their index in the vertices list. */
        private final Map<Vector3D, Integer> vertexIndexMap;
        /** List of unique vertices in the BSPTree boundary. */
        private final List<Vector3D> vertices;
        /** Triangular facets, each composed of 3 indices into {@link #vertices}. */
        private final List<int[]> facets;

        /**
         * @param precision Precision.
         */
        Mesh(DoublePrecisionContext precision) {
            vertexIndexMap = new TreeMap<>(new VertexComparator(precision));
            vertices = new ArrayList<>();
            facets = new ArrayList<>();
        }

        /**
         * @return the list of unique vertices found in the BSPTree.
         */
        List<Vector3D> getVertices() {
            return vertices;
        }

        /**
         * @return the list of facets (composed of vertex indices) for the BSPTree.
         */
        List<int[]> getFacets() {
            return facets;
        }

        /**
         * Adds a plane subset to this mesh.
         *
         * @param boundary Convex plane boundary.
         */
        private void add(final PlaneConvexSubset boundary) {
            if (!boundary.isEmpty()) {
                if (!boundary.isFinite()) {
                    throw new IllegalArgumentException("Cannot add infinite plane subset: " + boundary);
                }

                for (final Triangle3D tri : boundary.toTriangles()) {
                    facets.add(new int[] {
                            getVertexIndex(tri.getPoint1()),
                            getVertexIndex(tri.getPoint2()),
                            getVertexIndex(tri.getPoint3())
                        });
                }
            }
        }

        /**
         * @param vertex Vertex.
         * @return the index of the given vertex in the list of vertices.
         */
        private int getVertexIndex(final Vector3D vertex) {
            Integer idx = vertexIndexMap.get(vertex);
            if (idx == null) {
                idx = vertices.size();
                vertices.add(vertex);
                vertexIndexMap.put(vertex, idx);
            }

            return idx.intValue();
        }
    }

    /**
     * <a href="https://en.wikipedia.org/wiki/Wavefront_.obj_file">OBJ</a> format.
     */
    private static final class ObjWriter {
        /** Utility class. */
        private ObjWriter() {}

        /**
         * @param vertices Vertices.
         * @param facets Facets.
         * @param out Output.
         */
        static void write(List<Vector3D> vertices,
                          List<int[]> facets,
                          StringBuilder out) {
            final String sp = " ";
            final String ls = System.lineSeparator();
            final DecimalFormat df = new DecimalFormat("0.######");

            // Write vertices.
            for (final Vector3D v : vertices) {
                out.append("v")
                    .append(sp)
                    .append(df.format(v.getX()))
                    .append(sp)
                    .append(df.format(v.getY()))
                    .append(sp)
                    .append(df.format(v.getZ()))
                    .append(ls);
            }

            // Write Facets.
            for (int[] f : facets) {
                out.append("f")
                    .append(sp);
                for (int idx : f) {
                    out.append(String.valueOf(idx + 1)) // "OBJ" indices are 1-based.
                        .append(sp);
                }
                out.append(ls);
            }
        }
    }
}
