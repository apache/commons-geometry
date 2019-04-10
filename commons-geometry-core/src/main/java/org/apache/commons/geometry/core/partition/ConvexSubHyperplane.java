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
import org.apache.commons.geometry.core.Transform;

/** Extension of the {@link SubHyperplane} interface with the additional restriction
 * that instances represent convex regions of space.
 * @param <P> Point implementation type
 */
public interface ConvexSubHyperplane<P extends Point<P>> extends SubHyperplane<P> {

    /** Split a convex subhyperplane with a hyperplane.
     * @param splitter the splitting hyperplane
     * @return the results of the split operation
     */
    Split<P> split(Hyperplane<P> splitter);

    ConvexSubHyperplane<P> transform(Transform<P> transform);

    /** Class containing the result of splitting a convex subhyperplane with a hyperplane.
     * @param <P> Point implementation type
     */
    static class Split<P extends Point<P>> {

        /** Part of the sub-hyperplane on the minus side of the splitting hyperplane. */
        private final ConvexSubHyperplane<P> minus;

        /** Part of the  sub-hyperplane on the plus side of the splitting hyperplane. */
        private final ConvexSubHyperplane<P> plus;

        /** Build a SplitSubHyperplane from its parts.
         * @param minus part of the sub-hyperplane on the minus side of the
         *      splitting hyperplane
         * @param plus part of the sub-hyperplane on the plus side of the
         *      splitting hyperplane
         */
        public Split(final ConvexSubHyperplane<P> minus, final ConvexSubHyperplane<P> plus) {
            this.plus  = plus;
            this.minus = minus;
        }

        /** Get the part of the sub-hyperplane on the minus side of the splitting hyperplane.
         * @return part of the sub-hyperplane on the minus side of the splitting hyperplane
         */
        public ConvexSubHyperplane<P> getMinus() {
            return minus;
        }

        /** Get the part of the sub-hyperplane on the plus side of the splitting hyperplane.
         * @return part of the sub-hyperplane on the plus side of the splitting hyperplane
         */
        public ConvexSubHyperplane<P> getPlus() {
            return plus;
        }

        /** Get the location of the split sub-hyperplane with respect to its splitter.
         * @return {@link SplitLocation#PLUS} if only {@link #getPlus()} is neither null nor empty,
         * {@link SplitLocation#MINUS} if only {@link #getMinus()} is neither null nor empty,
         * {@link SplitLocation#BOTH} if both {@link #getPlus()} and {@link #getMinus()}
         * are neither null nor empty or {@link SplitLocation#ON} if both {@link #getPlus()} and
         * {@link #getMinus()} are either null or empty
         */
        public SplitLocation getLocation() {
            if (plus != null && !plus.isEmpty()) {
                if (minus != null && !minus.isEmpty()) {
                    return SplitLocation.BOTH;
                }
                else {
                    return SplitLocation.PLUS;
                }
            }
            else if (minus != null && !minus.isEmpty()) {
                return SplitLocation.MINUS;
            }
            else {
                return SplitLocation.ON;
            }
        }
    }
}
