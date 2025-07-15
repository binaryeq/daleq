package io.github.bineq.daleq.idb;

import io.github.bineq.daleq.Rules;
import org.junit.jupiter.api.Test;

public class TestIssue37a extends AbstractEquivalenceTest  {

    @Override
    public Rules getRules() {
        return Rules.defaultRules();
    }

    @Test
    public void testAnnotations() throws Exception {
        String class1 = "/issue37a/mvnc/ApplicationController.class";
        String class2 = "/issue37a/obfs/ApplicationController.class";

        // debugging
        String unProjectedIDB1 = IDBPrinter.print(computeIDB(class1));
        String unProjectedIDB2 = IDBPrinter.print(computeIDB(class2));

        testEquivalence(class1,class2);
    }
}
