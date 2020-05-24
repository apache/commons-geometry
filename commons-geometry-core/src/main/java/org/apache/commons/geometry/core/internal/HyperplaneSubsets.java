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
package org.apache.commons.geometry.core.internal;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.EmbeddingHyperplane;

/** Utility methods for {@link org.apache.commons.geometry.core.partitioning.HyperplaneSubset}
 * implementations.
 */
public final class HyperplaneSubsets {

    /** Utility class; no instantiation. */
    private HyperplaneSubsets() {
    }

    /** Classify a point against a region embedded in a hyperplane.
     * @param <P> Point implementation class
     * @param <S> Subspace point implementation class
     * @param <H> Hyperplane implementation class
     * @param <R> Region implementation class
     * @param pt the point to classify
     * @param hyperplane hyperplane containing the embedded region
     * @param embeddedRegion embedded region to classify against
     * @return the region location of the given point
     */
    public static <
        P extends Point<P>,
        S extends Point<S>,
        H extends EmbeddingHyperplane<P, S>,
        R extends Region<S>> RegionLocation classifyAgainstEmbeddedRegion(final P pt,
                final H hyperplane, final R embeddedRegion) {

        if (hyperplane.contains(pt)) {
            final S subPoint = hyperplane.toSubspace(pt);

            return embeddedRegion.classify(subPoint);
        }

        return RegionLocation.OUTSIDE;
    }

    /** Return the closest point to a given point in a region embedded in a hyperplane.
     * @param <P> Point implementation class
     * @param <S> Subspace point implementation class
     * @param <H> Hyperplane implementation class
     * @param <R> Region implementation class
     * @param pt point to find the closest point to
     * @param hyperplane hyperplane containing the embedded region
     * @param embeddedRegion embedded region to find the closest point in
     * @return the closest point to {@code pt} in the embedded region
     */
    public static <
        P extends Point<P>,
        S extends Point<S>,
        H extends EmbeddingHyperplane<P, S>,
        R extends Region<S>> P closestToEmbeddedRegion(final P pt,
                final H hyperplane, final R embeddedRegion) {

        final S subPt = hyperplane.toSubspace(pt);

        if (embeddedRegion.contains(subPt)) {
            return hyperplane.toSpace(subPt);
        }

        final S subProjected = embeddedRegion.project(subPt);
        if (subProjected != null) {
            return hyperplane.toSpace(subProjected);
        }

        return null;
    }
}
