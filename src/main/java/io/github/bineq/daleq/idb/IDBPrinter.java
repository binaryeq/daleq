package io.github.bineq.daleq.idb;

import com.google.common.base.Preconditions;
import io.github.bineq.daleq.Fact;
import io.github.bineq.daleq.edb.FactExtractor;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Print the IDB in a format suitable for text-based diffing.
 * I.e. all facts are combined and arranged in a predictable order.
 * The output is simular to tools like javap.
 * Comments and spaces are inserted to group/organise facts.
 * @author jens dietrich
 */
public class IDBPrinter {

    public static final Logger LOG = LoggerFactory.getLogger(IDBPrinter.class);

    public static Option OPT_IDB = Option.builder()
        .argName("idb")
        .option("i")
        .hasArg(true)
        .required(true)
        .desc("the location of the IDB (a folder containing fact files)")
        .build();

    public static Option OPT_OUTPUT = Option.builder()
        .argName("out")
        .option("o")
        .hasArg(true)
        .required(true)
        .desc("the location of the output file (a text file)")
        .build();

    public static void main(String[] args) throws Exception {

        Options options = new Options();
        options.addOption(OPT_IDB);
        options.addOption(OPT_OUTPUT);
        CommandLine cli = null;
        CommandLineParser parser = new DefaultParser();

        try {
            cli = parser.parse(options, args);
            String idbDirName = cli.getOptionValue(OPT_IDB);
            String outputFileName = cli.getOptionValue(OPT_OUTPUT);

            Path idbDir = Path.of(idbDirName);
            Preconditions.checkState(Files.exists(idbDir));
            Preconditions.checkState(Files.isDirectory(idbDir));

            Path out = Path.of(outputFileName);

            IDB idb = IDBReader.read(idbDir);
            List<String> lines = new ArrayList<>();

            lines.addAll(comment1("class facts"));
            lines.add(stringify(idb.bytecodeVersionFact));
            lines.add(stringify(idb.classSuperclassFact));
            for (Fact interfaceFact:idb.classInterfaceFacts) {
                lines.add(stringify(interfaceFact));
            }
            lines.add(stringify(idb.classRawAccessFact));
            lines.add(idb.classSignatureFact==null?missingFact("class signature"):stringify(idb.classSignatureFact));
            for (Fact accessFact:idb.classAccessFacts) {
                lines.add(stringify(accessFact));
            }

            lines.addAll(comment1("list of fields"));
            for (Fact fieldFact:idb.fieldFacts) {
                lines.add(stringify(fieldFact));
            }

            lines.addAll(comment1("field details"));
            List<String> fieldIds = idb.fieldFacts.stream().map(fact -> fact.values()[1].toString()).collect(Collectors.toList());
            for (String fieldId:fieldIds) {
                lines.addAll(comment2("details for field " + fieldId));
                Fact fieldSignatureFact = idb.fieldSignatureFacts.get(fieldId);
                lines.add(stringify(fieldSignatureFact));
                Fact fieldRawAccessFact = idb.fieldRawAccessFacts.get(fieldId);
                lines.add(stringify(fieldRawAccessFact));
                for (Fact fieldAccessFact:idb.fieldAccessFacts.getOrDefault(fieldId,Set.of())) {
                    lines.add(stringify(fieldAccessFact));
                }
            }

            lines.addAll(comment1("list of methods"));
            for (Fact methodFact:idb.methodFacts) {
                lines.add(stringify(methodFact));
            }

            lines.addAll(comment1("methods details"));
            List<String> methodIds = idb.methodFacts.stream().map(fact -> fact.values()[1].toString()).collect(Collectors.toList());
            for (String methodId:methodIds) {
                lines.addAll(comment2("details for method " + methodId));
                Fact methodSignatureFact = idb.methodSignatureFacts.get(methodId);
                lines.add(stringify(methodSignatureFact));
                Fact methodRawAccessFact = idb.methodRawAccessFacts.get(methodId);
                lines.add(stringify(methodRawAccessFact));
                for (Fact methodAccessFact:idb.methodAccessFacts.getOrDefault(methodId, Set.of())) {
                    lines.add(stringify(methodAccessFact));
                }

                lines.addAll(comment2("\tinstructions for method " + methodId));
                for (Fact methodInstructionFact:idb.methodInstructionFacts.getOrDefault(methodId, Set.of())) {
                    lines.add(stringify(methodInstructionFact));
                }
            }


            Files.write(out, lines);
            LOG.info("output written to {}", out);

        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -cp <path-to-built-jar> " + FactExtractor.class.getName(), options);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String stringify(Fact fact) {
        if (fact==null) {
            return "null";
        }
        return fact.predicate().getName() + "\t" + Arrays.stream(fact.values()).map(v -> String.valueOf(v)).collect(Collectors.joining("\t", "\t", ""));
    }

    // top level comment (header)
    private static List<String> comment1(String text) {
        return List.of("","// " + text.toUpperCase(),"");
    }

    // minor comments
    private static List<String> comment2(String text) {
        return List.of("","// " + text ,"");
    }

    private static String missingFact(String text) {
        return "// no fact found:" + text;
    }

}
