package io.github.bineq.daleq.edb;

import io.github.bineq.daleq.Fact;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

public class Issue18FirstTest extends AbstractFactExtractionTest {
    @Override
    protected String getTestClass() {
        return "/issue18/ReaderConfig.class";
    }

    @Test
    public void test() {
        String methodRef = FactExtractor.getMethodReference("com/ctc/wstx/api/ReaderConfig", "setProperty","(Ljava/lang/String;ILjava/lang/Object;)Z");
        List<Fact> facts = getInstructionFacts(methodRef).stream()
            .filter(fact -> fact.predicate().getName().equals("TABLESWITCH"))
            .collect(Collectors.toUnmodifiableList());
        assertTrue(facts.size()>0);
        String labelsTerm = facts.get(0).values()[4].toString();
        assertFalse(labelsTerm.contains("org.objectweb.asm.tree.LabelNode@"));
    }
}
