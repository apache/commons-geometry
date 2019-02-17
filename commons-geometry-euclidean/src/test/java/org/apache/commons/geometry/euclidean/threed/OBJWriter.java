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

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.geometry.core.partitioning.BSPTree_Old;
import org.apache.commons.geometry.core.partitioning.BSPTreeVisitor_Old;
import org.apache.commons.geometry.core.partitioning.BoundaryAttribute;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.twod.PolygonsSet;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

/** This class creates simple OBJ files from {@link PolyhedronsSet} instances.
 * The output files can be opened in a 3D viewer for visual debugging of 3D
 * regions. This class is only intended for use in testing.
 *
 * @see https://en.wikipedia.org/wiki/Wavefront_.obj_file
 */
public class OBJWriter {

    /** Writes an OBJ file representing the given {@link PolyhedronsSet}. Only
     * finite boundaries are written. Infinite boundaries are ignored.
     * @param file The path of the file to write
     * @param poly The input PolyhedronsSet
     * @throws IOException
     */
    public static void write(String file, PolyhedronsSet poly) throws IOException {
        write(new File(file), poly);
    }

    /** Writes an OBJ file representing the given {@link PolyhedronsSet}. Only
     * finite boundaries are written. Infinite boundaries are ignored.
     * @param file The file to write
     * @param poly The input PolyhedronsSet
     * @throws IOException
     */
    public static void write(File file, PolyhedronsSet poly) throws IOException {
        // get the vertices and faces
        MeshBuilder meshBuilder = new MeshBuilder(poly.getPrecision());
        poly.getTree(true).visit(meshBuilder);

        // write them to the file
        try (Writer writer = Files.newBufferedWriter(file.toPath())) {
            writer.write("# Generated by " + OBJWriter.class.getName() + " on " + new Date() + "\n");
            writeVertices(writer, meshBuilder.getVertices());
            writeFaces(writer, meshBuilder.getFaces());
        }
    }

    /** Writes the given list of vertices to the file in the OBJ format.
     * @param writer
     * @param vertices
     * @throws IOException
     */
    private static void writeVertices(Writer writer, List<Vector3D> vertices) throws IOException {
        DecimalFormat df = new DecimalFormat("0.######");

        for (Vector3D v : vertices) {
            writer.write("v ");
            writer.write(df.format(v.getX()));
            writer.write(" ");
            writer.write(df.format(v.getY()));
            writer.write(" ");
            writer.write(df.format(v.getZ()));
            writer.write("\n");
        }
    }

    /** Writes the given list of face vertex indices to the file in the OBJ format. The indices
     * are expected to be 0-based and are converted to the 1-based format used by OBJ.
     * @param writer
     * @param faces
     * @throws IOException
     */
    private static void writeFaces(Writer writer, List<int[]> faces) throws IOException {
        for (int[] face : faces) {
            writer.write("f ");
            for (int idx : face) {
                writer.write(String.valueOf(idx + 1)); // obj indices are 1-based
                writer.write(" ");
            }
            writer.write("\n");
        }
    }

    /** Class used to impose a strict sorting on 3D vertices.
     * If all of the components of two vertices are within tolerance of each
     * other, then the vertices are considered equal. This helps to avoid
     * writing duplicate vertices in the OBJ output.
     */
    private static class VertexComparator implements Comparator<Vector3D> {

        /** Precision context to deteremine floating-point equality */
        private final DoublePrecisionContext precision;

        /** Creates a new instance with the given tolerance value.
         * @param tolerance
         */
        public VertexComparator(final DoublePrecisionContext precision) {
            this.precision = precision;
        }

        /** {@inheritDoc} */
        @Override
        public int compare(Vector3D a, Vector3D b) {
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

    /** Class for converting a 3D BSPTree into a list of vertices
     * and face vertex indices.
     */
    private static class MeshBuilder implements BSPTreeVisitor_Old<Vector3D> {

        /** Precision context to deteremine floating-point equality */
        private final DoublePrecisionContext precision;

        /** Map of vertices to their index in the vertices list */
        private Map<Vector3D, Integer> vertexIndexMap;

        /** List of unique vertices in the BSPTree boundary */
        private List<Vector3D> vertices;

        /**
         * List of face vertex indices. Each face will have 3 indices. Indices
         * are 0-based.
         * */
        private List<int[]> faces;

        /** Creates a new instance with the given tolerance.
         * @param tolerance
         */
        public MeshBuilder(final DoublePrecisionContext precision) {
            this.precision = precision;
            this.vertexIndexMap = new TreeMap<>(new VertexComparator(precision));
            this.vertices = new ArrayList<>();
            this.faces = new ArrayList<>();
        }

        /** Returns the list of unique vertices found in the BSPTree.
         * @return
         */
        public List<Vector3D> getVertices() {
            return vertices;
        }

        /** Returns the list of 0-based face vertex indices for the BSPTree. Each face is
         * a triangle with 3 indices.
         * @return
         */
        public List<int[]> getFaces() {
            return faces;
        }

        /** {@inheritDoc} */
        @Override
        public Order visitOrder(BSPTree_Old<Vector3D> node) {
            return Order.SUB_MINUS_PLUS;
        }

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        public void visitInternalNode(BSPTree_Old<Vector3D> node) {
            BoundaryAttribute<Vector3D> attr = (BoundaryAttribute<Vector3D>) node.getAttribute();

            if (attr.getPlusOutside() != null) {
                addBoundary((SubPlane) attr.getPlusOutside());
            }
            else if (attr.getPlusInside() != null) {
                addBoundary((SubPlane) attr.getPlusInside());
            }
        }

        /** {@inheritDoc} */
        @Override
        public void visitLeafNode(BSPTree_Old<Vector3D> node) {
            // do nothing
        }

        /** Adds the region boundary defined by the given {@link SubPlane}
         * to the mesh.
         * @param subplane
         */
        private void addBoundary(SubPlane subplane) {
            Plane plane = (Plane) subplane.getHyperplane();
            PolygonsSet poly = (PolygonsSet) subplane.getRemainingRegion();

            TriangleExtractor triExtractor = new TriangleExtractor(precision);
            poly.getTree(true).visit(triExtractor);

            Vector3D v1, v2, v3;
            for (Vector2D[] tri : triExtractor.getTriangles()) {
                v1 = plane.toSpace(tri[0]);
                v2 = plane.toSpace(tri[1]);
                v3 = plane.toSpace(tri[2]);

                faces.add(new int[] {
                        getVertexIndex(v1),
                        getVertexIndex(v2),
                        getVertexIndex(v3)
                });
            }
        }

        /** Returns the 0-based index of the given vertex in the <code>vertices</code>
         * list. If the vertex has not been encountered before, it is added
         * to the list.
         * @param vertex
         * @return
         */
        private int getVertexIndex(Vector3D vertex) {
            Integer idx = vertexIndexMap.get(vertex);
            if (idx == null) {
                idx = vertices.size();

                vertices.add(vertex);
                vertexIndexMap.put(vertex, idx);
            }
            return idx.intValue();
        }
    }

    /** Visitor for extracting a collection of triangles from a 2D BSPTree.
     */
    private static class TriangleExtractor implements BSPTreeVisitor_Old<Vector2D> {

        /** Precision context to deteremine floating-point equality */
        private final DoublePrecisionContext precision;

        /** List of extracted triangles */
        private List<Vector2D[]> triangles = new ArrayList<>();

        /** Creates a new instance with the given geometric tolerance.
         * @param tolerance
         */
        public TriangleExtractor(final DoublePrecisionContext precision) {
            this.precision = precision;
        }

        /** Returns the list of extracted triangles.
         * @return
         */
        public List<Vector2D[]> getTriangles() {
            return triangles;
        }

        /** {@inheritDoc} */
        @Override
        public Order visitOrder(BSPTree_Old<Vector2D> node) {
            return Order.SUB_MINUS_PLUS;
        }

        /** {@inheritDoc} */
        @Override
        public void visitInternalNode(BSPTree_Old<Vector2D> node) {
            // do nothing
        }

        /** {@inheritDoc} */
        @Override
        public void visitLeafNode(BSPTree_Old<Vector2D> node) {
            if ((Boolean) node.getAttribute()) {
                PolygonsSet convexPoly = new PolygonsSet(node.pruneAroundConvexCell(Boolean.TRUE,
                        Boolean.FALSE, null), precision);

                for (Vector2D[] loop : convexPoly.getVertices()) {
                    if (loop.length > 0 && loop[0] != null) { // skip unclosed loops
                        addTriangles(loop);
                    }
                }
            }
        }

        /** Splits the 2D convex area defined by the given vertices into
         * triangles and adds them to the internal list.
         * @param vertices
         */
        private void addTriangles(Vector2D[] vertices) {
            // use a triangle fan to add the convex region
            for (int i=2; i<vertices.length; ++i) {
                triangles.add(new Vector2D[] { vertices[0], vertices[i-1], vertices[i] });
            }
        }
    }
}
