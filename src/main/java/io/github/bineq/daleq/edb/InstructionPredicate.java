package io.github.bineq.daleq.edb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.Arrays;
import java.util.Objects;

/**
 * Predicates for facts representing bytecode instructions.
 * @author jens dietrich
 */
public class InstructionPredicate implements Predicate {

    private String name = null;
    private int opCode = -1;
    private Slot[] slots = null;
    private String id = null;
    private String asmNodeType = null;  // for provenance of this has been inferred

    @Override
    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public int getOpCode() {
        return opCode;
    }

    @Override
    public Slot[] getSlots() {
        return slots;
    }

    public void setSlots(Slot[] slots) {
        this.slots = slots;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOpCode(int opCode) {
        this.opCode = opCode;
    }

    public String getAsmNodeType() {
        return asmNodeType;
    }

    public void setAsmNodeType(String asmNodeType) {
        this.asmNodeType = asmNodeType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstructionPredicate predicate = (InstructionPredicate) o;
        return opCode == predicate.opCode && Objects.equals(name, predicate.name) && Objects.deepEquals(slots, predicate.slots) && Objects.equals(id, predicate.id) && Objects.equals(asmNodeType, predicate.asmNodeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, opCode, Arrays.hashCode(slots), id, asmNodeType);
    }

    public void toJson(File f) throws IOException {
        String json = new GsonBuilder().setPrettyPrinting().create().toJson(this);
        try (FileWriter writer = new FileWriter(f)) {
            writer.write(json);
        }
    }

    public static InstructionPredicate fromJson(File f) throws IOException {
        try (FileReader reader = new FileReader(f)) {
            return new Gson().fromJson(reader, InstructionPredicate.class);
        }
    }

    @Override
    public boolean isInstructionPredicate() {
        return true;
    }
}
