package io.github.bineq.daleq.edb.instruction_fact_factories;

import javax.annotation.processing.Generated;

import io.github.bineq.daleq.SimpleFact;
import io.github.bineq.daleq.edb.EBDInstructionPredicate;
import io.github.bineq.daleq.edb.EDBPredicateRegistry;
import io.github.bineq.daleq.edb.FactExtractor;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import io.github.bineq.daleq.Fact;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-02-13T10:40Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__INVOKEDYNAMIC implements io.github.bineq.daleq.edb.InstructionPredicateFactFactory<org.objectweb.asm.tree.InvokeDynamicInsnNode> {

    @Override public String getVersion() {
        return "1f1af445-7633-49a6-b910-b03542d41ff5";
    }

    @Override public EBDInstructionPredicate getPredicate() {
        return EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(186);
    }

    @Override public Fact createFact(String factId, InvokeDynamicInsnNode node, String methodRef, int instructionCounter, FactExtractor.LabelMap labelMap) {
        return new SimpleFact(EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(186),new Object[]{factId,methodRef,instructionCounter,node.name,node.desc,encodeBsmArgs(node.bsmArgs),node.bsm.getOwner(),node.bsm.getName(),node.bsm.getDesc(),node.bsm.getTag(),node.bsm.isInterface()});
    }

    private Object[] encodeBsmArgs(Object[] args) {
        String[] encoded = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            encoded[i] = String.valueOf(args[i]);
        }
        return encoded;
    }

};
