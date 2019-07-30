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
package org.apache.commons.geometry.euclidean.twod;

import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.partition.AbstractEmbeddingSubHyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.twod.SubLine.SubLineBuilder;

/** Internal base class for subline implementations.
 */
abstract class AbstractSubLine<R extends Region<Vector1D>>
    extends AbstractEmbeddingSubHyperplane<Vector2D, Vector1D, Line> {

    /** Serializable UID */
    private static final long serialVersionUID = 20190729L;

    /** The line defining this instance. */
    private final Line line;

    AbstractSubLine(final Line line) {
        this.line = line;
    }

    /** Get the line that this segment lies on. This method is an alias
     * for {@link getHyperplane()}.
     * @return the line that this segment lies on
     * @see #getHyperplane()
     */
    public Line getLine() {
        return getHyperplane();
    }

    /** {@inheritDoc} */
    @Override
    public Line getHyperplane() {
        return line;
    }

    /** {@inheritDoc} */
    @Override
    public SubLineBuilder builder() {
        return new SubLineBuilder(line);
    }

    /** Return the object used to perform floating point comparisons, which is the
     * same object used by the underlying {@link Line).
     * @return precision object used to perform floating point comparisons.
     */
    public DoublePrecisionContext getPrecision() {
        return line.getPrecision();
    }
}
