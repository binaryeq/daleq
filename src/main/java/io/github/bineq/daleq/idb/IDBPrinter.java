package io.github.bineq.daleq.idb;

import com.google.common.base.Preconditions;
import io.github.bineq.daleq.Fact;
import io.github.bineq.daleq.Souffle;
import io.github.bineq.daleq.edb.FactExtractor;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Print the IDB in a format suitable for text-based diffing.
 * I.e. all facts are combined and arranged in a predictable order.
 * The output is similar to tools like javap.
 * Comments and spaces are inserted to group/organise facts.
 * Can be used as app (main) and via API (print).
 * @author jens dietrich
 */
public class IDBPrinter {

    public static final Logger LOG = LoggerFactory.getLogger(IDBPrinter.class);
    public static final Path DEFAULT_RULES = Path.of(IDBPrinter.class.getResource("/rules/vanilla.souffle").getPath());
    public static final Path TMP_DIR_ROOT = Path.of(".tmp/"+IDBPrinter.class.getName());


    static {
        try {
            Files.createDirectories(TMP_DIR_ROOT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Option OPT_IDB = Option.builder()
        .argName("idb")
        .option("i")
        .hasArg(true)
        .required(true)
        .desc("the location of the IDB (a folder containing fact files), or a bytecode file (.class)")
        .build();

    public static Option OPT_RULES = Option.builder()
        .argName("rules")
        .option("r")
        .hasArg(true)
        .required(false)
        .desc("the location of the rules to be used to generate the IDB (.souffle)")
        .build();

    public static Option OPT_OUTPUT = Option.builder()
        .argName("out")
        .option("o")
        .hasArg(true)
        .required(true)
        .desc("the location of the output file (a text file)")
        .build();

    public static Option OPT_PROJECT = Option.builder()
        .argName("project")
        .option("p")
        .hasArg(false)
        .required(false)
        .desc("whether to project the idb (projection is used for equivalence), default is false")
        .build();



    public static void main(String[] args) throws Exception {

        Options options = new Options();
        options.addOption(OPT_IDB);
        options.addOption(OPT_OUTPUT);
        options.addOption(OPT_RULES);
        options.addOption(OPT_PROJECT);

        CommandLine cli = null;
        CommandLineParser parser = new DefaultParser();

        try {
            cli = parser.parse(options, args);
            String inputName = cli.getOptionValue(OPT_IDB);
            String outputFileName = cli.getOptionValue(OPT_OUTPUT);
            String rulesFileName = cli.getOptionValue(OPT_RULES);
            Path out = Path.of(outputFileName);

            boolean project = cli.hasOption(OPT_PROJECT);

            Path input = Path.of(inputName);
            if (Files.isDirectory(input)) {
                LOG.info("Loading existing IDB from {}", input);
                printIDB(input, out,project);
            }
            else if (Files.isRegularFile(input) && input.toString().endsWith(".class") ) {

                // as the IDB is computed, we will need SOUFFLE
                Preconditions.checkState(Souffle.checkSouffleExe());

                Path classFile = input;
                LOG.info("Generating IDB from bytecode in {}", classFile);
                Path rules = null;
                if (rulesFileName!=null) {
                    rules = Path.of(rulesFileName);
                    LOG.info("Loading existing rules from {}", rules);
                }
                else {
                    rules = DEFAULT_RULES;
                    LOG.info("Using default rules from {}", rules);
                }
                Preconditions.checkState(Files.exists(rules));
                Path dir = Files.createTempDirectory(TMP_DIR_ROOT,null);
                Path idbDir = dir.resolve("idb");
                Path edbDir = dir.resolve("edb");
                Path edbDefFile = dir.resolve("db.souffle");
                Path mergedFactsAndRulesFile = dir.resolve("merged-facts-and-rules1.souffle");
                Files.createDirectories(edbDir);
                Files.createDirectories(idbDir);

                LOG.info("extacting EDB");
                FactExtractor.extractAndExport(classFile,edbDefFile,edbDir,true);
                LOG.info("EDB def extracted to {}" , edbDefFile);
                LOG.info("EDB facts extracted to {}" , edbDir);

                LOG.info("computing IDB");
                Souffle.createIDB(edbDefFile,rules,edbDir,idbDir,mergedFactsAndRulesFile);
                LOG.info("EDB facts extracted to {}" , idbDir);

                printIDB(idbDir, out,project);
            }

        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -cp <path-to-built-jar> " + FactExtractor.class.getName(), options);
            e.printStackTrace();
            System.exit(1);
        }
    }

    // for API use
    public static String print(IDB idb) throws IOException {
        List<String> lines = printToLines(idb);
        return String.join(System.lineSeparator(), lines);
    }

    private static void printIDB(Path idbDir, Path out, boolean project) throws IOException {
        Preconditions.checkState(Files.exists(idbDir));
        Preconditions.checkState(Files.isDirectory(idbDir));
        IDB idb = IDBReader.read(idbDir);
        if (project) {
            idb = idb.project();
        }
        printIDB(idb, out);
    }

    private static void printIDB(IDB idb, Path out) throws IOException {
        List<String> lines = printToLines(idb);
        Files.write(out, lines);
        LOG.info("output written to {}", out);
    }


    private static List<String> printToLines (IDB idb) throws IOException {

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

        lines.addAll(comment1("list of removed fields"));
        for (Fact removedFieldFact : idb.removedFieldFacts) {
            lines.add(stringify(removedFieldFact));
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

        lines.addAll(comment1("list of removed methods"));
        for (Fact removedMethodFact : idb.removedMethodFacts) {
            lines.add(stringify(removedMethodFact));
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

            lines.addAll(comment2("instructions for method " + methodId));
            for (Fact methodInstructionFact:idb.methodInstructionFacts.getOrDefault(methodId, Set.of())) {
                lines.add(stringify(methodInstructionFact));
            }
        }

        return lines;

    }


    private static String stringify(Fact fact) {
        return Stream.of(fact.values()).map(v -> String.valueOf(v)).collect(Collectors.joining("\t",fact.predicate().getName() + "\t", ""));
    }

    // top level comment (header)
    private static List<String> comment1(String text) {
        return List.of("","// " + text.toUpperCase(),"");
    }

    // minor comments
    private static List<String> comment2(String text) {
        return List.of("","// " + text ,"");
    }

    private static String missingFact(String kind) {
        return "// no fact found:" + kind;
    }

}
