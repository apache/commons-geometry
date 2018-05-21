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
package org.apache.commons.geometry.core.partitioning;

import java.util.Iterator;

import org.apache.commons.geometry.euclidean.oned.IntervalsSet;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.Point2D;
import org.apache.commons.geometry.euclidean.twod.PolygonsSet;
import org.apache.commons.geometry.euclidean.twod.SubLine;
import org.junit.Assert;
import org.junit.Test;

/** Tests for partitioning characterization. This is designed to test code
 * in commons-geometry-core but is placed here to allow access to the euclidean
 * spatial primitives.
 */
public class CharacterizationTest {

    private static final double TEST_TOLERANCE = 1e-10;

    @Test
    public void testCharacterize_insideLeaf() {
        // arrange
        BSPTree<Point2D> tree = new BSPTree<>(Boolean.TRUE);
        SubLine sub = buildSubLine(new Point2D(0, -1), new Point2D(0, 1));

        // act
        Characterization<Point2D> ch = new Characterization<>(tree, sub);

        // assert
        Assert.assertEquals(true, ch.touchInside());
        Assert.assertSame(sub, ch.insideTouching());
        Assert.assertEquals(0, size(ch.getInsideSplitters()));

        Assert.assertEquals(false, ch.touchOutside());
        Assert.assertEquals(null,  ch.outsideTouching());
        Assert.assertEquals(0, size(ch.getOutsideSplitters()));
    }

    @Test
    public void testCharacterize_outsideLeaf() {
        // arrange
        BSPTree<Point2D> tree = new BSPTree<>(Boolean.FALSE);
        SubLine sub = buildSubLine(new Point2D(0, -1), new Point2D(0, 1));

        // act
        Characterization<Point2D> ch = new Characterization<>(tree, sub);

        // assert
        Assert.assertEquals(false, ch.touchInside());
        Assert.assertSame(null, ch.insideTouching());
        Assert.assertEquals(0, size(ch.getInsideSplitters()));

        Assert.assertEquals(true, ch.touchOutside());
        Assert.assertEquals(sub,  ch.outsideTouching());
        Assert.assertEquals(0, size(ch.getOutsideSplitters()));
    }

    @Test
    public void testCharacterize_onPlusSide() {
        // arrange
        BSPTree<Point2D> tree = new BSPTree<>(Boolean.TRUE);
        cut(tree, buildLine(new Point2D(0, 0), new Point2D(1, 0)));

        SubLine sub = buildSubLine(new Point2D(0, -1), new Point2D(0, -2));

        // act
        Characterization<Point2D> ch = new Characterization<>(tree, sub);

        // assert
        Assert.assertEquals(false, ch.touchInside());
        Assert.assertSame(null, ch.insideTouching());
        Assert.assertEquals(0, size(ch.getInsideSplitters()));

        Assert.assertEquals(true, ch.touchOutside());
        Assert.assertEquals(sub,  ch.outsideTouching());
        Assert.assertEquals(0, size(ch.getOutsideSplitters()));
    }

    @Test
    public void testCharacterize_onMinusSide() {
        // arrange
        BSPTree<Point2D> tree = new BSPTree<>(Boolean.TRUE);
        cut(tree, buildLine(new Point2D(0, 0), new Point2D(1, 0)));

        SubLine sub = buildSubLine(new Point2D(0, 1), new Point2D(0, 2));

        // act
        Characterization<Point2D> ch = new Characterization<>(tree, sub);

        // assert
        Assert.assertEquals(true, ch.touchInside());
        Assert.assertSame(sub, ch.insideTouching());
        Assert.assertEquals(0, size(ch.getInsideSplitters()));

        Assert.assertEquals(false, ch.touchOutside());
        Assert.assertEquals(null,  ch.outsideTouching());
        Assert.assertEquals(0, size(ch.getOutsideSplitters()));
    }

    @Test
    public void testCharacterize_onBothSides() {
        // arrange
        BSPTree<Point2D> tree = new BSPTree<>(Boolean.TRUE);
        cut(tree, buildLine(new Point2D(0, 0), new Point2D(1, 0)));

        SubLine sub = buildSubLine(new Point2D(0, -1), new Point2D(0, 1));

        // act
        Characterization<Point2D> ch = new Characterization<>(tree, sub);

        // assert
        Assert.assertEquals(true, ch.touchInside());
        Assert.assertNotSame(sub, ch.insideTouching());

        SubLine inside = (SubLine) ch.insideTouching();
        Assert.assertEquals(1, inside.getSegments().size());
        assertVectorEquals(new Point2D(0, 0), inside.getSegments().get(0).getStart());
        assertVectorEquals(new Point2D(0, 1), inside.getSegments().get(0).getEnd());

        Assert.assertEquals(1, size(ch.getInsideSplitters()));
        Iterator<BSPTree<Point2D>> insideSplitterIter = ch.getInsideSplitters().iterator();
        Assert.assertSame(tree, insideSplitterIter.next());

        Assert.assertEquals(true, ch.touchOutside());
        Assert.assertNotSame(sub, ch.insideTouching());

        SubLine outside = (SubLine) ch.outsideTouching();
        Assert.assertEquals(1, outside.getSegments().size());
        assertVectorEquals(new Point2D(0, -1), outside.getSegments().get(0).getStart());
        assertVectorEquals(new Point2D(0, 0), outside.getSegments().get(0).getEnd());

        Assert.assertEquals(1, size(ch.getOutsideSplitters()));
        Iterator<BSPTree<Point2D>> outsideSplitterIter = ch.getOutsideSplitters().iterator();
        Assert.assertSame(tree, outsideSplitterIter.next());
    }

    @Test
    public void testCharacterize_multipleSplits_reunitedOnPlusSide() {
        // arrange
        BSPTree<Point2D> tree = new BSPTree<>(Boolean.TRUE);
        cut(tree, buildLine(new Point2D(0, 0), new Point2D(1, 0)));
        cut(tree.getMinus(), buildLine(new Point2D(-1, 0), new Point2D(0, 1)));

        SubLine sub = buildSubLine(new Point2D(0, -2), new Point2D(0, 2));

        // act
        Characterization<Point2D> ch = new Characterization<>(tree, sub);

        // assert
        Assert.assertEquals(true, ch.touchInside());
        Assert.assertNotSame(sub, ch.insideTouching());

        SubLine inside = (SubLine) ch.insideTouching();
        Assert.assertEquals(1, inside.getSegments().size());
        assertVectorEquals(new Point2D(0, 1), inside.getSegments().get(0).getStart());
        assertVectorEquals(new Point2D(0, 2), inside.getSegments().get(0).getEnd());

        Assert.assertEquals(2, size(ch.getInsideSplitters()));
        Iterator<BSPTree<Point2D>> insideSplitterIter = ch.getInsideSplitters().iterator();
        Assert.assertSame(tree, insideSplitterIter.next());
        Assert.assertSame(tree.getMinus(), insideSplitterIter.next());

        Assert.assertEquals(true, ch.touchOutside());
        Assert.assertNotSame(sub, ch.insideTouching());

        SubLine outside = (SubLine) ch.outsideTouching();
        Assert.assertEquals(1, outside.getSegments().size());
        assertVectorEquals(new Point2D(0, -2), outside.getSegments().get(0).getStart());
        assertVectorEquals(new Point2D(0, 1), outside.getSegments().get(0).getEnd());

        Assert.assertEquals(2, size(ch.getOutsideSplitters()));
        Iterator<BSPTree<Point2D>> outsideSplitterIter = ch.getOutsideSplitters().iterator();
        Assert.assertSame(tree, outsideSplitterIter.next());
        Assert.assertSame(tree.getMinus(), outsideSplitterIter.next());
    }

    @Test
    public void testCharacterize_multipleSplits_reunitedOnMinusSide() {
        // arrange
        BSPTree<Point2D> tree = new BSPTree<>(Boolean.TRUE);
        cut(tree, buildLine(new Point2D(0, 0), new Point2D(1, 0)));
        cut(tree.getMinus(), buildLine(new Point2D(-1, 0), new Point2D(0, 1)));
        cut(tree.getMinus().getPlus(), buildLine(new Point2D(-0.5, 0.5), new Point2D(0, 0)));

        SubLine sub = buildSubLine(new Point2D(0, -2), new Point2D(0, 2));

        // act
        Characterization<Point2D> ch = new Characterization<>(tree, sub);

        // assert
        Assert.assertEquals(true, ch.touchInside());
        Assert.assertNotSame(sub, ch.insideTouching());

        SubLine inside = (SubLine) ch.insideTouching();
        Assert.assertEquals(1, inside.getSegments().size());
        assertVectorEquals(new Point2D(0, 0), inside.getSegments().get(0).getStart());
        assertVectorEquals(new Point2D(0, 2), inside.getSegments().get(0).getEnd());

        Assert.assertEquals(2, size(ch.getInsideSplitters()));
        Iterator<BSPTree<Point2D>> insideSplitterIter = ch.getInsideSplitters().iterator();
        Assert.assertSame(tree, insideSplitterIter.next());
        Assert.assertSame(tree.getMinus(), insideSplitterIter.next());

        Assert.assertEquals(true, ch.touchOutside());
        Assert.assertNotSame(sub, ch.insideTouching());

        SubLine outside = (SubLine) ch.outsideTouching();
        Assert.assertEquals(1, outside.getSegments().size());
        assertVectorEquals(new Point2D(0, -2), outside.getSegments().get(0).getStart());
        assertVectorEquals(new Point2D(0, 0), outside.getSegments().get(0).getEnd());

        Assert.assertEquals(1, size(ch.getOutsideSplitters()));
        Iterator<BSPTree<Point2D>> outsideSplitterIter = ch.getOutsideSplitters().iterator();
        Assert.assertSame(tree, outsideSplitterIter.next());
    }

    @Test
    public void testCharacterize_onHyperplane_sameOrientation() {
        // arrange
        BSPTree<Point2D> tree = new BSPTree<>(Boolean.TRUE);
        cut(tree, buildLine(new Point2D(0, 0), new Point2D(1, 0)));

        SubLine sub = buildSubLine(new Point2D(0, 0), new Point2D(1, 0));

        // act
        Characterization<Point2D> ch = new Characterization<>(tree, sub);

        // assert
        Assert.assertEquals(true, ch.touchInside());
        Assert.assertSame(sub, ch.insideTouching());
        Assert.assertEquals(0, size(ch.getInsideSplitters()));

        Assert.assertEquals(false, ch.touchOutside());
        Assert.assertEquals(null,  ch.outsideTouching());
        Assert.assertEquals(0, size(ch.getOutsideSplitters()));
    }

    @Test
    public void testCharacterize_onHyperplane_oppositeOrientation() {
        // arrange
        BSPTree<Point2D> tree = new BSPTree<>(Boolean.TRUE);
        cut(tree, buildLine(new Point2D(0, 0), new Point2D(1, 0)));

        SubLine sub = buildSubLine(new Point2D(1, 0), new Point2D(0, 0));

        // act
        Characterization<Point2D> ch = new Characterization<>(tree, sub);

        // assert
        Assert.assertEquals(true, ch.touchInside());
        Assert.assertSame(sub, ch.insideTouching());
        Assert.assertEquals(0, size(ch.getInsideSplitters()));

        Assert.assertEquals(false, ch.touchOutside());
        Assert.assertEquals(null,  ch.outsideTouching());
        Assert.assertEquals(0, size(ch.getOutsideSplitters()));
    }

    @Test
    public void testCharacterize_onHyperplane_multipleSplits_sameOrientation() {
        // arrange
        BSPTree<Point2D> tree = new BSPTree<>(Boolean.TRUE);
        cut(tree, buildLine(new Point2D(0, 0), new Point2D(1, 0)));
        cut(tree.getMinus(), buildLine(new Point2D(-1, 0), new Point2D(0, 1)));

        SubLine sub = buildSubLine(new Point2D(-2, 0), new Point2D(2, 0));

        // act
        Characterization<Point2D> ch = new Characterization<>(tree, sub);

        // assert
        Assert.assertEquals(true, ch.touchInside());
        Assert.assertNotSame(sub, ch.insideTouching());

        SubLine inside = (SubLine) ch.insideTouching();
        Assert.assertEquals(1, inside.getSegments().size());
        assertVectorEquals(new Point2D(-2, 0), inside.getSegments().get(0).getStart());
        assertVectorEquals(new Point2D(-1, 0), inside.getSegments().get(0).getEnd());

        Assert.assertEquals(1, size(ch.getInsideSplitters()));
        Iterator<BSPTree<Point2D>> insideSplitterIter = ch.getInsideSplitters().iterator();
        Assert.assertSame(tree.getMinus(), insideSplitterIter.next());

        Assert.assertEquals(true, ch.touchOutside());
        Assert.assertNotSame(sub, ch.insideTouching());

        SubLine outside = (SubLine) ch.outsideTouching();
        Assert.assertEquals(1, outside.getSegments().size());
        assertVectorEquals(new Point2D(-1, 0), outside.getSegments().get(0).getStart());
        assertVectorEquals(new Point2D(2, 0), outside.getSegments().get(0).getEnd());

        Assert.assertEquals(1, size(ch.getOutsideSplitters()));
        Iterator<BSPTree<Point2D>> outsideSplitterIter = ch.getOutsideSplitters().iterator();
        Assert.assertSame(tree.getMinus(), outsideSplitterIter.next());
    }

    @Test
    public void testCharacterize_onHyperplane_multipleSplits_oppositeOrientation() {
        // arrange
        BSPTree<Point2D> tree = new BSPTree<>(Boolean.TRUE);
        cut(tree, buildLine(new Point2D(0, 0), new Point2D(1, 0)));
        cut(tree.getMinus(), buildLine(new Point2D(-1, 0), new Point2D(0, 1)));

        SubLine sub = buildSubLine(new Point2D(2, 0), new Point2D(-2, 0));

        // act
        Characterization<Point2D> ch = new Characterization<>(tree, sub);

        // assert
        Assert.assertEquals(true, ch.touchInside());
        Assert.assertNotSame(sub, ch.insideTouching());

        SubLine inside = (SubLine) ch.insideTouching();
        Assert.assertEquals(1, inside.getSegments().size());
        assertVectorEquals(new Point2D(-1, 0), inside.getSegments().get(0).getStart());
        assertVectorEquals(new Point2D(-2, 0), inside.getSegments().get(0).getEnd());

        Assert.assertEquals(1, size(ch.getInsideSplitters()));
        Iterator<BSPTree<Point2D>> insideSplitterIter = ch.getInsideSplitters().iterator();
        Assert.assertSame(tree.getMinus(), insideSplitterIter.next());

        Assert.assertEquals(true, ch.touchOutside());
        Assert.assertNotSame(sub, ch.insideTouching());

        SubLine outside = (SubLine) ch.outsideTouching();
        Assert.assertEquals(1, outside.getSegments().size());
        assertVectorEquals(new Point2D(2, 0), outside.getSegments().get(0).getStart());
        assertVectorEquals(new Point2D(-1, 0), outside.getSegments().get(0).getEnd());

        Assert.assertEquals(1, size(ch.getOutsideSplitters()));
        Iterator<BSPTree<Point2D>> outsideSplitterIter = ch.getOutsideSplitters().iterator();
        Assert.assertSame(tree.getMinus(), outsideSplitterIter.next());
    }

    @Test
    public void testCharacterize_onHyperplane_box() {
        // arrange
        PolygonsSet poly = new PolygonsSet(0, 1, 0, 1, TEST_TOLERANCE);
        BSPTree<Point2D> tree = poly.getTree(false);

        SubLine sub = buildSubLine(new Point2D(2, 0), new Point2D(-2, 0));

        // act
        Characterization<Point2D> ch = new Characterization<>(tree, sub);

        // assert
        Assert.assertEquals(true, ch.touchInside());
        Assert.assertNotSame(sub, ch.insideTouching());

        SubLine inside = (SubLine) ch.insideTouching();
        Assert.assertEquals(1, inside.getSegments().size());
        assertVectorEquals(new Point2D(1, 0), inside.getSegments().get(0).getStart());
        assertVectorEquals(new Point2D(0, 0), inside.getSegments().get(0).getEnd());

        Assert.assertEquals(2, size(ch.getInsideSplitters()));

        Assert.assertEquals(true, ch.touchOutside());
        Assert.assertNotSame(sub, ch.insideTouching());

        SubLine outside = (SubLine) ch.outsideTouching();
        Assert.assertEquals(2, outside.getSegments().size());
        assertVectorEquals(new Point2D(2, 0), outside.getSegments().get(0).getStart());
        assertVectorEquals(new Point2D(1, 0), outside.getSegments().get(0).getEnd());
        assertVectorEquals(new Point2D(0, 0), outside.getSegments().get(1).getStart());
        assertVectorEquals(new Point2D(-2, 0), outside.getSegments().get(1).getEnd());

        Assert.assertEquals(2, size(ch.getOutsideSplitters()));
    }

    private void cut(BSPTree<Point2D> tree, Line line) {
        if (tree.insertCut(line)) {
            tree.setAttribute(null);
            tree.getPlus().setAttribute(Boolean.FALSE);
            tree.getMinus().setAttribute(Boolean.TRUE);
        }
    }

    private int size(NodesSet<Point2D> nodes) {
        Iterator<BSPTree<Point2D>> it = nodes.iterator();

        int size = 0;
        while (it.hasNext()) {
            it.next();
            ++size;
        }

        return size;
    }

    private Line buildLine(Point2D p1, Point2D p2) {
        return new Line(p1, p2, TEST_TOLERANCE);
    }

    private SubLine buildSubLine(Point2D start, Point2D end) {
        Line line = new Line(start, end, TEST_TOLERANCE);
        double lower = (line.toSubSpace(start)).getX();
        double upper = (line.toSubSpace(end)).getX();
        return new SubLine(line, new IntervalsSet(lower, upper, TEST_TOLERANCE));
    }

    private void assertVectorEquals(Point2D expected, Point2D actual) {
        String msg = "Expected vector to equal " + expected + " but was " + actual + ";";
        Assert.assertEquals(msg, expected.getX(), actual.getX(), TEST_TOLERANCE);
        Assert.assertEquals(msg, expected.getY(), actual.getY(), TEST_TOLERANCE);
    }
}
