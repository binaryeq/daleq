package io.github.bineq.daleq.edb.instruction_fact_factories;

import javax.annotation.processing.Generated;

import io.github.bineq.daleq.edb.EDBInstructionPredicate;
import io.github.bineq.daleq.edb.FactExtractor;
import org.objectweb.asm.tree.InsnNode;
import io.github.bineq.daleq.Fact;

@Generated(value="io.github.bineq.daleq.edb.InstructionFactFactoryCodeGenerator", date= "2025-04-28T23:19Z",
      comments= "factory generated from ASM tree API nodes")
public class InstructionFactFactory__I2D implements io.github.bineq.daleq.edb.InstructionPredicateFactFactory<org.objectweb.asm.tree.InsnNode> {

    @Override public String getVersion() {
        return "e6972756-4b03-4dfe-b9ac-97ba07ca2e26";
    }

    @Override public EDBInstructionPredicate getPredicate() {
        return io.github.bineq.daleq.edb.EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(135);
    }

    @Override public Fact createFact(String factid, InsnNode node, String methodRef, int instructionCounter, FactExtractor.LabelMap labelMap) {
        return new io.github.bineq.daleq.SimpleFact(io.github.bineq.daleq.edb.EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(135),new Object[]{factid,methodRef,instructionCounter});
    }

};
