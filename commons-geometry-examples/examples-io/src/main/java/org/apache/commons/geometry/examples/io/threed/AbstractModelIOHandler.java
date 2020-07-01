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
package org.apache.commons.geometry.examples.io.threed;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;

/** Abstract base class for {@link ModelIOHandler} implementations.
 */
public abstract class AbstractModelIOHandler implements ModelIOHandler {

    /** {@inheritDoc} */
    @Override
    public BoundarySource3D read(final String type, final File in, final DoublePrecisionContext precision) {
        ensureTypeSupported(type);
        try {
            try (InputStream is = Files.newInputStream(in.toPath())) {
                return readInternal(type, is, precision);
            }
        } catch (IOException exc) {
            throw createUnchecked(exc);
        }
    }

    /** {@inheritDoc} */
    @Override
    public BoundarySource3D read(final String type, final InputStream in, final DoublePrecisionContext precision) {
        ensureTypeSupported(type);
        try {
            return readInternal(type, in, precision);
        } catch (IOException exc) {
            throw createUnchecked(exc);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void write(final BoundarySource3D model, final String type, final File out) {
        ensureTypeSupported(type);
        try {
            try (OutputStream os = Files.newOutputStream(out.toPath())) {
                writeInternal(model, type, os);
            }
        } catch (IOException exc) {
            throw createUnchecked(exc);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void write(final BoundarySource3D model, final String type, OutputStream out) {
        ensureTypeSupported(type);
        try {
            writeInternal(model, type, out);
        } catch (IOException exc) {
            throw createUnchecked(exc);
        }
    }

    /** Throw an exception if the given type is not supported by this instance.
     * @param type model type to check
     */
    private void ensureTypeSupported(final String type) {
        if (!handlesType(type)) {
            throw new IllegalArgumentException("File type is not supported by this handler: " + type);
        }
    }

    /** Create an unchecked exception from the given checked exception.
     * @param exc exception to wrap in an unchecked exception
     * @return the unchecked exception
     */
    private UncheckedIOException createUnchecked(final IOException exc) {
        final String msg = exc.getClass().getSimpleName() + ": " + exc.getMessage();
        return new UncheckedIOException(msg, exc);
    }

    /** Internal class used to read a model. {@link IOException}s thrown from here are
     * wrapped in {@link java.io.UncheckedIOException}s.
     * @param type model type; guaranteed to be supported by this instance
     * @param in input stream
     * @param precision precision context used to construct the model
     * @return 3D model
     * @throws IOException if an IO operation fails
     */
    protected abstract BoundarySource3D readInternal(String type, InputStream in, DoublePrecisionContext precision)
            throws IOException;

    /** Internal class used to write a model. {@link IOException}s thrown from here are
     * wrapped in {@link java.io.UncheckedIOException}s.
     * @param model model to write
     * @param type model type; guaranteed to be supported by this instance
     * @param out input stream
     * @throws IOException if an IO operation fails
     */
    protected abstract void writeInternal(BoundarySource3D model, String type, OutputStream out) throws IOException;
}
