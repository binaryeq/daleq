package io.github.bineq.daleq.idb;

import io.github.bineq.daleq.Rules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestIssue109 extends AbstractEquivalenceTest  {

    @Override
    public Rules getRules() {
        return Rules.defaultRules();
    }

    @Test
    public void testFinalInferenceForAllAnonymousInnerClasses() throws Exception {
        String class1 = "/issue109/mvnc/WeakHashtable$1.class";
        String class2 = "/issue109/obfs/WeakHashtable$1.class";

        // debugging
        String idb1 = IDBPrinter.print(computeIDB(class1).project());
        String idb2 = IDBPrinter.print(computeIDB(class2).project());

        assertEquals(idb1,idb2);
    }
}
