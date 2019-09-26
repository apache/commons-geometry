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

/** Class that wraps a {@link Function} with the {@link Transform1S} interface.
 */
public class FunctionTransform1S implements Transform1S {

    /** Static instance representing the identity transform. */
    private static final FunctionTransform1S IDENTITY = new FunctionTransform1S(Function.identity(), true);

    /** The underlying function for the transform. */
    private final Function<Point1S, Point1S> fn;

    /** True if the transform preserves spatial orientation. */
    private final boolean preservesOrientation;

    /** Construct a new instance from its component parts. No validation of the input is performed.
     * @param fn the underlying function for the transform
     * @param preservesOrientation true if the transform preserves spatial orientation
     */
    private FunctionTransform1S(final Function<Point1S, Point1S> fn, final boolean preservesOrientation) {
        this.fn = fn;
        this.preservesOrientation = preservesOrientation;
    }

    /** {@inheritDoc} */
    @Override
    public boolean preservesOrientation() {
        return preservesOrientation;
    }

    /** {@inheritDoc} */
    @Override
    public Point1S apply(final Point1S pt) {
        return fn.apply(pt);
    }

    /** Return an instance representing the identity transform.
     * @return an instance representing the identity transform
     */
    public static FunctionTransform1S identity() {
        return IDENTITY;
    }

    /** Construct a new transform instance from the given function.
     * @param fn the function to use for the transform
     * @return a new transform instance using the given function
     */
    public static FunctionTransform1S from(final Function<Point1S, Point1S> fn) {
        final Point1S tPi = fn.apply(Point1S.PI);
        final Point1S tZero = fn.apply(Point1S.ZERO);

        final boolean preservesOrientation = (tPi.getAzimuth() - tZero.getAzimuth()) > 0.0;

        return new FunctionTransform1S(fn, preservesOrientation);
    }
}
