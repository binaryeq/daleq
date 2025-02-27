package io.github.bineq.daleq.edb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
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
        if (instr == null) {
            LOG.warn("no instruction found for opcode {}", opcode);
        }
        return instr;
    }

    private static Logger LOG = LoggerFactory.getLogger(InstructionTable.class);

    static {
        try {
            URL url = InstructionTable.class.getResource("/bytecode-instructions.csv");
            Path loc = Path.of(url.getFile());
            for (String line : Files.readAllLines(loc)) {
                String[] tokens = line.split(",");
                assert tokens.length == 2;
                int opcode = Integer.parseInt(tokens[1]);
                String name = tokens[0];
                OPCODE_MAP.put(opcode,name);
            }

        } catch (IOException x) {
            LOG.error("Failed to load bytecode-instructions.csv");
            throw new RuntimeException(x);
        }
    }
}
