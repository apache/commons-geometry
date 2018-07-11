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
package org.apache.commons.geometry.spherical.oned;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.partitioning.AbstractRegion;
import org.apache.commons.geometry.core.partitioning.BSPTree;
import org.apache.commons.geometry.core.partitioning.BoundaryProjection;
import org.apache.commons.geometry.core.partitioning.Side;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.apache.commons.numbers.core.Precision;

/** This class represents a region of a circle: a set of arcs.
 * <p>
 * Note that due to the wrapping around \(2 \pi\), barycenter is
 * ill-defined here. It was defined only in order to fulfill
 * the requirements of the {@link
 * org.apache.commons.geometry.partitioning.Region Region}
 * interface, but its use is discouraged.
 * </p>
 */
public class ArcsSet extends AbstractRegion<S1Point, S1Point> implements Iterable<double[]> {

    /** Message used for internal errors. */
    private static final String INTERNAL_ERROR_MESSAGE = "Please file a bug report";

    /** Build an arcs set representing the whole circle.
     * @param tolerance tolerance below which close sub-arcs are merged together
     */
    public ArcsSet(final double tolerance) {
        super(tolerance);
    }

    /** Build an arcs set corresponding to a single arc.
     * <p>
     * If either {@code lower} is equals to {@code upper} or
     * the interval exceeds \( 2 \pi \), the arc is considered
     * to be the full circle and its initial defining boundaries
     * will be forgotten. {@code lower} is not allowed to be greater
     * than {@code upper} (an exception is thrown in this case).
     * </p>
     * @param lower lower bound of the arc
     * @param upper upper bound of the arc
     * @param tolerance tolerance below which close sub-arcs are merged together
     * @exception IllegalArgumentException if lower is greater than upper
     */
    public ArcsSet(final double lower, final double upper, final double tolerance)
        throws IllegalArgumentException {
        super(buildTree(lower, upper, tolerance), tolerance);
    }

    /** Build an arcs set from an inside/outside BSP tree.
     * <p>The leaf nodes of the BSP tree <em>must</em> have a
     * {@code Boolean} attribute representing the inside status of
     * the corresponding cell (true for inside cells, false for outside
     * cells). In order to avoid building too many small objects, it is
     * recommended to use the predefined constants
     * {@code Boolean.TRUE} and {@code Boolean.FALSE}</p>
     * @param tree inside/outside BSP tree representing the arcs set
     * @param tolerance tolerance below which close sub-arcs are merged together
     * @exception InconsistentStateAt2PiWrapping if the tree leaf nodes are not
     * consistent across the \( 0, 2 \pi \) crossing
     */
    public ArcsSet(final BSPTree<S1Point> tree, final double tolerance)
        throws InconsistentStateAt2PiWrapping {
        super(tree, tolerance);
        check2PiConsistency();
    }

    /** Build an arcs set from a Boundary REPresentation (B-rep).
     * <p>The boundary is provided as a collection of {@link
     * SubHyperplane sub-hyperplanes}. Each sub-hyperplane has the
     * interior part of the region on its minus side and the exterior on
     * its plus side.</p>
     * <p>The boundary elements can be in any order, and can form
     * several non-connected sets (like for example polygons with holes
     * or a set of disjoints polyhedrons considered as a whole). In
     * fact, the elements do not even need to be connected together
     * (their topological connections are not used here). However, if the
     * boundary does not really separate an inside open from an outside
     * open (open having here its topological meaning), then subsequent
     * calls to the {@link
     * org.apache.commons.geometry.partitioning.Region#checkPoint(org.apache.commons.geometry.Point)
     * checkPoint} method will not be meaningful anymore.</p>
     * <p>If the boundary is empty, the region will represent the whole
     * space.</p>
     * @param boundary collection of boundary elements
     * @param tolerance tolerance below which close sub-arcs are merged together
     * @exception InconsistentStateAt2PiWrapping if the tree leaf nodes are not
     * consistent across the \( 0, 2 \pi \) crossing
     */
    public ArcsSet(final Collection<SubHyperplane<S1Point>> boundary, final double tolerance)
        throws InconsistentStateAt2PiWrapping {
        super(boundary, tolerance);
        check2PiConsistency();
    }

    /** Build an inside/outside tree representing a single arc.
     * @param lower lower angular bound of the arc
     * @param upper upper angular bound of the arc
     * @param tolerance tolerance below which close sub-arcs are merged together
     * @return the built tree
     * @exception IllegalArgumentException if lower is greater than upper
     */
    private static BSPTree<S1Point> buildTree(final double lower, final double upper,
                                               final double tolerance)
        throws IllegalArgumentException {

        if (Precision.equals(lower, upper, 0) || (upper - lower) >= Geometry.TWO_PI) {
            // the tree must cover the whole circle
            return new BSPTree<>(Boolean.TRUE);
        } else  if (lower > upper) {
            throw new IllegalArgumentException("Endpoints do not specify an interval: [" + lower + ", " +  upper + "]");
        }

        // this is a regular arc, covering only part of the circle
        final double normalizedLower = PlaneAngleRadians.normalizeBetweenZeroAndTwoPi(lower);
        final double normalizedUpper = normalizedLower + (upper - lower);
        final SubHyperplane<S1Point> lowerCut =
                new LimitAngle(S1Point.of(normalizedLower), false, tolerance).wholeHyperplane();

        if (normalizedUpper <= Geometry.TWO_PI) {
            // simple arc starting after 0 and ending before 2 \pi
            final SubHyperplane<S1Point> upperCut =
                    new LimitAngle(S1Point.of(normalizedUpper), true, tolerance).wholeHyperplane();
            return new BSPTree<>(lowerCut,
                                         new BSPTree<S1Point>(Boolean.FALSE),
                                         new BSPTree<>(upperCut,
                                                               new BSPTree<S1Point>(Boolean.FALSE),
                                                               new BSPTree<S1Point>(Boolean.TRUE),
                                                               null),
                                         null);
        } else {
            // arc wrapping around 2 \pi
            final SubHyperplane<S1Point> upperCut =
                    new LimitAngle(S1Point.of(normalizedUpper - Geometry.TWO_PI), true, tolerance).wholeHyperplane();
            return new BSPTree<>(lowerCut,
                                         new BSPTree<>(upperCut,
                                                               new BSPTree<S1Point>(Boolean.FALSE),
                                                               new BSPTree<S1Point>(Boolean.TRUE),
                                                               null),
                                         new BSPTree<S1Point>(Boolean.TRUE),
                                         null);
        }

    }

    /** Check consistency.
    * @exception InconsistentStateAt2PiWrapping if the tree leaf nodes are not
    * consistent across the \( 0, 2 \pi \) crossing
    */
    private void check2PiConsistency() throws InconsistentStateAt2PiWrapping {

        // start search at the tree root
        BSPTree<S1Point> root = getTree(false);
        if (root.getCut() == null) {
            return;
        }

        // find the inside/outside state before the smallest internal node
        final Boolean stateBefore = (Boolean) getFirstLeaf(root).getAttribute();

        // find the inside/outside state after the largest internal node
        final Boolean stateAfter = (Boolean) getLastLeaf(root).getAttribute();

        if (stateBefore ^ stateAfter) {
            throw new InconsistentStateAt2PiWrapping();
        }

    }

    /** Get the first leaf node of a tree.
     * @param root tree root
     * @return first leaf node (i.e. node corresponding to the region just after 0.0 radians)
     */
    private BSPTree<S1Point> getFirstLeaf(final BSPTree<S1Point> root) {

        if (root.getCut() == null) {
            return root;
        }

        // find the smallest internal node
        BSPTree<S1Point> smallest = null;
        for (BSPTree<S1Point> n = root; n != null; n = previousInternalNode(n)) {
            smallest = n;
        }

        return leafBefore(smallest);

    }

    /** Get the last leaf node of a tree.
     * @param root tree root
     * @return last leaf node (i.e. node corresponding to the region just before \( 2 \pi \) radians)
     */
    private BSPTree<S1Point> getLastLeaf(final BSPTree<S1Point> root) {

        if (root.getCut() == null) {
            return root;
        }

        // find the largest internal node
        BSPTree<S1Point> largest = null;
        for (BSPTree<S1Point> n = root; n != null; n = nextInternalNode(n)) {
            largest = n;
        }

        return leafAfter(largest);

    }

    /** Get the node corresponding to the first arc start.
     * @return smallest internal node (i.e. first after 0.0 radians, in trigonometric direction),
     * or null if there are no internal nodes (i.e. the set is either empty or covers the full circle)
     */
    private BSPTree<S1Point> getFirstArcStart() {

        // start search at the tree root
        BSPTree<S1Point> node = getTree(false);
        if (node.getCut() == null) {
            return null;
        }

        // walk tree until we find the smallest internal node
        node = getFirstLeaf(node).getParent();

        // walk tree until we find an arc start
        while (node != null && !isArcStart(node)) {
            node = nextInternalNode(node);
        }

        return node;

    }

    /** Check if an internal node corresponds to the start angle of an arc.
     * @param node internal node to check
     * @return true if the node corresponds to the start angle of an arc
     */
    private boolean isArcStart(final BSPTree<S1Point> node) {

        if ((Boolean) leafBefore(node).getAttribute()) {
            // it has an inside cell before it, it may end an arc but not start it
            return false;
        }

        if (!(Boolean) leafAfter(node).getAttribute()) {
            // it has an outside cell after it, it is a dummy cut away from real arcs
            return false;
        }

        // the cell has an outside before and an inside after it
        // it is the start of an arc
        return true;

    }

    /** Check if an internal node corresponds to the end angle of an arc.
     * @param node internal node to check
     * @return true if the node corresponds to the end angle of an arc
     */
    private boolean isArcEnd(final BSPTree<S1Point> node) {

        if (!(Boolean) leafBefore(node).getAttribute()) {
            // it has an outside cell before it, it may start an arc but not end it
            return false;
        }

        if ((Boolean) leafAfter(node).getAttribute()) {
            // it has an inside cell after it, it is a dummy cut in the middle of an arc
            return false;
        }

        // the cell has an inside before and an outside after it
        // it is the end of an arc
        return true;

    }

    /** Get the next internal node.
     * @param node current internal node
     * @return next internal node in trigonometric order, or null
     * if this is the last internal node
     */
    private BSPTree<S1Point> nextInternalNode(BSPTree<S1Point> node) {

        if (childAfter(node).getCut() != null) {
            // the next node is in the sub-tree
            return leafAfter(node).getParent();
        }

        // there is nothing left deeper in the tree, we backtrack
        while (isAfterParent(node)) {
            node = node.getParent();
        }
        return node.getParent();

    }

    /** Get the previous internal node.
     * @param node current internal node
     * @return previous internal node in trigonometric order, or null
     * if this is the first internal node
     */
    private BSPTree<S1Point> previousInternalNode(BSPTree<S1Point> node) {

        if (childBefore(node).getCut() != null) {
            // the next node is in the sub-tree
            return leafBefore(node).getParent();
        }

        // there is nothing left deeper in the tree, we backtrack
        while (isBeforeParent(node)) {
            node = node.getParent();
        }
        return node.getParent();

    }

    /** Find the leaf node just before an internal node.
     * @param node internal node at which the sub-tree starts
     * @return leaf node just before the internal node
     */
    private BSPTree<S1Point> leafBefore(BSPTree<S1Point> node) {

        node = childBefore(node);
        while (node.getCut() != null) {
            node = childAfter(node);
        }

        return node;

    }

    /** Find the leaf node just after an internal node.
     * @param node internal node at which the sub-tree starts
     * @return leaf node just after the internal node
     */
    private BSPTree<S1Point> leafAfter(BSPTree<S1Point> node) {

        node = childAfter(node);
        while (node.getCut() != null) {
            node = childBefore(node);
        }

        return node;

    }

    /** Check if a node is the child before its parent in trigonometric order.
     * @param node child node considered
     * @return true is the node has a parent end is before it in trigonometric order
     */
    private boolean isBeforeParent(final BSPTree<S1Point> node) {
        final BSPTree<S1Point> parent = node.getParent();
        if (parent == null) {
            return false;
        } else {
            return node == childBefore(parent);
        }
    }

    /** Check if a node is the child after its parent in trigonometric order.
     * @param node child node considered
     * @return true is the node has a parent end is after it in trigonometric order
     */
    private boolean isAfterParent(final BSPTree<S1Point> node) {
        final BSPTree<S1Point> parent = node.getParent();
        if (parent == null) {
            return false;
        } else {
            return node == childAfter(parent);
        }
    }

    /** Find the child node just before an internal node.
     * @param node internal node at which the sub-tree starts
     * @return child node just before the internal node
     */
    private BSPTree<S1Point> childBefore(BSPTree<S1Point> node) {
        if (isDirect(node)) {
            // smaller angles are on minus side, larger angles are on plus side
            return node.getMinus();
        } else {
            // smaller angles are on plus side, larger angles are on minus side
            return node.getPlus();
        }
    }

    /** Find the child node just after an internal node.
     * @param node internal node at which the sub-tree starts
     * @return child node just after the internal node
     */
    private BSPTree<S1Point> childAfter(BSPTree<S1Point> node) {
        if (isDirect(node)) {
            // smaller angles are on minus side, larger angles are on plus side
            return node.getPlus();
        } else {
            // smaller angles are on plus side, larger angles are on minus side
            return node.getMinus();
        }
    }

    /** Check if an internal node has a direct limit angle.
     * @param node internal node to check
     * @return true if the limit angle is direct
     */
    private boolean isDirect(final BSPTree<S1Point> node) {
        return ((LimitAngle) node.getCut().getHyperplane()).isDirect();
    }

    /** Get the limit angle of an internal node.
     * @param node internal node to check
     * @return limit angle
     */
    private double getAngle(final BSPTree<S1Point> node) {
        return ((LimitAngle) node.getCut().getHyperplane()).getLocation().getAlpha();
    }

    /** {@inheritDoc} */
    @Override
    public ArcsSet buildNew(final BSPTree<S1Point> tree) {
        return new ArcsSet(tree, getTolerance());
    }

    /** {@inheritDoc} */
    @Override
    protected void computeGeometricalProperties() {
        if (getTree(false).getCut() == null) {
            setBarycenter(S1Point.NaN);
            setSize(((Boolean) getTree(false).getAttribute()) ? Geometry.TWO_PI : 0);
        } else {
            double size = 0.0;
            double sum  = 0.0;
            for (final double[] a : this) {
                final double length = a[1] - a[0];
                size += length;
                sum  += length * (a[0] + a[1]);
            }
            setSize(size);
            if (Precision.equals(size, Geometry.TWO_PI, 0)) {
                setBarycenter(S1Point.NaN);
            } else if (size >= Precision.SAFE_MIN) {
                setBarycenter(S1Point.of(sum / (2 * size)));
            } else {
                final LimitAngle limit = (LimitAngle) getTree(false).getCut().getHyperplane();
                setBarycenter(limit.getLocation());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public BoundaryProjection<S1Point> projectToBoundary(final S1Point point) {

        // get position of test point
        final double alpha = point.getAlpha();

        boolean wrapFirst = false;
        double first      = Double.NaN;
        double previous   = Double.NaN;
        for (final double[] a : this) {

            if (Double.isNaN(first)) {
                // remember the first angle in case we need it later
                first = a[0];
            }

            if (!wrapFirst) {
                if (alpha < a[0]) {
                    // the test point lies between the previous and the current arcs
                    // offset will be positive
                    if (Double.isNaN(previous)) {
                        // we need to wrap around the circle
                        wrapFirst = true;
                    } else {
                        final double previousOffset = alpha - previous;
                        final double currentOffset  = a[0] - alpha;
                        if (previousOffset < currentOffset) {
                            return new BoundaryProjection<>(point, S1Point.of(previous), previousOffset);
                        } else {
                            return new BoundaryProjection<>(point, S1Point.of(a[0]), currentOffset);
                        }
                    }
                } else if (alpha <= a[1]) {
                    // the test point lies within the current arc
                    // offset will be negative
                    final double offset0 = a[0] - alpha;
                    final double offset1 = alpha - a[1];
                    if (offset0 < offset1) {
                        return new BoundaryProjection<>(point, S1Point.of(a[1]), offset1);
                    } else {
                        return new BoundaryProjection<>(point, S1Point.of(a[0]), offset0);
                    }
                }
            }
            previous = a[1];
        }

        if (Double.isNaN(previous)) {

            // there are no points at all in the arcs set
            return new BoundaryProjection<>(point, null, Geometry.TWO_PI);

        } else {

            // the test point if before first arc and after last arc,
            // somewhere around the 0/2 \pi crossing
            if (wrapFirst) {
                // the test point is between 0 and first
                final double previousOffset = alpha - (previous - Geometry.TWO_PI);
                final double currentOffset  = first - alpha;
                if (previousOffset < currentOffset) {
                    return new BoundaryProjection<>(point, S1Point.of(previous), previousOffset);
                } else {
                    return new BoundaryProjection<>(point, S1Point.of(first), currentOffset);
                }
            } else {
                // the test point is between last and 2\pi
                final double previousOffset = alpha - previous;
                final double currentOffset  = first + Geometry.TWO_PI - alpha;
                if (previousOffset < currentOffset) {
                    return new BoundaryProjection<>(point, S1Point.of(previous), previousOffset);
                } else {
                    return new BoundaryProjection<>(point, S1Point.of(first), currentOffset);
                }
            }

        }

    }

    /** Build an ordered list of arcs representing the instance.
     * <p>This method builds this arcs set as an ordered list of
     * {@link Arc Arc} elements. An empty tree will build an empty list
     * while a tree representing the whole circle will build a one
     * element list with bounds set to \( 0 and 2 \pi \).</p>
     * @return a new ordered list containing {@link Arc Arc} elements
     */
    public List<Arc> asList() {
        final List<Arc> list = new ArrayList<>();
        for (final double[] a : this) {
            list.add(new Arc(a[0], a[1], getTolerance()));
        }
        return list;
    }

    /** {@inheritDoc}
     * <p>
     * The iterator returns the limit angles pairs of sub-arcs in trigonometric order.
     * </p>
     * <p>
     * The iterator does <em>not</em> support the optional {@code remove} operation.
     * </p>
     */
    @Override
    public Iterator<double[]> iterator() {
        return new SubArcsIterator();
    }

    /** Local iterator for sub-arcs. */
    private class SubArcsIterator implements Iterator<double[]> {

        /** Start of the first arc. */
        private final BSPTree<S1Point> firstStart;

        /** Current node. */
        private BSPTree<S1Point> current;

        /** Sub-arc no yet returned. */
        private double[] pending;

        /** Simple constructor.
         */
        SubArcsIterator() {

            firstStart = getFirstArcStart();
            current    = firstStart;

            if (firstStart == null) {
                // all the leaf tree nodes share the same inside/outside status
                if ((Boolean) getFirstLeaf(getTree(false)).getAttribute()) {
                    // it is an inside node, it represents the full circle
                    pending = new double[] {
                        0, Geometry.TWO_PI
                    };
                } else {
                    pending = null;
                }
            } else {
                selectPending();
            }
        }

        /** Walk the tree to select the pending sub-arc.
         */
        private void selectPending() {

            // look for the start of the arc
            BSPTree<S1Point> start = current;
            while (start != null && !isArcStart(start)) {
                start = nextInternalNode(start);
            }

            if (start == null) {
                // we have exhausted the iterator
                current = null;
                pending = null;
                return;
            }

            // look for the end of the arc
            BSPTree<S1Point> end = start;
            while (end != null && !isArcEnd(end)) {
                end = nextInternalNode(end);
            }

            if (end != null) {

                // we have identified the arc
                pending = new double[] {
                    getAngle(start), getAngle(end)
                };

                // prepare search for next arc
                current = end;

            } else {

                // the final arc wraps around 2\pi, its end is before the first start
                end = firstStart;
                while (end != null && !isArcEnd(end)) {
                    end = previousInternalNode(end);
                }
                if (end == null) {
                    // this should never happen
                    throw new IllegalStateException(INTERNAL_ERROR_MESSAGE);
                }

                // we have identified the last arc
                pending = new double[] {
                    getAngle(start), getAngle(end) + Geometry.TWO_PI
                };

                // there won't be any other arcs
                current = null;

            }

        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return pending != null;
        }

        /** {@inheritDoc} */
        @Override
        public double[] next() {
            if (pending == null) {
                throw new NoSuchElementException();
            }
            final double[] next = pending;
            selectPending();
            return next;
        }

        /** {@inheritDoc} */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    /** Compute the relative position of the instance with respect
     * to an arc.
     * <p>
     * The {@link Side#MINUS} side of the arc is the one covered by the arc.
     * </p>
     * @param arc arc to check instance against
     * @return one of {@link Side#PLUS}, {@link Side#MINUS}, {@link Side#BOTH}
     * or {@link Side#HYPER}
     * @deprecated as of 3.6, replaced with {@link #split(Arc)}.{@link Split#getSide()}
     */
    @Deprecated
    public Side side(final Arc arc) {
        return split(arc).getSide();
    }

    /** Split the instance in two parts by an arc.
     * @param arc splitting arc
     * @return an object containing both the part of the instance
     * on the plus side of the arc and the part of the
     * instance on the minus side of the arc
     */
    public Split split(final Arc arc) {

        final List<Double> minus = new ArrayList<>();
        final List<Double>  plus = new ArrayList<>();

        final double reference = Geometry.PI + arc.getInf();
        final double arcLength = arc.getSup() - arc.getInf();

        for (final double[] a : this) {
            final double syncedStart = PlaneAngleRadians.normalize(a[0], reference) - arc.getInf();
            final double arcOffset   = a[0] - syncedStart;
            final double syncedEnd   = a[1] - arcOffset;
            if (syncedStart < arcLength) {
                // the start point a[0] is in the minus part of the arc
                minus.add(a[0]);
                if (syncedEnd > arcLength) {
                    // the end point a[1] is past the end of the arc
                    // so we leave the minus part and enter the plus part
                    final double minusToPlus = arcLength + arcOffset;
                    minus.add(minusToPlus);
                    plus.add(minusToPlus);
                    if (syncedEnd > Geometry.TWO_PI) {
                        // in fact the end point a[1] goes far enough that we
                        // leave the plus part of the arc and enter the minus part again
                        final double plusToMinus = Geometry.TWO_PI + arcOffset;
                        plus.add(plusToMinus);
                        minus.add(plusToMinus);
                        minus.add(a[1]);
                    } else {
                        // the end point a[1] is in the plus part of the arc
                        plus.add(a[1]);
                    }
                } else {
                    // the end point a[1] is in the minus part of the arc
                    minus.add(a[1]);
                }
            } else {
                // the start point a[0] is in the plus part of the arc
                plus.add(a[0]);
                if (syncedEnd > Geometry.TWO_PI) {
                    // the end point a[1] wraps around to the start of the arc
                    // so we leave the plus part and enter the minus part
                    final double plusToMinus = Geometry.TWO_PI + arcOffset;
                    plus.add(plusToMinus);
                    minus.add(plusToMinus);
                    if (syncedEnd > Geometry.TWO_PI + arcLength) {
                        // in fact the end point a[1] goes far enough that we
                        // leave the minus part of the arc and enter the plus part again
                        final double minusToPlus = Geometry.TWO_PI + arcLength + arcOffset;
                        minus.add(minusToPlus);
                        plus.add(minusToPlus);
                        plus.add(a[1]);
                    } else {
                        // the end point a[1] is in the minus part of the arc
                        minus.add(a[1]);
                    }
                } else {
                    // the end point a[1] is in the plus part of the arc
                    plus.add(a[1]);
                }
            }
        }

        return new Split(createSplitPart(plus), createSplitPart(minus));

    }

    /** Add an arc limit to a BSP tree under construction.
     * @param tree BSP tree under construction
     * @param alpha arc limit
     * @param isStart if true, the limit is the start of an arc
     */
    private void addArcLimit(final BSPTree<S1Point> tree, final double alpha, final boolean isStart) {

        final LimitAngle limit = new LimitAngle(S1Point.of(alpha), !isStart, getTolerance());
        final BSPTree<S1Point> node = tree.getCell(limit.getLocation(), getTolerance());
        if (node.getCut() != null) {
            // this should never happen
            throw new IllegalStateException(INTERNAL_ERROR_MESSAGE);
        }

        node.insertCut(limit);
        node.setAttribute(null);
        node.getPlus().setAttribute(Boolean.FALSE);
        node.getMinus().setAttribute(Boolean.TRUE);

    }

    /** Create a split part.
     * <p>
     * As per construction, the list of limit angles is known to have
     * an even number of entries, with start angles at even indices and
     * end angles at odd indices.
     * </p>
     * @param limits limit angles of the split part
     * @return split part (may be null)
     */
    private ArcsSet createSplitPart(final List<Double> limits) {
        if (limits.isEmpty()) {
            return null;
        } else {

            // collapse close limit angles
            for (int i = 0; i < limits.size(); ++i) {
                final int    j  = (i + 1) % limits.size();
                final double lA = limits.get(i);
                final double lB = PlaneAngleRadians.normalize(limits.get(j), lA);
                if (Math.abs(lB - lA) <= getTolerance()) {
                    // the two limits are too close to each other, we remove both of them
                    if (j > 0) {
                        // regular case, the two entries are consecutive ones
                        limits.remove(j);
                        limits.remove(i);
                        i = i - 1;
                    } else {
                        // special case, i the the last entry and j is the first entry
                        // we have wrapped around list end
                        final double lEnd   = limits.remove(limits.size() - 1);
                        final double lStart = limits.remove(0);
                        if (limits.isEmpty()) {
                            // the ends were the only limits, is it a full circle or an empty circle?
                            if (lEnd - lStart > Geometry.PI) {
                                // it was full circle
                                return new ArcsSet(new BSPTree<S1Point>(Boolean.TRUE), getTolerance());
                            } else {
                                // it was an empty circle
                                return null;
                            }
                        } else {
                            // we have removed the first interval start, so our list
                            // currently starts with an interval end, which is wrong
                            // we need to move this interval end to the end of the list
                            limits.add(limits.remove(0) + Geometry.TWO_PI);
                        }
                    }
                }
            }

            // build the tree by adding all angular sectors
            BSPTree<S1Point> tree = new BSPTree<>(Boolean.FALSE);
            for (int i = 0; i < limits.size() - 1; i += 2) {
                addArcLimit(tree, limits.get(i),     true);
                addArcLimit(tree, limits.get(i + 1), false);
            }

            if (tree.getCut() == null) {
                // we did not insert anything
                return null;
            }

            return new ArcsSet(tree, getTolerance());

        }
    }

    /** Class holding the results of the {@link #split split} method.
     */
    public static class Split {

        /** Part of the arcs set on the plus side of the splitting arc. */
        private final ArcsSet plus;

        /** Part of the arcs set on the minus side of the splitting arc. */
        private final ArcsSet minus;

        /** Build a Split from its parts.
         * @param plus part of the arcs set on the plus side of the
         * splitting arc
         * @param minus part of the arcs set on the minus side of the
         * splitting arc
         */
        private Split(final ArcsSet plus, final ArcsSet minus) {
            this.plus  = plus;
            this.minus = minus;
        }

        /** Get the part of the arcs set on the plus side of the splitting arc.
         * @return part of the arcs set on the plus side of the splitting arc
         */
        public ArcsSet getPlus() {
            return plus;
        }

        /** Get the part of the arcs set on the minus side of the splitting arc.
         * @return part of the arcs set on the minus side of the splitting arc
         */
        public ArcsSet getMinus() {
            return minus;
        }

        /** Get the side of the split arc with respect to its splitter.
         * @return {@link Side#PLUS} if only {@link #getPlus()} returns non-null,
         * {@link Side#MINUS} if only {@link #getMinus()} returns non-null,
         * {@link Side#BOTH} if both {@link #getPlus()} and {@link #getMinus()}
         * return non-null or {@link Side#HYPER} if both {@link #getPlus()} and
         * {@link #getMinus()} return null
         */
        public Side getSide() {
            if (plus != null) {
                if (minus != null) {
                    return Side.BOTH;
                } else {
                    return Side.PLUS;
                }
            } else if (minus != null) {
                return Side.MINUS;
            } else {
                return Side.HYPER;
            }
        }

    }

    /** Specialized exception for inconsistent BSP tree state inconsistency.
     * <p>
     * This exception is thrown at {@link ArcsSet} construction time when the
     * {@link org.apache.commons.geometry.partitioning.Region.Location inside/outside}
     * state is not consistent at the 0, \(2 \pi \) crossing.
     * </p>
     */
    public static class InconsistentStateAt2PiWrapping extends IllegalArgumentException {

        /** Serializable UID. */
        private static final long serialVersionUID = 20140107L;

        /** Simple constructor.
         */
        public InconsistentStateAt2PiWrapping() {
            super("Inconsistent state at 2\\u03c0 wrapping");
        }

    }

}
