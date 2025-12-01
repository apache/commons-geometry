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
package org.apache.commons.geometry.examples.tutorials.bsp;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.geometry.euclidean.twod.Bounds2D;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.Lines;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D.RegionNode2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.core.Precision;

/** Class containing tutorial code for constructing a 2D BSP tree using
 * a bottom-up approach.
 */
public final class BottomUpBSPTreeConstruction {

    /** String defining the name format of output svg files. */
    private static final String OUTPUT_FILE_FORMAT = "bu-cut-%d.svg";

    /** No instantiation. */
    private BottomUpBSPTreeConstruction() {}

    /** Tutorial code entry point.
     * @param args command arguments; if given, the first argument is used as the location of
     *      output folder
     */
    public static void main(final String[] args) {
        final File outputFolder = new File(args.length > 0 ? args[0] : ".");
        final BSPTreeSVGWriter svgWriter = new BSPTreeSVGWriter(Bounds2D.from(Vector2D.of(-8, -8), Vector2D.of(8, 8)));

        final Map<RegionNode2D, String> nodeNames = new HashMap<>();
        int cutCount = -1;

        // create a precision context for floating point comparisons
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-6);

        // construct an empty tree
        final RegionBSPTree2D tree = RegionBSPTree2D.empty();

        final Line rootCut = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, precision);
        final RegionNode2D a = tree.getRoot();

        nodeNames.put(a, "a");
        svgWriter.write(tree, nodeNames, new File(outputFolder, String.format(OUTPUT_FILE_FORMAT, ++cutCount)));

        // add a single cut
        a.cut(rootCut);

        final RegionNode2D b = a.getMinus();
        final RegionNode2D c = a.getPlus();

        nodeNames.put(b, "b");
        nodeNames.put(c, "c");
        svgWriter.write(tree, nodeNames, new File(outputFolder, String.format(OUTPUT_FILE_FORMAT, ++cutCount)));

        // add another cut
        b.insertCut(Lines.fromPoints(Vector2D.of(1, 0), Vector2D.of(-1, 1), precision));

        final RegionNode2D d = b.getMinus();
        final RegionNode2D e = b.getPlus();

        nodeNames.put(d, "d");
        nodeNames.put(e, "e");
        svgWriter.write(tree, nodeNames, new File(outputFolder, String.format(OUTPUT_FILE_FORMAT, ++cutCount)));

        d.insertCut(Lines.fromPointAndDirection(Vector2D.of(-5, 1), Vector2D.Unit.MINUS_Y, precision));

        final RegionNode2D f = d.getMinus();
        final RegionNode2D g = d.getPlus();

        nodeNames.put(f, "f");
        nodeNames.put(g, "g");
        svgWriter.write(tree, nodeNames, new File(outputFolder, String.format(OUTPUT_FILE_FORMAT, ++cutCount)));

        // other side
        c.insertCut(Lines.fromPoints(Vector2D.of(-1, 0), Vector2D.of(1, -1), precision));

        final RegionNode2D h = c.getMinus();
        final RegionNode2D i = c.getPlus();

        nodeNames.put(h, "h");
        nodeNames.put(i, "i");
        svgWriter.write(tree, nodeNames, new File(outputFolder, String.format(OUTPUT_FILE_FORMAT, ++cutCount)));

        h.insertCut(Lines.fromPointAndDirection(Vector2D.of(5, -1), Vector2D.Unit.PLUS_Y, precision));

        final RegionNode2D j = h.getMinus();
        final RegionNode2D k = h.getPlus();

        nodeNames.put(j, "j");
        nodeNames.put(k, "k");
        svgWriter.write(tree, nodeNames, new File(outputFolder, String.format(OUTPUT_FILE_FORMAT, ++cutCount)));
    }
}
