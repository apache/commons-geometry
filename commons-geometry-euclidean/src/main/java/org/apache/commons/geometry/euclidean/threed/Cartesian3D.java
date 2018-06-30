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

import org.apache.commons.geometry.core.Spatial;
import org.apache.commons.geometry.core.util.internal.SimpleTupleFormat;

/** This class represents a Cartesian coordinate value in
 * three-dimensional Euclidean space.
 */
public abstract class Cartesian3D implements Spatial, Serializable {

    /** Serializable UID. */
    private static final long serialVersionUID = 6249091865814886817L;

    /** Abscissa (first coordinate value) */
    private final double x;

    /** Ordinate (second coordinate value) */
    private final double y;

    /** Height (third coordinate value)*/
    private final double z;

    /** Simple constructor.
     * @param x abscissa (first coordinate value)
     * @param y ordinate (second coordinate value)
     * @param z height (third coordinate value)
     */
    protected Cartesian3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /** Returns the abscissa (first coordinate) value of the instance.
     * @return the abscisaa
     */
    public double getX() {
        return x;
    }

    /** Returns the ordinate (second coordinate) value of the instance.
     * @return the ordinate
     */
    public double getY() {
        return y;
    }

    /** Returns the height (third coordinate) value of the instance.
     * @return the height
     */
    public double getZ() {
        return z;
    }

    /** Get the coordinates for this instance as a dimension 3 array.
     * @return the coordinates for this instance
     */
    public double[] toArray() {
        return new double[] { x, y, z };
    }

    /** {@inheritDoc} */
    @Override
    public int getDimension() {
        return 3;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isNaN() {
        return Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInfinite() {
        return !isNaN() && (Double.isInfinite(x) || Double.isInfinite(y) || Double.isInfinite(z));
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return SimpleTupleFormat.getDefault().format(getX(), getY(), getZ());
    }

    /** Returns the Euclidean distance from this set of coordinates to the given coordinates.
     * @param other coordinates to compute the distance to.
     * @return Euclidean distance value
     */
    protected double euclideanDistance(Cartesian3D other) {
        // there are no cancellation problems here, so we use the straightforward formula
        final double dx = x - other.x;
        final double dy = y - other.y;
        final double dz = z - other.z;

        return Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
    }
}
