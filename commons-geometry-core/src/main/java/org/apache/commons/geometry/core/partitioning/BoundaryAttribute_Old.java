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

import org.apache.commons.geometry.core.Point;

/** Class holding boundary attributes.
 *
 * <p>This class is used for the attributes associated with the
 * nodes of region boundary shell trees returned by the {@link
 * Region_Old#getTree(boolean) Region.getTree(includeBoundaryAttributes)}
 * when the boolean {@code includeBoundaryAttributes} parameter is
 * set to {@code true}. It contains the parts of the node cut
 * sub-hyperplane that belong to the boundary.</p>
 *
 * <p>This class is a simple placeholder, it does not provide any
 * processing methods.</p>
 *
 * @param <P> Point type defining the space
 * @see Region_Old#getTree
 */
public class BoundaryAttribute_Old<P extends Point<P>> {

    /** Part of the node cut sub-hyperplane that belongs to the
     * boundary and has the outside of the region on the plus side of
     * its underlying hyperplane (may be null).
     */
    private final SubHyperplane_Old<P> plusOutside;

    /** Part of the node cut sub-hyperplane that belongs to the
     * boundary and has the inside of the region on the plus side of
     * its underlying hyperplane (may be null).
     */
    private final SubHyperplane_Old<P> plusInside;

    /** Sub-hyperplanes that were used to split the boundary part. */
    private final NodesSet_Old<P> splitters;

    /** Simple constructor.
     * @param plusOutside part of the node cut sub-hyperplane that
     * belongs to the boundary and has the outside of the region on
     * the plus side of its underlying hyperplane (may be null)
     * @param plusInside part of the node cut sub-hyperplane that
     * belongs to the boundary and has the inside of the region on the
     * plus side of its underlying hyperplane (may be null)
     * @param splitters sub-hyperplanes that were used to
     * split the boundary part (may be null)
     */
    BoundaryAttribute_Old(final SubHyperplane_Old<P> plusOutside,
                      final SubHyperplane_Old<P> plusInside,
                      final NodesSet_Old<P> splitters) {
        this.plusOutside = plusOutside;
        this.plusInside  = plusInside;
        this.splitters   = splitters;
    }

    /** Get the part of the node cut sub-hyperplane that belongs to the
     * boundary and has the outside of the region on the plus side of
     * its underlying hyperplane.
     * @return part of the node cut sub-hyperplane that belongs to the
     * boundary and has the outside of the region on the plus side of
     * its underlying hyperplane
     */
    public SubHyperplane_Old<P> getPlusOutside() {
        return plusOutside;
    }

    /** Get the part of the node cut sub-hyperplane that belongs to the
     * boundary and has the inside of the region on the plus side of
     * its underlying hyperplane.
     * @return part of the node cut sub-hyperplane that belongs to the
     * boundary and has the inside of the region on the plus side of
     * its underlying hyperplane
     */
    public SubHyperplane_Old<P> getPlusInside() {
        return plusInside;
    }

    /** Get the sub-hyperplanes that were used to split the boundary part.
     * @return sub-hyperplanes that were used to split the boundary part
     */
    public NodesSet_Old<P> getSplitters() {
        return splitters;
    }

}
