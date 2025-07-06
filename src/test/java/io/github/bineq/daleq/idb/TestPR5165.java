package io.github.bineq.daleq.idb;

import io.github.bineq.daleq.Rules;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

// test for support for bytecode changes in this PR: https://github.com/openjdk/jdk/pull/5165
public class TestPR5165 extends AbstractIDBTest {

    @Override
    public Rules getRules() {
        return Rules.defaultRules();
    }

    @Override
    public String getPathOfClassUnderTest() {
        return "/PR5165/LoggingEventPreSerializationTransformer.class";
    }

    public static final String METHOD_ID = "ch/qos/logback/classic/net/LoggingEventPreSerializationTransformer::transform(Lch/qos/logback/classic/spi/ILoggingEvent;)Ljava/io/Serializable;";
    public static final String LINE = "2800";


    @Test
    public void testEDB() throws Exception {

        // slot positions in both CHECKCAST and NOP facts
        int methodIdSlotPosition = 1;
        int instructionCounterSlotPosition = 2;

        Predicate<String[]> methodContext = line -> line[methodIdSlotPosition].equals(METHOD_ID);
        Predicate<String[]> firstInstructionId = line -> line[instructionCounterSlotPosition].equals(LINE);

        List<String[]> facts = this.getEDBFacts("INVOKEINTERFACE",methodContext.and(firstInstructionId));
        assertEquals(1,facts.size());
        String[] fact = facts.get(0);
        assertEquals("getClass",fact[4]);
        assertEquals("()Ljava/lang/Class;",fact[5]);

        List<String[]> facts2 = this.getEDBFacts("INVOKEVIRTUAL",methodContext.and(firstInstructionId));
        assertEquals(0,facts2.size());

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
        assertEquals("getClass",fact[4]);
        assertEquals("()Ljava/lang/Class;",fact[5]);

        List<String[]> facts2 = this.getEDBFacts("IDB_INVOKEINTERFACE",methodContext.and(firstInstructionId));
        assertEquals(0,facts2.size());

        // test removal
        List<String[]> removedInstructionFacts = this.getIDBFacts("REMOVED_INSTRUCTION",methodContext.and(firstInstructionId));
        assertEquals(1,removedInstructionFacts.size());
        String[] removed = removedInstructionFacts.get(0);
        System.out.println(removed);
        assertEquals(METHOD_ID,removed[1]);
        assertEquals(LINE,removed[2]);
    }

}
