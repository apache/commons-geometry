/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.euclidean.twod;

import java.util.List;

import org.apache.commons.geometry.core.partitioning.BoundaryList;

/** {@link BoundarySource2D} implementation that uses boundaries stored in
 * a list. Lists given during construction are used directly; no copies are made.
 * Thread safety and immutability therefore depend on the underlying list and its
 * usage outside of this class. The boundary list cannot be modified through this
 * class.
 */
public class BoundaryList2D extends BoundaryList<Vector2D, LineConvexSubset>
    implements BoundarySource2D {

    /** Construct a new instance with the given list of boundaries. The
     * argument is used directly; no copy is made.
     * @param boundaries list of boundaries for the instance
     */
    public BoundaryList2D(final List<? extends LineConvexSubset> boundaries) {
        super(boundaries);
    }

    /** Return this instance.
     * @return this instance
     */
    @Override
    public BoundaryList2D toList() {
        return this;
    }
}
