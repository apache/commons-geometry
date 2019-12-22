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
 *
 * <p>
 * This package contains code related to partitioning of spaces by hyperplanes.
 * </p>
 *
 * <p>
 * A hyperplane is a subspace of dimension one less than its surrounding space.
 * In Euclidean 3D space, the hyperplanes are 2-dimensional planes, while in
 * Euclidean 2D space, the hyperplanes are 1-dimensional lines. Hyperplanes have
 * the property that they partition the entire surrounding space into 3 distinct sets
 * of points: (1) points lying on one side of the hyperplane, (2) points lying on the
 * opposite side, and (3) points lying directly on the hyperplane itself. In order
 * to differentiate between the two sides of the hyperplane, one side is labeled
 * as the <em>plus</em> side and the other as the <em>minus</em> side. The plus side
 * of a Euclidean plane, for example, lies in the direction of the plane normal.
 * </p>
 */
package org.apache.commons.geometry.core.partitioning;
