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
package org.apache.commons.geometry.euclidean.twod.path;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.Sized;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.twod.BoundarySource2D;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.LineConvexSubset;
import org.apache.commons.geometry.euclidean.twod.Lines;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

/** Class representing a connected path of {@link LineConvexSubset line convex subsets}.
 * The elements in the path are connected end to end, with the end vertex of the previous
 * element equivalent to the start vertex of the next element. Elements are not required to
 * be finite. However, since path elements are connected, only the first element and/or last
 * element may be infinite.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public class LinePath implements BoundarySource2D, Sized {
    /** Line path instance containing no elements. */
    private static final LinePath EMPTY = new LinePath(Collections.emptyList());

    /** The line convex subsets comprising the path. */
    private final List<LineConvexSubset> elements;

    /** Simple constructor. Callers are responsible for ensuring that the given list of
     * line subsets defines a valid path. No validation is performed.
     * @param elements elements defining the path.
     */
    LinePath(final List<LineConvexSubset> elements) {
        this.elements = Collections.unmodifiableList(elements);
    }

    /** {@inheritDoc} */
    @Override
    public Stream<LineConvexSubset> boundaryStream() {
        return getElements().stream();
    }

    /** Get the sequence of line subsets comprising the path.
     * @return the sequence of line subsets comprising the path
     */
    public List<LineConvexSubset> getElements() {
        return elements;
    }

    /** Get the line subset at the start of the path or null if the path is empty. If the
     * path consists of a single line subset, then the returned instance with be the same
     * as that returned by {@link #getEnd()}.
     * @return the line subset at the start of the path or null if the path is empty
     * @see #getEnd()
     */
    public LineConvexSubset getStart() {
        if (!isEmpty()) {
            return elements.get(0);
        }
        return null;
    }

    /** Get the line subset at the end of the path or null if the path is empty. If the
     * path consists of a single line subset, then the returned instance with be the same
     * as that returned by {@link #getStart()}.
     * @return the line subset at the end of the path or null if the path is empty
     * @see #getStart()
     */
    public LineConvexSubset getEnd() {
        if (!isEmpty()) {
            return elements.get(elements.size() - 1);
        }
        return null;
    }

    /** Get the sequence of vertices defined by the path. Vertices appear in the
     * list as many times as they are visited in the path. For example, the vertex
     * sequence for a closed path contains the start point at the beginning
     * of the list as well as the end.
     * @return the sequence of vertices defined by the path
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
        for (final LineConvexSubset sub : elements) {
            pt = sub.getEndPoint();
            if (pt != null) {
                sequence.add(pt);
            }
        }

        return sequence;
    }

    /** Return true if the path has an element with infinite size.
     * @return true if the path is infinite
     */
    @Override
    public boolean isInfinite() {
        return !isEmpty() && (getStartVertex() == null || getEndVertex() == null);
    }

    /** Return true if the path has a finite size. This will be true if there are
     * no elements in the path or if all elements have a finite length.
     * @return true if the path is finite
     */
    @Override
    public boolean isFinite() {
        return !isInfinite();
    }

    /** {@inheritDoc}
     *
     * <p>The size of the path is defined as the sum of the sizes (lengths) of all path elements.</p>
     */
    @Override
    public double getSize() {
        double sum = 0.0;
        for (final LineConvexSubset element : elements) {
            sum += element.getSize();
        }

        return sum;
    }

    /** Return true if the path does not contain any elements.
     * @return true if the path does not contain any elements
     */
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    /** Return true if the path is closed, meaning that the end point for the last
     * element is equivalent to the start point of the first.
     * @return true if the end point for the last element is equivalent to the
     *      start point for the first
     */
    public boolean isClosed() {
        final LineConvexSubset endElement = getEnd();

        if (endElement != null) {
            final Vector2D start = getStartVertex();
            final Vector2D end = endElement.getEndPoint();

            return start != null && end != null && start.eq(end, endElement.getPrecision());
        }

        return false;
    }

    /** Transform this instance with the argument, returning the result in a new instance.
     * @param transform the transform to apply
     * @return a new instance, transformed by the argument
     */
    public LinePath transform(final Transform<Vector2D> transform) {
        if (!isEmpty()) {
            final List<LineConvexSubset> transformed = elements.stream()
                .map(s -> s.transform(transform))
                .collect(Collectors.toCollection(ArrayList::new));

            return new LinePath(transformed);
        }

        return this;
    }

    /** Return a new instance with all line subset directions, and their order,
     * reversed. The last line subset in this instance will be the first in the
     * returned instance.
     * @return a new instance with the path reversed
     */
    public LinePath reverse() {
        if (!isEmpty()) {
            final List<LineConvexSubset> reversed = elements.stream()
                .map(LineConvexSubset::reverse)
                .collect(Collectors.toCollection(ArrayList::new));
            Collections.reverse(reversed);

            return new LinePath(reversed);
        }

        return this;
    }

    /** Simplify this path, if possible, by combining adjacent elements that lie on the
     * same line (as determined by {@link Line#equals(Object)}).
     * @return a simplified instance
     */
    public LinePath simplify() {
        final List<LineConvexSubset> simplified = new ArrayList<>();

        final int size = elements.size();

        LineConvexSubset current;
        Line currentLine;
        double end;

        int idx = 0;
        int testIdx;
        while (idx < size) {
            current = elements.get(idx);
            currentLine = current.getLine();
            end = current.getSubspaceEnd();

            // try to combine with forward neighbors
            testIdx = idx + 1;
            while (testIdx < size && currentLine.equals(elements.get(testIdx).getLine())) {
                end = Math.max(end, elements.get(testIdx).getSubspaceEnd());
                ++testIdx;
            }

            if (testIdx > idx + 1) {
                // we found something to merge
                simplified.add(Lines.subsetFromInterval(currentLine, current.getSubspaceStart(), end));
            } else {
                simplified.add(current);
            }

            idx = testIdx;
        }

        // combine the first and last items if needed
        if (isClosed() && simplified.size() > 2 && simplified.get(0).getLine().equals(
                simplified.get(simplified.size() - 1).getLine())) {

            final LineConvexSubset startElement = simplified.get(0);
            final LineConvexSubset endElement = simplified.remove(simplified.size() - 1);

            final LineConvexSubset combined = Lines.subsetFromInterval(
                    endElement.getLine(), endElement.getSubspaceStart(), startElement.getSubspaceEnd());

            simplified.set(0, combined);
        }

        return new SimplifiedLinePath(simplified);
    }

    /** Return a string representation of the path.
     *
     * <p>In order to keep the string representation short but useful, the exact format of the return
     * value depends on the properties of the path. See below for examples.
     *
     * <ul>
     *      <li>Empty path
     *          <ul>
     *              <li>{@code LinePath[empty= true]}</li>
     *          </ul>
     *      </li>
     *      <li>Single element
     *          <ul>
     *              <li>{@code LinePath[single= Segment[startPoint= (0.0, 0.0), endPoint= (1.0, 0.0)]]}</li>
     *          </ul>
     *      </li>
     *      <li>Path with infinite start element
     *          <ul>
     *              <li>{@code LinePath[startDirection= (1.0, 0.0), vertices= [(1.0, 0.0), (1.0, 1.0)]]}</li>
     *          </ul>
     *      </li>
     *      <li>Path with infinite end element
     *          <ul>
     *              <li>{@code LinePath[vertices= [(0.0, 1.0), (0.0, 0.0)], endDirection= (1.0, 0.0)]}</li>
     *          </ul>
     *      </li>
     *      <li>Path with infinite start and end elements
     *          <ul>
     *              <li>{@code LinePath[startDirection= (0.0, 1.0), vertices= [(0.0, 0.0)], endDirection= (1.0, 0.0)]}</li>
     *          </ul>
     *      </li>
     *      <li>Path with no infinite elements
     *          <ul>
     *              <li>{@code LinePath[vertices= [(0.0, 0.0), (1.0, 0.0), (1.0, 1.0)]]}</li>
     *          </ul>
     *      </li>
     * </ul>
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName())
            .append('[');

        if (elements.isEmpty()) {
            sb.append("empty= true");
        } else if (elements.size() == 1) {
            sb.append("single= ")
                .append(elements.get(0));
        } else {
            final LineConvexSubset startElement = getStart();
            if (startElement.getStartPoint() == null) {
                sb.append("startDirection= ")
                    .append(startElement.getLine().getDirection())
                    .append(", ");
            }

            sb.append("vertexSequence= ")
                .append(getVertexSequence());

            final LineConvexSubset endElement = getEnd();
            if (endElement.getEndPoint() == null) {
                sb.append(", endDirection= ")
                    .append(endElement.getLine().getDirection());
            }
        }

        sb.append(']');

        return sb.toString();
    }

    /** Get the start vertex for the path or null if the path is empty
     * or has an infinite start line subset.
     * @return the start vertex for the path or null if the path does
     *      not start with a vertex
     */
    private Vector2D getStartVertex() {
        final LineConvexSubset seg = getStart();
        return (seg != null) ? seg.getStartPoint() : null;
    }

    /** Get the end vertex for the path or null if the path is empty
     * or has an infinite end line subset.
     * @return the end vertex for the path or null if the path does
     *      not end with a vertex
     */
    private Vector2D getEndVertex() {
        final LineConvexSubset seg = getEnd();
        return (seg != null) ? seg.getEndPoint() : null;
    }

    /** Build a new path from the given line subsets.
     * @param subsets the line subsets to comprise the path
     * @return new path containing the given line subsets in order
     * @throws IllegalStateException if the line subsets do not form a connected path
     */
    public static LinePath from(final LineConvexSubset... subsets) {
        return from(Arrays.asList(subsets));
    }

    /** Build a new path from the given line subsets.
     * @param subsets the line subsets to comprise the path
     * @return new path containing the given line subsets in order
     * @throws IllegalStateException if the subsets do not form a connected path
     */
    public static LinePath from(final Collection<LineConvexSubset> subsets) {
        final Builder builder = builder(null);

        for (final LineConvexSubset subset : subsets) {
            builder.append(subset);
        }

        return builder.build();
    }

    /** Build a new path from the given vertices. A line segment is created
     * from the last vertex to the first one, if the two vertices are not already
     * considered equal using the given precision context. This method is equivalent to
     * calling {@link #fromVertices(Collection, boolean, DoublePrecisionContext)
     * fromVertices(vertices, true, precision)}
     * @param vertices the vertices to construct the closed path from
     * @param precision precision context used to construct the line segment
     *      instances for the path
     * @return new closed path constructed from the given vertices
     * @throws IllegalStateException if {@code vertices} contains only a single unique vertex
     * @see #fromVertices(Collection, boolean, DoublePrecisionContext)
     */
    public static LinePath fromVertexLoop(final Collection<Vector2D> vertices,
            final DoublePrecisionContext precision) {

        return fromVertices(vertices, true, precision);
    }

    /** Build a new path from the given vertices. No additional segment is added
     * from the last vertex to the first. This method is equivalent to calling
     * {@link #fromVertices(Collection, boolean, DoublePrecisionContext)
     * fromVertices(vertices, false, precision)}.
     * @param vertices the vertices to construct the path from
     * @param precision precision context used to construct the line segment
     *      instances for the path
     * @return new path constructed from the given vertices
     * @throws IllegalStateException if {@code vertices} contains only a single unique vertex
     * @see #fromVertices(Collection, boolean, DoublePrecisionContext)
     */
    public static LinePath fromVertices(final Collection<Vector2D> vertices,
            final DoublePrecisionContext precision) {

        return fromVertices(vertices, false, precision);
    }

    /** Build a new path from the given vertices.
     * @param vertices the vertices to construct the path from
     * @param close if true, a line segment is created from the last vertex
     *      given to the first one, if the two vertices are not already considered
     *      equal using the given precision context.
     * @param precision precision context used to construct the line segment
     *      instances for the path
     * @return new path constructed from the given vertices
     * @throws IllegalStateException if {@code vertices} contains only a single unique vertex
     */
    public static LinePath fromVertices(final Collection<Vector2D> vertices,
            final boolean close, final DoublePrecisionContext precision) {

        return builder(precision)
                .appendVertices(vertices)
                .build(close);
    }

    /** Return a path containing no elements.
     * @return a path containing no elements
     */
    public static LinePath empty() {
        return EMPTY;
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

    /** Class used to build line paths.
     */
    public static final class Builder {
        /** Line subsets appended to the path. */
        private List<LineConvexSubset> appended = null;

        /** Line subsets prepended to the path. */
        private List<LineConvexSubset> prepended = null;

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
         * {@link LineConvexSubset} instances since those contain their own precision contexts.
         * @param builderPrecision precision context to use when creating line segments
         *      from vertices
         * @return this instance
         */
        public Builder setPrecision(final DoublePrecisionContext builderPrecision) {
            this.precision = builderPrecision;

            return this;
        }

        /** Get the line subset at the start of the path or null if it does not exist.
         * @return the line subset at the start of the path
         */
        public LineConvexSubset getStart() {
            LineConvexSubset start = getLast(prepended);
            if (start == null) {
                start = getFirst(appended);
            }
            return start;
        }

        /** Get the line subset at the end of the path or null if it does not exist.
         * @return the line subset at the end of the path
         */
        public LineConvexSubset getEnd() {
            LineConvexSubset end = getLast(appended);
            if (end == null) {
                end = getFirst(prepended);
            }
            return end;
        }

        /** Append a line subset to the end of the path.
         * @param subset line subset to append to the path
         * @return the current builder instance
         * @throws IllegalStateException if the path contains a previous element
         *      and the end vertex of the previous element is not equivalent to the
         *      start vertex of the argument
         */
        public Builder append(final LineConvexSubset subset) {
            validateConnected(getEnd(), subset);
            appendInternal(subset);

            return this;
        }

        /** Add a vertex to the end of this path. If the path already has an end vertex,
         * then a line segment is added between the previous end vertex and this vertex,
         * using the configured precision context.
         * @param vertex the vertex to add
         * @return this instance
         * @see #setPrecision(DoublePrecisionContext)
         */
        public Builder append(final Vector2D vertex) {
            final DoublePrecisionContext vertexPrecision = getAddVertexPrecision();

            if (endVertex == null) {
                // make sure that we're not adding to an infinite element
                final LineConvexSubset end = getEnd();
                if (end != null) {
                    throw new IllegalStateException(
                            MessageFormat.format("Cannot add vertex {0} after infinite line subset: {1}",
                                    vertex, end));
                }

                // this is the first vertex added
                startVertex = vertex;
                endVertex = vertex;
                endVertexPrecision = vertexPrecision;
            } else if (!endVertex.eq(vertex, endVertexPrecision)) {
                // only add the vertex if its not equal to the end point
                // of the last element
                appendInternal(Lines.segmentFromPoints(endVertex, vertex, endVertexPrecision));
            }

            return this;
        }

        /** Convenience method for appending a collection of vertices to the path in a single method call.
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

        /** Convenience method for appending multiple vertices to the path at once.
         * @param vertices the vertices to append
         * @return this instance
         * @see #append(Vector2D)
         */
        public Builder appendVertices(final Vector2D... vertices) {
            return appendVertices(Arrays.asList(vertices));
        }

        /** Prepend a line subset to the beginning of the path.
         * @param subset line subset to prepend to the path
         * @return the current builder instance
         * @throws IllegalStateException if the path contains a start element
         *      and the end vertex of the argument is not equivalent to the
         *      start vertex of the start element.
         */
        public Builder prepend(final LineConvexSubset subset) {
            validateConnected(subset, getStart());
            prependInternal(subset);

            return this;
        }

        /** Add a vertex to the front of this path. If the path already has a start vertex,
         * then a line segment is added between this vertex and the previous start vertex,
         * using the configured precision context.
         * @param vertex the vertex to add
         * @return this instance
         * @see #setPrecision(DoublePrecisionContext)
         */
        public Builder prepend(final Vector2D vertex) {
            final DoublePrecisionContext vertexPrecision = getAddVertexPrecision();

            if (startVertex == null) {
                // make sure that we're not adding to an infinite element
                final LineConvexSubset start = getStart();
                if (start != null) {
                    throw new IllegalStateException(
                            MessageFormat.format("Cannot add vertex {0} before infinite line subset: {1}",
                                    vertex, start));
                }

                // this is the first vertex added
                startVertex = vertex;
                endVertex = vertex;
                endVertexPrecision = vertexPrecision;
            } else if (!vertex.eq(startVertex, vertexPrecision)) {
                // only add if the vertex is not equal to the start
                // point of the first element
                prependInternal(Lines.segmentFromPoints(vertex, startVertex, vertexPrecision));
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
        public Builder prependVertices(final Collection<Vector2D> vertices) {
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
        public Builder prependVertices(final Vector2D... vertices) {
            for (int i = vertices.length - 1; i >= 0; --i) {
                prepend(vertices[i]);
            }

            return this;
        }

        /** Close the current path and build a new {@link LinePath} instance.  This method is equivalent
         * to {@code builder.build(true)}.
         * @return new closed path instance
         * @throws IllegalStateException if the builder was given only a single unique vertex
         */
        public LinePath close() {
            return build(true);
        }

        /** Build a {@link LinePath} instance from the configured path. This method is equivalent
         * to {@code builder.build(false)}.
         * @return new path instance
         * @throws IllegalStateException if the builder was given only a single unique vertex
         */
        public LinePath build() {
            return build(false);
        }

        /** Build a {@link LinePath} instance from the configured path.
         * @param close if true, the path will be closed by adding an end point equivalent to the
         *      start point
         * @return new path instance
         * @throws IllegalStateException if the builder was given only a single unique vertex
         */
        public LinePath build(final boolean close) {
            if (close) {
                closePath();
            }

            // combine all of the line subsets
            List<LineConvexSubset> result = null;

            if (prepended != null) {
                result = prepended;
                Collections.reverse(result);
            }

            if (appended != null) {
                if (result == null) {
                    result = appended;
                } else {
                    result.addAll(appended);
                }
            }

            if (result == null) {
                result = Collections.emptyList();
            }

            if (result.isEmpty() && startVertex != null) {
                throw new IllegalStateException(
                        MessageFormat.format("Unable to create line path; only a single unique vertex provided: {0} ",
                                startVertex));
            }

            // clear internal state
            appended = null;
            prepended = null;

            // build the final path instance, using the shared empty instance if
            // no line subsets are present

            return result.isEmpty() ? empty() : new LinePath(result);
        }

        /** Close the path by adding an end point equivalent to the path start point.
         * @throws IllegalStateException if the path cannot be closed
         */
        private void closePath() {
            final LineConvexSubset end = getEnd();

            if (end != null) {
                if (startVertex != null && endVertex != null) {
                    if (!endVertex.eq(startVertex, endVertexPrecision)) {
                        appendInternal(Lines.segmentFromPoints(endVertex, startVertex, endVertexPrecision));
                    }
                } else {
                    throw new IllegalStateException("Unable to close line path: line path is infinite");
                }
            }
        }

        /** Validate that the given line subsets  are connected, meaning that the end vertex of {@code previous}
         * is equivalent to the start vertex of {@code next}. The line subsets are considered valid if either
         * line subset is null.
         * @param previous previous line subset
         * @param next next line subset
         * @throws IllegalStateException if previous and next are not null and the end vertex of previous
         *      is not equivalent the start vertex of next
         */
        private void validateConnected(final LineConvexSubset previous, final LineConvexSubset next) {
            if (previous != null && next != null) {
                final Vector2D nextStartVertex = next.getStartPoint();
                final Vector2D previousEndVertex = previous.getEndPoint();
                final DoublePrecisionContext previousPrecision = previous.getPrecision();

                if (nextStartVertex == null || previousEndVertex == null ||
                        !(nextStartVertex.eq(previousEndVertex, previousPrecision))) {

                    throw new IllegalStateException(
                            MessageFormat.format("Path line subsets are not connected: previous= {0}, next= {1}",
                                    previous, next));
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

        /** Append the given, validated line subsets to the path.
         * @param subset validated line subset to append
         */
        private void appendInternal(final LineConvexSubset subset) {
            if (appended == null) {
                appended = new ArrayList<>();
            }

            if (appended.isEmpty() &&
                    (prepended == null || prepended.isEmpty())) {
                startVertex = subset.getStartPoint();
            }

            endVertex = subset.getEndPoint();
            endVertexPrecision = subset.getPrecision();

            appended.add(subset);
        }

        /** Prepend the given, validated line subset to the path.
         * @param subset validated line subset to prepend
         */
        private void prependInternal(final LineConvexSubset subset) {
            if (prepended == null) {
                prepended = new ArrayList<>();
            }

            startVertex = subset.getStartPoint();

            if (prepended.isEmpty() &&
                    (appended == null || appended.isEmpty())) {
                endVertex = subset.getEndPoint();
                endVertexPrecision = subset.getPrecision();
            }

            prepended.add(subset);
        }

        /** Get the first element in the list or null if the list is null
         * or empty.
         * @param list the list to return the first item from
         * @return the first item from the given list or null if it does not exist
         */
        private LineConvexSubset getFirst(final List<LineConvexSubset> list) {
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
        private LineConvexSubset getLast(final List<LineConvexSubset> list) {
            if (list != null && !list.isEmpty()) {
                return list.get(list.size() - 1);
            }
            return null;
        }
    }

    /** Internal class returned when a line path is simplified to remove unnecessary line subset divisions.
     * The {@link #simplify()} method on this class simply returns the same instance.
     */
    private static final class SimplifiedLinePath extends LinePath {
        /** Create a new instance containing the given line subsets. No validation is
         * performed on the inputs. Caller must ensure that the given line subsets represent
         * a valid, simplified path.
         * @param elements line subsets comprising the path
         */
        private SimplifiedLinePath(final List<LineConvexSubset> elements) {
            super(elements);
        }

        /** {@inheritDoc} */
        @Override
        public SimplifiedLinePath simplify() {
            // already simplified
            return this;
        }
    }
}
