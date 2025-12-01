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

/** Interface representing a geometric element that embeds a region in a subspace.
 * @param <P> Point type defining the embedding space.
 * @param <S> Point type defining the embedded subspace.
 * @see Embedding
 * @see Region
 */
public interface RegionEmbedding<P extends Point<P>, S extends Point<S>>
    extends Embedding<P, S> {

    /** Get the embedded subspace region.
     * @return the embedded subspace region
     */
    Region<S> getSubspaceRegion();
}
