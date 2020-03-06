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
package org.apache.commons.geometry.euclidean.threed.rotation;

import java.util.function.DoubleFunction;

import org.apache.commons.numbers.quaternion.Slerp;

/** Class used to perform spherical linear interpolation (ie, "Slerp") between quaternion rotations.
 * This class serves as a geometry-specific wrapper around the generic {@link Slerp} class.
 * @see org.apache.commons.numbers.quaternion.Slerp
 * @see <a href="https://en.wikipedia.org/wiki/Slerp">Slerp</a>
 */
public final class SlerpFunction implements DoubleFunction<QuaternionRotation> {

    /** The start rotation. */
    private final QuaternionRotation start;

    /** The end rotation. */
    private final QuaternionRotation end;

    /** Slerp instance that will perform the interpolation. */
    private final Slerp slerp;

    /** Create a new instance that interpolates between the given start and end rotations.
     * @param start start of the interpolation
     * @param end end of the interpolation
     */
    public SlerpFunction(final QuaternionRotation start, final QuaternionRotation end) {
        this.start = start;
        this.end = end;
        this.slerp = new Slerp(start.getQuaternion(), end.getQuaternion());
    }

    /** Get the start value for the interpolation.
     * @return the start value for the interpolation
     */
    public QuaternionRotation getStart() {
        return start;
    }

    /** Get the end value for the interpolation.
     * @return the end value for the interpolation
     */
    public QuaternionRotation getEnd() {
        return end;
    }

    /** Perform the interpolation. The rotation returned by this method is controlled by the interpolation
     * parameter, {@code t}. If {@code t = 0}, a rotation equal to the start instance is returned. If {@code t = 1},
     * a rotation equal to the end instance is returned.  All other values are interpolated (or extrapolated if
     * {@code t} is outside of the {@code [0, 1]} range).
     * @param t interpolation control parameter
     * @return an interpolated rotation
     */
    @Override
    public QuaternionRotation apply(final double t) {
        return QuaternionRotation.of(slerp.apply(t));
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName())
            .append("[start= ")
            .append(start)
            .append(", end= ")
            .append(end)
            .append(']');

        return sb.toString();
    }
}
