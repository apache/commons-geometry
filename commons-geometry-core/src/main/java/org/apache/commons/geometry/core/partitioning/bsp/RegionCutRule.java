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
package org.apache.commons.geometry.core.partitioning.bsp;

/** Enum describing the possible behaviors when cutting a region BSP tree node
 * with a hyperplane to produce two new child nodes.
 */
public enum RegionCutRule {

    /** Set the minus side of the cutting hyperplane as the inside of the region
     * and the plus side as the outside. This is the default convention for hyperplanes.
     */
    MINUS_INSIDE,

    /** Set the plus side of the cutting hyperplane as the inside of the region and
     * the minus side as the outside.
     */
    PLUS_INSIDE,

    /** Set both child nodes to the same location as the parent node. For example, if the
     * parent node is marked as inside, both child nodes will be marked as inside. Similarly
     * if the parent node is marked as outside, both child nodes will be marked as outside.
     * This rule can be used to modify the tree structure (to perhaps produce a more efficient,
     * balanced tree) without changing the represented region.
     */
    INHERIT
}
