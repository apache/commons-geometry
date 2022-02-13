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
package org.apache.commons.geometry.euclidean;

import org.apache.commons.geometry.core.internal.AbstractSingleDimensionPointMap;
import org.apache.commons.geometry.euclidean.oned.PointMap1D;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.numbers.core.Precision;

/** Internal implementation of {@link PointMap1D}.
 * @param <V> Map value type
 */
final class PointMap1DImpl<V>
    extends AbstractSingleDimensionPointMap<Vector1D, V>
    implements PointMap1D<V> {

    /** Construct a new instance using the given precision context to determine
     * floating point equality.
     * @param precision precision context
     */
    PointMap1DImpl(final Precision.DoubleEquivalence precision) {
        super((a, b) -> precision.compare(a.getX(), b.getX()));
    }
}
