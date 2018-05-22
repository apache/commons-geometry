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
package org.apache.commons.geometry.euclidean.oned;

import org.apache.commons.geometry.core.Spatial;

/** This class represents a Cartesian coordinate value in
 * one-dimensional Euclidean space.
 */
public abstract class Cartesian1D implements Spatial {

    /** Serializable UID. */
    private static final long serialVersionUID = -1178039568877797126L;

    /** Abscissa (coordinate value). */
    protected final double x;

    /**
     * Simple constructor.
     * @param x abscissa (coordinate value)
     */
    protected Cartesian1D(double x) {
        this.x = x;
    }

    /**
     * Returns the abscissa (coordinate value) of the instance.
     * @return the abscissa value
     */
    public double getX() {
        return x;
    }

    /** {@inheritDoc} */
    @Override
    public int getDimension() {
        return 1;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isNaN() {
        return Double.isNaN(x);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInfinite() {
        return !isNaN() && Double.isInfinite(x);
    }
}
