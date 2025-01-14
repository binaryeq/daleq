package io.github.bineq.daleq.factextraction.instruction_fact_factories;

import javax.annotation.processing.Generated;
import io.github.bineq.daleq.factextraction.Fact;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-01-14T02:05Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__IASTORE implements io.github.bineq.daleq.factextraction.InstructionPredicateFactFactory<org.objectweb.asm.tree.InsnNode> {

    @Override public String getVersion() {
        return "0c975835-de4a-4d1b-b524-12bf87e03d08";
    }

    @Override public io.github.bineq.daleq.factextraction.InstructionPredicate getPredicate() {
        return io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(79);
    }

    @Override public Fact createFact(org.objectweb.asm.tree.InsnNode node,String methodRef,int instructionCounter) {
        return new io.github.bineq.daleq.factextraction.SimpleFact(io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(79),new Object[]{methodRef,instructionCounter});
    }

};
