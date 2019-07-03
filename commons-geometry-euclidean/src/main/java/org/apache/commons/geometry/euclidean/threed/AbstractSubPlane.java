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

import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.partition.AbstractEmbeddingSubHyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.SubPlane.SubPlaneBuilder;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

abstract class AbstractSubPlane<R extends Region<Vector2D>>
    extends AbstractEmbeddingSubHyperplane<Vector3D, Vector2D, Plane> {

    /** The plane defining this instance. */
    private final Plane plane;

    AbstractSubPlane(final Plane plane) {
        this.plane = plane;
    }

    /** Get the plane that this subplane lies on. This method is an alias
     * for {@link getHyperplane()}.
     * @return the plane that this subplane lies on
     * @see #getHyperplane()
     */
    public Plane getPlane() {
        return getHyperplane();
    }

    /** {@inheritDoc} */
    @Override
    public Plane getHyperplane() {
        return plane;
    }

    /** {@inheritDoc} */
    @Override
    public SubPlaneBuilder builder() {
        return new SubPlaneBuilder(plane);
    }

    /** Return the object used to perform floating point comparisons, which is the
     * same object used by the underlying {@link Plane).
     * @return precision object used to perform floating point comparisons.
     */
    public DoublePrecisionContext getPrecision() {
        return plane.getPrecision();
    }
}
