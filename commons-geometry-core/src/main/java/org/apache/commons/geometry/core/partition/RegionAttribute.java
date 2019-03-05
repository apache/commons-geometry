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

import org.apache.commons.geometry.core.Point;

/** Attribute attached to nodes in {@link RegionBSPTree} instances.
 */
public class RegionAttribute<P extends Point<P>> {

    public static enum Location {
        INSIDE,
        OUTSIDE
    }

    private static final RegionAttribute<? extends Point<?>> INSIDE = new RegionAttribute<>(Location.INSIDE, null);

    private static final RegionAttribute<? extends Point<?>> OUTSIDE = new RegionAttribute<>(Location.OUTSIDE, null);

    private final Location location;

    private final BoundarySet<P> boundarySet;

    private RegionAttribute(final Location location, final BoundarySet<P> boundarySet) {
        this.location = location;
        this.boundarySet = boundarySet;
    }

    public Location getLocation() {
        return location;
    }

    public boolean isInside() {
        return location == Location.INSIDE;
    }

    public boolean isOutside() {
        return location == Location.OUTSIDE;
    }

    public BoundarySet<P> getBoundarySet() {
        return boundarySet;
    }

    @SuppressWarnings("unchecked")
    public static <P extends Point<P>> RegionAttribute<P> getInside() {
        return (RegionAttribute<P>) INSIDE;
    }

    @SuppressWarnings("unchecked")
    public static <P extends Point<P>> RegionAttribute<P> getOutside() {
        return (RegionAttribute<P>) OUTSIDE;
    }
}
