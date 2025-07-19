package io.github.bineq.daleq.edb;

import io.github.bineq.daleq.Fact;
import io.github.bineq.daleq.SimpleFact;
import io.github.bineq.daleq.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InstructionFactFactoryCodeGenerator {

    public static final String PACKAGE_NAME = "io.github.bineq.daleq.edb.instruction_fact_factories";
    public static final Path DESTINATION = Path.of("inferred-instructions-fact-factories");
    public static final Logger LOG = LoggerFactory.getLogger(InstructionFactFactoryCodeGenerator.class);
    public static final String FACTORY_INTERFACE = InstructionPredicateFactFactory.class.getName();
    public static final String INSTRUCTION_PREDICATE = EDBInstructionPredicate.class.getName();
    public static final String SIMPLE_FACT = SimpleFact.class.getName();
    public static final String FACT = Fact.class.getName();
    public static final String FACT_EXTRACTOR = FactExtractor.class.getName();
    public static final String INSTRUCTION_PREDICATE_REGISTRY = EDBPredicateRegistry.class.getName() + ".INSTRUCTION_PREDICATES";

    // for ISO 8601 timestamps
    public static final TimeZone TIME_ZONE = TimeZone.getTimeZone("UTC");
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    static {
        DATE_FORMAT.setTimeZone(TIME_ZONE);
    }

    public static void main(String[] args) throws IOException {

        Map<Integer, EDBInstructionPredicate> REGISTRY = EDBPredicateRegistry.INSTRUCTION_PREDICATES;
        LOG.info("generating fact factories for {} instructions", REGISTRY.size());

        if (!Files.exists(DESTINATION)) {
            Files.createDirectories(DESTINATION);
        }

        List<String> generatedClasses = new ArrayList<>();

        for (Map.Entry<Integer, EDBInstructionPredicate> entry : REGISTRY.entrySet()) {
            EDBInstructionPredicate predicate = entry.getValue();
            assert predicate.getOpCode() == entry.getKey();
            int opCode = entry.getKey();

            String className = "InstructionFactFactory__" + predicate.getName();
            String qClassName = PACKAGE_NAME + "." + className;
            String classFileName = className + ".java";
            Path classDir = DESTINATION.resolve(PACKAGE_NAME.replace('.','/'));
            if (!Files.exists(classDir)) {
                Files.createDirectories(classDir);
            }
            Path classFile = classDir.resolve(classFileName);

            List<String> lines = new ArrayList<>();
            lines.add("package " + PACKAGE_NAME + ";");
            lines.add("");
            lines.add("import javax.annotation.processing.Generated;");
            lines.add("import java.util.Map;");
            lines.add("import org.objectweb.asm.tree.LabelNode;");
            lines.add("import " + FACT + ';');
            lines.add("");
            lines.add("@Generated(value=\""+ InstructionFactFactoryCodeGenerator.class.getName() + "\", date= \"" + getTimestamp()+ "\",\n" + "      comments= \"factory generated from ASM tree API nodes\")");
            lines.add("public class " + className + " implements " + FACTORY_INTERFACE + "<" + predicate.getAsmNodeType() + "> {");

            lines.add("");
            lines.add("    @Override public String getVersion() {");
            lines.add("        return \"" + predicate.getId() + "\";");
            lines.add("    }");

            lines.add("");
            lines.add("    @Override public " + INSTRUCTION_PREDICATE + " getPredicate() {");
            lines.add("        return io.github.bineq.daleq.edb.EDBPredicateRegistry.INSTRUCTION_PREDICATES.get("+ opCode + ");");
            lines.add("    }");

            lines.add("");
            lines.add("    @Override public Fact createFact(String " + Fact.ID_SLOT_NAME + "," +  predicate.getAsmNodeType() + " node,String methodRef,int instructionCounter,io.github.bineq.daleq.edb.FactExtractor.LabelMap labelMap) {");
            Object values = IntStream.range(0,predicate.getSlots().length)
                .mapToObj(i -> generateCode(i,predicate.getSlots()[i]))
                .collect(Collectors.joining(",","new Object[]{","}"));
            lines.add("        return new " + SIMPLE_FACT + "(" + INSTRUCTION_PREDICATE_REGISTRY + ".get("+ entry.getKey() + ")," + values + ");");
            lines.add("    }");

            lines.add("");
            lines.add("};");
            Files.write(classFile, lines);
            LOG.info("created file {}", classFile);

            generatedClasses.add(qClassName);

        }

        Path serviceRegistryFile = DESTINATION.resolve(FACTORY_INTERFACE);
        List<String> lines = generatedClasses.stream().collect(Collectors.toList());
        Files.write(serviceRegistryFile, lines);
        LOG.info("generated service registry (copy to resources/services): {}",serviceRegistryFile);
    }

    private static String generateCode(int i, Slot s) {
        if (i==0) {
            assert s.name().equals("factid");
            return "factid";
        }
        if (i==1) {
            assert s.name().equals("methodref");
            return "methodRef";
        }
        else if (i==2) {
            assert s.name().equals("instructioncounter");
            return "instructionCounter";
        }
        else {
            if (s.jtype().equals("org.objectweb.asm.tree.LabelNode")) {
                return "labelMap.get(node." + s.name()+")";
            }
            else {
                return "node." + s.name();
            }
        }
    }

    private static String getTimestamp() {
        return DATE_FORMAT.format(new Date());
    }
}
