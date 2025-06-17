package io.github.bineq.daleq.edb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * A list of opecodes.
 * Data sources from org/objectweb/asm/Opcodes.java.
 * @author jens dietrich
 */
public class InstructionTable {

    private static final Map<Integer,String> OPCODE_MAP = new HashMap<>();

    static String getInstruction(int opcode) {
        String instr = OPCODE_MAP.get(opcode);
        if (instr == null && opcode > -1) {
            LOG.warn("no instruction found for opcode {}", opcode);
        }
        return instr;
    }

    private static Logger LOG = LoggerFactory.getLogger(InstructionTable.class);

    static {
        URL url = InstructionTable.class.getResource("/bytecode-instructions.csv");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));) {
            reader.lines().forEach(line -> {
                String[] tokens = line.split(",");
                assert tokens.length == 2;
                int opcode = Integer.parseInt(tokens[1]);
                String name = tokens[0];
                OPCODE_MAP.put(opcode,name);
            });

        } catch (IOException x) {
            LOG.error("Failed to load bytecode-instructions.csv");
            throw new RuntimeException(x);
        }
    }
}
