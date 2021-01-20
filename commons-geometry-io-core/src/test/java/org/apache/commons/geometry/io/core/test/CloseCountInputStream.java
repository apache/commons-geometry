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
package org.apache.commons.geometry.io.core.test;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/** {@code InputStream} that counts how many times the {@link #close()}
 * method has been invoked.
 */
public class CloseCountInputStream extends FilterInputStream {

    /** Number of times close() has been called on this instance. */
    private int closeCount;

    /** Construct a new instance that delegates all calls to the
     * given input stream.
     * @param in underlying input stream
     */
    public CloseCountInputStream(final InputStream in) {
        super(in);
    }

    /** Get the number of times {@link #close()} has been invoked
     * on this instance.
     * @return number of times {@link #close()} has been invoked
     */
    public int getCloseCount() {
        return closeCount;
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws IOException {
        ++closeCount;

        super.close();
    }
}
