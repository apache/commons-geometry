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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** Class representing a polyline, ie, a connected series of line segments. The line
 * segments in the polyline are connected end to end, with the end vertex of the previous
 * line segment equivalent to the start vertex of the next line segment.
 *
 * <p>In order to make this class more applicable for use with infinite regions, the contained path
 * elements are not required to be finite line segments. Instead, they are only required to be convex
 * sublines. This means that the start element, end element or both, may be infinite, e.g. one of
 * {@link Line.Span}, {@link Ray}, or {@link TerminatedLine}.</p>
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 * @see <a href="https://en.wikipedia.org/wiki/Polygonal_chain">Polygonal chain</a>
 */
public class Polyline implements BoundarySource2D, Linecastable2D {
    /** Polyline instance containing no sublines. */
    private static final Polyline EMPTY = new Polyline(Collections.emptyList());

    /** List of sublines comprising the instance. */
    private final List<ConvexSubLine> sublines;

    /** Simple constructor. No validation is performed on the input.
     * @param sublines sublines comprising the instance
     */
    private Polyline(final List<ConvexSubLine> sublines) {
        this.sublines = Collections.unmodifiableList(sublines);
    }

    /** {@inheritDoc} */
    @Override
    public Stream<ConvexSubLine> boundaryStream() {
        return getSubLines().stream();
    }

    /** Get the sublines comprising the polyline.
     * @return the sublines comprising the polyline
     */
    public List<ConvexSubLine> getSubLines() {
        return sublines;
    }

    /** Get the start subline for the polyline or null if the polyline is empty.
     * @return the start subline for the polyline or null if the polyline is empty
     */
    public ConvexSubLine getStartSubLine() {
        if (!isEmpty()) {
            return sublines.get(0);
        }
        return null;
    }

    /** Get the end subline for the polyline or null if the polyline is empty.
     * @return the end subline for the polyline or null if the polyline is empty
     */
    public ConvexSubLine getEndSubLine() {
        if (!isEmpty()) {
            return sublines.get(sublines.size() - 1);
        }
        return null;
    }

    /** Get the start vertex for the polyline or null if the polyline is empty
     * or has an infinite start subline.
     * @return the start vertex for the polyline
     */
    public Vector2D getStartVertex() {
        final ConvexSubLine seg = getStartSubLine();
        return (seg != null) ? seg.getStartPoint() : null;
    }

    /** Get the end vertex for the polyline or null if the polyline is empty
     * or has an infinite end subline.
     * @return the end vertex for the polyline
     */
    public Vector2D getEndVertex() {
        final ConvexSubLine seg = getEndSubLine();
        return (seg != null) ? seg.getEndPoint() : null;
    }

    /** Get the sequence of vertices defined by the polyline. Vertices appear in the
     * list as many times as they are visited in the path. For example, the vertex
     * sequence for a closed polyline contains the start point at the beginning
     * of the list as well as the end.
     * @return the sequence of vertices defined by the polyline
     */
    public List<Vector2D> getVertexSequence() {
        final List<Vector2D> sequence = new ArrayList<>();

        Vector2D pt;

        // add the start point, if present
        pt = getStartVertex();
        if (pt != null) {
            sequence.add(pt);
        }

        // add end points
        for (final ConvexSubLine sub : sublines) {
            pt = sub.getEndPoint();
            if (pt != null) {
                sequence.add(pt);
            }
        }

        return sequence;
    }

    /** Return true if the polyline has a start or end subline that
     * extends to infinity.
     * @return true if the polyline is infinite
     */
    public boolean isInfinite() {
        return !isEmpty() && (getStartVertex() == null || getEndVertex() == null);
    }

    /** Return true if the polyline has a finite length. This will be true if there are
     * no sublines in the polyline or if all sublines have a finite length.
     * @return true if the polyline is finite
     */
    public boolean isFinite() {
        return !isInfinite();
    }

    /** Return true if the polyline does not contain any sublines.
     * @return true if the polyline does not contain any sublines
     */
    public boolean isEmpty() {
        return sublines.isEmpty();
    }

    /** Return true if the polyline is closed, meaning that the end point for the last
     * subline is equivalent to the start point of the first.
     * @return true if the end point for the last subline is equivalent to the
     *      start point for the first
     */
    public boolean isClosed() {
        final ConvexSubLine endSubLine = getEndSubLine();

        if (endSubLine != null) {
            final Vector2D start = getStartVertex();
            final Vector2D end = endSubLine.getEndPoint();

            return start != null && end != null && start.eq(end, endSubLine.getPrecision());
        }

        return false;
    }

    /** Transform this instance with the argument, returning the result in a new instance.
     * @param transform the transform to apply
     * @return a new instance, transformed by the argument
     */
    public Polyline transform(final Transform<Vector2D> transform) {
        if (!isEmpty()) {
            final List<ConvexSubLine> transformed = sublines.stream()
                .map(s -> s.transform(transform))
                .collect(Collectors.toCollection(ArrayList::new));

            return new Polyline(transformed);
        }

        return this;
    }

    /** Return a new instance with all subline directions, and their order,
     * reversed. The last subline in this instance will be the first in the
     * returned instance.
     * @return a new instance with the polyline reversed
     */
    public Polyline reverse() {
        if (!isEmpty()) {
            final List<ConvexSubLine> reversed = sublines.stream()
                .map(ConvexSubLine::reverse)
                .collect(Collectors.toCollection(ArrayList::new));
            Collections.reverse(reversed);

            return new Polyline(reversed);
        }

        return this;
    }

    /** Simplify this polyline, if possible, by combining adjacent sublines that lie on the
     * same line (as determined by {@link Line#equals(Object)}).
     * @return a simplified instance
     */
    public Polyline simplify() {
        final List<ConvexSubLine> simplified = new ArrayList<>();

        final int size = sublines.size();

        ConvexSubLine current;
        Line currentLine;
        double end;

        int idx = 0;
        int testIdx;
        while (idx < size) {
            current = sublines.get(idx);
            currentLine = current.getLine();
            end = current.getSubspaceEnd();

            // try to combine with forward neighbors
            testIdx = idx + 1;
            while (testIdx < size && currentLine.equals(sublines.get(testIdx).getLine())) {
                end = Math.max(end, sublines.get(testIdx).getSubspaceEnd());
                ++testIdx;
            }

            if (testIdx > idx + 1) {
                // we found something to merge
                simplified.add(ConvexSubLine.fromInterval(currentLine, current.getSubspaceStart(), end));
            } else {
                simplified.add(current);
            }

            idx = testIdx;
        }

        // combine the first and last items if needed
        if (isClosed() && simplified.size() > 2 && simplified.get(0).getLine().equals(
                simplified.get(simplified.size() - 1).getLine())) {

            final ConvexSubLine startSubLine = simplified.get(0);
            final ConvexSubLine endSubLine = simplified.remove(simplified.size() - 1);

            final ConvexSubLine combined = ConvexSubLine.fromInterval(
                    endSubLine.getLine(), endSubLine.getSubspaceStart(), startSubLine.getSubspaceEnd());

            simplified.set(0, combined);
        }

        return new SimplifiedPolyline(simplified);
    }

    /** {@inheritDoc} */
    @Override
    public List<LinecastPoint2D> linecast(final ConvexSubLine subline) {
        return new BoundarySourceLinecaster2D(this).linecast(subline);
    }

    /** {@inheritDoc} */
    @Override
    public LinecastPoint2D linecastFirst(final ConvexSubLine subline) {
        return new BoundarySourceLinecaster2D(this).linecastFirst(subline);
    }

    /** Return a string representation of the polyline.
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
     *      <li>Single subline
     *          <ul>
     *              <li>{@code Polyline[subLine= Line.Span[origin= (0.0, 0.0), direction= (1.0, 0.0)]]}</li>
     *              <li>{@code Polyline[subLine= Segment[startPoint= (0.0, 0.0), endPoint= (1.0, 0.0)]]}</li>
     *          </ul>
     *      </li>
     *      <li>Path with infinite start subline
     *          <ul>
     *              <li>{@code Polyline[startDirection= (1.0, 0.0), vertices= [(1.0, 0.0), (1.0, 1.0)]]}</li>
     *          </ul>
     *      </li>
     *      <li>Path with infinite end subline
     *          <ul>
     *              <li>{@code Polyline[vertices= [(0.0, 1.0), (0.0, 0.0)], endDirection= (1.0, 0.0)]}</li>
     *          </ul>
     *      </li>
     *      <li>Path with infinite start and end sublines
     *          <ul>
     *              <li>{@code Polyline[startDirection= (0.0, 1.0), vertices= [(0.0, 0.0)], endDirection= (1.0, 0.0)]}</li>
     *          </ul>
     *      </li>
     *      <li>Path with no infinite sublines
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

        if (sublines.isEmpty()) {
            sb.append("empty= true");
        } else if (sublines.size() == 1) {
            sb.append("subLine= ")
                .append(sublines.get(0));
        } else {
            final ConvexSubLine startSubLine = getStartSubLine();
            if (startSubLine.getStartPoint() == null) {
                sb.append("startDirection= ")
                    .append(startSubLine.getLine().getDirection())
                    .append(", ");
            }

            sb.append("vertices= ")
                .append(getVertexSequence());

            final ConvexSubLine endSubLine = getEndSubLine();
            if (endSubLine.getEndPoint() == null) {
                sb.append(", endDirection= ")
                    .append(endSubLine.getLine().getDirection());
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

    /** Build a new polyline from the given sublines.
     * @param sublines the sublines to comprise the polyline
     * @return new polyline containing the given sublines in order
     * @throws IllegalStateException if the sublines do not form a connected polyline
     */
    public static Polyline fromSubLines(final ConvexSubLine... sublines) {
        return fromSubLines(Arrays.asList(sublines));
    }

    /** Build a new polyline from the given sublines.
     * @param sublines the sublines to comprise the path
     * @return new polyline containing the given sublines in order
     * @throws IllegalStateException if the sublines do not form a connected polyline
     */
    public static Polyline fromSubLines(final Collection<ConvexSubLine> sublines) {
        final Builder builder = builder(null);

        for (final ConvexSubLine subline : sublines) {
            builder.append(subline);
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

        return builder(precision)
                .appendVertices(vertices)
                .build(close);
    }

    /** Return a polyline containing no sublines.
     * @return a polyline containing no sublines.
     */
    public static Polyline empty() {
        return EMPTY;
    }

    /** Class used to build polylines.
     */
    public static final class Builder {
        /** Sublines appended to the polyline. */
        private List<ConvexSubLine> appendedSublines = null;

        /** Sublines prepended to the polyline. */
        private List<ConvexSubLine> prependedSublines = null;

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
         * {@link ConvexSubLine} instances since those contain their own precision contexts.
         * @param builderPrecision precision context to use when creating line segments
         *      from vertices
         * @return this instance
         */
        public Builder setPrecision(final DoublePrecisionContext builderPrecision) {
            this.precision = builderPrecision;

            return this;
        }

        /** Get the subline at the start of the polyline or null if it does not exist.
         * @return the subline at the start of the polyline
         */
        public ConvexSubLine getStartSubLine() {
            ConvexSubLine start = getLast(prependedSublines);
            if (start == null) {
                start = getFirst(appendedSublines);
            }
            return start;
        }

        /** Get the subline at the end of the polyline or null if it does not exist.
         * @return the subline at the end of the polyline
         */
        public ConvexSubLine getEndSubLine() {
            ConvexSubLine end = getLast(appendedSublines);
            if (end == null) {
                end = getFirst(prependedSublines);
            }
            return end;
        }

        /** Append a subline to the end of the polyline.
         * @param subline subline to append to the polyline
         * @return the current builder instance
         * @throws IllegalStateException if the polyline contains a previous subline
         *      and the end vertex of the previous subline is not equivalent to the
         *      start vertex of the given subline.
         */
        public Builder append(final ConvexSubLine subline) {
            validateSubLinesConnected(getEndSubLine(), subline);
            appendInternal(subline);

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
                // make sure that we're not adding to an infinite subline
                final ConvexSubLine end = getEndSubLine();
                if (end != null) {
                    throw new IllegalStateException(
                            MessageFormat.format("Cannot add vertex {0} after infinite subline: {1}",
                                    vertex, end));
                }

                // this is the first vertex added
                startVertex = vertex;
                endVertex = vertex;
                endVertexPrecision = vertexPrecision;
            } else if (!endVertex.eq(vertex, endVertexPrecision)) {
                // only add the vertex if its not equal to the end point
                // of the last subline
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
            for (final Vector2D vertex : vertices) {
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

        /** Prepend a subline to the beginning of the polyline.
         * @param subline subline to prepend to the polyline
         * @return the current builder instance
         * @throws IllegalStateException if the polyline contains a start subline
         *      and the end vertex of the given subline is not equivalent to the
         *      start vertex of the start subline.
         */
        public Builder prepend(final ConvexSubLine subline) {
            validateSubLinesConnected(subline, getStartSubLine());
            prependInternal(subline);

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
                // make sure that we're not adding to an infinite subline
                final ConvexSubLine start = getStartSubLine();
                if (start != null) {
                    throw new IllegalStateException(
                            MessageFormat.format("Cannot add vertex {0} before infinite subline: {1}",
                                    vertex, start));
                }

                // this is the first vertex added
                startVertex = vertex;
                endVertex = vertex;
                endVertexPrecision = vertexPrecision;
            } else if (!vertex.eq(startVertex, vertexPrecision)) {
                // only add if the vertex is not equal to the start
                // point of the first subline
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

        /** Close the current polyline and build a new {@link Polyline} instance.  This method is equivalent
         * to {@code builder.build(true)}.
         * @return new closed polyline instance
         */
        public Polyline close() {
            return build(true);
        }

        /** Build a {@link Polyline} instance from the configured polyline. This method is equivalent
         * to {@code builder.build(false)}.
         * @return new polyline instance
         */
        public Polyline build() {
            return build(false);
        }

        /** Build a {@link Polyline} instance from the configured polyline.
         * @param close if true, the path will be closed by adding an end point equivalent to the
         *      start point
         * @return new polyline instance
         */
        public Polyline build(final boolean close) {
            if (close) {
                closePath();
            }

            // combine all of the sublines
            List<ConvexSubLine> result = null;

            if (prependedSublines != null) {
                result = prependedSublines;
                Collections.reverse(result);
            }

            if (appendedSublines != null) {
                if (result == null) {
                    result = appendedSublines;
                } else {
                    result.addAll(appendedSublines);
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
            appendedSublines = null;
            prependedSublines = null;

            // build the final polyline instance, using the shared empty instance if
            // no sublines are present
            return result.isEmpty() ? empty() : new Polyline(result);
        }

        /** Close the path by adding an end point equivalent to the path start point.
         * @throws IllegalStateException if the path cannot be closed
         */
        private void closePath() {
            final ConvexSubLine end = getEndSubLine();

            if (end != null) {
                if (startVertex != null && endVertex != null) {
                    if (!endVertex.eq(startVertex, endVertexPrecision)) {
                        appendInternal(Segment.fromPoints(endVertex, startVertex, endVertexPrecision));
                    }
                } else {
                    throw new IllegalStateException("Unable to close polyline: polyline is infinite");
                }
            }
        }

        /** Validate that the given sublines are connected, meaning that the end vertex of {@code previous}
         * is equivalent to the start vertex of {@code next}. The sublines are considered valid if either
         * subline is null.
         * @param previous previous subline
         * @param next next subline
         * @throws IllegalStateException if previous and next are not null and the end vertex of previous
         *      is not equivalent the start vertex of next
         */
        private void validateSubLinesConnected(final ConvexSubLine previous, final ConvexSubLine next) {
            if (previous != null && next != null) {
                final Vector2D nextStartVertex = next.getStartPoint();
                final Vector2D previousEndVertex = previous.getEndPoint();
                final DoublePrecisionContext previousPrecision = previous.getPrecision();

                if (nextStartVertex == null || previousEndVertex == null ||
                        !(nextStartVertex.eq(previousEndVertex, previousPrecision))) {

                    throw new IllegalStateException(
                            MessageFormat.format("Polyline sublines are not connected: previous= {0}, next= {1}",
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

        /** Append the given, validated subline to the polyline.
         * @param subline validated subline to append
         */
        private void appendInternal(final ConvexSubLine subline) {
            if (appendedSublines == null) {
                appendedSublines = new ArrayList<>();
            }

            if (appendedSublines.isEmpty() &&
                    (prependedSublines == null || prependedSublines.isEmpty())) {
                startVertex = subline.getStartPoint();
            }

            endVertex = subline.getEndPoint();
            endVertexPrecision = subline.getPrecision();

            appendedSublines.add(subline);
        }

        /** Prepend the given, validated subline to the polyline.
         * @param subline validated subline to prepend
         */
        private void prependInternal(final ConvexSubLine subline) {
            if (prependedSublines == null) {
                prependedSublines = new ArrayList<>();
            }

            startVertex = subline.getStartPoint();

            if (prependedSublines.isEmpty() &&
                    (appendedSublines == null || appendedSublines.isEmpty())) {
                endVertex = subline.getEndPoint();
                endVertexPrecision = subline.getPrecision();
            }

            prependedSublines.add(subline);
        }

        /** Get the first element in the list or null if the list is null
         * or empty.
         * @param list the list to return the first item from
         * @return the first item from the given list or null if it does not exist
         */
        private ConvexSubLine getFirst(final List<ConvexSubLine> list) {
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
            return null;
        }

        /** Get the last element in the list or null if the list is null
         * or empty.
         * @param list the list to return the last item from
         * @return the last item from the given list or null if it does not exist
         */
        private ConvexSubLine getLast(final List<ConvexSubLine> list) {
            if (list != null && !list.isEmpty()) {
                return list.get(list.size() - 1);
            }
            return null;
        }
    }

    /** Internal class returned when a polyline is simplified to remove
     * unecessary subline divisions. The {@link #simplify()} method on this
     * class simply returns the same instance.
     */
    private static final class SimplifiedPolyline extends Polyline {
        /** Create a new instance containing the given sublines. No validation is
         * performed on the inputs. Caller must ensure that the given sublines represent
         * a valid, simplified path.
         * @param sublines sublines comprising the path
         */
        private SimplifiedPolyline(final List<ConvexSubLine> sublines) {
            super(sublines);
        }

        /** {@inheritDoc} */
        @Override
        public Polyline simplify() {
            // already simplified
            return this;
        }
    }
}
