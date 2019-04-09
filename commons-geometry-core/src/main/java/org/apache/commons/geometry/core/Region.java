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
package org.apache.commons.geometry.core;

public interface Region<P extends Point<P>> {

    /** Classify the given point with respect to the region.
     * @param pt the point to classify
     * @return the location of the point with respect to the region
     */
    RegionLocation classify(P pt);

    /** Return true if the given point does not lie on the outside of
     * the region. This will be the case if the point is on the inside
     * or on the boundary.
     * @param pt the point to test
     * @return true if the point is on the inside or boundary of the region
     */
    default boolean contains(P pt) {
        final RegionLocation location = classify(pt);
        return location != null && location != RegionLocation.OUTSIDE;
    }
}
