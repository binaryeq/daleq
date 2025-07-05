package io.github.bineq.daleq.edb.instruction_fact_factories;

import javax.annotation.processing.Generated;

import io.github.bineq.daleq.SimpleFact;
import io.github.bineq.daleq.edb.EDBInstructionPredicate;
import io.github.bineq.daleq.edb.EDBPredicateRegistry;
import io.github.bineq.daleq.edb.FactExtractor;
import io.github.bineq.daleq.Fact;
import org.objectweb.asm.tree.VarInsnNode;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-02-13T10:40Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__ISTORE implements io.github.bineq.daleq.edb.InstructionPredicateFactFactory<org.objectweb.asm.tree.VarInsnNode> {

    @Override public String getVersion() {
        return "64c7428d-a625-4271-b4d7-83fc70962062";
    }

    @Override public EDBInstructionPredicate getPredicate() {
        return EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(54);
    }

    @Override public Fact createFact(String factId, VarInsnNode node, String methodRef, int instructionCounter, FactExtractor.LabelMap labelMap) {
        return new SimpleFact(EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(54),new Object[]{factId,methodRef,instructionCounter,node.var});
    }

};
