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

    public static final boolean DEFAULT_INCLUDE_PROVENANCE = false;
    public static final boolean DEFAULT_INCLUDE_INSTRUCTION_COUNTERS = false;
    public static final boolean DEFAULT_INCLUDE_CLASS_METHOD_FIELD_IDS = false;
    public static final boolean DEFAULT_INCLUDE_REMOVED = false;

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

    public static Option OPT_INCLUDE_PROVENANCE_SLOT = Option.builder()
        .argName("includeprovenance")
        .option("ip")
        .hasArg(true)
        .required(false)
        .desc("whether to include provenance terms as they are only metadata (true or false, default is " + DEFAULT_INCLUDE_PROVENANCE + ")")
        .build();

    public static Option OPT_INCLUDE_INSTRUCTIONCOUNTER_SLOT = Option.builder()
        .argName("includeinstructioncounter")
        .option("ic")
        .hasArg(true)
        .required(false)
        .desc("whether to include instruction counters as they are only used for ordering facts (true or false, default is " + DEFAULT_INCLUDE_INSTRUCTION_COUNTERS + ")")
        .build();

    public static Option OPT_INCLUDE_IDS_IN_INSTRUCTIONS_SLOT = Option.builder()
        .argName("includeids")
        .option("ii")
        .hasArg(true)
        .required(false)
        .desc("whether to include class/method/field ids in instruction facts as those facts are grouped by class/method/field anyway (true or false, default is " + DEFAULT_INCLUDE_CLASS_METHOD_FIELD_IDS + ")")
        .build();

    public static Option OPT_INCLUDE_REMOVED_ITEMS = Option.builder()
        .argName("includeremoved")
        .option("ir")
        .hasArg(true)
        .required(false)
        .desc("whether to include methods, fields or instructions that have been removed (true or false, default is " + DEFAULT_INCLUDE_REMOVED + ")")
        .build();

    public static void main(String[] args) throws Exception {

        Options options = new Options();
        options.addOption(OPT_IDB);
        options.addOption(OPT_OUTPUT);
        options.addOption(OPT_RULES);
        options.addOption(OPT_INCLUDE_PROVENANCE_SLOT);
        options.addOption(OPT_INCLUDE_INSTRUCTIONCOUNTER_SLOT);
        options.addOption(OPT_INCLUDE_IDS_IN_INSTRUCTIONS_SLOT);
        options.addOption(OPT_INCLUDE_REMOVED_ITEMS);

        CommandLine cli = null;
        CommandLineParser parser = new DefaultParser();

        try {
            cli = parser.parse(options, args);
            String inputName = cli.getOptionValue(OPT_IDB);
            String outputFileName = cli.getOptionValue(OPT_OUTPUT);
            String rulesFileName = cli.getOptionValue(OPT_RULES);
            Path out = Path.of(outputFileName);

            boolean includeProvenance =  getBooleanOptValue(cli,OPT_INCLUDE_PROVENANCE_SLOT,DEFAULT_INCLUDE_PROVENANCE);
            boolean includeInstructionCounters =  getBooleanOptValue(cli,OPT_INCLUDE_INSTRUCTIONCOUNTER_SLOT,DEFAULT_INCLUDE_INSTRUCTION_COUNTERS);
            boolean includeClassOrMethodOrFieldIds =  getBooleanOptValue(cli,OPT_INCLUDE_IDS_IN_INSTRUCTIONS_SLOT, DEFAULT_INCLUDE_CLASS_METHOD_FIELD_IDS);
            boolean includeRemoved =  getBooleanOptValue(cli,OPT_INCLUDE_REMOVED_ITEMS, DEFAULT_INCLUDE_REMOVED);

            Path input = Path.of(inputName);
            if (Files.isDirectory(input)) {
                LOG.info("Loading existing IDB from {}", input);
                printIDB(input, out,includeProvenance,includeInstructionCounters,includeClassOrMethodOrFieldIds,includeRemoved);
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

                printIDB(idbDir, out,includeProvenance,includeInstructionCounters,includeClassOrMethodOrFieldIds,includeRemoved);
            }

        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -cp <path-to-built-jar> " + FactExtractor.class.getName(), options);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static boolean getBooleanOptValue(CommandLine cli, Option option, boolean defaultValue) {
        if (!cli.hasOption(option)) {
            return defaultValue;
        }
        String valueAsString = cli.getOptionValue(option);
        Preconditions.checkArgument("true".equals(valueAsString) || "false".equals(valueAsString));
        return Boolean.parseBoolean(valueAsString);
    }


    static void printIDB(Path idbDir, Path out,boolean includeProvenance,boolean includeInstructionCounters,boolean includeClassOrMethodOrFieldIds,boolean includeRemoved) throws IOException {
        Preconditions.checkState(Files.exists(idbDir));
        Preconditions.checkState(Files.isDirectory(idbDir));
        IDB idb = IDBReader.read(idbDir);
        printIDB(idb, out,includeProvenance,includeInstructionCounters,includeClassOrMethodOrFieldIds,includeRemoved);
    }

    static void printIDB(IDB idb, Path out, boolean includeProvenance,boolean includeInstructionCounters,boolean includeClassOrMethodOrFieldIds,boolean includeRemoved) throws IOException {

        List<String> lines = new ArrayList<>();

        lines.addAll(comment1("class facts"));
        lines.add(stringifyMemberFact(idb.bytecodeVersionFact,includeProvenance,includeClassOrMethodOrFieldIds));
        lines.add(stringifyMemberFact(idb.classSuperclassFact,includeProvenance,includeClassOrMethodOrFieldIds));
        for (Fact interfaceFact:idb.classInterfaceFacts) {
            lines.add(stringifyMemberFact(interfaceFact,includeProvenance,includeClassOrMethodOrFieldIds));
        }
        lines.add(stringifyMemberFact(idb.classRawAccessFact,includeProvenance,includeClassOrMethodOrFieldIds));
        lines.add(idb.classSignatureFact==null?missingFact("class signature"):stringifyOtherFact(idb.classSignatureFact,includeProvenance));
        for (Fact accessFact:idb.classAccessFacts) {
            lines.add(stringifyMemberFact(accessFact,includeProvenance,includeClassOrMethodOrFieldIds));
        }

        lines.addAll(comment1("list of fields"));
        for (Fact fieldFact:idb.fieldFacts) {
            lines.add(stringifyOtherFact(fieldFact,includeProvenance));
        }

        if (includeRemoved) {
            lines.addAll(comment1("list of removed fields"));
            for (Fact removedFieldFact : idb.removedFieldFacts) {
                lines.add(stringifyOtherFact(removedFieldFact, includeProvenance));
            }
        }

        lines.addAll(comment1("field details"));
        List<String> fieldIds = idb.fieldFacts.stream().map(fact -> fact.values()[1].toString()).collect(Collectors.toList());
        for (String fieldId:fieldIds) {
            lines.addAll(comment2("details for field " + fieldId));
            Fact fieldSignatureFact = idb.fieldSignatureFacts.get(fieldId);
            lines.add(stringifyMemberFact(fieldSignatureFact,includeProvenance,includeClassOrMethodOrFieldIds));
            Fact fieldRawAccessFact = idb.fieldRawAccessFacts.get(fieldId);
            lines.add(stringifyMemberFact(fieldRawAccessFact,includeProvenance,includeClassOrMethodOrFieldIds));
            for (Fact fieldAccessFact:idb.fieldAccessFacts.getOrDefault(fieldId,Set.of())) {
                lines.add(stringifyMemberFact(fieldAccessFact,includeProvenance,includeClassOrMethodOrFieldIds));
            }
        }

        lines.addAll(comment1("list of methods"));
        for (Fact methodFact:idb.methodFacts) {
            lines.add(stringifyOtherFact(methodFact,includeProvenance));
        }

        if (includeRemoved) {
            lines.addAll(comment1("list of removed methods"));
            for (Fact removedMethodFact : idb.removedMethodFacts) {
                lines.add(stringifyOtherFact(removedMethodFact, includeProvenance));
            }
        }

        lines.addAll(comment1("methods details"));
        List<String> methodIds = idb.methodFacts.stream().map(fact -> fact.values()[1].toString()).collect(Collectors.toList());
        for (String methodId:methodIds) {
            lines.addAll(comment2("details for method " + methodId));
            Fact methodSignatureFact = idb.methodSignatureFacts.get(methodId);
            lines.add(stringifyMemberFact(methodSignatureFact,includeProvenance,includeClassOrMethodOrFieldIds));
            Fact methodRawAccessFact = idb.methodRawAccessFacts.get(methodId);
            lines.add(stringifyMemberFact(methodRawAccessFact,includeProvenance,includeClassOrMethodOrFieldIds));
            for (Fact methodAccessFact:idb.methodAccessFacts.getOrDefault(methodId, Set.of())) {
                lines.add(stringifyMemberFact(methodAccessFact,includeProvenance,includeClassOrMethodOrFieldIds));
            }

            lines.addAll(comment2("\tinstructions for method " + methodId));
            for (Fact methodInstructionFact:idb.methodInstructionFacts.getOrDefault(methodId, Set.of())) {
                boolean isRemovedInstruction = methodInstructionFact.predicate().getName().equals("REMOVED_INSTRUCTION");
                boolean skip = isRemovedInstruction && !includeRemoved;
                if (!skip) {
                    lines.add(stringifyBytecodeInstructionFact(methodInstructionFact, includeProvenance, includeInstructionCounters, includeClassOrMethodOrFieldIds));
                }
            }
        }

        Files.write(out, lines);
        LOG.info("output written to {}", out);

    }

    private static String stringifyOtherFact(Fact fact,boolean includeProvenance) {
        Set slotsToSkip = new HashSet();
        if (!includeProvenance) slotsToSkip.add(0);
        return stringify(fact,slotsToSkip);
    }

    private static String stringifyMemberFact(Fact fact,boolean includeProvenance,boolean includeClassOrMethodOrFieldIds) {
        Set slotsToSkip = new HashSet();
        if (!includeProvenance) slotsToSkip.add(0);
        if (!includeClassOrMethodOrFieldIds) slotsToSkip.add(1);
        return stringify(fact,slotsToSkip);
    }

    private static String stringifyBytecodeInstructionFact(Fact fact,boolean includeProvenance,boolean includeInstructionCounters,boolean includeClassOrMethodOrFieldIds) {
        Set slotsToSkip = new HashSet();
        if (!includeProvenance) slotsToSkip.add(0);
        if (!includeClassOrMethodOrFieldIds) slotsToSkip.add(1);
        if (!includeInstructionCounters) slotsToSkip.add(2);
        return stringify(fact,slotsToSkip);
    }

    private static String stringify(Fact fact,Set<Integer> slotsToSkip) {

        List<String> values = new ArrayList<>(fact.values().length);
        for (int i = 0; i < fact.values().length; i++) {
            String value = String.valueOf(fact.values()[i]);
            if (!slotsToSkip.contains(i)) {
                values.add(value);
            }
        }
        return fact.predicate().getName() + "\t" + values.stream().collect(Collectors.joining("\t", "\t", ""));

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
