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
public class InstructionFactFactory__ICONST_4 implements io.github.bineq.daleq.edb.InstructionPredicateFactFactory<org.objectweb.asm.tree.InsnNode> {

    @Override public String getVersion() {
        return "76bb5efa-83ea-4841-b0af-edf94cdd49b8";
    }

    @Override public EBDInstructionPredicate getPredicate() {
        return EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(7);
    }

    @Override public Fact createFact(String factId, InsnNode node, String methodRef, int instructionCounter, Map<LabelNode, String> labelMap) {
        return new SimpleFact(EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(7),new Object[]{factId,methodRef,instructionCounter});
    }

};
