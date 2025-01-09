package io.github.bineq.daleq.factextraction.instruction_fact_factories;

import javax.annotation.processing.Generated;
import io.github.bineq.daleq.factextraction.Fact;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-01-07T02:07Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__LDC implements io.github.bineq.daleq.factextraction.InstructionPredicateFactFactory<org.objectweb.asm.tree.LdcInsnNode> {

    @Override public String getVersion() {
        return "2faab171-669b-4459-b712-66f3ddec922b";
    }

    @Override public io.github.bineq.daleq.factextraction.InstructionPredicate getPredicate() {
        return io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(18);
    }

    @Override public Fact createFact(org.objectweb.asm.tree.LdcInsnNode node,String methodRef,int instructionCounter) {
        return new io.github.bineq.daleq.factextraction.SimpleFact(io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(18),new Object[]{methodRef,instructionCounter,node.cst});
    }

};
