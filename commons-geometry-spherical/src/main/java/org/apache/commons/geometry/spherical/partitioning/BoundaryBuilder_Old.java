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

import org.apache.commons.geometry.core.Point;

/** Visitor building boundary shell tree.
 *
 * <p>
 * The boundary shell is represented as {@link BoundaryAttribute_Old boundary attributes}
 * at each internal node.
 * </p>
 *
 * @param <P> Point type defining the space.
 */
class BoundaryBuilder_Old<P extends Point<P>> implements BSPTreeVisitor_Old<P> {

    /** {@inheritDoc} */
    @Override
    public Order visitOrder(BSPTree_Old<P> node) {
        return Order.PLUS_MINUS_SUB;
    }

    /** {@inheritDoc} */
    @Override
    public void visitInternalNode(BSPTree_Old<P> node) {

        SubHyperplane_Old<P> plusOutside = null;
        SubHyperplane_Old<P> plusInside  = null;
        NodesSet_Old<P>      splitters   = null;

        // characterize the cut sub-hyperplane,
        // first with respect to the plus sub-tree
        final Characterization_Old<P> plusChar = new Characterization_Old<>(node.getPlus(), node.getCut().copySelf());

        if (plusChar.touchOutside()) {
            // plusChar.outsideTouching() corresponds to a subset of the cut sub-hyperplane
            // known to have outside cells on its plus side, we want to check if parts
            // of this subset do have inside cells on their minus side
            final Characterization_Old<P> minusChar = new Characterization_Old<>(node.getMinus(), plusChar.outsideTouching());
            if (minusChar.touchInside()) {
                // this part belongs to the boundary,
                // it has the outside on its plus side and the inside on its minus side
                plusOutside = minusChar.insideTouching();
                splitters = new NodesSet_Old<>();
                splitters.addAll(minusChar.getInsideSplitters());
                splitters.addAll(plusChar.getOutsideSplitters());
            }
        }

        if (plusChar.touchInside()) {
            // plusChar.insideTouching() corresponds to a subset of the cut sub-hyperplane
            // known to have inside cells on its plus side, we want to check if parts
            // of this subset do have outside cells on their minus side
            final Characterization_Old<P> minusChar = new Characterization_Old<>(node.getMinus(), plusChar.insideTouching());
            if (minusChar.touchOutside()) {
                // this part belongs to the boundary,
                // it has the inside on its plus side and the outside on its minus side
                plusInside = minusChar.outsideTouching();
                if (splitters == null) {
                    splitters = new NodesSet_Old<>();
                }
                splitters.addAll(minusChar.getOutsideSplitters());
                splitters.addAll(plusChar.getInsideSplitters());
            }
        }

        if (splitters != null) {
            // the parent nodes are natural splitters for boundary sub-hyperplanes
            for (BSPTree_Old<P> up = node.getParent(); up != null; up = up.getParent()) {
                splitters.add(up);
            }
        }

        // set the boundary attribute at non-leaf nodes
        node.setAttribute(new BoundaryAttribute_Old<>(plusOutside, plusInside, splitters));

    }

    /** {@inheritDoc} */
    @Override
    public void visitLeafNode(BSPTree_Old<P> node) {
    }

}
