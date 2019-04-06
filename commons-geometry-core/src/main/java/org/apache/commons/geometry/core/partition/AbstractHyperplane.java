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
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

public abstract class AbstractHyperplane<P extends Point<P>> implements Hyperplane<P> {

    private final DoublePrecisionContext precision;

    protected AbstractHyperplane(final DoublePrecisionContext precision) {
        this.precision = precision;
    }

    /** {@inheritDoc} */
    @Override
    public Side classify(final P point) {
        final double offsetValue = offset(point);
        final int cmp = precision.sign(offsetValue);
        if (cmp > 0) {
            return Side.PLUS;
        }
        else if (cmp < 0) {
            return Side.MINUS;
        }
        return Side.HYPER;
    }

    public DoublePrecisionContext getPrecision() {
        return precision;
    }
}
