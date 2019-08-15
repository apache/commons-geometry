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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.geometry.core.Point;

/** This class implements the dimension-independent parts of {@link SubHyperplane_Old}.

 * <p>sub-hyperplanes are obtained when parts of an {@link
 * Hyperplane_Old hyperplane} are chopped off by other hyperplanes that
 * intersect it. The remaining part is a convex region. Such objects
 * appear in {@link BSPTree_Old BSP trees} as the intersection of a cut
 * hyperplane with the convex region which it splits, the chopping
 * hyperplanes are the cut hyperplanes closer to the tree root.</p>

 * @param <P> Point type defining the space
 * @param <S> Point type defining the sub-space
 */
public abstract class AbstractSubHyperplane_Old<P extends Point<P>, S extends Point<S>>
    implements SubHyperplane_Old<P> {

    /** Underlying hyperplane. */
    private final Hyperplane_Old<P> hyperplane;

    /** Remaining region of the hyperplane. */
    private final Region_Old<S> remainingRegion;

    /** Build a sub-hyperplane from an hyperplane and a region.
     * @param hyperplane underlying hyperplane
     * @param remainingRegion remaining region of the hyperplane
     */
    protected AbstractSubHyperplane_Old(final Hyperplane_Old<P> hyperplane,
                                    final Region_Old<S> remainingRegion) {
        this.hyperplane      = hyperplane;
        this.remainingRegion = remainingRegion;
    }

    /** Build a sub-hyperplane from an hyperplane and a region.
     * @param hyper underlying hyperplane
     * @param remaining remaining region of the hyperplane
     * @return a new sub-hyperplane
     */
    protected abstract AbstractSubHyperplane_Old<P, S> buildNew(final Hyperplane_Old<P> hyper,
                                                            final Region_Old<S> remaining);

    /** {@inheritDoc} */
    @Override
    public AbstractSubHyperplane_Old<P, S> copySelf() {
        return buildNew(hyperplane.copySelf(), remainingRegion);
    }

    /** Get the underlying hyperplane.
     * @return underlying hyperplane
     */
    @Override
    public Hyperplane_Old<P> getHyperplane() {
        return hyperplane;
    }

    /** Get the remaining region of the hyperplane.
     * <p>The returned region is expressed in the canonical hyperplane
     * frame and has the hyperplane dimension. For example a chopped
     * hyperplane in the 3D Euclidean is a 2D plane and the
     * corresponding region is a convex 2D polygon.</p>
     * @return remaining region of the hyperplane
     */
    public Region_Old<S> getRemainingRegion() {
        return remainingRegion;
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        return remainingRegion.getSize();
    }

    /** {@inheritDoc} */
    @Override
    public AbstractSubHyperplane_Old<P, S> reunite(final SubHyperplane_Old<P> other) {
        @SuppressWarnings("unchecked")
        AbstractSubHyperplane_Old<P, S> o = (AbstractSubHyperplane_Old<P, S>) other;
        return buildNew(hyperplane,
                        new RegionFactory_Old<S>().union(remainingRegion, o.remainingRegion));
    }

    /** Apply a transform to the instance.
     * <p>The instance must be a (D-1)-dimension sub-hyperplane with
     * respect to the transform <em>not</em> a (D-2)-dimension
     * sub-hyperplane the transform knows how to transform by
     * itself. The transform will consist in transforming first the
     * hyperplane and then the all region using the various methods
     * provided by the transform.</p>
     * @param transform D-dimension transform to apply
     * @return the transformed instance
     */
    public AbstractSubHyperplane_Old<P, S> applyTransform(final Transform_Old<P, S> transform) {
        final Hyperplane_Old<P> tHyperplane = transform.apply(hyperplane);

        // transform the tree, except for boundary attribute splitters
        final Map<BSPTree_Old<S>, BSPTree_Old<S>> map = new HashMap<>();
        final BSPTree_Old<S> tTree =
            recurseTransform(remainingRegion.getTree(false), tHyperplane, transform, map);

        // set up the boundary attributes splitters
        for (final Map.Entry<BSPTree_Old<S>, BSPTree_Old<S>> entry : map.entrySet()) {
            if (entry.getKey().getCut() != null) {
                @SuppressWarnings("unchecked")
                BoundaryAttribute_Old<S> original = (BoundaryAttribute_Old<S>) entry.getKey().getAttribute();
                if (original != null) {
                    @SuppressWarnings("unchecked")
                    BoundaryAttribute_Old<S> transformed = (BoundaryAttribute_Old<S>) entry.getValue().getAttribute();
                    for (final BSPTree_Old<S> splitter : original.getSplitters()) {
                        transformed.getSplitters().add(map.get(splitter));
                    }
                }
            }
        }

        return buildNew(tHyperplane, remainingRegion.buildNew(tTree));

    }

    /** Recursively transform a BSP-tree from a sub-hyperplane.
     * @param node current BSP tree node
     * @param transformed image of the instance hyperplane by the transform
     * @param transform transform to apply
     * @param map transformed nodes map
     * @return a new tree
     */
    private BSPTree_Old<S> recurseTransform(final BSPTree_Old<S> node,
                                        final Hyperplane_Old<P> transformed,
                                        final Transform_Old<P, S> transform,
                                        final Map<BSPTree_Old<S>, BSPTree_Old<S>> map) {

        final BSPTree_Old<S> transformedNode;
        if (node.getCut() == null) {
            transformedNode = new BSPTree_Old<>(node.getAttribute());
        } else {

            @SuppressWarnings("unchecked")
            BoundaryAttribute_Old<S> attribute = (BoundaryAttribute_Old<S>) node.getAttribute();
            if (attribute != null) {
                final SubHyperplane_Old<S> tPO = (attribute.getPlusOutside() == null) ?
                    null : transform.apply(attribute.getPlusOutside(), hyperplane, transformed);
                final SubHyperplane_Old<S> tPI = (attribute.getPlusInside() == null) ?
                    null : transform.apply(attribute.getPlusInside(), hyperplane, transformed);
                // we start with an empty list of splitters, it will be filled in out of recursion
                attribute = new BoundaryAttribute_Old<>(tPO, tPI, new NodesSet_Old<S>());
            }

            transformedNode = new BSPTree_Old<>(transform.apply(node.getCut(), hyperplane, transformed),
                    recurseTransform(node.getPlus(),  transformed, transform, map),
                    recurseTransform(node.getMinus(), transformed, transform, map),
                    attribute);
        }

        map.put(node, transformedNode);
        return transformedNode;

    }

    /** {@inheritDoc} */
    @Override
    public abstract SplitSubHyperplane<P> split(Hyperplane_Old<P> hyper);

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return remainingRegion.isEmpty();
    }

}
