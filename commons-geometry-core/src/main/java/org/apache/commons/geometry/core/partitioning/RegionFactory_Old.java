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
package org.apache.commons.geometry.core.partitioning;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.partitioning.BSPTree_Old.VanishingCutHandler;
import org.apache.commons.geometry.core.partitioning.Region_Old.Location;
import org.apache.commons.geometry.core.partitioning.SubHyperplane_Old.SplitSubHyperplane;

/** This class is a factory for {@link Region_Old}.

 * @param <P> Point type defining the space
 */
public class RegionFactory_Old<P extends Point<P>> {

    /** Visitor removing internal nodes attributes. */
    private final NodesCleaner nodeCleaner;

    /** Simple constructor.
     */
    public RegionFactory_Old() {
        nodeCleaner = new NodesCleaner();
    }

    /** Build a convex region from a collection of bounding hyperplanes.
     * @param hyperplanes collection of bounding hyperplanes
     * @return a new convex region, or null if the collection is empty
     */
    @SafeVarargs
    public final Region_Old<P> buildConvex(final Hyperplane_Old<P> ... hyperplanes) {
        if ((hyperplanes == null) || (hyperplanes.length == 0)) {
            return null;
        }

        // use the first hyperplane to build the right class
        final Region_Old<P> region = hyperplanes[0].wholeSpace();

        // chop off parts of the space
        BSPTree_Old<P> node = region.getTree(false);
        node.setAttribute(Boolean.TRUE);
        for (final Hyperplane_Old<P> hyperplane : hyperplanes) {
            if (node.insertCut(hyperplane)) {
                node.setAttribute(null);
                node.getPlus().setAttribute(Boolean.FALSE);
                node = node.getMinus();
                node.setAttribute(Boolean.TRUE);
            } else {
                // the hyperplane could not be inserted in the current leaf node
                // either it is completely outside (which means the input hyperplanes
                // are wrong), or it is parallel to a previous hyperplane
                SubHyperplane_Old<P> s = hyperplane.wholeHyperplane();
                for (BSPTree_Old<P> tree = node; tree.getParent() != null && s != null; tree = tree.getParent()) {
                    final Hyperplane_Old<P>         other = tree.getParent().getCut().getHyperplane();
                    final SplitSubHyperplane<P> split = s.split(other);
                    switch (split.getSide()) {
                        case HYPER :
                            // the hyperplane is parallel to a previous hyperplane
                            if (!hyperplane.sameOrientationAs(other)) {
                                // this hyperplane is opposite to the other one,
                                // the region is thinner than the tolerance, we consider it empty
                                return getComplement(hyperplanes[0].wholeSpace());
                            }
                            // the hyperplane is an extension of an already known hyperplane, we just ignore it
                            break;
                        case PLUS :
                            // the hyperplane is outside of the current convex zone,
                            // the input hyperplanes are inconsistent
                            throw new IllegalArgumentException("Hyperplanes do not define a convex region");
                        default :
                            s = split.getMinus();
                    }
                }
            }
        }

        return region;

    }

    /** Compute the union of two regions.
     * @param region1 first region (will be unusable after the operation as
     * parts of it will be reused in the new region)
     * @param region2 second region (will be unusable after the operation as
     * parts of it will be reused in the new region)
     * @return a new region, result of {@code region1 union region2}
     */
    public Region_Old<P> union(final Region_Old<P> region1, final Region_Old<P> region2) {
        final BSPTree_Old<P> tree =
            region1.getTree(false).merge(region2.getTree(false), new UnionMerger());
        tree.visit(nodeCleaner);
        return region1.buildNew(tree);
    }

    /** Compute the intersection of two regions.
     * @param region1 first region (will be unusable after the operation as
     * parts of it will be reused in the new region)
     * @param region2 second region (will be unusable after the operation as
     * parts of it will be reused in the new region)
     * @return a new region, result of {@code region1 intersection region2}
     */
    public Region_Old<P> intersection(final Region_Old<P> region1, final Region_Old<P> region2) {
        final BSPTree_Old<P> tree =
            region1.getTree(false).merge(region2.getTree(false), new IntersectionMerger());
        tree.visit(nodeCleaner);
        return region1.buildNew(tree);
    }

    /** Compute the symmetric difference (exclusive or) of two regions.
     * @param region1 first region (will be unusable after the operation as
     * parts of it will be reused in the new region)
     * @param region2 second region (will be unusable after the operation as
     * parts of it will be reused in the new region)
     * @return a new region, result of {@code region1 xor region2}
     */
    public Region_Old<P> xor(final Region_Old<P> region1, final Region_Old<P> region2) {
        final BSPTree_Old<P> tree =
            region1.getTree(false).merge(region2.getTree(false), new XorMerger());
        tree.visit(nodeCleaner);
        return region1.buildNew(tree);
    }

    /** Compute the difference of two regions.
     * @param region1 first region (will be unusable after the operation as
     * parts of it will be reused in the new region)
     * @param region2 second region (will be unusable after the operation as
     * parts of it will be reused in the new region)
     * @return a new region, result of {@code region1 minus region2}
     */
    public Region_Old<P> difference(final Region_Old<P> region1, final Region_Old<P> region2) {
        final BSPTree_Old<P> tree =
            region1.getTree(false).merge(region2.getTree(false), new DifferenceMerger(region1, region2));
        tree.visit(nodeCleaner);
        return region1.buildNew(tree);
    }

    /** Get the complement of the region (exchanged interior/exterior).
     * @param region region to complement, it will not modified, a new
     * region independent region will be built
     * @return a new region, complement of the specified one
     */
    /** Get the complement of the region (exchanged interior/exterior).
     * @param region region to complement, it will not modified, a new
     * region independent region will be built
     * @return a new region, complement of the specified one
     */
    public Region_Old<P> getComplement(final Region_Old<P> region) {
        return region.buildNew(recurseComplement(region.getTree(false)));
    }

    /** Recursively build the complement of a BSP tree.
     * @param node current node of the original tree
     * @return new tree, complement of the node
     */
    private BSPTree_Old<P> recurseComplement(final BSPTree_Old<P> node) {

        // transform the tree, except for boundary attribute splitters
        final Map<BSPTree_Old<P>, BSPTree_Old<P>> map = new HashMap<>();
        final BSPTree_Old<P> transformedTree = recurseComplement(node, map);

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

        return transformedTree;

    }

    /** Recursively build the complement of a BSP tree.
     * @param node current node of the original tree
     * @param map transformed nodes map
     * @return new tree, complement of the node
     */
    private BSPTree_Old<P> recurseComplement(final BSPTree_Old<P> node,
                                         final Map<BSPTree_Old<P>, BSPTree_Old<P>> map) {

        final BSPTree_Old<P> transformedNode;
        if (node.getCut() == null) {
            transformedNode = new BSPTree_Old<>(((Boolean) node.getAttribute()) ? Boolean.FALSE : Boolean.TRUE);
        } else {

            @SuppressWarnings("unchecked")
            BoundaryAttribute_Old<P> attribute = (BoundaryAttribute_Old<P>) node.getAttribute();
            if (attribute != null) {
                final SubHyperplane_Old<P> plusOutside =
                        (attribute.getPlusInside() == null) ? null : attribute.getPlusInside().copySelf();
                final SubHyperplane_Old<P> plusInside  =
                        (attribute.getPlusOutside() == null) ? null : attribute.getPlusOutside().copySelf();
                // we start with an empty list of splitters, it will be filled in out of recursion
                attribute = new BoundaryAttribute_Old<>(plusOutside, plusInside, new NodesSet_Old<P>());
            }

            transformedNode = new BSPTree_Old<>(node.getCut().copySelf(),
                                             recurseComplement(node.getPlus(),  map),
                                             recurseComplement(node.getMinus(), map),
                                             attribute);
        }

        map.put(node, transformedNode);
        return transformedNode;

    }

    /** BSP tree leaf merger computing union of two regions. */
    private class UnionMerger implements BSPTree_Old.LeafMerger<P> {
        /** {@inheritDoc} */
        @Override
        public BSPTree_Old<P> merge(final BSPTree_Old<P> leaf, final BSPTree_Old<P> tree,
                                final BSPTree_Old<P> parentTree,
                                final boolean isPlusChild, final boolean leafFromInstance) {
            if ((Boolean) leaf.getAttribute()) {
                // the leaf node represents an inside cell
                leaf.insertInTree(parentTree, isPlusChild, new VanishingToLeaf(true));
                return leaf;
            }
            // the leaf node represents an outside cell
            tree.insertInTree(parentTree, isPlusChild, new VanishingToLeaf(false));
            return tree;
        }
    }

    /** BSP tree leaf merger computing intersection of two regions. */
    private class IntersectionMerger implements BSPTree_Old.LeafMerger<P> {
        /** {@inheritDoc} */
        @Override
        public BSPTree_Old<P> merge(final BSPTree_Old<P> leaf, final BSPTree_Old<P> tree,
                                final BSPTree_Old<P> parentTree,
                                final boolean isPlusChild, final boolean leafFromInstance) {
            if ((Boolean) leaf.getAttribute()) {
                // the leaf node represents an inside cell
                tree.insertInTree(parentTree, isPlusChild, new VanishingToLeaf(true));
                return tree;
            }
            // the leaf node represents an outside cell
            leaf.insertInTree(parentTree, isPlusChild, new VanishingToLeaf(false));
            return leaf;
        }
    }

    /** BSP tree leaf merger computing symmetric difference (exclusive or) of two regions. */
    private class XorMerger implements BSPTree_Old.LeafMerger<P> {
        /** {@inheritDoc} */
        @Override
        public BSPTree_Old<P> merge(final BSPTree_Old<P> leaf, final BSPTree_Old<P> tree,
                                final BSPTree_Old<P> parentTree, final boolean isPlusChild,
                                final boolean leafFromInstance) {
            BSPTree_Old<P> t = tree;
            if ((Boolean) leaf.getAttribute()) {
                // the leaf node represents an inside cell
                t = recurseComplement(t);
            }
            t.insertInTree(parentTree, isPlusChild, new VanishingToLeaf(true));
            return t;
        }
    }

    /** BSP tree leaf merger computing difference of two regions. */
    private class DifferenceMerger implements BSPTree_Old.LeafMerger<P>, VanishingCutHandler<P> {

        /** Region to subtract from. */
        private final Region_Old<P> region1;

        /** Region to subtract. */
        private final Region_Old<P> region2;

        /** Simple constructor.
         * @param region1 region to subtract from
         * @param region2 region to subtract
         */
        DifferenceMerger(final Region_Old<P> region1, final Region_Old<P> region2) {
            this.region1 = region1.copySelf();
            this.region2 = region2.copySelf();
        }

        /** {@inheritDoc} */
        @Override
        public BSPTree_Old<P> merge(final BSPTree_Old<P> leaf, final BSPTree_Old<P> tree,
                                final BSPTree_Old<P> parentTree, final boolean isPlusChild,
                                final boolean leafFromInstance) {
            if ((Boolean) leaf.getAttribute()) {
                // the leaf node represents an inside cell
                final BSPTree_Old<P> argTree =
                    recurseComplement(leafFromInstance ? tree : leaf);
                argTree.insertInTree(parentTree, isPlusChild, this);
                return argTree;
            }
            // the leaf node represents an outside cell
            final BSPTree_Old<P> instanceTree =
                leafFromInstance ? leaf : tree;
            instanceTree.insertInTree(parentTree, isPlusChild, this);
            return instanceTree;
        }

        /** {@inheritDoc} */
        @Override
        public BSPTree_Old<P> fixNode(final BSPTree_Old<P> node) {
            // get a representative point in the degenerate cell
            final BSPTree_Old<P> cell = node.pruneAroundConvexCell(Boolean.TRUE, Boolean.FALSE, null);
            final Region_Old<P> r = region1.buildNew(cell);
            final P p = r.getBarycenter();
            return new BSPTree_Old<>(region1.checkPoint(p) == Location.INSIDE &&
                                  region2.checkPoint(p) == Location.OUTSIDE);
        }

    }

    /** Visitor removing internal nodes attributes. */
    private class NodesCleaner implements  BSPTreeVisitor_Old<P> {

        /** {@inheritDoc} */
        @Override
        public Order visitOrder(final BSPTree_Old<P> node) {
            return Order.PLUS_SUB_MINUS;
        }

        /** {@inheritDoc} */
        @Override
        public void visitInternalNode(final BSPTree_Old<P> node) {
            node.setAttribute(null);
        }

        /** {@inheritDoc} */
        @Override
        public void visitLeafNode(final BSPTree_Old<P> node) {
        }

    }

    /** Handler replacing nodes with vanishing cuts with leaf nodes. */
    private class VanishingToLeaf implements VanishingCutHandler<P> {

        /** Inside/outside indocator to use for ambiguous nodes. */
        private final boolean inside;

        /** Simple constructor.
         * @param inside inside/outside indicator to use for ambiguous nodes
         */
        VanishingToLeaf(final boolean inside) {
            this.inside = inside;
        }

        /** {@inheritDoc} */
        @Override
        public BSPTree_Old<P> fixNode(final BSPTree_Old<P> node) {
            if (node.getPlus().getAttribute().equals(node.getMinus().getAttribute())) {
                // no ambiguity
                return new BSPTree_Old<>(node.getPlus().getAttribute());
            } else {
                // ambiguous node
                return new BSPTree_Old<>(inside);
            }
        }

    }

}
