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
package org.apache.commons.geometry.euclidean.threed;

import java.util.Objects;

import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.HyperplaneSubset;
import org.apache.commons.geometry.euclidean.threed.line.Line3D;
import org.apache.commons.geometry.euclidean.threed.line.LineConvexSubset3D;

/** Abstract base class for {@link PlaneSubset} implementations.
 */
abstract class AbstractPlaneSubset implements PlaneSubset {

    /** {@inheritDoc} */
    @Override
    public Plane getHyperplane() {
        return getPlane();
    }

    /** {@inheritDoc} */
    @Override
    public HyperplaneSubset.Builder<Vector3D> builder() {
        return new Builder(getPlane());
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D intersection(final Line3D line) {
        return Planes.intersection(this, line);
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D intersection(LineConvexSubset3D lineSubset) {
        return Planes.intersection(this, lineSubset);
    }

    /** Internal implementation of the {@link HyperplaneSubset.Builder} interface. In cases where only a single
     * convex subset is given to the builder, this class returns the convex subset instance directly. In all other
     * cases, an {@link EmbeddedTreePlaneSubset} is used to construct the final subset.
     */
    private static final class Builder implements HyperplaneSubset.Builder<Vector3D> {
        /** Plane that a subset is being constructed for. */
        private final Plane plane;

        /** Embedded tree subset. */
        private EmbeddedTreePlaneSubset treeSubset;

        /** Convex subset added as the first subset to the builder. This is returned directly if
         * no other subsets are added.
         */
        private PlaneConvexSubset convexSubset;

        /** Create a new subset builder for the given plane.
         * @param plane plane to build a subset for
         */
        Builder(final Plane plane) {
            this.plane = plane;
        }

        /** {@inheritDoc} */
        @Override
        public void add(final HyperplaneSubset<Vector3D> sub) {
            addInternal(sub);
        }

        /** {@inheritDoc} */
        @Override
        public void add(final HyperplaneConvexSubset<Vector3D> sub) {
            addInternal(sub);
        }

        /** {@inheritDoc} */
        @Override
        public PlaneSubset build() {
            // return the convex subset directly if that was all we were given
            if (convexSubset != null) {
                return convexSubset;
            }
            return getTreeSubset();
        }

        /** Internal method for adding hyperplane subsets to this builder.
         * @param sub the hyperplane subset to add; may be either convex or non-convex
         */
        private void addInternal(final HyperplaneSubset<Vector3D> sub) {
            Objects.requireNonNull(sub, "Hyperplane subset must not be null");

            if (sub instanceof PlaneConvexSubset) {
                addConvexSubset((PlaneConvexSubset) sub);
            } else if (sub instanceof EmbeddedTreePlaneSubset) {
                addTreeSubset((EmbeddedTreePlaneSubset) sub);
            } else {
                throw new IllegalArgumentException("Unsupported hyperplane subset type: " + sub.getClass().getName());
            }
        }

        /** Add a convex subset to the builder.
         * @param convex convex subset to add
         */
        private void addConvexSubset(final PlaneConvexSubset convex) {
            Planes.validatePlanesEquivalent(plane, convex.getPlane());

            if (treeSubset == null && convexSubset == null) {
                convexSubset = convex;
            } else {
                getTreeSubset().add(convex);
            }
        }

        /** Add an embedded tree subset to the builder.
         * @param tree embedded tree subset to add
         */
        private void addTreeSubset(final EmbeddedTreePlaneSubset tree) {
            // no need to validate the line here since the add() method does that for us
            getTreeSubset().add(tree);
        }

        /** Get the tree subset for the builder, creating it if needed.
         * @return the tree subset for the builder
         */
        private EmbeddedTreePlaneSubset getTreeSubset() {
            if (treeSubset == null) {
                treeSubset = new EmbeddedTreePlaneSubset(plane.getEmbedding());

                if (convexSubset != null) {
                    treeSubset.add(convexSubset);

                    convexSubset = null;
                }
            }

            return treeSubset;
        }
    }
}
