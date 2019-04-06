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

import java.io.Serializable;
import java.util.Objects;

public class Interval implements Serializable {

    /** Serializable UID. */
    private static final long serialVersionUID = 20190210L;

    private final double min;

    private final double max;

    private Interval(final double a, final double b) {
        this.min = Math.min(a, b);
        this.max = Math.max(a, b);
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public boolean isInfinite() {
        return Double.isInfinite(min) || Double.isInfinite(max);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        else if (!(obj instanceof Interval)) {
            return false;
        }

        Interval other = (Interval) obj;

        return Double.compare(min, other.min) == 0 &&
                Double.compare(max, other.max) == 0;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName())
            .append("[min= ")
            .append(min)
            .append(", max= ")
            .append(max)
            .append(']');

        return sb.toString();
    }

    public static Interval of(final double a, final double b) {
        return new Interval(a, b);
    }
}
