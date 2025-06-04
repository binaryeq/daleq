package io.github.bineq.daleq.cli;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DaleqAnalyserTests {

    @Test
    public void testRuleLabel1() {
        String rule = "IS_ROOT_METHOD(\"R_IS_ROOT_METHOD_EQUALS\",\"equals\",\"(Ljava/lang/Object;)Z\").";
        String label = DaleqAnalyser.insertRuleLabelAndHighlightId(rule);
        String oracl = "IS_ROOT_METHOD(\"<strong><a id=\"R_IS_ROOT_METHOD_EQUALS\">R_IS_ROOT_METHOD_EQUALS</a></strong>\",\"equals\",\"(Ljava/lang/Object;)Z\").";
        assertEquals(oracl,label);
    }

    @Test
    public void testRuleLabel2() {
        String rule = "// foo";
        String label = DaleqAnalyser.insertRuleLabelAndHighlightId(rule);
        assertEquals("// foo",label);
    }
}
