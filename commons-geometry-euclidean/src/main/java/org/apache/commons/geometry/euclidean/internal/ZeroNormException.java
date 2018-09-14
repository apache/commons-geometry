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
package org.apache.commons.geometry.euclidean.internal;

/** Internal exception class with constants for frequently used messages.
 * This exception is thrown when vector operations requiring a non-zero
 * vector norm are attempted with a vector with a zero norm.
 */
public class ZeroNormException extends IllegalStateException {

    /** Default zero-norm error message. */
    public static final String ZERO_NORM_MSG = "Norm is zero";

    /** Error message for cases where code is attempting to use a zero-norm vector
     * as a base vector.
     */
    public static final String INVALID_BASE = "Invalid base vector: norm is zero";

    /** Serializable version identifier. */
    private static final long serialVersionUID = 20180903L;

    /**
     * Simple constructor, using the default error message.
     */
    public ZeroNormException() {
        this(ZERO_NORM_MSG);
    }

    /**
     * Constructs an instance with the given error message.
     * @param msg error message
     */
    public ZeroNormException(String msg) {
        super(msg);
    }
}
