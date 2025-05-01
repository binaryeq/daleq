package io.github.bineq.daleq.edb;

import io.github.bineq.daleq.Fact;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test fact extraction for a class containing conditionals.
 * @author jens dietrich
 */
public class TestFactExtraction3 extends AbstractFactExtractionTest {

    @Override
    protected String getTestClass() {
        return "/misc/encoding/ParserCLI.class";
    }


    @Test
    public void testFactVerification() throws VerificationException {
        for (Fact fact : facts) {
            fact.verify();
        }
    }

    @Test
    public void testInvokeDynamic() {

        String methodRef = FactExtractor.getMethodReference("org/jooq/ParserCLI", "interactiveMode","(Lorg/jooq/DSLContext;Lorg/jooq/ParserCLI$Args;)V");
        List<Fact> instructionFacts = getInstructionFacts(methodRef);
        List<Fact> invokeDynamicFacts = instructionFacts.stream()
            .filter(fact -> fact.predicate().getName().equals("INVOKEDYNAMIC"))
            .filter(fact -> fact.values()[3].equals("makeConcatWithConstants"))
            .collect(Collectors.toUnmodifiableList());

        System.out.println(invokeDynamicFacts);

//        Set<Fact> jumpFacts = instructionFacts.stream()
//            .filter(fact -> isJump(fact))
//            .collect(Collectors.toSet());
//
//        assertEquals(2,jumpFacts.size());
//        assertTrue(jumpFacts.contains(instructionFacts.get(2)));
//        assertTrue(jumpFacts.contains(instructionFacts.get(8)));
//
//        // inspect src/test/resources/conditional/mypck/ClassWithConditionals.javap for oracle
//        containsJump(instructionFacts.get(2),"L2");
//        containsJump(instructionFacts.get(8),"L4");

    }

}
