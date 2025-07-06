package io.github.bineq.daleq.idb;

import io.github.bineq.daleq.Rules;
import org.junit.jupiter.api.Test;

public class TestConstantPoolOrderNormalisation extends AbstractEquivalenceTest {

    @Override
    public Rules getRules() {
        return Rules.defaultRules();
    }

    @Test
    public void testConstantPoolOrderNormalisation() throws Exception {
        String class1 = "/test-scenarios/scenario1/version1/Headers.class";
        String class2 = "/test-scenarios/scenario1/version2/Headers.class";
        testEquivalence(class1,class2);
    }

}
