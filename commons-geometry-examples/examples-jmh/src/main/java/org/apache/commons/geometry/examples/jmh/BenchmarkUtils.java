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
package org.apache.commons.geometry.examples.jmh;

import org.apache.commons.rng.UniformRandomProvider;

/** Class containing static utility methods for performance benchmarks.
 */
public final class BenchmarkUtils {

    /** Utility class; no instantiation. */
    private BenchmarkUtils() {}

    /** Creates a random double number with a random sign and mantissa and a large range for
     * the exponent. The numbers will not be uniform over the range.
     * @param rng random number generator
     * @return the random number
     */
    public static double randomDouble(final UniformRandomProvider rng) {
        // Create random doubles using random bits in the sign bit and the mantissa.
        // Then create an exponent in the range -64 to 64. Thus the sum product
        // of 4 max or min values will not over or underflow.
        final long mask = ((1L << 52) - 1) | 1L << 63;
        final long bits = rng.nextLong() & mask;
        // The exponent must be unsigned so + 1023 to the signed exponent
        final long exp = rng.nextInt(129) - 64 + 1023;
        return Double.longBitsToDouble(bits | (exp << 52));
    }

    /** Create an array of doubles populated using {@link #randomDouble(UniformRandomProvider)}.
     * @param rng uniform random provider
     * @param len array length
     * @return array containing {@code len} random doubles
     */
    public static double[] randomDoubleArray(final UniformRandomProvider rng, final int len) {
        final double[] arr = new double[len];

        for (int i = 0; i < arr.length; ++i) {
            arr[i] = randomDouble(rng);
        }

        return arr;
    }
}
