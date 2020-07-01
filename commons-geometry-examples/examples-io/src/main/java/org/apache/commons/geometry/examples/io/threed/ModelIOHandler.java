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

/** Interface for classes that handle reading and writing of 3D model files types.
 * For convenience and better compatibility with streams and functional programming,
 * all IO methods throw {@link java.io.UncheckedIOException} instead of {@link java.io.IOException}.
 *
 * <p>Implementations of this interface are expected to be thread-safe.</p>
 */
public interface ModelIOHandler {

    /** Return true if this instance handles 3D model files of the given type.
     * @param type type 3D model type, indicated by file extension
     * @return true if this instance can handle the 3D model file type
     */
    boolean handlesType(String type);

    /** Read a 3D model represented as a {@link BoundarySource3D} from the given file.
     * @param type the model file type
     * @param in file to read
     * @param precision precision context to use in model construction
     * @return a 3D model represented as a boundary source
     * @throws java.io.UncheckedIOException if an IO operation fails
     * @throws IllegalArgumentException if the file type is not supported
     */
    BoundarySource3D read(String type, File in, DoublePrecisionContext precision);

    /** Read a 3D model represented as a {@link BoundarySource3D} from the given input stream.
     * The input stream is closed before method return.
     * @param type the model input type
     * @param in input stream
     * @param precision precision context to use in model construction
     * @return a 3D model represented as a boundary source
     * @throws java.io.UncheckedIOException if an IO operation fails
     * @throws IllegalArgumentException if the file type is not supported
     */
    BoundarySource3D read(String type, InputStream in, DoublePrecisionContext precision);

    /** Write the model to the file using the specified file type.
     * @param model model to write
     * @param type the model file type
     * @param out output file
     * @throws java.io.UncheckedIOException if an IO operation fails
     * @throws IllegalArgumentException if the file type is not supported
     */
    void write(BoundarySource3D model, String type, File out);

    /** Write the model to the given output stream, using the specified model type.
     * The output stream is closed before method return.
     * @param model model to write
     * @param type the model file type
     * @param out output stream
     * @throws java.io.UncheckedIOException if an IO operation fails
     * @throws IllegalArgumentException if the file type is not supported
     */
    void write(BoundarySource3D model, String type, OutputStream out);
}
