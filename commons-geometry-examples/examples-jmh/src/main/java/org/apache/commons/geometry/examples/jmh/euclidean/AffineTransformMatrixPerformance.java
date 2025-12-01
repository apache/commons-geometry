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
package org.apache.commons.geometry.examples.jmh.euclidean;

import java.util.concurrent.TimeUnit;

import org.apache.commons.geometry.euclidean.oned.AffineTransformMatrix1D;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.threed.AffineTransformMatrix3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.twod.AffineTransformMatrix2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.geometry.examples.jmh.BenchmarkUtils;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/** Benchmarks for
 * {@link org.apache.commons.geometry.euclidean.AbstractAffineTransformMatrix AbstractAffineTransformMatrix}
 * subclasses.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"-server", "-Xms512M", "-Xmx512M"})
public class AffineTransformMatrixPerformance {

    /** Input class providing random arrays of double values for transformation.
     */
    @State(Scope.Thread)
    public static class TransformArrayInput {

        /** The number of elements in the input array. */
        @Param({"6000", "600000"})
        private int size;

        /** Array containing the input elements. */
        private double[] array;

        /** Get the configured size of the input array.
         * @return the configured size of the input array
         */
        public int getSize() {
            return size;
        }

        /** Get the input array.
         * @return input array
         */
        public double[] getArray() {
            return array;
        }

        /** Set up the input array.
         */
        @Setup(Level.Iteration)
        public void setup() {
            final UniformRandomProvider rand = RandomSource.XO_RO_SHI_RO_128_PP.create();

            array = new double[size];

            for (int i = 0; i < array.length; ++i) {
                array[i] = BenchmarkUtils.randomDouble(rand);
            }
        }
    }

    /** Input class providing a 1D transform matrix.
     */
    @State(Scope.Thread)
    public static class TransformMatrixInput1D {

        /** Input transform matrix. */
        private AffineTransformMatrix1D transform;

        /** Get the input transform matrix.
         * @return the input transform matrix
         */
        public AffineTransformMatrix1D getTransform() {
            return transform;
        }

        /** Set up the input. */
        @Setup
        public void setup() {
            final UniformRandomProvider rand = RandomSource.XO_RO_SHI_RO_128_PP.create();

            transform = AffineTransformMatrix1D.of(BenchmarkUtils.randomDoubleArray(2, rand));
        }
    }

    /** Input class providing a 2D transform matrix.
     */
    @State(Scope.Thread)
    public static class TransformMatrixInput2D {

        /** Input transform matrix. */
        private AffineTransformMatrix2D transform;

        /** Get the input transform matrix.
         * @return the input transform matrix
         */
        public AffineTransformMatrix2D getTransform() {
            return transform;
        }

        /** Set up the input. */
        @Setup
        public void setup() {
            final UniformRandomProvider rand = RandomSource.XO_RO_SHI_RO_128_PP.create();

            transform = AffineTransformMatrix2D.of(BenchmarkUtils.randomDoubleArray(6, rand));
        }
    }

    /** Input class providing a 3D transform matrix.
     */
    @State(Scope.Thread)
    public static class TransformMatrixInput3D {

        /** Input transform matrix. */
        private AffineTransformMatrix3D transform;

        /** Get the input transform matrix.
         * @return the input transform matrix
         */
        public AffineTransformMatrix3D getTransform() {
            return transform;
        }

        /** Set up the input. */
        @Setup
        public void setup() {
            final UniformRandomProvider rand = RandomSource.XO_RO_SHI_RO_128_PP.create();

            transform = AffineTransformMatrix3D.of(BenchmarkUtils.randomDoubleArray(12, rand));
        }
    }

    /** Baseline benchmark for 1D transforms on array data.
     * @param arrayInput array input
     * @return transformed output
     */
    @Benchmark
    public double[] baselineArray1D(final TransformArrayInput arrayInput) {
        final double[] arr = arrayInput.getArray();

        double x;
        for (int i = 0; i < arr.length; ++i) {
            x = arr[i];

            arr[i] = x + 1;
        }

        return arr;
    }

    /** Benchmark testing the performance of transforming an array of doubles by converting each group
     * to a Vector1D.
     * @param arrayInput array input
     * @param transformInput transform input
     * @return transformed output
     */
    @Benchmark
    public double[] transformArrayAsVectors1D(final TransformArrayInput arrayInput,
            final TransformMatrixInput1D transformInput) {
        final double[] arr = arrayInput.getArray();
        final AffineTransformMatrix1D t = transformInput.getTransform();

        Vector1D in;
        Vector1D out;
        for (int i = 0; i < arr.length; ++i) {
            in = Vector1D.of(arr[i]);

            out = t.apply(in);

            arr[i] = out.getX();
        }

        return arr;
    }

    /** Benchmark testing the performance of transforming an array of doubles by transforming
     * the components directly.
     * @param arrayInput array input
     * @param transformInput transform input
     * @return transformed output
     */
    @Benchmark
    public double[] transformArrayComponents1D(final TransformArrayInput arrayInput,
            final TransformMatrixInput1D transformInput) {
        final double[] arr = arrayInput.getArray();
        final AffineTransformMatrix1D t = transformInput.getTransform();

        double x;
        for (int i = 0; i < arr.length; ++i) {
            x = arr[i];

            arr[i] = t.applyX(x);
        }

        return arr;
    }

    /** Baseline benchmark for 2D transforms on array data.
     * @param arrayInput array input
     * @return transformed output
     */
    @Benchmark
    public double[] baselineArray2D(final TransformArrayInput arrayInput) {
        final double[] arr = arrayInput.getArray();

        double x;
        double y;
        for (int i = 0; i < arr.length; i += 2) {
            x = arr[i];
            y = arr[i + 1];

            arr[i] = x + 1;
            arr[i + 1] = y + 1;
        }

        return arr;
    }

    /** Benchmark testing the performance of transforming an array of doubles by converting each group
     * to a Vector2D.
     * @param arrayInput array input
     * @param transformInput transform input
     * @return transformed output
     */
    @Benchmark
    public double[] transformArrayAsVectors2D(final TransformArrayInput arrayInput,
            final TransformMatrixInput2D transformInput) {
        final double[] arr = arrayInput.getArray();
        final AffineTransformMatrix2D t = transformInput.getTransform();

        Vector2D in;
        Vector2D out;
        for (int i = 0; i < arr.length; i += 2) {
            in = Vector2D.of(
                    arr[i],
                    arr[i + 1]);

            out = t.apply(in);

            arr[i] = out.getX();
            arr[i + 1] = out.getY();
        }

        return arr;
    }

    /** Benchmark testing the performance of transforming an array of doubles by transforming
     * the components directly.
     * @param arrayInput array input
     * @param transformInput transform input
     * @return transformed output
     */
    @Benchmark
    public double[] transformArrayComponents2D(final TransformArrayInput arrayInput,
            final TransformMatrixInput2D transformInput) {
        final double[] arr = arrayInput.getArray();
        final AffineTransformMatrix2D t = transformInput.getTransform();

        double x;
        double y;
        for (int i = 0; i < arr.length; i += 2) {
            x = arr[i];
            y = arr[i + 1];

            arr[i] = t.applyX(x, y);
            arr[i + 1] = t.applyY(x, y);
        }

        return arr;
    }

    /** Baseline benchmark for 3D transforms on array data.
     * @param arrayInput array input
     * @return transformed output
     */
    @Benchmark
    public double[] baselineArray3D(final TransformArrayInput arrayInput) {
        final double[] arr = arrayInput.getArray();

        double x;
        double y;
        double z;
        for (int i = 0; i < arr.length; i += 3) {
            x = arr[i];
            y = arr[i + 1];
            z = arr[i + 2];

            arr[i] = x + 1;
            arr[i + 1] = y + 1;
            arr[i + 2] = z + 1;
        }

        return arr;
    }

    /** Benchmark testing the performance of transforming an array of doubles by converting each group
     * to a Vector3D.
     * @param arrayInput array input
     * @param transformInput transform input
     * @return transformed output
     */
    @Benchmark
    public double[] transformArrayAsVectors3D(final TransformArrayInput arrayInput,
            final TransformMatrixInput3D transformInput) {
        final double[] arr = arrayInput.getArray();
        final AffineTransformMatrix3D t = transformInput.getTransform();

        Vector3D in;
        Vector3D out;
        for (int i = 0; i < arr.length; i += 3) {
            in = Vector3D.of(
                    arr[i],
                    arr[i + 1],
                    arr[i + 2]);

            out = t.apply(in);

            arr[i] = out.getX();
            arr[i + 1] = out.getY();
            arr[i + 2] = out.getZ();
        }

        return arr;
    }

    /** Benchmark testing the performance of transforming an array of doubles by transforming
     * the components directly.
     * @param arrayInput array input
     * @param transformInput transform input
     * @return transformed output
     */
    @Benchmark
    public double[] transformArrayComponents3D(final TransformArrayInput arrayInput,
            final TransformMatrixInput3D transformInput) {
        final double[] arr = arrayInput.getArray();
        final AffineTransformMatrix3D t = transformInput.getTransform();

        double x;
        double y;
        double z;
        for (int i = 0; i < arr.length; i += 3) {
            x = arr[i];
            y = arr[i + 1];
            z = arr[i + 2];

            arr[i] = t.applyX(x, y, z);
            arr[i + 1] = t.applyY(x, y, z);
            arr[i + 2] = t.applyZ(x, y, z);
        }

        return arr;
    }
}
