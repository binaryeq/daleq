package io.github.bineq.daleq.factextraction.instruction_fact_factories;

import javax.annotation.processing.Generated;
import java.util.Map;
import org.objectweb.asm.tree.LabelNode;
import io.github.bineq.daleq.factextraction.Fact;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-02-13T10:40Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__IFNE implements io.github.bineq.daleq.factextraction.InstructionPredicateFactFactory<org.objectweb.asm.tree.JumpInsnNode> {

    @Override public String getVersion() {
        return "d5d2e4c4-0e79-4a7b-9693-0631ab1d7cf7";
    }

    @Override public io.github.bineq.daleq.factextraction.InstructionPredicate getPredicate() {
        return io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(154);
    }

    @Override public Fact createFact(String factId,org.objectweb.asm.tree.JumpInsnNode node,String methodRef,int instructionCounter,Map<LabelNode,Integer> labelMap) {
        return new io.github.bineq.daleq.factextraction.SimpleFact(io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(154),new Object[]{factId,methodRef,instructionCounter,labelMap.get(node.label)});
    }

};
