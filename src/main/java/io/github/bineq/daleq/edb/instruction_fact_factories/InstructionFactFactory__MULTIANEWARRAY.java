package io.github.bineq.daleq.edb.instruction_fact_factories;

import javax.annotation.processing.Generated;

import io.github.bineq.daleq.edb.FactExtractor;
import io.github.bineq.daleq.Fact;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;

@Generated(value="io.github.bineq.daleq.edb.InstructionFactFactoryCodeGenerator", date= "2025-04-28T23:19Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__MULTIANEWARRAY implements io.github.bineq.daleq.edb.InstructionPredicateFactFactory<org.objectweb.asm.tree.MultiANewArrayInsnNode> {

    @Override public String getVersion() {
        return "79583451-b9cd-4093-820c-226df3bde882";
    }

    @Override public io.github.bineq.daleq.edb.EBDInstructionPredicate getPredicate() {
        return io.github.bineq.daleq.edb.EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(197);
    }

    @Override public Fact createFact(String factid, MultiANewArrayInsnNode node, String methodRef, int instructionCounter, FactExtractor.LabelMap labelMap) {
        return new io.github.bineq.daleq.SimpleFact(io.github.bineq.daleq.edb.EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(197),new Object[]{factid,methodRef,instructionCounter,node.desc,node.dims});
    }

};
