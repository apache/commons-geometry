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
package org.apache.commons.geometry.core.partitioning.test;

import org.apache.commons.geometry.core.Point;

/** Class representing a point in two dimensional Euclidean space. This
 * class should only be used for testing purposes.
 */
public final class TestPoint2D implements Point<TestPoint2D> {

    /** Instance representing the coordinates {@code (0, 0)} */
    public static final TestPoint2D ZERO = new TestPoint2D(0, 0);

    /** Instance representing the coordinates {@code (1, 0)} */
    public static final TestPoint2D PLUS_X = new TestPoint2D(1, 0);

    /** Instance representing the coordinates {@code (0, 1)} */
    public static final TestPoint2D PLUS_Y = new TestPoint2D(0, 1);

    /** X coordinate */
    private final double x;

    /** Y coordinate */
    private final double y;

    /** Simple constructor.
     * @param x x coordinate
     * @param y y coordinate
     */
    public TestPoint2D(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    /** Get the x coordinate value.
     * @return x coordinate value
     */
    public double getX() {
        return x;
    }

    /** Get the y coordinate value.
     * @return y coordinate value
     */
    public double getY() {
        return y;
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
        return Double.isInfinite(x) || Double.isInfinite(y);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFinite() {
        return Double.isFinite(x) && Double.isFinite(y);
    }

    /** {@inheritDoc} */
    @Override
    public double distance(final TestPoint2D p) {
        final double dx = x - p.x;
        final double dy = y - p.y;

        return Math.sqrt((dx * dx) + (dy * dy));
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
