package io.github.bineq.daleq.factextraction.instruction_fact_factories;

import javax.annotation.processing.Generated;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-01-05T23:01Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__GETSTATIC implements io.github.bineq.daleq.factextraction.InstructionPredicateFactFactory<org.objectweb.asm.tree.FieldInsnNode> {

    @Override public String getVersion() {
        return "14a29458-20dd-42f0-919a-dc5fca45f76e";
    }

    @Override public io.github.bineq.daleq.factextraction.InstructionPredicate getPredicate() {
        return io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(178);
    }

    @Override public String createFact(org.objectweb.asm.tree.FieldInsnNode node,String methodRef,int instructionCounter) {
        return methodRef + '\t' + String.valueOf(instructionCounter) + '\t' + String.valueOf(node.owner) + '\t' + String.valueOf(node.name) + '\t' + String.valueOf(node.desc);
    }

};
