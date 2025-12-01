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
package org.apache.commons.geometry.euclidean.threed.rotation;

import java.util.function.UnaryOperator;

import org.apache.commons.geometry.euclidean.threed.Vector3D;

/**
 * A collection of standard vector rotation operators implemented as
 * {@link UnaryOperator}s. These can be used to test rotation algorithms.
 */
public final class StandardRotations {

    /** The identity rotation; the input vector is returned unchanged. */
    public static final UnaryOperator<Vector3D> IDENTITY = v -> v;

    /** Rotates {@code pi/2} around the {@code +x} axis */
    public static final UnaryOperator<Vector3D> PLUS_X_HALF_PI = v -> Vector3D.of(v.getX(), -v.getZ(), v.getY());

    /** Rotates {@code pi/2} around the {@code -x} axis */
    public static final UnaryOperator<Vector3D> MINUS_X_HALF_PI = v -> Vector3D.of(v.getX(), v.getZ(), -v.getY());

    /** Rotates {@code pi} around the {@code x} axis (+x and -x are the same) */
    public static final UnaryOperator<Vector3D> X_PI = v -> Vector3D.of(v.getX(), -v.getY(), -v.getZ());

    /** Rotates {@code pi/2} around the {@code +y} axis */
    public static final UnaryOperator<Vector3D> PLUS_Y_HALF_PI = v -> Vector3D.of(v.getZ(), v.getY(), -v.getX());

    /** Rotates {@code pi/2} around the {@code -y} axis */
    public static final UnaryOperator<Vector3D> MINUS_Y_HALF_PI = v -> Vector3D.of(-v.getZ(), v.getY(), v.getX());

    /** Rotates {@code pi} around the {@code y} axis (+y and -y are the same) */
    public static final UnaryOperator<Vector3D> Y_PI = v -> Vector3D.of(-v.getX(), v.getY(), -v.getZ());

    /** Rotates {@code pi/2} around the {@code -y} axis */
    public static final UnaryOperator<Vector3D> PLUS_Z_HALF_PI = v -> Vector3D.of(-v.getY(), v.getX(), v.getZ());

    /** Rotates {@code pi/2} around the {@code -y} axis */
    public static final UnaryOperator<Vector3D> MINUS_Z_HALF_PI = v -> Vector3D.of(v.getY(), -v.getX(), v.getZ());

    /** Rotates {@code pi} around the {@code y} axis (+y and -y are the same) */
    public static final UnaryOperator<Vector3D> Z_PI = v -> Vector3D.of(-v.getX(), -v.getY(), v.getZ());

    /** Rotates {@code 2pi/3} around the {@code (1, 1, 1)} axis */
    public static final UnaryOperator<Vector3D> PLUS_DIAGONAL_TWO_THIRDS_PI = v ->
        Vector3D.of(v.getZ(), v.getX(), v.getY());

    /** Rotates {@code 2pi/3} around the {@code (-1, -1, -1)} axis */
    public static final UnaryOperator<Vector3D> MINUS_DIAGONAL_TWO_THIRDS_PI = v ->
        Vector3D.of(v.getY(), v.getZ(), v.getX());

    /** Private constructor. */
    private StandardRotations() {}
}
