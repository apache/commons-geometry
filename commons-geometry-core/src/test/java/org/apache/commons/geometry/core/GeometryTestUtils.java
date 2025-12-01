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
package org.apache.commons.geometry.core;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

/** Class containing various geometry-related test utilities.
 */
public final class GeometryTestUtils {

    /** Utility class; no instantiation. */
    private GeometryTestUtils() {}

    /** Asserts that the given value is positive infinity.
     * @param value
     */
    public static void assertPositiveInfinity(final double value) {
        final String msg = "Expected value to be positive infinity but was " + value;
        Assertions.assertTrue(Double.isInfinite(value), msg);
        Assertions.assertTrue(value > 0, msg);
    }

    /** Asserts that the given value is negative infinity.
     * @param value
     */
    public static void assertNegativeInfinity(final double value) {
        final String msg = "Expected value to be negative infinity but was " + value;
        Assertions.assertTrue(Double.isInfinite(value), msg);
        Assertions.assertTrue(value < 0, msg);
    }

    /** Asserts that the Executable throws an exception matching the given type and message.
     * @param executable the Executable instance
     * @param exceptionType the expected exception type
     * @param message the expected exception message; may be null
     */
    public static <T extends Throwable> void assertThrowsWithMessage(final Executable executable,
            final Class<T> exceptionType, final String message) {
        Assertions.assertEquals(message, Assertions.assertThrows(exceptionType, executable).getMessage());
    }

    /** Asserts that the Executable throws an exception of the given type with a non-null message matching
     * the specified regex pattern.
     * @param executable the Executable instance
     * @param exceptionType the expected exception type
     * @param pattern regex pattern to match
     */
    public static <T extends Throwable> void assertThrowsWithMessage(final Executable executable,
            final Class<T> exceptionType, final Pattern pattern) {
        final String message = Assertions.assertThrows(exceptionType, executable).getMessage();
        Assertions.assertTrue(pattern.matcher(message).matches(),
                "Expected exception message to match /" + pattern + "/ but was [" + message + "]");
    }

    /** Assert that a string contains a given substring value.
     * @param substr
     * @param actual
     */
    public static void assertContains(final String substr, final String actual) {
        final String msg = "Expected string to contain [" + substr + "] but was [" + actual + "]";
        Assertions.assertTrue(actual.contains(substr), msg);
    }

    /** Assert that the {@code equals} method of the argument meets the following requirements:
     * <ol>
     *  <li>{@code obj} is not equal to null</li>
     *  <li>{@code obj} is not equal to an instance of a supertype ({@code java.lang.Object})</li>
     *  <li>{@code obj} is equal to itself</li>
     * </ol>
     * @param obj object to test the {@code equals} method of
     */
    public static void assertSimpleEqualsCases(final Object obj) {
        // Use the JUnit boolean assertions here to ensure that the equals methods are actually
        // invoked and no assertion shortcuts are taken

        Assertions.assertFalse(obj.equals(null), "Object should not equal null");

        if (obj.getClass().getSuperclass() != null) {
            Assertions.assertFalse(obj.equals(new Object()), "Object should not equal an instance of different type");
        }

        Assertions.assertTrue(obj.equals(obj), "Object should equal itself");
    }
}
