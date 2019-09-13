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
package org.apache.commons.geometry.euclidean.internal;

import java.util.function.Function;

import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

/** This class consists exclusively of static transform utility methods.
 */
public class Transforms {

    /** Return true if the given function preserves spatial orientation when used as
     * a transform.
     * @param fn function to test
     * @return true if the given function preserves spatial orientation
     * @see org.apache.commons.geometry.core.Transform#preservesOrientation
     */
    public static boolean preservesOrientation1D(final Function<Vector1D, Vector1D> fn) {
        final Vector1D tOne = fn.apply(Vector1D.ONE);
        final Vector1D tZero = fn.apply(Vector1D.ZERO);

        return tZero.vectorTo(tOne).getX() > 0;
    }

    /** Return true if the given function preserves spatial orientation when used as
     * a transform.
     * @param fn function to test
     * @return true if the given function preserves spatial orientation
     * @see org.apache.commons.geometry.core.Transform#preservesOrientation
     */
    public static boolean preservesOrientation2D(final Function<Vector2D, Vector2D> fn) {

        // orientation is only preserved if the determinant of the matrix form of the
        // transform is non-zero

        final Vector2D tx = fn.apply(Vector2D.PLUS_X);
        final Vector2D ty = fn.apply(Vector2D.PLUS_Y);

        final double det = Matrices.determinant(
                tx.getX(), ty.getX(),
                tx.getY(), ty.getY());

        return det > 0.0;
    }

    /** Return true if the given function preserves spatial orientation when used as
     * a transform.
     * @param fn function to test
     * @return true if the given function preserves spatial orientation
     * @see org.apache.commons.geometry.core.Transform#preservesOrientation
     */
    public static boolean preservesOrientation3D(final Function<Vector3D, Vector3D> fn) {

        // orientation is only preserved if the determinant of the matrix form of the
        // transform is non-zero

        final Vector3D tx = fn.apply(Vector3D.PLUS_X);
        final Vector3D ty = fn.apply(Vector3D.PLUS_Y);
        final Vector3D tz = fn.apply(Vector3D.PLUS_Z);

        final double det = Matrices.determinant(
                tx.getX(), ty.getX(), tz.getX(),
                tx.getY(), ty.getY(), tz.getY(),
                tx.getZ(), ty.getZ(), tz.getZ());

        return det > 0.0;
    }

    /** Private constructor */
    private Transforms() {}
}
