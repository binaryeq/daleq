package io.github.bineq.daleq.edb.instruction_fact_factories;

import javax.annotation.processing.Generated;
import java.util.Map;
import org.objectweb.asm.tree.LabelNode;
import io.github.bineq.daleq.edb.Fact;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-02-13T10:40Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__ILOAD implements io.github.bineq.daleq.edb.InstructionPredicateFactFactory<org.objectweb.asm.tree.VarInsnNode> {

    @Override public String getVersion() {
        return "5f3298a4-bd27-4981-9e51-9db70a187268";
    }

    @Override public io.github.bineq.daleq.edb.InstructionPredicate getPredicate() {
        return io.github.bineq.daleq.edb.PredicateRegistry.INSTRUCTION_PREDICATES.get(21);
    }

    @Override public Fact createFact(String factId,org.objectweb.asm.tree.VarInsnNode node,String methodRef,int instructionCounter,Map<LabelNode,Integer> labelMap) {
        return new io.github.bineq.daleq.edb.SimpleFact(io.github.bineq.daleq.edb.PredicateRegistry.INSTRUCTION_PREDICATES.get(21),new Object[]{factId,methodRef,instructionCounter,node.var});
    }

};
