package io.github.bineq.daleq.edb.instruction_fact_factories;

import javax.annotation.processing.Generated;
import java.util.Map;

import io.github.bineq.daleq.SimpleFact;
import io.github.bineq.daleq.edb.EBDInstructionPredicate;
import io.github.bineq.daleq.edb.EDBPredicateRegistry;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LabelNode;
import io.github.bineq.daleq.Fact;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-02-13T10:40Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__NEWARRAY implements io.github.bineq.daleq.edb.InstructionPredicateFactFactory<org.objectweb.asm.tree.IntInsnNode> {

    @Override public String getVersion() {
        return "96cb62fe-750c-4c6d-a44c-579cc96ead01";
    }

    @Override public EBDInstructionPredicate getPredicate() {
        return EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(188);
    }

    @Override public Fact createFact(String factId, IntInsnNode node, String methodRef, int instructionCounter, Map<LabelNode, String> labelMap) {
        return new SimpleFact(EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(188),new Object[]{factId,methodRef,instructionCounter,node.operand});
    }

};
