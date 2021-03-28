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

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import org.apache.commons.geometry.euclidean.internal.Vectors;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.io.core.internal.SimpleTextParser;

/** Low-level parser class for reading 3D polygon (face) data in the OBJ file format.
 * This class provides access to OBJ data structures but does not retain any of the
 * parsed data. For example, it is up to callers to store vertices as they are parsed
 * for later reference. This allows callers to determine what values are stored and in
 * what format.
 */
public class PolygonObjParser extends AbstractObjParser {

    /** Set containing OBJ keywords commonly used with files containing only polygon content. */
    private static final Set<String> STANDARD_POLYGON_KEYWORDS =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                        ObjConstants.VERTEX_KEYWORD,
                        ObjConstants.VERTEX_NORMAL_KEYWORD,
                        ObjConstants.TEXTURE_COORDINATE_KEYWORD,
                        ObjConstants.FACE_KEYWORD,

                        ObjConstants.OBJECT_KEYWORD,
                        ObjConstants.GROUP_KEYWORD,
                        ObjConstants.SMOOTHING_GROUP_KEYWORD,

                        ObjConstants.MATERIAL_LIBRARY_KEYWORD,
                        ObjConstants.USE_MATERIAL_KEYWORD
                    )));

    /** Number of vertex keywords encountered in the file so far. */
    private int vertexCount;

    /** Number of vertex normal keywords encountered in the file so far. */
    private int vertexNormalCount;

    /** Number of texture coordinate keywords encountered in the file so far. */
    private int textureCoordinateCount;

    /** If true, parsing will fail when non-polygon keywords are encountered in the OBJ content. */
    private boolean failOnNonPolygonKeywords;

    /** Construct a new instance for parsing OBJ content from the given reader.
     * @param reader reader to parser content from
     */
    public PolygonObjParser(final Reader reader) {
        this(new SimpleTextParser(reader));
    }

    /** Construct a new instance for parsing OBJ content from the given text parser.
     * @param parser text parser to read content from
     */
    public PolygonObjParser(final SimpleTextParser parser) {
        super(parser);
    }

    /** Get the number of {@link ObjConstants#VERTEX_KEYWORD vertex keywords} parsed
     * so far.
     * @return the number of vertex keywords parsed so far
     */
    public int getVertexCount() {
        return vertexCount;
    }

    /** Get the number of {@link ObjConstants#VERTEX_NORMAL_KEYWORD vertex normal keywords} parsed
     * so far.
     * @return the number of vertex normal keywords parsed so far
     */
    public int getVertexNormalCount() {
        return vertexNormalCount;
    }

    /** Get the number of {@link ObjConstants#TEXTURE_COORDINATE_KEYWORD texture coordinate keywords} parsed
     * so far.
     * @return the number of texture coordinate keywords parsed so far
     */
    public int getTextureCoordinateCount() {
        return textureCoordinateCount;
    }

    /** Return true if the instance is configured to throw an {@link IOException} when OBJ keywords not commonly
     * used with files containing only polygon data are encountered. The default value is {@code false},
     * meaning that no keyword validation is performed. When set to true, only the following keywords are
     * accepted:
     * <ul>
     *  <li>{@code v}</li>
     *  <li>{@code vn}</li>
     *  <li>{@code vt}</li>
     *  <li>{@code f}</li>
     *  <li>{@code o}</li>
     *  <li>{@code g}</li>
     *  <li>{@code s}</li>
     *  <li>{@code mtllib}</li>
     *  <li>{@code usemtl}</li>
     * </ul>
     * @return true if the instance is configured to fail when a non-polygon keyword is encountered
     */
    public boolean getFailOnNonPolygonKeywords() {
        return failOnNonPolygonKeywords;
    }

    /** Set the flag determining if the instance should throw an {@link IOException} when encountering keywords
     * not commonly used with OBJ files containing only polygon data. If true, only the following keywords are
     * accepted:
     * <ul>
     *  <li>{@code v}</li>
     *  <li>{@code vn}</li>
     *  <li>{@code vt}</li>
     *  <li>{@code f}</li>
     *  <li>{@code o}</li>
     *  <li>{@code g}</li>
     *  <li>{@code s}</li>
     *  <li>{@code mtllib}</li>
     *  <li>{@code usemtl}</li>
     * </ul>
     * If false, all keywords are accepted.
     * @param failOnNonPolygonKeywords new flag value
     */
    public void setFailOnNonPolygonKeywords(final boolean failOnNonPolygonKeywords) {
        this.failOnNonPolygonKeywords = failOnNonPolygonKeywords;
    }

    /** {@inheritDoc} */
    @Override
    protected void handleKeyword(final String keywordValue) throws IOException {
        if (failOnNonPolygonKeywords && !STANDARD_POLYGON_KEYWORDS.contains(keywordValue)) {
            final String allowedKeywords = STANDARD_POLYGON_KEYWORDS.stream()
                    .sorted()
                    .collect(Collectors.joining(", "));

            throw getTextParser().tokenError("expected keyword to be one of [" + allowedKeywords +
                    "] but was [" + keywordValue + "]");
        }

        // update counts in order to validate face vertex attributes
        switch (keywordValue) {
        case ObjConstants.VERTEX_KEYWORD:
            ++vertexCount;
            break;
        case ObjConstants.VERTEX_NORMAL_KEYWORD:
            ++vertexNormalCount;
            break;
        case ObjConstants.TEXTURE_COORDINATE_KEYWORD:
            ++textureCoordinateCount;
            break;
        default:
            break;
        }
    }

    /** Read an OBJ face definition from the current line.
     * @return OBJ face definition read from the current line
     * @throws IOException if an I/O error occurs
     * @throws IllegalStateException if a face definition is not able to be parsed
     */
    public Face readFace() throws IOException {
        final List<VertexAttributes> vertices = new ArrayList<>();

        while (nextDataLineContent()) {
            vertices.add(readFaceVertex());
        }

        if (vertices.size() < 3) {
            throw getTextParser().parseError(
                    "face must contain at least 3 vertices but found only " + vertices.size());
        }

        discardDataLine();

        return new Face(vertices);
    }

    /** Read an OBJ face vertex definition from the current parser position.
     * @return OBJ face vertex definition
     * @throws IOException if an I/O error occurs
     * @throws IllegalStateException if a vertex definition is not able to be parsed
     */
    private VertexAttributes readFaceVertex() throws IOException {
        final SimpleTextParser parser = getTextParser();

        discardDataLineWhitespace();

        final int vertexIndex = readNormalizedVertexAttributeIndex("vertex", vertexCount);

        int textureIndex = -1;
        if (parser.peekChar() == ObjConstants.FACE_VERTEX_ATTRIBUTE_SEP_CHAR) {
            parser.discard(1);

            if (parser.peekChar() != ObjConstants.FACE_VERTEX_ATTRIBUTE_SEP_CHAR) {
                textureIndex = readNormalizedVertexAttributeIndex("texture", textureCoordinateCount);
            }
        }

        int normalIndex = -1;
        if (parser.peekChar() == ObjConstants.FACE_VERTEX_ATTRIBUTE_SEP_CHAR) {
            parser.discard(1);

            if (SimpleTextParser.isIntegerPart(parser.peekChar())) {
                normalIndex = readNormalizedVertexAttributeIndex("normal", vertexNormalCount);
            }
        }

        return new VertexAttributes(vertexIndex, textureIndex, normalIndex);
    }

    /** Read a vertex attribute index from the current parser position and normalize it to
     * be 0-based and positive.
     * @param type type of attribute being read; this value is used in error messages
     * @param available number of available values of the given type parsed from the content
     *      so far
     * @return 0-based positive attribute index
     * @throws IOException if an I/O error occurs
     * @throws IllegalStateException if the integer index cannot be parsed or the index is
     *      out of range for the number of parsed elements of the given type
     */
    private int readNormalizedVertexAttributeIndex(final String type, final int available) throws IOException {
        final SimpleTextParser parser = getTextParser();

        final int objIndex = parser
                .nextWithLineContinuation(ObjConstants.LINE_CONTINUATION_CHAR, SimpleTextParser::isIntegerPart)
                .getCurrentTokenAsInt();

        final int normalizedIndex = objIndex < 0 ?
                available + objIndex :
                objIndex - 1;

        if (normalizedIndex < 0 || normalizedIndex >= available) {
            final StringBuilder err = new StringBuilder();
            err.append(type)
                .append(" index ");

            if (available < 1) {
                err.append("cannot be used because no values of that type have been defined");
            } else {
                err.append("must evaluate to be within the range [1, ")
                    .append(available)
                    .append("] but was ")
                    .append(objIndex);
            }

            throw parser.tokenError(err.toString());
        }

        return normalizedIndex;
    }

    /** Class representing an OBJ face definition. Faces are defined with the format
     * <p>
     *  <code>
     *      f v<sub>1</sub>/vt<sub>1</sub>/vn<sub>1</sub> v<sub>2</sub>/vt<sub>2</sub>/vn<sub>2</sub> v<sub>3</sub>/vt<sub>3</sub>/vn<sub>3</sub> ...
     *  </code>
     * </p>
     * <p>where the {@code v} elements are indices into the model vertices, the {@code vt}
     * elements are indices into the model texture coordinates, and the {@code vn} elements
     * are indices into the model normal coordinates. Only the vertex indices are required.</p>
     *
     * <p>All vertex attribute indices are normalized to be 0-based and positive and all
     * faces are assumed to define geometrically valid convex polygons.</p>
     */
    public static final class Face {

        /** List of vertex attributes for the face. */
        private final List<VertexAttributes> vertexAttributes;

        /** Construct a new instance with the given vertex attributes.
         * @param vertexAttributes face vertex attributes
         */
        Face(final List<VertexAttributes> vertexAttributes) {
            this.vertexAttributes = Collections.unmodifiableList(vertexAttributes);
        }

        /** Get the list of vertex attributes for the instance.
         * @return list of vertex attribute
         */
        public List<VertexAttributes> getVertexAttributes() {
            return vertexAttributes;
        }

        /** Get a composite normal for the face by computing the sum of all defined vertex
         * normals and normalizing the result. Null is returned if no vertex normals are
         * defined or the defined normals sum to zero.
         * @param modelNormalFn function used to access normals parsed earlier in the model;
         *      callers are responsible for storing these values as they are parsed
         * @return composite face normal or null if no composite normal can be determined from the
         *      normals defined for the face
         */
        public Vector3D getDefinedCompositeNormal(final IntFunction<Vector3D> modelNormalFn) {
            Vector3D sum = Vector3D.ZERO;

            int normalIdx;
            for (final VertexAttributes vertex : vertexAttributes) {
                normalIdx = vertex.getNormalIndex();
                if (normalIdx > -1) {
                    sum = sum.add(modelNormalFn.apply(normalIdx));
                }
            }

            return Vectors.tryNormalize(sum);
        }

        /** Compute a normal for the face using its first three vertices. The vertices will wind in a
         * counter-clockwise direction when viewed looking down the returned normal. Null is returned
         * if the normal could not be determined, which would be the case if the vertices lie in the
         * same line or two or more are equal.
         * @param modelVertexFn function used to access model vertices parsed earlier in the content;
         *      callers are responsible for storing these values as they are passed
         * @return a face normal computed from the first 3 vertices or null if a normal cannot
         *      be determined
         */
        public Vector3D computeNormalFromVertices(final IntFunction<Vector3D> modelVertexFn) {
            final Vector3D p0 = modelVertexFn.apply(vertexAttributes.get(0).getVertexIndex());
            final Vector3D p1 = modelVertexFn.apply(vertexAttributes.get(1).getVertexIndex());
            final Vector3D p2 = modelVertexFn.apply(vertexAttributes.get(2).getVertexIndex());

            return Vectors.tryNormalize(p0.vectorTo(p1).cross(p0.vectorTo(p2)));
        }

        /** Get the vertex attributes for the face listed in the order that produces a counter-clockwise
         * winding of vertices when viewed looking down the given normal direction. If {@code normal}
         * is null, the original vertex sequence is used.
         * @param normal requested face normal; may be null
         * @param modelVertexFn function used to access model vertices parsed earlier in the content;
         *      callers are responsible for storing these values as they are passed
         * @return list of vertex attributes for the face, oriented to correspond with the given
         *      face normal
         */
        public List<VertexAttributes> getVertexAttributesCounterClockwise(final Vector3D normal,
                final IntFunction<Vector3D> modelVertexFn) {
            List<VertexAttributes> result = vertexAttributes;

            if (normal != null) {
                final Vector3D computedNormal = computeNormalFromVertices(modelVertexFn);
                if (computedNormal != null && normal.dot(computedNormal) < 0) {
                    // face is oriented the opposite way; reverse the order of the vertices
                    result = new ArrayList<>(vertexAttributes);
                    Collections.reverse(result);
                }
            }

            return result;
        }

        /** Get the face vertices in the order defined in the face definition.
         * @param modelVertexFn function used to access model vertices parsed earlier in the content;
         *      callers are responsible for storing these values as they are passed
         * @return face vertices in their defined ordering
         */
        public List<Vector3D> getVertices(final IntFunction<Vector3D> modelVertexFn) {
            return vertexAttributes.stream()
                    .map(v -> modelVertexFn.apply(v.getVertexIndex()))
                    .collect(Collectors.toList());
        }

        /** Get the face vertices in the order that produces a counter-clockwise winding when viewed
         * looking down the given normal.
         * @param normal requested face normal
         * @param modelVertexFn function used to access model vertices parsed earlier in the content;
         *      callers are responsible for storing these values as they are passed
         * @return face vertices in the order that produces a counter-clockwise winding when viewed
         *      looking down the given normal
         * @see #getVertexAttributesCounterClockwise(Vector3D, IntFunction)
         */
        public List<Vector3D> getVerticesCounterClockwise(final Vector3D normal,
                final IntFunction<Vector3D> modelVertexFn) {
            return getVertexAttributesCounterClockwise(normal, modelVertexFn).stream()
                    .map(v -> modelVertexFn.apply(v.getVertexIndex()))
                    .collect(Collectors.toList());
        }

        /** Get the vertex indices for the face.
         * @return vertex indices for the face
         */
        public int[] getVertexIndices() {
            return getIndices(VertexAttributes::getVertexIndex);
        }

        /** Get the texture indices for the face. The value {@code -1} is used if a texture index
         * is not set.
         * @return texture indices
         */
        public int[] getTextureIndices() {
            return getIndices(VertexAttributes::getTextureIndex);
        }

        /** Get the normal indices for the face. The value {@code -1} is used if a texture index
         * is not set.
         * @return normal indices
         */
        public int[] getNormalIndices() {
            return getIndices(VertexAttributes::getNormalIndex);
        }

        /** Get indices for the face, using the given function to extract the value from
         * the vertex attributes.
         * @param fn function used to extract the required value from each vertex attribute
         * @return extracted indices
         */
        private int[] getIndices(final ToIntFunction<VertexAttributes> fn) {
            final int len = vertexAttributes.size();
            final int[] indices = new int[len];

            for (int i = 0; i < len; ++i) {
                indices[i] = fn.applyAsInt(vertexAttributes.get(i));
            }

            return indices;
        }
    }

    /** Class representing a set of attributes for a face vertex. All index values are 0-based
     * and positive, in contrast with OBJ indices which are 1-based and support negative
     * values. If an index value is not given in the OBJ content, it is set to {@code -1}.
     */
    public static final class VertexAttributes {

        /** Vertex index. */
        private final int vertexIndex;

        /** Texture coordinate index. */
        private final int textureIndex;

        /** Vertex normal index. */
        private final int normalIndex;

        /** Construct a new instance with the given vertices.
         * @param vertexIndex vertex index
         * @param textureIndex texture index
         * @param normalIndex vertex normal index
         */
        VertexAttributes(final int vertexIndex, final int textureIndex, final int normalIndex) {
            this.vertexIndex = vertexIndex;
            this.textureIndex = textureIndex;
            this.normalIndex = normalIndex;
        }

        /** Get the vertex position index for this instance. This value is required and is guaranteed to
         * be a valid index into the list of vertex positions parsed so far in the OBJ content.
         * @return vertex index
         */
        public int getVertexIndex() {
            return vertexIndex;
        }

        /** Get the texture index for this instance or {@code -1} if not specified in the
         * OBJ content.
         * @return texture index or {@code -1} if not specified in the OBJ content.
         */
        public int getTextureIndex() {
            return textureIndex;
        }

        /** Get the normal index for this instance or {@code -1} if not specified in the
         * OBJ content.
         * @return normal index or {@code -1} if not specified in the OBJ content.
         */
        public int getNormalIndex() {
            return normalIndex;
        }
    }
}
