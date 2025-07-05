package io.github.bineq.daleq.edb.instruction_fact_factories;

import javax.annotation.processing.Generated;

import io.github.bineq.daleq.SimpleFact;
import io.github.bineq.daleq.edb.EDBInstructionPredicate;
import io.github.bineq.daleq.edb.EDBPredicateRegistry;
import io.github.bineq.daleq.edb.FactExtractor;
import org.objectweb.asm.tree.FieldInsnNode;
import io.github.bineq.daleq.Fact;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-02-13T10:40Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__PUTFIELD implements io.github.bineq.daleq.edb.InstructionPredicateFactFactory<org.objectweb.asm.tree.FieldInsnNode> {

    @Override public String getVersion() {
        return "1614e5c3-867c-4d6d-917e-8a3a82f75bd4";
    }

    @Override public EDBInstructionPredicate getPredicate() {
        return EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(181);
    }

    @Override public Fact createFact(String factId, FieldInsnNode node, String methodRef, int instructionCounter, FactExtractor.LabelMap labelMap) {
        return new SimpleFact(EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(181),new Object[]{factId,methodRef,instructionCounter,node.owner,node.name,node.desc});
    }

};
