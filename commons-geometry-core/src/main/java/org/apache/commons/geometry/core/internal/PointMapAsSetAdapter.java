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
package org.apache.commons.geometry.core.internal;

import java.util.AbstractSet;
import java.util.Iterator;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.collection.PointMap;
import org.apache.commons.geometry.core.collection.PointSet;

/** Class that exposes a {@link PointMap} as a {@link PointSet}.
 * @param <P> Point type
 */
public class PointMapAsSetAdapter<P extends Point<P>, M extends PointMap<P, Object>>
    extends AbstractSet<P>
    implements PointSet<P> {

    /** Dummy map value used to indicate presence in the set. */
    private static final Object PRESENT = new Object();

    /** Backing map. */
    private final M map;

    public PointMapAsSetAdapter(final M backingMap) {
        this.map = backingMap;
    }

    /** {@inheritDoc} */
    @Override
    public P resolve(final P pt) {
        return map.resolveKey(pt);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<P> iterator() {
        return map.keySet().iterator();
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return map.size();
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(final Object obj) {
        return map.containsKey(obj);
    }

    /** {@inheritDoc} */
    @Override
    public boolean add(final P pt) {
        return map.put(pt, PRESENT) == null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean remove(final Object obj) {
        final Object prev = map.remove(obj);
        return GeometryInternalUtils.sameInstance(prev, PRESENT);
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        map.clear();
    }

    /** Get the backing map instance.
     * @return backing map instance
     */
    protected M getMap() {
        return map;
    }
}
