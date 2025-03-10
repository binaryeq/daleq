package io.github.bineq.daleq.idb;

import io.github.bineq.daleq.Predicate;
import io.github.bineq.daleq.Slot;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Opcodes, mapping names to opcodes.
 * @author jens dietrich
 */
public class IDBAccessPredicates {

    // data extracted from ASM source code org/objectweb/asm/Opcodes.java
    public static Map<String,String> OP_CODES = new HashMap<>();
    static {
        OP_CODES.put("PUBLIC","0x0001");
        OP_CODES.put("PRIVATE","0x0002");
        OP_CODES.put("PROTECTED","0x0004");
        OP_CODES.put("STATIC","0x0008");
        OP_CODES.put("FINAL","0x0010");
        OP_CODES.put("SUPER","0x0020");
        OP_CODES.put("SYNCHRONIZED","0x0020");
        OP_CODES.put("OPEN","0x0020");
        OP_CODES.put("TRANSITIVE","0x0020");
        OP_CODES.put("VOLATILE","0x0040");
        OP_CODES.put("BRIDGE","0x0040");
        OP_CODES.put("STATIC_PHASE","0x0040");
        OP_CODES.put("VARARGS","0x0080");
        OP_CODES.put("TRANSIENT","0x0080");
        OP_CODES.put("NATIVE","0x0100");
        OP_CODES.put("INTERFACE","0x0200");
        OP_CODES.put("ABSTRACT","0x0400");
        OP_CODES.put("STRICT","0x0800");
        OP_CODES.put("SYNTHETIC","0x1000");
        OP_CODES.put("ANNOTATION","0x2000");
        OP_CODES.put("ENUM","0x4000");
        OP_CODES.put("MANDATED","0x8000");
        OP_CODES.put("MODULE","0x8000");
    }

    public static final String ACCESS_PREDICATE_PREFIX = "IDB_IS_";

    // convert a method to a predicate, the arg is a key like "PUBLIC"
    private static Predicate buildAccessPredicate(String name) {
        return new Predicate() {

            @Override
            public Slot[] getSlots() {
                // example: .decl IDB_IS_TRANSIENT(factid: symbol,classOrMethodOrFieldId: symbol)
                return new Slot[] {
                    Slot.symslot("factid"),
                    Slot.symslot("classOrMethodOrFieldId")
                };
            }

            @Override
            public String getName() {
                return ACCESS_PREDICATE_PREFIX + name;
            }

            @Override
            public boolean isInstructionPredicate() {
                return false;
            }

            @Override
            public boolean isEDBPredicate() {
                return false;
            }

            @Override
            public boolean isIDBPredicate() {
                return true;
            }
        };
    }

    public static Set<Predicate> ALL = OP_CODES.keySet().stream()
        .map(name -> buildAccessPredicate(name))
        .collect(Collectors.toSet());

}
