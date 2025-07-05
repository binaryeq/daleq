package io.github.bineq.daleq.edb.instruction_fact_factories;

import javax.annotation.processing.Generated;

import io.github.bineq.daleq.SimpleFact;
import io.github.bineq.daleq.edb.EDBInstructionPredicate;
import io.github.bineq.daleq.edb.EDBPredicateRegistry;
import io.github.bineq.daleq.edb.FactExtractor;
import io.github.bineq.daleq.Fact;
import org.objectweb.asm.tree.TypeInsnNode;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-02-13T10:40Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__ANEWARRAY implements io.github.bineq.daleq.edb.InstructionPredicateFactFactory<org.objectweb.asm.tree.TypeInsnNode> {

    @Override public String getVersion() {
        return "306338a4-7b1f-4bba-b18a-e4f2a422120b";
    }

    @Override public EDBInstructionPredicate getPredicate() {
        return EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(189);
    }

    @Override public Fact createFact(String factId, TypeInsnNode node, String methodRef, int instructionCounter, FactExtractor.LabelMap labelMap) {
        return new SimpleFact(EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(189),new Object[]{factId,methodRef,instructionCounter,node.desc});
    }

};
