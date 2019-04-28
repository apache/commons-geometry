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
package org.apache.commons.geometry.core.partition.region;

import java.io.Serializable;

import org.apache.commons.geometry.core.Point;

/** Class containing geometric properties of a region.
 */
public class RegionProperties<P extends Point<P>> implements Serializable {

    /** Serializable UID */
    private static final long serialVersionUID = 20190428L;

    /** The size of the region */
    private final double size;

    /** The barycenter of the region */
    private final P barycenter;

    /** Simple constructor.
     * @param size the region size
     * @param barycenter the region barycenter
     */
    public RegionProperties(final double size, final P barycenter) {
        this.size = size;
        this.barycenter = barycenter;
    }

    /** Get the size of the region.
     * @return the size of the region
     */
    public double getSize() {
        return size;
    }

    /** Get the barycenter of the region.
     * @return the barycenter of the region
     */
    public P getBarycenter() {
        return barycenter;
    }
}
