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
}