package io.github.bineq.daleq.idb.rulegeneration;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Create predicates and IDB rules from access flags.
 * @author jens dietrich
 */
public class AccessRuleGeneration {

    public static final Logger LOG = LoggerFactory.getLogger(AccessRuleGeneration.class);

    // data extracted from ASM source code org/objectweb/asm/Opcodes.java
    private static Map<String,String> OP_CODES = new HashMap<>();
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

    public static Option OPT_DB = Option.builder()
        .argName("rules")
        .option("r")
        .hasArg()
        .required(true)
        .desc("a file where to create the souffle program (.souffle file) containing rules")
        .build();


    public static void main(String[] args) throws Exception {

        Options options = new Options();
        options.addOption(OPT_DB);
        CommandLine cli = null;
        CommandLineParser parser = new DefaultParser();

        try {
            cli = parser.parse(options, args);
            String ruleFileName = cli.getOptionValue(OPT_DB);

            generateRules(Path.of(ruleFileName));

        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -cp <path-to-built-jar> " + BaselineRuleGeneration.class.getName(), options);
            e.printStackTrace();
            System.exit(1);
        }
    }

    static void generateRules (Path ruleFile) throws Exception {
        List<String> lines = OP_CODES.keySet().stream()
            .flatMap(name -> generateRule(name).stream())
            .collect(Collectors.toList());
        Files.write(ruleFile, lines);
        LOG.info("rules written to: {}",ruleFile.toFile().getAbsolutePath());
    }

    static List<String> generateRule(String name) {

//        .decl IDB_IS_ENUM(factid: symbol,classOrMethodOrFieldId: symbol)
//        IDB_IS_ENUM(cat("R_IS_ENUM","[",factid,"]"),classOrMethodOrFieldId) :- ACCESS(factid,classOrMethodOrFieldId,access), (access band 16384)!=0.
//        .output IDB_IS_ENUM

        String predicateName = "IDB_IS_"+name;
        String ruleId = "R_IS_"+name;
        String opCode = OP_CODES.get(name);
        assert opCode != null;

        String declaration = String.format(".decl %s(factid: symbol,classOrMethodOrFieldId: symbol)",predicateName);
        String rule = String.format("%s(cat(\"%s\",\"[\",factid,\"]\"),classOrMethodOrFieldId) :- ACCESS(factid,classOrMethodOrFieldId,access), (access band %s)!=0.",predicateName,ruleId,opCode);
        String output = String.format(".output %s",predicateName);

        return List.of(declaration,rule,output,"");

    }
}
