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
import org.apache.commons.geometry.core.Transform;

/** Extension of the {@link HyperplaneSubset} interface with the additional restriction
 * that instances represent convex regions of space.
 * @param <P> Point implementation type
 */
public interface HyperplaneConvexSubset<P extends Point<P>> extends HyperplaneSubset<P> {

    /** Reverse the orientation of the hyperplane for this instance, returning the result as
     * a new instance. The returned subset contains the same points but has a reversed orientation.
     * @return a hyperplane convex subset representing the same region but with the
     *      opposite orientation.
     */
    HyperplaneConvexSubset<P> reverse();

    /** {@inheritDoc}
     *
     * <p>The parts resulting from a split operation with a convex subset
     * are guaranteed to also be convex.</p>
     */
    @Override
    Split<? extends HyperplaneConvexSubset<P>> split(Hyperplane<P> splitter);

    /** {@inheritDoc}
     *
     * <p>Hyperplane convex subsets subjected to affine transformations remain
     * convex.</p>
     */
    @Override
    HyperplaneConvexSubset<P> transform(Transform<P> transform);
}
