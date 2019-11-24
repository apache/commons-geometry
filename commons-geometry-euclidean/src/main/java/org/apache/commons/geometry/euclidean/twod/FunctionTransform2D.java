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
package org.apache.commons.geometry.euclidean.twod;

import java.util.function.Function;

import org.apache.commons.geometry.euclidean.internal.Matrices;

/** Class that wraps a {@link Function} with the {@link Transform2D} interface.
 */
public final class FunctionTransform2D implements Transform2D {

    /** Static instance representing the identity transform. */
    private static final FunctionTransform2D IDENTITY =
            new FunctionTransform2D(Function.identity(), true, Vector2D.ZERO);

    /** The underlying function for the transform. */
    private final Function<Vector2D, Vector2D> fn;

    /** True if the transform preserves spatial orientation. */
    private final boolean preservesOrientation;

    /** The translation component of the transform. */
    private final Vector2D translation;

    /** Construct a new instance from its component parts. No validation of the input is performed.
     * @param fn the underlying function for the transform
     * @param preservesOrientation true if the transform preserves spatial orientation
     * @param translation the translation component of the transform
     */
    private FunctionTransform2D(final Function<Vector2D, Vector2D> fn, final boolean preservesOrientation,
            final Vector2D translation) {
        this.fn = fn;
        this.preservesOrientation = preservesOrientation;
        this.translation = translation;
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D apply(final Vector2D pt) {
        return fn.apply(pt);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D applyVector(final Vector2D vec) {
        return apply(vec).subtract(translation);
    }

    /** {@inheritDoc} */
    @Override
    public boolean preservesOrientation() {
        return preservesOrientation;
    }

    /** {@inheritDoc} */
    @Override
    public AffineTransformMatrix2D toMatrix() {
        final Vector2D u = applyVector(Vector2D.Unit.PLUS_X);
        final Vector2D v = applyVector(Vector2D.Unit.PLUS_Y);

        return AffineTransformMatrix2D.fromColumnVectors(u, v, translation);
    }

    /** Return an instance representing the identity transform.
     * @return an instance representing the identity transform
     */
    public static FunctionTransform2D identity() {
        return IDENTITY;
    }

    /** Construct a new transform instance from the given function.
     * @param fn the function to use for the transform
     * @return a new transform instance using the given function
     */
    public static FunctionTransform2D from(final Function<Vector2D, Vector2D> fn) {
        final Vector2D tPlusX = fn.apply(Vector2D.Unit.PLUS_X);
        final Vector2D tPlusY = fn.apply(Vector2D.Unit.PLUS_Y);
        final Vector2D tZero = fn.apply(Vector2D.ZERO);

        final double det = Matrices.determinant(
                tPlusX.getX(), tPlusY.getX(),
                tPlusX.getY(), tPlusY.getY()
            );
        final boolean preservesOrientation = det > 0;

        return new FunctionTransform2D(fn, preservesOrientation, tZero);
    }
}
