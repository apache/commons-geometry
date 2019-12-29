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
package org.apache.commons.geometry.euclidean;

import java.util.Objects;

import org.apache.commons.geometry.core.Embedding;
import org.apache.commons.geometry.euclidean.oned.Vector1D;

/** Base class for intersections discovered during linecast operations. This class contains
 * the intersection point and the normal of the target boundary at the point of intersection
 * along with the intersecting line and abscissa.
 * @param <P> Euclidean point/vector implementation type
 * @param <U> Unit-length Euclidean vector implementation type
 * @param <L> Line implementation type
 */
public abstract class AbstractLinecastPoint<
    P extends EuclideanVector<P>,
    U extends P,
    L extends Embedding<P, Vector1D>> {

    /** Line intersection point. */
    private final P point;

    /** Normal of the target boundary at the intersection point. */
    private final U normal;

    /** The intersecting line. */
    private final L line;

    /** Abscissa of the intersection point along the intersecting line. */
    private final double abscissa;

    /** Construct a new instance from its components.
     * @param point intersection point
     * @param normal surface normal
     * @param line line that the intersection point belongs to
     */
    protected AbstractLinecastPoint(final P point, final U normal, final L line) {
        this.point = point;
        this.normal = normal;
        this.line = line;

        this.abscissa = line.toSubspace(point).getX();
    }

    /** Get the line intersection point.
     * @return the line intersection point
     */
    public P getPoint() {
        return point;
    }

    /** Get the normal of the target boundary at the intersection point.
     * @return the normal of the target boundary at the intersection point
     */
    public U getNormal() {
        return normal;
    }

    /** Get the intersecting line.
     * @return the intersecting line
     */
    public L getLine() {
        return line;
    }

    /** Get the abscissa (1D position) of the intersection point
     * along the linecast line.
     * @return the abscissa of the intersection point.
     */
    public double getAbscissa() {
        return abscissa;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(point, normal, line);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }

        final AbstractLinecastPoint<?, ?, ?> other = (AbstractLinecastPoint<?, ?, ?>) obj;

        return Objects.equals(point, other.point) &&
                Objects.equals(normal, other.normal) &&
                Objects.equals(line, other.line);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
            .append("[point= ")
            .append(getPoint())
            .append(", normal= ")
            .append(getNormal())
            .append(", abscissa= ")
            .append(getAbscissa())
            .append(", line= ")
            .append(getLine())
            .append(']');

        return sb.toString();
    }
}
