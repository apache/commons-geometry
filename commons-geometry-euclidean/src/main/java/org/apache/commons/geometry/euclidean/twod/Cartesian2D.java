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

package org.apache.commons.geometry.euclidean.twod;

import java.io.Serializable;

import org.apache.commons.geometry.core.Spatial;
import org.apache.commons.geometry.core.util.internal.SimpleTupleFormat;

/** This class represents a set of Cartesian coordinates in
 * two-dimensional Euclidean space.
 */
public abstract class Cartesian2D implements Spatial, Serializable {

    /** Serializable UID */
    private static final long serialVersionUID = 2918583078965478552L;

    /** Abscissa (first coordinate) */
    private final double x;

    /** Ordinate (second coordinate) */
    private final double y;

    /**
     * Simple Cartesian constructor.
     * @param x abscissa (first coordinate)
     * @param y ordinate (second coordinate)
     */
    protected Cartesian2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /** Returns the abscissa (first coordinate value) of the instance.
     * @return the abscissa
     */
    public double getX() {
        return x;
    }

    /** Returns the ordinate (second coordinate value) of the instance.
     * @return the ordinate
     */
    public double getY() {
        return y;
    }

    /** Get the coordinates for this instance as a dimension 2 array.
     * @return coordinates for this instance
     */
    public double[] toArray() {
        return new double[] { x, y };
    }

    /** {@inheritDoc} */
    @Override
    public int getDimension() {
        return 2;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isNaN() {
        return Double.isNaN(x) || Double.isNaN(y);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInfinite() {
        return !isNaN() && (Double.isInfinite(x) || Double.isInfinite(y));
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return SimpleTupleFormat.getDefault().format(getX(), getY());
    }

    /** Returns the Euclidean distance from this value to the given value.
     * @param other the set of coordinates to compute the distance to
     * @return Euclidean distance
     */
    protected double euclideanDistance(Cartesian2D other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt((dx * dx) + (dy * dy));
    }
}
