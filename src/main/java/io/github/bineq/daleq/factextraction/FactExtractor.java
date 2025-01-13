package io.github.bineq.daleq.factextraction;

import com.google.common.base.Preconditions;
import org.apache.commons.cli.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.tree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
    public static final String INFERRED_INSTRUCTION_PREDICATE_SPECS = "inferred-instruction-predicates";

    public static final Map<Integer,InstructionPredicate> REGISTRY = new HashMap<>();
    public static final Map<Integer,InstructionPredicateFactFactory> FACT_FACTORIES = new HashMap<>();

    static {
        LOG.info("Loading instruction predicate registry");
        URL folder = InstructionPredicate.class.getResource("/instruction-predicates");
        assert folder != null;
        File dir = new File(folder.getPath());
        File[] files = dir.listFiles(f -> f.getName().endsWith(".json"));
        LOG.info("{} instruction predicates found", files.length);
        for (File file : files) {
            try {
                InstructionPredicate predicate = InstructionPredicate.fromJson(file);
                int opCode = predicate.getOpCode();
                if (REGISTRY.containsKey(opCode)) {
                    LOG.warn("Duplicate instruction predicate for op code {}", opCode);
                }
                REGISTRY.put(opCode, predicate);
            }
            catch (Exception x) {
                LOG.error("Failed to load instruction predicate from " + file.getAbsolutePath(), x);
            }
        }
        LOG.info(""+REGISTRY.size() + " instruction predicates loaded");

        LOG.info("Loading instruction predicate fact factories");
        ServiceLoader<InstructionPredicateFactFactory> factories = ServiceLoader.load(InstructionPredicateFactFactory.class);
        for (InstructionPredicateFactFactory factory : factories) {
            InstructionPredicate predicate = factory.getPredicate();
            try {
                factory.verify();
            }
            catch (Exception x) {
                LOG.error("Failed to verify fact factory", x);
                System.exit(1);
            }
            FACT_FACTORIES.put(predicate.getOpCode(), factory);
        }
        LOG.info(""+FACT_FACTORIES.size() + " instruction predicate fact factories loaded");
    }

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

    public static Option OPT_VERIFY = Option.builder()
        .argName("verify")
        .option("v")
        .hasArg(false)
        .required(false)
        .desc("whether to verify the datalog facts against the schema, must be true or false, default is true")
        .build();

    public static void main(String[] args) throws Exception {

        Options options = new Options();
        options.addOption(OPT_CLASSLOC);
        options.addOption(OPT_DB);
        options.addOption(OPT_VERIFY);
        CommandLine cli = null;
        CommandLineParser parser = new DefaultParser();

        try {
            cli = parser.parse(options, args);
            String dbFolderName = cli.getOptionValue(OPT_DB);
            String classLocation = cli.getOptionValue(OPT_CLASSLOC);
            boolean verify = true;
            if (cli.hasOption(OPT_VERIFY)) {
                verify = Boolean.valueOf(cli.getOptionValue(OPT_VERIFY));
            }

            extractAndExport(Path.of(classLocation),Path.of(dbFolderName),verify);

        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -cp <path-to-built-jar> " + FactExtractor.class.getName(), options);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void extractAndExport (Path classPath, Path dbDir, boolean verify) throws Exception {

        LOG.info("extracting classes from {}", classPath);
        List<Path> classFiles = Utils.getClassFiles(classPath);
        LOG.info("{} class files extracted", classFiles.size());

        List<Fact> allFacts = new ArrayList<>();
        List<Fact> currentFacts = new ArrayList<>();

        for (Path classFile : classFiles) {
            currentFacts.clear();
            byte[] bytes = Files.readAllBytes(classFile);
            currentFacts = extract(bytes,verify);
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

            Path factFile = dbDir.resolve(predicate.asSouffleFactFileNameWithExtension());
            Files.write(factFile, factRecords);
            LOG.info("facts written to: {}",factFile.toFile().getAbsolutePath());
        }

        String dbName = "database.souffle";
        Path rulesFile = dbDir.resolve(dbName);
        Files.write(rulesFile,dbMain);
        LOG.info("database definition written to: {}",rulesFile.toFile().getAbsolutePath());

    }

    static String getMethodReference(String className, String methodName,String descriptor) {
        return className + "::" + methodName + descriptor;
    }

    static String getFieldReference(String className, String fieldName,String descriptor) {
        return className + "::" + fieldName + descriptor;
    }

    public static List<Fact> extract (byte[] bytes, boolean verify) throws VerificationException {
        ClassNode classNode = new ClassNode();
        new ClassReader(bytes).accept(classNode, 0);
        List<Fact> facts = new ArrayList<>();

        facts.add(new SimpleFact(AdditionalPredicates.SUPERCLASS, classNode.name, classNode.superName));
        for (String intrf : classNode.interfaces) {
            facts.add(new SimpleFact(AdditionalPredicates.INTERFACE, classNode.name, intrf));
        }

        facts.add(new SimpleFact(AdditionalPredicates.VERSION,classNode.name,classNode.version));

        // fields
        classNode.fields.stream().sorted((FIELD_COMP)).forEach(fieldNode -> {
            String fieldId = getFieldReference(classNode.name,fieldNode.name,fieldNode.desc);
            facts.add(new SimpleFact(AdditionalPredicates.FIELD, fieldId,classNode.name, fieldNode.name,fieldNode.desc));
            facts.add(new SimpleFact(AdditionalPredicates.FIELD_SIGNATURE, fieldId, fieldNode.signature));
        });

        // methods
        classNode.methods.stream().sorted((METHOD_NODE_COMPARATOR)).forEach(methodNode -> {
            AtomicInteger instructionCounter = new AtomicInteger(0);
            String methodId = getMethodReference(classNode.name,methodNode.name,methodNode.desc);
            facts.add(new SimpleFact(AdditionalPredicates.METHOD, methodId, classNode.name, methodNode.name, methodNode.desc));
            facts.add(new SimpleFact(AdditionalPredicates.METHOD_SIGNATURE,methodId, methodNode.signature));
            //AtomicInteger line = new AtomicInteger(-1);

            methodNode.instructions.forEach(instructionNode -> {
                // TODO deal with pseudo nodes
                //  if (instructionNode instanceof LineNumberNode) {
                //      line.set(((LineNumberNode) instructionNode).line);
                //  }

                // TODO: label nodes, frame nodes, line NUMBER nodes
                int opCode = instructionNode.getOpcode();
                String instr = InstructionTable.getInstruction(opCode);
                if (instr == null) {
                    LOG.warn("unknown instruction type found, opcode is {}", opCode);
                }
                else {
                    int instCounter = instructionCounter.incrementAndGet();

                    // find predicate
                    InstructionPredicate predicate = findPredicate(opCode,instr,instructionNode.getClass());
                    assert predicate != null;

                    // create fact
                    Fact fact = createFact(predicate,instCounter,methodId,instructionNode);
                    assert fact != null;

                    facts.add(fact);
                }
            });

            // TODO annotations


        });


        if (verify) {
            LOG.info("verifying facts");
            for (Fact fact : facts) {
                fact.verify();
            }
        }
        return facts;
    }

    private static Fact createFact(InstructionPredicate predicate, int instCounter, String methodId, AbstractInsnNode instructionNode) {
        InstructionPredicateFactFactory factory = FACT_FACTORIES.get(predicate.getOpCode());
        Preconditions.checkNotNull(factory,"no fact factory found for instruction " + predicate.getName());
        return factory.createFact(instructionNode,methodId,instCounter);
    }

    private static InstructionPredicate findPredicate(int opCode, String instr, Class<? extends AbstractInsnNode> aClass) {
        InstructionPredicate predicate = REGISTRY.get(opCode);
        if (predicate == null) {
            // use reflection to construct predicate
            predicate = new InstructionPredicate();
            predicate.setId(createUUID());
            predicate.setOpCode(opCode);
            predicate.setName(instr);
            predicate.setAsmNodeType(aClass.getName());
            List<Slot> slots = new ArrayList<>();
            slots.add(Slot.symslot("methodid"));
            slots.add(Slot.numslot("instructioncounter",Integer.TYPE.getName()));

            // TODO add additional slots from inspecting asm node class for properties
            try {
                Field[] fields = aClass.getDeclaredFields();
                Arrays.sort(fields,Comparator.comparing(Field::getName));
                for (Field field : fields) {
                    if (Modifier.isPublic(field.getModifiers())) {
                        Class cl = field.getType();
                        SlotType slotType = null;
                        String jSlotType = getClassName(cl);
                        boolean addSingle = true;

                        // deep access for properties of special types
                        if (cl== Handle.class) {
                            addSingle = false;
                            slots.add(new Slot(field.getName()+".getOwner()", SlotType.SYMBOL, String.class.getName()));
                            slots.add(new Slot(field.getName()+".getName()", SlotType.SYMBOL, String.class.getName()));
                            slots.add(new Slot(field.getName()+".getDesc()", SlotType.SYMBOL, String.class.getName()));
                            slots.add(new Slot(field.getName()+".getTag()", SlotType.NUMBER, Integer.TYPE.getName()));
                            slots.add(new Slot(field.getName()+".isInterface()", SlotType.NUMBER, Boolean.TYPE.getName()));
                        }
                        else if (Number.class.isAssignableFrom(cl)) {
                            slotType = SlotType.NUMBER;
                        }
                        else if (Boolean.class.isAssignableFrom(cl)) {
                            slotType = SlotType.NUMBER;
                        }
                        else if (String.class.isAssignableFrom(cl)) {
                            slotType = SlotType.SYMBOL;
                        }
                        else if (cl == Integer.TYPE || cl == Long.TYPE || cl == Short.TYPE || cl == Byte.TYPE || cl == Boolean.TYPE ) {
                            slotType = SlotType.NUMBER;
                        }
                        else if (cl == Double.TYPE || cl == Float.TYPE ) {
                            slotType = SlotType.FLOAT;
                        }
                        else if (cl.isArray()) {
                            slotType = SlotType.SYMBOL; // serialized as list
                        }
                        else {
                            LOG.warn("Mapping slot type {} to SYMBOL",cl);
                            slotType = SlotType.SYMBOL;
                        }
                        if (addSingle) {
                            Slot slot = new Slot(field.getName(), slotType, jSlotType);
                            slots.add(slot);
                        }
                    }
                }

            }
            catch (Exception x) {
                LOG.error("Error inferring slots for class "+aClass.getName(),x);
            }


            predicate.setSlots(slots.stream().toArray(Slot[]::new));

            // make persistent
            File dir = new File(INFERRED_INSTRUCTION_PREDICATE_SPECS);
            if (!dir.exists()) {
                dir.mkdirs();
                LOG.info("Created folder for inferred instructiin predicate specs: {}", dir.getAbsolutePath());
            }
            File spec = new File(dir,instr+".json");
            try {
                predicate.toJson(spec);
                LOG.info("Written inferred instruction predicate spec to file {}, verify and copy to resources to use in the future", spec.getAbsolutePath());
            } catch (IOException x) {
                LOG.error("Error writing inferred instruction predicate spec file", x);
            }
        }
        return predicate;
    }

    private static String getClassName(Class cl) {
        if (cl.isArray()) {
            return getClassName(cl.getComponentType())+"[]";
        }
        else return cl.getName();

    }


    private static String createUUID() {
        return UUID.randomUUID().toString();
    }
}
