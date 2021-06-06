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
package org.apache.commons.geometry.io.core.utils;

import java.util.function.DoubleFunction;

/** Class containing static utility methods and constants for formatting double values
 * as strings. All instances returned by methods in this class are guaranteed to be
 * thread-safe.
 */
public final class DoubleFormats {

    /** Double format function that simply calls {@link Double#toString(double)}.
     */
    public static final DoubleFunction<String> DOUBLE_TO_STRING = Double::toString;

    /** Double format function that converts the argument to a float and calls
     * {@link Float#toString(float)}.
     */
    public static final DoubleFunction<String> FLOAT_TO_STRING = d -> Float.toString((float) d);

    /** Minimum possible decimal exponent for double values. */
    private static final int MIN_DOUBLE_EXPONENT = -325;

    /** Utility class; no instantiation. */
    private DoubleFormats() {}

    /** Return a double format function that provides similar behavior to {@link Double#toString(double)}
     * but with a configurable max precision. For values with an absolute magnitude less than
     * 10<sup>7</sup> and greater than or equal to 10<sup>-3</sup> (after any necessary rounding), the returned
     * string is in plain, non-scientific format. All other values are in scientific format. Rounding is performed
     * using {@link java.math.RoundingMode#HALF_EVEN half even} rounding.
     * <table>
     *  <caption>Format Examples</caption>
     *  <tr><th>Value</th><th>(maxPrecision= 0)</th><th>(maxPrecision= 4)</th></tr>
     *  <tr><td>1.0E-4</td><td>1.0E-4</td><td>1.0E-4</td></tr>
     *  <tr><td>-0.0635</td><td>-0.0635</td><td>-0.0635</td></tr>
     *  <tr><td>510.751</td><td>510.751</td><td>510.8</td></tr>
     *  <tr><td>-123456.0</td><td>-123456.0</td><td>-123500.0</td></tr>
     *  <tr><td>4.20785E7</td><td>4.20785E7</td><td>4.208E7</td></tr>
     * </table>
     * @param maxPrecision Maximum number of significant decimal digits in strings produced by the returned formatter.
     *      Numbers are rounded as necessary so that the number of significant digits does not exceed this value. A
     *      value of {@code 0} indicates no maximum precision.
     * @return double format function
     * @throws IllegalArgumentException if {@code maxPrecision} is less than zero
     */
    public static DoubleFunction<String> createDefault(final int maxPrecision) {
        return createDefault(maxPrecision, MIN_DOUBLE_EXPONENT);
    }

    /** Return a double format function that provides similar behavior to {@link Double#toString(double)}
     * but with a configurable max precision and min exponent. For values with an absolute magnitude less than
     * 10<sup>7</sup> and greater than or equal to 10<sup>-3</sup> (after any necessary rounding), the returned
     * string is in plain, non-scientific format. All other values are in scientific format. Rounding is performed
     * using {@link java.math.RoundingMode#HALF_EVEN half even} rounding.
     * <table>
     *  <caption>Format Examples</caption>
     *  <tr><th>Value</th><th>(maxPrecision= 0, minExponent= -2)</th><th>(maxPrecision= 4, minExponent= -2)</th></tr>
     *  <tr><td>1.0E-4</td><td>0.0</td><td>0.0</td></tr>
     *  <tr><td>-0.0635</td><td>-0.06</td><td>-0.06</td></tr>
     *  <tr><td>510.751</td><td>510.75</td><td>510.8</td></tr>
     *  <tr><td>-123456.0</td><td>-123456.0</td><td>-123500.0</td></tr>
     *  <tr><td>4.20785E7</td><td>4.20785E7</td><td>4.208E7</td></tr>
     * </table>
     * @param maxPrecision Maximum number of significant decimal digits in strings produced by the returned formatter.
     *      Numbers are rounded as necessary so that the number of significant digits does not exceed this value. A
     *      value of {@code 0} indicates no maximum precision.
     * @param minExponent Minimum decimal exponent in strings produced by the returned formatter.
     * @return double format function
     * @throws IllegalArgumentException if {@code maxPrecision} is less than zero
     */
    public static DoubleFunction<String> createDefault(final int maxPrecision, final int minExponent) {
        return new DefaultFormat(maxPrecision, minExponent);
    }

    /** Return a double format function that produces strings in plain, non-scientific format.
     * Rounding is performed using {@link java.math.RoundingMode#HALF_EVEN half even} rounding.
     * <table>
     *  <caption>Format Examples</caption>
     *  <tr><th>Value</th><th>(maxPrecision= 0)</th><th>(maxPrecision= 4)</th></tr>
     *  <tr><td>1.0E-4</td><td>0.0001</td><td>0.0001</td></tr>
     *  <tr><td>-0.0635</td><td>-0.0635</td><td>-0.0635</td></tr>
     *  <tr><td>510.751</td><td>510.751</td><td>510.8</td></tr>
     *  <tr><td>-123456.0</td><td>-123456.0</td><td>-123500.0</td></tr>
     *  <tr><td>4.20785E7</td><td>42078500.0</td><td>42080000.0</td></tr>
     * </table>
     * @param maxPrecision Maximum number of significant decimal digits in strings produced by the returned formatter.
     *      Numbers are rounded as necessary so that the number of significant digits does not exceed this value. A
     *      value of {@code 0} indicates no maximum precision.
     * @return double format function
     */
    public static DoubleFunction<String> createPlain(final int maxPrecision) {
        return createPlain(maxPrecision, MIN_DOUBLE_EXPONENT);
    }

    /** Return a double format function that produces strings in plain, non-scientific format.
     * Rounding is performed using {@link java.math.RoundingMode#HALF_EVEN half even} rounding.
     * <table>
     *  <caption>Format Examples</caption>
     *  <tr><th>Value</th><th>(maxPrecision= 0, minExponent= -2)</th><th>(maxPrecision= 4, minExponent= -2)</th></tr>
     *  <tr><td>1.0E-4</td><td>0.0</td><td>0.0</td></tr>
     *  <tr><td>-0.0635</td><td>-0.06</td><td>-0.06</td></tr>
     *  <tr><td>510.751</td><td>510.75</td><td>510.8</td></tr>
     *  <tr><td>-123456.0</td><td>-123456.0</td><td>-123500.0</td></tr>
     *  <tr><td>4.20785E7</td><td>42078500.0</td><td>42080000.0</td></tr>
     * </table>
     * @param maxPrecision Maximum number of significant decimal digits in strings produced by the returned formatter.
     *      Numbers are rounded as necessary so that the number of significant digits does not exceed this value. A
     *      value of {@code 0} indicates no maximum precision.
     * @param minExponent Minimum decimal exponent in strings produced by the returned formatter.
     * @return double format function
     */
    public static DoubleFunction<String> createPlain(final int maxPrecision, final int minExponent) {
        return new PlainFormat(maxPrecision, minExponent);
    }

    /** Return a double format function that produces strings in scientific format. Exponents of
     * zero are not included in formatted strings. Rounding is performed using
     * {@link java.math.RoundingMode#HALF_EVEN half even} rounding.
     * <table>
     *  <caption>Format Examples</caption>
     *  <tr><th>Value</th><th>(maxPrecision= 0)</th><th>(maxPrecision= 4)</th></tr>
     *  <tr><td>1.0E-4</td><td>1.0E-4</td><td>1.0E-4</td></tr>
     *  <tr><td>-0.0635</td><td>-6.35E-2</td><td>-6.35E-2</td></tr>
     *  <tr><td>510.751</td><td>5.10751E2</td><td>5.108E2</td></tr>
     *  <tr><td>-123456.0</td><td>-1.23456E5</td><td>-1.235E5</td></tr>
     *  <tr><td>4.20785E7</td><td>4.20785E7</td><td>4.208E7</td></tr>
     * </table>
     * @param maxPrecision Maximum number of significant decimal digits in strings produced by the returned formatter.
     *      Numbers are rounded as necessary so that the number of significant digits does not exceed this value. A
     *      value of {@code 0} indicates no maximum precision.
     * @return double format function
     */
    public static DoubleFunction<String> createScientific(final int maxPrecision) {
        return createScientific(maxPrecision, MIN_DOUBLE_EXPONENT);
    }

    /** Return a double format function that produces strings in scientific format. Exponents of
     * zero are not included in formatted strings. Rounding is performed using
     * {@link java.math.RoundingMode#HALF_EVEN half even} rounding.
     * <table>
     *  <caption>Format Examples</caption>
     *  <tr><th>Value</th><th>(maxPrecision= 0, minExponent= -2)</th><th>(maxPrecision= 4, minExponent= -2)</th></tr>
     *  <tr><td>1.0E-4</td><td>0.0</td><td>0.0</td></tr>
     *  <tr><td>-0.0635</td><td>-6.0E-2</td><td>-6.0E-2</td></tr>
     *  <tr><td>510.751</td><td>5.1075E2</td><td>5.108E2</td></tr>
     *  <tr><td>-123456.0</td><td>-1.23456E5</td><td>-1.235E5</td></tr>
     *  <tr><td>4.20785E7</td><td>4.20785E7</td><td>4.208E7</td></tr>
     * </table>
     * @param maxPrecision Maximum number of significant decimal digits in strings produced by the returned formatter.
     *      Numbers are rounded as necessary so that the number of significant digits does not exceed this value. A
     *      value of {@code 0} indicates no maximum precision.
     * @param minExponent Minimum decimal exponent in strings produced by the returned formatter.
     * @return double format function
     */
    public static DoubleFunction<String> createScientific(final int maxPrecision, final int minExponent) {
        return new ScientificFormat(maxPrecision, minExponent);
    }

    /** Return a double format function that produces strings in
     * <a href="https://en.wikipedia.org/wiki/Engineering_notation">engineering notation</a> where any exponents
     * are adjusted to be multiples of 3. Exponents of zero are not included in formatted strings. Rounding is
     * performed using {@link java.math.RoundingMode#HALF_EVEN half even} rounding.
     * <table>
     *  <caption>Format Examples</caption>
     *  <tr><th>Value</th><th>(maxPrecision= 0)</th><th>(maxPrecision= 4)</th></tr>
     *  <tr><td>1.0E-4</td><td>100.0E-6</td><td>100.0E-6</td></tr>
     *  <tr><td>-0.0635</td><td>-63.5E-3</td><td>-63.5E-3</td></tr>
     *  <tr><td>510.751</td><td>510.751</td><td>510.8</td></tr>
     *  <tr><td>-123456.0</td><td>-123.456E3</td><td>-123.5E3</td></tr>
     *  <tr><td>4.20785E7</td><td>42.0785E6</td><td>42.08E6</td></tr>
     * </table>
     * @param maxPrecision Maximum number of significant decimal digits in strings produced by the returned formatter.
     *      Numbers are rounded as necessary so that the number of significant digits does not exceed this value. A
     *      value of {@code 0} indicates no maximum precision.
     * @return double format function
     */
    public static DoubleFunction<String> createEngineering(final int maxPrecision) {
        return createEngineering(maxPrecision, MIN_DOUBLE_EXPONENT);
    }

    /** Return a double format function that produces strings in
     * <a href="https://en.wikipedia.org/wiki/Engineering_notation">engineering notation</a>, where exponents
     * are adjusted to be multiples of 3. Exponents of zero are not included in formatted strings. Rounding is
     * performed using {@link java.math.RoundingMode#HALF_EVEN half even} rounding.
     * <table>
     *  <caption>Format Examples</caption>
     *  <tr><th>Value</th><th>(maxPrecision= 0, minExponent= -2)</th><th>(maxPrecision= 4, minExponent= -2)</th></tr>
     *  <tr><td>1.0E-4</td><td>0.0</td><td>0.0</td></tr>
     *  <tr><td>-0.0635</td><td>-60.0E-3</td><td>-60.0E-3</td></tr>
     *  <tr><td>510.751</td><td>510.75</td><td>510.8</td></tr>
     *  <tr><td>-123456.0</td><td>-123.456E3</td><td>-123.5E3</td></tr>
     *  <tr><td>4.20785E7</td><td>42.0785E6</td><td>42.08E6</td></tr>
     * </table>
     * @param maxPrecision Maximum number of significant decimal digits in strings produced by the returned formatter.
     *      Numbers are rounded as necessary so that the number of significant digits does not exceed this value. A
     *      value of {@code 0} indicates no maximum precision.
     * @param minExponent Minimum decimal exponent in strings produced by the returned formatter.
     * @return double format function
     */
    public static DoubleFunction<String> createEngineering(final int maxPrecision, final int minExponent) {
        return new EngineeringFormat(maxPrecision, minExponent);
    }

    /** Base class for standard double formatting classes.
     */
    private abstract static class AbstractFormat implements DoubleFunction<String> {

        /** Maximum precision to use when formatting values. */
        private final int maxPrecision;

        /** The minimum exponent to allow in the result. Value with exponents less than this are
         * rounded to positive zero.
         */
        private final int minExponent;

        /** Construct a new instance with the given maximum precision and minimum exponent.
         * @param maxPrecision maximum number of significant decimal digits
         * @param minExponent minimum decimal exponent; values less than this that do not round up
         *      are considered to be zero
         * @throws IllegalArgumentException if {@code maxPrecision} is less than zero
         */
        AbstractFormat(final int maxPrecision, final int minExponent) {
            if (maxPrecision < 0) {
                throw new IllegalArgumentException(
                        "Max precision must be greater than or equal to zero; was " + maxPrecision);
            }

            this.maxPrecision = maxPrecision;
            this.minExponent = minExponent;
        }

        /** {@inheritDoc} */
        @Override
        public String apply(final double d) {
            if (Double.isFinite(d)) {
                final ParsedDouble n = ParsedDouble.from(d);

                int roundExponent = Math.max(n.getExponent(), minExponent);
                if (maxPrecision > 0) {
                    roundExponent = Math.max(n.getScientificExponent() - maxPrecision + 1, roundExponent);
                }

                final ParsedDouble rounded = n.round(roundExponent);

                return formatInternal(rounded);
            }

            return Double.toString(d); // NaN or infinite; use default Double toString() method
        }

        /** Format the given parsed double value.
         * @param val value to format
         * @return formatted double value
         */
        protected abstract String formatInternal(ParsedDouble val);
    }

    /** Format class that produces plain decimal strings that do not use
     * scientific notation.
     */
    private static class PlainFormat extends AbstractFormat {

        /** Construct a new instance with the given maximum precision and minimum exponent.
         * @param maxPrecision maximum number of significant decimal digits
         * @param minExponent minimum decimal exponent; values less than this that do not round up
         *      are considered to be zero
         * @throws IllegalArgumentException if {@code maxPrecision} is less than zero
         */
        PlainFormat(final int maxPrecision, final int minExponent) {
            super(maxPrecision, minExponent);
        }

        /** {@inheritDoc} */
        @Override
        protected String formatInternal(final ParsedDouble val) {
            return val.toPlainString(true);
        }
    }

    /** Format class producing results similar to {@link Double#toString()}, with
     * plain decimal notation for small numbers relatively close to zero and scientific
     * notation otherwise.
     */
    private static class DefaultFormat extends AbstractFormat {

        /** Decimal exponent upper bound for use of plain formatted strings. */
        private static final int UPPER_PLAIN_EXP = 7;

        /** Decimal exponent lower bound for use of plain formatted strings. */
        private static final int LOWER_PLAIN_EXP = -4;

        /** Construct a new instance with the given maximum precision and minimum exponent.
         * @param maxPrecision maximum number of significant decimal digits
         * @param minExponent minimum decimal exponent; values less than this that do not round up
         *      are considered to be zero
         * @throws IllegalArgumentException if {@code maxPrecision} is less than zero
         */
        DefaultFormat(final int maxPrecision, final int minExponent) {
            super(maxPrecision, minExponent);
        }

        /** {@inheritDoc} */
        @Override
        protected String formatInternal(final ParsedDouble val) {
            final int sciExp = val.getScientificExponent();
            return sciExp < UPPER_PLAIN_EXP && sciExp > LOWER_PLAIN_EXP ?
                    val.toPlainString(true) :
                    val.toScientificString(true);
        }
    }

    /** Format class that uses scientific notation for all values.
     */
    private static class ScientificFormat extends AbstractFormat {

        /** Construct a new instance with the given maximum precision and minimum exponent.
         * @param maxPrecision maximum number of significant decimal digits
         * @param minExponent minimum decimal exponent; values less than this that do not round up
         *      are considered to be zero
         * @throws IllegalArgumentException if {@code maxPrecision} is less than zero
         */
        ScientificFormat(final int maxPrecision, final int minExponent) {
            super(maxPrecision, minExponent);
        }

        /** {@inheritDoc} */
        @Override
        public String formatInternal(final ParsedDouble val) {
            return val.toScientificString(true);
        }
    }

    /** Format class that uses engineering notation for all values.
     */
    private static class EngineeringFormat extends AbstractFormat {

        /** Construct a new instance with the given maximum precision and minimum exponent.
         * @param maxPrecision maximum number of significant decimal digits
         * @param minExponent minimum decimal exponent; values less than this that do not round up
         *      are considered to be zero
         * @throws IllegalArgumentException if {@code maxPrecision} is less than zero
         */
        EngineeringFormat(final int maxPrecision, final int minExponent) {
            super(maxPrecision, minExponent);
        }

        /** {@inheritDoc} */
        @Override
        public String formatInternal(final ParsedDouble val) {
            return val.toEngineeringString(true);
        }
    }
}
