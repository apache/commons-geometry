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

import java.util.function.Function;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.euclidean.internal.Transforms;

/** Extension of the {@link Transform} interface for Euclidean 3D points.
 */
public interface Transform3D extends Transform<Vector3D> {

    /** Return an instance representing the identity transform.
     * @return an instance representing the identity transform.
     */
    static Transform3D identity() {

        return new Transform3D() {

            @Override
            public boolean preservesOrientation() {
                return true;
            }

            @Override
            public Vector3D apply(final Vector3D pt) {
                return pt;
            };
        };
    }

    /** Create a new {@link Transform3D} instance from the given function.
     * @param fn function used to transform points
     * @return a new transform instance
     */
    static Transform3D from(final Function<Vector3D, Vector3D> fn) {

        final boolean preservesOrientation = Transforms.preservesOrientation3D(fn);

        return new Transform3D() {

            @Override
            public boolean preservesOrientation() {
                return preservesOrientation;
            }

            @Override
            public Vector3D apply(final Vector3D pt) {
                return fn.apply(pt);
            }
        };
    }
}
