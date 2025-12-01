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
package org.apache.commons.geometry.spherical;

import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.oned.Point1S;
import org.apache.commons.geometry.spherical.twod.Point2S;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;

/** Class containing various test utilities for spherical space.
 */
public final class SphericalTestUtils {

    /** Private constructor. */
    private SphericalTestUtils() {}

    /** Assert that the given points are equal, using the specified tolerance value.
     * @param expected
     * @param actual
     * @param tolerance
     */
    public static void assertPointsEqual(final Point1S expected, final Point1S actual, final double tolerance) {
        final String msg = "Expected point to equal " + expected + " but was " + actual + ";";
        Assertions.assertEquals(expected.getAzimuth(), actual.getAzimuth(), tolerance, msg);
    }

    /** Assert that the given points are equal, using the specified tolerance value.
     * @param expected
     * @param actual
     * @param tolerance
     */
    public static void assertPointsEqual(final Point2S expected, final Point2S actual, final double tolerance) {
        final String msg = "Expected point to equal " + expected + " but was " + actual + ";";
        Assertions.assertEquals(expected.getAzimuth(), actual.getAzimuth(), tolerance, msg);
        Assertions.assertEquals(expected.getPolar(), actual.getPolar(), tolerance, msg);
    }

    /** Assert that the given points are equivalent, using the specified tolerance value.
     * @param expected
     * @param actual
     * @param tolerance
     */
    public static void assertPointsEq(final Point2S expected, final Point2S actual, final double tolerance) {
        final String msg = "Expected point to be equivalent to " + expected + " but was " + actual + ";";
        Assertions.assertTrue(expected.eq(actual, Precision.doubleEquivalenceOfEpsilon(tolerance)), msg);
    }

    /** Assert that the given vectors are equal, using the specified tolerance value.
     * @param expected
     * @param actual
     * @param tolerance
     */
    public static void assertVectorsEqual(final Vector3D expected, final Vector3D actual, final double tolerance) {
        final String msg = "Expected vector to equal " + expected + " but was " + actual + ";";
        Assertions.assertEquals(expected.getX(), actual.getX(), tolerance, msg);
        Assertions.assertEquals(expected.getY(), actual.getY(), tolerance, msg);
        Assertions.assertEquals(expected.getZ(), actual.getZ(), tolerance, msg);
    }

    /** Assert that the given points lie in the specified location relative to the region.
     * @param region region to test
     * @param loc expected location of the given points
     * @param pts points to test
     */
    public static void checkClassify(final Region<Point2S> region, final RegionLocation loc, final Point2S... pts) {
        for (final Point2S pt : pts) {
            Assertions.assertEquals(loc, region.classify(pt), "Unexpected location for point " + pt);
        }
    }
}
