package io.github.bineq.daleq.factextraction.instruction_fact_factories;

import javax.annotation.processing.Generated;
import java.util.Map;
import org.objectweb.asm.tree.LabelNode;
import io.github.bineq.daleq.factextraction.Fact;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-02-13T10:40Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__BIPUSH implements io.github.bineq.daleq.factextraction.InstructionPredicateFactFactory<org.objectweb.asm.tree.IntInsnNode> {

    @Override public String getVersion() {
        return "273be631-3b83-40d4-9cd4-201f7c55addd";
    }

    @Override public io.github.bineq.daleq.factextraction.InstructionPredicate getPredicate() {
        return io.github.bineq.daleq.factextraction.PredicateRegistry.INSTRUCTION_PREDICATES.get(16);
    }

    @Override public Fact createFact(String factId,org.objectweb.asm.tree.IntInsnNode node,String methodRef,int instructionCounter,Map<LabelNode,Integer> labelMap) {
<<<<<<< HEAD
        return new io.github.bineq.daleq.factextraction.SimpleFact(io.github.bineq.daleq.factextraction.PredicateRegistry.INSTRUCTION_PREDICATES.get(16),new Object[]{factId,methodRef,instructionCounter,node.operand});
=======
        return new io.github.bineq.daleq.factextraction.SimpleFact(io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(16),new Object[]{factId,methodRef,instructionCounter,node.operand});
>>>>>>> 53b61a67ecfe151d2d821f2a207ffaee6b9d2d98
    }

};
