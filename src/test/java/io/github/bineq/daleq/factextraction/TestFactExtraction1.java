package io.github.bineq.daleq.factextraction;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test fact extraction.
 * @author jens dietrich
 */
public class TestFactExtraction1 extends AbstractFactExtractionTest {

    @Override
    protected String getTestClass() {
        return "/basic/mypck/MyClass.class";
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
        assertEquals("mypck/MyClass",superClassFact.values()[0]);
        assertEquals("java/lang/Object",superClassFact.values()[1]);
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
        assertEquals("mypck/MyClass",classVersionFact.values()[0]);
        assertEquals(65,classVersionFact.values()[1]);
    }

    @Test
    public void testMethod1() {

        String methodRef = FactExtractor.getMethodReference("mypck/MyClass", "<init>","()V");
        List<Fact> instructionFacts = getInstructionFacts(methodRef);

        assertEquals(3,instructionFacts.size());

        assertEquals("ALOAD",instructionFacts.get(0).predicate().getName());
        assertEquals("INVOKESPECIAL",instructionFacts.get(1).predicate().getName());
        assertEquals("RETURN",instructionFacts.get(2).predicate().getName());

        assertEquals(methodRef,instructionFacts.get(0).values()[0]);
        assertEquals(methodRef,instructionFacts.get(1).values()[0]);
        assertEquals(methodRef,instructionFacts.get(2).values()[0]);

        assertEquals(1,instructionFacts.get(0).values()[1]);
        assertEquals(2,instructionFacts.get(1).values()[1]);
        assertEquals(3,instructionFacts.get(2).values()[1]);

    }

}
