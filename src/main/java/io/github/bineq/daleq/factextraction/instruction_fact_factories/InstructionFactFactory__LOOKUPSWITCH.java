package io.github.bineq.daleq.factextraction.instruction_fact_factories;

import javax.annotation.processing.Generated;
import java.util.Map;
import org.objectweb.asm.tree.LabelNode;
import io.github.bineq.daleq.factextraction.Fact;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-02-13T10:40Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__LOOKUPSWITCH implements io.github.bineq.daleq.factextraction.InstructionPredicateFactFactory<org.objectweb.asm.tree.LookupSwitchInsnNode> {

    @Override public String getVersion() {
        return "cf81047e-c7fb-48b8-94b7-ff250a4791e8";
    }

    @Override public io.github.bineq.daleq.factextraction.InstructionPredicate getPredicate() {
        return io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(171);
    }

    @Override public Fact createFact(String factId,org.objectweb.asm.tree.LookupSwitchInsnNode node,String methodRef,int instructionCounter,Map<LabelNode,Integer> labelMap) {
        return new io.github.bineq.daleq.factextraction.SimpleFact(io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(171),new Object[]{factId,methodRef,instructionCounter,labelMap.get(node.dflt),node.keys,node.labels});
    }

};
