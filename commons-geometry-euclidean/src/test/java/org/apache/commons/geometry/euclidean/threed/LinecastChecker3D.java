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
package org.apache.commons.geometry.euclidean.threed;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.geometry.euclidean.threed.line.Line3D;
import org.apache.commons.geometry.euclidean.threed.line.LineConvexSubset3D;
import org.apache.commons.geometry.euclidean.threed.line.LinecastPoint3D;
import org.apache.commons.geometry.euclidean.threed.line.Linecastable3D;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;

/** Helper class designed to assist with linecast test assertions in 3D.
 */
class LinecastChecker3D {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    /** The linecastable target. */
    private final Linecastable3D target;

    /** List of expected results from the line cast operation. */
    private final List<ExpectedResult> expectedResults = new ArrayList<>();

    /** Construct a new instance that performs linecast assertions against the
     * given target.
     * @param target
     */
    LinecastChecker3D(final Linecastable3D target) {
        this.target = target;
    }

    /** Configure the instance to expect no results (an empty list from linecast() and null from
     * linecastFirst()) from the next linecast operation performed by {@link #whenGiven(Line3D)}
     * or {@link #whenGiven(LineConvexSubset3D)}.
     * @return
     */
    public LinecastChecker3D expectNothing() {
        expectedResults.clear();

        return this;
    }

    /** Configure the instance to expect a linecast point with the given parameters on the next
     * linecast operation. Multiple calls to this method and/or {@link #and(Vector3D, Vector3D)}
     * create an internal ordered list of results.
     * @param point
     * @param normal
     * @return
     */
    public LinecastChecker3D expect(final Vector3D point, final Vector3D normal) {
        expectedResults.add(new ExpectedResult(point, normal));

        return this;
    }

    /** Fluent API alias for {@link #expect(Vector3D, Vector3D)}.
     * @param point
     * @param normal
     * @return
     */
    public LinecastChecker3D and(final Vector3D point, final Vector3D normal) {
        return expect(point, normal);
    }

    /** Perform {@link Linecastable3D#linecast(Line3D)} and {@link Linecastable3D#linecastFirst(Line3D)}
     * operations using the given line and assert that the results match the configured expected
     * values.
     * @param line
     */
    public void whenGiven(final Line3D line) {
        checkLinecastResults(target.linecast(line), line);
        checkLinecastFirstResult(target.linecastFirst(line), line);
    }

    /** Perform {@link Linecastable3D#linecast(LineConvexSubset3D)} and {@link Linecastable3D#linecastFirst(LineConvexSubset3D)}
     * operations using the given line segment and assert that the results match the configured
     * expected results.
     * @param segment
     */
    public void whenGiven(final LineConvexSubset3D segment) {
        final Line3D line = segment.getLine();

        checkLinecastResults(target.linecast(segment), line);
        checkLinecastFirstResult(target.linecastFirst(segment), line);
    }

    /** Check that the given set of linecast result points matches those expected.
     * @param results
     * @param line
     */
    private void checkLinecastResults(final List<? extends LinecastPoint3D> results, final Line3D line) {
        Assertions.assertNotNull(results, "Linecast result list cannot be null");
        Assertions.assertEquals(expectedResults.size(), results.size(), "Unexpected result size for linecast");

        for (int i = 0; i < expectedResults.size(); ++i) {
            final LinecastPoint3D expected = toLinecastPoint(expectedResults.get(i), line);
            final LinecastPoint3D actual = results.get(i);

            if (!eq(expected, actual)) {
                Assertions.fail("Unexpected linecast point at index " + i + " expected " + expected +
                        " but was " + actual);
            }
        }
    }

    /** Check that the given linecastFirst result matches that expected.
     * @param result
     * @param line
     */
    private void checkLinecastFirstResult(final LinecastPoint3D result, final Line3D line) {
        if (expectedResults.isEmpty()) {
            Assertions.assertNull(result, "Expected linecastFirst result to be null");
        } else {
            final LinecastPoint3D expected = toLinecastPoint(expectedResults.get(0), line);

            Assertions.assertNotNull(result, "Expected linecastFirst result to not be null");

            if (!eq(expected, result)) {
                Assertions.fail("Unexpected result from linecastFirst: expected " + expected +
                        " but was " + result);
            }
        }
    }

    /** Fluent API method for creating new instances.
     * @param src
     * @return
     */
    public static LinecastChecker3D with(final Linecastable3D src) {
        return new LinecastChecker3D(src);
    }

    /** Return true if the given linecast points are equivalent according to the test precision.
     * @param a
     * @param b
     * @return
     */
    private static boolean eq(final LinecastPoint3D a, final LinecastPoint3D b) {
        return a.getPoint().eq(b.getPoint(), TEST_PRECISION) &&
                a.getNormal().eq(b.getNormal(), TEST_PRECISION) &&
                a.getLine().equals(b.getLine()) &&
                TEST_PRECISION.eq(a.getAbscissa(), b.getAbscissa());
    }

    /** Convert an {@link ExpectedResult} struct to a {@link org.apache.commons.geometry.euclidean.twod.LinecastPoint2D} instance
     * using the given line.
     * @param expected
     * @param line
     * @return
     */
    private static LinecastPoint3D toLinecastPoint(final ExpectedResult expected, final Line3D line) {
        return new LinecastPoint3D(expected.getPoint(), expected.getNormal(), line);
    }

    /** Class containing intermediate expected results for a linecast operation.
     */
    private static final class ExpectedResult {
        private final Vector3D point;
        private final Vector3D normal;

        ExpectedResult(final Vector3D point, final Vector3D normal) {
            this.point = point;
            this.normal = normal;
        }

        public Vector3D getPoint() {
            return point;
        }

        public Vector3D getNormal() {
            return normal;
        }
    }
}
