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
package org.apache.commons.geometry.core.internal;

import java.util.Comparator;

/** Class combining a value with a distance.
 * @param <V> Value type
 */
public final class DistancedValue<V> {

    /** Value object. */
    private final V value;

    /** Distance value. */
    private final double distance;

    /** Simple constructor.
     * @param value value
     * @param distance distance
     */
    private DistancedValue(final V value, final double distance) {
        this.value = value;
        this.distance = distance;
    }

    /** Get the value.
     * @return the value
     */
    public V getValue() {
        return value;
    }

    /** Get the distance.
     * @return the distance
     */
    public double getDistance() {
        return distance;
    }

    /** Construct a new instance.
     * @param <V> Value type
     * @param value value
     * @param distance distance
     * @return new instance
     */
    public static <V> DistancedValue<V> of(final V value, final double distance) {
        return new DistancedValue<>(value, distance);
    }

    /** Return a {@link Comparator} that orders {@link DistancedValue} instances in
     * ascending distance order.
     * @param <V> Value type
     * @return comparator that places instances in ascending distance order
     */
    public static <V> Comparator<DistancedValue<V>> ascendingDistance() {
        return (a, b) -> Double.compare(a.distance, b.distance);
    }

    /** Return a {@link Comparator} that orders {@link DistancedValue} instances in
     * descending distance order.
     * @param <V> Value type
     * @return comparator that places instances in descending distance order
     */
    public static <V> Comparator<DistancedValue<V>> descendingDistance() {
        return (a, b) -> Double.compare(b.distance, a.distance);
    }
}
