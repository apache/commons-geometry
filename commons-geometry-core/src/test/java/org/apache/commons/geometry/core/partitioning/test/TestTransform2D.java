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
package org.apache.commons.geometry.core.partitioning.test;

import java.util.function.UnaryOperator;

import org.apache.commons.geometry.core.Transform;

/** Implementation class for 2D {@link Transform}s. This
 * class should only be used for testing purposes.
 */
public final class TestTransform2D implements Transform<TestPoint2D> {

    /** Underlying transform function. */
    private final UnaryOperator<TestPoint2D> fn;

    /** True if the transform preserves the handedness of the space. */
    private final boolean preservesHandedness;

    /** Create a new instance using the given transform function.
     * @param fn transform function
     */
    public TestTransform2D(final UnaryOperator<TestPoint2D> fn) {
        this.fn = fn;

        final TestPoint2D tx = fn.apply(TestPoint2D.PLUS_X);
        final TestPoint2D ty = fn.apply(TestPoint2D.PLUS_Y);

        final double signedArea = (tx.getX() * ty.getY()) -
                (tx.getY() * ty.getX());

        this.preservesHandedness = signedArea > 0;
    }

    /** {@inheritDoc} */
    @Override
    public TestPoint2D apply(final TestPoint2D pt) {
        return fn.apply(pt);
    }

    /** {@inheritDoc} */
    @Override
    public boolean preservesOrientation() {
        return preservesHandedness;
    }

    /** {@inheritDoc} */
    @Override
    public Transform<TestPoint2D> inverse() {
        throw new UnsupportedOperationException();
    }
}
