package io.github.bineq.daleq.evaluation;

/**
 * Classification of content.
 * @author jens dietrich
 */
public class ContentClassification {
    public static boolean isSourceCode(String fileName) {
        return isJavaSourceCode(fileName)
            || isKotlinSourceCode(fileName)
            || isScalaSourceCode(fileName)
            || isGroovySourceCode(fileName)
            || isClojureSourceCode(fileName)
            ;
    }

    public static boolean isJavaSourceCode(String fileName) {
        return fileName.endsWith(".java");
    }

    public static boolean isKotlinSourceCode(String fileName) {
        return fileName.endsWith(".kt");
    }

    public static boolean isScalaSourceCode(String fileName) {
        return fileName.endsWith(".sc");
    }

    public static boolean isGroovySourceCode(String fileName) {
        return fileName.endsWith(".groovy")
            || fileName.endsWith(".gvy")
            || fileName.endsWith(".gy")
            || fileName.endsWith(".gysh")
            ;
    }

    public static boolean isClojureSourceCode(String fileName) {
        return fileName.endsWith(".clj")
            || fileName.endsWith(".cljs")
            || fileName.endsWith(".cljc")
            ;
    }

    public static boolean isMetaData(String fileName) {
        return fileName.startsWith("META-INF") || fileName.startsWith("/META-INF");
    }

    public static boolean isByteCode(String fileName) {
        return fileName.endsWith(".class");
    }

    public static boolean isOther(String fileName) {
        return !isMetaData(fileName)
            && !isSourceCode(fileName)
            && !isByteCode(fileName)
            ;
    }

}
