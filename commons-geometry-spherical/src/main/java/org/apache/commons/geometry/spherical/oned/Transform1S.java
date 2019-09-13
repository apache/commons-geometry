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
package org.apache.commons.geometry.spherical.oned;

import java.util.function.Function;

import org.apache.commons.geometry.core.Transform;

/** Extension of the {@link Transform} interface for spherical 1D points.
 */
public interface Transform1S extends Transform<Point1S> {

    /** Return an instance representing the identity transform.
     * @return an instance representing the identity transform.
     */
    static Transform1S identity() {

        return new Transform1S() {

            @Override
            public boolean preservesOrientation() {
                return true;
            }

            @Override
            public Point1S apply(final Point1S pt) {
                return pt;
            }
        };
    }

    /** Create a new {@link Transform1S} instance from the given function.
     * @param fn function used to transform points
     * @return a new transform instance
     */
    static Transform1S from(final Function<Point1S, Point1S> fn) {

        final Point1S tPi = fn.apply(Point1S.PI);
        final Point1S tZero = fn.apply(Point1S.ZERO);

        final boolean preservesOrientation = (tPi.getAzimuth() - tZero.getAzimuth()) > 0.0;

        return new Transform1S() {

            @Override
            public boolean preservesOrientation() {
                return preservesOrientation;
            }

            @Override
            public Point1S apply(final Point1S pt) {
                return fn.apply(pt);
            }
        };
    }
}
