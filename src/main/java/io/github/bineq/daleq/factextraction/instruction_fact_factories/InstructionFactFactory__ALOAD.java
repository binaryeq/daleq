package io.github.bineq.daleq.factextraction.instruction_fact_factories;

import javax.annotation.processing.Generated;
import io.github.bineq.daleq.factextraction.Fact;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-01-07T02:07Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__ALOAD implements io.github.bineq.daleq.factextraction.InstructionPredicateFactFactory<org.objectweb.asm.tree.VarInsnNode> {

    @Override public String getVersion() {
        return "d3d2b524-971e-46f4-bd50-788e0276e020";
    }

    @Override public io.github.bineq.daleq.factextraction.InstructionPredicate getPredicate() {
        return io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(25);
    }

    @Override public Fact createFact(org.objectweb.asm.tree.VarInsnNode node,String methodRef,int instructionCounter) {
        return new io.github.bineq.daleq.factextraction.SimpleFact(io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(25),new Object[]{methodRef,instructionCounter,node.var});
    }

};
