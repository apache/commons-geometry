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
package org.apache.commons.geometry.io.euclidean.threed.obj;

import java.io.Closeable;
import java.io.Reader;

import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.io.core.internal.GeometryIOUtils;

/** Abstract base class for types that read OBJ polygon content using
 * {@link PolygonObjParser}.
 */
public abstract class AbstractObjPolygonReader implements Closeable {

    /** Underlying reader. */
    private final Reader reader;

    /** OBJ polygon parser. */
    private final PolygonObjParser parser;

    /** Construct a new instance that reads OBJ content from the given reader.
     * @param reader reader to read characters from
     */
    protected AbstractObjPolygonReader(final Reader reader) {
        this.reader = reader;
        this.parser = new PolygonObjParser(reader);
    }

    /** Get the flag indicating whether or not an {@link IllegalStateException} will be thrown
     * if the OBJ content contains any keywords defining non-polygon geometric content
     * (ex: {@code curv}). If false, non-polygon data is ignored.
     * @return flag indicating whether or not an {@link IllegalStateException} will be thrown
     *      if non-polygon content is encountered
     * @see PolygonObjParser#isFailOnNonPolygonKeywords()
     */
    public boolean isFailOnNonPolygonKeywords() {
        return parser.isFailOnNonPolygonKeywords();
    }

    /** Set the flag indicating whether or not an {@link IllegalStateException} will be thrown
     * if the OBJ content contains any keywords defining non-polygon geometric content
     * (ex: {@code curv}). If set to false, non-polygon data is ignored.
     * @param fail flag indicating whether or not an {@link IllegalStateException} will be thrown
     *      if non-polygon content is encountered
     */
    public void setFailOnNonPolygonKeywords(final boolean fail) {
        parser.setFailOnNonPolygonKeywords(fail);
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        GeometryIOUtils.closeUnchecked(reader);
    }

    /** Return the next face from the OBJ content or {@code null} if no face is found.
     * @return the next face from the OBJ content or {@code null} if no face is found
     * @throws IllegalStateException if a parsing error occurs
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    protected PolygonObjParser.Face readFace() {
        while (parser.nextKeyword()) {
            switch (parser.getCurrentKeyword()) {
            case ObjConstants.VERTEX_KEYWORD:
                handleVertex(parser.readVector());
                break;
            case ObjConstants.VERTEX_NORMAL_KEYWORD:
                handleNormal(parser.readVector());
                break;
            case ObjConstants.FACE_KEYWORD:
                return parser.readFace();
            default:
                break;
            }
        }

        return null;
    }

    /** Method called when a vertex is found in the OBJ content.
     * @param vertex vertex value
     */
    protected abstract void handleVertex(Vector3D vertex);

    /** Method called when a normal is found in the OBJ content.
     * @param normal normal value
     */
    protected abstract void handleNormal(Vector3D normal);
}
