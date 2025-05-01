package io.github.bineq.daleq.edb;

import com.google.common.base.Preconditions;
import io.github.bineq.daleq.*;
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

    public static final Map<Integer,InstructionPredicateFactFactory> FACT_FACTORIES = new HashMap<>();

    // by inserting gaps, rules can insert additional instructions
    public static final int INSTRUCTION_COUNTER_STEP_SIZE = 100;

    private static class HashMapWthKeyAccessRecording<K,V> extends HashMap<K,V> {
        Set<K> accessedKeys = new HashSet<>();

        @Override
        public V get(Object key) {
            accessedKeys.add((K) key);
            return super.get(key);
        }

        @Override
        public V getOrDefault(Object key, V defaultValue) {
            accessedKeys.add((K) key);
            return super.getOrDefault(key, defaultValue);
        }
    }

    static {

        LOG.info("Loading instruction predicate fact factories");
        ServiceLoader<InstructionPredicateFactFactory> factories = ServiceLoader.load(InstructionPredicateFactFactory.class);
        for (InstructionPredicateFactFactory factory : factories) {
            EBDInstructionPredicate predicate = factory.getPredicate();
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
        .argName("souffle")
        .option("s")
        .hasArg()
        .required(true)
        .desc("a file where to create the souffle program (.souffle file) containing imports and input predicate declarations")
        .build();

    public static Option OPT_FACTS = Option.builder()
        .argName("facts")
        .option("f")
        .hasArg()
        .required(true)
        .desc("a folder where to create the extension database (input .facts files), the folder will be created if it does not exist")
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
        options.addOption(OPT_FACTS);
        options.addOption(OPT_VERIFY);
        CommandLine cli = null;
        CommandLineParser parser = new DefaultParser();

        try {
            cli = parser.parse(options, args);
            String edbDefFileName = cli.getOptionValue(OPT_DB);
            String factsFolderName = cli.getOptionValue(OPT_FACTS);
            String classLocation = cli.getOptionValue(OPT_CLASSLOC);
            boolean verify = true;
            if (cli.hasOption(OPT_VERIFY)) {
                verify = Boolean.valueOf(cli.getOptionValue(OPT_VERIFY));
            }

            extractAndExport(Path.of(classLocation),Path.of(edbDefFileName),Path.of(factsFolderName),verify);

        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -cp <path-to-built-jar> " + FactExtractor.class.getName(), options);
            e.printStackTrace();
            System.exit(1);
        }
    }


    public static void extractAndExport (Path classPath, Path dbDef, Path factDir, boolean verify) throws Exception {

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

        if (!Files.exists(factDir)) {
            LOG.info("creating folder: {}", factDir);
            Files.createDirectories(factDir);
        }
        if (!Files.exists(dbDef.getParent())) {
            LOG.info("creating folder: {}", dbDef.getParent());
            Files.createDirectories(dbDef.getParent());
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

        // generate fact files
        for (Predicate predicate : EDBPredicateRegistry.ALL) {
            List<Fact> facts = factsByPredicate.get(predicate);
            if (facts == null) {
                facts = new ArrayList<>();
            }
            List<String> factRecords = facts.stream()
                .map(fact -> fact.asSouffleFact())
                .collect(Collectors.toList());

            // comments makes sure that the fact file is written even if there are no facts
            // factRecords.add(0,"// facts for predicate " + predicate.getName());

            Path factFile = factDir.resolve(predicate.asSouffleFactFileNameWithExtension());
            Files.write(factFile, factRecords);
            LOG.debug("facts written to: {}",factFile.toFile().getAbsolutePath());
        }
        LOG.info("facts written to: {}",factDir.toFile().getAbsolutePath());

        // generate imports for the predicates where we have facts
        // generate declarations for all predicates available (as they are used in rule sets)
        List<String> dbMain = new ArrayList<>();
        for (Predicate predicate : EDBPredicateRegistry.ALL) {
            dbMain.add(predicate.asSouffleDecl());
            dbMain.add(predicate.asSouffleFactImportStatement());
            dbMain.add("");
        }

        Files.write(dbDef,dbMain);
        LOG.info("database definition written to: {}",dbDef.toFile().getAbsolutePath());

    }

    public static String getMethodReference(String className, String methodName,String descriptor) {
        return className + "::" + methodName + descriptor;
    }

    static String getFieldReference(String className, String fieldName,String descriptor) {
        // need to seperate field name and descriptor somehow
        return className + "::" + fieldName + '(' + descriptor;
    }

    public static List<Fact> extract (byte[] bytes, boolean verify) throws VerificationException {
        ClassNode classNode = new ClassNode();
        new ClassReader(bytes).accept(classNode, 0);
        List<Fact> facts = new ArrayList<>();

        facts.add(new SimpleFact(EBDAdditionalPredicates.SUPERCLASS, FactIdGenerator.nextId(EBDAdditionalPredicates.SUPERCLASS),classNode.name, classNode.superName));
        for (String intrf : classNode.interfaces) {
            facts.add(new SimpleFact(EBDAdditionalPredicates.INTERFACE, FactIdGenerator.nextId(EBDAdditionalPredicates.INTERFACE),classNode.name, intrf));
        }

        facts.add(new SimpleFact(EBDAdditionalPredicates.CLASS_SIGNATURE,FactIdGenerator.nextId(EBDAdditionalPredicates.CLASS_SIGNATURE),classNode.name, classNode.signature));
        facts.add(new SimpleFact(EBDAdditionalPredicates.VERSION,FactIdGenerator.nextId(EBDAdditionalPredicates.VERSION),classNode.name,classNode.version));
        facts.add(new SimpleFact(EBDAdditionalPredicates.ACCESS,FactIdGenerator.nextId(EBDAdditionalPredicates.ACCESS),classNode.name,classNode.access));

        // fields
        classNode.fields.stream().sorted((FIELD_COMP)).forEach(fieldNode -> {
            String fieldId = getFieldReference(classNode.name,fieldNode.name,fieldNode.desc);
            facts.add(new SimpleFact(EBDAdditionalPredicates.FIELD,FactIdGenerator.nextId(EBDAdditionalPredicates.FIELD), fieldId,classNode.name, fieldNode.name,fieldNode.desc));
            facts.add(new SimpleFact(EBDAdditionalPredicates.FIELD_SIGNATURE, FactIdGenerator.nextId(EBDAdditionalPredicates.FIELD_SIGNATURE), fieldId, fieldNode.signature));
            facts.add(new SimpleFact(EBDAdditionalPredicates.ACCESS,FactIdGenerator.nextId(EBDAdditionalPredicates.ACCESS),fieldId,fieldNode.access));
        });

        // methods
        classNode.methods.stream().sorted((METHOD_NODE_COMPARATOR)).forEach(methodNode -> {
            AtomicInteger instructionCounter = new AtomicInteger(0);
            String methodId = getMethodReference(classNode.name,methodNode.name,methodNode.desc);
            facts.add(new SimpleFact(EBDAdditionalPredicates.METHOD, FactIdGenerator.nextId(EBDAdditionalPredicates.METHOD),methodId, classNode.name, methodNode.name, methodNode.desc));
            facts.add(new SimpleFact(EBDAdditionalPredicates.METHOD_SIGNATURE,FactIdGenerator.nextId(EBDAdditionalPredicates.METHOD_SIGNATURE),methodId, methodNode.signature));
            facts.add(new SimpleFact(EBDAdditionalPredicates.ACCESS,FactIdGenerator.nextId(EBDAdditionalPredicates.ACCESS),methodId,methodNode.access));

            AtomicInteger labelCounter = new AtomicInteger(-1);

            // first iteration to collect labels
            final HashMapWthKeyAccessRecording<LabelNode,String> labelMap = new HashMapWthKeyAccessRecording<>();
            for (AbstractInsnNode instructionNode:methodNode.instructions) {
                // TODO: label nodes, frame nodes, line NUMBER nodes
                int opCode = instructionNode.getOpcode();
                String instr = InstructionTable.getInstruction(opCode);
                if (instr == null) {
                    if (instructionNode instanceof LabelNode labelNode) {
                        LOG.debug("label: " + labelNode);
                        labelMap.put(labelNode, "L" + labelCounter.incrementAndGet());
                    }
                }
            }

            // second iteration to simulate building facts and check which labels are used
            instructionCounter.set(0); // reset !
            for (AbstractInsnNode instructionNode:methodNode.instructions) {
                // TODO deal with pseudo nodes
                int opCode = instructionNode.getOpcode();
                String instr = InstructionTable.getInstruction(opCode);
                if (instr != null) {
                    int instCounter = instructionCounter.incrementAndGet();

                    EBDInstructionPredicate predicate = findPredicate(opCode,instr,instructionNode.getClass());
                    assert predicate != null;
                    try {
                        createFact(predicate, INSTRUCTION_COUNTER_STEP_SIZE*instCounter, methodId, instructionNode,labelMap);
                        // do not add fact, this is only to see which labels are used by factories !
                    }
                    catch (Exception x) {
                        LOG.error("Fact generation has failed",x);
                    }
                }
            };

            // third iteration to actually build and collect facts
            instructionCounter.set(0); // reset !
            for (AbstractInsnNode instructionNode:methodNode.instructions) {
                // TODO deal with pseudo nodes
                int opCode = instructionNode.getOpcode();
                String instr = InstructionTable.getInstruction(opCode);
                if (instr == null) {
                    if (instructionNode instanceof LabelNode labelNode && labelMap.accessedKeys.contains(labelNode)) {
                        Predicate predicate = EBDAdditionalPredicates.LABEL;
                        String factId = FactIdGenerator.nextId(predicate);
                        int instCounter = instructionCounter.incrementAndGet();
                        //new Object[]{factId,methodRef,instructionCounter,labelMap.get(node.label)});
                        Fact fact = new SimpleFact(predicate,factId,methodId,INSTRUCTION_COUNTER_STEP_SIZE*instCounter,labelMap.get(labelNode));
                        facts.add(fact);
                    }
                    else {
                        LOG.debug("unknown instruction type found, opcode is {}", opCode);
                    }
                }
                else {
                    int instCounter = instructionCounter.incrementAndGet();

                    // find predicate
                    EBDInstructionPredicate predicate = findPredicate(opCode,instr,instructionNode.getClass());
                    assert predicate != null;

                    // create fact
                    try {
                        Fact fact = createFact(predicate, INSTRUCTION_COUNTER_STEP_SIZE*instCounter, methodId, instructionNode,labelMap);
                        //assert fact != null;
                        facts.add(fact);
                    }
                    catch (Exception x) {
                        LOG.error("Fact generation has failed",x);
                    }
                }
            };

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

    private static Fact createFact(EBDInstructionPredicate predicate, int instCounter, String methodId, AbstractInsnNode instructionNode, Map<LabelNode,String> labelMap) {
        InstructionPredicateFactFactory factory = FACT_FACTORIES.get(predicate.getOpCode());
        Preconditions.checkNotNull(factory,"no fact factory found for instruction " + predicate.getName());
        String factId = FactIdGenerator.nextId(predicate);
        return factory.createFact(factId,instructionNode,methodId,instCounter,labelMap);
    }

    private static EBDInstructionPredicate findPredicate(int opCode, String instr, Class<? extends AbstractInsnNode> aClass) {
        EBDInstructionPredicate predicate = EDBPredicateRegistry.INSTRUCTION_PREDICATES.get(opCode);
        if (predicate == null) {
            predicate = new EBDInstructionPredicate();
            predicate.setId(createUUID());
            predicate.setOpCode(opCode);
            predicate.setName(instr);
            predicate.setAsmNodeType(aClass.getName());
            List<Slot> slots = new ArrayList<>();
            slots.add(Slot.symslot(Fact.ID_SLOT_NAME));
            slots.add(Slot.symslot("methodid"));
            slots.add(Slot.numslot("instructioncounter",Integer.TYPE.getName()));

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
                LOG.info("Created folder for inferred instruction predicate specs: {}", dir.getAbsolutePath());
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
