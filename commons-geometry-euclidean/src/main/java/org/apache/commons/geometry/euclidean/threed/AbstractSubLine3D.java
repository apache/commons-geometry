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
package org.apache.commons.geometry.euclidean.threed;

import java.io.Serializable;

import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.Vector1D;

/** Internal base class for 3 dimensional subline implementations.
 * @param <R> 1D subspace region type
 */
abstract class AbstractSubLine3D<R extends Region<Vector1D>> implements Serializable {

    /** Serializable UID. */
    private static final long serialVersionUID = 20190812L;

    /** The line that this instance belongs to. */
    private final Line3D line;

    /** Construct a new instance belonging to the given line.
     * @param line line the instance belongs to
     */
    protected AbstractSubLine3D(final Line3D line) {
        this.line = line;
    }

    /** Get the line that this subline belongs to.
     * @return the line that this subline belongs to.
     */
    public Line3D getLine() {
        return line;
    }

    /** Get the precision object used to perform floating point
     * comparisons for this instance.
     * @return the precision object for this instance
     */
    public DoublePrecisionContext getPrecision() {
        return line.getPrecision();
    }

    /** Get the subspace region for the subline.
     * @return the subspace region for the subline
     */
    public abstract R getSubspaceRegion();
}
