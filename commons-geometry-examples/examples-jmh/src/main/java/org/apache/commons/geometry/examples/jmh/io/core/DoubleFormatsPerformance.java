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
package org.apache.commons.geometry.examples.jmh.io.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleFunction;

import org.apache.commons.geometry.examples.jmh.BenchmarkUtils;
import org.apache.commons.geometry.io.core.utils.DoubleFormats;
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
import org.openjdk.jmh.infra.Blackhole;

/** Benchmarks for the {@link DoubleFormats} class.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"-server", "-Xms512M", "-Xmx512M"})
public class DoubleFormatsPerformance {

    /** Benchmark input providing a source of random double values. */
    @State(Scope.Thread)
    public static class DoubleInput {

        /** The number of doubles in the input array. */
        @Param({"10000"})
        private int size;

        /** Minimum base 2 exponent for random input doubles. */
        @Param("-20")
        private int minExp;

        /** Maximum base 2 exponent for random input doubles. */
        @Param("20")
        private int maxExp;

        /** Double input array. */
        private double[] input;

        /** Get the input doubles.
         * @return the input doubles
         */
        public double[] getInput() {
            return input;
        }

        /** Set up the instance for the benchmark. */
        @Setup(Level.Iteration)
        public void setup() {
            input = BenchmarkUtils.randomDoubleArray(size, minExp, maxExp,
                    RandomSource.create(RandomSource.XO_RO_SHI_RO_128_PP));
        }
    }

    /** Run a benchmark test on a function accepting a double argument.
     * @param <T> function output type
     * @param input double array
     * @param bh jmh blackhole for consuming output
     * @param fn function to call
     */
    private static <T> void runDoubleFunction(final DoubleInput input, final Blackhole bh,
            final DoubleFunction<T> fn) {
        for (final double d : input.getInput()) {
            bh.consume(fn.apply(d));
        }
    }

    /** Benchmark testing just the overhead of the benchmark harness.
     * @param input benchmark state input
     * @param bh jmh blackhole for consuming output
     */
    @Benchmark
    public void baseline(final DoubleInput input, final Blackhole bh) {
        runDoubleFunction(input, bh, d -> "");
    }

    /** Benchmark testing the {@link Double#toString()} method.
     * @param input benchmark state input
     * @param bh jmh blackhole for consuming output
     */
    @Benchmark
    public void doubleToString(final DoubleInput input, final Blackhole bh) {
        runDoubleFunction(input, bh, Double::toString);
    }

    /** Benchmark testing the {@link String#format(String, Object...)} method.
     * @param input benchmark state input
     * @param bh jmh blackhole for consuming output
     */
    @Benchmark
    public void stringFormat(final DoubleInput input, final Blackhole bh) {
        runDoubleFunction(input, bh, d -> String.format("%d", d));
    }

    /** Benchmark testing the BigDecimal formatting performance.
     * @param input benchmark state input
     * @param bh jmh blackhole for consuming output
     */
    @Benchmark
    public void bigDecimal(final DoubleInput input, final Blackhole bh) {
        final DoubleFunction<String> fn = d -> BigDecimal.valueOf(d)
                .setScale(3, RoundingMode.HALF_EVEN)
                .stripTrailingZeros()
                .toString();
        runDoubleFunction(input, bh, fn);
    }

    /** Benchmark testing the {@link DecimalFormat} class.
     * @param input benchmark state input
     * @param bh jmh blackhole for consuming output
     */
    @Benchmark
    public void decimalFormat(final DoubleInput input, final Blackhole bh) {
        final DecimalFormat fmt = new DecimalFormat("0.###");
        runDoubleFunction(input, bh, fmt::format);
    }

    /** Benchmark testing the {@link DoubleFormats#createDefault(int, int)} method.
     * @param input benchmark state input
     * @param bh jmh blackhole for consuming output
     */
    @Benchmark
    public void doubleFormatsDefault(final DoubleInput input, final Blackhole bh) {
        runDoubleFunction(input, bh, DoubleFormats.createDefault(0, -3));
    }

    /** Benchmark testing the {@link DoubleFormats#createPlain(int, int)} method.
     * @param input benchmark state input
     * @param bh jmh blackhole for consuming output
     */
    @Benchmark
    public void doubleFormatsPlain(final DoubleInput input, final Blackhole bh) {
        runDoubleFunction(input, bh, DoubleFormats.createPlain(0, -3));
    }

    /** Benchmark testing the {@link DoubleFormats#createScientific(int, int)} method.
     * @param input benchmark state input
     * @param bh jmh blackhole for consuming output
     */
    @Benchmark
    public void doubleFormatsScientific(final DoubleInput input, final Blackhole bh) {
        runDoubleFunction(input, bh, DoubleFormats.createScientific(0, -3));
    }

    /** Benchmark testing the {@link DoubleFormats#createEngineering(int, int)} method.
     * @param input benchmark state input
     * @param bh jmh blackhole for consuming output
     */
    @Benchmark
    public void doubleFormatsEngineering(final DoubleInput input, final Blackhole bh) {
        runDoubleFunction(input, bh, DoubleFormats.createEngineering(0, -3));
    }
}
