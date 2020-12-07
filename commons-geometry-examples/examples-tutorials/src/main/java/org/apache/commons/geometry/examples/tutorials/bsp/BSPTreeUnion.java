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
package org.apache.commons.geometry.examples.tutorials.bsp;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.twod.Bounds2D;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.geometry.euclidean.twod.path.LinePath;

/** Class containing tutorial code for computing the union of two trees.
 */
public final class BSPTreeUnion {

    /** No instantiation. */
    private BSPTreeUnion() {}

    /** Tutorial code entry point.
     * @param args command arguments; if given, the first argument is used as the location of
     *      output folder
     */
    public static void main(final String[] args) {
        final File outputFolder = new File(args.length > 0 ? args[0] : ".");
        final BSPTreeSVGWriter svgWriter = new BSPTreeSVGWriter(Bounds2D.from(Vector2D.of(-8, -8), Vector2D.of(8, 8)));

        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-6);

        final RegionBSPTree2D a = LinePath.fromVertexLoop(Arrays.asList(
                    Vector2D.of(2, 0),
                    Vector2D.of(-4, 3),
                    Vector2D.of(-4, -3)
                ), precision).toTree();

        final RegionBSPTree2D b = LinePath.fromVertexLoop(Arrays.asList(
                Vector2D.of(-2, 0),
                Vector2D.of(4, -3),
                Vector2D.of(4, 3)
            ), precision).toTree();

        final RegionBSPTree2D result = RegionBSPTree2D.empty();

        result.union(a, b);

        svgWriter.write(result, new File(outputFolder, "union.svg"));
    }
}
