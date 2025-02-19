package io.github.bineq.daleq.factextraction;

import java.util.Objects;

/**
 * An element of a record, souffle calls them fields, slot is used here to avoid confusion with Java fields.
 * @author jens dietrich
 */
public record Slot(String name,SlotType type,String jtype) {

    static Slot symslot(String name) {
        return new Slot(name, SlotType.SYMBOL,String.class.getName());
    }

    static Slot symslot(String name,String jname) {
        return new Slot(name, SlotType.SYMBOL,jname);
    }

    static Slot numslot(String name,String jname) {
        return new Slot(name, SlotType.NUMBER,jname);
    }

    // if the name is a keyword, encode it
    public String encodeName() {
        String name = this.name();

        // encode souffle keywords
        if (Objects.equals(name,"max")) {
            return "max_";
        }
        else if (Objects.equals(name,"min")) {
            return "min_";
        }
        else if (name.contains(".") || name.contains("(") || name.contains(")")) {
            return name.replace(".","_")
                .replace("(","")
                .replace(")","");
        }
        else {
            return name;
        }
    }
}
