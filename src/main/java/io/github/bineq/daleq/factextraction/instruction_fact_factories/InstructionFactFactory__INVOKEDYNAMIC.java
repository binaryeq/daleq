package io.github.bineq.daleq.factextraction.instruction_fact_factories;

import javax.annotation.processing.Generated;
import java.util.Map;
import org.objectweb.asm.tree.LabelNode;
import io.github.bineq.daleq.factextraction.Fact;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-02-13T10:40Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__INVOKEDYNAMIC implements io.github.bineq.daleq.factextraction.InstructionPredicateFactFactory<org.objectweb.asm.tree.InvokeDynamicInsnNode> {

    @Override public String getVersion() {
        return "1f1af445-7633-49a6-b910-b03542d41ff5";
    }

    @Override public io.github.bineq.daleq.factextraction.InstructionPredicate getPredicate() {
        return io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(186);
    }

    @Override public Fact createFact(String factId,org.objectweb.asm.tree.InvokeDynamicInsnNode node,String methodRef,int instructionCounter,Map<LabelNode,Integer> labelMap) {
        return new io.github.bineq.daleq.factextraction.SimpleFact(io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(186),new Object[]{factId,methodRef,instructionCounter,node.name,node.desc,node.bsmArgs,node.bsm.getOwner(),node.bsm.getName(),node.bsm.getDesc(),node.bsm.getTag(),node.bsm.isInterface()});
    }

};
