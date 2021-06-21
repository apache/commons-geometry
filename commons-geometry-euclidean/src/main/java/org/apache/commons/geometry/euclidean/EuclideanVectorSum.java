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

import java.util.function.Consumer;
import java.util.function.Supplier;

/** Class representing a sum of Euclidean vectors.
 * @param <V> Vector implementation type
 */
public abstract class EuclideanVectorSum<V extends EuclideanVector<V>>
    implements Supplier<V>, Consumer<V> {

    /** Add a vector to this instance. This method is an alias for {@link #add(EuclideanVector)}.
     * @param vec vector to add
     */
    @Override
    public void accept(final V vec) {
        add(vec);
    }

    /** Add a vector to this instance.
     * @param vec vector to add
     * @return this instance
     */
    public abstract EuclideanVectorSum<V> add(V vec);

    /** Add a scaled vector to this instance. In general, the result produced by this method
     * will be more accurate than if the vector was scaled first and then added directly. In other
     * words, {@code sum.addScale(scale, vec)} will generally produce a better result than
     * {@code sum.add(vec.multiply(scale))}.
     * @param scale scale factor
     * @param vec vector to scale and add
     * @return this instance
     */
    public abstract EuclideanVectorSum<V> addScaled(double scale, V vec);
}
