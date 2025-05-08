package io.github.bineq.daleq.edb;

import io.github.bineq.daleq.Fact;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue18SecondTest extends AbstractFactExtractionTest {
    @Override
    protected String getTestClass() {
        return "/issue18/StreamBootstrapper.class";
    }


    @Test
    public void test() {
        String methodRef = FactExtractor.getMethodReference("com/ctc/wstx/io/StreamBootstrapper", "resolveStreamEncoding","()V");
        List<Fact> facts = getInstructionFacts(methodRef).stream()
            .filter(fact -> fact.predicate().getName().equals("LOOKUPSWITCH"))
            .collect(Collectors.toUnmodifiableList());
        assertTrue(facts.size()>0);

        String labelsTerm = facts.get(0).values()[5].toString();
        assertFalse(labelsTerm.contains("org.objectweb.asm.tree.LabelNode@"));


    }
}
