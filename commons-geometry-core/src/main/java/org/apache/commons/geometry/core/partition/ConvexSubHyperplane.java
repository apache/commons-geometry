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
import org.apache.commons.geometry.core.Transform;

/** Extension of the {@link SubHyperplane} interface with the additional restriction
 * that instances represent convex regions of space.
 * @param <P> Point implementation type
 */
public interface ConvexSubHyperplane<P extends Point<P>> extends SubHyperplane<P> {

    /** Split a convex subhyperplane with a hyperplane.
     * @param splitter the splitting hyperplane
     * @return the results of the split operation
     */
    @Override
    Split<? extends ConvexSubHyperplane<P>> split(Hyperplane<P> splitter);

    /** Transform this instance using the argument.
     * @param transform the transform instance to apply
     * @return transformed convex subhyerplane
     */
    ConvexSubHyperplane<P> transform(Transform<P> transform);
}
