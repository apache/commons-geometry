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
package org.apache.commons.geometry.euclidean.threed;

import java.util.function.UnaryOperator;

import org.apache.commons.geometry.euclidean.internal.Matrices;

/** Class that wraps a {@link UnaryOperator} with the {@link Transform3D} interface.
 */
public final class FunctionTransform3D implements Transform3D {

    /** Static instance representing the identity transform. */
    private static final FunctionTransform3D IDENTITY =
            new FunctionTransform3D(UnaryOperator.identity(), true, Vector3D.ZERO);

    /** The underlying function for the transform. */
    private final UnaryOperator<Vector3D> fn;

    /** True if the transform preserves spatial orientation. */
    private final boolean preservesOrientation;

    /** The translation component of the transform. */
    private final Vector3D translation;

    /** Construct a new instance from its component parts. No validation of the input is performed.
     * @param fn the underlying function for the transform
     * @param preservesOrientation true if the transform preserves spatial orientation
     * @param translation the translation component of the transform
     */
    private FunctionTransform3D(final UnaryOperator<Vector3D> fn, final boolean preservesOrientation,
            final Vector3D translation) {
        this.fn = fn;
        this.preservesOrientation = preservesOrientation;
        this.translation = translation;
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D apply(final Vector3D pt) {
        return fn.apply(pt);
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D applyVector(final Vector3D vec) {
        return apply(vec).subtract(translation);
    }

    /** {@inheritDoc} */
    @Override
    public boolean preservesOrientation() {
        return preservesOrientation;
    }

    /** {@inheritDoc} */
    @Override
    public AffineTransformMatrix3D toMatrix() {
        final Vector3D u = applyVector(Vector3D.Unit.PLUS_X);
        final Vector3D v = applyVector(Vector3D.Unit.PLUS_Y);
        final Vector3D w = applyVector(Vector3D.Unit.PLUS_Z);

        return AffineTransformMatrix3D.fromColumnVectors(u, v, w, translation);
    }

    /** Return an instance representing the identity transform.
     * @return an instance representing the identity transform
     */
    public static FunctionTransform3D identity() {
        return IDENTITY;
    }

    /** Construct a new transform instance from the given function.
     * @param fn the function to use for the transform
     * @return a new transform instance using the given function
     */
    public static FunctionTransform3D from(final UnaryOperator<Vector3D> fn) {
        final Vector3D tPlusX = fn.apply(Vector3D.Unit.PLUS_X);
        final Vector3D tPlusY = fn.apply(Vector3D.Unit.PLUS_Y);
        final Vector3D tPlusZ = fn.apply(Vector3D.Unit.PLUS_Z);

        final Vector3D tZero = fn.apply(Vector3D.ZERO);

        final double det = Matrices.determinant(
                tPlusX.getX(), tPlusY.getX(), tPlusZ.getX(),
                tPlusX.getY(), tPlusY.getY(), tPlusZ.getY(),
                tPlusX.getZ(), tPlusY.getZ(), tPlusZ.getZ()
            );
        final boolean preservesOrientation = det > 0;

        return new FunctionTransform3D(fn, preservesOrientation, tZero);
    }
}
