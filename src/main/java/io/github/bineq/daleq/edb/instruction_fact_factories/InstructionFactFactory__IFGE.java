package io.github.bineq.daleq.edb.instruction_fact_factories;

import javax.annotation.processing.Generated;

import io.github.bineq.daleq.edb.EDBInstructionPredicate;
import io.github.bineq.daleq.edb.FactExtractor;
import org.objectweb.asm.tree.JumpInsnNode;
import io.github.bineq.daleq.Fact;

@Generated(value="io.github.bineq.daleq.edb.InstructionFactFactoryCodeGenerator", date= "2025-04-28T23:19Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__IFGE implements io.github.bineq.daleq.edb.InstructionPredicateFactFactory<org.objectweb.asm.tree.JumpInsnNode> {

    @Override public String getVersion() {
        return "ecaf6fe3-dd97-4d6d-bce3-ab31927bae5d";
    }

    @Override public EDBInstructionPredicate getPredicate() {
        return io.github.bineq.daleq.edb.EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(156);
    }

    @Override public Fact createFact(String factid, JumpInsnNode node, String methodRef, int instructionCounter, FactExtractor.LabelMap labelMap) {
        return new io.github.bineq.daleq.SimpleFact(io.github.bineq.daleq.edb.EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(156),new Object[]{factid,methodRef,instructionCounter,labelMap.get(node.label)});
    }

};
