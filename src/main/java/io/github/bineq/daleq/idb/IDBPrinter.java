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

    public static Option OPT_SKIP_PROVENANCE = Option.builder()
        .argName("skipprovenance")
        .option("sp")
        .hasArg(false)
        .required(false)
        .desc("whether to skip provenance terms as they are only metadata (default is not to skip)")
        .build();

    public static void main(String[] args) throws Exception {

        Options options = new Options();
        options.addOption(OPT_IDB);
        options.addOption(OPT_OUTPUT);
        options.addOption(OPT_RULES);
        options.addOption(OPT_SKIP_PROVENANCE);
        CommandLine cli = null;
        CommandLineParser parser = new DefaultParser();

        try {
            cli = parser.parse(options, args);
            String inputName = cli.getOptionValue(OPT_IDB);
            String outputFileName = cli.getOptionValue(OPT_OUTPUT);
            String rulesFileName = cli.getOptionValue(OPT_RULES);
            Path out = Path.of(outputFileName);
            boolean skipProvenance = cli.hasOption(OPT_SKIP_PROVENANCE);

            Path input = Path.of(inputName);
            if (Files.isDirectory(input)) {
                LOG.info("Loading existing IDB from {}", input);
                printIDB(input, out,skipProvenance);
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

                printIDB(idbDir, out,skipProvenance);
            }

        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -cp <path-to-built-jar> " + FactExtractor.class.getName(), options);
            e.printStackTrace();
            System.exit(1);
        }
    }


    static void printIDB(Path idbDir, Path out,boolean skipProvenance) throws IOException {
        Preconditions.checkState(Files.exists(idbDir));
        Preconditions.checkState(Files.isDirectory(idbDir));
        IDB idb = IDBReader.read(idbDir);
        printIDB(idb, out,skipProvenance);
    }

    static void printIDB(IDB idb, Path out, boolean skipProvenance) throws IOException {

        List<String> lines = new ArrayList<>();

        lines.addAll(comment1("class facts"));
        lines.add(stringify(idb.bytecodeVersionFact,skipProvenance));
        lines.add(stringify(idb.classSuperclassFact,skipProvenance));
        for (Fact interfaceFact:idb.classInterfaceFacts) {
            lines.add(stringify(interfaceFact,skipProvenance));
        }
        lines.add(stringify(idb.classRawAccessFact,skipProvenance));
        lines.add(idb.classSignatureFact==null?missingFact("class signature"):stringify(idb.classSignatureFact,skipProvenance));
        for (Fact accessFact:idb.classAccessFacts) {
            lines.add(stringify(accessFact,skipProvenance));
        }

        lines.addAll(comment1("list of fields"));
        for (Fact fieldFact:idb.fieldFacts) {
            lines.add(stringify(fieldFact,skipProvenance));
        }

        lines.addAll(comment1("field details"));
        List<String> fieldIds = idb.fieldFacts.stream().map(fact -> fact.values()[1].toString()).collect(Collectors.toList());
        for (String fieldId:fieldIds) {
            lines.addAll(comment2("details for field " + fieldId));
            Fact fieldSignatureFact = idb.fieldSignatureFacts.get(fieldId);
            lines.add(stringify(fieldSignatureFact,skipProvenance));
            Fact fieldRawAccessFact = idb.fieldRawAccessFacts.get(fieldId);
            lines.add(stringify(fieldRawAccessFact,skipProvenance));
            for (Fact fieldAccessFact:idb.fieldAccessFacts.getOrDefault(fieldId,Set.of())) {
                lines.add(stringify(fieldAccessFact,skipProvenance));
            }
        }

        lines.addAll(comment1("list of methods"));
        for (Fact methodFact:idb.methodFacts) {
            lines.add(stringify(methodFact,skipProvenance));
        }

        lines.addAll(comment1("methods details"));
        List<String> methodIds = idb.methodFacts.stream().map(fact -> fact.values()[1].toString()).collect(Collectors.toList());
        for (String methodId:methodIds) {
            lines.addAll(comment2("details for method " + methodId));
            Fact methodSignatureFact = idb.methodSignatureFacts.get(methodId);
            lines.add(stringify(methodSignatureFact,skipProvenance));
            Fact methodRawAccessFact = idb.methodRawAccessFacts.get(methodId);
            lines.add(stringify(methodRawAccessFact,skipProvenance));
            for (Fact methodAccessFact:idb.methodAccessFacts.getOrDefault(methodId, Set.of())) {
                lines.add(stringify(methodAccessFact,skipProvenance));
            }

            lines.addAll(comment2("\tinstructions for method " + methodId));
            for (Fact methodInstructionFact:idb.methodInstructionFacts.getOrDefault(methodId, Set.of())) {
                lines.add(stringify(methodInstructionFact,skipProvenance));
            }
        }

        Files.write(out, lines);
        LOG.info("output written to {}", out);

    }

    private static String stringify(Fact fact,boolean skipProvenance) {
        if (fact==null) {
            return "null";
        }
        String values = skipProvenance ?
            Arrays.stream(fact.values()).skip(1)    .map(v -> String.valueOf(v)).collect(Collectors.joining("\t", "\t", "")):
            Arrays.stream(fact.values())               .map(v -> String.valueOf(v)).collect(Collectors.joining("\t", "\t", ""));

        return fact.predicate().getName() + "\t" + values;
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
