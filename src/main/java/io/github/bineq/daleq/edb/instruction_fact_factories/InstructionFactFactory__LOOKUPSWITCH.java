package io.github.bineq.daleq.edb.instruction_fact_factories;

import io.github.bineq.daleq.Fact;
import io.github.bineq.daleq.SimpleFact;
import io.github.bineq.daleq.edb.EBDInstructionPredicate;
import io.github.bineq.daleq.edb.EDBPredicateRegistry;
import io.github.bineq.daleq.edb.FactExtractor;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;

import javax.annotation.processing.Generated;
import java.util.List;
import java.util.stream.Collectors;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-02-13T10:40Z",
      comments= "factory generated from ASM tree API nodes")

// WARNING: manually added code to serialise labels !


public class InstructionFactFactory__LOOKUPSWITCH implements io.github.bineq.daleq.edb.InstructionPredicateFactFactory<org.objectweb.asm.tree.LookupSwitchInsnNode> {

    @Override public String getVersion() {
        return "cf81047e-c7fb-48b8-94b7-ff250a4791e8";
    }

    @Override public EBDInstructionPredicate getPredicate() {
        return EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(171);
    }

    @Override public Fact createFact(String factId, LookupSwitchInsnNode node, String methodRef, int instructionCounter, FactExtractor.LabelMap labelMap) {
        return new SimpleFact(EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(171),new Object[]{factId,methodRef,instructionCounter,labelMap.get(node.dflt),node.keys,serializeLabels(labelMap,node.labels)});
    }


    // manually inserted code !
    private String serializeLabels(FactExtractor.LabelMap labelMap, List<LabelNode> labelNodes) {
        return labelNodes.stream()
            .map(node -> labelMap.get(node))
            .collect(Collectors.joining(","));
    }
};
