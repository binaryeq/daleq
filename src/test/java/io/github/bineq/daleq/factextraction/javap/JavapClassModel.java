package io.github.bineq.daleq.factextraction.javap;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Representation of the output of javap -c -p on a class file.
 * @author jens dietrich
 */
public class JavapClassModel {

    public static final Predicate<String> IS_METHOD_OR_CONSTRUCTOR_DEF = Pattern.compile(".*\\(.*\\);").asMatchPredicate();
    public static final Predicate<String> IS_INSTRUCTION = Pattern.compile("\\d+:\\s+(\\w|\\d|\\_)+(\\s+#\\d+)?(\\s+//\\s*.*)?").asMatchPredicate();
    public static final Predicate<String> IS_LABEL= Pattern.compile("\\d+:").asMatchPredicate();
    public static final Predicate<String> IS_INSTRUCTION_NAME= Pattern.compile("(\\w|\\d|\\_)+").asMatchPredicate();
    public static final Predicate<String> IS_CONSTANT_POOL_REF= Pattern.compile("#\\d+").asMatchPredicate();
    public static final Predicate<String> IS_VALUE= s -> s.startsWith("//");

    public static final Pattern METHOD_OR_CONSTRUCTOR_NAME = Pattern.compile("(\\w|\\d|\\_|\\$|\\.)*\\(.*\\);");


    private List<JavapMethodModel> methods = new ArrayList<>();
    private String name = null;

    public static JavapClassModel parse(String className, Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        JavapClassModel classModel = new JavapClassModel();
        classModel.name = className;
        JavapMethodModel methodModel = new JavapMethodModel();
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {}
            else if (line.startsWith("Compiled from ")) {}
            else if (IS_METHOD_OR_CONSTRUCTOR_DEF.test(line)) {
                // method or constructor
                String methodName = null;
                Matcher matcher = METHOD_OR_CONSTRUCTOR_NAME.matcher(line);
                if (matcher.find()) {
                    methodName = matcher.group(0);
                    methodName = methodName.substring(0,methodName.indexOf('('));
                    methodModel = new JavapMethodModel();
                    methodModel.setName(methodName.equals(className)?"<init>":methodName);
                    classModel.methods.add(methodModel);
                }
                else {
                    assert false:"method name not found in line " + line;
                }
            }
            else if (line.equals("Code:")) {}
            else if (IS_INSTRUCTION.test(line)) {
                String[] tokens = line.split("\s+");
                assert tokens.length <= 7;
                assert tokens.length >= 2;

                assert IS_LABEL.test(tokens[0]);
                int label = Integer.parseInt(tokens[0].substring(0, tokens[0].length()-1));

                assert IS_INSTRUCTION_NAME.test(tokens[1]);
                String instr = tokens[1];

                int constPoolRef = -1;
                if (tokens.length>=3) {
                    assert IS_CONSTANT_POOL_REF.test(tokens[2]);
                    constPoolRef = Integer.parseInt(tokens[2].substring(1));
                }

                String value = null;
                if (tokens.length>=4) {
                    assert tokens[3].equals("//");
                    assert tokens[4].equals("Method") || tokens[4].equals("Field") || tokens[4].equals("String");
                    // value is more complex, as string literals might have whitespaces that have been tokenised
                    value = line.substring(line.indexOf(tokens[5],line.indexOf("//")));
                }
                JavapInstructionModel instructionModel = new JavapInstructionModel(label,instr,constPoolRef,value);
                methodModel.getInstructions().add(instructionModel);
            }
        }

        return classModel;
    }

    public List<JavapMethodModel> getMethods() {
        return methods;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavapClassModel that = (JavapClassModel) o;
        return Objects.equals(methods, that.methods) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methods, name);
    }
}
