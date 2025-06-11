package io.github.bineq.daleq.edb.instruction_fact_factories;

import io.github.bineq.daleq.Fact;
import io.github.bineq.daleq.SimpleFact;
import io.github.bineq.daleq.edb.EBDInstructionPredicate;
import io.github.bineq.daleq.edb.EDBPredicateRegistry;
import io.github.bineq.daleq.edb.FactExtractor;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

import javax.annotation.processing.Generated;
import java.util.List;
import java.util.stream.Collectors;

@Generated(value="io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator", date= "2025-02-13T10:40Z", comments= "factory generated from ASM tree API nodes")

// WARNING: manually added code to serialise labels !

public class InstructionFactFactory__TABLESWITCH implements io.github.bineq.daleq.edb.InstructionPredicateFactFactory<org.objectweb.asm.tree.TableSwitchInsnNode> {

    @Override public String getVersion() {
        return "34b7f7e7-c408-463f-9673-6be96d9291ff";
    }

    @Override public EBDInstructionPredicate getPredicate() {
        return EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(170);
    }

    @Override public Fact createFact(String factId, TableSwitchInsnNode node, String methodRef, int instructionCounter, FactExtractor.LabelMap labelMap) {
        return new SimpleFact(EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(170),new Object[]{factId,methodRef,instructionCounter,labelMap.get(node.dflt),serializeLabels(labelMap,node.labels),node.max,node.min});
    }

    // manually inserted code !
    private String serializeLabels(FactExtractor.LabelMap labelMap, List<LabelNode> labelNodes) {
        return labelNodes.stream()
            .map(node -> labelMap.get(node))
            .collect(Collectors.joining(","));
    }


};
