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

import org.apache.commons.geometry.core.Point;

/** Visitor computing the boundary size.
 * @param <P> Point type defining the space
 */
class BoundarySizeVisitor<P extends Point<P>> implements BSPTreeVisitor<P> {

    /** Size of the boundary. */
    private double boundarySize;

    /** Simple constructor.
     */
    BoundarySizeVisitor() {
        boundarySize = 0;
    }

    /** {@inheritDoc}*/
    @Override
    public Order visitOrder(final BSPTree<P> node) {
        return Order.MINUS_SUB_PLUS;
    }

    /** {@inheritDoc}*/
    @Override
    public void visitInternalNode(final BSPTree<P> node) {
        @SuppressWarnings("unchecked")
        final BoundaryAttribute<P> attribute =
            (BoundaryAttribute<P>) node.getAttribute();
        if (attribute.getPlusOutside() != null) {
            boundarySize += attribute.getPlusOutside().getSize();
        }
        if (attribute.getPlusInside() != null) {
            boundarySize += attribute.getPlusInside().getSize();
        }
    }

    /** {@inheritDoc}*/
    @Override
    public void visitLeafNode(final BSPTree<P> node) {
    }

    /** Get the size of the boundary.
     * @return size of the boundary
     */
    public double getSize() {
        return boundarySize;
    }

}
