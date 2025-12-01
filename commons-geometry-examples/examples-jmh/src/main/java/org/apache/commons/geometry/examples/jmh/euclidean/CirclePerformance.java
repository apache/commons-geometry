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

import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.geometry.euclidean.twod.shape.Circle;
import org.apache.commons.numbers.core.Precision;
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

/** Benchmarks for the {@link Circle} class.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"-server", "-Xms512M", "-Xmx512M"})
public class CirclePerformance {

    /** Precision epsilon value. */
    private static final double EPS = 1e-10;

    /** Minimum value for random doubles. */
    private static final double MIN_VALUE = 1e-1;

    /** Maximum value for random doubles. */
    private static final double MAX_VALUE = 1e-100;

    /** Benchmark input providing a source of random {@link Circle} instances.
     */
    @State(Scope.Thread)
    public static class RandomCircle {

        /** The circle instance for the benchmark iteration. */
        private Circle circle;

        /** Set up the instance for the benchmark. */
        @Setup(Level.Iteration)
        public void setup() {
            circle = randomCircle(RandomSource.XO_RO_SHI_RO_128_PP.create());
        }

        /** Get the input circle.
         * @return the input circle
         */
        public Circle getCircle() {
            return circle;
        }
    }

    /** Class defining input values to the {@link Circle#toTree(int)} method.
     */
    @State(Scope.Thread)
    public static class ToTreeInput {

        /** The number of segments in the circle approximation. */
        @Param({"10", "100", "1000"})
        private int segments;

        /** Get the number of segments to use for the circle approximation.
         * @return the number of segments to use for the circle approximation
         */
        public int getSegments() {
            return segments;
        }
    }

    /** Input class providing a pre-computed bsp tree circle approximation.
     */
    @State(Scope.Thread)
    public static class ToTreeInstance extends ToTreeInput {

        /** The bsp tree input instance. */
        private RegionBSPTree2D tree;

        /** Set up the instance for the benchmark. */
        @Setup(Level.Iteration)
        public void setup() {
            final Circle circle = randomCircle(RandomSource.XO_RO_SHI_RO_128_PP.create());

            tree = circle.toTree(getSegments());
        }

        /** Get the computed bsp tree circle approximation.
         * @return the computed bsp tree circle approximation
         */
        public RegionBSPTree2D getTree() {
            return tree;
        }
    }

    /** Create a random circle using the given random provider. The double values
     * in the circle are between {@link #MIN_VALUE} and {@link #MAX_VALUE}.
     * @param rand random provider
     * @return a circle with random parameters.
     */
    private static Circle randomCircle(final UniformRandomProvider rand) {
        final Vector2D center = Vector2D.of(nextDouble(rand), nextDouble(rand));
        final double radius = nextDouble(rand);

        return Circle.from(center, radius, Precision.doubleEquivalenceOfEpsilon(EPS));
    }

    /** Return a random double bounded by {@link #MAX_VALUE} and {@link #MIN_VALUE}.
     * @param rand random provider
     * @return a random double value
     */
    private static double nextDouble(final UniformRandomProvider rand) {
        return (rand.nextDouble() * (MAX_VALUE - MIN_VALUE)) + MIN_VALUE;
    }

    /** Benchmark testing the performance of the {@link Circle#toTree(int)} method.
     * @param randomCircle circle input
     * @param toTreeInput toTree input parameters
     * @return created bsp tree
     */
    @Benchmark
    public RegionBSPTree2D toTreeCreation(final RandomCircle randomCircle, final ToTreeInput toTreeInput) {
        final Circle circle = randomCircle.getCircle();
        final int segments = toTreeInput.getSegments();

        return circle.toTree(segments);
    }

    /** Benchmark testing the performance of the computation of the size of the bsp trees
     * created by the {@link Circle#toTree(int)} method.
     * @param toTreeInstance bsp tree circle approximation instance
     * @return the size (area) of the region represented by the tree
     */
    @Benchmark
    public double toTreeSize(final ToTreeInstance toTreeInstance) {
        final RegionBSPTree2D tree = toTreeInstance.getTree();

        return tree.getSize();
    }
}
