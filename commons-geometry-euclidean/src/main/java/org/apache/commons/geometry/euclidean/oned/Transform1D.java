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

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.euclidean.internal.Transforms;

/** Extension of the {@link Transform} interface for Euclidean 1D points.
 */
public interface Transform1D extends Transform<Vector1D> {

    /** Return an instance representing the identity transform.
     * @return an instance representing the identity transform.
     */
    static Transform1D identity() {

        return new Transform1D() {

            @Override
            public boolean preservesOrientation() {
                return true;
            }

            @Override
            public Vector1D apply(final Vector1D pt) {
                return pt;
            }
        };
    }

    /** Create a new {@link Transform1D} instance from the given function.
     * @param fn function used to transform points
     * @return a new transform instance
     */
    static Transform1D from(final Function<Vector1D, Vector1D> fn) {

        final boolean preservesOrientation = Transforms.preservesOrientation1D(fn);

        return new Transform1D() {

            @Override
            public boolean preservesOrientation() {
                return preservesOrientation;
            }

            @Override
            public Vector1D apply(final Vector1D pt) {
                return fn.apply(pt);
            }
        };
    }
}
