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
package org.apache.commons.geometry.core;

/** Interface representing a generic element in a mathematical space.
 */
public interface Spatial {

    /** Returns the number of dimensions in the space that this element
     * belongs to.
     * @return the number of dimensions in the element's space
     */
    int getDimension();

    /** Returns true if any value in this element is NaN; otherwise
     * returns false.
     * @return true if any value in this element is NaN
     */
    boolean isNaN();

    /** Returns true if any value in this element is infinite and none
     * are NaN; otherwise, returns false.
     * @return true if any value in this element is infinite and none
     *      are NaN
     */
    boolean isInfinite();

    /** Returns true if all values in this element are finite, meaning
     * they are not NaN or infinite.
     * @return true if all values in this element are finite
     */
    boolean isFinite();
}
