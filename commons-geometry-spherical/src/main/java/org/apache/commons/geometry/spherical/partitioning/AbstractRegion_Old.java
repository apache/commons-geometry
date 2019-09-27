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
package org.apache.commons.geometry.spherical.partitioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** Abstract class for all regions, independent of geometry type or dimension.

 * @param <P> Point type defining the space
 * @param <S> Point type defining the sub-space
 */
public abstract class AbstractRegion_Old<P extends Point<P>, S extends Point<S>> implements Region_Old<P> {

    /** Inside/Outside BSP tree. */
    private BSPTree_Old<P> tree;

    /** Precision context used to determine floating point equality. */
    private final DoublePrecisionContext precision;

    /** Size of the instance. */
    private double size;

    /** Barycenter. */
    private P barycenter;

    /** Build a region representing the whole space.
     * @param precision precision context used to compare floating point numbers
     */
    protected AbstractRegion_Old(final DoublePrecisionContext precision) {
        this.tree      = new BSPTree_Old<>(Boolean.TRUE);
        this.precision = precision;
    }

    /** Build a region from an inside/outside BSP tree.
     * <p>The leaf nodes of the BSP tree <em>must</em> have a
     * {@code Boolean} attribute representing the inside status of
     * the corresponding cell (true for inside cells, false for outside
     * cells). In order to avoid building too many small objects, it is
     * recommended to use the predefined constants
     * {@code Boolean.TRUE} and {@code Boolean.FALSE}. The
     * tree also <em>must</em> have either null internal nodes or
     * internal nodes representing the boundary as specified in the
     * {@link #getTree getTree} method).</p>
     * @param tree inside/outside BSP tree representing the region
     * @param precision precision context used to compare floating point values
     */
    protected AbstractRegion_Old(final BSPTree_Old<P> tree, final DoublePrecisionContext precision) {
        this.tree      = tree;
        this.precision = precision;
    }

    /** Build a Region from a Boundary REPresentation (B-rep).
     * <p>The boundary is provided as a collection of {@link
     * SubHyperplane_Old sub-hyperplanes}. Each sub-hyperplane has the
     * interior part of the region on its minus side and the exterior on
     * its plus side.</p>
     * <p>The boundary elements can be in any order, and can form
     * several non-connected sets (like for example polygons with holes
     * or a set of disjoints polyhedrons considered as a whole). In
     * fact, the elements do not even need to be connected together
     * (their topological connections are not used here). However, if the
     * boundary does not really separate an inside open from an outside
     * open (open having here its topological meaning), then subsequent
     * calls to the {@link #checkPoint(Point) checkPoint} method will not be
     * meaningful anymore.</p>
     * <p>If the boundary is empty, the region will represent the whole
     * space.</p>
     * @param boundary collection of boundary elements, as a
     * collection of {@link SubHyperplane_Old SubHyperplane} objects
     * @param precision precision context used to compare floating point values
     */
    protected AbstractRegion_Old(final Collection<SubHyperplane_Old<P>> boundary, final DoublePrecisionContext precision) {

        this.precision = precision;

        if (boundary.size() == 0) {

            // the tree represents the whole space
            tree = new BSPTree_Old<>(Boolean.TRUE);

        } else {

            // sort the boundary elements in decreasing size order
            // (we don't want equal size elements to be removed, so
            // we use a trick to fool the TreeSet)
            final TreeSet<SubHyperplane_Old<P>> ordered = new TreeSet<>(new Comparator<SubHyperplane_Old<P>>() {
                /** {@inheritDoc} */
                @Override
                public int compare(final SubHyperplane_Old<P> o1, final SubHyperplane_Old<P> o2) {
                    final double size1 = o1.getSize();
                    final double size2 = o2.getSize();
                    return (size2 < size1) ? -1 : ((o1 == o2) ? 0 : +1);
                }
            });
            ordered.addAll(boundary);

            // build the tree top-down
            tree = new BSPTree_Old<>();
            insertCuts(tree, ordered);

            // set up the inside/outside flags
            tree.visit(new BSPTreeVisitor_Old<P>() {

                /** {@inheritDoc} */
                @Override
                public Order visitOrder(final BSPTree_Old<P> node) {
                    return Order.PLUS_SUB_MINUS;
                }

                /** {@inheritDoc} */
                @Override
                public void visitInternalNode(final BSPTree_Old<P> node) {
                }

                /** {@inheritDoc} */
                @Override
                public void visitLeafNode(final BSPTree_Old<P> node) {
                    if (node.getParent() == null || node == node.getParent().getMinus()) {
                        node.setAttribute(Boolean.TRUE);
                    } else {
                        node.setAttribute(Boolean.FALSE);
                    }
                }
            });

        }

    }

    /** Build a convex region from an array of bounding hyperplanes.
     * @param hyperplanes array of bounding hyperplanes (if null, an
     * empty region will be built)
     * @param precision precision context used to compare floating point values
     */
    public AbstractRegion_Old(final Hyperplane_Old<P>[] hyperplanes, final DoublePrecisionContext precision) {
        this.precision = precision;
        if ((hyperplanes == null) || (hyperplanes.length == 0)) {
            tree = new BSPTree_Old<>(Boolean.FALSE);
        } else {

            // use the first hyperplane to build the right class
            tree = hyperplanes[0].wholeSpace().getTree(false);

            // chop off parts of the space
            BSPTree_Old<P> node = tree;
            node.setAttribute(Boolean.TRUE);
            for (final Hyperplane_Old<P> hyperplane : hyperplanes) {
                if (node.insertCut(hyperplane)) {
                    node.setAttribute(null);
                    node.getPlus().setAttribute(Boolean.FALSE);
                    node = node.getMinus();
                    node.setAttribute(Boolean.TRUE);
                }
            }

        }

    }

    /** {@inheritDoc} */
    @Override
    public abstract AbstractRegion_Old<P, S> buildNew(BSPTree_Old<P> newTree);

    /** Get the object used to determine floating point equality for this region.
     * @return the floating point precision context for the instance
     */
    public DoublePrecisionContext getPrecision() {
        return precision;
    }

    /** Recursively build a tree by inserting cut sub-hyperplanes.
     * @param node current tree node (it is a leaf node at the beginning
     * of the call)
     * @param boundary collection of edges belonging to the cell defined
     * by the node
     */
    private void insertCuts(final BSPTree_Old<P> node, final Collection<SubHyperplane_Old<P>> boundary) {

        final Iterator<SubHyperplane_Old<P>> iterator = boundary.iterator();

        // build the current level
        Hyperplane_Old<P> inserted = null;
        while ((inserted == null) && iterator.hasNext()) {
            inserted = iterator.next().getHyperplane();
            if (!node.insertCut(inserted.copySelf())) {
                inserted = null;
            }
        }

        if (!iterator.hasNext()) {
            return;
        }

        // distribute the remaining edges in the two sub-trees
        final ArrayList<SubHyperplane_Old<P>> plusList  = new ArrayList<>();
        final ArrayList<SubHyperplane_Old<P>> minusList = new ArrayList<>();
        while (iterator.hasNext()) {
            final SubHyperplane_Old<P> other = iterator.next();
            final SubHyperplane_Old.SplitSubHyperplane<P> split = other.split(inserted);
            switch (split.getSide()) {
            case PLUS:
                plusList.add(other);
                break;
            case MINUS:
                minusList.add(other);
                break;
            case BOTH:
                plusList.add(split.getPlus());
                minusList.add(split.getMinus());
                break;
            default:
                // ignore the sub-hyperplanes belonging to the cut hyperplane
            }
        }

        // recurse through lower levels
        insertCuts(node.getPlus(),  plusList);
        insertCuts(node.getMinus(), minusList);

    }

    /** {@inheritDoc} */
    @Override
    public AbstractRegion_Old<P, S> copySelf() {
        return buildNew(tree.copySelf());
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return isEmpty(tree);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty(final BSPTree_Old<P> node) {

        // we use a recursive function rather than the BSPTreeVisitor
        // interface because we can stop visiting the tree as soon as we
        // have found an inside cell

        if (node.getCut() == null) {
            // if we find an inside node, the region is not empty
            return !((Boolean) node.getAttribute());
        }

        // check both sides of the sub-tree
        return isEmpty(node.getMinus()) && isEmpty(node.getPlus());

    }

    /** {@inheritDoc} */
    @Override
    public boolean isFull() {
        return isFull(tree);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFull(final BSPTree_Old<P> node) {

        // we use a recursive function rather than the BSPTreeVisitor
        // interface because we can stop visiting the tree as soon as we
        // have found an outside cell

        if (node.getCut() == null) {
            // if we find an outside node, the region does not cover full space
            return (Boolean) node.getAttribute();
        }

        // check both sides of the sub-tree
        return isFull(node.getMinus()) && isFull(node.getPlus());

    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(final Region_Old<P> region) {
        return new RegionFactory_Old<P>().difference(region, this).isEmpty();
    }

    /** {@inheritDoc}
     */
    @Override
    public BoundaryProjection_Old<P> projectToBoundary(final P point) {
        final BoundaryProjector_Old<P, S> projector = new BoundaryProjector_Old<>(point);
        getTree(true).visit(projector);
        return projector.getProjection();
    }

    /** {@inheritDoc} */
    @Override
    public Location checkPoint(final P point) {
        return checkPoint(tree, point);
    }

    /** Check a point with respect to the region starting at a given node.
     * @param node root node of the region
     * @param point point to check
     * @return a code representing the point status: either {@link
     * Region_Old.Location#INSIDE INSIDE}, {@link Region_Old.Location#OUTSIDE
     * OUTSIDE} or {@link Region_Old.Location#BOUNDARY BOUNDARY}
     */
    protected Location checkPoint(final BSPTree_Old<P> node, final P point) {
        final BSPTree_Old<P> cell = node.getCell(point, precision);
        if (cell.getCut() == null) {
            // the point is in the interior of a cell, just check the attribute
            return ((Boolean) cell.getAttribute()) ? Location.INSIDE : Location.OUTSIDE;
        }

        // the point is on a cut-sub-hyperplane, is it on a boundary ?
        final Location minusCode = checkPoint(cell.getMinus(), point);
        final Location plusCode  = checkPoint(cell.getPlus(),  point);
        return (minusCode == plusCode) ? minusCode : Location.BOUNDARY;

    }

    /** {@inheritDoc} */
    @Override
    public BSPTree_Old<P> getTree(final boolean includeBoundaryAttributes) {
        if (includeBoundaryAttributes && (tree.getCut() != null) && (tree.getAttribute() == null)) {
            // compute the boundary attributes
            tree.visit(new BoundaryBuilder_Old<P>());
        }
        return tree;
    }

    /** {@inheritDoc} */
    @Override
    public double getBoundarySize() {
        final BoundarySizeVisitor_Old<P> visitor = new BoundarySizeVisitor_Old<>();
        getTree(true).visit(visitor);
        return visitor.getSize();
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        if (barycenter == null) {
            computeGeometricalProperties();
        }
        return size;
    }

    /** Set the size of the instance.
     * @param size size of the instance
     */
    protected void setSize(final double size) {
        this.size = size;
    }

    /** {@inheritDoc} */
    @Override
    public P getBarycenter() {
        if (barycenter == null) {
            computeGeometricalProperties();
        }
        return barycenter;
    }

    /** Set the barycenter of the instance.
     * @param barycenter barycenter of the instance
     */
    protected void setBarycenter(final P barycenter) {
        this.barycenter = barycenter;
    }

    /** Compute some geometrical properties.
     * <p>The properties to compute are the barycenter and the size.</p>
     */
    protected abstract void computeGeometricalProperties();

    /** {@inheritDoc} */
    @Override
    public SubHyperplane_Old<P> intersection(final SubHyperplane_Old<P> sub) {
        return recurseIntersection(tree, sub);
    }

    /** Recursively compute the parts of a sub-hyperplane that are
     * contained in the region.
     * @param node current BSP tree node
     * @param sub sub-hyperplane traversing the region
     * @return filtered sub-hyperplane
     */
    private SubHyperplane_Old<P> recurseIntersection(final BSPTree_Old<P> node, final SubHyperplane_Old<P> sub) {

        if (node.getCut() == null) {
            return (Boolean) node.getAttribute() ? sub.copySelf() : null;
        }

        final Hyperplane_Old<P> hyperplane = node.getCut().getHyperplane();
        final SubHyperplane_Old.SplitSubHyperplane<P> split = sub.split(hyperplane);
        if (split.getPlus() != null) {
            if (split.getMinus() != null) {
                // both sides
                final SubHyperplane_Old<P> plus  = recurseIntersection(node.getPlus(),  split.getPlus());
                final SubHyperplane_Old<P> minus = recurseIntersection(node.getMinus(), split.getMinus());
                if (plus == null) {
                    return minus;
                } else if (minus == null) {
                    return plus;
                } else {
                    return plus.reunite(minus);
                }
            } else {
                // only on plus side
                return recurseIntersection(node.getPlus(), sub);
            }
        } else if (split.getMinus() != null) {
            // only on minus side
            return recurseIntersection(node.getMinus(), sub);
        } else {
            // on hyperplane
            return recurseIntersection(node.getPlus(),
                                       recurseIntersection(node.getMinus(), sub));
        }

    }

    /** Transform a region.
     * <p>Applying a transform to a region consist in applying the
     * transform to all the hyperplanes of the underlying BSP tree and
     * of the boundary (and also to the sub-hyperplanes embedded in
     * these hyperplanes) and to the barycenter. The instance is not
     * modified, a new instance is built.</p>
     * @param transform transform to apply
     * @return a new region, resulting from the application of the
     * transform to the instance
     */
    public AbstractRegion_Old<P, S> applyTransform(final Transform_Old<P, S> transform) {

        // transform the tree, except for boundary attribute splitters
        final Map<BSPTree_Old<P>, BSPTree_Old<P>> map = new HashMap<>();
        final BSPTree_Old<P> transformedTree = recurseTransform(getTree(false), transform, map);

        // set up the boundary attributes splitters
        for (final Map.Entry<BSPTree_Old<P>, BSPTree_Old<P>> entry : map.entrySet()) {
            if (entry.getKey().getCut() != null) {
                @SuppressWarnings("unchecked")
                BoundaryAttribute_Old<P> original = (BoundaryAttribute_Old<P>) entry.getKey().getAttribute();
                if (original != null) {
                    @SuppressWarnings("unchecked")
                    BoundaryAttribute_Old<P> transformed = (BoundaryAttribute_Old<P>) entry.getValue().getAttribute();
                    for (final BSPTree_Old<P> splitter : original.getSplitters()) {
                        transformed.getSplitters().add(map.get(splitter));
                    }
                }
            }
        }

        return buildNew(transformedTree);

    }

    /** Recursively transform an inside/outside BSP-tree.
     * @param node current BSP tree node
     * @param transform transform to apply
     * @param map transformed nodes map
     * @return a new tree
     */
    @SuppressWarnings("unchecked")
    private BSPTree_Old<P> recurseTransform(final BSPTree_Old<P> node, final Transform_Old<P, S> transform,
                                        final Map<BSPTree_Old<P>, BSPTree_Old<P>> map) {

        final BSPTree_Old<P> transformedNode;
        if (node.getCut() == null) {
            transformedNode = new BSPTree_Old<>(node.getAttribute());
        } else {

            final SubHyperplane_Old<P>  sub = node.getCut();
            final SubHyperplane_Old<P> tSub = ((AbstractSubHyperplane_Old<P, S>) sub).applyTransform(transform);
            BoundaryAttribute_Old<P> attribute = (BoundaryAttribute_Old<P>) node.getAttribute();
            if (attribute != null) {
                final SubHyperplane_Old<P> tPO = (attribute.getPlusOutside() == null) ?
                    null : ((AbstractSubHyperplane_Old<P, S>) attribute.getPlusOutside()).applyTransform(transform);
                final SubHyperplane_Old<P> tPI = (attribute.getPlusInside()  == null) ?
                    null  : ((AbstractSubHyperplane_Old<P, S>) attribute.getPlusInside()).applyTransform(transform);
                // we start with an empty list of splitters, it will be filled in out of recursion
                attribute = new BoundaryAttribute_Old<>(tPO, tPI, new NodesSet_Old<P>());
            }

            transformedNode = new BSPTree_Old<>(tSub,
                                             recurseTransform(node.getPlus(),  transform, map),
                                             recurseTransform(node.getMinus(), transform, map),
                                             attribute);
        }

        map.put(node, transformedNode);
        return transformedNode;

    }

}