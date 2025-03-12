package io.github.bineq.daleq.edb.instruction_fact_factories;

import javax.annotation.processing.Generated;
import java.util.Map;

import io.github.bineq.daleq.SimpleFact;
import io.github.bineq.daleq.edb.EBDInstructionPredicate;
import io.github.bineq.daleq.edb.EDBPredicateRegistry;
import org.objectweb.asm.tree.LabelNode;
import io.github.bineq.daleq.Fact;
import org.objectweb.asm.tree.VarInsnNode;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-02-13T10:40Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__ALOAD implements io.github.bineq.daleq.edb.InstructionPredicateFactFactory<org.objectweb.asm.tree.VarInsnNode> {

    @Override public String getVersion() {
        return "d3d2b524-971e-46f4-bd50-788e0276e020";
    }

    @Override public EBDInstructionPredicate getPredicate() {
        return EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(25);
    }

    @Override public Fact createFact(String factId, VarInsnNode node, String methodRef, int instructionCounter, Map<LabelNode, String> labelMap) {
        return new SimpleFact(EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(25),new Object[]{factId,methodRef,instructionCounter,node.var});
    }

};
