package io.github.bineq.daleq.edb.instruction_fact_factories;

import javax.annotation.processing.Generated;
import java.util.Map;

import io.github.bineq.daleq.SimpleFact;
import io.github.bineq.daleq.edb.EBDInstructionPredicate;
import io.github.bineq.daleq.edb.EDBPredicateRegistry;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import io.github.bineq.daleq.Fact;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-02-13T10:40Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__IREM implements io.github.bineq.daleq.edb.InstructionPredicateFactFactory<org.objectweb.asm.tree.InsnNode> {

    @Override public String getVersion() {
        return "a084c2cd-1b53-4957-ba31-dacc578a9177";
    }

    @Override public EBDInstructionPredicate getPredicate() {
        return EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(112);
    }

    @Override public Fact createFact(String factId, InsnNode node, String methodRef, int instructionCounter, Map<LabelNode, String> labelMap) {
        return new SimpleFact(EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(112),new Object[]{factId,methodRef,instructionCounter});
    }

};
