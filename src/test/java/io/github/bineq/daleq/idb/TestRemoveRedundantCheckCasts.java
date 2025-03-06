package io.github.bineq.daleq.idb;

import io.github.bineq.daleq.Souffle;
import io.github.bineq.daleq.edb.FactExtractor;
import org.junit.jupiter.api.Test;
import java.util.function.Predicate;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRemoveRedundantCheckCasts extends AbstractIDBTest {

    @Override
    public String getRulesPath() {
        return "/rules/remove-redundant-checkcast.souffle";
    }

    @Override
    public String getPathOfClassUnderTest() {
        return "/test-scenarios/scenario2/version1/XMLPropertyListConfiguration.class";
    }

    @Test
    public void testEDB() throws Exception {
        FactExtractor.extractAndExport(this.classFile,this.edbDef,this.edbFactDir,true);
        // Souffle.createIDB(this.edbDef,rules,this.edbFactDir,this.idbFactDir);

        // slot positions in both CHECKCAST and NOP facts
        int idSlotPosition = 0;
        int methodIdSlotPosition = 1;
        int instructionCounterSlotPosition = 2;

        Predicate<String[]> methodContext = line -> line[methodIdSlotPosition].equals("org/apache/commons/configuration2/plist/XMLPropertyListConfiguration::addPropertyInternal(Ljava/lang/String;Ljava/lang/Object;)V");
        Predicate<String[]> firstInstructionId = line -> line[instructionCounterSlotPosition].equals("18");
        Predicate<String[]> secondInstructionId = line -> line[instructionCounterSlotPosition].equals("19");

        assertEquals(1,this.getEDBFacts("CHECKCAST",methodContext.and(firstInstructionId)).size());
        assertEquals(1,this.getEDBFacts("CHECKCAST",methodContext.and(secondInstructionId)).size());

    }

    @Test
    public void testIDB() throws Exception {
        FactExtractor.extractAndExport(this.classFile,this.edbDef,this.edbFactDir,true);
        Souffle.createIDB(this.edbDef,rules,this.edbFactDir,this.idbFactDir,this.mergedEDBAndRules);

        // slot positions in CHECKCAST and NOP facts
        int idSlotPosition = 0;
        int methodIdSlotPosition = 1;
        int instructionCounterSlotPosition = 2;

        Predicate<String[]> methodContext = line -> line[methodIdSlotPosition].equals("org/apache/commons/configuration2/plist/XMLPropertyListConfiguration::addPropertyInternal(Ljava/lang/String;Ljava/lang/Object;)V");
        Predicate<String[]> firstInstructionId = line -> line[instructionCounterSlotPosition].equals("18");
        Predicate<String[]> secondInstructionId = line -> line[instructionCounterSlotPosition].equals("19");

        assertEquals(1,this.getIDBFacts("IDB_CHECKCAST",methodContext.and(firstInstructionId)).size());
        assertEquals(1,this.getIDBFacts("NOPE",methodContext.and(secondInstructionId)).size());

        // one instruction should have been removed !
        assertEquals(0,this.getIDBFacts("IDB_CHECKCAST",methodContext.and(secondInstructionId)).size());

    }


}
