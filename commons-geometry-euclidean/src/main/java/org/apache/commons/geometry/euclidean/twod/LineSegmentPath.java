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
package org.apache.commons.geometry.euclidean.twod;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** Class representing a sequence of one or more line segments connected end
 * to end.
 *
 * <p>This class is guaranteed to be immutable.</p>
 */
public class LineSegmentPath implements Serializable {

    /** Serializable UID */
    private static final long serialVersionUID = 20190522L;

    /** List of line segments comprising the path. */
    private List<LineSegment> segments;

    /** Simple constructor. No validation is performed on the input segments.
     * @param segments line segments comprising the path
     */
    private LineSegmentPath(final List<LineSegment> segments) {
        this.segments = Collections.unmodifiableList(segments);
    }

    /** Get the line segments comprising the path.
     * @return the line segments comprising the path
     */
    public List<LineSegment> getSegments() {
        return segments;
    }

    /** Get the start segment for the path or null if the path is empty.
     * @return the start segment for the path or null if the path is empty
     */
    public LineSegment getStart() {
        if (!isEmpty()) {
            return segments.get(0);
        }
        return null;
    }

    /** Get the end segment for the path or null if the path is empty.
     * @return the end segment for the path or null if the path is empty
     */
    public LineSegment getEnd() {
        if (!isEmpty()) {
            return segments.get(segments.size() - 1);
        }
        return null;
    }

    /** Get the start vertex for the path or null if the path is empty
     * or has an infinite start segment.
     * @return the start vertex for the path
     */
    public Vector2D getStartVertex() {
        final LineSegment seg = getStart();
        return (seg != null) ? seg.getStart() : null;
    }

    /** Get the end vertex for the path or null if the path is empty
     * or has an infinite end segment.
     * @return the end vertex for the path
     */
    public Vector2D getEndVertex() {
        final LineSegment seg = getEnd();
        return (seg != null) ? seg.getEnd() : null;
    }

    /** Get the vertices contained in the path in the order they appear.
     * Closed paths contain the start point at the beginning of the list
     * as well as the end.
     * @return the vertices contained in the path in order they appear
     */
    public List<Vector2D> getVertices() {
        final List<Vector2D> vertices = new ArrayList<>();

        Vector2D pt;

        // add the start point, if present
        pt = getStartVertex();
        if (pt != null) {
            vertices.add(pt);
        }

        // add end points
        for (LineSegment seg : segments) {
            pt = seg.getEnd();
            if (pt != null) {
                vertices.add(pt);
            }
        }

        return vertices;
    }

    /** Return true if the path has a start of end line segment that
     * extends to infinity.
     * @return true if the path is infinite
     */
    public boolean isInfinite() {
        return !isEmpty() && (getStartVertex() == null || getEndVertex() == null);
    }

    /** Return true if the path does not contain any line segments.
     * @return true if the path does not contain any line segments
     */
    public boolean isEmpty() {
        return segments.isEmpty();
    }

    /** Return true if the path is closed, meaning that the end
     * point for the last line segment is equal to the start point
     * for the path.
     * @return true if the end point for the last line segment is
     *      equal to the start point for the path.
     */
    public boolean isClosed() {
        final LineSegment endSegment = getEnd();

        if (endSegment != null) {
            final Vector2D start = getStartVertex();
            final Vector2D end = endSegment.getEnd();

            return start != null && end != null && start.eq(end, endSegment.getPrecision());
        }

        return false;
    }

    /** Construct a {@link RegionBSPTree2D} from the line segments in this instance.
     * @return a bsp tree constructed from the line segments in this instance
     */
    public RegionBSPTree2D toTree() {
        RegionBSPTree2D tree = RegionBSPTree2D.empty();

        for (LineSegment segment : segments) {
            tree.insert(segment);
        }

        return tree;
    }

    /** Return a default {@link LineSegmentPathBuilder} instance. The returned instance
     * must be given a precision context if raw vertices are to be used in the construction
     * of the path.
     * @return a default {@link LineSegmentPathBuilder} instance
     * @see LineSegmentPathBuilder#LineSegmentPathBuilder()
     */
    public static LineSegmentPathBuilder builder() {
        return new LineSegmentPathBuilder();
    }

    /** Return a {@link LineSegmentPathBuilder} instance configured with the given precision
     * context.
     * @param precision precision context to use when building line segments from raw vertices
     * @return a new {@link LineSegmentPathBuilder} instance
     * @see LineSegmentPathBuilder#LineSegmentPathBuilder(DoublePrecisionContext)
     */
    public static LineSegmentPathBuilder builder(final DoublePrecisionContext precision) {
        return new LineSegmentPathBuilder(precision);
    }

    /** Build a new line segment path from the given segments.
     * @param segments the segment to comprise the path
     * @return new line segment path containing the given line segment in order
     * @throws IllegalStateException if the segments to not form a connected path
     */
    public static LineSegmentPath fromSegments(final LineSegment ... segments) {
        return fromSegments(Arrays.asList(segments));
    }

    /** Build a new line segment path from the given segments.
     * @param segments the segment to comprise the path
     * @return new line segment path containing the given line segment in order
     * @throws IllegalStateException if the segments to not form a connected path
     */
    public static LineSegmentPath fromSegments(final Collection<LineSegment> segments) {
        LineSegmentPathBuilder builder = builder();

        for (LineSegment segment : segments) {
            builder.append(segment);
        }

        return builder.build();
    }

    /** Build a new line segment path from the given vertices.
     * @param vertices the vertices to construct the path from
     * @param precision precision context used to construct the line segment
     *      instances for the path
     * @return new line segment path constructed from the given vertices
     */
    public static LineSegmentPath fromVertices(final Collection<Vector2D> vertices,
            final DoublePrecisionContext precision) {

        return builder(precision).appendVertices(vertices).build();
    }

    /** Class used to build line segment paths.
     */
    public static class LineSegmentPathBuilder implements Serializable {

        /** Serializable UID */
        private static final long serialVersionUID = 20190522L;

        /** Line segments appended to the path. */
        private List<LineSegment> appendedSegments = null;

        /** Line segments prepended to the path. */
        private List<LineSegment> prependedSegments = null;

        /** Precision context used when creating line segments directly from vertices. */
        private DoublePrecisionContext precision;

        /** The current vertex at the start of the path. */
        private Vector2D startVertex;

        /** The current vertex at the end of the path. */
        private Vector2D endVertex;

        /** The precision context used when performing comparisons involving the current
         * end vertex.
         */
        private DoublePrecisionContext endVertexPrecision;

        /** Default constructor. No vertex precision is configured when this constructor
         * is used.
         */
        public LineSegmentPathBuilder() {
            this(null);
        }

        /** Construct a new instance configured to use the given precision context when
         * constructing line segments from raw vertices.
         * @param vertexPrecision precision context to use when creating line segments
         *      from vertices
         */
        public LineSegmentPathBuilder(final DoublePrecisionContext vertexPrecision) {
            setPrecision(vertexPrecision);
        }

        /** Set the precision context. This context is used only when creating line segments
         * from appended or prepended vertices. It is not used when adding existing
         * {@link LineSegment} instances since those contain their own precision contexts.
         * @param precision precision context to use when creating line segments
         *      from vertices
         * @return this instance
         */
        public LineSegmentPathBuilder setPrecision(final DoublePrecisionContext precision) {
            this.precision = precision;

            return this;
        }

        /** Append a line segment to the end of the path.
         * @return the current builder instance
         * @throws IllegalStateException if the path contains a previous segment
         *      and the end vertex of the previous segment is not equivalent to the
         *      start vertex of the given segment.
         */
        public LineSegmentPathBuilder append(final LineSegment segment) {
            validateSegmentsConnected(getEnd(), segment);
            appendInternal(segment);

            return this;
        }

        /** Add a vertex to the end of this path. If the path already has an end vertex,
         * then a line segment is added between the previous end vertex and this vertex,
         * using the configured precision context.
         * @param vertex the vertex to add
         * @return this instance
         * @see #setPrecision(DoublePrecisionContext)
         */
        public LineSegmentPathBuilder append(final Vector2D vertex) {
            final DoublePrecisionContext vertexPrecision = getAddVertexPrecision();

            if (endVertex == null) {
                // make sure that we're not adding to an infinite segment
                final LineSegment end = getEnd();
                if (end != null) {
                    throw new IllegalStateException("Cannot add vertex " + vertex + " after infinite line segment: " + end);
                }

                // this is the first vertex added
                startVertex = vertex;
                endVertex = vertex;
                endVertexPrecision = vertexPrecision;
            }
            else if (!endVertex.eq(vertex, endVertexPrecision)) {
                // only add the vertex if its not equal to the end point
                // of the last segment
                appendInternal(LineSegment.fromPoints(endVertex, vertex, endVertexPrecision));
            }

            return this;
        }

        /** Convenience method for appending a collection of vertices to the path in a single method call.
         * @param vertices the vertices to append
         * @return this instance
         * @see #append(Vector2D)
         */
        public LineSegmentPathBuilder appendVertices(final Collection<Vector2D> vertices) {
            for (Vector2D vertex : vertices) {
                append(vertex);
            }

            return this;
        }

        /** Convenience method for appending multiple vertices to the path at once.
         * @param vertices the vertices to append
         * @return this instance
         * @see #append(Vector2D)
         */
        public LineSegmentPathBuilder appendVertices(final Vector2D ... vertices) {
            return appendVertices(Arrays.asList(vertices));
        }

        /** Prepend a line segment to the beginning of the path.
         * @return the current builder instance
         * @throws IllegalStateException if the path contains a start segment
         *      and the end vertex of the given segment is not equivalent to the
         *      start vertex of the start segment.
         */
        public LineSegmentPathBuilder prepend(final LineSegment segment) {
            validateSegmentsConnected(segment, getStart());
            prependInternal(segment);

            return this;
        }

        /** Add a vertex to the front of this path. If the path already has a start vertex,
         * then a line segment is added between this vertex and the previous start vertex,
         * using the configured precision context.
         * @param vertex the vertex to add
         * @return this instance
         * @see #setPrecision(DoublePrecisionContext)
         */
        public LineSegmentPathBuilder prepend(final Vector2D vertex) {
            final DoublePrecisionContext vertexPrecision = getAddVertexPrecision();

            if (startVertex == null) {
                // make sure that we're not adding to an infinite segment
                final LineSegment start = getStart();
                if (start != null) {
                    throw new IllegalStateException("Cannot add vertex " + vertex + " before infinite line segment: " + start);
                }

                // this is the first vertex added
                startVertex = vertex;
                endVertex = vertex;
                endVertexPrecision = vertexPrecision;
            }
            else if (!vertex.eq(startVertex, vertexPrecision)) {
                // only add if the vertex is not equal to the start
                // point of the first segment
                prependInternal(LineSegment.fromPoints(vertex, startVertex, vertexPrecision));
            }

            return this;
        }

        /** Convenience method for prepending a collection of vertices to the path in a single method call.
         * The vertices are logically prepended as a single group, meaning that the first vertex
         * in the given collection appears as the first vertex in the path after this method call.
         * Internally, this means that the vertices are actually passed to the {@link #prepend(Vector2D)}
         * method in reverse order.
         * @param vertices the vertices to prepend
         * @return this instance
         * @see #prepend(Vector2D)
         */
        public LineSegmentPathBuilder prependVertices(final Collection<Vector2D> vertices) {
            return prependVertices(vertices.toArray(new Vector2D[0]));
        }

        /** Convenience method for prepending multiple vertices to the path in a single method call.
         * The vertices are logically prepended as a single group, meaning that the first vertex
         * in the given collection appears as the first vertex in the path after this method call.
         * Internally, this means that the vertices are actually passed to the {@link #prepend(Vector2D)}
         * method in reverse order.
         * @param vertices the vertices to prepend
         * @return this instance
         * @see #prepend(Vector2D)
         */
        public LineSegmentPathBuilder prependVertices(final Vector2D ... vertices) {
            for (int i=vertices.length - 1; i >=0 ; --i) {
                prepend(vertices[i]);
            }

            return this;
        }

        /** Close the current path and build a new {@link LineSegmentPath} instance.
         * @return line segment path
         */
        public LineSegmentPath close() {
            final LineSegment end = getEnd();

            if (end != null) {
                if (startVertex != null && endVertex != null) {
                    if (!endVertex.eq(startVertex, endVertexPrecision)) {
                        appendInternal(LineSegment.fromPoints(endVertex, startVertex, endVertexPrecision));
                    }
                }
                else {
                    throw new IllegalStateException("Unable to close line segment path: path is infinite");
                }
            }

            return build();
        }

        /** Build a {@link LineSegmentPath} instance from the configured path.
         * @return line segment path
         */
        public LineSegmentPath build() {
            // combine all of the segments
            List<LineSegment> result = null;

            if (prependedSegments != null) {
                result = prependedSegments;
                Collections.reverse(result);
            }

            if (appendedSegments != null) {
                if (result == null) {
                    result = appendedSegments;
                }
                else {
                    result.addAll(appendedSegments);
                }
            }

            if (result == null) {
                result = Collections.emptyList();
            }

            if (result.isEmpty() && startVertex != null) {
                throw new IllegalStateException("Unable to create line segment path; only a single vertex provided: " + startVertex);
            }

            // clear internal state
            appendedSegments = null;
            prependedSegments = null;

            // build the final path instance
            return new LineSegmentPath(result);
        }

        /** Validate that the given segments are connected, meaning that the end vertex of {@code previous}
         * is equivalent to the start vertex of {@code next}. The segments are considered valid if either
         * segment is null.
         * @throws IllegalStateException if previous and next are not null and the end vertex of previous
         *      is not equivalent the start vertex of next
         */
        private void validateSegmentsConnected(final LineSegment previous, final LineSegment next) {
            if (previous != null && next != null) {
                final Vector2D nextStartVertex = next.getStart();
                final Vector2D previousEndVertex = previous.getEnd();
                final DoublePrecisionContext precision = previous.getPrecision();

                if (nextStartVertex == null || previousEndVertex == null ||
                        !(nextStartVertex.eq(previousEndVertex, precision))) {

                    throw new IllegalStateException("Path line segments are not connected: " +
                        "previous= " + previous + ", next= " + next);
                }
            }
        }

        /** Get the precision context used when adding raw vertices to the path. An exception is thrown
         * if no precision has been specified.
         * @return the precision context used when creating working with raw vertices
         * @throws IllegalStateException if no precision context is configured
         */
        private DoublePrecisionContext getAddVertexPrecision() {
            if (precision == null) {
                throw new IllegalStateException("Unable to create line segment: no vertex precision specified");
            }

            return precision;
        }

        /** Append the given, validated segment to the path.
         * @param segment validated segment to append
         */
        private void appendInternal(final LineSegment segment) {
            if (appendedSegments == null) {
                appendedSegments = new ArrayList<>();
            }

            if (appendedSegments.isEmpty() &&
                    (prependedSegments == null || prependedSegments.isEmpty())) {
                startVertex = segment.getStart();
            }

            endVertex = segment.getEnd();
            endVertexPrecision = segment.getPrecision();

            appendedSegments.add(segment);
        }

        /** Prepend the given, validated segment to the path.
         * @param segment validated segment to prepend
         */
        private void prependInternal(final LineSegment segment) {
            if (prependedSegments == null) {
                prependedSegments = new ArrayList<>();
            }

            startVertex = segment.getStart();

            if (prependedSegments.isEmpty() &&
                    (appendedSegments == null || appendedSegments.isEmpty())) {
                endVertex = segment.getEnd();
                endVertexPrecision = segment.getPrecision();
            }

            prependedSegments.add(segment);
        }

        /** Get the line segment at the start of the path or null if
         * it does not exist.
         * @return the line segment at the start of the path
         */
        private LineSegment getStart() {
            LineSegment start = getLast(prependedSegments);
            if (start == null) {
                start = getFirst(appendedSegments);
            }
            return start;
        }

        /** Get the line segment at the end of the path or null if
         * it does not exist.
         * @return the line segment at the end of the path
         */
        private LineSegment getEnd() {
            LineSegment end = getLast(appendedSegments);
            if (end == null) {
                end = getFirst(prependedSegments);
            }
            return end;
        }

        /** Get the first element in the list or null if the list is null
         * or empty.
         * @param list the list to return the first item from
         * @return the first item from the given list or null if it does not exist
         */
        private LineSegment getFirst(final List<LineSegment> list) {
            if (list != null && list.size() > 0) {
                return list.get(0);
            }
            return null;
        }

        /** Get the last element in the list or null if the list is null
         * or empty.
         * @param list the list to return the last item from
         * @return the last item from the given list or null if it does not exist
         */
        private LineSegment getLast(final List<LineSegment> list) {
            if (list != null && list.size() > 0) {
                return list.get(list.size() - 1);
            }
            return null;
        }
    }
}
