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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;

/** Object that holds an internal registry {@link ModelIOHandler} and delegates
 * read/write operations to them as determined by their supported model types.
 *
 * <p>Instances of this class are thread-safe as long as all registered handlers are
 * also thread-safe.</p>
 */
public class ModelIOHandlerRegistry implements ModelIOHandler {

    /** Handler list. */
    private final List<ModelIOHandler> handlers = new ArrayList<>();

    /** Get the {@link ModelIOHandler} for the given type or null if no
     * handler is found.
     * @param type model type to retrieve the handler for
     * @return the handler for the given type or null if no handler is found
     */
    public ModelIOHandler getHandlerForType(final String type) {
        synchronized (handlers) {
            for (final ModelIOHandler handler : handlers) {
                if (handler.handlesType(type)) {
                    return handler;
                }
            }

            return null;
        }
    }

    /** Get the list of registered {@link ModelIOHandler}s.
     * @return the registered {@link ModelIOHandler}s
     */
    public List<ModelIOHandler> getHandlers() {
        synchronized (handlers) {
            return Collections.unmodifiableList(new ArrayList<>(handlers));
        }
    }

    /** Set the list of registered {@link ModelIOHandler}s.
     * @param newHandlers the new list of {@link ModelIOHandler}s.
     */
    public void setHandlers(final List<ModelIOHandler> newHandlers) {
        synchronized (handlers) {
            handlers.clear();

            if (newHandlers != null) {
                handlers.addAll(newHandlers);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean handlesType(final String type) {
        return getHandlerForType(type) != null;
    }

    /** Read a 3D model from the given file, using the file extension as the model type.
     * @param in file to read from
     * @param precision precision context to use in model construction
     * @return a 3D model represented as a boundary source
     * @throws java.io.UncheckedIOException if an IO operation fails
     * @throws IllegalArgumentException if the file does not have a file extension or the
     *      file extension does not indicate a supported model type
     */
    public BoundarySource3D read(final File in, final DoublePrecisionContext precision) {
        return read(getFileExtension(in), in, precision);
    }

    /** {@inheritDoc} */
    @Override
    public BoundarySource3D read(final String type, final File in, final DoublePrecisionContext precision) {
        return requireHandlerForType(type).read(type, in, precision);
    }

    /** {@inheritDoc} */
    @Override
    public BoundarySource3D read(final String type, final InputStream in, final DoublePrecisionContext precision) {
        return requireHandlerForType(type).read(type, in, precision);
    }

    /** Write the model to the file. The file type is determined by the file extension
     * of the target file.
     * @param model model to write
     * @param out output file
     * @throws java.io.UncheckedIOException if an IO operation fails
     * @throws IllegalArgumentException if the file does not have a file extension or the
     *      file extension does not indicate a supported model type
     */
    public void write(final BoundarySource3D model, final File out) {
        write(model, getFileExtension(out), out);
    }

    /** {@inheritDoc} */
    @Override
    public void write(final BoundarySource3D model, final String type, final File out) {
        requireHandlerForType(type).write(model, type, out);
    }

    /** {@inheritDoc} */
    @Override
    public void write(final BoundarySource3D model, final String type, final OutputStream out) {
        requireHandlerForType(type).write(model, type, out);
    }

    /** Get the file extension of the given file, throwing an exception if one cannot be found.
     * @param file the file to get the extension for
     * @return the file extension
     * @throws IllegalArgumentException if the file does not have a file extension
     */
    private String getFileExtension(final File file) {
        final String name = file.getName();
        final int idx = name.lastIndexOf('.');
        if (idx > -1) {
            return name.substring(idx + 1).toLowerCase();
        }

        throw new IllegalArgumentException("Cannot determine target file type: \"" + file +
                "\" does not have a file extension");
    }

    /** Get the handler for the given type, throwing an exception if not found.
     * @param type model type
     * @return the handler for the given type
     * @throws IllegalArgumentException if no handler for the type is found
     */
    private ModelIOHandler requireHandlerForType(final String type) {
        final ModelIOHandler handler = getHandlerForType(type);
        if (handler == null) {
            throw new IllegalArgumentException("No handler found for type \"" + type + "\"");
        }

        return handler;
    }
}
