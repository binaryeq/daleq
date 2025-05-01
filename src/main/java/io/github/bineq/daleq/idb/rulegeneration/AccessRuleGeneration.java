package io.github.bineq.daleq.idb.rulegeneration;

import io.github.bineq.daleq.idb.IDBAccessPredicates;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.bineq.daleq.idb.IDBAccessPredicates.ACCESS_PREDICATE_PREFIX;

/**
 * Create predicates and IDB rules from access flags.
 * @author jens dietrich
 */
public class AccessRuleGeneration {

    public static final Logger LOG = LoggerFactory.getLogger(AccessRuleGeneration.class);


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
            formatter.printHelp("java -cp <path-to-built-jar> " + AccessRuleGeneration.class.getName(), options);
            e.printStackTrace();
            System.exit(1);
        }
    }

    static void generateRules (Path ruleFile) throws Exception {
        List<String> lines = IDBAccessPredicates.OP_CODES.keySet().stream()
            .flatMap(name -> generateRule(name).stream())
            .collect(Collectors.toList());
        Files.write(ruleFile, lines);
        LOG.info("rules written to: {}",ruleFile.toFile().getAbsolutePath());
    }

    static List<String> generateRule(String name) {

//        .decl IDB_IS_ENUM(factid: symbol,classOrMethodOrFieldId: symbol)
//        IDB_IS_ENUM(cat("R_IS_ENUM","[",factid,"]"),classOrMethodOrFieldId) :- ACCESS(factid,classOrMethodOrFieldId,access), (access band 16384)!=0.
//        .output IDB_IS_ENUM

        String predicateName = ACCESS_PREDICATE_PREFIX+name;
        String ruleId = "R_IS_"+name;
        String opCode = IDBAccessPredicates.OP_CODES.get(name);
        assert opCode != null;

        String declaration = String.format(".decl %s(factid: symbol,classOrMethodOrFieldId: symbol)",predicateName);
        String rule = String.format("%s(cat(\"%s\",\"[\",factid,\"]\"),classOrMethodOrFieldId) :- ACCESS(factid,classOrMethodOrFieldId,access), (access band %s)!=0.",predicateName,ruleId,opCode);
        String output = String.format(".output %s",predicateName);

        return List.of(declaration,rule,output,"");

    }
}
