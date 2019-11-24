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
package org.apache.commons.geometry.core.partitioning;

import org.apache.commons.geometry.core.Embedding;
import org.apache.commons.geometry.core.Point;

/** Hyperplane that also embeds a subspace.
 * @param <P> Point implementation type
 * @param <S> Subspace point implementation type
 * @see Hyperplane
 * @see Embedding
 */
public interface EmbeddingHyperplane<P extends Point<P>, S extends Point<S>>
    extends Hyperplane<P>, Embedding<P, S> {
}
