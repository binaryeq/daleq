package io.github.bineq.daleq.factextraction;

import io.github.bineq.daleq.factextraction.javap.JavapClassModel;
import io.github.bineq.daleq.factextraction.javap.JavapModelTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test fact extraction, use javap as oracle.
 * @author jens dietrich
 */
public class TestFactExtraction1 {
    private JavapClassModel classModel;
    private byte[] byteCode;
    private List<Fact> facts = new ArrayList<>();

    @BeforeEach
    public void setup() throws Exception {
        classModel = JavapClassModel.parse("mypck.MyClass", Path.of(JavapModelTest.class.getResource("/basic/mypck/MyClass.javap").getFile()));
        Path classFile = Path.of(JavapModelTest.class.getResource("/basic/mypck/MyClass.class").getFile());
        byteCode = Files.readAllBytes(classFile);
        facts = FactExtractor.extract(byteCode,false); // verification will be done in dedicated test
    }

    private Fact getFirstFact(AdditionalPredicates predicate) {
        return facts.stream()
            .filter(fact -> fact.predicate().equals(predicate))
            .findFirst()
            .orElseThrow();
    }

    private List<Fact> getFacts(AdditionalPredicates predicate) {
        return facts.stream()
            .filter(fact -> fact.predicate().equals(predicate))
            .collect(Collectors.toUnmodifiableList());
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
}
