package io.github.bineq.daleq.factextraction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class InstructionFactFactoryCodeGenerator {

    public static final String PACKAGE_NAME = "io.github.bineq.daleq.factextraction.instruction_fact_factories";
    public static final Path DESTINATION = Path.of("inferred-instructions-fact-factories");
    public static final Logger LOG = LoggerFactory.getLogger(InstructionFactFactoryCodeGenerator.class);

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

        for (Map.Entry<Integer,InstructionPredicate> entry : REGISTRY.entrySet()) {
            InstructionPredicate predicate = entry.getValue();
            assert predicate.getOpCode() == entry.getKey();
            int opCode = entry.getKey();

            String className = "InstructionFactFactory__" + predicate.getName();
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
            lines.add("public class " + className + " implements InstructionFactFactory<" + predicate.getAsmNodeType() + "> {");

            lines.add("");
            lines.add("    @Override String getVersion() {");
            lines.add("        return \"" + predicate.getId() + "\";");
            lines.add("    }");

            lines.add("");
            lines.add("    @Override InstructionPredicate getPredicate() {");
            lines.add("        return " + FactExtractor.class.getName() +".REGISTRY.get("+ opCode + ");");
            lines.add("    }");

            lines.add("");
            lines.add("    @Override String createFact(" +  predicate.getAsmNodeType() + " node) {");
            lines.add("        // TODO");
            lines.add("    }");

            lines.add("");
            lines.add("};");
            Files.write(classFile, lines);
            LOG.info("created file {}", classFile);

        }
    }

    private static String getTimestamp() {

        return DATE_FORMAT.format(new Date());
    }
}
