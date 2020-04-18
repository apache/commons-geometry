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

import org.apache.commons.geometry.core.partitioning.HyperplaneBoundedRegion;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.Vector1D;

/** Class representing a subline in 3D Euclidean space. A subline is defined in this library
 * as a subset of the points lying on a line. For examples, line segments and rays are sublines.
 * Sublines may be finite or infinite.
 */
public abstract class SubLine3D {
    /** The line containing this instance. */
    private final Line3D line;

    /** Construct a new instance based on the given line.
     * @param line line containing the instance
     */
    SubLine3D(final Line3D line) {
        this.line = line;
    }

    /** Get the line containing this subline.
     * @return the line containing this subline
     */
    public Line3D getLine() {
        return line;
    }

    /** Get the precision object used to perform floating point
     * comparisons for this instance. This is the same instance as
     * that used by the containing line.
     * @return the precision object for this instance
     */
    public DoublePrecisionContext getPrecision() {
        return line.getPrecision();
    }

    /** Get the subspace region for the subline.
     * @return the subspace region for the subline
     */
    public abstract HyperplaneBoundedRegion<Vector1D> getSubspaceRegion();
}
