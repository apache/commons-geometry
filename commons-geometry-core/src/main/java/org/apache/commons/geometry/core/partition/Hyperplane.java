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
package org.apache.commons.geometry.core.partition;

import org.apache.commons.geometry.core.Point;

public interface Hyperplane<P extends Point<P>> {

    double offset(P point);

    Side classify(P point);

    P project(P point);

    Hyperplane<P> transform(Transform<P> transform);

    /** Return true if this instance has a similar orientation to the given hyperplane,
     * meaning that they point in generally the same direction. This method is not
     * used to determine exact equality of hyperplanes, but rather to determine whether
     * two hyperplanes that contain the same points are parallel (point in the same direction)
     * or anti-parallel (point in opposite directions).
     * @param other the hyperplane to compare with
     * @return true if the hyperplanes point in generally the same direction and could
     *      possibly be parallel
     */
    boolean similarOrientation(Hyperplane<P> other);

    ConvexSubHyperplane<P> wholeHyperplane();
}
