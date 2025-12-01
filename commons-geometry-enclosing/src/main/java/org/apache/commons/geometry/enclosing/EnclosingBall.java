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
package org.apache.commons.geometry.enclosing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.numbers.core.Precision;

/** This class represents a ball enclosing some points.
 * @param <P> Point type.
 * @see Point
 * @see Encloser
 */
public class EnclosingBall<P extends Point<P>> {
    /** Center of the ball. */
    private final P center;

    /** Radius of the ball. */
    private final double radius;

    /** Support points used to define the ball. */
    private final List<P> support;

    /** Construct an enclosing ball defined by a collection of support points. Callers are responsible
     * for ensuring that the given points lie inside the ball. No validation is performed.
     * @param center center of the ball
     * @param radius radius of the ball
     * @param support support points used to define the ball
     */
    public EnclosingBall(final P center, final double radius, final Collection<P> support) {
        this.center  = center;
        this.radius  = radius;
        this.support = Collections.unmodifiableList(new ArrayList<>(support));
    }

    /** Get the center of the ball.
     * @return center of the ball
     */
    public P getCenter() {
        return center;
    }

    /** Get the radius of the ball.
     * @return radius of the ball (can be negative if the ball is empty)
     */
    public double getRadius() {
        return radius;
    }

    /** Get the support points used to define the ball.
     * @return support points used to define the ball
     */
    public List<P> getSupport() {
        return support;
    }

    /** Get the number of support points used to define the ball.
     * @return number of support points used to define the ball
     */
    public int getSupportSize() {
        return support.size();
    }

    /** Check if a point is within the ball or on the boundary. True is returned if the
     * distance from the center of the ball to the given point is strictly less than
     * or equal to the ball radius.
     * @param point point to test
     * @return true if the point is within the ball or on the boundary
     */
    public boolean contains(final P point) {
        return point.distance(center) <= radius;
    }

    /** Check if a point is within the ball or on the boundary, using the given precision
     * context for floating point comparison. True is returned if the distance from the
     * center of the ball to the given point is less than or equal to the ball radius
     * as evaluated by the precision context.
     * @param point point to test
     * @param precision precision context to use for floating point comparisons
     * @return true if the point is within the ball or on the boundary as evaluated by
     *      the precision context
     */
    public boolean contains(final P point, final Precision.DoubleEquivalence precision) {
        return precision.lte(point.distance(center), radius);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(128);
        sb.append(getClass().getSimpleName())
            .append("[center= ")
            .append(getCenter())
            .append(", radius= ")
            .append(getRadius())
            .append(']');

        return sb.toString();
    }
}
