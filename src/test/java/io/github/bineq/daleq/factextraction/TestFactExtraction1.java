package io.github.bineq.daleq.factextraction;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertEquals(2,interfaceFacts.size());
        List<String> interfaces = interfaceFacts.stream()
            .map(fact -> fact.values()[1].toString())
            .collect(Collectors.toUnmodifiableList());
        assertTrue(interfaces.contains("java/io/Serializable"));
        assertTrue(interfaces.contains("java/lang/Cloneable"));
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

        // arguments
        assertEquals("java/lang/Object",instructionFacts.get(1).values()[2]);
        assertEquals("<init>",instructionFacts.get(1).values()[3]);
        assertEquals("()V",instructionFacts.get(1).values()[4]);

    }


    @Test
    public void testMethod2() {

        String methodRef = FactExtractor.getMethodReference("mypck/MyClass", "main","([Ljava/lang/String;)V");
        List<Fact> instructionFacts = getInstructionFacts(methodRef);

        assertEquals(4,instructionFacts.size());

        assertEquals("GETSTATIC",instructionFacts.get(0).predicate().getName());
        assertEquals("LDC",instructionFacts.get(1).predicate().getName());
        assertEquals("INVOKEVIRTUAL",instructionFacts.get(2).predicate().getName());
        assertEquals("RETURN",instructionFacts.get(3).predicate().getName());

        assertEquals(methodRef,instructionFacts.get(0).values()[0]);
        assertEquals(methodRef,instructionFacts.get(1).values()[0]);
        assertEquals(methodRef,instructionFacts.get(2).values()[0]);
        assertEquals(methodRef,instructionFacts.get(3).values()[0]);

        assertEquals(1,instructionFacts.get(0).values()[1]);
        assertEquals(2,instructionFacts.get(1).values()[1]);
        assertEquals(3,instructionFacts.get(2).values()[1]);
        assertEquals(4,instructionFacts.get(3).values()[1]);

        // arguments

        assertEquals("java/lang/System",instructionFacts.get(0).values()[2]);
        assertEquals("out",instructionFacts.get(0).values()[3]);
        assertEquals("Ljava/io/PrintStream;",instructionFacts.get(0).values()[4]);

        assertEquals("Hello World",instructionFacts.get(1).values()[2]);

        assertEquals("java/io/PrintStream",instructionFacts.get(2).values()[2]);
        assertEquals("println",instructionFacts.get(2).values()[3]);
        assertEquals("(Ljava/lang/String;)V",instructionFacts.get(2).values()[4]);
    }

}
