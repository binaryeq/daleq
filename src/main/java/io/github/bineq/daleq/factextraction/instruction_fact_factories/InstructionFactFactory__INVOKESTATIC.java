package io.github.bineq.daleq.factextraction.instruction_fact_factories;

import javax.annotation.processing.Generated;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-01-05T23:01Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__INVOKESTATIC implements io.github.bineq.daleq.factextraction.InstructionPredicateFactFactory<org.objectweb.asm.tree.MethodInsnNode> {

    @Override public String getVersion() {
        return "55abea99-d1e8-4821-bb5b-9bf877cef040";
    }

    @Override public io.github.bineq.daleq.factextraction.InstructionPredicate getPredicate() {
        return io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(184);
    }

    @Override public String createFact(org.objectweb.asm.tree.MethodInsnNode node,String methodRef,int instructionCounter) {
        return methodRef + '\t' + String.valueOf(instructionCounter) + '\t' + String.valueOf(node.owner) + '\t' + String.valueOf(node.name) + '\t' + String.valueOf(node.desc) + '\t' + String.valueOf(node.itf);
    }

};
