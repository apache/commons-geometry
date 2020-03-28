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

import java.util.concurrent.TimeUnit;

import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.shapes.Sphere;
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

/** Benchmarks for the {@link Sphere} class.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"-server", "-Xms512M", "-Xmx512M"})
public class SpherePerformance {

    /** Precision epsilon value. */
    private static final double EPS = 1e-10;

    /** Minimum value for random doubles. */
    private static final double MIN_VALUE = 1e-1;

    /** Maximum value for random doubles. */
    private static final double MAX_VALUE = 1e-100;

    /** Benchmark input providing a source of random {@link Sphere} instances.
     */
    @State(Scope.Thread)
    public static class RandomSphere {

        /** The sphere instance for the benchmark iteration. */
        private Sphere sphere;

        /** Set up the instance for the benchmark. */
        @Setup(Level.Iteration)
        public void setup() {
            sphere = randomSphere(RandomSource.create(RandomSource.XO_RO_SHI_RO_128_PP));
        }

        /** Get the input sphere.
         * @return the input sphere
         */
        public Sphere getSphere() {
            return sphere;
        }
    }

    /** Class defining input values to the {@link Sphere#toTree(int, int)} method.
     */
    @State(Scope.Thread)
    public static class ToTreeInput {

        /** The number of "stacks" and "slices" in the sphere approximation. */
        @Param({"10", "25", "50"})
        private int size;

        /** Get the number of stacks and slices to use for the sphere approximation.
         * @return the number of stacks and slices to use for the sphere approximation
         */
        public int getSize() {
            return size;
        }
    }

    /** Input class providing a pre-computed bsp tree sphere approximation.
     */
    @State(Scope.Thread)
    public static class ToTreeInstance extends ToTreeInput {

        /** The bsp tree input instance. */
        private RegionBSPTree3D tree;

        /** Set up the instance for the benchmark. */
        @Setup(Level.Iteration)
        public void setup() {
            final Sphere sphere = randomSphere(RandomSource.create(RandomSource.XO_RO_SHI_RO_128_PP));

            tree = sphere.toTree(getSize(), getSize());
        }

        /** Get the computed bsp tree sphere approximation.
         * @return the computed bsp tree sphere approximation
         */
        public RegionBSPTree3D getTree() {
            return tree;
        }
    }

    /** Create a random sphere using the given random provider. The double values
     * in the sphere are between {@link #MIN_VALUE} and {@link #MAX_VALUE}.
     * @param rand random provider
     * @return a sphere with random parameters.
     */
    private static Sphere randomSphere(final UniformRandomProvider rand) {
        final Vector3D center = Vector3D.of(nextDouble(rand), nextDouble(rand), nextDouble(rand));
        final double radius = nextDouble(rand);

        return Sphere.from(center, radius, new EpsilonDoublePrecisionContext(EPS));
    }

    /** Return a random double bounded by {@link #MAX_VALUE} and {@link #MIN_VALUE}.
     * @param rand random provider
     * @return a random double value
     */
    private static double nextDouble(final UniformRandomProvider rand) {
        return (rand.nextDouble() * (MAX_VALUE - MIN_VALUE)) + MIN_VALUE;
    }

    /** Benchmark testing the performance of the {@link Sphere#toTree(int, int)} method.
     * @param randomSphere sphere input
     * @param toTreeInput toTree input parameters
     * @return created bsp tree
     */
    @Benchmark
    public RegionBSPTree3D toTreeCreation(final RandomSphere randomSphere, final ToTreeInput toTreeInput) {
        final Sphere sphere = randomSphere.getSphere();
        final int size = toTreeInput.getSize();

        return sphere.toTree(size, size);
    }

    /** Benchmark testing the performance of the computation of the size of the bsp trees
     * created by the {@link Sphere#toTree(int, int)} method.
     * @param toTreeInstance bsp tree sphere approximation instance
     * @return the size (volume) of the region represented by the tree
     */
    @Benchmark
    public double toTreeSize(final ToTreeInstance toTreeInstance) {
        final RegionBSPTree3D tree = toTreeInstance.getTree();

        return tree.getSize();
    }
}
