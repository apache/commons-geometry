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
package org.apache.commons.geometry.euclidean.twod;

import java.util.function.UnaryOperator;

import org.apache.commons.geometry.euclidean.EuclideanTransform;

/** Extension of the {@link EuclideanTransform} interface for 2D space.
 */
public interface Transform2D extends EuclideanTransform<Vector2D> {

    /** Return an affine transform matrix representing the same transform
     * as this instance.
     * @return an affine tranform matrix representing the same transform
     *      as this instance
     */
    AffineTransformMatrix2D toMatrix();

    /** Return a transform representing the identity transform.
     * @return a transform representing the identity transform
     */
    static Transform2D identity() {
        return FunctionTransform2D.identity();
    }

    /** Construct a transform instance from the given function. Callers are responsible for
     * ensuring that the given function meets all the requirements for
     * {@link org.apache.commons.geometry.core.Transform Transform} instances.
     * @param fn the function to use for the transform
     * @return a new transform instance using the given function
     */
    static Transform2D from(final UnaryOperator<Vector2D> fn) {
        return FunctionTransform2D.from(fn);
    }
}
