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
package org.apache.commons.geometry.core.partition;

import java.io.Serializable;

import org.apache.commons.geometry.core.Point;

/** Class representing the portion of a
 * {@link RegionBSPTree.RegionNode RegionNode}'s cut subhyperplane that
 * lies on the boundary of the region. Portions of this subhyperplane
 * may be oriented so that the plus side of the subhyperplane points toward
 * the outside of the region ({@link #getOutsideFacing()}) and other portions
 * of the same subhyperplane may be oriented so that the plus side points
 * toward the inside of the region ({@link #getInsideFacing()}).
 */
public final class RegionCutBoundary<P extends Point<P>> implements Serializable {

    /** Serializable UID */
    private static final long serialVersionUID = 20190310L;

    /** Portion of the region cut subhyperplane with its plus side facing the
     * inside of the region.
     */
    private final SubHyperplane<P> insideFacing;

    /** Portion of the region cut subhyperplane with its plus side facing the
     * outside of the region.
     */
    private final SubHyperplane<P> outsideFacing;

    public RegionCutBoundary(final SubHyperplane<P> insideFacing, final SubHyperplane<P> outsideFacing) {
        this.insideFacing = insideFacing;
        this.outsideFacing = outsideFacing;
    }

    /** Get the portion of the region cut subhyperplane with its plus side facing the
     * inside of the region.
     * @return the portion of the region cut subhyperplane with its plus side facing the
     *      inside of the region
     */
    public SubHyperplane<P> getInsideFacing() {
        return insideFacing;
    }

    /** Get the portion of the region cut subhyperplane with its plus side facing the
     * outside of the region.
     * @return the portion of the region cut subhyperplane with its plus side facing the
     *      outside of the region
     */
    public SubHyperplane<P> getOutsideFacing() {
        return outsideFacing;
    }
}
