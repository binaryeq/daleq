package io.github.bineq.daleq.idb.rulegeneration;

import com.google.common.collect.Streams;
import io.github.bineq.daleq.Fact;
import io.github.bineq.daleq.Predicate;
import io.github.bineq.daleq.edb.EBDAdditionalPredicates;
import io.github.bineq.daleq.edb.EDBPredicateRegistry;
import io.github.bineq.daleq.idb.IDBRemovalPredicates;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Create rules to relocate instruction EDB fact to different methods when generating IDB fact.
 * Only rules will be generated, declarations and output files for each predicate
 * will be generated in @BaselineRuleGeneration.
 * @author jens dietrich
 */
public class InstructionRelocationRuleGeneration {

    public static final Logger LOG = LoggerFactory.getLogger(InstructionRelocationRuleGeneration.class);

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
            formatter.printHelp("java -cp <path-to-built-jar> " + InstructionRelocationRuleGeneration.class.getName(), options);
            e.printStackTrace();
            System.exit(1);
        }
    }

    static void generateRules (Path ruleFile) throws Exception {
        List<Predicate> predicates = EDBPredicateRegistry.ALL;
        List<String> lines = predicates.stream()
            .filter(predicate -> predicate.isInstructionPredicate())
            .flatMap(predicate -> generateRule(predicate).stream())
            .collect(Collectors.toList());
        Files.write(ruleFile, lines);
        LOG.info("rules written to: {}",ruleFile.toFile().getAbsolutePath());
    }

    static List<String> generateRule(Predicate predicate) {

        String idbPredicateName = "IDB_" + predicate.getName();

        String pre =  idbPredicateName + '(';
        String post =  ")";


        // synthesise provenance !

        assert predicate.getSlots()[0].name().equals(Fact.ID_SLOT_NAME);
        String ruleId = "R_"+predicate.getName()+"_MOVED"; // there is only one rule for each predicate, so this is a unique identifier !
        String provenanceTerm = String.format("cat(\"%s\",\"[\",%s,%s,\"]\")", ruleId,"factid1","factid2");
        List<String> terms = Streams.concat(
            // warning: instructioncounter must match term, in prereq !!
            List.of(provenanceTerm,"destMethodId","destInstructionCounter+instructioncounter/100").stream(),
            Arrays.stream(predicate.getSlots())
                .skip(3)
                .map(slot -> slot.encodeName()))
            .collect(Collectors.toUnmodifiableList());

        String head = terms.stream().collect(Collectors.joining(",",pre, post));

        // first premise
        pre =  predicate.getName() + '(';
        terms = Streams.concat(
            List.of("factid1").stream(),
            Arrays.stream(predicate.getSlots())
            .skip(1) // id field
            .map(slot -> slot.encodeName())
        ).collect(Collectors.toUnmodifiableList());


        String premise1 = terms.stream().collect(Collectors.joining(",",pre, post));

        pre = IDBRemovalPredicates.MOVED_INSTRUCTION.getName() + '(';
        terms = List.of(
            "factid2",
            predicate.getSlots()[1].encodeName(),
            predicate.getSlots()[2].encodeName(),
            "destMethodId",
            "destInstructionCounter",
            "\""+predicate.getName()+'\"'
        );
        String premise2 =  terms.stream().collect(Collectors.joining(",",pre, post));

        String rule = head +  " :- " + premise1 + " , " + premise2 + ".";
        return List.of(rule,"");

    }
}
