package io.github.bineq.daleq.factextraction.javap;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a method in a javap disassembly.
 * @author jens dietrich
 */
public class JavapMethodModel {
    private String name;
    private List<JavapInstructionModel> instructions = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<JavapInstructionModel> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<JavapInstructionModel> instructions) {
        this.instructions = instructions;
    }
}
