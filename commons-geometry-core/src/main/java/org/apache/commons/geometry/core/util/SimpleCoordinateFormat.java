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
package org.apache.commons.geometry.core.util;

import java.text.ParsePosition;

/** Class for performing simple formatting and parsing of coordinate tuples in common dimensions.
 */
public class SimpleCoordinateFormat extends AbstractCoordinateParser {

    /** Default coordinate separator value */
    private static final String DEFAULT_SEPARATOR = ",";

    /** Space character */
    private static final String SPACE = " ";

    /** Creates a new format instance with the default separator value and the given
     * tuple prefix and suffix.
     * @param prefix coordinate tuple prefix; may be null
     * @param suffix coordinate tuple suffix; may be null
     */
    public SimpleCoordinateFormat(String prefix, String suffix) {
        this(DEFAULT_SEPARATOR, prefix, suffix);
    }

    /** Creates a new format instance with the given separator, prefix, and suffix.
     * @param separator string separating coordinate values
     * @param prefix coordinate tuple prefix; may be null
     * @param suffix coordinate tuple suffix; may be null
     */
    public SimpleCoordinateFormat(String separator, String prefix, String suffix) {
        super(separator, prefix, suffix);
    }

    /** Returns a 1D coordinate tuple string with the given value.
     * @param v coordinate value
     * @return 1D coordinate tuple string
     */
    public String format1D(double v) {
        StringBuilder sb = new StringBuilder();

        if (getPrefix() != null) {
            sb.append(getPrefix());
        }

        sb.append(v);

        if (getSuffix() != null) {
            sb.append(getSuffix());
        }

        return sb.toString();
    }

    /** Returns a 2D coordinate tuple string with the given values.
     * @param v1 first coordinate value
     * @param v2 second coordinate value
     * @return 2D coordinate tuple string
     */
    public String format2D(double v1, double v2) {
        StringBuilder sb = new StringBuilder();

        if (getPrefix() != null) {
            sb.append(getPrefix());
        }

        sb.append(v1);
        sb.append(getSeparator());
        sb.append(SPACE);
        sb.append(v2);

        if (getSuffix() != null) {
            sb.append(getSuffix());
        }

        return sb.toString();
    }

    /** Returns a 3D coordinate tuple string with the given values.
     * @param v1 first coordinate value
     * @param v2 second coordinate value
     * @param v3 third coordinate value
     * @return 3D coordinate tuple string
     */
    public String format3D(double v1, double v2, double v3) {
        StringBuilder sb = new StringBuilder();

        if (getPrefix() != null) {
            sb.append(getPrefix());
        }

        sb.append(v1);
        sb.append(getSeparator());
        sb.append(SPACE);
        sb.append(v2);
        sb.append(getSeparator());
        sb.append(SPACE);
        sb.append(v3);

        if (getSuffix() != null) {
            sb.append(getSuffix());
        }

        return sb.toString();
    }

    /** Parses the given string as a 1D coordinate tuple and passes the coordinate value to the
     * given factory. The object created by the factory is returned.
     * @param str the string to be parsed
     * @param factory object that will be passed the parsed coordinate value
     * @return object created by {@code factory}
     * @throws IllegalArgumentException if the input string format is invalid
     */
    public <T> T parse1D(String str, Coordinates.Factory1D<T> factory) throws IllegalArgumentException {
        final ParsePosition pos = new ParsePosition(0);

        readPrefix(str, pos);
        final double v = readCoordinateValue(str, pos);
        readSuffix(str, pos);
        endParse(str, pos);

        return factory.create(v);
    }

    /** Parses the given string as a 2D coordinate tuple and passes the coordinate values to the
     * given factory. The object created by the factory is returned.
     * @param str the string to be parsed
     * @param factory object that will be passed the parsed coordinate values
     * @return object created by {@code factory}
     * @throws IllegalArgumentException if the input string format is invalid
     */
    public <T> T parse2D(String str, Coordinates.Factory2D<T> factory) throws IllegalArgumentException {
        final ParsePosition pos = new ParsePosition(0);

        readPrefix(str, pos);
        final double v1 = readCoordinateValue(str, pos);
        final double v2 = readCoordinateValue(str, pos);
        readSuffix(str, pos);
        endParse(str, pos);

        return factory.create(v1, v2);
    }

    /** Parses the given string as a 3D coordinate tuple and passes the coordinate values to the
     * given factory. The object created by the factory is returned.
     * @param str the string to be parsed
     * @param factory object that will be passed the parsed coordinate values
     * @return object created by {@code factory}
     * @throws IllegalArgumentException if the input string format is invalid
     */
    public <T> T parse3D(String str, Coordinates.Factory3D<T> factory) throws IllegalArgumentException {
        ParsePosition pos = new ParsePosition(0);

        readPrefix(str, pos);
        final double v1 = readCoordinateValue(str, pos);
        final double v2 = readCoordinateValue(str, pos);
        final double v3 = readCoordinateValue(str, pos);
        readSuffix(str, pos);
        endParse(str, pos);

        return factory.create(v1, v2, v3);
    }
}
