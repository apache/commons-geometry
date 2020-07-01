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
package org.apache.commons.geometry.euclidean.threed.mesh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.Bounds3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.Triangle3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

/** A simple implementation of the {@link TriangleMesh} interface. This class ensures that
 * faces always contain 3 valid references into the vertex list but does not enforce that
 * the referenced vertices are unique or that they define a triangle with non-zero size. For
 * example, a mesh could contain a face with 3 vertices that are considered equivalent by the
 * configured precision context. Attempting to call the {@link TriangleMesh.Face#getPolygon()}
 * method on such a face results in an exception. The
 * {@link TriangleMesh.Face#definesPolygon()} method can be used to determine if a face defines
 * a valid triangle.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public final class SimpleTriangleMesh implements TriangleMesh {

    /** Vertices in the mesh. */
    private final List<Vector3D> vertices;

    /** Faces in the mesh. */
    private final List<int[]> faces;

    /** The bounds of the mesh. */
    private final Bounds3D bounds;

    /** Object used for floating point comparisons. */
    private final DoublePrecisionContext precision;

    /** Construct a new instance from a vertex list and set of faces. No validation is
     * performed on the input.
     * @param vertices vertex list
     * @param faces face indices list
     * @param bounds mesh bounds
     * @param precision precision context used when creating face polygons
     */
    private SimpleTriangleMesh(final List<Vector3D> vertices, final List<int[]> faces, final Bounds3D bounds,
            final DoublePrecisionContext precision) {
        this.vertices = Collections.unmodifiableList(vertices);
        this.faces = Collections.unmodifiableList(faces);
        this.bounds = bounds;
        this.precision = precision;
    }

    /** {@inheritDoc} */
    @Override
    public Iterable<Vector3D> vertices() {
        return getVertices();
    }

    /** {@inheritDoc} */
    @Override
    public List<Vector3D> getVertices() {
        return vertices;
    }

    /** {@inheritDoc} */
    @Override
    public int getVertexCount() {
        return vertices.size();
    }

    /** {@inheritDoc} */
    @Override
    public Iterable<TriangleMesh.Face> faces() {
        return () -> {
            return new FaceIterator<Face>(Function.identity());
        };
    }

    /** {@inheritDoc} */
    @Override
    public List<TriangleMesh.Face> getFaces() {
        final int count = getFaceCount();

        final List<Face> faceList = new ArrayList<>(count);
        for (int i = 0; i < count; ++i) {
            faceList.add(getFace(i));
        }

        return faceList;
    }

    /** {@inheritDoc} */
    @Override
    public int getFaceCount() {
        return faces.size();
    }

    /** {@inheritDoc} */
    @Override
    public TriangleMesh.Face getFace(final int index) {
        return new SimpleTriangleFace(index, faces.get(index));
    }

    /** {@inheritDoc} */
    @Override
    public Bounds3D getBounds() {
        return bounds;
    }

    /** Get the precision context for the mesh. This context is used during construction of
     * face {@link Triangle3D} instances.
     * @return the precision context for the mesh
     */
    public DoublePrecisionContext getPrecision() {
        return precision;
    }

    /** {@inheritDoc} */
    @Override
    public Stream<PlaneConvexSubset> boundaryStream() {
        return createFaceStream(Face::getPolygon);
    }

    /** {@inheritDoc} */
    @Override
    public Stream<Triangle3D> triangleStream() {
        return createFaceStream(Face::getPolygon);
    }

    /** {@inheritDoc} */
    @Override
    public SimpleTriangleMesh transform(final Transform<Vector3D> transform) {
        // only the vertices and bounds are modified; the faces are the same
        final Bounds3D.Builder boundsBuilder = Bounds3D.builder();
        final List<Vector3D> tVertices = vertices.stream()
                .map(transform)
                .peek(boundsBuilder::add)
                .collect(Collectors.toList());

        final Bounds3D tBounds = boundsBuilder.hasBounds() ?
                boundsBuilder.build() :
                null;

        return new SimpleTriangleMesh(tVertices, faces, tBounds, precision);
    }

    /** Return this instance if the given precision context is equal to the current precision context.
     * Otherwise, create a new mesh with the given precision context but the same vertices, faces, and
     * bounds.
     * @param meshPrecision precision context to use when generating face polygons
     * @return a mesh instance with the given precision context and the same mesh structure as the current
     *      instance
     */
    @Override
    public SimpleTriangleMesh toTriangleMesh(final DoublePrecisionContext meshPrecision) {
        if (this.precision.equals(meshPrecision)) {
            return this;
        }

        return new SimpleTriangleMesh(vertices, faces, bounds, meshPrecision);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
            .append("[vertexCount= ")
            .append(getVertexCount())
            .append(", faceCount= ")
            .append(getFaceCount())
            .append(", bounds= ")
            .append(getBounds())
            .append(']');

        return sb.toString();
    }

    /** Create a stream containing the results of applying {@code fn} to each face in
     * the mesh.
     * @param <T> Stream element type
     * @param fn function used to extract the stream values from each face
     * @return a stream containing the results of applying {@code fn} to each face in
     *      the mesh
     */
    private <T> Stream<T> createFaceStream(final Function<TriangleMesh.Face, T> fn) {
        final Iterable<T> iterable = () -> new FaceIterator<>(fn);
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    /** Return a builder for creating new triangle mesh objects.
     * @param precision precision object used for floating point comparisons
     * @return a builder for creating new triangle mesh objects
     */
    public static Builder builder(final DoublePrecisionContext precision) {
        return new Builder(precision);
    }

    /** Construct a new triangle mesh from the given vertices and face indices.
     * @param vertices vertices for the mesh
     * @param faces face indices for the mesh
     * @param precision precision context used for floating point comparisons
     * @return a new triangle mesh instance
     * @throws IllegalArgumentException if any of the face index arrays does not have exactly 3 elements or
     *       if any index is not a valid index into the vertex list
     */
    public static SimpleTriangleMesh from(final Vector3D[] vertices, int[][] faces,
            final DoublePrecisionContext precision) {
        return from(Arrays.asList(vertices), Arrays.asList(faces), precision);
    }

    /** Construct a new triangle mesh from the given vertices and face indices.
     * @param vertices vertices for the mesh
     * @param faces face indices for the mesh
     * @param precision precision context used for floating point comparisons
     * @return a new triangle mesh instance
     * @throws IllegalArgumentException if any of the face index arrays does not have exactly 3 elements or
     *       if any index is not a valid index into the vertex list
     */
    public static SimpleTriangleMesh from(final Collection<Vector3D> vertices, Collection<int[]> faces,
            final DoublePrecisionContext precision) {
        final Builder builder = builder(precision);

        return builder.addVertices(vertices)
                .addFaces(faces)
                .build();
    }

    /** Construct a new mesh instance containing all triangles from the given boundary
     * source. Equivalent vertices are reused wherever possible.
     * @param boundarySrc boundary source to construct a mesh from
     * @param precision precision context used for floating point comparisons
     * @return new mesh instance containing all triangles from the given boundary
     *      source
     * @throws IllegalStateException if any boundary in the boundary source has infinite size and cannot
     *      be converted to triangles
     */
    public static SimpleTriangleMesh from(final BoundarySource3D boundarySrc, final DoublePrecisionContext precision) {
        final Builder builder = builder(precision);
        try (Stream<Triangle3D> stream = boundarySrc.triangleStream()) {
            stream.forEach(tri -> {
                builder.addFaceUsingVertices(
                        tri.getPoint1(),
                        tri.getPoint2(),
                        tri.getPoint3());
            });
        }

        return builder.build();
    }

    /** Internal implementation of {@link TriangleMesh.Face}.
     */
    private final class SimpleTriangleFace implements TriangleMesh.Face {

        /** The index of the face in the mesh. */
        private final int index;

        /** Vertex indices for the face. */
        private final int[] vertexIndices;

        SimpleTriangleFace(final int index, final int[] vertexIndices) {
            this.index = index;
            this.vertexIndices = vertexIndices;
        }

        /** {@inheritDoc} */
        @Override
        public int getIndex() {
            return index;
        }

        /** {@inheritDoc} */
        @Override
        public int[] getVertexIndices() {
            return vertexIndices.clone();
        }

        /** {@inheritDoc} */
        @Override
        public List<Vector3D> getVertices() {
            return Arrays.asList(
                    getPoint1(),
                    getPoint2(),
                    getPoint3());
        }

        /** {@inheritDoc} */
        @Override
        public Vector3D getPoint1() {
            return vertices.get(vertexIndices[0]);
        }

        /** {@inheritDoc} */
        @Override
        public Vector3D getPoint2() {
            return vertices.get(vertexIndices[1]);
        }

        /** {@inheritDoc} */
        @Override
        public Vector3D getPoint3() {
            return vertices.get(vertexIndices[2]);
        }

        /** {@inheritDoc} */
        @Override
        public boolean definesPolygon() {
            final Vector3D p1 = getPoint1();
            final Vector3D v1 = p1.vectorTo(getPoint2());
            final Vector3D v2 = p1.vectorTo(getPoint3());

            return !precision.eqZero(v1.cross(v2).norm());
        }

        /** {@inheritDoc} */
        @Override
        public Triangle3D getPolygon() {
            return Planes.triangleFromVertices(
                    getPoint1(),
                    getPoint2(),
                    getPoint3(),
                    precision);
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append(getClass().getSimpleName())
                .append("[index= ")
                .append(getIndex())
                .append(", vertexIndices= ")
                .append(Arrays.toString(getVertexIndices()))
                .append(", vertices= ")
                .append(getVertices())
                .append(']');

            return sb.toString();
        }
    }

    /** Internal class for iterating through the mesh faces and extracting a value from each.
     * @param <T> Type returned by the iterator
     */
    private final class FaceIterator<T> implements Iterator<T> {

        /** The current index of the iterator. */
        private int index = 0;

        /** Function to apply to each face in the mesh. */
        private final Function<TriangleMesh.Face, T> fn;

        /** Construct a new instance for iterating through the mesh faces and extracting
         * a value from each.
         * @param fn function to apply to each face in order to obtain the iterated value
         */
        FaceIterator(final Function<TriangleMesh.Face, T> fn) {
            this.fn = fn;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return index < faces.size();
        }

        /** {@inheritDoc} */
        @Override
        public T next() {
            final Face face = getFace(index++);
            return fn.apply(face);
        }
    }

    /** Builder class for creating mesh instances.
     */
    public static final class Builder {

        /** List of vertices. */
        private final ArrayList<Vector3D> vertices = new ArrayList<>();

        /** Map of vertices to their first occurrence in the vertex list. */
        private Map<Vector3D, Integer> vertexIndexMap;

        /** List of face vertex indices. */
        private final ArrayList<int[]> faces = new ArrayList<>();

        /** Object used to construct the 3D bounds of the vertex list. */
        private final Bounds3D.Builder boundsBuilder = Bounds3D.builder();

        /** Precision context used for floating point comparisons; this value may be null
         * if vertices are not to be combined in this builder.
         */
        private final DoublePrecisionContext precision;

        /** Flag set to true once a mesh is constructed from this builder. */
        private boolean built = false;

        /** Construct a new builder.
         * @param precision precision context used for floating point comparisons; may
         *      be null if vertices are not to be combined in this builder.
         */
        private Builder(final DoublePrecisionContext precision) {
            Objects.requireNonNull(precision, "Precision context must not be null");

            this.precision = precision;
        }

        /** Use a vertex in the constructed mesh. If an equivalent vertex already exist, as determined
         * by the configured {@link DoublePrecisionContext}, then the index of the previously added
         * vertex is returned. Otherwise, the given vertex is added to the vertex list and the index
         * of the new entry is returned. This is in contrast with the {@link #addVertex(Vector3D)},
         * which always adds a new entry to the vertex list.
         * @param vertex vertex to use
         * @return the index of the added vertex or an equivalent vertex that was added previously
         * @see #addVertex(Vector3D)
         */
        public int useVertex(final Vector3D vertex) {
            final int nextIdx = vertices.size();
            final int actualIdx = addToVertexIndexMap(vertex, nextIdx, getVertexIndexMap());

            // add to the vertex list if not already present
            if (actualIdx == nextIdx) {
                addToVertexList(vertex);
            }

            return actualIdx;
        }

        /** Add a vertex directly to the vertex list, returning the index of the added vertex.
         * The vertex is added regardless of whether or not an equivalent vertex already
         * exists in the list. This is in contrast with the {@link #useVertex(Vector3D)} method,
         * which only adds a new entry to the vertex list if an equivalent one does not
         * already exist.
         * @param vertex the vertex to append
         * @return the index of the appended vertex in the vertex list
         */
        public int addVertex(final Vector3D vertex) {
            final int idx = addToVertexList(vertex);

            if (vertexIndexMap != null) {
                // add to the map in order to keep it in sync
                addToVertexIndexMap(vertex, idx, vertexIndexMap);
            }

            return idx;
        }

        /** Add a group of vertices directly to the vertex list. No equivalent vertices are reused.
         * @param newVertices vertices to append
         * @return this instance
         * @see #addVertex(Vector3D)
         */
        public Builder addVertices(final Vector3D[] newVertices) {
            return addVertices(Arrays.asList(newVertices));
        }

        /** Add a group of vertices directly to the vertex list. No equivalent vertices are reused.
         * @param newVertices vertices to append
         * @return this instance
         * @see #addVertex(Vector3D)
         */
        public Builder addVertices(final Collection<Vector3D> newVertices) {
            final int newSize = vertices.size() + newVertices.size();
            ensureVertexCapacity(newSize);

            for (final Vector3D vertex : newVertices) {
                addVertex(vertex);
            }

            return this;
        }

        /** Ensure that this instance has enough capacity to store at least {@code numVertices}
         * number of vertices without reallocating space. This can be used to help improve performance
         * and memory usage when creating meshes with large numbers of vertices.
         * @param numVertices the number of vertices to ensure that this instance can contain
         * @return this instance
         */
        public Builder ensureVertexCapacity(final int numVertices) {
            vertices.ensureCapacity(numVertices);
            return this;
        }

        /** Get the current number of vertices in this mesh.
         * @return the current number of vertices in this mesh
         */
        public int getVertexCount() {
            return vertices.size();
        }

        /** Append a face to this mesh.
         * @param index1 index of the first vertex in the face
         * @param index2 index of the second vertex in the face
         * @param index3 index of the third vertex in the face
         * @return this instance
         * @throws IllegalArgumentException if any of the arguments is not a valid index into
         *      the current vertex list
         */
        public Builder addFace(final int index1, final int index2, final int index3) {
            validateCanModify();

            final int[] indices = {
                validateVertexIndex(index1),
                validateVertexIndex(index2),
                validateVertexIndex(index3)
            };

            faces.add(indices);

            return this;
        }

        /** Append a group of faces to this mesh.
         * @param faceIndices faces to append
         * @return this instance
         * @throws IllegalArgumentException if any of the face index arrays does not have exactly 3 elements or
         *       if any index is not a valid index into the current vertex list
         */
        public Builder addFaces(final int[][] faceIndices) {
            return addFaces(Arrays.asList(faceIndices));
        }

        /** Append a group of faces to this mesh.
         * @param faceIndices faces to append
         * @return this instance
         * @throws IllegalArgumentException if any of the face index arrays does not have exactly 3 elements or
         *       if any index is not a valid index into the current vertex list
         */
        public Builder addFaces(final Collection<int[]> faceIndices) {
            final int newSize = faces.size() + faceIndices.size();
            ensureFaceCapacity(newSize);

            for (final int[] face : faceIndices) {
                if (face.length != 3) {
                    throw new IllegalArgumentException("Face must contain 3 vertex indices; found " + face.length);
                }

                addFace(face[0], face[1], face[2]);
            }

            return this;
        }

        /** Add a face to this mesh, only adding vertices to the vertex list if equivalent vertices are
         * not found.
         * @param p1 first face vertex
         * @param p2 second face vertex
         * @param p3 third face vertex
         * @return this instance
         * @see #useVertex(Vector3D)
         */
        public Builder addFaceUsingVertices(final Vector3D p1, final Vector3D p2, final Vector3D p3) {
            return addFace(
                        useVertex(p1),
                        useVertex(p2),
                        useVertex(p3)
                    );
        }

        /** Add a face and its vertices to this mesh. The vertices are always added to the vertex list,
         * regardless of whether or not equivalent vertices exist in the vertex list.
         * @param p1 first face vertex
         * @param p2 second face vertex
         * @param p3 third face vertex
         * @return this instance
         * @see #addVertex(Vector3D)
         */
        public Builder addFaceAndVertices(final Vector3D p1, final Vector3D p2, final Vector3D p3) {
            return addFace(
                        addVertex(p1),
                        addVertex(p2),
                        addVertex(p3)
                    );
        }

        /** Ensure that this instance has enough capacity to store at least {@code numFaces}
         * number of faces without reallocating space. This can be used to help improve performance
         * and memory usage when creating meshes with large numbers of faces.
         * @param numFaces the number of faces to ensure that this instance can contain
         * @return this instance
         */
        public Builder ensureFaceCapacity(final int numFaces) {
            faces.ensureCapacity(numFaces);
            return this;
        }

        /** Get the current number of faces in this mesh.
         * @return the current number of faces in this meshr
         */
        public int getFaceCount() {
            return faces.size();
        }

        /** Build a triangle mesh containing the vertices and faces in this builder.
         * @return a triangle mesh containing the vertices and faces in this builder
         */
        public SimpleTriangleMesh build() {
            built = true;

            final Bounds3D bounds = boundsBuilder.hasBounds() ?
                    boundsBuilder.build() :
                    null;

            vertices.trimToSize();
            faces.trimToSize();

            return new SimpleTriangleMesh(
                    vertices,
                    faces,
                    bounds,
                    precision);
        }

        /** Get the vertex index map, creating and initializing it if needed.
         * @return the vertex index map
         */
        private Map<Vector3D, Integer> getVertexIndexMap() {
            if (vertexIndexMap == null) {
                vertexIndexMap = new TreeMap<>(new FuzzyVectorComparator(precision));

                // populate the index map
                final int size = vertices.size();
                for (int i = 0; i < size; ++i) {
                    addToVertexIndexMap(vertices.get(i), i, vertexIndexMap);
                }
            }
            return vertexIndexMap;
        }

        /** Add a vertex to the given vertex index map. The vertex is inserted and mapped to {@code targetidx}
         *  if an equivalent vertex does not already exist. The index now associated with the given vertex
         *  or its equivalent is returned.
         * @param vertex vertex to add
         * @param targetIdx the index to associate with the vertex if no equivalent vertex has already been
         *      mapped
         * @param map vertex index map
         * @return the index now associated with the given vertex or its equivalent
         */
        private int addToVertexIndexMap(final Vector3D vertex, final int targetIdx, final Map<Vector3D, Integer> map) {
            validateCanModify();

            final Integer actualIdx = map.putIfAbsent(vertex, targetIdx);

            return actualIdx != null ?
                    actualIdx.intValue() :
                    targetIdx;
        }

        /** Append the given vertex to the end of the vertex list. The index of the vertex is returned.
         * @param vertex the vertex to append
         * @return the index of the appended vertex
         */
        private int addToVertexList(final Vector3D vertex) {
            validateCanModify();

            boundsBuilder.add(vertex);

            int idx = vertices.size();
            vertices.add(vertex);

            return idx;
        }

        /** Throw an exception if the given vertex index is not valid.
         * @param idx vertex index to validate
         * @return the validated index
         * @throws IllegalArgumentException if the given index is not a valid index into
         *      the vertices list
         */
        private int validateVertexIndex(final int idx) {
            if (idx < 0 || idx >= vertices.size()) {
                throw new IllegalArgumentException("Invalid vertex index: " + idx);
            }

            return idx;
        }

        /** Throw an exception if the builder has been used to construct a mesh instance
         * and can no longer be modified.
         */
        private void validateCanModify() {
            if (built) {
                throw new IllegalStateException("Builder instance cannot be modified: mesh construction is complete");
            }
        }
    }

    /** Comparator used to sort vectors using non-strict ("fuzzy") comparisons.
     * Vectors are considered equal if their values in all coordinate dimensions
     * are equivalent as evaluated by the precision context.
     */
    private static final class FuzzyVectorComparator implements Comparator<Vector3D> {
        /** Precision context to determine floating-point equality. */
        private final DoublePrecisionContext precision;

        /** Construct a new instance that uses the given precision context for
         * floating point comparisons.
         * @param precision precision context used for floating point comparisons
         */
        FuzzyVectorComparator(final DoublePrecisionContext precision) {
            this.precision = precision;
        }

        /** {@inheritDoc} */
        @Override
        public int compare(final Vector3D a, final Vector3D b) {
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
}
