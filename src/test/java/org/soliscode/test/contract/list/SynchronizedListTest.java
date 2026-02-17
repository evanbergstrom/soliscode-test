package org.soliscode.test.contract.list;

import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.support.WithArrayList;
import org.soliscode.test.contract.support.WithIntegerElement;

import java.util.ArrayList;

/**
 * **Contract-based tests for `SynchronizedListTest`**
 *
 * This class provides the implementation of the [ListContract] for testing
 * a [java.util.List] implementation, specifically [ArrayList] in this context.
 *
 * ## Test Scope
 * This class tests all methods defined in the [java.util.List] interface as implemented
 * by [ArrayList], including:
 * - Basic operations: `add`, `remove`, `get`, `set`, `size`, `isEmpty`
 * - Positional access and modification
 * - Iterator and ListIterator behavior
 * - Search operations: `indexOf`, `lastIndexOf`
 * - Range-view operations: `subList`
 *
 * ## Configuration
 * - The tests use [Integer] elements.
 * - The underlying implementation is [ArrayList] (via [WithArrayList]).
 *
 * @author evanbergstrom
 * @see ArrayList
 * @see ListContract
 * @since 1.0.0
 */
public class SynchronizedListTest extends AbstractTest
        implements ListContract<Integer, ArrayList<Integer>>, WithArrayList<Integer>, WithIntegerElement {


}
