package io.github.bineq.daleq.factextraction.instruction_fact_factories;

import javax.annotation.processing.Generated;
import io.github.bineq.daleq.factextraction.Fact;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-01-07T02:07Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__IINC implements io.github.bineq.daleq.factextraction.InstructionPredicateFactFactory<org.objectweb.asm.tree.IincInsnNode> {

    @Override public String getVersion() {
        return "0e3d75db-d1c4-4094-8510-3cf1167a8ad2";
    }

    @Override public io.github.bineq.daleq.factextraction.InstructionPredicate getPredicate() {
        return io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(132);
    }

    @Override public Fact createFact(org.objectweb.asm.tree.IincInsnNode node,String methodRef,int instructionCounter) {
        return new io.github.bineq.daleq.factextraction.SimpleFact(io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(132),new Object[]{methodRef,instructionCounter,node.incr,node.var});
    }

};
