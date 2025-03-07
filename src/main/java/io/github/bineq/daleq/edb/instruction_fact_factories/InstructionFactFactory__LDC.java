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
public class InstructionFactFactory__LDC implements io.github.bineq.daleq.edb.InstructionPredicateFactFactory<org.objectweb.asm.tree.LdcInsnNode> {

    @Override public String getVersion() {
        return "2faab171-669b-4459-b712-66f3ddec922b";
    }

    @Override public EBDInstructionPredicate getPredicate() {
        return EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(18);
    }

    @Override public Fact createFact(String factId,org.objectweb.asm.tree.LdcInsnNode node,String methodRef,int instructionCounter,Map<LabelNode,Integer> labelMap) {
        return new SimpleFact(EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(18),new Object[]{factId,methodRef,instructionCounter,node.cst});
    }

};
