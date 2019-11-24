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
package org.apache.commons.geometry.euclidean.oned;

import java.util.function.Function;

/** Class that wraps a {@link Function} with the {@link Transform1D} interface.
 */
public final class FunctionTransform1D implements Transform1D {

    /** Static instance representing the identity transform. */
    private static final FunctionTransform1D IDENTITY =
            new FunctionTransform1D(Function.identity(), true, Vector1D.ZERO);

    /** The underlying function for the transform. */
    private final Function<Vector1D, Vector1D> fn;

    /** True if the transform preserves spatial orientation. */
    private final boolean preservesOrientation;

    /** The translation component of the transform. */
    private final Vector1D translation;

    /** Construct a new instance from its component parts. No validation of the input is performed.
     * @param fn the underlying function for the transform
     * @param preservesOrientation true if the transform preserves spatial orientation
     * @param translation the translation component of the transform
     */
    private FunctionTransform1D(final Function<Vector1D, Vector1D> fn, final boolean preservesOrientation,
            final Vector1D translation) {
        this.fn = fn;
        this.preservesOrientation = preservesOrientation;
        this.translation = translation;
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D apply(final Vector1D pt) {
        return fn.apply(pt);
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D applyVector(final Vector1D vec) {
        return apply(vec).subtract(translation);
    }

    /** {@inheritDoc} */
    @Override
    public boolean preservesOrientation() {
        return preservesOrientation;
    }

    /** {@inheritDoc} */
    @Override
    public AffineTransformMatrix1D toMatrix() {
        final Vector1D tOne = applyVector(Vector1D.Unit.PLUS);

        return AffineTransformMatrix1D.of(tOne.getX(), translation.getX());
    }

    /** Return an instance representing the identity transform.
     * @return an instance representing the identity transform
     */
    public static FunctionTransform1D identity() {
        return IDENTITY;
    }

    /** Construct a new transform instance from the given function.
     * @param fn the function to use for the transform
     * @return a new transform instance using the given function
     */
    public static FunctionTransform1D from(final Function<Vector1D, Vector1D> fn) {
        final Vector1D tOne = fn.apply(Vector1D.Unit.PLUS);
        final Vector1D tZero = fn.apply(Vector1D.ZERO);

        final boolean preservesOrientation = tOne.getX() > 0.0;

        return new FunctionTransform1D(fn, preservesOrientation, tZero);
    }
}
