package io.github.bineq.daleq.factextraction.instruction_fact_factories;

import javax.annotation.processing.Generated;
import java.util.Map;
import org.objectweb.asm.tree.LabelNode;
import io.github.bineq.daleq.factextraction.Fact;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-01-15T01:04Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__ANEWARRAY implements io.github.bineq.daleq.factextraction.InstructionPredicateFactFactory<org.objectweb.asm.tree.TypeInsnNode> {

    @Override public String getVersion() {
        return "306338a4-7b1f-4bba-b18a-e4f2a422120b";
    }

    @Override public io.github.bineq.daleq.factextraction.InstructionPredicate getPredicate() {
        return io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(189);
    }

    @Override public Fact createFact(org.objectweb.asm.tree.TypeInsnNode node,String methodRef,int instructionCounter,Map<LabelNode,Integer> labelMap) {
        return new io.github.bineq.daleq.factextraction.SimpleFact(io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(189),new Object[]{methodRef,instructionCounter,node.desc});
    }

};
