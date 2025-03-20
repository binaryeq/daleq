package io.github.bineq.daleq.idb.rulegeneration;

import io.github.bineq.daleq.Fact;
import io.github.bineq.daleq.Predicate;
import io.github.bineq.daleq.edb.*;
import io.github.bineq.daleq.idb.IDBRemovalPredicates;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Create baseline rules that just translate each EDB fact into an IDB fact.
 * @author jens dietrich
 */
public class BaselineRuleGeneration {

    public static final Logger LOG = LoggerFactory.getLogger(BaselineRuleGeneration.class);

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
        List<Predicate> predicates = EDBPredicateRegistry.ALL;
        List<String> lines = predicates.stream()
            .flatMap(predicate -> generateRule(predicate).stream())
            .collect(Collectors.toList());
        Files.write(ruleFile, lines);
        LOG.info("rules written to: {}",ruleFile.toFile().getAbsolutePath());
    }

    static List<String> generateRule(Predicate predicate) {

        String idbPredicateName = "IDB_" + predicate.getName();

        // IDB predicate declaration
        String pre =  ".decl " + idbPredicateName + '(';
        String post =  ")";
        String declaration = Arrays.stream(predicate.getSlots())
            .map(slot -> slot.encodeName() + ": " + slot.type().souffleType())
            .collect(Collectors.joining(",",pre, post));

        pre =  idbPredicateName + '(';

        // synthesise provenance !

        assert predicate.getSlots()[0].name().equals(Fact.ID_SLOT_NAME);
        String ruleId = "R_"+predicate.getName(); // there is only one rule for each predicate, so this is a unique identifier !
        String provenanceTerm = String.format("cat(\"%s\",\"[\",%s,\"]\")", ruleId,Fact.ID_SLOT_NAME);
        List<String> headTerms = Arrays.stream(predicate.getSlots())
            .skip(1) // id field
            .map(slot -> slot.encodeName())
            .collect(Collectors.toList());
        headTerms.add(0,provenanceTerm);
        String head = headTerms.stream().collect(Collectors.joining(",",pre, post));

        pre =  predicate.getName() + '(';
        String body = Arrays.stream(predicate.getSlots())
            .map(slot -> slot.encodeName())
            .collect(Collectors.joining(",",pre, post));

        // TODO construct guards
        String guard = null;
        if (predicate.isInstructionPredicate()) {
            pre = "!"+ IDBRemovalPredicates.REMOVED_INSTRUCTION.getName() + '(';
            String body2 = List.of("_",predicate.getSlots()[1].name(),predicate.getSlots()[2].name()).stream()
                .collect(Collectors.joining(",",pre, post));
            guard = ","+body2;
        }
        else
        if (predicate==EBDAdditionalPredicates.METHOD) {
            pre = "!"+ IDBRemovalPredicates.REMOVED_METHOD.getName() + '(';
            String body2 = List.of("_",predicate.getSlots()[1].name()).stream()
                .collect(Collectors.joining(",",pre, post));
            guard = ","+body2;
        }
        else if (predicate==EBDAdditionalPredicates.FIELD) {
            pre = "!"+ IDBRemovalPredicates.REMOVED_FIELD.getName() + '(';
            String body2 = List.of("_",predicate.getSlots()[1].name()).stream()
                .collect(Collectors.joining(",",pre, post));
            guard = ","+body2;
        }

        if (guard != null) {
            body  = body + guard;
        }
        String rule = head +  " :- " + body + ".";
        String outDecl = ".output " + idbPredicateName;
        return List.of(declaration,rule,outDecl,"");

    }
}
