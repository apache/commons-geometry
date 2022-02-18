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
package org.apache.commons.geometry.spherical;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.geometry.core.internal.GeometryInternalUtils;
import org.apache.commons.geometry.spherical.twod.Point2S;
import org.apache.commons.geometry.spherical.twod.PointMap2S;
import org.apache.commons.numbers.core.Precision;

final class PointMap2SImpl<V> implements PointMap2S<V> {

    PointMap2SImpl(final Precision.DoubleEquivalence precision) {
    }

    /** {@inheritDoc} */
    @Override
    public Point2S resolveKey(final Point2S pt) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Entry<Point2S, V> resolveEntry(final Point2S pt) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsKey(final Object key) {
        // TODO Auto-generated method stub
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsValue(final Object value) {
        // TODO Auto-generated method stub
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public V get(final Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public V put(final Point2S key, final V value) {
        GeometryInternalUtils.validatePointMapKey(key);

        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public V remove(final Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void putAll(final Map<? extends Point2S, ? extends V> m) {
        // TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        // TODO Auto-generated method stub
    }

    /** {@inheritDoc} */
    @Override
    public Set<Point2S> keySet() {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<V> values() {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Set<Entry<Point2S, V>> entrySet() {
        // TODO Auto-generated method stub
        return null;
    }
}
