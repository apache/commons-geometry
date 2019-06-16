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

/** Class containing the result of splitting an object with a hyperplane.
 * @param <T> Split type
 */
public class Split<T> {

    /** Part of the object lying on the minus side of the splitting hyperplane.
     */
    private final T minus;

    /** Part of the object lying on the plus side of the splitting hyperplane.
     */
    private final T plus;

    /** Build a new instance from its parts.
     * @param minus part of the object lying on the minus side of the
     *      splitting hyperplane or null if no such part exists
     * @param plus part of the object lying on the plus side of the
     *      splitting hyperplane or null if no such part exists.
     */
    public Split(final T minus, final T plus) {
        this.minus = minus;
        this.plus = plus;
    }

    /** Get the part of the object lying on the minus side of the splitting
     * hyperplane or null if no such part exists.
     * @return part of the object lying on the minus side of the splitting
     *      hyperplane
     */
    public T getMinus() {
        return minus;
    }

    /** Get the part of the object lying on the plus side of the splitting
     * hyperplane or null if no such part exists.
     * @return part of the object lying on the plus side of the splitting
     *      hyperplane
     */
    public T getPlus() {
        return plus;
    }

    /** Get the location of the object with respect to its splitting
     * hyperplane.
     * @return
     *  <ul>
     *      <li>{@link SplitLocation#PLUS} - if only {@link #getPlus()} is not null</li>
     *      <li>{@link SplitLocation#MINUS} - if only {@link #getMinus()} is not null</li>
     *      <li>{@link SplitLocation#BOTH} - if both {@link #getPlus()} and {@link #getMinus()}
     *          are not null</li>
     *      <li>{@link SplitLocation#NEITHER} - if both {@link #getPlus()} and {@link #getMinus()}
     *          are null</li>
     *  </ul>
     */
    public SplitLocation getLocation() {
        if (minus != null) {
            return plus != null ? SplitLocation.BOTH : SplitLocation.MINUS;
        }
        else if (plus != null) {
            return SplitLocation.PLUS;
        }
        return SplitLocation.NEITHER;
    }
}
