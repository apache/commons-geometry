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

import java.util.List;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.Region;

/** Interface representing regions with boundaries defined by hyperplanes or
 * portions of hyperplanes. This interface is intended to represent closed regions
 * with finite sizes as well as infinite and empty spaces. Regions of this type
 * can be recursively split by hyperplanes into similar regions.
 * @param <P> Point implementation type
 */
public interface HyperplaneBoundedRegion<P extends Point<P>>
    extends Region<P>, Splittable<P, HyperplaneBoundedRegion<P>> {

    /** Convert this instance into a list of convex regions.
     * @return a list of convex region covering the same space as this
     *      instance
     */
    List<? extends ConvexHyperplaneBoundedRegion<P>> toConvex();
}
