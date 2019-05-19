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

import java.io.Serializable;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** Base class for hyperplane implementations.
 * @param <P> Point implementation type
 */
public abstract class AbstractHyperplane<P extends Point<P>> implements Hyperplane<P>, Serializable {

    /** Serializable UID */
    private static final long serialVersionUID = 1L;

    /** Precision object used to perform floating point comparisons */
    private final DoublePrecisionContext precision;

    /** Construct an instance using the given precision context.
     * @param precision object used to perform floating point comparisons
     */
    protected AbstractHyperplane(final DoublePrecisionContext precision) {
        this.precision = precision;
    }

    /** {@inheritDoc} */
    @Override
    public HyperplaneLocation classify(final P point) {
        final double offsetValue = offset(point);

        final int cmp = precision.sign(offsetValue);
        if (cmp > 0) {
            return HyperplaneLocation.PLUS;
        }
        else if (cmp < 0) {
            return HyperplaneLocation.MINUS;
        }
        return HyperplaneLocation.ON;
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(final P point) {
        final HyperplaneLocation loc = classify(point);
        return loc == HyperplaneLocation.ON;
    }

    /** Get the precision object used to perform floating point
     * comparisons for this instance.
     * @return the precision object for this instance
     */
    public DoublePrecisionContext getPrecision() {
        return precision;
    }
}
