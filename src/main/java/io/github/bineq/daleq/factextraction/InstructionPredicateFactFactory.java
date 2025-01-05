package io.github.bineq.daleq.factextraction;

import org.objectweb.asm.tree.AbstractInsnNode;

import javax.annotation.processing.Generated;

/**
 * Interface for factories for facts from ASM tree API nodes.
 * @author jens dietrich
 */
@Generated({})
public interface InstructionPredicateFactFactory<NT extends AbstractInsnNode> {

    char SEP = '\t';

    // a unique version id, used to verify that the generator is consistent with the predicate spec (InstructionPredicate::id).
    String getVersion() ;

    InstructionPredicate getPredicate();

    String createFact(NT node, String methodRef,int instructionCounter);

}
