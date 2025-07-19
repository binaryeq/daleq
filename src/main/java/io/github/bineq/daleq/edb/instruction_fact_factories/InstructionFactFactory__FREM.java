package io.github.bineq.daleq.edb.instruction_fact_factories;

import io.github.bineq.daleq.Fact;

import javax.annotation.processing.Generated;

@Generated(value="io.github.bineq.daleq.edb.InstructionFactFactoryCodeGenerator", date= "2025-07-19T00:01Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__FREM implements io.github.bineq.daleq.edb.InstructionPredicateFactFactory<org.objectweb.asm.tree.InsnNode> {

    @Override public String getVersion() {
        return "02f5e92e-38d7-40e2-9177-4f70c1b31752";
    }

    @Override public io.github.bineq.daleq.edb.EDBInstructionPredicate getPredicate() {
        return io.github.bineq.daleq.edb.EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(114);
    }

    @Override public Fact createFact(String factid,org.objectweb.asm.tree.InsnNode node,String methodRef,int instructionCounter,io.github.bineq.daleq.edb.FactExtractor.LabelMap labelMap) {
        return new io.github.bineq.daleq.SimpleFact(io.github.bineq.daleq.edb.EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(114),new Object[]{factid,methodRef,instructionCounter});
    }

};
