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

import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.HyperplaneSubset;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.junit.Assert;

/**
 * Class containing various Euclidean-related test utilities.
 */
public final class EuclideanTestUtils {

    // no instantiation
    private EuclideanTestUtils() {}

    /** Callback interface for {@link #permute(double, double, double, PermuteCallback2D)}. */
    @FunctionalInterface
    public interface PermuteCallback2D {
        void accept(double x, double y);
    }

    /** Callback interface for {@link #permute(double, double, double, PermuteCallback3D)} */
    @FunctionalInterface
    public interface PermuteCallback3D {
        void accept(double x, double y, double z);
    }

    /** Iterate through all {@code (x, y)} permutations for the given range of numbers and
     * call {@code callback} for each.
     *
     * @param min the minimum number in the range
     * @param max the maximum number in the range
     * @param step the step (increment) value for the range
     * @param callback callback to invoke for each permutation.
     */
    public static void permute(double min, double max, double step, PermuteCallback2D callback) {
        permuteInternal(min, max, step, false, callback);
    }

    /** Same as {@link #permute(double, double, double, PermuteCallback2D)} but skips the {@code (0, 0))}
     * permutation.
     *
     * @param min the minimum number in the range
     * @param max the maximum number in the range
     * @param step the step (increment) value for the range
     * @param callback callback to invoke for each permutation.
     */
    public static void permuteSkipZero(double min, double max, double step, PermuteCallback2D callback) {
        permuteInternal(min, max, step, true, callback);
    }

    /** Internal permutation method. Iterates through all {@code (x, y)} permutations for the given range
     * of numbers and calls {@code callback} for each.
     *
     * @param min the minimum number in the range
     * @param max the maximum number in the range
     * @param step the step (increment) value for the range
     * @param skipZero if true, the {@code (0, 0)} permutation will be skipped
     * @param callback callback to invoke for each permutation.
     */
    private static void permuteInternal(double min, double max, double step, boolean skipZero, PermuteCallback2D callback) {
        for (double x = min; x <= max; x += step) {
            for (double y = min; y <= max; y += step) {
                if (!skipZero || (x != 0.0 || y != 0.0)) {
                    callback.accept(x, y);
                }
            }
        }
    }

    /** Iterate through all {@code (x, y, z)} permutations for the given range of numbers and
     * call {@code callback} for each.
     *
     * @param min the minimum number in the range
     * @param max the maximum number in the range
     * @param step the step (increment) value for the range
     * @param callback callback to invoke for each permutation.
     */
    public static void permute(double min, double max, double step, PermuteCallback3D callback) {
        permuteInternal(min, max, step, false, callback);
    }

    /** Same as {@link #permute(double, double, double, PermuteCallback3D)} but skips the {@code (0, 0, 0)}
     * permutation.
     *
     * @param min the minimum number in the range
     * @param max the maximum number in the range
     * @param step the step (increment) value for the range
     * @param callback callback to invoke for each permutation.
     */
    public static void permuteSkipZero(double min, double max, double step, PermuteCallback3D callback) {
        permuteInternal(min, max, step, true, callback);
    }

    /** Internal permutation method. Iterates through all {@code (x, y)} permutations for the given range
     * of numbers and calls {@code callback} for each.
     *
     * @param min the minimum number in the range
     * @param max the maximum number in the range
     * @param step the step (increment) value for the range
     * @param skipZero if true, the {@code (0, 0, 0)} permutation will be skipped
     * @param callback callback to invoke for each permutation.
     */
    private static void permuteInternal(double min, double max, double step, boolean skipZero, PermuteCallback3D callback) {
        for (double x = min; x <= max; x += step) {
            for (double y = min; y <= max; y += step) {
                for (double z = min; z <= max; z += step) {
                    if (!skipZero || (x != 0.0 || y != 0.0 || z != 0.0)) {
                        callback.accept(x, y, z);
                    }
                }
            }
        }
    }

    /**
     * Asserts that corresponding values in the given vectors are equal, using the
     * specified tolerance value.
     *
     * @param expected
     * @param actual
     * @param tolerance
     */
    public static void assertCoordinatesEqual(Vector1D expected, Vector1D actual, double tolerance) {
        String msg = "Expected coordinates to equal " + expected + " but was " + actual + ";";
        Assert.assertEquals(msg, expected.getX(), actual.getX(), tolerance);
    }

    /**
     * Asserts that corresponding values in the given vectors are equal, using the
     * specified tolerance value.
     *
     * @param expected
     * @param actual
     * @param tolerance
     */
    public static void assertCoordinatesEqual(Vector2D expected, Vector2D actual, double tolerance) {
        String msg = "Expected coordinates to equal " + expected + " but was " + actual + ";";
        Assert.assertEquals(msg, expected.getX(), actual.getX(), tolerance);
        Assert.assertEquals(msg, expected.getY(), actual.getY(), tolerance);
    }

    /**
     * Asserts that corresponding values in the given vectors are equal, using the
     * specified tolerance value.
     *
     * @param expected
     * @param actual
     * @param tolerance
     */
    public static void assertCoordinatesEqual(Vector3D expected, Vector3D actual, double tolerance) {
        String msg = "Expected coordinates to equal " + expected + " but was " + actual + ";";
        Assert.assertEquals(msg, expected.getX(), actual.getX(), tolerance);
        Assert.assertEquals(msg, expected.getY(), actual.getY(), tolerance);
        Assert.assertEquals(msg, expected.getZ(), actual.getZ(), tolerance);
    }

    /**
     * Asserts that the given value is positive infinity.
     *
     * @param value
     */
    public static void assertPositiveInfinity(double value) {
        String msg = "Expected value to be positive infinity but was " + value;
        Assert.assertTrue(msg, Double.isInfinite(value));
        Assert.assertTrue(msg, value > 0);
    }

    /**
     * Assert that the given lists represent equivalent vertex loops. The loops must contain the same sequence
     * of vertices but do not need to start at the same point.
     * @param expected
     * @param actual
     * @param precision
     */
    public static void assertVertexLoopSequence(List<Vector3D> expected, List<Vector3D> actual,
            DoublePrecisionContext precision) {
        Assert.assertEquals("Vertex sequences have different sizes", expected.size(), actual.size());

        if (expected.size() > 0) {

            int offset = -1;
            Vector3D start = expected.get(0);
            for (int i = 0; i < actual.size(); ++i) {
                if (actual.get(i).eq(start, precision)) {
                    offset = i;
                    break;
                }
            }

            if (offset < 0) {
                Assert.fail("Vertex loops do not share any points: expected " + expected + " but was " + actual);
            }

            Vector3D expectedVertex;
            Vector3D actualVertex;
            for (int i = 0; i < expected.size(); ++i) {
                expectedVertex = expected.get(i);
                actualVertex = actual.get((i + offset) % actual.size());

                if (!expectedVertex.eq(actualVertex, precision)) {
                    Assert.fail("Unexpected vertex at index " + i + ": expected " + expectedVertex +
                            " but was " + actualVertex);
                }
            }
        }
    }

    /**
     * Asserts that the given value is negative infinity..
     *
     * @param value
     */
    public static void assertNegativeInfinity(double value) {
        String msg = "Expected value to be negative infinity but was " + value;
        Assert.assertTrue(msg, Double.isInfinite(value));
        Assert.assertTrue(msg, value < 0);
    }

    /** Assert that all of the given points lie within the specified location relative to
     * {@code region}.
     * @param region
     * @param loc
     * @param pts
     */
    public static void assertRegionLocation(Region<Vector1D> region, RegionLocation loc, Vector1D... pts) {
        for (Vector1D pt : pts) {
            Assert.assertEquals("Unexpected region location for point " + pt, loc, region.classify(pt));
        }
    }

    /** Assert that all of the given points lie within the specified location relative to
     * {@code region}.
     * @param region
     * @param loc
     * @param pts
     */
    public static void assertRegionLocation(Region<Vector2D> region, RegionLocation loc, Vector2D... pts) {
        for (Vector2D pt : pts) {
            Assert.assertEquals("Unexpected region location for point " + pt, loc, region.classify(pt));
        }
    }

    /** Assert that all of the given points lie within the specified location relative to
     * {@code region}.
     * @param region
     * @param loc
     * @param pts
     */
    public static void assertRegionLocation(Region<Vector3D> region, RegionLocation loc, Vector3D... pts) {
        for (Vector3D pt : pts) {
            Assert.assertEquals("Unexpected region location for point " + pt, loc, region.classify(pt));
        }
    }

    /** Assert that all of the given points lie within the specified location relative to {@code sub}.
     * @param sub
     * @param loc
     * @param pts
     */
    public static void assertRegionLocation(HyperplaneSubset<Vector1D> sub, RegionLocation loc, Vector1D... pts) {
        for (Vector1D pt : pts) {
            Assert.assertEquals("Unexpected region location for point " + pt, loc, sub.classify(pt));
        }
    }

    /** Assert that all of the given points lie within the specified location relative to {@code sub}.
     * @param sub
     * @param loc
     * @param pts
     */
    public static void assertRegionLocation(HyperplaneSubset<Vector2D> sub, RegionLocation loc, Vector2D... pts) {
        for (Vector2D pt : pts) {
            Assert.assertEquals("Unexpected region location for point " + pt, loc, sub.classify(pt));
        }
    }

    /** Assert that all of the given points lie within the specified location relative to {@code sub}.
     * @param sub
     * @param loc
     * @param pts
     */
    public static void assertRegionLocation(HyperplaneSubset<Vector3D> sub, RegionLocation loc, Vector3D... pts) {
        for (Vector3D pt : pts) {
            Assert.assertEquals("Unexpected region location for point " + pt, loc, sub.classify(pt));
        }
    }
}
