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
package org.apache.commons.geometry.examples.jmh.euclidean;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.apache.commons.geometry.core.Vector;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Benchmarks for the Euclidean vector classes.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"-server", "-Xms512M", "-Xmx512M"})
public class VectorPerformance {

    /**
     * An array of edge numbers that will produce edge case results from functions:
     * {@code +/-inf, +/-max, +/-min, +/-0, nan}.
     */
    private static final double[] EDGE_NUMBERS = {
        Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.MAX_VALUE,
        -Double.MAX_VALUE, Double.MIN_VALUE, -Double.MIN_VALUE, 0.0, -0.0, Double.NaN
    };

    /** String constant used to request random double values. */
    private static final String RANDOM = "random";

    /** String constant used to request a set of double values capable of normalization. */
    private static final String NORMALIZABLE = "normalizable";

    /** String constant used to request edge-case double values. */
    private static final String EDGE = "edge";

    /** Base class for vector inputs.
     * @param <V> Vector implementation type
     */
    public abstract static class VectorInputBase<V extends Vector<V>> {

        /** The dimension of the vector. */
        private final int dimension;

        /** The vector for the instance. */
        private V vector;

        /** Create a new instance with the vector dimension.
         * @param dimension vector dimension
         */
        VectorInputBase(final int dimension) {
            this.dimension = dimension;
        }

        /** Set up the instance for the benchmark.
         */
        @Setup
        public void setup() {
            final String type = getType();

            double[] values = new double[dimension];
            for (int i = 0; i < dimension; ++i) {
                values[i] = randomDouble(type);
            }

            vector = createVector(values);
        }

        /** Get the vector for the instance.
         * @return the vector for the instance
         */
        public V getVector() {
            return vector;
        }

        /** Get the type of double values to use in the creation of the vector.
         * @return the type of double values to use in the creation of the vector
         */
        public abstract String getType();

        /** Create a vector from an array of values.
         * @param arr array of values to place in the vector
         * @return the new vector
         */
        public abstract V createVector(double[] arr);
    }

    /** Vector input class producing {@link Vector1D} instances with random
     * double values.
     */
    @State(Scope.Thread)
    public static class VectorInput1D extends VectorInputBase<Vector1D> {

        /** The type of values to use in the vector. */
        @Param({RANDOM, EDGE})
        private String type;

        /** Default constructor. */
        public VectorInput1D() {
            super(1);
        }

        /** {@inheritDoc} */
        @Override
        public String getType() {
            return type;
        }

        /** {@inheritDoc} */
        @Override
        public Vector1D createVector(double[] arr) {
            return Vector1D.of(arr[0]);
        }
    }

    /** Vector input class producing {@link Vector1D} instances capable of being normalized.
     */
    @State(Scope.Thread)
    public static class NormalizableVectorInput1D extends VectorInputBase<Vector1D> {

        /** Default constructor. */
        public NormalizableVectorInput1D() {
            super(1);
        }

        /** {@inheritDoc} */
        @Override
        public String getType() {
            return NORMALIZABLE;
        }

        /** {@inheritDoc} */
        @Override
        public Vector1D createVector(double[] arr) {
            return Vector1D.of(arr[0]);
        }
    }

    /** Vector input class producing {@link Vector2D} instances with random
     * double values.
     */
    @State(Scope.Thread)
    public static class VectorInput2D extends VectorInputBase<Vector2D> {

        /** The type of values to use in the vector. */
        @Param({RANDOM, EDGE})
        private String type;

        /** Default constructor. */
        public VectorInput2D() {
            super(2);
        }

        /** {@inheritDoc} */
        @Override
        public String getType() {
            return type;
        }

        /** {@inheritDoc} */
        @Override
        public Vector2D createVector(double[] arr) {
            return Vector2D.of(arr);
        }
    }

    /** Vector input class producing {@link Vector2D} instances capable of being normalized.
     */
    @State(Scope.Thread)
    public static class NormalizableVectorInput2D extends VectorInputBase<Vector2D> {

        /** Default constructor. */
        public NormalizableVectorInput2D() {
            super(2);
        }

        /** {@inheritDoc} */
        @Override
        public String getType() {
            return NORMALIZABLE;
        }

        /** {@inheritDoc} */
        @Override
        public Vector2D createVector(double[] arr) {
            return Vector2D.of(arr);
        }
    }

    /** Vector input class producing {@link Vector2D} instances with random
     * double values.
     */
    @State(Scope.Thread)
    public static class VectorInput3D extends VectorInputBase<Vector3D> {

        /** The type of values to use in the vector. */
        @Param({RANDOM, EDGE})
        private String type;

        /** Default constructor. */
        public VectorInput3D() {
            super(3);
        }

        /** {@inheritDoc} */
        @Override
        public String getType() {
            return type;
        }

        /** {@inheritDoc} */
        @Override
        public Vector3D createVector(double[] arr) {
            return Vector3D.of(arr);
        }
    }

    /** Vector input class producing {@link Vector3D} instances capable of being normalized.
     */
    @State(Scope.Thread)
    public static class NormalizableVectorInput3D extends VectorInputBase<Vector3D> {

        /** Default constructor. */
        public NormalizableVectorInput3D() {
            super(3);
        }

        /** {@inheritDoc} */
        @Override
        public String getType() {
            return NORMALIZABLE;
        }

        /** {@inheritDoc} */
        @Override
        public Vector3D createVector(double[] arr) {
            return Vector3D.of(arr);
        }
    }

    /** Create a random double value of the given type.
     * @param type type of value to produce
     * @return a random double value of the given type
     */
    private static double randomDouble(final String type) {
        Random rng = getRandom();

        switch (type) {
        case RANDOM:
            return createRandomDouble(rng);
        case NORMALIZABLE:
            return createRandomNonZeroDouble(rng);
        case EDGE:
            return EDGE_NUMBERS[rng.nextInt(EDGE_NUMBERS.length)];
        default:
            throw new IllegalStateException("Invalid number type: " + type);
        }
    }

    /** Get a {@link Random} instance for use in the benchmark.
     * @return a Random instance suitable for use in the benchmark
     */
    private static Random getRandom() {
        return ThreadLocalRandom.current();
    }

    /** Creates a random double number with a random sign and mantissa and a large range for
     * the exponent. The numbers will not be uniform over the range.
     * @param rng random number generator
     * @return the random number
     */
    private static double createRandomDouble(final Random rng) {
        // Create random doubles using random bits in the sign bit and the mantissa.
        // Then create an exponent in the range -64 to 64. Thus the sum product
        // of 4 max or min values will not over or underflow.
        final long mask = ((1L << 52) - 1) | 1L << 63;
        final long bits = rng.nextLong() & mask;
        // The exponent must be unsigned so + 1023 to the signed exponent
        final long exp = rng.nextInt(129) - 64 + 1023;
        return Double.longBitsToDouble(bits | (exp << 52));
    }

    /** Create a random double that is guaranteed to not equal zero.
     * @param rng random number generator
     * @return the random, non-zero number
     */
    private static double createRandomNonZeroDouble(final Random rng) {
        double num = createRandomDouble(rng);
        if (Math.abs(num) == 0.0) {
            // simply add an offset if exactly zero
            num += 0.1;
        }
        return num;
    }

    /** Benchmark testing the performance of the {@link Vector1D#norm()} method.
     * @param input benchmark state input
     * @return vector norm
     */
    @Benchmark
    public double norm1D(final VectorInput1D input) {
        return input.getVector().norm();
    }

    /** Benchmark testing the performance of the {@link Vector2D#norm()} method.
     * @param input benchmark state input
     * @return vector norm
     */
    @Benchmark
    public double norm2D(final VectorInput2D input) {
        return input.getVector().norm();
    }

    /** Benchmark testing the performance of the {@link Vector3D#norm()} method.
     * @param input benchmark state input
     * @return vector norm
     */
    @Benchmark
    public double norm3D(final VectorInput3D input) {
        return input.getVector().norm();
    }

    /** Benchmark testing the performance of the {@link Vector1D#normalize()} method.
     * @param input benchmark state input
     * @return normalized vector
     */
    @Benchmark
    public Vector1D normalize1D(final NormalizableVectorInput1D input) {
        return input.getVector().normalize();
    }


    /** Benchmark testing the performance of the {@link Vector2D#normalize()}
     * method.
     * @param input benchmark state input
     * @return normalized vector
     */
    @Benchmark
    public Vector2D normalize2D(final NormalizableVectorInput2D input) {
        return input.getVector().normalize();
    }

    /** Benchmark testing the performance of the {@link Vector3D#normalize()}
     * method.
     * @param input benchmark state input
     * @return normalized vector
     */
    @Benchmark
    public Vector3D normalize3D(final NormalizableVectorInput3D input) {
        return input.getVector().normalize();
    }
}
