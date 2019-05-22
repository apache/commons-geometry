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
package org.apache.commons.geometry.core.partition.test;

import java.io.Serializable;

import org.apache.commons.geometry.core.Point;

/** Class representing a point in one dimensional Euclidean space. This
 * class should only be used for testing purposes.
 */
public class TestPoint1D implements Point<TestPoint1D>, Serializable {

    /** Serializable UID */
    private static final long serialVersionUID = 1L;

    /** X coordinate */
    private final double x;

    /** Simple constructor.
     * @param x x coordinate
     */
    public TestPoint1D(final double x) {
        this.x = x;
    }

    /** Get the x coordinate of the point.
     * @return the x coordinate of the point
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
        return Double.isInfinite(x);
    }

    /** {@inheritDoc} */
    @Override
    public double distance(final TestPoint1D p) {
        return Math.abs(this.x - p.x);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "(" + x + ")";
    }
}
