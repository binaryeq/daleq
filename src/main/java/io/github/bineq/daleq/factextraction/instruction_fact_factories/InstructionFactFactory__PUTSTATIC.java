package io.github.bineq.daleq.factextraction.instruction_fact_factories;

import javax.annotation.processing.Generated;
import java.util.Map;
import org.objectweb.asm.tree.LabelNode;
import io.github.bineq.daleq.factextraction.Fact;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-02-13T10:40Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__PUTSTATIC implements io.github.bineq.daleq.factextraction.InstructionPredicateFactFactory<org.objectweb.asm.tree.FieldInsnNode> {

    @Override public String getVersion() {
        return "87f968d0-4610-44f7-b2d4-8d862d58c8bd";
    }

    @Override public io.github.bineq.daleq.factextraction.InstructionPredicate getPredicate() {
        return io.github.bineq.daleq.factextraction.PredicateRegistry.INSTRUCTION_PREDICATES.get(179);
    }

    @Override public Fact createFact(String factId,org.objectweb.asm.tree.FieldInsnNode node,String methodRef,int instructionCounter,Map<LabelNode,Integer> labelMap) {
        return new io.github.bineq.daleq.factextraction.SimpleFact(io.github.bineq.daleq.factextraction.PredicateRegistry.INSTRUCTION_PREDICATES.get(179),new Object[]{factId,methodRef,instructionCounter,node.owner,node.name,node.desc});
    }

};
