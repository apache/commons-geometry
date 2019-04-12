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

    /** Return true if the region spans the entire space. In other words,
     * a region is full if no points in the space are classified as
     * {@link RegionLocation#OUTSIDE outside}.
     * @return true if the region spans the entire space
     */
    boolean isFull();

    /** Return true if the region is completely empty, ie all points in
     * the space are classified as {@link RegionLocation#OUTSIDE outside}.
     * @return true if the region is empty
     */
    boolean isEmpty();

    /** Classify the given point with respect to the region.
     * @param pt the point to classify
     * @return the location of the point with respect to the region
     */
    RegionLocation classify(P pt);

    /** Return true if the given point is on the inside or boundary
     * of the region.
     * @param pt the point to test
     * @return true if the point is on the inside or boundary of the region
     */
    default boolean contains(P pt) {
        final RegionLocation location = classify(pt);
        return location != null && location != RegionLocation.OUTSIDE;
    }
}
