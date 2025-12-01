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
package org.apache.commons.geometry.core;

/** Interface representing a point in a mathematical space.
 *
 * <p>Implementations of this interface are sufficient to define a
 * space since they define both the structure of the points making up
 * the space and the operations permitted on them. The only mathematical
 * requirement at this level is that the represented space have a defined
 * distance metric, meaning an operation that can compute the distance
 * between two points (ie, the space must be a metric space).
 * </p>
 *
 * <p>This interface uses self-referencing generic parameters to ensure
 * that implementations are only used with instances of their own type.
 * This removes the need for casting inside of methods in order to access
 * implementation-specific data, such as coordinate values.
 * </p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Metric_space">Metric space</a>
 *
 * @param <P> Point implementation type
 */
public interface Point<P extends Point<P>> extends Spatial {

    /** Compute the distance between this point and another point.
     * @param p second point
     * @return the distance between this point and p
     */
    double distance(P p);
}
