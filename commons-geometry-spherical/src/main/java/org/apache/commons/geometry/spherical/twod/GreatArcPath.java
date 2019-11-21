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
package org.apache.commons.geometry.spherical.twod;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** Class representing a connected sequence of {@link GreatArc} instances.
 */
public class GreatArcPath implements Iterable<GreatArc>, Serializable {

    /** Serializable UID */
    private static final long serialVersionUID = 20191028L;

    /** Instance containing no arcs. */
    private static final GreatArcPath EMPTY = new GreatArcPath(Collections.emptyList());

    /** Arcs comprising the instance. */
    private final List<GreatArc> arcs;

    /** Simple constructor. No validation is performed on the input arc.
     * @param arcs arcs for the path, in connection order
     */
    private GreatArcPath(final List<GreatArc> arcs) {
        this.arcs = Collections.unmodifiableList(arcs);
    }

    /** Get the arcs in path.
     * @return the arcs in the path
     */
    public List<GreatArc> getArcs() {
        return arcs;
    }

    /** Get the start arc for the path or null if the path is empty.
     * @return the start arc for the path or null if the path is empty
     */
    public GreatArc getStartArc() {
        if (!isEmpty()) {
            return arcs.get(0);
        }
        return null;
    }

    /** Get the end arc for the path or null if the path is empty.
     * @return the end arc for the path or null if the path is empty
     */
    public GreatArc getEndArc() {
        if (!isEmpty()) {
            return arcs.get(arcs.size() - 1);
        }
        return null;
    }

    /** Get the start vertex for the path or null if the path is empty
     * or consists of a single, full arc.
     * @return the start vertex for the path
     */
    public Point2S getStartVertex() {
        final GreatArc arc = getStartArc();
        return (arc != null) ? arc.getStartPoint() : null;
    }

    /** Get the end vertex for the path or null if the path is empty
     * or consists of a single, full arc.
     * @return the end vertex for the path
     */
    public Point2S getEndVertex() {
        final GreatArc arc = getEndArc();
        return (arc != null) ? arc.getEndPoint() : null;
    }

    /** Get the vertices contained in the path in the order they appear.
     * Closed paths contain the start vertex at the beginning of the list
     * as well as the end.
     * @return the vertices contained in the path in order they appear
     */
    public List<Point2S> getVertices() {
        final List<Point2S> vertices = new ArrayList<>();

        Point2S pt;

        // add the start point, if present
        pt = getStartVertex();
        if (pt != null) {
            vertices.add(pt);
        }

        // add end points
        for (GreatArc arc : arcs) {
            pt = arc.getEndPoint();
            if (pt != null) {
                vertices.add(pt);
            }
        }

        return vertices;
    }

    /** Return true if the path does not contain any arcs.
     * @return true if the path does not contain any arcs
     */
    public boolean isEmpty() {
        return arcs.isEmpty();
    }

    /** Return true if the path is closed, meaning that the end
     * point for the last arc is equal to the start point
     * for the path.
     * @return true if the end point for the last arc is
     *      equal to the start point for the path
     */
    public boolean isClosed() {
        final GreatArc endArc = getEndArc();

        if (endArc != null) {
            final Point2S start = getStartVertex();
            final Point2S end = endArc.getEndPoint();

            return start != null && end != null && start.eq(end, endArc.getPrecision());
        }

        return false;
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<GreatArc> iterator() {
        return arcs.iterator();
    }

    /** Construct a {@link RegionBSPTree2S} from the arcs in this instance.
     * @return a bsp tree constructed from the arcs in this instance
     */
    public RegionBSPTree2S toTree() {
        RegionBSPTree2S tree = RegionBSPTree2S.empty();
        tree.insert(this);

        return tree;
    }

    /** Return a string representation of this arc path instance.
    *
    * <p>In order to keep the string representation short but useful, the exact format of the return
    * value depends on the properties of the path. See below for examples.
    *
    * <ul>
    *      <li>Empty path
    *          <ul>
    *              <li>{@code GreatArcPath[empty= true]}</li>
    *          </ul>
    *      </li>
    *      <li>Single, full arc
    *          <ul>
    *              <li>{@code GreatArcPath[full= true, circle= GreatCircle[pole= (0.0, 0.0, 1.0), x= (0.0, 1.0, -0.0), y= (-1.0, 0.0, 0.0)]]}</li>
    *          </ul>
    *      </li>
    *      <li>One or more non-full arcs
    *          <ul>
    *              <li>{@code GreatArcPath[vertices= [(0.0, 1.5707963267948966), (1.5707963267948966, 1.5707963267948966)]]}</li>
    *          </ul>
    *      </li>
    * </ul>
    * </p>
    */
   @Override
   public String toString() {
       final StringBuilder sb = new StringBuilder();
       sb.append(this.getClass().getSimpleName())
           .append('[');

       if (isEmpty()) {
           sb.append("empty= true");
       }
       else if (arcs.size() == 1 && arcs.get(0).isFull()) {
           sb.append("full= true, circle= ")
               .append(arcs.get(0).getCircle());
       }
       else {
           sb.append("vertices= ")
               .append(getVertices());
       }

      sb.append("]");

       return sb.toString();
   }

    /** Construct a new path from the given arcs.
     * @param arcs arc instance to use to construct the path
     * @return a new instance constructed from the given arc instances
     */
    public static GreatArcPath fromArcs(final GreatArc ... arcs) {
        return fromArcs(Arrays.asList(arcs));
    }

    /** Construct a new path from the given arcs.
     * @param arcs arc instance to use to construct the path
     * @return a new instance constructed from the given arc instances
     */
    public static GreatArcPath fromArcs(final Collection<GreatArc> arcs) {
        final Builder builder = builder(null);
        for (GreatArc arc : arcs) {
            builder.append(arc);
        }

        return builder.build();
    }

    /** Return a new path formed by connecting the given vertices. An additional arc is added
     * from the last point to the first point to construct a loop, if the two points are not
     * already considered equal by the given precision context. This method is equivalent
     * to calling {@link #fromVertices(Collection, boolean, DoublePrecisionContext)
     * fromPoints(points, true, precision)}.
     * @param vertices the points to construct the path from
     * @param precision precision precision context used to construct the arc instances for the
     *      path
     * @return a new path formed by connecting the given vertices
     * @see #fromVertices(Collection, boolean, DoublePrecisionContext)
     */
    public static GreatArcPath fromVertexLoop(final Collection<Point2S> vertices, final DoublePrecisionContext precision) {
        return fromVertices(vertices, true, precision);
    }

    /** Return a new path formed by connecting the given vertices. No additional arc
     * is inserted to connect the last point to the first. This method is equivalent
     * to calling {@link #fromVertices(Collection, boolean, DoublePrecisionContext)
     * fromPoint(points, false, precision)}.
     * @param vertices the points to construct the path from
     * @param precision precision context used to construct the arc instances for the
     *      path
     * @return a new path formed by connecting the given vertices
     * @see #fromVertices(Collection, boolean, DoublePrecisionContext)
     */
    public static GreatArcPath fromVertices(final Collection<Point2S> vertices, final DoublePrecisionContext precision) {
        return fromVertices(vertices, false, precision);
    }

    /** Return a new path formed by connecting the given vertices.
     * @param vertices the points to construct the path from
     * @param close if true, then an additional arc will be added from the last point
     *      to the first, if the points are not already considered equal by the given
     *      precision context
     * @param precision precision context used to construct the arc instances for the
     *      path
     * @return a new path formed by connecting the given points
     */
    public static GreatArcPath fromVertices(final Collection<Point2S> vertices, final boolean close,
            final DoublePrecisionContext precision) {

        final Builder builder = builder(precision);
        builder.appendVertices(vertices);

        return close ?
                builder.close() :
                builder.build();
    }

    /** Return a {@link Builder} instance configured with the given precision
     * context. The precision context is used when building arcs from points
     * and may be omitted if raw points are not used.
     * @param precision precision context to use when building arcs from
     *      raw points; may be null if raw points are not used.
     * @return a new {@link Builder} instance
     */
    public static Builder builder(final DoublePrecisionContext precision) {
        return new Builder(precision);
    }

    /** Get an instance containing no arcs.
     * @return an instance containing no arcs
     */
    public static GreatArcPath empty() {
        return EMPTY;
    }

    /** Class used to build arc paths.
     */
    public static final class Builder implements Serializable {

        /** Serializable UID */
        private static final long serialVersionUID = 20191031L;

        /** Arcs appended to the path. */
        private List<GreatArc> appendedArcs = null;

        /** Arcs prepended to the path. */
        private List<GreatArc> prependedArcs = null;

        /** Precision context used when creating arcs directly from points. */
        private DoublePrecisionContext precision;

        /** The current point at the start of the path. */
        private Point2S startVertex;

        /** The current point at the end of the path. */
        private Point2S endVertex;

        /** The precision context used when performing comparisons involving the current
         * end point.
         */
        private DoublePrecisionContext endVertexPrecision;

        /** Construct a new instance configured with the given precision context. The
         * precision context is used when building arcs from points and
         * may be omitted if raw points are not used.
         * @param precision precision context to use when creating arcs
         *      from points
         */
        private Builder(final DoublePrecisionContext precision) {
            setPrecision(precision);
        }

        /** Set the precision context. This context is used only when creating arcs
         * from appended or prepended points. It is not used when adding existing
         * {@link GreatArc} instances since those contain their own precision contexts.
         * @param precision precision context to use when creating arcs from points
         * @return this instance
         */
        public Builder setPrecision(final DoublePrecisionContext precision) {
            this.precision = precision;

            return this;
        }

        /** Get the arc at the start of the path or null if it does not exist.
         * @return the arc at the start of the path
         */
        public GreatArc getStartArc() {
            GreatArc start = getLast(prependedArcs);
            if (start == null) {
                start = getFirst(appendedArcs);
            }
            return start;
        }

        /** Get the arc at the end of the path or null if it does not exist.
         * @return the arc at the end of the path
         */
        public GreatArc getEndArc() {
            GreatArc end = getLast(appendedArcs);
            if (end == null) {
                end = getFirst(prependedArcs);
            }
            return end;
        }

        /** Append an arc to the end of the path.
         * @return the current builder instance
         * @throws IllegalStateException if the path contains a previous arc
         *      and the end point of the previous arc is not equivalent to the
         *      start point of the given arc
         */
        public Builder append(final GreatArc arc) {
            validateArcsConnected(getEndArc(), arc);
            appendInternal(arc);

            return this;
        }

        /** Add a vertex to the end of this path. If the path already has an end vertex,
         * then an arc is added between the previous end vertex and this vertex,
         * using the configured precision context.
         * @param vertex the vertex to add
         * @return this instance
         * @see #setPrecision(DoublePrecisionContext)
         */
        public Builder append(final Point2S vertex) {
            final DoublePrecisionContext precision = getAddPointPrecision();

            if (endVertex == null) {
                // make sure that we're not adding to a full arc
                final GreatArc end = getEndArc();
                if (end != null) {
                    throw new IllegalStateException("Cannot add point " + vertex + " after full arc: " + end);
                }

                // this is the first vertex added
                startVertex = vertex;
                endVertex = vertex;
                endVertexPrecision = precision;
            }
            else if (!endVertex.eq(vertex, endVertexPrecision)) {
                // only add the vertex if its not equal to the end point
                // of the last arc
                appendInternal(GreatArc.fromPoints(endVertex, vertex, endVertexPrecision));
            }

            return this;
        }

        /** Convenience method for appending a collection of vertices to the path in a single
         * method call.
         * @param vertices the vertices to append
         * @return this instance
         * @see #append(Point2S)
         */
        public Builder appendVertices(final Collection<Point2S> vertices) {
            for (Point2S vertex : vertices) {
                append(vertex);
            }

            return this;
        }

        /** Convenience method for appending multiple vertices to the path at once.
         * @param vertices the points to append
         * @return this instance
         * @see #append(Point2S)
         */
        public Builder appendVertices(final Point2S ... vertices) {
            return appendVertices(Arrays.asList(vertices));
        }

        /** Prepend an arc to the beginning of the path.
         * @return the current builder instance
         * @throws IllegalStateException if the path contains a start arc
         *      and the end point of the given arc is not equivalent to the
         *      start point of the start arc
         */
        public Builder prepend(final GreatArc arc) {
            validateArcsConnected(arc, getStartArc());
            prependInternal(arc);

            return this;
        }

        /** Add a vertex to the front of this path. If the path already has a start vertex,
         * then an arc is added between this vertex and the previous start vertex,
         * using the configured precision context.
         * @param vertex the vertex to add
         * @return this instance
         * @see #setPrecision(DoublePrecisionContext)
         */
        public Builder prepend(final Point2S vertex) {
            final DoublePrecisionContext vertexPrecision = getAddPointPrecision();

            if (startVertex == null) {
                // make sure that we're not adding to a full arc
                final GreatArc start = getStartArc();
                if (start != null) {
                    throw new IllegalStateException("Cannot add point " + vertex + " before full arc: " + start);
                }

                // this is the first vertex added
                startVertex = vertex;
                endVertex = vertex;
                endVertexPrecision = vertexPrecision;
            }
            else if (!vertex.eq(startVertex, vertexPrecision)) {
                // only add if the vertex is not equal to the start
                // point of the first arc
                prependInternal(GreatArc.fromPoints(vertex, startVertex, vertexPrecision));
            }

            return this;
        }

        /** Convenience method for prepending a collection of vertices to the path in a single method call.
         * The vertices are logically prepended as a single group, meaning that the first vertex
         * in the given collection appears as the first vertex in the path after this method call.
         * Internally, this means that the vertices are actually passed to the {@link #prepend(Point2S)}
         * method in reverse order.
         * @param vertices the points to prepend
         * @return this instance
         * @see #prepend(Point2S)
         */
        public Builder prependPoints(final Collection<Point2S> vertices) {
            return prependPoints(vertices.toArray(new Point2S[0]));
        }

        /** Convenience method for prepending multiple vertices to the path in a single method call.
         * The vertices are logically prepended as a single group, meaning that the first vertex
         * in the given collection appears as the first vertex in the path after this method call.
         * Internally, this means that the vertices are actually passed to the {@link #prepend(Point2S)}
         * method in reverse order.
         * @param vertices the vertices to prepend
         * @return this instance
         * @see #prepend(Point2S)
         */
        public Builder prependPoints(final Point2S ... vertices) {
            for (int i=vertices.length - 1; i >=0 ; --i) {
                prepend(vertices[i]);
            }

            return this;
        }

        /** Close the current path and build a new {@link GreatArcPath} instance.
         * @return new closed path instance
         */
        public GreatArcPath close() {
            final GreatArc end = getEndArc();

            if (end != null) {
                if (startVertex != null && endVertex != null) {
                    if (!endVertex.eq(startVertex, endVertexPrecision)) {
                        appendInternal(GreatArc.fromPoints(endVertex, startVertex, endVertexPrecision));
                    }
                }
                else {
                    throw new IllegalStateException("Unable to close path: path is full");
                }
            }

            return build();
        }

        /** Build a {@link GreatArcPath} instance from the configured path.
         * @return new path instance
         */
        public GreatArcPath build() {
            // combine all of the arcs
            List<GreatArc> result = null;

            if (prependedArcs != null) {
                result = prependedArcs;
                Collections.reverse(result);
            }

            if (appendedArcs != null) {
                if (result == null) {
                    result = appendedArcs;
                }
                else {
                    result.addAll(appendedArcs);
                }
            }

            if (result == null) {
                result = Collections.emptyList();
            }

            if (result.isEmpty() && startVertex != null) {
                throw new IllegalStateException("Unable to create path; only a single point provided: " + startVertex);
            }

            // clear internal state
            appendedArcs = null;
            prependedArcs = null;

            // build the final path instance, using the shared empty instance if
            // no arcs are present
            return result.isEmpty() ? empty() : new GreatArcPath(result);
        }

        /** Validate that the given arcs are connected, meaning that the end point of {@code previous}
         * is equivalent to the start point of {@code next}. The arcs are considered valid if either
         * arc is null.
         * @throws IllegalStateException if previous and next are not null and the end point of previous
         *      is not equivalent the start point of next
         */
        private void validateArcsConnected(final GreatArc previous, final GreatArc next) {
            if (previous != null && next != null) {
                final Point2S nextStartVertex = next.getStartPoint();
                final Point2S previousEndVertex = previous.getEndPoint();
                final DoublePrecisionContext precision = previous.getPrecision();

                if (nextStartVertex == null || previousEndVertex == null ||
                        !(nextStartVertex.eq(previousEndVertex, precision))) {

                    throw new IllegalStateException("Path arcs are not connected: " +
                        "previous= " + previous + ", next= " + next);
                }
            }
        }

        /** Get the precision context used when adding raw points to the path. An exception is thrown
         * if no precision has been specified.
         * @return the precision context used when working with raw points
         * @throws IllegalStateException if no precision context is configured
         */
        private DoublePrecisionContext getAddPointPrecision() {
            if (precision == null) {
                throw new IllegalStateException("Unable to create arc: no point precision specified");
            }

            return precision;
        }

        /** Append the given, validated arc to the path.
         * @param arc validated arc to append
         */
        private void appendInternal(final GreatArc arc) {
            if (appendedArcs == null) {
                appendedArcs = new ArrayList<>();
            }

            if (appendedArcs.isEmpty() &&
                    (prependedArcs == null || prependedArcs.isEmpty())) {
                startVertex = arc.getStartPoint();
            }

            endVertex = arc.getEndPoint();
            endVertexPrecision = arc.getPrecision();

            appendedArcs.add(arc);
        }

        /** Prepend the given, validated arc to the path.
         * @param arc validated arc to prepend
         */
        private void prependInternal(final GreatArc arc) {
            if (prependedArcs == null) {
                prependedArcs = new ArrayList<>();
            }

            startVertex = arc.getStartPoint();

            if (prependedArcs.isEmpty() &&
                    (appendedArcs == null || appendedArcs.isEmpty())) {
                endVertex = arc.getEndPoint();
                endVertexPrecision = arc.getPrecision();
            }

            prependedArcs.add(arc);
        }

        /** Get the first element in the list or null if the list is null
         * or empty.
         * @param list the list to return the first item from
         * @return the first item from the given list or null if it does not exist
         */
        private GreatArc getFirst(final List<GreatArc> list) {
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
        private GreatArc getLast(final List<GreatArc> list) {
            if (list != null && list.size() > 0) {
                return list.get(list.size() - 1);
            }
            return null;
        }
    }
}
