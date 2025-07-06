package io.github.bineq.daleq.idb;

import io.github.bineq.daleq.Rules;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TestRemoveDuplicatedCheckcast extends AbstractEquivalenceTest {

    @Override
    public Rules getRules() {
        return Rules.defaultRules();
    }

    @Disabled // need new oracles, as there are changes related to JEP280 in those classes
    @Test
    public void testRemoveDuplicatedCheckcast() throws Exception {
        String class1 = "/test-scenarios/scenario2/version1/XMLPropertyListConfiguration.class";
        String class2 = "/test-scenarios/scenario2/version2/XMLPropertyListConfiguration.class";
        testEquivalence(class1,class2);
    }
}
