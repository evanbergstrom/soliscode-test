package org.soliscode.test.contract.list;

import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.support.WithArrayList;
import org.soliscode.test.contract.support.WithIntegerElement;

import java.util.ArrayList;

public class SynchronizedListTest extends AbstractTest
        implements ListContract<Integer, ArrayList<Integer>>, WithArrayList<Integer>, WithIntegerElement {


}
