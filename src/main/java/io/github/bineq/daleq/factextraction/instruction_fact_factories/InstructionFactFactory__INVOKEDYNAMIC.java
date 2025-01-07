package io.github.bineq.daleq.factextraction.instruction_fact_factories;

import javax.annotation.processing.Generated;
import io.github.bineq.daleq.factextraction.Fact;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-01-07T21:36Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__INVOKEDYNAMIC implements io.github.bineq.daleq.factextraction.InstructionPredicateFactFactory<org.objectweb.asm.tree.InvokeDynamicInsnNode> {

    @Override public String getVersion() {
        return "1f1af445-7633-49a6-b910-b03542d41ff5";
    }

    @Override public io.github.bineq.daleq.factextraction.InstructionPredicate getPredicate() {
        return io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(186);
    }

    @Override public Fact createFact(org.objectweb.asm.tree.InvokeDynamicInsnNode node,String methodRef,int instructionCounter) {
        return new io.github.bineq.daleq.factextraction.SimpleFact(io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(186),new Object[]{methodRef,instructionCounter,node.name,node.desc,node.bsmArgs,node.bsm.getOwner(),node.bsm.getName(),node.bsm.getDesc(),node.bsm.getTag(),node.bsm.isInterface()});
    }

};
