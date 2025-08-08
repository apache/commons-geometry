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
package org.apache.commons.geometry.euclidean.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.apache.commons.geometry.core.internal.GeometryInternalUtils;

/** Abstract base class for joining unconnected path elements into connected, directional
 * paths. The connection algorithm is exposed as a set of protected methods, allowing subclasses
 * to define their own public API. Implementations must supply their own subclass of {@link ConnectableElement}
 * specific for the objects being connected.
 *
 * <p>The connection algorithm proceeds as follows:
 * <ul>
 *      <li>Create a sorted list of {@link ConnectableElement}s.</li>
 *      <li>For each element, attempt to find other elements with start points next the
 *      first instance's end point by calling {@link ConnectableElement#getConnectionSearchKey()} and
 *      using the returned instance to locate a search start location in the sorted element list.</li>
 *      <li>Search up through the sorted list from the start location, testing each element for possible connectivity
 *      with {@link ConnectableElement#canConnectTo(AbstractPathConnector.ConnectableElement)}. Collect possible
 *      connections in a list. Terminate the search when
 *      {@link ConnectableElement#shouldContinueConnectionSearch(AbstractPathConnector.ConnectableElement, boolean)}
 *      returns false.
 *      <li>Repeat the previous step searching downward through the list from the start location.</li>
 *      <li>Select the best connection option from the list of possible connections, using
 *      {@link #selectPointConnection(AbstractPathConnector.ConnectableElement, List)}
 *      and/or {@link #selectConnection(AbstractPathConnector.ConnectableElement, List)} when multiple possibilities
 *      are found.</li>
 *      <li>Repeat the above steps for each element. When done, the elements represent a linked list
 *      of connected paths.</li>
 * </ul>
 *
 * <p>This class is not thread-safe.</p>
 *
 * @param <E> Element type
 * @see ConnectableElement
 */
public abstract class AbstractPathConnector<E extends AbstractPathConnector.ConnectableElement<E>> {
    /** List of path elements. */
    private final NavigableSet<E> pathElements = new TreeSet<>();

    /** View of the path element set in descending order. */
    private final NavigableSet<E> pathElementsDescending = pathElements.descendingSet();

    /** List used to store possible connections for the current element. */
    private final List<E> possibleConnections = new ArrayList<>();

    /** List used to store possible point-like (zero-length) connections for the current element. */
    private final List<E> possiblePointConnections = new ArrayList<>();

    /** Create an instance. */
    public AbstractPathConnector() {
        // Do nothing
    }

    /** Add a collection of path elements to the connector and attempt to connect each new element
     * with previously added ones.
     * @param elements path elements to connect
     */
    protected void connectPathElements(final Iterable<E> elements) {
        elements.forEach(this::addPathElement);

        for (final E element : elements) {
            makeForwardConnection(element);
        }
    }

    /** Add a single path element to the connector, leaving it unconnected until a later call
     * to {@link #connectPathElements(Iterable)} or {@link #computePathRoots()}.
     * @param element value to add to the connector
     * @see #connectPathElements(Iterable)
     * @see #computePathRoots()
     */
    protected void addPathElement(final E element) {
        pathElements.add(element);
    }

    /** Compute all connected paths and return a list of path elements representing
     * the roots (start locations) of each. Each returned element is the head of a
     * (possibly circular) linked list that follows a connected path.
     *
     * <p>The connector is reset after this call. Further calls to add elements
     * will result in new paths being generated.</p>
     * @return a list of root elements for the computed connected paths
     */
    protected List<E> computePathRoots() {
        for (final E element : pathElements) {
            followForwardConnections(element);
        }

        final List<E> rootEntries = new ArrayList<>();
        E root;
        for (final E element : pathElements) {
            root = element.exportPath();
            if (root != null) {
                rootEntries.add(root);
            }
        }

        pathElements.clear();
        possibleConnections.clear();
        possiblePointConnections.clear();

        return rootEntries;
    }

    /** Find and follow forward connections from the given start element.
     * @param start element to begin the connection operation with
     */
    private void followForwardConnections(final E start) {
        E current = start;

        while (current != null && current.hasEnd() && !current.hasNext()) {
            current = makeForwardConnection(current);
        }
    }

    /** Connect the end point of the given element to the start point of another element. Returns
     * the newly connected element or null if no forward connection was made.
     * @param element element to connect
     * @return the next element in the path or null if no connection was made
     */
    private E makeForwardConnection(final E element) {
        findPossibleConnections(element);

        E next = null;

        // select from all available connections, handling point-like segments first
        if (!possiblePointConnections.isEmpty()) {
            next = (possiblePointConnections.size() == 1) ?
                    possiblePointConnections.get(0) :
                    selectPointConnection(element, possiblePointConnections);
        } else if (!possibleConnections.isEmpty()) {

            next = (possibleConnections.size() == 1) ?
                    possibleConnections.get(0) :
                    selectConnection(element, possibleConnections);
        }

        if (next != null) {
            element.connectTo(next);
        }

        return next;
    }

    /** Find possible connections for the given element and place them in the
     * {@link #possibleConnections} and {@link #possiblePointConnections} lists.
     * @param element the element to find connections for
     */
    private void findPossibleConnections(final E element) {
        possibleConnections.clear();
        possiblePointConnections.clear();

        if (element.hasEnd()) {
            final E searchKey = element.getConnectionSearchKey();

            // search up
            for (final E candidate : pathElements.tailSet(searchKey)) {
                if (!addPossibleConnection(element, candidate) &&
                        !element.shouldContinueConnectionSearch(candidate, true)) {
                    break;
                }
            }

            // search down
            for (final E candidate : pathElementsDescending.tailSet(searchKey, false)) {
                if (!addPossibleConnection(element, candidate) &&
                        !element.shouldContinueConnectionSearch(candidate, false)) {
                    break;
                }
            }
        }
    }

    /** Add the candidate to one of the connection lists if it represents a possible connection. Returns
     * true if the candidate was added, otherwise false.
     * @param element element to check for connections with
     * @param candidate candidate connection element
     * @return true if the candidate is a possible connection
     */
    private boolean addPossibleConnection(final E element, final E candidate) {
        if (!GeometryInternalUtils.sameInstance(element, candidate) &&
                !candidate.hasPrevious() &&
                candidate.hasStart() &&
                element.canConnectTo(candidate)) {

            if (element.endPointsEq(candidate)) {
                possiblePointConnections.add(candidate);
            } else {
                possibleConnections.add(candidate);
            }

            return true;
        }

        return false;
    }

    /** Method called to select a connection to use for a given element when multiple zero-length connections are
     * available. The algorithm here attempts to choose the point most likely to produce a logical path by selecting
     * the outgoing element with the smallest relative angle with the incoming element, with unconnected element
     * preferred over ones that are already connected (thereby allowing other connections to occur in the path).
     * @param incoming the incoming element
     * @param outgoingList list of available outgoing point-like connections
     * @return the connection to use
     */
    protected E selectPointConnection(final E incoming, final List<E> outgoingList) {

        double angle;
        boolean isUnconnected;

        double smallestAngle = 0.0;
        E bestElement = null;
        boolean bestIsUnconnected = false;

        for (final E outgoing : outgoingList) {
            angle = Math.abs(incoming.getRelativeAngle(outgoing));
            isUnconnected = !outgoing.hasNext();

            if (bestElement == null || (!bestIsUnconnected && isUnconnected) ||
                    (bestIsUnconnected == isUnconnected && angle < smallestAngle)) {

                smallestAngle = angle;
                bestElement = outgoing;
                bestIsUnconnected = isUnconnected;
            }
        }

        return bestElement;
    }

    /** Method called to select a connection to use for a given segment when multiple non-length-zero
     * connections are available. In this case, the selection of the outgoing connection depends only
     * on the desired characteristics of the connected path.
     * @param incoming the incoming segment
     * @param outgoing list of available outgoing connections; will always contain at least
     *      two elements
     * @return the connection to use
     */
    protected abstract E selectConnection(E incoming, List<E> outgoing);

    /** Class used to represent connectable path elements for use with {@link AbstractPathConnector}.
     * Subclasses must fulfill the following requirements in order for path connection operations
     * to work correctly:
     * <ul>
     *      <li>Implement {@link #compareTo(Object)} such that elements are sorted by their start
     *      point locations. Other criteria may be used as well but elements with start points in close
     *      proximity must be grouped together.</li>
     *      <li>Implement {@link #getConnectionSearchKey()} such that it returns an instance that will be placed
     *      next to elements with start points close to the current instance's end point when sorted with
     *      {@link #compareTo(Object)}.</li>
     *      <li>Implement {@link #shouldContinueConnectionSearch(AbstractPathConnector.ConnectableElement, boolean)}
     *      such that it returns false when the search for possible connections through a sorted list of elements
     *      may terminate.</li>
     * </ul>
     *
     * @param <E> Element type
     * @see AbstractPathConnector
     */
    public abstract static class ConnectableElement<E extends ConnectableElement<E>>
        implements Comparable<E> {
        /** Next connected element. */
        private E next;

        /** Previous connected element. */
        private E previous;

        /** Flag set to true when this element has exported its value to a path. */
        private boolean exported;

        /** Create an instance. */
        public ConnectableElement() {
            // Do nothing
        }

        /** Return true if the instance is connected to another element's start point.
         * @return true if the instance has a next element
         */
        public boolean hasNext() {
            return next != null;
        }

        /** Get the next connected element in the path, if any.
         * @return the next connected segment in the path; may be null
         */
        public E getNext() {
            return next;
        }

        /** Set the next connected element for this path. This is intended for
         * internal use only. Callers should use the {@link #connectTo(AbstractPathConnector.ConnectableElement)}
         * method instead.
         * @param next next path element
         */
        protected void setNext(final E next) {
            this.next = next;
        }

        /** Return true if another element is connected to this instance's start point.
         * @return true if the instance has a previous element
         */
        public boolean hasPrevious() {
            return previous != null;
        }

        /** Get the previous connected element in the path, if any.
         * @return the previous connected element in the path; may be null
         */
        public E getPrevious() {
            return previous;
        }

        /** Set the previous connected element for this path. This is intended for
         * internal use only. Callers should use the {@link #connectTo(AbstractPathConnector.ConnectableElement)}
         * method instead.
         * @param previous previous path element
         */
        protected void setPrevious(final E previous) {
            this.previous = previous;
        }

        /** Connect this instance's end point to the given element's start point. No validation
         * is performed in this method. The {@link #canConnectTo(AbstractPathConnector.ConnectableElement)}
         * method must have been called previously.
         * @param nextElement the next element in the path
         */
        public void connectTo(final E nextElement) {
            setNext(nextElement);
            nextElement.setPrevious(getSelf());
        }

        /** Export the path that this element belongs to, returning the root
         * segment. This method traverses all connected element, sets their
         * exported flags to true, and returns the root element of the path
         * (or this element in the case of a loop). Each path can only be
         * exported once. Later calls to this method on this instance or any of its
         * connected elements will return null.
         * @return the root of the path or null if the path that this element
         *      belongs to has already been exported
         */
        public E exportPath() {
            if (markExported()) {

                // export the connected portions of the path, moving both
                // forward and backward
                E current;
                E root = getSelf();

                // forward
                current = next;
                while (current != null && current.markExported()) {
                    current = current.getNext();
                }

                // backward
                current = previous;
                while (current != null && current.markExported()) {
                    root = current;
                    current = current.getPrevious();
                }

                return root;
            }

            return null;
        }

        /** Set the export flag for this instance to true. Returns true
         * if the flag was changed and false otherwise.
         * @return true if the flag was changed and false if it was
         *      already set to true
         */
        protected boolean markExported() {
            if (!exported) {
                exported = true;
                return true;
            }
            return false;
        }

        /** Return true if this instance has a start point that can be
         * connected to another element's end point.
         * @return true if this instance has a start point that can be
         *      connected to another element's end point
         */
        public abstract boolean hasStart();

        /** Return true if this instance has an end point that can be
         * connected to another element's start point.
         * @return true if this instance has an end point that can be
         *      connected to another element's start point
         */
        public abstract boolean hasEnd();

        /** Return true if the end point of this instance should be considered
         * equivalent to the end point of the argument.
         * @param other element to compare end points with
         * @return true if this instance has an end point equivalent to that
         *      of the argument
         */
        public abstract boolean endPointsEq(E other);

        /** Return true if this instance's end point can be connected to
         * the argument's start point.
         * @param nextElement candidate for the next element in the path; this value
         *      is guaranteed to not be null and to contain a start point
         * @return true if this instance's end point can be connected to
         *      the argument's start point
         */
        public abstract boolean canConnectTo(E nextElement);

        /** Return the relative angle between this element and the argument.
         * @param other element to compute the angle with
         * @return the relative angle between this element and the argument
         */
        public abstract double getRelativeAngle(E other);

        /** Get a new instance used as a search key to help locate other elements
         * with start points matching this instance's end point. The only restriction
         * on the returned instance is that it be compatible with the implementation
         * class' {@link #compareTo(Object)} method.
         * @return a new instance used to help locate other path elements with start
         *      points equivalent to this instance's end point
         */
        public abstract E getConnectionSearchKey();

        /** Return true if the search for possible connections should continue through
         * the sorted set of possible path elements given the current candidate element
         * and search direction. The search operation stops for the given direction
         * when this method returns false.
         * @param candidate last tested candidate connection element
         * @param ascending true if the search is proceeding in an ascending direction;
         *      false otherwise
         * @return true if the connection search should continue
         */
        public abstract boolean shouldContinueConnectionSearch(E candidate, boolean ascending);

        /** Return the current instance as the generic type.
         * @return the current instance as the generic type.
         */
        protected abstract E getSelf();
    }
}
