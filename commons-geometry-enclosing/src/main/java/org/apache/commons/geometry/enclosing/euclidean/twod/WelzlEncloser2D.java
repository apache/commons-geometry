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
package org.apache.commons.geometry.enclosing.euclidean.twod;

import org.apache.commons.geometry.enclosing.WelzlEncloser;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.core.Precision;

/** Extension of the {@link WelzlEncloser} class for Euclidean 2D space. This is
 * primarily a convenience class to simplify instantiation.
 */
public class WelzlEncloser2D extends WelzlEncloser<Vector2D> {

    /** Construct a new instance with the given precision context. A new {@link DiskGenerator}
     * instance is used as the support ball generator.
     * @param precision precision context to use for floating point comparisons.
     */
    public WelzlEncloser2D(final Precision.DoubleEquivalence precision) {
        super(new DiskGenerator(), precision);
    }
}
