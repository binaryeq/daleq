package io.github.bineq.daleq.edb.instruction_fact_factories;

import io.github.bineq.daleq.Fact;
import io.github.bineq.daleq.edb.FactExtractor;

import javax.annotation.processing.Generated;

@Generated(value="io.github.bineq.daleq.edb.InstructionFactFactoryCodeGenerator", date= "2025-07-13T21:15Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__FCONST_2 implements io.github.bineq.daleq.edb.InstructionPredicateFactFactory<org.objectweb.asm.tree.InsnNode> {

    @Override public String getVersion() {
        return "621b768e-afd6-4926-a621-159699becb66";
    }

    @Override public io.github.bineq.daleq.edb.EDBInstructionPredicate getPredicate() {
        return io.github.bineq.daleq.edb.EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(13);
    }

    @Override public Fact createFact(String factid, org.objectweb.asm.tree.InsnNode node, String methodRef, int instructionCounter, FactExtractor.LabelMap labelMap) {
        return new io.github.bineq.daleq.SimpleFact(io.github.bineq.daleq.edb.EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(13),new Object[]{factid,methodRef,instructionCounter});
    }

};
