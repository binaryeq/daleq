package io.github.bineq.daleq.souffle.provenance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


public class RuleParserTests {

    private void assertRuleIdEquals(String id, String rule) {
        String id2 = ProvenanceDB.parseSouffleRule(rule);
        assertEquals(id,id2);
    }

    private void assertRuleIdIsNull(String rule) {
        String id = ProvenanceDB.parseSouffleRule(rule);
        assertNull(id);
    }

    @Test
    public void testParseSouffleRule1() {
        assertRuleIdEquals(
            "R_REMOVE_PR5165",
            "REMOVED_INSTRUCTION(cat(\"R_REMOVE_PR5165\",\"[\",factid1,\",\",factid2,\"]\"),methodid,instructioncounter) :- "
        );
    }

    @Test
    public void testParseSouffleRule2() {
        assertRuleIdEquals(
            "R_IS_ENUM_STATIC_BLOCK",
            "IS_ENUM_STATIC_BLOCK(cat(\"R_IS_ENUM_STATIC_BLOCK\",\"[\",factid1,\",\",factid2,\"]\"),id) :- METHOD(factid1,id,classname,name,descriptor),IDB_IS_ENUM(factid2,classname),name=\"<clinit>\",descriptor=\"()V\"."
        );
    }

    @Test
    public void testParseSouffleRule3() {
        assertRuleIdEquals(
            "R_IS_ROOT_METHOD_GETCLASS",
            "IS_ROOT_METHOD(\"R_IS_ROOT_METHOD_GETCLASS\",\"getClass\", \"()Ljava/lang/Class;\")."
        );
    }

    @Test
    public void testParseSouffleRule4() {
        assertRuleIdIsNull(
            ".decl foo"
        );
    }

    @Test
    public void testParseSouffleRule5() {
        assertRuleIdIsNull(
            ""
        );
    }

    @Test
    public void testParseSouffleRule6() {
        assertRuleIdIsNull(
            "// this is a comment"
        );
    }

    @Test
    public void testParseSouffleRule7() {
        assertRuleIdIsNull(
            "  // this is a comment"
        );
    }


}
