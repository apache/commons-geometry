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

/** Interface specifying basic methods of traversing Binary Space Partitioning
 * (BSP) trees.
 */
public interface BSPTreeTraversal<P extends Point<P>, T> {

    /** Call the given {@link BSPTreeVisitor} with each node from the
     * tree.
     * @param visitor visitor call with each tree node
     */
    void visit(BSPTreeVisitor<P, T> visitor);
}
