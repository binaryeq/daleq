package io.github.bineq.daleq.edb.instruction_fact_factories;

import javax.annotation.processing.Generated;
import java.util.Map;
import org.objectweb.asm.tree.LabelNode;
import io.github.bineq.daleq.Fact;

@Generated(value="io.github.bineq.daleq.edb.InstructionFactFactoryCodeGenerator", date= "2025-04-28T23:19Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__IFGT implements io.github.bineq.daleq.edb.InstructionPredicateFactFactory<org.objectweb.asm.tree.JumpInsnNode> {

    @Override public String getVersion() {
        return "4cbe00a2-a6b0-41c5-bb40-d2879c68de2b";
    }

    @Override public io.github.bineq.daleq.edb.EBDInstructionPredicate getPredicate() {
        return io.github.bineq.daleq.edb.EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(157);
    }

    @Override public Fact createFact(String factid,org.objectweb.asm.tree.JumpInsnNode node,String methodRef,int instructionCounter,Map<LabelNode,String> labelMap) {
        return new io.github.bineq.daleq.SimpleFact(io.github.bineq.daleq.edb.EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(157),new Object[]{factid,methodRef,instructionCounter,labelMap.get(node.label)});
    }

};
