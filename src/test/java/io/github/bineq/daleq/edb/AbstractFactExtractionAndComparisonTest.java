package io.github.bineq.daleq.edb;

import io.github.bineq.daleq.Fact;
import io.github.bineq.daleq.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.objectweb.asm.tree.JumpInsnNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract fact abstraction test.
 * @author jens dietrich
 */
public abstract class AbstractFactExtractionAndComparisonTest {

    protected byte[] byteCode1;
    protected byte[] byteCode2;
    protected List<Fact> facts1 = new ArrayList<>();
    protected List<Fact> facts2 = new ArrayList<>();

    protected abstract String getTestClass1();
    protected abstract String getTestClass2();

    @BeforeEach
    public void setup() throws Exception {
        Path classFile1 = Path.of(AbstractFactExtractionAndComparisonTest.class.getResource(getTestClass1()).getFile());
        byteCode1 = Files.readAllBytes(classFile1);
        facts1 = FactExtractor.extract(byteCode1,false);

        Path classFile2 = Path.of(AbstractFactExtractionAndComparisonTest.class.getResource(getTestClass2()).getFile());
        byteCode2 = Files.readAllBytes(classFile2);
        facts2 = FactExtractor.extract(byteCode2,false); // verification will be done in dedicated test
    }

    protected Fact getFirstFact(List<Fact> facts,EBDAdditionalPredicates predicate) {
        return facts.stream()
            .filter(fact -> fact.predicate().equals(predicate))
            .findFirst()
            .orElseThrow();
    }


    protected List<Fact> getFacts(List<Fact> facts,EBDAdditionalPredicates predicate) {
        return facts.stream()
            .filter(fact -> fact.predicate().equals(predicate))
            .collect(Collectors.toUnmodifiableList());
    }


    /**
     * Get the facts representing the instructions within a particular method, in order of occurrence.
     * @param methodRef
     * @return
     */
    List<Fact> getInstructionFacts(List<Fact> facts,String methodRef) {
        return facts.stream()
            .filter(fact -> fact.predicate().isInstructionPredicate())
            .filter(fact -> fact.values()[1].equals(methodRef))
            .sorted(Comparator.comparing(fact -> Integer.parseInt(fact.values()[2].toString())))
            .collect(Collectors.toList());
    }

    /**
     * Get the facts representing the instructions within a particular method, in order of occurrence.
     * @param owner
     * @param methodName
     * @param descriptor
     * @return
     */
    List<Fact> getInstructionFacts(List<Fact> facts,String owner,String methodName,String descriptor) {
        String methodRef = FactExtractor.getMethodReference(owner, methodName, descriptor);
        return getInstructionFacts(facts,methodRef);
    }


    protected void containsJump(Fact fact, String label) {
        Predicate predicate = fact.predicate();
        for (int i = 0; i < predicate.getSlots().length; i++) {
            if (predicate.getSlots()[i].name().equals("label")) {
                assertEquals(label,fact.values()[i].toString());
                return;
            }
        }
        assertTrue(false);
    }

    protected boolean isJump(Fact fact) {
        Predicate predicate = fact.predicate();
        if (predicate instanceof EBDInstructionPredicate) {
            EBDInstructionPredicate instructionPredicate = (EBDInstructionPredicate) predicate;
            return instructionPredicate.getAsmNodeType().equals(JumpInsnNode.class.getName());
        }
        else {
            return false;
        }
    }
}
