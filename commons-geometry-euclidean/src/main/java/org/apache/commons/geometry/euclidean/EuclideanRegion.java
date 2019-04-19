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

import org.apache.commons.geometry.core.Region;

public interface EuclideanRegion<V extends EuclideanVector<V>> extends Region<V> {

    /** Get the size of the region. The units of this value will depend on the
     * dimensions of the region.
     * @return the size of the region
     */
    double size();

    /** Get the barycenter of the region or null if no barycenter exists.
     * @return the barycenter of the region or null if none exists
     */
    V barycenter();
}
