package io.github.bineq.daleq.factextraction;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test fact extraction for a class containing conditionals.
 * @author jens dietrich
 */
public class TestFactExtraction2 extends AbstractFactExtractionTest {

    @Override
    protected String getTestClass() {
        return "/conditional/mypck/ClassWithConditionals.class";
    }


    @Test
    public void testFactVerification() throws VerificationException {
        for (Fact fact : facts) {
            fact.verify();
        }
    }

    @Test
    public void testSuperClass() {
        Fact superClassFact = getFirstFact(AdditionalPredicates.SUPERCLASS);
        assertEquals(AdditionalPredicates.SUPERCLASS,superClassFact.predicate());
        assertEquals("mypck/ClassWithConditionals",superClassFact.values()[1]);
        assertEquals("java/lang/Object",superClassFact.values()[2]);
    }

    @Test
    public void testInterfaces() {
        List<Fact> interfaceFacts = getFacts(AdditionalPredicates.INTERFACE);
        assertEquals(0,interfaceFacts.size());
    }

    @Test
    public void testClassVersion() {
        Fact classVersionFact = getFirstFact(AdditionalPredicates.VERSION);
        assertEquals(AdditionalPredicates.VERSION,classVersionFact.predicate());
        assertEquals("mypck/ClassWithConditionals",classVersionFact.values()[1]);
        assertEquals(65,classVersionFact.values()[2]);
    }

    @Test
    public void testMethod1() {

        String methodRef = FactExtractor.getMethodReference("mypck/ClassWithConditionals", "<init>","()V");
        List<Fact> instructionFacts = getInstructionFacts(methodRef);

        assertEquals(3,instructionFacts.size());

        assertEquals("ALOAD",instructionFacts.get(0).predicate().getName());
        assertEquals("INVOKESPECIAL",instructionFacts.get(1).predicate().getName());
        assertEquals("RETURN",instructionFacts.get(2).predicate().getName());

        assertEquals(methodRef,instructionFacts.get(0).values()[1]);
        assertEquals(methodRef,instructionFacts.get(1).values()[1]);
        assertEquals(methodRef,instructionFacts.get(2).values()[1]);

        assertEquals(1,instructionFacts.get(0).values()[2]);
        assertEquals(2,instructionFacts.get(1).values()[2]);
        assertEquals(3,instructionFacts.get(2).values()[2]);

    }

    @Test
    public void testMethod2() {

        String methodRef = FactExtractor.getMethodReference("mypck/ClassWithConditionals", "foo","(II)I");
        List<Fact> instructionFacts = getInstructionFacts(methodRef);

        assertEquals(12,instructionFacts.size());

        assertEquals("ILOAD",instructionFacts.get(0).predicate().getName());
        assertEquals("ILOAD",instructionFacts.get(1).predicate().getName());
        assertEquals("IF_ICMPGE",instructionFacts.get(2).predicate().getName());
        assertEquals("ICONST_1",instructionFacts.get(3).predicate().getName());
        assertEquals("IRETURN",instructionFacts.get(4).predicate().getName());
        assertEquals("ILOAD",instructionFacts.get(5).predicate().getName());
        assertEquals("ILOAD",instructionFacts.get(6).predicate().getName());
        assertEquals("IF_ICMPLE",instructionFacts.get(7).predicate().getName());
        assertEquals("ICONST_M1",instructionFacts.get(8).predicate().getName());
        assertEquals("IRETURN",instructionFacts.get(9).predicate().getName());
        assertEquals("ICONST_0",instructionFacts.get(10).predicate().getName());
        assertEquals("IRETURN",instructionFacts.get(11).predicate().getName());


    }

    @Test
    public void testMethod2Jumps() {

        String methodRef = FactExtractor.getMethodReference("mypck/ClassWithConditionals", "foo","(II)I");
        List<Fact> instructionFacts = getInstructionFacts(methodRef);

        Set<Fact> jumpFacts = instructionFacts.stream()
            .filter(fact -> isJump(fact))
            .collect(Collectors.toSet());

        // inspect src/test/resources/conditional/mypck/ClassWithConditionals.javap for oracle
        assertEquals(2,jumpFacts.size());
        containsJump(jumpFacts,3,6);
        containsJump(jumpFacts,8,11);
    }

}
