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

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.Point;

/** Simple implementation of {@link BoundarySource} containing boundaries stored in a list.
 * Lists given during construction are used directly; no copies are made. Thread safety and
 * immutability therefore depend on the underlying list and its usage outside of this class.
 * The boundary list cannot be modified through this class.
 * @param <P> Point implementation type
 * @param <S> Hyperplane convex subset implementation type
 */
public class BoundaryList<P extends Point<P>, S extends HyperplaneConvexSubset<P>>
    implements BoundarySource<S> {

    /** List of boundaries. */
    private final List<S> boundaries;

    /** Construct a new instance containing the given boundaries. The input list is
     * used directly; no copy is made.
     * @param boundaries boundary list
     */
    public BoundaryList(final List<? extends S> boundaries) {
        this.boundaries = Collections.unmodifiableList(boundaries);
    }

    /** Get the boundaries for the instance. The returned list cannot be modified.
     * @return boundaries for the instance
     */
    public List<S> getBoundaries() {
        return boundaries;
    }

    /** Get the number of boundaries in the instance. This is exactly
     * equivalent to {@code boundaryList.getBoundaries().size()} but the
     * word "size" is avoided here to prevent confusion with geometric
     * size.
     * @return number of boundaries in the instance
     */
    public int count() {
        return boundaries.size();
    }

    /** {@inheritDoc} */
    @Override
    public Stream<S> boundaryStream() {
        return boundaries.stream();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName())
            // only display the count and not the actual boundaries
            // since the list could be huge
            .append("[count= ")
            .append(count())
            .append(']');

        return sb.toString();
    }
}
