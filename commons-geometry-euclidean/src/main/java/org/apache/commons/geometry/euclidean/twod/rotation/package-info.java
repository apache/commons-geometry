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
/**
 * <p>
 * This package provides components related to rotations in 2 dimensional
 * Euclidean space.
 * </p>
 *
 * <h2>Conventions</h2>
 * <p>
 * The following list contains some of the primary rotation conventions for this package.
 * All method parameters, return values, and operations follow these conventions unless
 * explicitly stated otherwise.
 *
 * <ul>
 *      <li><strong>Active</strong> -- All rotations are "active", meaning that
 *      they transform the vector or point they are applied to instead of transforming the
 *      coordinate system. An active rotation can be converted to a passive one and vice
 *      versa simply by taking the inverse. See
 *      <a href="https://en.wikipedia.org/wiki/Active_and_passive_transformation">here</a> for more details.
 *      </li>
 *      <li><strong>Counter-clockwise</strong> -- All rotations through positive angles occur in
 *      a counter-clockwise direction. Rotations through negative angles are in a clockwise direction.
 *      </li>
 *      <li><strong>Radians</strong> -- All angles are in radians.
 *      </li>
 * </ul>
 */
package org.apache.commons.geometry.euclidean.twod.rotation;
