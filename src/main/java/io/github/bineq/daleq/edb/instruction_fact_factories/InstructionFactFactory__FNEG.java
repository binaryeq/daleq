package io.github.bineq.daleq.edb.instruction_fact_factories;

import io.github.bineq.daleq.Fact;
import io.github.bineq.daleq.edb.FactExtractor;

import javax.annotation.processing.Generated;

@Generated(value="io.github.bineq.daleq.edb.InstructionFactFactoryCodeGenerator", date= "2025-07-13T21:15Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__FNEG implements io.github.bineq.daleq.edb.InstructionPredicateFactFactory<org.objectweb.asm.tree.InsnNode> {

    @Override public String getVersion() {
        return "280fabfc-c1e4-4910-a043-2d94477af929";
    }

    @Override public io.github.bineq.daleq.edb.EDBInstructionPredicate getPredicate() {
        return io.github.bineq.daleq.edb.EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(118);
    }

    @Override public Fact createFact(String factid, org.objectweb.asm.tree.InsnNode node, String methodRef, int instructionCounter, FactExtractor.LabelMap labelMap) {
        return new io.github.bineq.daleq.SimpleFact(io.github.bineq.daleq.edb.EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(118),new Object[]{factid,methodRef,instructionCounter});
    }

};
