package io.github.bineq.daleq.factextraction;

import org.apache.commons.cli.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Extracts facts from bytecode.
 * Facts are stored in map associating table (predicate) names with records.
 * @author jens dietrich
 */
public class FactExtractor   {

    // sort members to make order predictable
    public static final Comparator<FieldNode> FIELD_COMP = Comparator.comparing(fn -> fn.name);
    public static final Comparator<MethodNode> METHOD_NODE_COMPARATOR = Comparator.comparing(mn -> mn.name + mn.desc);

    public static final Logger LOG = LoggerFactory.getLogger(FactExtractor.class);

    public static Option OPT_CLASSLOC = Option.builder()
        .argName("classes")
        .option("cl")
        .hasArg()
        .required(true)
        .desc("the location of compiled classes, a jar file or folder")
        .build();

    public static Option OPT_DB = Option.builder()
        .argName("database")
        .option("db")
        .hasArg()
        .required(true)
        .desc("a folder where to create the database, the folder will be created if it does not exist")
        .build();

    public static void main(String[] args) throws IOException {

        Options options = new Options();
        options.addOption(OPT_CLASSLOC);
        options.addOption(OPT_DB);
        CommandLine cli = null;
        CommandLineParser parser = new DefaultParser();

        try {
            cli = parser.parse(options, args);
            String dbFolderName = cli.getOptionValue(OPT_DB);
            String classLocation = cli.getOptionValue(OPT_CLASSLOC);

            extractAndExport(Path.of(classLocation),Path.of(dbFolderName));

        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -cp <path-to-built-jar> " + FactExtractor.class.getName(), options);
            e.printStackTrace();
            System.exit(1);
        }
    }

    static void extractAndExport (Path classPath, Path dbDir) throws IOException {

        LOG.info("extracting classes from {}", classPath);
        List<Path> classFiles = Utils.getClassFiles(classPath);
        LOG.info("{} class files extracted", classFiles.size());

        List<Fact> allFacts = new ArrayList<>();
        List<Fact> currentFacts = new ArrayList<>();

        for (Path classFile : classFiles) {
            currentFacts.clear();
            byte[] bytes = Files.readAllBytes(classFile);
            currentFacts = extract(bytes);
            LOG.info("reading {}",classFile);
            LOG.info("{} facts created",currentFacts.size());
            allFacts.addAll(currentFacts);
        }

        if (!Files.exists(dbDir)) {
            LOG.info("creating folder: {}", dbDir);
            Files.createDirectories(dbDir);
        }

        // write db

        // sort facts by predicate
        Map<Predicate,List<Fact>> factsByPredicate = new HashMap<>();
        for (Fact fact:allFacts) {
            Predicate predicate = fact.predicate();
            List<Fact> facts = factsByPredicate.computeIfAbsent(predicate, k -> new ArrayList<>());
            facts.add(fact);
        }

        // lines of predicate definitions and imports
        List<String> dbMain = new ArrayList<>();
        for (Predicate predicate : factsByPredicate.keySet()) {
            dbMain.add(predicate.asSouffleDecl());
            dbMain.add(predicate.asSouffleFactImportStatement());
            dbMain.add("");

            List<Fact> facts = factsByPredicate.get(predicate);
            List<String> factRecords = facts.stream()
                .map(fact -> fact.asSouffleFact())
                .collect(Collectors.toUnmodifiableList());

            Files.write(dbDir.resolve(predicate.asSouffleFactFileNameWithExtension()), factRecords);
            LOG.info("facts written to: {}",predicate.asSouffleFactFileNameWithExtension() );
        }

        String dbName = "database.souffle";
        Files.write(dbDir.resolve(dbName),dbMain);
        LOG.info("databse definition written to: {}",dbName);

    }

    static List<Fact> extract (byte[] bytes) throws IOException {
        ClassNode classNode = new ClassNode();
        new ClassReader(bytes).accept(classNode, 0);
        List<Fact> facts = new ArrayList<>();

        facts.add(new SimpleFact(Predicate.SUPERCLASS, classNode.name, classNode.superName));
        for (String intrf : classNode.interfaces) {
            facts.add(new SimpleFact(Predicate.INTERFACE, classNode.name, intrf));
        }

        facts.add(new SimpleFact(Predicate.VERSION,classNode.name,classNode.version));

        // fields
        classNode.fields.stream().sorted((FIELD_COMP)).forEach(fieldNode -> {
            String fieldId = classNode.name + "::" +  fieldNode.name +  fieldNode.desc;
            facts.add(new SimpleFact(Predicate.FIELD, fieldId,classNode.name, fieldNode.name,fieldNode.desc));
            facts.add(new SimpleFact(Predicate.FIELD_SIGNATURE, fieldId, fieldNode.signature));
        });

        // methods
        classNode.methods.stream().sorted((METHOD_NODE_COMPARATOR)).forEach(methodNode -> {
            String methodId = classNode.name + "::" +  methodNode.name +  methodNode.desc;
            facts.add(new SimpleFact(Predicate.METHOD, methodId, classNode.name, methodNode.name, methodNode.desc));
            facts.add(new SimpleFact(Predicate.METHOD_SIGNATURE,methodId, methodNode.signature));
            //AtomicInteger line = new AtomicInteger(-1);
            
            methodNode.instructions.forEach(instructionNode -> {
                //  if (instructionNode instanceof LineNumberNode) {
                //      line.set(((LineNumberNode) instructionNode).line);
                //  }

                // TODO: label nodes, frame nodes, line number nodes
                int opCode = instructionNode.getOpcode();
                String instr = InstructionTable.getInstruction(opCode);
                if (instr == null) {
                    LOG.warn("unknown instruction type found, opcode is {}", opCode);
                }
                else {
                    facts.add(new SimpleFact(Predicate.INSTRUCTION, methodId, instr));
                    if (instructionNode instanceof FieldInsnNode fInsNode) {
                        facts.add(new SimpleFact(Predicate.FIELD_INS, methodId,fInsNode.name, fInsNode.desc,instr));
                    }
                    else  {
                        LOG.warn("TODO: create detailed fact for instruction {} , node type {}",instr,instructionNode.getClass());
                    }
                }
            });

            // TODO annotations


        });
        return facts;
    }
}
