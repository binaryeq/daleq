package io.github.bineq.daleq.factextraction.instruction_fact_factories;

import javax.annotation.processing.Generated;
import io.github.bineq.daleq.factextraction.Fact;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-01-14T02:05Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__TABLESWITCH implements io.github.bineq.daleq.factextraction.InstructionPredicateFactFactory<org.objectweb.asm.tree.TableSwitchInsnNode> {

    @Override public String getVersion() {
        return "34b7f7e7-c408-463f-9673-6be96d9291ff";
    }

    @Override public io.github.bineq.daleq.factextraction.InstructionPredicate getPredicate() {
        return io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(170);
    }

    @Override public Fact createFact(org.objectweb.asm.tree.TableSwitchInsnNode node,String methodRef,int instructionCounter) {
        return new io.github.bineq.daleq.factextraction.SimpleFact(io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(170),new Object[]{methodRef,instructionCounter,node.dflt,node.labels,node.max,node.min});
    }

};
