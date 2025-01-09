package io.github.bineq.daleq.factextraction.instruction_fact_factories;

import javax.annotation.processing.Generated;
import io.github.bineq.daleq.factextraction.Fact;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-01-07T02:07Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__BIPUSH implements io.github.bineq.daleq.factextraction.InstructionPredicateFactFactory<org.objectweb.asm.tree.IntInsnNode> {

    @Override public String getVersion() {
        return "273be631-3b83-40d4-9cd4-201f7c55addd";
    }

    @Override public io.github.bineq.daleq.factextraction.InstructionPredicate getPredicate() {
        return io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(16);
    }

    @Override public Fact createFact(org.objectweb.asm.tree.IntInsnNode node,String methodRef,int instructionCounter) {
        return new io.github.bineq.daleq.factextraction.SimpleFact(io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(16),new Object[]{methodRef,instructionCounter,node.operand});
    }

};
