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
 * This package is the top-level package for Euclidean geometry components.
 *
 * <h2>Definition</h2>
 * <p>
 * Euclidean space is the space commonly thought of when people think of
 * geometry. It corresponds with the common notion of "flat" space or the space
 * that we usually experience in the physical world. Mathematically, Euclidean
 * space is an <a href="https://en.wikipedia.org/wiki/Affine_space">affine
 * space</a>, meaning that it consists of points and displacement vectors
 * representing translations between points. Distances between points are given
 * by the formula <code>&radic;(A - B)<sup>2</sup></code>, which is also known
 * as the <em>Euclidean norm</em>.
 * </p>
 *
 * <h2>Points and Vectors</h2>
 * <p>
 * As mentioned above, points and vectors are separate, distinct entities:
 * points represent locations in a space and vectors represent displacements.
 * This difference is the reason that commons-geometry has separate
 * {@link org.apache.commons.geometry.core.Point Point} and
 * {@link org.apache.commons.geometry.core.Vector Vector} interfaces. However,
 * in the case of Euclidean space, the data structures used for points and
 * vectors are identical and there is overlap in the methods needed for each
 * type. Creating separate classes for Euclidean points and vectors therefore
 * means a large amount of very similar or exactly duplicated code in order to
 * maintain mathematical purity. This is not desirable, so a compromise position
 * has been taken: there is a single class for each dimension that implements
 * both {@link org.apache.commons.geometry.core.Point Point} <em>and</em>
 * {@link org.apache.commons.geometry.core.Vector Vector}. These classes are
 * named <code>Vector?D</code> to reflect the fact that they support the full
 * range of vector operations. It is up to users of the library to make the
 * correct distinctions between instances that represent points and those that
 * represent displacements. This approach is commonly used in other geometric
 * libraries as well, such as the
 * <a href="https://www.khronos.org/opengl/wiki/OpenGL_Shading_Language">OpenGL
 * Shading Language (GLSL)</a>, <a href=
 * "https://casual-effects.com/g3d/G3D10/G3D-base.lib/include/G3D-base/Vector3.h">G3D</a>,
 * and <a href=
 * "https://threejs.org/docs/index.html#api/en/math/Vector3">Three.js</a>.
 * </p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Euclidean_space">Euclidean
 *      Space</a>
 */
package org.apache.commons.geometry.euclidean;
