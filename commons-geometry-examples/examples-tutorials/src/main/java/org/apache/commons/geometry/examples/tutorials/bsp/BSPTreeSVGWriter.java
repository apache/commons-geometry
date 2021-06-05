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
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.bsp.BSPTreeVisitor;
import org.apache.commons.geometry.core.partitioning.bsp.RegionCutBoundary;
import org.apache.commons.geometry.euclidean.twod.AffineTransformMatrix2D;
import org.apache.commons.geometry.euclidean.twod.Bounds2D;
import org.apache.commons.geometry.euclidean.twod.LineConvexSubset;
import org.apache.commons.geometry.euclidean.twod.PolarCoordinates;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D.RegionNode2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.geometry.euclidean.twod.path.LinePath;
import org.apache.commons.geometry.euclidean.twod.shape.Parallelogram;
import org.apache.commons.numbers.core.Precision;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Class for writing SVG visualizations of 2D BSP trees.
 */
public class BSPTreeSVGWriter {

    /** SVG XML namespace. */
    private static final String SVG_NAMESPACE = "http://www.w3.org/2000/svg";

    /** SVG version. */
    private static final String SVG_VERSION = "1.1";

    /** Property key used to set the indent level of the output xml. */
    private static final String INDENT_AMOUNT_KEY = "{http://xml.apache.org/xslt}indent-amount";

    /** Indent amount for the output xml. */
    private static final int INDENT_AMOUNT = 4;

    /** String containing default node name characters. */
    private static final String DEFAULT_NODE_NAMES = "abcdefghijklmnopqrstuvwxyz";

    /** Id used for the geometry area clip path. */
    private static final String GEOMETRY_AREA_CLIP_PATH_ID = "geometry-area";

    /** Path string command to move to a point. */
    private static final char PATH_MOVE_TO = 'M';

    /** Path string command to draw a line to a point. */
    private static final char PATH_LINE_TO = 'L';

    /** Space character. */
    private static final char SPACE = ' ';

    /** Name of the SVG "rect" element. */
    private static final String RECT_ELEMENT = "rect";

    /** Name of the SVG "path" element. */
    private static final String PATH_ELEMENT = "path";

    /** Name of the "class" attribute. */
    private static final String CLASS_ATTR = "class";

    /** Name of the "width" attribute. */
    private static final String WIDTH_ATTR = "width";

    /** Name of the "height" attribute. */
    private static final String HEIGHT_ATTR = "height";

    /** Name of the "x" attribute. */
    private static final String X_ATTR = "x";

    /** Name of the "y" attribute. */
    private static final String Y_ATTR = "y";

    /** CSS style string for the generated SVG. */
    private static final String STYLE =
        "text { font-size: 14px; } " +
        ".node-name { text-anchor: middle; font-family: \"Courier New\", Courier, monospace; } " +
        ".geometry-border { fill: none; stroke: gray; stroke-width: 1; } " +
        ".arrow { fill: none; stroke: blue; stroke-width: 1; } " +
        ".cut { fill: none; stroke: blue; stroke-width: 1; stroke-dasharray: 5,3; } " +
        ".region-boundary { stroke: orange; stroke-width: 2; } " +
        ".inside { fill: #aaa; opacity: 0.2; } " +
        ".tree-path { fill: none; stroke: gray; stroke-width: 1; } " +
        ".inside-node { font-weight: bold; }";

    /** Geometry bounds; only geometry within these bounds is rendered. */
    private final Bounds2D bounds;

    /** The width of the SVG. */
    private final int width = 750;

    /** The height of the SVG. */
    private final int height = 375;

    /** The margin used in the SVG. */
    private final int margin = 5;

    /** Amount of the overall width of the SVG to use for the geometry area. */
    private final double geometryAreaWidthFactor = 0.5;

    /** Amount of the overall width of the SVG to use for the tree structure area. */
    private final double treeAreaWidthFactor = 1.0 - geometryAreaWidthFactor;

    /** Angle that arrow heads on lines make with the direction of the line. */
    private final double arrowAngle = 0.8 * Math.PI;

    /** Length of arrow head lines. */
    private final double arrowLength = 8;

    /** Distance between levels of the tree in the tree structure display. */
    private final double treeVerticalSpacing = 45;

    /** Line end margin used in the lines between nodes in the tree structure display. */
    private final double treeLineMargin = 10;

    /** Factor determining how much of the available horizontal width for a node should be used to
     * offset it from its parent.
     */
    private double treeParentOffsetFactor = 0.25;

    /** Minimum horizontal offset for tree nodes from their parents. */
    private double treeParentXOffsetMin = 0;

    /** Precision context used for floating point comparisons. */
    private final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-6);

    /** Construct a new instance that will render regions within the given bounds.
     * @param bounds bounds used to determine what output
     */
    public BSPTreeSVGWriter(final Bounds2D bounds) {
        this.bounds = bounds;
    }

    /** Set the offset factor determining how much of the available horizontal width for
     * a node should be used to offset it from its parent.
     * @param treeParentOffsetFactor offset factor
     */
    public void setTreeParentOffsetFactor(final double treeParentOffsetFactor) {
        this.treeParentOffsetFactor = treeParentOffsetFactor;
    }

    /** Set the minimum horizontal offset for tree nodes from their parents.
     * @param treeParentXOffsetMin minimum offset
     */
    public void setTreeParentXOffsetMin(final double treeParentXOffsetMin) {
        this.treeParentXOffsetMin = treeParentXOffsetMin;
    }

    /** Write an SVG visualization of the given BSP tree. Default names are assigned to the tree nodes.
     * @param tree tree to output
     * @param file path of the svg file to write
     */
    public void write(final RegionBSPTree2D tree, final File file) {
        final Deque<RegionNode2D> nodeQueue = new LinkedList<>();
        nodeQueue.add(tree.getRoot());

        final Map<RegionNode2D, String> nodeNames = new HashMap<>();

        final String names = DEFAULT_NODE_NAMES;
        RegionNode2D node;
        for (int i = 0; i < names.length() && !nodeQueue.isEmpty(); ++i) {
            node = nodeQueue.removeFirst();

            nodeNames.put(node, names.substring(i, i + 1));

            if (node.isInternal()) {
                nodeQueue.add(node.getMinus());
                nodeQueue.add(node.getPlus());
            }
        }

        write(tree, nodeNames, file);
    }

    /** Write an SVG visualization of the given BSP tree.
     * @param tree tree to output
     * @param nodeNames map of node instances to the names that should be used for them in the svg
     * @param file path of the svg file to write
     */
    public void write(final RegionBSPTree2D tree, final Map<RegionNode2D, String> nodeNames, final File file) {
        try {
            final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

            final Document doc = docBuilder.newDocument();

            // create the svg element
            final Element root = svgElement("svg", doc);
            doc.appendChild(root);

            root.setAttribute("version", SVG_VERSION);
            root.setAttribute(WIDTH_ATTR, String.valueOf(width));
            root.setAttribute(HEIGHT_ATTR, String.valueOf(height));

            // add a defs element for later use
            final Element defs = svgElement("defs", doc);
            root.appendChild(defs);

            // add a style element
            final Element style = svgElement("style", doc);
            root.appendChild(style);
            style.setTextContent(STYLE);

            // write the tree
            writeTreeGeometryArea(tree, nodeNames, root, defs, doc);
            writeTreeStructureArea(tree, nodeNames, root, doc);

            // output to the target file
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(INDENT_AMOUNT_KEY, String.valueOf(INDENT_AMOUNT));

            final DOMSource source = new DOMSource(doc);
            final StreamResult target = new StreamResult(file);

            transformer.transform(source, target);

        } catch (final ParserConfigurationException | TransformerException e) {
            // throw as a runtime exception for convenience
            throw new RuntimeException("Failed to create SVG", e);
        }
    }

    /** Write the svg area containing the visual representation of the tree geometry.
     * @param tree tree to write
     * @param nodeNames map of nodes to the names that should be used to identify them
     * @param root root svg element
     * @param defs svg defs element
     * @param doc xml document
     */
    private void writeTreeGeometryArea(final RegionBSPTree2D tree, final Map<RegionNode2D, String> nodeNames,
            final Element root, final Element defs, final Document doc) {
        final double geometrySvgX = margin;
        final double geometrySvgY = margin;
        final double geometrySvgWidth = (geometryAreaWidthFactor * width) - (2 * margin);
        final double geometrySvgHeight = height - (2 * margin);

        defineClipRect(GEOMETRY_AREA_CLIP_PATH_ID,
                geometrySvgX, geometrySvgY,
                geometrySvgWidth, geometrySvgHeight,
                defs, doc);

        // create the box containing the 2D content
        final Element geometryGroup = svgElement("g", doc);
        root.appendChild(geometryGroup);
        geometryGroup.setAttribute(CLASS_ATTR, "geometry");
        geometryGroup.setAttribute("clip-path", "url(#" + GEOMETRY_AREA_CLIP_PATH_ID + ")");

        // set up the transform so we can write geometry elements with their natural coordinates
        final AffineTransformMatrix2D transform = computeGeometryTransform(bounds, geometrySvgX, geometrySvgY,
                geometrySvgWidth, geometrySvgHeight);

        // add the tree geometry
        tree.accept(new TreeGeometryVisitor(transform, nodeNames, geometryGroup, doc));

        // create a box outlining the geometry area
        final Element border = svgElement(RECT_ELEMENT, doc);
        border.setAttribute(CLASS_ATTR, "geometry-border");
        floatAttr(border, X_ATTR, geometrySvgX);
        floatAttr(border, Y_ATTR, geometrySvgY);
        floatAttr(border, WIDTH_ATTR, geometrySvgWidth);
        floatAttr(border, HEIGHT_ATTR, geometrySvgHeight);

        root.appendChild(border);

    }

    /** Write the svg area containing the visual representation of the tree structure.
     * @param tree tree to write
     * @param nodeNames map of nodes to the names that should be used to identify them
     * @param root svg root element
     * @param doc xml document
     */
    private void writeTreeStructureArea(final RegionBSPTree2D tree, final Map<RegionNode2D, String> nodeNames,
            final Element root, final Document doc) {
        final Element treeGroup = svgElement("g", doc);
        root.appendChild(treeGroup);
        treeGroup.setAttribute(CLASS_ATTR, "tree");

        final double offsetX = ((1 - treeAreaWidthFactor) * width) + margin;
        final double offsetY = margin;

        final double svgWidth = (treeAreaWidthFactor * width) - (2 * margin);
        final double svgHeight = height - (2 * margin);

        treeGroup.setAttribute("transform", "translate(" + offsetX + " " + offsetY + ")");

        tree.accept(new TreeStructureVisitor(tree.height(), svgWidth, svgHeight, nodeNames, treeGroup, doc));
    }

    /** Compute the transform required to convert from the geometry coordinate system given in {@code bounds}
     * to the one defined by the given svg coordinates. The y-axis from the geometry is oriented to point
     * upwards in the svg.
     * @param bounds the bounds of the geometry
     * @param svgX x coordinate in the upper-left corner of the svg target box
     * @param svgY y coordinate in the upper-left corner of the svg target box
     * @param svgWidth width of the svg target box
     * @param svgHeight height of the svg target box
     * @return a transform converting from geometry space to svg space
     */
    private static AffineTransformMatrix2D computeGeometryTransform(final Bounds2D bounds,
            final double svgX, final double svgY, final double svgWidth, final double svgHeight) {

        final Vector2D boundsDiagonal = bounds.getDiagonal();

        return AffineTransformMatrix2D
            .createTranslation(bounds.getMin().negate())
            .scale(svgWidth / boundsDiagonal.getX(), -svgHeight / boundsDiagonal.getY())
            .translate(svgX, svgY + svgHeight);
    }

    /** Define an SVG clipping rectangle.
     * @param id id of the clipping rectangle to add
     * @param x x coordinate of the clipping rectangle
     * @param y y coordinate of the clipping rectangle
     * @param svgWidth width of the clipping rectangle
     * @param svgHeight height of the clipping rectangle
     * @param defs svg "defs" element
     * @param doc xml document
     */
    private void defineClipRect(final String id, final double x, final double y,
            final double svgWidth, final double svgHeight, final Element defs, final Document doc) {

        final Element clipPath = svgElement("clipPath", doc);
        clipPath.setAttribute("id", id);

        defs.appendChild(clipPath);

        final Element rect = svgElement(RECT_ELEMENT, doc);
        floatAttr(rect, X_ATTR, x);
        floatAttr(rect, Y_ATTR, y);
        floatAttr(rect, WIDTH_ATTR, svgWidth);
        floatAttr(rect, HEIGHT_ATTR, svgHeight);

        clipPath.appendChild(rect);
    }

    /** Convenience method for setting a floating-point attribute on an element.
     * @param element element to set the attribute on
     * @param name name of the attribute to set
     * @param value value of the attribute to set
     */
    private static void floatAttr(final Element element, final String name, final double value) {
        element.setAttribute(name, String.valueOf(value));
    }

    /** Convenience method for creating an element from the svg namespace.
     * @param name the name of the element
     * @param doc document to create the element in
     * @return the element from the svg namespace
     */
    private static Element svgElement(final String name, final Document doc) {
        return doc.createElementNS(SVG_NAMESPACE, name);
    }

    /** Base class for BSP tree visitors that output SVG content.
     */
    private abstract class AbstractSVGTreeVisitor implements BSPTreeVisitor<Vector2D, RegionNode2D> {

        /** Map of nodes to the names that should be used to identify them. */
        private final Map<RegionNode2D, String> nodeNames;

        /** Parent SVG element to append new elements to. */
        private final Element parent;

        /** Xml document. */
        private final Document doc;

        /** Number of nodes visited so far. */
        private int count = 0;

        /** Construct a new instance.
         * @param nodeNames map of nodes ot the names that should be used to identify them
         * @param parent parent SVG element
         * @param doc xml document
         */
        AbstractSVGTreeVisitor(final Map<RegionNode2D, String> nodeNames, final Element parent, final Document doc) {
            this.nodeNames = nodeNames;
            this.parent = parent;
            this.doc = doc;
        }

        /** {@inheritDoc} */
        @Override
        public Order visitOrder(final RegionNode2D internalNode) {
            return Order.NODE_MINUS_PLUS;
        }

        /** {@inheritDoc} */
        @Override
        public Result visit(final RegionNode2D node) {
            ++count;

            final String name = (nodeNames != null && nodeNames.containsKey(node)) ?
                    nodeNames.get(node) :
                    String.valueOf(count);

            visitNode(name, node);

            return Result.CONTINUE;
        }

        /** Create a new svg element with the given name and append it to the parent node.
         * @param name name of the element
         * @return the created element, already appended to the parent
         */
        protected Element createChild(final String name) {
            final Element child = createElement(name);
            parent.appendChild(child);

            return child;
        }

        /** Create an svg element with the given name. The element is <em>not</em> appended to the
         * parent node.
         * @param name name of the element
         * @return the created element
         */
        protected Element createElement(final String name) {
            return svgElement(name, doc);
        }

        /** Create an SVG text element containing the name of a tree node. The returned element is
         * <em>not</em> appended to the parent.
         * @param name name of the tree node
         * @param svgPt location to place the text
         * @return text element containing the given node name
         */
        protected Element createNodeNameElement(final String name, final Vector2D svgPt) {
            final Element text = createElement("text");
            text.setAttribute(CLASS_ATTR, "node-name");
            text.setAttribute("dominant-baseline", "middle");
            floatAttr(text, X_ATTR, svgPt.getX());
            floatAttr(text, Y_ATTR, svgPt.getY());
            text.setTextContent(name);

            return text;
        }

        /** Create a path element representing a line from {@code svgStart} to {@code svgEnd}.
         * @param className class name to place on the element
         * @param svgStart start point
         * @param svgEnd end point
         * @return path element
         */
        protected Element createPathElement(final String className, final Vector2D svgStart, final Vector2D svgEnd) {
            final Element path = createElement(PATH_ELEMENT);
            path.setAttribute(CLASS_ATTR, className);

            final StringBuilder pathStr = new StringBuilder();
            pathStr.append(PATH_MOVE_TO)
                .append(pointString(svgStart))
                .append(SPACE)
                .append(PATH_LINE_TO)
                .append(pointString(svgEnd));

            path.setAttribute("d", pathStr.toString());

            return path;
        }

        /** Create a string containing the coordinates of the given point in the format used by the SVG
         * {@code path} element.
         * @param pt point to represent as an SVG string
         * @return SVG string representation of the point
         */
        protected String pointString(final Vector2D pt) {
            return pt.getX() + " " + pt.getY();
        }

        /** Visit a node in the tree.
         * @param name the name for the node in the visualization
         * @param node the node being visited
         */
        protected abstract void visitNode(String name, RegionNode2D node);
    }

    /** BSP tree visitor that outputs SVG representing the tree geometry.
     */
    private final class TreeGeometryVisitor extends AbstractSVGTreeVisitor {

        /** The geometry bounds as a region instance. */
        private final Parallelogram boundsRegion;

        /** Transform converting from geometry space to SVG space. */
        private final AffineTransformMatrix2D transform;

        /** Group element containing the tree geometry paths. */
        private final Element pathGroup;

        /** Group element containing the tree node labels. */
        private final Element labelGroup;

        /** Construct a new instance for generating SVG geometry content.
         * @param transform transform converting from geometry space to SVG space
         * @param nodeNames map of nodes to the names that should be used to identify them
         * @param parent parent SVG element
         * @param doc xml document
         */
        TreeGeometryVisitor(final AffineTransformMatrix2D transform, final Map<RegionNode2D, String> nodeNames,
                final Element parent, final Document doc) {
            super(nodeNames, parent, doc);

            this.boundsRegion = bounds.toRegion(precision);

            this.transform = transform;

            // place the label group after the geometry group so the labels appear on top
            this.pathGroup = svgElement("g", doc);
            pathGroup.setAttribute(CLASS_ATTR, "paths");
            parent.appendChild(pathGroup);

            this.labelGroup = svgElement("g", doc);
            labelGroup.setAttribute(CLASS_ATTR, "labels");
            parent.appendChild(labelGroup);
        }

        /** {@inheritDoc} */
        @Override
        protected void visitNode(final String name, final RegionNode2D node) {
            if (node.isLeaf()) {
                visitLeafNode(name, node);
            } else {
                visitInternalNode(name, node);
            }
        }

        /** Visit a leaf node.
         * @param name name of the node
         * @param node the leaf node
         */
        private void visitLeafNode(final String name, final RegionNode2D node) {
            final RegionBSPTree2D tree = node.getNodeRegion().toTree();
            tree.intersection(boundsRegion.toTree());

            final Vector2D svgCentroid = toSvgSpace(tree.getCentroid());

            labelGroup.appendChild(createNodeNameElement(name, svgCentroid));

            if (node.isInside()) {
                for (final LinePath linePath : tree.getBoundaryPaths()) {
                    final Element path = createElement(PATH_ELEMENT);
                    pathGroup.appendChild(path);
                    path.setAttribute(CLASS_ATTR, "inside");

                    final StringBuilder sb = new StringBuilder();

                    for (final Vector2D pt : linePath.getVertexSequence()) {
                        if (sb.length() < 1) {
                            sb.append(PATH_MOVE_TO);
                        } else {
                            sb.append(SPACE)
                                .append(PATH_LINE_TO);
                        }

                        sb.append(pointString(toSvgSpace(pt)));
                    }

                    path.setAttribute("d", sb.toString());
                }
            }
        }

        /** Visit an internal node.
         * @param name name of the node
         * @param node the internal node
         */
        private void visitInternalNode(final String name, final RegionNode2D node) {
            final LineConvexSubset trimmedCut = boundsRegion.trim(node.getCut());

            final Vector2D svgStart = toSvgSpace(trimmedCut.getStartPoint());
            final Vector2D svgEnd = toSvgSpace(trimmedCut.getEndPoint());

            final Vector2D svgMid = svgStart.lerp(svgEnd, 0.5);

            labelGroup.appendChild(createNodeNameElement(name, svgMid));

            pathGroup.appendChild(createPathElement("cut", svgStart, svgEnd));

            final String arrowPathString = createCutArrowPathString(svgStart, svgEnd);
            if (arrowPathString != null) {
                final Element arrowPath = createElement(PATH_ELEMENT);
                pathGroup.appendChild(arrowPath);
                arrowPath.setAttribute(CLASS_ATTR, "arrow");
                arrowPath.setAttribute("d", arrowPathString);
            }

            final RegionCutBoundary<Vector2D> boundary = node.getCutBoundary();
            if (boundary != null) {
                addRegionBoundaries(boundary.getInsideFacing());
                addRegionBoundaries(boundary.getOutsideFacing());
            }
        }

        /** Add path elements for the given list of region boundaries.
         * @param boundaries boundaries to add path elements for
         */
        private void addRegionBoundaries(final List<HyperplaneConvexSubset<Vector2D>> boundaries) {
            LineConvexSubset trimmed;
            for (final HyperplaneConvexSubset<Vector2D> boundary : boundaries) {
                trimmed = boundsRegion.trim(boundary);

                if (trimmed != null) {
                    pathGroup.appendChild(createPathElement(
                            "region-boundary",
                            toSvgSpace(trimmed.getStartPoint()),
                            toSvgSpace(trimmed.getEndPoint())));
                }
            }
        }

        /** Create an SVG path string defining an arrow head for the line segment that extends from
         * {@code svgStart} to {@code svgEnd}.
         * @param svgStart line segment start point
         * @param svgEnd line segment end point
         * @return an SVG path string defining an arrow head for the line segment
         */
        private String createCutArrowPathString(final Vector2D svgStart, final Vector2D svgEnd) {
            final Vector2D dir = svgStart.vectorTo(svgEnd);
            if (!dir.eq(Vector2D.ZERO, precision)) {

                final double az = Math.atan2(dir.getY(), dir.getX());
                final Vector2D upperArrowPt = PolarCoordinates
                        .toCartesian(arrowLength, az + arrowAngle)
                        .add(svgEnd);

                final Vector2D lowerArrowPt = PolarCoordinates
                        .toCartesian(arrowLength, az - arrowAngle)
                        .add(svgEnd);

                final StringBuilder sb = new StringBuilder();
                sb.append(PATH_MOVE_TO)
                    .append(pointString(upperArrowPt))
                    .append(SPACE)
                    .append(PATH_LINE_TO)
                    .append(pointString(svgEnd))
                    .append(SPACE)
                    .append(PATH_LINE_TO)
                    .append(pointString(lowerArrowPt));

                return sb.toString();
            }

            return null;
        }

        /** Convert the given point in geometry space to SVG space.
         * @param pt point in geometry space to convert
         * @return point in SVG space
         */
        private Vector2D toSvgSpace(final Vector2D pt) {
            return transform.apply(pt);
        }
    }

    /** BSP tree visitor that outputs SVG representing the tree structure.
     */
    private final class TreeStructureVisitor extends AbstractSVGTreeVisitor {

        /** Top of the content area. */
        private final double svgTop;

        /** Width of the content area. */
        private final double svgWidth;

        /** Map of nodes to their rendered locations in the content area. */
        private final Map<RegionNode2D, Vector2D> nodeLocations = new HashMap<>();

        /** Construct a new instance for rendering a representation of the structure of a BSP tree.
         * @param treeNodeHeight the height of the BSP tree
         * @param svgWidth available SVG width for the content
         * @param svgHeight available SVG height for the content
         * @param nodeNames map of nodes to the names that should be used to identify them
         * @param parent parent SVG element
         * @param doc xml document
         */
        TreeStructureVisitor(final int treeNodeHeight, final double svgWidth, final double svgHeight,
                final Map<RegionNode2D, String> nodeNames, final Element parent, final Document doc) {
            super(nodeNames, parent, doc);

            final double requiredSvgHeight = treeNodeHeight * treeVerticalSpacing;
            final double svgMid = 0.5 * svgHeight;

            this.svgTop = svgMid - (0.5 * requiredSvgHeight);
            this.svgWidth = svgWidth;
        }

        /** {@inheritDoc} */
        @Override
        protected void visitNode(final String name, final RegionNode2D node) {
            final Vector2D loc = getNodeLocation(node);
            nodeLocations.put(node, loc);

            final Element nodeGroup = createChild("g");
            nodeGroup.setAttribute(CLASS_ATTR, getNodeClassNames(name, node));

            nodeGroup.appendChild(createNodeNameElement(name, loc));

            final Vector2D parentLoc = nodeLocations.get(node.getParent());
            if (parentLoc != null) {
                final Vector2D offset = loc.vectorTo(parentLoc).withNorm(treeLineMargin);
                nodeGroup.appendChild(createPathElement("tree-path", loc.add(offset), parentLoc.subtract(offset)));
            }
        }

        /** Get a string containing the class names that should be used for the given node.
         * @param name name of the node
         * @param node the node
         * @return a string containing the class names that should be used for the given node
         */
        private String getNodeClassNames(final String name, final RegionNode2D node) {
            final StringBuilder sb = new StringBuilder();
            sb.append("node-").append(name);

            if (node.isLeaf()) {
                sb.append(SPACE)
                    .append(node.isInside() ? "inside-node" : "outside-node");
            }

            return sb.toString();
        }

        /** Get the SVG location where the visual representation of the given node should be rendered.
         * @param node the node to determine the location for
         * @return the SVG location where the node should be rendered
         */
        private Vector2D getNodeLocation(final RegionNode2D node) {
            // find the node parent
            final RegionNode2D parent = node.getParent();
            final Vector2D parentLoc = nodeLocations.get(parent);
            if (parentLoc == null) {
                // this is the root node
                return Vector2D.of(
                        0.5 * svgWidth,
                        svgTop);
            } else {
                // align with the parent
                double parentXOffset = Math.max(
                        treeParentOffsetFactor * (svgWidth / (1 << parent.depth())),
                        treeParentXOffsetMin);
                if (node.isMinus()) {
                    parentXOffset = -parentXOffset;
                }

                return Vector2D.of(
                            parentLoc.getX() + parentXOffset,
                            parentLoc.getY() + treeVerticalSpacing
                        );
            }
        }
    }
}
