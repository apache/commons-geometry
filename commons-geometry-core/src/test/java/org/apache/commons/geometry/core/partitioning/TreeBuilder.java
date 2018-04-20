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

import java.io.IOException;
import java.text.ParseException;
import java.util.StringTokenizer;

import org.apache.commons.geometry.core.Space;
import org.apache.commons.geometry.core.partitioning.AbstractRegion;
import org.apache.commons.geometry.core.partitioning.BSPTree;
import org.apache.commons.geometry.core.partitioning.Hyperplane;

/** Local class for building an {@link AbstractRegion} tree.
 * @param <S> Type of the space.
 */
public abstract class TreeBuilder<S extends Space> {

    /** Keyword for tolerance. */
    private static final String TOLERANCE = "tolerance";

    /** Keyword for internal nodes. */
    private static final String INTERNAL  = "internal";

    /** Keyword for leaf nodes. */
    private static final String LEAF      = "leaf";

    /** Keyword for plus children trees. */
    private static final String PLUS      = "plus";

    /** Keyword for minus children trees. */
    private static final String MINUS     = "minus";

    /** Keyword for true flags. */
    private static final String TRUE      = "true";

    /** Keyword for false flags. */
    private static final String FALSE     = "false";

    /** Tree root. */
    private BSPTree<S> root;

    /** Tolerance. */
    private final double tolerance;

    /** Tokenizer parsing string representation. */
    private final StringTokenizer tokenizer;

    /** Simple constructor.
     * @param type type of the expected representation
     * @param reader reader for the string representation
     * @exception IOException if the string cannot be read
     * @exception ParseException if the string cannot be parsed
     */
    public TreeBuilder(final String type, final String s)
        throws IOException, ParseException {
        root = null;
        tokenizer = new StringTokenizer(s);
        getWord(type);
        getWord(TOLERANCE);
        tolerance = getNumber();
        getWord(PLUS);
        root = new BSPTree<>();
        parseTree(root);
        if (tokenizer.hasMoreTokens()) {
            throw new ParseException("unexpected " + tokenizer.nextToken(), 0);
        }
    }

    /** Parse a tree.
     * @param node start node
     * @exception IOException if the string cannot be read
     * @exception ParseException if the string cannot be parsed
     */
    private void parseTree(final BSPTree<S> node)
        throws IOException, ParseException {
        if (INTERNAL.equals(getWord(INTERNAL, LEAF))) {
            // this is an internal node, it has a cut sub-hyperplane (stored as a whole hyperplane)
            // then a minus tree, then a plus tree
            node.insertCut(parseHyperplane());
            getWord(MINUS);
            parseTree(node.getMinus());
            getWord(PLUS);
            parseTree(node.getPlus());
        } else {
            // this is a leaf node, it has only an inside/outside flag
            node.setAttribute(getBoolean());
        }
    }

    /** Get next word.
     * @param allowed allowed values
     * @return parsed word
     * @exception IOException if the string cannot be read
     * @exception ParseException if the string cannot be parsed
     */
    protected String getWord(final String ... allowed)
        throws IOException, ParseException {
        final String token = tokenizer.nextToken();
        for (final String a : allowed) {
            if (a.equals(token)) {
                return token;
            }
        }
        throw new ParseException(token + " != " + allowed[0], 0);
    }

    /** Get next number.
     * @return parsed number
     * @exception IOException if the string cannot be read
     * @exception NumberFormatException if the string cannot be parsed
     */
    protected double getNumber()
        throws IOException, NumberFormatException {
        return Double.parseDouble(tokenizer.nextToken());
    }

    /** Get next boolean.
     * @return parsed boolean
     * @exception IOException if the string cannot be read
     * @exception ParseException if the string cannot be parsed
     */
    protected boolean getBoolean()
        throws IOException, ParseException {
        return getWord(TRUE, FALSE).equals(TRUE);
    }

    /** Get the built tree.
     * @return built tree
     */
    public BSPTree<S> getTree() {
        return root;
    }

    /** Get the tolerance.
     * @return tolerance
     */
    public double getTolerance() {
        return tolerance;
    }

    /** Parse an hyperplane.
     * @return next hyperplane from the stream
     * @exception IOException if the string cannot be read
     * @exception ParseException if the string cannot be parsed
     */
    protected abstract Hyperplane<S> parseHyperplane()
        throws IOException, ParseException;

}