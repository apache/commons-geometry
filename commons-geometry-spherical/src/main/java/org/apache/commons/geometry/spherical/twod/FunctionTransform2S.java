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
package org.apache.commons.geometry.spherical.twod;

import java.util.function.Function;

import org.apache.commons.geometry.euclidean.internal.Matrices;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

/** Class that wraps a {@link Function} with the {@link Transform2S} interface.
 */
public class FunctionTransform2S implements Transform2S {

    /** Static instance representing the identity transform. */
    private static final FunctionTransform2S IDENTITY = new FunctionTransform2S(Function.identity(), true);

    /** The underlying function for the transform. */
    private final Function<Point2S, Point2S> fn;

    /** True if the transform preserves spatial orientation. */
    private final boolean preservesOrientation;

    /** Construct a new instance from its component parts. No validation of the input is performed.
     * @param fn the underlying function for the transform
     * @param preservesOrientation true if the transform preserves spatial orientation
     */
    private FunctionTransform2S(final Function<Point2S, Point2S> fn, final boolean preservesOrientation) {
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
    public Point2S apply(final Point2S pt) {
        return fn.apply(pt);
    }

    /** Return an instance representing the identity transform.
     * @return an instance representing the identity transform
     */
    public static FunctionTransform2S identity() {
        return IDENTITY;
    }

    /** Construct a new transform instance from the given function.
     * @param fn the function to use for the transform
     * @return a new transform instance using the given function
     */
    public static FunctionTransform2S from(final Function<Point2S, Point2S> fn) {
        // determine the orientation of the transform by how it affects the underlying
        // 3d vectors
        final Vector3D tPlusX = fn.apply(Point2S.from(Vector3D.Unit.PLUS_X)).getVector();
        final Vector3D tPlusY = fn.apply(Point2S.from(Vector3D.Unit.PLUS_Y)).getVector();
        final Vector3D tPlusZ = fn.apply(Point2S.from(Vector3D.Unit.PLUS_Z)).getVector();

        final double det = Matrices.determinant(
                tPlusX.getX(), tPlusY.getX(), tPlusZ.getX(),
                tPlusX.getY(), tPlusY.getY(), tPlusZ.getY(),
                tPlusX.getZ(), tPlusY.getZ(), tPlusZ.getZ()
            );
        final boolean preservesOrientation = det > 0;

        return new FunctionTransform2S(fn, preservesOrientation);
    }
}
