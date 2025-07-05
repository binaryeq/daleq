package io.github.bineq.daleq.edb.instruction_fact_factories;

import javax.annotation.processing.Generated;

import io.github.bineq.daleq.SimpleFact;
import io.github.bineq.daleq.edb.EDBInstructionPredicate;
import io.github.bineq.daleq.edb.EDBPredicateRegistry;
import io.github.bineq.daleq.edb.FactExtractor;
import io.github.bineq.daleq.Fact;
import org.objectweb.asm.tree.MethodInsnNode;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-02-13T10:40Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__INVOKEVIRTUAL implements io.github.bineq.daleq.edb.InstructionPredicateFactFactory<org.objectweb.asm.tree.MethodInsnNode> {

    @Override public String getVersion() {
        return "bee02d9c-4b76-4833-af95-f1e263ce3c44";
    }

    @Override public EDBInstructionPredicate getPredicate() {
        return EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(182);
    }

    @Override public Fact createFact(String factId, MethodInsnNode node, String methodRef, int instructionCounter, FactExtractor.LabelMap labelMap) {
        return new SimpleFact(EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(182),new Object[]{factId,methodRef,instructionCounter,node.owner,node.name,node.desc,node.itf});
    }

};
