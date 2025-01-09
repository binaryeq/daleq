package io.github.bineq.daleq.factextraction.instruction_fact_factories;

import javax.annotation.processing.Generated;
import io.github.bineq.daleq.factextraction.Fact;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-01-07T02:07Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__GETFIELD implements io.github.bineq.daleq.factextraction.InstructionPredicateFactFactory<org.objectweb.asm.tree.FieldInsnNode> {

    @Override public String getVersion() {
        return "2483b888-64f1-476d-9976-50553cfd3082";
    }

    @Override public io.github.bineq.daleq.factextraction.InstructionPredicate getPredicate() {
        return io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(180);
    }

    @Override public Fact createFact(org.objectweb.asm.tree.FieldInsnNode node,String methodRef,int instructionCounter) {
        return new io.github.bineq.daleq.factextraction.SimpleFact(io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(180),new Object[]{methodRef,instructionCounter,node.owner,node.name,node.desc});
    }

};
