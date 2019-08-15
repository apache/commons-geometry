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

/** Interface representing a convex, possibly infinite, region of space with
 * boundaries defined by hyperplanes.
 * @param <P> Point implementation type
 * @see HyperplaneBoundedRegion
 */
public interface ConvexHyperplaneBoundedRegion<P extends Point<P>> extends HyperplaneBoundedRegion<P> {

    /** Transform this instance with the argument.
     * @param transform transform to apply to this instance
     * @return a new region representing the transformation of the current instance by
     *      the argument
     */
    ConvexHyperplaneBoundedRegion<P> transform(Transform<P> transform);

    /** Trim the given convex subhyperplane to the portion contained inside this instance.
     * @param convexSubHyperplane convex subhyperplane to trim. Null is returned if the subhyperplane
     * does not intersect the instance.
     * @return portion of the argument that lies entirely inside the region represented by
     *      this instance, or null if it does not intersect.
     */
    ConvexSubHyperplane<P> trim(ConvexSubHyperplane<P> convexSubHyperplane);

    /** {@inheritDoc}
    *
    * <p>The parts resulting from a split operation with a convex region
    * are guaranteed to also be convex.</p>
    */
    @Override
    Split<? extends ConvexHyperplaneBoundedRegion<P>> split(Hyperplane<P> splitter);
}
