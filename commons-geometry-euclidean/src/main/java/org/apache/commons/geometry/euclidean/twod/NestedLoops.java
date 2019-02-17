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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.geometry.core.partitioning.Region;
import org.apache.commons.geometry.core.partitioning.RegionFactory;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.IntervalsSet;

/** This class represent a tree of nested 2D boundary loops.

 * <p>This class is used for piecewise polygons construction.
 * Polygons are built using the outline edges as
 * representative of boundaries, the orientation of these lines are
 * meaningful. However, we want to allow the user to specify its
 * outline loops without having to take care of this orientation. This
 * class is devoted to correct mis-oriented loops.<p>

 * <p>Orientation is computed assuming the piecewise polygon is finite,
 * i.e. the outermost loops have their exterior side facing points at
 * infinity, and hence are oriented counter-clockwise. The orientation of
 * internal loops is computed as the reverse of the orientation of
 * their immediate surrounding loop.</p>
 */
class NestedLoops {

    /** Boundary loop. */
    private Vector2D[] loop;

    /** Surrounded loops. */
    private List<NestedLoops> surrounded;

    /** Polygon enclosing a finite region. */
    private Region<Vector2D> polygon;

    /** Indicator for original loop orientation. */
    private boolean originalIsClockwise;

    /** Precision context used to compare floating point numbers. */
    private final DoublePrecisionContext precision;

    /** Simple Constructor.
     * <p>Build an empty tree of nested loops. This instance will become
     * the root node of a complete tree, it is not associated with any
     * loop by itself, the outermost loops are in the root tree child
     * nodes.</p>
     * @param precision precision context used to compare floating point values
     */
    NestedLoops(final DoublePrecisionContext precision) {
        this.surrounded = new ArrayList<>();
        this.precision  = precision;
    }

    /** Constructor.
     * <p>Build a tree node with neither parent nor children</p>
     * @param loop boundary loop (will be reversed in place if needed)
     * @param precision precision context used to compare floating point values
     * @exception IllegalArgumentException if an outline has an open boundary loop
     */
    private NestedLoops(final Vector2D[] loop, DoublePrecisionContext precision)
        throws IllegalArgumentException {

        if (loop[0] == null) {
            throw new IllegalArgumentException("An outline boundary loop is open");
        }

        this.loop       = loop;
        this.surrounded = new ArrayList<>();
        this.precision  = precision;

        // build the polygon defined by the loop
        final ArrayList<SubHyperplane<Vector2D>> edges = new ArrayList<>();
        Vector2D current = loop[loop.length - 1];
        for (int i = 0; i < loop.length; ++i) {
            final Vector2D previous = current;
            current = loop[i];
            final Line   line   = Line.fromPoints(previous, current, precision);
            final IntervalsSet region =
                new IntervalsSet(line.toSubSpace(previous).getX(),
                                 line.toSubSpace(current).getX(),
                                 precision);
            edges.add(new SubLine(line, region));
        }
        polygon = new PolygonsSet(edges, precision);

        // ensure the polygon encloses a finite region of the plane
        if (Double.isInfinite(polygon.getSize())) {
            polygon = new RegionFactory<Vector2D>().getComplement(polygon);
            originalIsClockwise = false;
        } else {
            originalIsClockwise = true;
        }

    }

    /** Add a loop in a tree.
     * @param bLoop boundary loop (will be reversed in place if needed)
     * @exception IllegalArgumentException if an outline has crossing
     * boundary loops or open boundary loops
     */
    public void add(final Vector2D[] bLoop) {
        add(new NestedLoops(bLoop, precision));
    }

    /** Add a loop in a tree.
     * @param node boundary loop (will be reversed in place if needed)
     * @exception IllegalArgumentException if an outline has boundary
     * loops that cross each other
     */
    private void add(final NestedLoops node) {

        // check if we can go deeper in the tree
        for (final NestedLoops child : surrounded) {
            if (child.polygon.contains(node.polygon)) {
                child.add(node);
                return;
            }
        }

        // check if we can absorb some of the instance children
        for (final Iterator<NestedLoops> iterator = surrounded.iterator(); iterator.hasNext();) {
            final NestedLoops child = iterator.next();
            if (node.polygon.contains(child.polygon)) {
                node.surrounded.add(child);
                iterator.remove();
            }
        }

        // we should be separate from the remaining children
        RegionFactory<Vector2D> factory = new RegionFactory<>();
        for (final NestedLoops child : surrounded) {
            if (!factory.intersection(node.polygon, child.polygon).isEmpty()) {
                throw new IllegalArgumentException("Some outline boundary loops cross each other");
            }
        }

        surrounded.add(node);

    }

    /** Correct the orientation of the loops contained in the tree.
     * <p>This is this method that really inverts the loops that where
     * provided through the {@link #add(Vector2D[]) add} method if
     * they are mis-oriented</p>
     */
    public void correctOrientation() {
        for (NestedLoops child : surrounded) {
            child.setClockWise(true);
        }
    }

    /** Set the loop orientation.
     * @param clockwise if true, the loop should be set to clockwise
     * orientation
     */
    private void setClockWise(final boolean clockwise) {

        if (originalIsClockwise ^ clockwise) {
            // we need to inverse the original loop
            int min = -1;
            int max = loop.length;
            while (++min < --max) {
                final Vector2D tmp = loop[min];
                loop[min] = loop[max];
                loop[max] = tmp;
            }
        }

        // go deeper in the tree
        for (final NestedLoops child : surrounded) {
            child.setClockWise(!clockwise);
        }

    }

}
