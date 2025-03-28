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
package org.apache.commons.geometry.euclidean;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.Vector;
import org.apache.commons.geometry.euclidean.internal.Vectors;
import org.apache.commons.numbers.core.Precision;

/** Abstract base class for Euclidean vectors <em>and</em> points. See
 * {@link org.apache.commons.geometry.euclidean here} for a discussion
 * of the combination of point and vector functionality into a single
 * class hierarchy.
 *
 * @param <V> Vector implementation type
 */
public abstract class EuclideanVector<V extends EuclideanVector<V>>
    implements Vector<V>, Point<V> {

    /** Create an instance. */
    public EuclideanVector() {
        // Do nothing
    }

    /** Return the vector representing the displacement from this vector
     * to the given vector. This is exactly equivalent to {@code v.subtract(thisVector)}
     * but with a method name that is much easier to visualize.
     * @param v the vector that the returned vector will be directed toward
     * @return vector representing the displacement <em>from</em> this vector <em>to</em> the given vector
     */
    public abstract V vectorTo(V v);

    /** Return the unit vector representing the direction of displacement from this
     * vector to the given vector. This is exactly equivalent to {@code v.subtract(thisVector).normalize()}
     * but without the intermediate vector instance.
     * @param v the vector that the returned vector will be directed toward
     * @return unit vector representing the direction of displacement <em>from</em> this vector
     *      <em>to</em> the given vector
     * @throws IllegalArgumentException if the norm of the vector pointing
     *      from this instance to {@code v} is zero, NaN, or infinite
     */
    public abstract V directionTo(V v);

    /** Get a vector constructed by linearly interpolating between this vector and the given vector.
     * The vector coordinates are generated by the equation {@code V = (1 - t)*A + t*B}, where {@code A}
     * is the current vector and {@code B} is the given vector. This means that if {@code t = 0}, a
     * vector equal to the current vector will be returned. If {@code t = 1}, a vector equal to the
     * argument will be returned. The {@code t} parameter is not constrained to the range {@code [0, 1]},
     * meaning that linear extrapolation can also be performed with this method.
     * @param v other vector
     * @param t interpolation parameter
     * @return interpolated or extrapolated vector
     */
    public abstract V lerp(V v, double t);

    /** Return true if the current instance and given vector are considered equal as evaluated by the
     * given precision context.
     *
     * <p>Equality is determined by comparing each pair of components in turn from the two
     * vectors. If all components evaluate as equal, then the vectors are considered equal. If any are
     * not equal, then the vectors are not considered equal. Note that this approach means that the
     * calculated distance between two "equal" vectors may be as much as <code>&radic;(n * eps<sup>2</sup>)</code>,
     * where {@code n} is the number of components in the vector and {@code eps} is the maximum epsilon
     * value allowed by the precision context.
     * @param v vector to check for equality
     * @param precision precision context used to determine floating point equality
     * @return true if the current instance is considered equal to the given vector when using
     *      the given precision context; otherwise false
     */
    public abstract boolean eq(V v, Precision.DoubleEquivalence precision);

    /** Return true if the current instance is considered equal to the zero vector as evaluated by the
     * given precision context. This is a convenience method equivalent to
     * {@code vec.equals(vec.getZero(), precision)}.
     *
     * @param precision precision context used to determine floating point equality
     * @return true if the current instance is considered equal to the zero vector when using
     *      the given precision context; otherwise false
     * @see #eq(EuclideanVector, Precision.DoubleEquivalence)
     */
    public boolean isZero(final Precision.DoubleEquivalence precision) {
        return eq(getZero(), precision);
    }

    /** Return the vector norm value, throwing an {@link IllegalArgumentException} if the value is not real
     * (ie, NaN or infinite) or zero.
     * @return the vector norm value, guaranteed to be real and non-zero
     * @throws IllegalArgumentException if the vector norm is zero, NaN, or infinite
     */
    protected double getCheckedNorm() {
        return Vectors.checkedNorm(this);
    }
}
