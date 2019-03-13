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

/** Enumeration representing the location of an element with respect to a
 * {@link Hyperplane hyperplane}.
 */
public enum Side {

    /** Value indicating that an element lies on the plus side of a
     * hyperplane.
     */
    PLUS,

    /** Value indicating that an element lies on the minus side of a
     * hyperplane.
     */
    MINUS,

    /** Value indicating that an element cross the hyperplane and lies
     * on both the plus and minus sides.
     */
    BOTH,

    /** Value indicating that an element lies directly on the hyperplane
     * itself.
     */
    HYPER;

}
