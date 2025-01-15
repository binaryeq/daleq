package io.github.bineq.daleq.factextraction;

import io.github.bineq.daleq.factextraction.javap.JavapModelTest;
import org.junit.jupiter.api.BeforeEach;
import org.objectweb.asm.tree.JumpInsnNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Abstract fact abstraction test.
 * @author jens dietrich
 */
public abstract class AbstractFactExtractionTest {

    protected byte[] byteCode;
    protected List<Fact> facts = new ArrayList<>();

    protected abstract String getTestClass();

    @BeforeEach
    public void setup() throws Exception {
        Path classFile = Path.of(JavapModelTest.class.getResource(getTestClass()).getFile());
        byteCode = Files.readAllBytes(classFile);
        facts = FactExtractor.extract(byteCode,false); // verification will be done in dedicated test
    }

    protected Fact getFirstFact(AdditionalPredicates predicate) {
        return facts.stream()
            .filter(fact -> fact.predicate().equals(predicate))
            .findFirst()
            .orElseThrow();
    }

    protected List<Fact> getFacts(AdditionalPredicates predicate) {
        return facts.stream()
            .filter(fact -> fact.predicate().equals(predicate))
            .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Get the facts representing the instructions within a particular method, in order of occurrence.
     * @param methodRef
     * @return
     */
    List<Fact> getInstructionFacts(String methodRef) {
        return facts.stream()
            .filter(fact -> fact.predicate().isInstructionPredicate())
            .filter(fact -> fact.values()[0].equals(methodRef))
            .sorted(Comparator.comparing(fact -> Integer.parseInt(fact.values()[1].toString())))
            .collect(Collectors.toList());
    }

    /**
     * Get the facts representing the instructions within a particular method, in order of occurrence.
     * @param owner
     * @param methodName
     * @param descriptor
     * @return
     */
    List<Fact> getInstructionFacts(String owner,String methodName,String descriptor) {
        String methodRef = FactExtractor.getMethodReference(owner, methodName, descriptor);
        return getInstructionFacts(methodRef);
    }


    protected void containsJump(Collection<Fact> facts, int from, int to) {
        int count = (int)facts.stream()
            .filter(f -> f.values()[1].equals(from))
            .filter(f -> f.values()[2].equals(to))
            .count();
        assertEquals(1,count);
    }

    protected boolean isJump(Fact fact) {
        Predicate predicate = fact.predicate();
        if (predicate instanceof InstructionPredicate) {
            InstructionPredicate instructionPredicate = (InstructionPredicate) predicate;
            return instructionPredicate.getAsmNodeType().equals(JumpInsnNode.class.getName());
        }
        else {
            return false;
        }
    }
}
