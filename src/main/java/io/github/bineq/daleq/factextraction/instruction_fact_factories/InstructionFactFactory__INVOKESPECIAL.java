package io.github.bineq.daleq.factextraction.instruction_fact_factories;

import javax.annotation.processing.Generated;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-01-05T23:01Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__INVOKESPECIAL implements io.github.bineq.daleq.factextraction.InstructionPredicateFactFactory<org.objectweb.asm.tree.MethodInsnNode> {

    @Override public String getVersion() {
        return "9554a36b-ada5-43e7-b411-43d623738a2f";
    }

    @Override public io.github.bineq.daleq.factextraction.InstructionPredicate getPredicate() {
        return io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(183);
    }

    @Override public String createFact(org.objectweb.asm.tree.MethodInsnNode node,String methodRef,int instructionCounter) {
        return methodRef + '\t' + String.valueOf(instructionCounter) + '\t' + String.valueOf(node.owner) + '\t' + String.valueOf(node.name) + '\t' + String.valueOf(node.desc) + '\t' + String.valueOf(node.itf);
    }

};
