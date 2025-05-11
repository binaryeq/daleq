package io.github.bineq.daleq.edb;

import io.github.bineq.daleq.Fact;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue19Test extends AbstractFactExtractionTest {
    @Override
    protected String getTestClass() {
        return "/issue19/MockitoCore.class";
    }

    @Test
    public void test() {
        String methodRef = FactExtractor.getMethodReference("org/mockito/internal/MockitoCore", "lambda$mockConstruction$0","(Ljava/util/function/Function;Ljava/lang/Class;Lorg/mockito/MockedConstruction$Context;)Lorg/mockito/mock/MockCreationSettings;");
        List<Fact> facts = getInstructionFacts(methodRef).stream()
            .filter(fact -> fact.predicate().getName().equals("INVOKEDYNAMIC"))
            .collect(Collectors.toUnmodifiableList());
        assertTrue(facts.size()==2);
        String serializedFact = facts.get(0).asSouffleFact();
        assertFalse(serializedFact.contains("\n"));
    }
}
