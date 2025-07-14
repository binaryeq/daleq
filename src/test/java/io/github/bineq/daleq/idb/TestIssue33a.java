package io.github.bineq.daleq.idb;

import io.github.bineq.daleq.Rules;
import org.junit.jupiter.api.Test;

public class TestIssue33a extends AbstractEquivalenceTest  {

    @Override
    public Rules getRules() {
        return Rules.defaultRules();
    }

    @Test
    public void testFinalInferenceForAllAnonymousInnerClasses() throws Exception {
        String class1 = "/issue33a/mvnc/CacheBuilder$3.class";
        String class2 = "/issue33a/gaoss/CacheBuilder$3.class";

        // debugging
        String unProjectedIDB1 = IDBPrinter.print(computeIDB(class1));
        String unProjectedIDB2 = IDBPrinter.print(computeIDB(class2));

        testEquivalence(class1,class2);
    }
}
