package io.github.bineq.daleq.factextraction;

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
import java.util.stream.Stream;

public class InstructionFactFactoryCodeGenerator {

    public static final String PACKAGE_NAME = "io.github.bineq.daleq.factextraction.instruction_fact_factories";
    public static final Path DESTINATION = Path.of("inferred-instructions-fact-factories");
    public static final Logger LOG = LoggerFactory.getLogger(InstructionFactFactoryCodeGenerator.class);
    public static final String FACTORY_INTERFACE = InstructionPredicateFactFactory.class.getName();
    public static final String INSTRUCTION_PREDICATE = InstructionPredicate.class.getName();

    // for ISO 8601 timestamps
    public static final TimeZone TIME_ZONE = TimeZone.getTimeZone("UTC");
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    static {
        DATE_FORMAT.setTimeZone(TIME_ZONE);
    }

    public static void main(String[] args) throws IOException {

        Map<Integer,InstructionPredicate> REGISTRY = FactExtractor.REGISTRY;
        LOG.info("generating fact factories for {} instructions", REGISTRY.size());

        if (!Files.exists(DESTINATION)) {
            Files.createDirectories(DESTINATION);
        }

        List<String> generatedClasses = new ArrayList<>();

        for (Map.Entry<Integer,InstructionPredicate> entry : REGISTRY.entrySet()) {
            InstructionPredicate predicate = entry.getValue();
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
            lines.add("");
            lines.add("@Generated(value=\""+ InstructionFactFactoryCodeGenerator.class.getName() + "\", date= \"" + getTimestamp()+ "\",\n" + "      comments= \"factory generated from ASM tree API nodes\")");
            lines.add("public class " + className + " implements " + FACTORY_INTERFACE + "<" + predicate.getAsmNodeType() + "> {");

            lines.add("");
            lines.add("    @Override public String getVersion() {");
            lines.add("        return \"" + predicate.getId() + "\";");
            lines.add("    }");

            lines.add("");
            lines.add("    @Override public " + INSTRUCTION_PREDICATE + " getPredicate() {");
            lines.add("        return " + FactExtractor.class.getName() +".REGISTRY.get("+ opCode + ");");
            lines.add("    }");

            lines.add("");
            lines.add("    @Override public String createFact(" +  predicate.getAsmNodeType() + " node,String methodRef,int instructionCounter) {");
            String terms = IntStream.range(0,predicate.getSlots().length)
                .mapToObj(i -> generateCode(i,predicate.getSlots()[i]))
                .collect(Collectors.joining(" + \'\\t\' + "));
            lines.add("        return " + terms+ ";");
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

    private static String generateCode(int i,Slot s) {
        if (i==0) {
            return "methodRef";
        }
        else if (i==1) {
            return "String.valueOf(instructionCounter)";
        }
        else {
            return "String.valueOf(node." + s.name() + ')';  // TODO finetuning, support for particular types
        }
    }

    private static String getTimestamp() {
        return DATE_FORMAT.format(new Date());
    }
}
