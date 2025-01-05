package io.github.bineq.daleq.factextraction.instruction_fact_factories;

import javax.annotation.processing.Generated;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-01-05T23:01Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__RETURN implements io.github.bineq.daleq.factextraction.InstructionPredicateFactFactory<org.objectweb.asm.tree.InsnNode> {

    @Override public String getVersion() {
        return "7e30c472-5f9b-4d2f-b561-b10d5ba5285e";
    }

    @Override public io.github.bineq.daleq.factextraction.InstructionPredicate getPredicate() {
        return io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(177);
    }

    @Override public String createFact(org.objectweb.asm.tree.InsnNode node,String methodRef,int instructionCounter) {
        return methodRef + '\t' + String.valueOf(instructionCounter);
    }

};
