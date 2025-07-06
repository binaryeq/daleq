package io.github.bineq.daleq.idb;

import io.github.bineq.daleq.Rules;
import org.junit.jupiter.api.Test;

public class TestInlineValuesForEnums extends AbstractEquivalenceTest {

    @Override
    public Rules getRules() {
        return Rules.defaultRules();
    }

    @Test
    public void testInlinedValuesForEnums() throws Exception {
        String class1 = "/test-scenarios/scenario3/version1/NoopCallback.class";
        String class2 = "/test-scenarios/scenario3/version2/NoopCallback.class";

        // debugging
        // String unProjectedDB1 = IDBPrinter.print(computeIDB(class1));
        // String unProjectedDB2 = IDBPrinter.print(computeIDB(class2));

        testEquivalence(class1,class2);
    }
}
