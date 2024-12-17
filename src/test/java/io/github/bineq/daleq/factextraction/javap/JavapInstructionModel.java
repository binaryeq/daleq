package io.github.bineq.daleq.factextraction.javap;

import java.util.Objects;

/**
 * Represents an instruction in a javap disassembly.
 * @author jens dietrich
 */
public class JavapInstructionModel {
    private int label = -1;
    private String instruction = null;
    private int constantPoolRef = -1;
    private String value = null; // resolved constant pool ref

    JavapInstructionModel(int label, String instruction, int constantPoolRef, String value) {
        this.label = label;
        this.instruction = instruction;
        this.constantPoolRef = constantPoolRef;
        this.value = value;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public int getConstantPoolRef() {
        return constantPoolRef;
    }

    public void setConstantPoolRef(int constantPoolRef) {
        this.constantPoolRef = constantPoolRef;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavapInstructionModel that = (JavapInstructionModel) o;
        return label == that.label && constantPoolRef == that.constantPoolRef && Objects.equals(instruction, that.instruction) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, instruction, constantPoolRef, value);
    }
}
