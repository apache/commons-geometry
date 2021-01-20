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
package org.apache.commons.geometry.io.euclidean.threed;

import org.apache.commons.geometry.io.euclidean.threed.obj.OBJBoundaryReadHandler3D;
import org.apache.commons.geometry.io.euclidean.threed.obj.OBJBoundaryWriteHandler3D;
import org.apache.commons.geometry.io.euclidean.threed.txt.TextBoundaryReadHandler3D;
import org.apache.commons.geometry.io.euclidean.threed.txt.TextBoundaryWriteHandler3D;

/** {@link BoundaryIOManager3D} subclass that automatically registers handlers
 * for internally supported format types. The following formats are supported:
 * <ul>
 *  <li>{@link IO3D#OBJ OBJ}</li>
 *  <li>{@link IO3D#TXT TXT}</li>
 *  <li>{@link IO3D#CSV CSV}</li>
 * </ul>
 * The UTF-8 charset is used to read and write the OBJ, TXT, and CSV data formats.
 */
public class DefaultBoundaryIOManager3D extends BoundaryIOManager3D {

    /** Construct a new instance with default registered {@link BoundaryReadHandler3D read}
     * and {@link BoundaryWriteHandler3D write} handlers.
     */
    public DefaultBoundaryIOManager3D() {
        registerReadHandler(IO3D.TXT, new TextBoundaryReadHandler3D());
        registerWriteHandler(IO3D.TXT, new TextBoundaryWriteHandler3D());

        registerReadHandler(IO3D.CSV, new TextBoundaryReadHandler3D());
        registerWriteHandler(IO3D.CSV, TextBoundaryWriteHandler3D.csvFormat());

        registerReadHandler(IO3D.OBJ, new OBJBoundaryReadHandler3D());
        registerWriteHandler(IO3D.OBJ, new OBJBoundaryWriteHandler3D());
    }
}
