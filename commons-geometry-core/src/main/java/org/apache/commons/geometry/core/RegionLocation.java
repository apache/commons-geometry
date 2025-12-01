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

/** Enumeration containing the possible locations of a point with
 * respect to a region.
 * @see Region
 */
public enum RegionLocation {

    /** Value indicating that a point lies on the inside of
     * a region.
     */
    INSIDE,

    /** Value indicating that a point lies on the outside of
     * a region.
     */
    OUTSIDE,

    /** Value indicating that a point lies on the boundary of
     * a region.
     */
    BOUNDARY
}
