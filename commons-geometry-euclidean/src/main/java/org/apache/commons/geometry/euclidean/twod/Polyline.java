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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** Class representing a polyline, ie, a connected series of line segments. The line
 * segments in the polyline are connected end to end, with the end vertex of the previous
 * line segment equivalent to the start vertex of the next line segment. The first segment,
 * the last segment, or both may be infinite.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 * @see <a href="https://en.wikipedia.org/wiki/Polygonal_chain">Polygonal chain</a>
 */
public class Polyline implements Iterable<Segment>, Serializable {

    /** Serializable UID. */
    private static final long serialVersionUID = 20190522L;

    /** Polyline instance containing no segments. */
    private static final Polyline EMPTY = new Polyline(Collections.emptyList());

    /** List of line segments comprising the instance. */
    private final List<Segment> segments;

    /** Simple constructor. No validation is performed on the input segments.
     * @param segments line segments comprising the instance
     */
    private Polyline(final List<Segment> segments) {
        this.segments = Collections.unmodifiableList(segments);
    }

    /** Get the line segments comprising the polyline.
     * @return the line segments comprising the polyline
     */
    public List<Segment> getSegments() {
        return segments;
    }

    /** Get the start segment for the polyline or null if the polyline is empty.
     * @return the start segment for the polyline or null if the polyline is empty
     */
    public Segment getStartSegment() {
        if (!isEmpty()) {
            return segments.get(0);
        }
        return null;
    }

    /** Get the end segment for the polyline or null if the polyline is empty.
     * @return the end segment for the polyline or null if the polyline is empty
     */
    public Segment getEndSegment() {
        if (!isEmpty()) {
            return segments.get(segments.size() - 1);
        }
        return null;
    }

    /** Get the start vertex for the polyline or null if the polyline is empty
     * or has an infinite start segment.
     * @return the start vertex for the polyline
     */
    public Vector2D getStartVertex() {
        final Segment seg = getStartSegment();
        return (seg != null) ? seg.getStartPoint() : null;
    }

    /** Get the end vertex for the polyline or null if the polyline is empty
     * or has an infinite end segment.
     * @return the end vertex for the polyline
     */
    public Vector2D getEndVertex() {
        final Segment seg = getEndSegment();
        return (seg != null) ? seg.getEndPoint() : null;
    }

    /** Get the vertices contained in the polyline in the order they appear.
     * Closed polyline contain the start point at the beginning of the list
     * as well as the end.
     * @return the vertices contained in the polyline in order they appear
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
        for (Segment seg : segments) {
            pt = seg.getEndPoint();
            if (pt != null) {
                vertices.add(pt);
            }
        }

        return vertices;
    }

    /** Return true if the polyline has a start of end line segment that
     * extends to infinity.
     * @return true if the polyline is infinite
     */
    public boolean isInfinite() {
        return !isEmpty() && (getStartVertex() == null || getEndVertex() == null);
    }

    /** Return true if the polyline has a finite length. This will be true if there are
     * no segments in the polyline or if all segments have a finite length.
     * @return true if the polyline is finite
     */
    public boolean isFinite() {
        return !isInfinite();
    }

    /** Return true if the polyline does not contain any line segments.
     * @return true if the polyline does not contain any line segments
     */
    public boolean isEmpty() {
        return segments.isEmpty();
    }

    /** Return true if the polyline is closed, meaning that the end
     * point for the last line segment is equal to the start point
     * for the polyline.
     * @return true if the end point for the last line segment is
     *      equal to the start point for the polyline.
     */
    public boolean isClosed() {
        final Segment endSegment = getEndSegment();

        if (endSegment != null) {
            final Vector2D start = getStartVertex();
            final Vector2D end = endSegment.getEndPoint();

            return start != null && end != null && start.eq(end, endSegment.getPrecision());
        }

        return false;
    }

    /** Transform this instance with the argument, returning the result in a new instance.
     * @param transform the transform to apply
     * @return a new instance, transformed by the argument
     */
    public Polyline transform(final Transform<Vector2D> transform) {
        if (!isEmpty()) {
            final List<Segment> transformed = segments.stream()
                    .map(s -> s.transform(transform))
                    .collect(Collectors.toCollection(() -> new ArrayList<>()));

            return new Polyline(transformed);
        }

        return this;
    }

    /** Return a new instance with all line segment directions, and their order,
     * reversed. The last segment in this instance will be the first in the
     * returned instance.
     * @return a new instance with the polyline reversed
     */
    public Polyline reverse() {
        if (!isEmpty()) {
            final List<Segment> reversed = segments.stream()
                    .map(s -> s.reverse())
                    .collect(Collectors.toCollection(() -> new ArrayList<>()));
            Collections.reverse(reversed);

            return new Polyline(reversed);
        }

        return this;
    }

    /** Construct a {@link RegionBSPTree2D} from the line segments in this instance.
     * @return a bsp tree constructed from the line segments in this instance
     */
    public RegionBSPTree2D toTree() {
        RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.insert(this);

        return tree;
    }

    /** Simplify this polyline, if possible, by combining adjacent segments that lie on the
     * same line (as determined by {@link Line#equals(Object)}).
     * @return a simplified instance
     */
    public Polyline simplify() {
        final List<Segment> simplified = new ArrayList<>();

        final int size = segments.size();

        Segment current;
        Line currentLine;
        double end;

        int idx = 0;
        int testIdx;
        while (idx < size) {
            current = segments.get(idx);
            currentLine = current.getLine();
            end = current.getSubspaceEnd();

            // try to combine with forward neighbors
            testIdx = idx + 1;
            while (testIdx < size && currentLine.equals(segments.get(testIdx).getLine())) {
                end = Math.max(end, segments.get(testIdx).getSubspaceEnd());
                ++testIdx;
            }

            if (testIdx > idx + 1) {
                // we found something to merge
                simplified.add(currentLine.segment(current.getSubspaceStart(), end));
            } else {
                simplified.add(current);
            }

            idx = testIdx;
        }

        // combine the first and last items if needed
        if (isClosed() && simplified.size() > 2 && simplified.get(0).getLine().equals(
                simplified.get(simplified.size() - 1).getLine())) {

            final Segment startSegment = simplified.get(0);
            final Segment endSegment = simplified.remove(simplified.size() - 1);

            final Segment combined = endSegment.getLine().segment(endSegment.getSubspaceStart(),
                    startSegment.getSubspaceEnd());

            simplified.set(0, combined);
        }

        return new SimplifiedPolyline(simplified);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<Segment> iterator() {
        return segments.iterator();
    }

    /** Return a string representation of the segment polyline.
     *
     * <p>In order to keep the string representation short but useful, the exact format of the return
     * value depends on the properties of the polyline. See below for examples.
     *
     * <ul>
     *      <li>Empty path
     *          <ul>
     *              <li>{@code Polyline[empty= true]}</li>
     *          </ul>
     *      </li>
     *      <li>Single segment
     *          <ul>
     *              <li>{@code Polyline[segment= Segment[lineOrigin= (0.0, 0.0), lineDirection= (1.0, 0.0)]]}</li>
     *              <li>{@code Polyline[segment= Segment[start= (0.0, 0.0), end= (1.0, 0.0)]]}</li>
     *          </ul>
     *      </li>
     *      <li>Path with infinite start segment
     *          <ul>
     *              <li>{@code Polyline[startDirection= (1.0, 0.0), vertices= [(1.0, 0.0), (1.0, 1.0)]]}</li>
     *          </ul>
     *      </li>
     *      <li>Path with infinite end segment
     *          <ul>
     *              <li>{@code Polyline[vertices= [(0.0, 1.0), (0.0, 0.0)], endDirection= (1.0, 0.0)]}</li>
     *          </ul>
     *      </li>
     *      <li>Path with infinite start and end segments
     *          <ul>
     *              <li>{@code Polyline[startDirection= (0.0, 1.0), vertices= [(0.0, 0.0)], endDirection= (1.0, 0.0)]}</li>
     *          </ul>
     *      </li>
     *      <li>Path with no infinite segments
     *          <ul>
     *              <li>{@code Polyline[vertices= [(0.0, 0.0), (1.0, 0.0), (1.0, 1.0)]]}</li>
     *          </ul>
     *      </li>
     * </ul>
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName())
            .append('[');

        if (segments.isEmpty()) {
            sb.append("empty= true");
        } else if (segments.size() == 1) {
            sb.append("segment= ")
                .append(segments.get(0));
        } else {
            final Segment startSegment = getStartSegment();
            if (startSegment.getStartPoint() == null) {
                sb.append("startDirection= ")
                    .append(startSegment.getLine().getDirection())
                    .append(", ");
            }

            sb.append("vertices= ")
                .append(getVertices());

            final Segment endSegment = getEndSegment();
            if (endSegment.getEndPoint() == null) {
                sb.append(", endDirection= ")
                    .append(endSegment.getLine().getDirection());
            }
        }

        sb.append(']');

        return sb.toString();
    }

    /** Return a {@link Builder} instance configured with the given precision
     * context. The precision context is used when building line segments from
     * vertices and may be omitted if raw vertices are not used.
     * @param precision precision context to use when building line segments from
     *      raw vertices; may be null if raw vertices are not used.
     * @return a new {@link Builder} instance
     */
    public static Builder builder(final DoublePrecisionContext precision) {
        return new Builder(precision);
    }

    /** Build a new polyline from the given segments.
     * @param segments the segment to comprise the polyline
     * @return new polyline containing the given line segment in order
     * @throws IllegalStateException if the segments do not form a connected polyline
     */
    public static Polyline fromSegments(final Segment... segments) {
        return fromSegments(Arrays.asList(segments));
    }

    /** Build a new polyline from the given segments.
     * @param segments the segment to comprise the path
     * @return new polyline containing the given line segments in order
     * @throws IllegalStateException if the segments do not form a connected polyline
     */
    public static Polyline fromSegments(final Collection<Segment> segments) {
        Builder builder = builder(null);

        for (Segment segment : segments) {
            builder.append(segment);
        }

        return builder.build();
    }

    /** Build a new polyline from the given vertices. A line segment is created
     * from the last vertex to the first one, if the two vertices are not already
     * considered equal using the given precision context. This method is equivalent to
     * calling {@link #fromVertices(Collection, boolean, DoublePrecisionContext)
     * fromVertices(vertices, true, precision)}
     * @param vertices the vertices to construct the closed path from
     * @param precision precision context used to construct the line segment
     *      instances for the path
     * @return new closed polyline constructed from the given vertices
     * @see #fromVertices(Collection, boolean, DoublePrecisionContext)
     */
    public static Polyline fromVertexLoop(final Collection<Vector2D> vertices,
            final DoublePrecisionContext precision) {

        return fromVertices(vertices, true, precision);
    }

    /** Build a new polyline from the given vertices. No additional segment is added
     * from the last vertex to the first. This method is equivalent to calling
     * {@link #fromVertices(Collection, boolean, DoublePrecisionContext)
     * fromVertices(vertices, false, precision)}.
     * @param vertices the vertices to construct the path from
     * @param precision precision context used to construct the line segment
     *      instances for the path
     * @return new polyline constructed from the given vertices
     * @see #fromVertices(Collection, boolean, DoublePrecisionContext)
     */
    public static Polyline fromVertices(final Collection<Vector2D> vertices,
            final DoublePrecisionContext precision) {

        return fromVertices(vertices, false, precision);
    }

    /** Build a new polyline from the given vertices.
     * @param vertices the vertices to construct the path from
     * @param close if true, a line segment is created from the last vertex
     *      given to the first one, if the two vertices are not already considered
     *      equal using the given precision context.
     * @param precision precision context used to construct the line segment
     *      instances for the path
     * @return new polyline constructed from the given vertices
     */
    public static Polyline fromVertices(final Collection<Vector2D> vertices,
            final boolean close, final DoublePrecisionContext precision) {

        final Builder builder = builder(precision).appendVertices(vertices);

        return close ?
                builder.close() :
                builder.build();
    }

    /** Return a line segment path containing no segments.
     * @return a line segment path containing no segments.
     */
    public static Polyline empty() {
        return EMPTY;
    }

    /** Class used to build polylines.
     */
    public static final class Builder implements Serializable {

        /** Serializable UID. */
        private static final long serialVersionUID = 20190522L;

        /** Line segments appended to the polyline. */
        private List<Segment> appendedSegments = null;

        /** Line segments prepended to the polyline. */
        private List<Segment> prependedSegments = null;

        /** Precision context used when creating line segments directly from vertices. */
        private DoublePrecisionContext precision;

        /** The current vertex at the start of the polyline. */
        private Vector2D startVertex;

        /** The current vertex at the end of the polyline. */
        private Vector2D endVertex;

        /** The precision context used when performing comparisons involving the current
         * end vertex.
         */
        private DoublePrecisionContext endVertexPrecision;

        /** Construct a new instance configured with the given precision context. The
         * precision context is used when building line segments from vertices and
         * may be omitted if raw vertices are not used.
         * @param precision precision context to use when creating line segments
         *      from vertices
         */
        private Builder(final DoublePrecisionContext precision) {
            setPrecision(precision);
        }

        /** Set the precision context. This context is used only when creating line segments
         * from appended or prepended vertices. It is not used when adding existing
         * {@link Segment} instances since those contain their own precision contexts.
         * @param builderPrecision precision context to use when creating line segments
         *      from vertices
         * @return this instance
         */
        public Builder setPrecision(final DoublePrecisionContext builderPrecision) {
            this.precision = builderPrecision;

            return this;
        }

        /** Get the line segment at the start of the polyline or null if
         * it does not exist.
         * @return the line segment at the start of the polyline
         */
        public Segment getStartSegment() {
            Segment start = getLast(prependedSegments);
            if (start == null) {
                start = getFirst(appendedSegments);
            }
            return start;
        }

        /** Get the line segment at the end of the polyline or null if
         * it does not exist.
         * @return the line segment at the end of the polyline
         */
        public Segment getEndSegment() {
            Segment end = getLast(appendedSegments);
            if (end == null) {
                end = getFirst(prependedSegments);
            }
            return end;
        }

        /** Append a line segment to the end of the polyline.
         * @param segment line segment to append to the polyline
         * @return the current builder instance
         * @throws IllegalStateException if the polyline contains a previous segment
         *      and the end vertex of the previous segment is not equivalent to the
         *      start vertex of the given segment.
         */
        public Builder append(final Segment segment) {
            validateSegmentsConnected(getEndSegment(), segment);
            appendInternal(segment);

            return this;
        }

        /** Add a vertex to the end of this polyline. If the polyline already has an end vertex,
         * then a line segment is added between the previous end vertex and this vertex,
         * using the configured precision context.
         * @param vertex the vertex to add
         * @return this instance
         * @see #setPrecision(DoublePrecisionContext)
         */
        public Builder append(final Vector2D vertex) {
            final DoublePrecisionContext vertexPrecision = getAddVertexPrecision();

            if (endVertex == null) {
                // make sure that we're not adding to an infinite segment
                final Segment end = getEndSegment();
                if (end != null) {
                    throw new IllegalStateException(
                            MessageFormat.format("Cannot add vertex {0} after infinite line segment: {1}",
                                    vertex, end));
                }

                // this is the first vertex added
                startVertex = vertex;
                endVertex = vertex;
                endVertexPrecision = vertexPrecision;
            } else if (!endVertex.eq(vertex, endVertexPrecision)) {
                // only add the vertex if its not equal to the end point
                // of the last segment
                appendInternal(Segment.fromPoints(endVertex, vertex, endVertexPrecision));
            }

            return this;
        }

        /** Convenience method for appending a collection of vertices to the polyline in a single method call.
         * @param vertices the vertices to append
         * @return this instance
         * @see #append(Vector2D)
         */
        public Builder appendVertices(final Collection<Vector2D> vertices) {
            for (Vector2D vertex : vertices) {
                append(vertex);
            }

            return this;
        }

        /** Convenience method for appending multiple vertices to the polyline at once.
         * @param vertices the vertices to append
         * @return this instance
         * @see #append(Vector2D)
         */
        public Builder appendVertices(final Vector2D... vertices) {
            return appendVertices(Arrays.asList(vertices));
        }

        /** Prepend a line segment to the beginning of the polyline.
         * @param segment line segment to prepend to the polyline
         * @return the current builder instance
         * @throws IllegalStateException if the polyline contains a start segment
         *      and the end vertex of the given segment is not equivalent to the
         *      start vertex of the start segment.
         */
        public Builder prepend(final Segment segment) {
            validateSegmentsConnected(segment, getStartSegment());
            prependInternal(segment);

            return this;
        }

        /** Add a vertex to the front of this polyline. If the polyline already has a start vertex,
         * then a line segment is added between this vertex and the previous start vertex,
         * using the configured precision context.
         * @param vertex the vertex to add
         * @return this instance
         * @see #setPrecision(DoublePrecisionContext)
         */
        public Builder prepend(final Vector2D vertex) {
            final DoublePrecisionContext vertexPrecision = getAddVertexPrecision();

            if (startVertex == null) {
                // make sure that we're not adding to an infinite segment
                final Segment start = getStartSegment();
                if (start != null) {
                    throw new IllegalStateException(
                            MessageFormat.format("Cannot add vertex {0} before infinite line segment: {1}",
                                    vertex, start));
                }

                // this is the first vertex added
                startVertex = vertex;
                endVertex = vertex;
                endVertexPrecision = vertexPrecision;
            } else if (!vertex.eq(startVertex, vertexPrecision)) {
                // only add if the vertex is not equal to the start
                // point of the first segment
                prependInternal(Segment.fromPoints(vertex, startVertex, vertexPrecision));
            }

            return this;
        }

        /** Convenience method for prepending a collection of vertices to the polyline in a single method call.
         * The vertices are logically prepended as a single group, meaning that the first vertex
         * in the given collection appears as the first vertex in the polyline after this method call.
         * Internally, this means that the vertices are actually passed to the {@link #prepend(Vector2D)}
         * method in reverse order.
         * @param vertices the vertices to prepend
         * @return this instance
         * @see #prepend(Vector2D)
         */
        public Builder prependVertices(final Collection<Vector2D> vertices) {
            return prependVertices(vertices.toArray(new Vector2D[0]));
        }

        /** Convenience method for prepending multiple vertices to the polyline in a single method call.
         * The vertices are logically prepended as a single group, meaning that the first vertex
         * in the given collection appears as the first vertex in the polyline after this method call.
         * Internally, this means that the vertices are actually passed to the {@link #prepend(Vector2D)}
         * method in reverse order.
         * @param vertices the vertices to prepend
         * @return this instance
         * @see #prepend(Vector2D)
         */
        public Builder prependVertices(final Vector2D... vertices) {
            for (int i = vertices.length - 1; i >= 0; --i) {
                prepend(vertices[i]);
            }

            return this;
        }

        /** Close the current polyline and build a new {@link Polyline} instance.
         * @return new closed polyline instance
         */
        public Polyline close() {
            final Segment end = getEndSegment();

            if (end != null) {
                if (startVertex != null && endVertex != null) {
                    if (!endVertex.eq(startVertex, endVertexPrecision)) {
                        appendInternal(Segment.fromPoints(endVertex, startVertex, endVertexPrecision));
                    }
                } else {
                    throw new IllegalStateException("Unable to close polyline: polyline is infinite");
                }
            }

            return build();
        }

        /** Build a {@link Polyline} instance from the configured polyline.
         * @return new polyline instance
         */
        public Polyline build() {
            // combine all of the segments
            List<Segment> result = null;

            if (prependedSegments != null) {
                result = prependedSegments;
                Collections.reverse(result);
            }

            if (appendedSegments != null) {
                if (result == null) {
                    result = appendedSegments;
                } else {
                    result.addAll(appendedSegments);
                }
            }

            if (result == null) {
                result = Collections.emptyList();
            }

            if (result.isEmpty() && startVertex != null) {
                throw new IllegalStateException(
                        MessageFormat.format("Unable to create polyline; only a single vertex provided: {0} ",
                                startVertex));
            }

            // clear internal state
            appendedSegments = null;
            prependedSegments = null;

            // build the final polyline instance, using the shared empty instance if
            // no segments are present
            return result.isEmpty() ? empty() : new Polyline(result);
        }

        /** Validate that the given segments are connected, meaning that the end vertex of {@code previous}
         * is equivalent to the start vertex of {@code next}. The segments are considered valid if either
         * segment is null.
         * @param previous previous segment
         * @param next next segment
         * @throws IllegalStateException if previous and next are not null and the end vertex of previous
         *      is not equivalent the start vertex of next
         */
        private void validateSegmentsConnected(final Segment previous, final Segment next) {
            if (previous != null && next != null) {
                final Vector2D nextStartVertex = next.getStartPoint();
                final Vector2D previousEndVertex = previous.getEndPoint();
                final DoublePrecisionContext previousPrecision = previous.getPrecision();

                if (nextStartVertex == null || previousEndVertex == null ||
                        !(nextStartVertex.eq(previousEndVertex, previousPrecision))) {

                    throw new IllegalStateException(
                            MessageFormat.format("Polyline segments are not connected: previous= {0}, next= {1}",
                                    previous, next));
                }
            }
        }

        /** Get the precision context used when adding raw vertices to the polyline. An exception is thrown
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

        /** Append the given, validated segment to the polyline.
         * @param segment validated segment to append
         */
        private void appendInternal(final Segment segment) {
            if (appendedSegments == null) {
                appendedSegments = new ArrayList<>();
            }

            if (appendedSegments.isEmpty() &&
                    (prependedSegments == null || prependedSegments.isEmpty())) {
                startVertex = segment.getStartPoint();
            }

            endVertex = segment.getEndPoint();
            endVertexPrecision = segment.getPrecision();

            appendedSegments.add(segment);
        }

        /** Prepend the given, validated segment to the polyline.
         * @param segment validated segment to prepend
         */
        private void prependInternal(final Segment segment) {
            if (prependedSegments == null) {
                prependedSegments = new ArrayList<>();
            }

            startVertex = segment.getStartPoint();

            if (prependedSegments.isEmpty() &&
                    (appendedSegments == null || appendedSegments.isEmpty())) {
                endVertex = segment.getEndPoint();
                endVertexPrecision = segment.getPrecision();
            }

            prependedSegments.add(segment);
        }

        /** Get the first element in the list or null if the list is null
         * or empty.
         * @param list the list to return the first item from
         * @return the first item from the given list or null if it does not exist
         */
        private Segment getFirst(final List<Segment> list) {
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
        private Segment getLast(final List<Segment> list) {
            if (list != null && list.size() > 0) {
                return list.get(list.size() - 1);
            }
            return null;
        }
    }

    /** Internal class returned when a polyline is simplified to remove
     * unecessary line segments divisions. The {@link #simplify()} method on this
     * class simply returns the same instance.
     */
    private static final class SimplifiedPolyline extends Polyline {

        /** Serializable UID. */
        private static final long serialVersionUID = 20190619;

        /** Create a new instance containing the given line segments. No validation is
         * performed on the inputs. Caller must ensure that the given segments represent
         * a valid, simplified path.
         * @param segments line segments comprising the path
         */
        private SimplifiedPolyline(final List<Segment> segments) {
            super(segments);
        }

        /** {@inheritDoc} */
        @Override
        public Polyline simplify() {
            // already simplified
            return this;
        }
    }
}
