package io.github.bineq.daleq.factextraction.instruction_fact_factories;

import javax.annotation.processing.Generated;
import java.util.Map;
import org.objectweb.asm.tree.LabelNode;
import io.github.bineq.daleq.factextraction.Fact;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-02-13T10:40Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__NEW implements io.github.bineq.daleq.factextraction.InstructionPredicateFactFactory<org.objectweb.asm.tree.TypeInsnNode> {

    @Override public String getVersion() {
        return "baaa476c-4e7a-4d88-8e9b-c6a4fc4f7a0a";
    }

    @Override public io.github.bineq.daleq.factextraction.InstructionPredicate getPredicate() {
        return io.github.bineq.daleq.factextraction.PredicateRegistry.INSTRUCTION_PREDICATES.get(187);
    }

    @Override public Fact createFact(String factId,org.objectweb.asm.tree.TypeInsnNode node,String methodRef,int instructionCounter,Map<LabelNode,Integer> labelMap) {
<<<<<<< HEAD
        return new io.github.bineq.daleq.factextraction.SimpleFact(io.github.bineq.daleq.factextraction.PredicateRegistry.INSTRUCTION_PREDICATES.get(187),new Object[]{factId,methodRef,instructionCounter,node.desc});
=======
        return new io.github.bineq.daleq.factextraction.SimpleFact(io.github.bineq.daleq.factextraction.FactExtractor.REGISTRY.get(187),new Object[]{factId,methodRef,instructionCounter,node.desc});
>>>>>>> 53b61a67ecfe151d2d821f2a207ffaee6b9d2d98
    }

};
