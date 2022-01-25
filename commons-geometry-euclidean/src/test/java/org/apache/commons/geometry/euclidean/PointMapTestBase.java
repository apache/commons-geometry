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

import java.util.List;

import org.apache.commons.geometry.core.collection.PointMap;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Base test class for Euclidean {@link PointMap} implementations.
 * @param <P> Point type
 */
public abstract class PointMapTestBase<P extends EuclideanVector<P>> {

    private static final double EPS = 1e-10;

    private static final Precision.DoubleEquivalence PRECISION =
            Precision.doubleEquivalenceOfEpsilon(EPS);

    /** Get a new point map instance for testing.
     * @param <V> Value type
     * @param precision precision context to determine floating point equality
     * @return a new map instance for testing.
     */
    public abstract <V> PointMap<P, V> getMap(Precision.DoubleEquivalence precision);

    /** Get {@code cnt} number of unique test points that differ from each other in
     * each dimension by at least {@code eps}.
     * @param cnt number of points to return
     * @param eps minimum value that each point must differ from other points along
     *      each dimension
     * @return list of test points
     */
    public abstract List<P> getTestPoints(int cnt, double eps);

    @Test
    void testEmpty() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final P pt = getTestPoints(1, EPS).get(0);

        // act/assert
        Assertions.assertEquals(0, map.size());
        Assertions.assertTrue(map.isEmpty());

        Assertions.assertNull(map.get(pt));
        Assertions.assertFalse(map.containsKey(pt));
        Assertions.assertFalse(map.containsValue(pt));
    }
}
