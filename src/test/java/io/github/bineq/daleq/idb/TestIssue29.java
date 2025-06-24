package io.github.bineq.daleq.idb;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

// test for support for bytecode changes in issue19

// see issue 29 for discussion

@Disabled public class TestIssue29 extends AbstractIDBTest {

    @Override
    public String getRulesPath() {
        return "/rules/advanced.souffle";
    }

    @Override
    public String getPathOfClassUnderTest() {
        return "/issue29/jar1/AbstractYAMLBasedConfiguration.class";
    }

    public static final String METHOD_ID = "org/apache/commons/configuration2/AbstractYAMLBasedConfiguration::lambda$parseMap$1(Lorg/apache/commons/configuration2/tree/ImmutableNode$Builder;Ljava/lang/String;Ljava/lang/Object;)V";
    public static final String LINE = "600";

    @Test
    public void testEDB() throws Exception {

        int methodIdSlotPosition = 1;
        int instructionCounterSlotPosition = 2;

        Predicate<String[]> methodContext = line -> line[methodIdSlotPosition].equals(METHOD_ID);
        Predicate<String[]> firstInstructionId = line -> line[instructionCounterSlotPosition].equals(LINE);

        List<String[]> facts = this.getEDBFacts("INVOKESTATIC",methodContext.and(firstInstructionId));
        assertEquals(1,facts.size());
        String[] fact = facts.get(0);
        assertEquals("java/util/Objects",fact[3]);
        assertEquals("requireNonNull",fact[4]);
//        assertEquals("(Ljava/lang/Object;)Ljava/lang/Object;",fact[5]);

    }

    @Test
    public void testIDB() throws Exception {

        int methodIdSlotPosition = 1;
        int instructionCounterSlotPosition = 2;

        Predicate<String[]> methodContext = line -> line[methodIdSlotPosition].equals(METHOD_ID);
        Predicate<String[]> firstInstructionId = line -> line[instructionCounterSlotPosition].equals(LINE);

        List<String[]> facts = this.getIDBFacts("IDB_INVOKEVIRTUAL",methodContext.and(firstInstructionId));
        assertEquals(1,facts.size());
        String[] fact = facts.get(0);
        assertEquals("java/lang/Object",fact[3]);
        assertEquals("getClass",fact[4]);
        assertEquals("()Ljava/lang/Class;",fact[5]);

    }

}
