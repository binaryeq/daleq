package io.github.bineq.daleq.edb.instruction_fact_factories;

import javax.annotation.processing.Generated;

import io.github.bineq.daleq.SimpleFact;
import io.github.bineq.daleq.edb.EDBInstructionPredicate;
import io.github.bineq.daleq.edb.EDBPredicateRegistry;
import io.github.bineq.daleq.edb.FactExtractor;
import org.objectweb.asm.tree.InsnNode;
import io.github.bineq.daleq.Fact;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-02-13T10:40Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__I2C implements io.github.bineq.daleq.edb.InstructionPredicateFactFactory<org.objectweb.asm.tree.InsnNode> {

    @Override public String getVersion() {
        return "cb77a5e2-5155-483c-8d08-d0dbaf62e72c";
    }

    @Override public EDBInstructionPredicate getPredicate() {
        return EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(146);
    }

    @Override public Fact createFact(String factId, InsnNode node, String methodRef, int instructionCounter, FactExtractor.LabelMap labelMap) {
        return new SimpleFact(EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(146),new Object[]{factId,methodRef,instructionCounter});
    }

};
