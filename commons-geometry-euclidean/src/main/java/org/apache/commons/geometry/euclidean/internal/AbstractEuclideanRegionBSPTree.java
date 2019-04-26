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
package org.apache.commons.geometry.euclidean.internal;

import java.io.Serializable;

import org.apache.commons.geometry.core.partition.region.AbstractRegionBSPTree;
import org.apache.commons.geometry.euclidean.EuclideanRegion;
import org.apache.commons.geometry.euclidean.EuclideanVector;

public abstract class AbstractEuclideanRegionBSPTree<V extends EuclideanVector<V>, N extends AbstractRegionBSPTree.AbstractRegionNode<V, N>>
    extends AbstractRegionBSPTree<V, N> implements EuclideanRegion<V> {

    /** Serializable UID */
    private static final long serialVersionUID = 20190419L;

    /** The tree version that the region properties were last computed with */
    private int regionPropertiesVersion = -1;

    /** The current set of geometric properties for the region. */
    private EuclideanRegionProperties<V> regionProperties;

    protected AbstractEuclideanRegionBSPTree(boolean full) {
        super(full);
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        return getRegionProperties().getSize();
    }

    /** {@inheritDoc} */
    @Override
    public V getBarycenter() {
        return getRegionProperties().getBarycenter();
    }

    /** Get the properties for the region, computing if needed.
     * @return the properties for the region.
     */
    protected EuclideanRegionProperties<V> getRegionProperties() {
        if (regionPropertiesVersion != getVersion()) {
            regionProperties = computeRegionProperties();

            regionPropertiesVersion = getVersion();
        }

        return regionProperties;
    }

    /** Compute the geometric properties for this region.
     * @return the geometric properties for this region
     */
    protected abstract EuclideanRegionProperties<V> computeRegionProperties();

    /** Class containing basic geometry propertie for regions in Euclidean space.
     * @param <V> Point implementation type
     */
    public static class EuclideanRegionProperties<V extends EuclideanVector<V>> implements Serializable {

        /** Serializable UID */
        private static final long serialVersionUID = 20190419L;

        private final double size;

        private final V barycenter;

        public EuclideanRegionProperties(final double size, final V barycenter) {
            this.size = size;
            this.barycenter = barycenter;
        }

        public double getSize() {
            return size;
        }

        public V getBarycenter() {
            return barycenter;
        }
    }
}
