package io.github.bineq.daleq.cli;

import java.util.function.Predicate;

/**
 * An enum of file types an analyser can compare.
 * @author jens dietrich
 */
public enum AnalysedResourceType {

    Any("any", s -> true),
    JavaByteCode("Java Byte Code", s -> s.endsWith(".class")),
    JavaSourceCode("Java Source Code", s -> s.endsWith(".java")),
    JVMSourceCode("Any JVM Language Source Code",
        s -> s.endsWith(".java") ||
            s.endsWith(".kt") ||
            s.endsWith(".kts") ||
            s.endsWith(".scala") ||
            s.endsWith(".groovy") ||
            s.endsWith(".gvy") ||
            s.endsWith(".gy")
    ),
    Properties("Properties",
        s ->  s.endsWith(".settings") ||
            s.endsWith(".mf") ||
            s.endsWith(".manifest") ||
            s.endsWith(".properties") ||
            s.endsWith(".config")
    ),
    Text("Text",
        Properties.getFilePatterns().or(
        s -> s.endsWith(".txt") ||
            s.endsWith(".csv") ||
            s.endsWith(".tsv") ||
            s.endsWith(".json") ||
            s.endsWith(".yml") ||
            s.endsWith(".yaml") ||
            s.endsWith(".xml") ||
            s.endsWith(".xsd") ||
            s.endsWith(".g4")
        )
    ),
    PythonSourceCode("Python Source Code",
        s -> s.endsWith(".txt") ||
            s.endsWith(".csv") ||
            s.endsWith(".tsv") ||
            s.endsWith(".json") ||
            s.endsWith(".yml") ||
            s.endsWith(".yaml") ||
            s.endsWith(".xml") ||
            s.endsWith(".xsd") ||
            s.endsWith(".g4") ||
            s.endsWith(".settings") ||
            s.endsWith(".mf") ||
            s.endsWith(".manifest") ||
            s.endsWith(".properties") ||
            s.endsWith(".config")
    ),
    ;

    private final String name;
    private final Predicate<String> filePatterns;

    AnalysedResourceType(String name, Predicate<String> filePatterns) {
        this.name = name;
        this.filePatterns = filePatterns;
    }

    public String getName() {
        return name;
    }


    public Predicate<String> getFilePatterns() {
        return filePatterns;
    }
}
