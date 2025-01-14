package io.github.bineq.daleq.factextraction.instruction_fact_factories;

import javax.annotation.processing.Generated;
import io.github.bineq.daleq.factextraction.Fact;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-01-14T02:05Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__LOOKUPSWITCH implements io.github.bineq.daleq.factextraction.InstructionPredicateFactFactory<org.objectweb.asm.tree.LookupSwitchInsnNode> {

    @Override public String getVersion() {
        return "cf81047e-c7fb-48b8-94b7-ff250a4791e8";
    }

    @Override public io.github.bineq.daleq.factextraction.InstructionPredicate getPredicate() {
        return io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(171);
    }

    @Override public Fact createFact(org.objectweb.asm.tree.LookupSwitchInsnNode node,String methodRef,int instructionCounter) {
        return new io.github.bineq.daleq.factextraction.SimpleFact(io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(171),new Object[]{methodRef,instructionCounter,node.dflt,node.keys,node.labels});
    }

};
