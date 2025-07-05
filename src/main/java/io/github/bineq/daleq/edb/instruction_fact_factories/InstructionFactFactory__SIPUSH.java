package io.github.bineq.daleq.edb.instruction_fact_factories;

import javax.annotation.processing.Generated;

import io.github.bineq.daleq.SimpleFact;
import io.github.bineq.daleq.edb.EDBInstructionPredicate;
import io.github.bineq.daleq.edb.EDBPredicateRegistry;
import io.github.bineq.daleq.edb.FactExtractor;
import org.objectweb.asm.tree.IntInsnNode;
import io.github.bineq.daleq.Fact;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-02-13T10:40Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__SIPUSH implements io.github.bineq.daleq.edb.InstructionPredicateFactFactory<org.objectweb.asm.tree.IntInsnNode> {

    @Override public String getVersion() {
        return "7d02c1d9-1b44-4480-b3be-7aeda11017aa";
    }

    @Override public EDBInstructionPredicate getPredicate() {
        return EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(17);
    }

    @Override public Fact createFact(String factId, IntInsnNode node, String methodRef, int instructionCounter, FactExtractor.LabelMap labelMap) {
        return new SimpleFact(EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(17),new Object[]{factId,methodRef,instructionCounter,node.operand});
    }

};
