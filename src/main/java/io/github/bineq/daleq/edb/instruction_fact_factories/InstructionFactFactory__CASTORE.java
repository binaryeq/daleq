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
public class InstructionFactFactory__CASTORE implements io.github.bineq.daleq.edb.InstructionPredicateFactFactory<org.objectweb.asm.tree.InsnNode> {

    @Override public String getVersion() {
        return "2197e7a2-a675-4ca9-a073-880a45762383";
    }

    @Override public EBDInstructionPredicate getPredicate() {
        return EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(85);
    }

    @Override public Fact createFact(String factId, InsnNode node, String methodRef, int instructionCounter, Map<LabelNode, String> labelMap) {
        return new SimpleFact(EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(85),new Object[]{factId,methodRef,instructionCounter});
    }

};
