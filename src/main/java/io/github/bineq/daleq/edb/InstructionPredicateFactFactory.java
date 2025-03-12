package io.github.bineq.daleq.edb;

import io.github.bineq.daleq.Fact;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;

import javax.annotation.processing.Generated;
import java.util.Map;
import java.util.Objects;

/**
 * Interface for factories for facts from ASM tree API nodes.
 * @author jens dietrich
 */
@Generated({})
public interface InstructionPredicateFactFactory<NT extends AbstractInsnNode> {

    char SEP = '\t';

    // a unique version id, used to verify that the generator is consistent with the predicate spec (InstructionPredicate::id).
    String getVersion() ;

    EBDInstructionPredicate getPredicate();

    Fact createFact(String id, NT node, String methodRef, int instructionCounter, Map<LabelNode, String> labelMap);

    default void verify() throws VerificationException {
        if (!Objects.equals(getVersion(), getPredicate().getId())) {
            throw new VerificationException("Predicate id does not match factory id/version -- factory must be regenerated: " + getVersion() + " does not match " + getPredicate().getId());
        }
    }

}
