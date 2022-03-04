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
package org.apache.commons.geometry.core.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.numbers.core.Precision;

/** Base test class for point collection types.
 * @param <P> Point type
 */
public abstract class PointCollectionTestBase<P extends Point<P>> {

    public static final double EPS = 1e-10;

    public static final Precision.DoubleEquivalence PRECISION =
            Precision.doubleEquivalenceOfEpsilon(EPS);

    /** Create an empty array of the target point type.
     * @return empty array of the target point type
     */
    protected abstract P[] createPointArray();

    /** Get a list of points with {@code NaN} coordinates.
     * @return list of points with {@code NaN} coordinates
     */
    protected abstract List<P> getNaNPoints();

    /** Get a list of points with infinite coordinates.
     * @return list of points with infinite coordinates
     */
    protected abstract List<P> getInfPoints();

    /** Get {@code cnt} number of unique test points that differ from each other in
     * each dimension by <em>at least</em> {@code eps}.
     * @param cnt number of points to return
     * @param eps minimum value that each point must differ from other points along
     *      each dimension
     * @return list of test points
     */
    protected abstract List<P> getTestPoints(int cnt, double eps);

    /** Get a list of points that lie {@code dist} distance from {@code pt}.
     * @param pt input point
     * @param dist distance from {@code pt}
     * @return list of points that lie {@code dist} distance from {@code pt}
     */
    protected abstract List<P> getTestPointsAtDistance(P pt, double dist);

    /** Get {@code cnt} number of unique test points that differ from each other in
     * each dimension by <em>at least</em> {@code eps}. The returned list is shuffled
     * using {@code rnd}.
     * @param cnt number of points to return
     * @param eps minimum value that each point must differ from other points along
     *      each dimension
     * @param rnd random instance used to shuffle the order of the points
     * @return randomly ordered list of test points
     */
    protected List<P> getTestPoints(final int cnt, final double eps, final Random rnd) {
        final List<P> pts = new ArrayList<>(getTestPoints(cnt, eps));
        Collections.shuffle(pts, rnd);

        return pts;
    }
}
