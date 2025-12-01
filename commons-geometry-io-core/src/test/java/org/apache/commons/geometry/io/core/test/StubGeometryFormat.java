/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.io.core.test;

import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.io.core.GeometryFormat;

/** Stub implementation of {@link GeometryFormat} for use in tests.
 */
public class StubGeometryFormat implements GeometryFormat {

    /** Format name. */
    private final String formatName;

    /** File extensions. */
    private final List<String> fileExtensions;

    /** Construct a new instance with the given format name. The file extensions
     * are taken to be a single element list containing the format name.
     * @param formatName format name
     */
    public StubGeometryFormat(final String formatName) {
        this(formatName, Collections.singletonList(formatName));
    }

    /** Construct a new instance with the given format name and file extensions.
     * @param formatName format name
     * @param fileExtensions file extensions
     */
    public StubGeometryFormat(final String formatName, final List<String> fileExtensions) {
        this.formatName = formatName;
        this.fileExtensions = fileExtensions;
    }

    /** {@inheritDoc} */
    @Override
    public String getFormatName() {
        return formatName;
    }

    /** {@inheritDoc} */
    @Override
    public String getDefaultFileExtension() {
        return fileExtensions.get(0);
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getFileExtensions() {
        return fileExtensions;
    }
}
