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
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;

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
    public static void permute(final double min, final double max, final double step, final PermuteCallback2D callback) {
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
    public static void permuteSkipZero(final double min, final double max, final double step, final PermuteCallback2D callback) {
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
    private static void permuteInternal(final double min, final double max, final double step, final boolean skipZero, final PermuteCallback2D callback) {
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
    public static void permute(final double min, final double max, final double step, final PermuteCallback3D callback) {
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
    public static void permuteSkipZero(final double min, final double max, final double step, final PermuteCallback3D callback) {
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
    private static void permuteInternal(final double min, final double max, final double step, final boolean skipZero, final PermuteCallback3D callback) {
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
    public static void assertCoordinatesEqual(final Vector1D expected, final Vector1D actual, final double tolerance) {
        final String msg = "Expected coordinates to equal " + expected + " but was " + actual + ";";
        Assertions.assertEquals(expected.getX(), actual.getX(), tolerance, msg);
    }

    /**
     * Asserts that corresponding values in the given vectors are equal, using the
     * specified tolerance value.
     *
     * @param expected
     * @param actual
     * @param tolerance
     */
    public static void assertCoordinatesEqual(final Vector2D expected, final Vector2D actual, final double tolerance) {
        final String msg = "Expected coordinates to equal " + expected + " but was " + actual + ";";
        Assertions.assertEquals(expected.getX(), actual.getX(), tolerance, msg);
        Assertions.assertEquals(expected.getY(), actual.getY(), tolerance, msg);
    }

    /**
     * Asserts that corresponding values in the given vectors are equal, using the
     * specified tolerance value.
     *
     * @param expected
     * @param actual
     * @param tolerance
     */
    public static void assertCoordinatesEqual(final Vector3D expected, final Vector3D actual, final double tolerance) {
        final String msg = "Expected coordinates to equal " + expected + " but was " + actual + ";";
        Assertions.assertEquals(expected.getX(), actual.getX(), tolerance, msg);
        Assertions.assertEquals(expected.getY(), actual.getY(), tolerance, msg);
        Assertions.assertEquals(expected.getZ(), actual.getZ(), tolerance, msg);
    }

    /**
     * Asserts that the given value is positive infinity.
     *
     * @param value
     */
    public static void assertPositiveInfinity(final double value) {
        final String msg = "Expected value to be positive infinity but was " + value;
        Assertions.assertTrue(Double.isInfinite(value), msg);
        Assertions.assertTrue(value > 0, msg);
    }

    /**
     * Assert that the given lists represent equivalent vertex loops. The loops must contain the same sequence
     * of vertices but do not need to start at the same point.
     * @param <V> Vector implementation type
     * @param expected
     * @param actual
     * @param precision
     */
    public static <V extends EuclideanVector<V>> void assertVertexLoopSequence(final List<V> expected, final List<V> actual,
                                                                               final Precision.DoubleEquivalence precision) {
        Assertions.assertEquals(expected.size(), actual.size(), "Vertex sequences have different sizes");

        if (!expected.isEmpty()) {

            int offset = -1;
            final V start = expected.get(0);
            for (int i = 0; i < actual.size(); ++i) {
                if (actual.get(i).eq(start, precision)) {
                    offset = i;
                    break;
                }
            }

            if (offset < 0) {
                Assertions.fail("Vertex loops do not share any points: expected " + expected + " but was " + actual);
            }

            V expectedVertex;
            V actualVertex;
            for (int i = 0; i < expected.size(); ++i) {
                expectedVertex = expected.get(i);
                actualVertex = actual.get((i + offset) % actual.size());

                if (!expectedVertex.eq(actualVertex, precision)) {
                    Assertions.fail("Unexpected vertex at index " + i + ": expected " + expectedVertex +
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
    public static void assertNegativeInfinity(final double value) {
        final String msg = "Expected value to be negative infinity but was " + value;
        Assertions.assertTrue(Double.isInfinite(value), msg);
        Assertions.assertTrue(value < 0, msg);
    }

    /** Assert that all of the given points lie within the specified location relative to
     * {@code region}.
     * @param region
     * @param loc
     * @param pts
     */
    public static void assertRegionLocation(final Region<Vector1D> region, final RegionLocation loc, final Vector1D... pts) {
        for (final Vector1D pt : pts) {
            Assertions.assertEquals(loc, region.classify(pt), "Unexpected region location for point " + pt);
        }
    }

    /** Assert that all of the given points lie within the specified location relative to
     * {@code region}.
     * @param region
     * @param loc
     * @param pts
     */
    public static void assertRegionLocation(final Region<Vector2D> region, final RegionLocation loc, final Vector2D... pts) {
        for (final Vector2D pt : pts) {
            Assertions.assertEquals(loc, region.classify(pt), "Unexpected region location for point " + pt);
        }
    }

    /** Assert that all of the given points lie within the specified location relative to
     * {@code region}.
     * @param region
     * @param loc
     * @param pts
     */
    public static void assertRegionLocation(final Region<Vector3D> region, final RegionLocation loc, final Vector3D... pts) {
        for (final Vector3D pt : pts) {
            Assertions.assertEquals(loc, region.classify(pt), "Unexpected region location for point " + pt);
        }
    }

    /** Assert that all of the given points lie within the specified location relative to {@code sub}.
     * @param sub
     * @param loc
     * @param pts
     */
    public static void assertRegionLocation(final HyperplaneSubset<Vector1D> sub, final RegionLocation loc, final Vector1D... pts) {
        for (final Vector1D pt : pts) {
            Assertions.assertEquals(loc, sub.classify(pt), "Unexpected region location for point " + pt);
        }
    }

    /** Assert that all of the given points lie within the specified location relative to {@code sub}.
     * @param sub
     * @param loc
     * @param pts
     */
    public static void assertRegionLocation(final HyperplaneSubset<Vector2D> sub, final RegionLocation loc, final Vector2D... pts) {
        for (final Vector2D pt : pts) {
            Assertions.assertEquals(loc, sub.classify(pt), "Unexpected region location for point " + pt);
        }
    }

    /** Assert that all of the given points lie within the specified location relative to {@code sub}.
     * @param sub
     * @param loc
     * @param pts
     */
    public static void assertRegionLocation(final HyperplaneSubset<Vector3D> sub, final RegionLocation loc, final Vector3D... pts) {
        for (final Vector3D pt : pts) {
            Assertions.assertEquals(loc, sub.classify(pt), "Unexpected region location for point " + pt);
        }
    }
}
