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

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;

/** Utility class containing constants and static convenience methods related to 3D model
 * input and output.
 */
public final class ModelIO {

    /** String representing the OBJ file format.
     * @see <a href="https://en.wikipedia.org/wiki/Wavefront_.obj_file">Wavefront .obj file</a>
     */
    public static final String OBJ = "obj";

    /** Singleton handler registry. */
    private static final ModelIOHandlerRegistry HANDLER_REGISTRY = new DefaultModelIOHandlerRegistry();

    /** Utility class; no instantiation. */
    private ModelIO() {}

    /** Get the {@link ModelIOHandlerRegistry} singleton instance.
     * @return the {@link ModelIOHandlerRegistry} singleton instance
     */
    public static ModelIOHandlerRegistry getModelIOHandlerRegistry() {
        return HANDLER_REGISTRY;
    }

    /** Read a 3D model from the given file, using the file extension as the model type. The call is delegated
     * to the {@link ModelIOHandlerRegistry} singleton.
     * @param in file to read
     * @param precision precision context to use in model construction
     * @return a 3D model represented as a boundary source
     * @throws java.io.UncheckedIOException if an IO operation fails
     * @throws IllegalArgumentException if the file does not have a file extension or the
     *      file extension does not indicate a supported model type
     * @see #getModelIOHandlerRegistry()
     * @see ModelIOHandlerRegistry#read(File, DoublePrecisionContext)
     */
    public static BoundarySource3D read(final File in, final DoublePrecisionContext precision) {
        return HANDLER_REGISTRY.read(in, precision);
    }

    /** Read a 3D model of the given type from the file. The call is delegated to the {@link ModelIOHandlerRegistry}
     * singleton.
     * @param type model input type
     * @param in input file
     * @param precision precision context to use in model construction
     * @return a 3D model represented as a boundary source
     * @throws java.io.UncheckedIOException if an IO operation fails
     * @throws IllegalArgumentException if the model input type is not supported
     * @see #getModelIOHandlerRegistry()
     * @see ModelIOHandler#read(String, File, DoublePrecisionContext)
     */
    public static BoundarySource3D read(final String type, final File in, final DoublePrecisionContext precision) {
        return HANDLER_REGISTRY.read(type, in, precision);
    }

    /** Read a 3D model of the given type from the input stream. The call is delegated to the
     * {@link ModelIOHandlerRegistry} singleton.
     * @param type model input type
     * @param in input stream to read from
     * @param precision precision context to use in model construction
     * @return a 3D model represented as a boundary source
     * @throws java.io.UncheckedIOException if an IO operation fails
     * @throws IllegalArgumentException if the model input type is not supported
     * @see #getModelIOHandlerRegistry()
     * @see ModelIOHandler#read(String, InputStream, DoublePrecisionContext)
     */
    public static BoundarySource3D read(final String type, final InputStream in,
            final DoublePrecisionContext precision) {
        return HANDLER_REGISTRY.read(type, in, precision);
    }

    /** Write the model to the file. The file type is determined by the file extension of the target file.
     * The call is delegated to the {@link ModelIOHandlerRegistry} singleton.
     * @param model model to write
     * @param out output file
     * @throws java.io.UncheckedIOException if an IO operation fails
     * @throws IllegalArgumentException if the file does not have a file extension or the
     *      file extension does not indicate a supported model type
     * @see #getModelIOHandlerRegistry()
     * @see ModelIOHandlerRegistry#write(BoundarySource3D, File)
     */
    public static void write(final BoundarySource3D model, final File out) {
        HANDLER_REGISTRY.write(model, out);
    }

    /** Write the model to the file using the specified file type. The call is delegated to the
     * {@link ModelIOHandlerRegistry} singleton.
     * @param model model to write
     * @param type model file type
     * @param out output file
     * @throws java.io.UncheckedIOException if an IO operation fails
     * @throws IllegalArgumentException if the file type is not supported
     * @see #getModelIOHandlerRegistry()
     * @see ModelIOHandler#write(BoundarySource3D, String, File)
     */
    public static void write(final BoundarySource3D model, final String type, final File out) {
        HANDLER_REGISTRY.write(model, type, out);
    }

    /** Write the model to the output stream using the specific file type. The call is delegated to the
     * {@link ModelIOHandlerRegistry} singleton.
     * @param model model to write
     * @param type model file type
     * @param out output stream
     * @throws java.io.UncheckedIOException if an IO operation fails
     * @throws IllegalArgumentException if the file type is not supported
     * @see #getModelIOHandlerRegistry()
     * @see ModelIOHandler#write(BoundarySource3D, String, OutputStream)
     */
    public static void write(final BoundarySource3D model, final String type, final OutputStream out) {
        HANDLER_REGISTRY.write(model, type, out);
    }
}
