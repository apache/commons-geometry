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
package org.apache.commons.geometry.euclidean.twod.rotation;

import org.apache.commons.geometry.euclidean.EuclideanTransform;
import org.apache.commons.geometry.euclidean.twod.AffineTransformMatrix2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

/** Class representing a rotation in 2 dimensional Euclidean space. Positive
 * rotations are in a <em>counter-clockwise</em> direction.
 */
public final class Rotation2D implements EuclideanTransform<Vector2D> {

    /** The angle of the rotation in radians. */
    private final double angle;

    /** The cosine of the angle of rotation, cached to avoid repeated computation. */
    private final double cosAngle;

    /** The sine of the angle of rotation, cached to avoid repeated computation. */
    private final double sinAngle;

    /** Create a new instance representing the given angle.
     * @param angle the angle of rotation, in radians
     */
    private Rotation2D(final double angle) {
        this.angle = angle;
        this.cosAngle = Math.cos(angle);
        this.sinAngle = Math.sin(angle);
    }

    /** Get the angle of rotation in radians.
     * @return the angle of rotation in radians
     */
    public double getAngle() {
        return angle;
    }

    /** {@inheritDoc} */
    @Override
    public Rotation2D inverse() {
        return new Rotation2D(-angle);
    }

    /** {@inheritDoc}
     *
     * <p>This method simply returns true since rotations always preserve the orientation
     * of the space.</p>
     */
    @Override
    public boolean preservesOrientation() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D apply(final Vector2D pt) {
        final double x = pt.getX();
        final double y = pt.getY();

        return Vector2D.of(
                    (x * cosAngle) - (y * sinAngle),
                    (x * sinAngle) + (y * cosAngle)
                );
    }

    /** {@inheritDoc}
     *
     * <p>This method simply calls {@code apply(vec)} since rotations treat
     * points and vectors similarly.</p>
     * */
    @Override
    public Vector2D applyVector(Vector2D vec) {
        return apply(vec);
    }

    /** Return an {@link AffineTransformMatrix2D} representing the same rotation
     * as this instance.
     * @return a transform matrix representing the same rotation
     */
    public AffineTransformMatrix2D toMatrix() {
        return AffineTransformMatrix2D.of(
                    cosAngle, -sinAngle, 0.0,
                    sinAngle, cosAngle, 0.0
                );
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Double.hashCode(angle);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Rotation2D)) {
            return false;
        }

        final Rotation2D other = (Rotation2D) obj;

        return Double.compare(this.angle, other.angle) == 0;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName())
            .append("[angle=")
            .append(angle)
            .append(']');

        return sb.toString();
    }

    /** Create a new instance with the given angle of rotation.
     * @param angle the angle of rotation in radians
     * @return a new instance with the given angle of rotation
     */
    public static Rotation2D of(final double angle) {
        return new Rotation2D(angle);
    }
}
