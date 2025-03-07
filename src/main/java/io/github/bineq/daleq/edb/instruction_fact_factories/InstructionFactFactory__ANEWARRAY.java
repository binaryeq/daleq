package io.github.bineq.daleq.edb.instruction_fact_factories;

import javax.annotation.processing.Generated;
import java.util.Map;

import io.github.bineq.daleq.SimpleFact;
import io.github.bineq.daleq.edb.EBDInstructionPredicate;
import io.github.bineq.daleq.edb.EDBPredicateRegistry;
import org.objectweb.asm.tree.LabelNode;
import io.github.bineq.daleq.Fact;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-02-13T10:40Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__ANEWARRAY implements io.github.bineq.daleq.edb.InstructionPredicateFactFactory<org.objectweb.asm.tree.TypeInsnNode> {

    @Override public String getVersion() {
        return "306338a4-7b1f-4bba-b18a-e4f2a422120b";
    }

    @Override public EBDInstructionPredicate getPredicate() {
        return EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(189);
    }

    @Override public Fact createFact(String factId,org.objectweb.asm.tree.TypeInsnNode node,String methodRef,int instructionCounter,Map<LabelNode,Integer> labelMap) {
        return new SimpleFact(EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(189),new Object[]{factId,methodRef,instructionCounter,node.desc});
    }

};
