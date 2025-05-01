package io.github.bineq.daleq.edb.instruction_fact_factories;

import javax.annotation.processing.Generated;
import java.util.Map;
import org.objectweb.asm.tree.LabelNode;
import io.github.bineq.daleq.Fact;

@Generated(value="io.github.bineq.daleq.edb.InstructionFactFactoryCodeGenerator", date= "2025-04-28T23:19Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__DASTORE implements io.github.bineq.daleq.edb.InstructionPredicateFactFactory<org.objectweb.asm.tree.InsnNode> {

    @Override public String getVersion() {
        return "4e3f510a-6cae-4d3f-a867-84825b9b1c0b";
    }

    @Override public io.github.bineq.daleq.edb.EBDInstructionPredicate getPredicate() {
        return io.github.bineq.daleq.edb.EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(82);
    }

    @Override public Fact createFact(String factid,org.objectweb.asm.tree.InsnNode node,String methodRef,int instructionCounter,Map<LabelNode,String> labelMap) {
        return new io.github.bineq.daleq.SimpleFact(io.github.bineq.daleq.edb.EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(82),new Object[]{factid,methodRef,instructionCounter});
    }

};
