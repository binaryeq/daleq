package io.github.bineq.daleq.cli;

import com.google.common.base.Preconditions;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility to create structures to resources like diff files created during analysis.
 * @author jens dietrich
 */
public class ResourceUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceUtil.class);

    static boolean isSourcecode(String resource) {
        return resource.endsWith(".java"); // TODO: kotlin, scala, groovy etc
    }

    static boolean isCharData (String resource) {

        return isSourcecode(resource)
            || resource.endsWith(".xml")
            || resource.endsWith(".json")
            || resource.endsWith(".properties")
            || resource.endsWith(".mf")
            || resource.endsWith(".MF")
            || resource.startsWith("META-INF/services/")
            || resource.endsWith(".list")
            || resource.endsWith(".LIST")
            || resource.endsWith(".sql")
            || resource.endsWith(".htm")
            || resource.endsWith(".html")
            || resource.endsWith(".css")
            || resource.endsWith(".js")
            || resource.endsWith(".ts")
            || resource.endsWith(".txt")
            ;
    }

    static Path createResourceFolder (Path outputDir,String resourceUnderAnalysis, Analyser analyser) throws IOException {
        Path analysisSpecificDir = createAnalysisFolder(outputDir,analyser);
        Path resourceSpecificDir = analysisSpecificDir.resolve(resourceUnderAnalysis);
        if (!Files.exists(resourceSpecificDir)) {
            Files.createDirectories(resourceSpecificDir);
        }
        return resourceSpecificDir;
    }

    // for "global" resources for a given analyser
    static Path createAnalysisFolder (Path outputDir, Analyser analyser) throws IOException {
        Path analysisSpecificDir = outputDir.resolve(analyser.getClass().getName());
        if (!Files.exists(analysisSpecificDir)) {
            Files.createDirectories(analysisSpecificDir);
        }
        return analysisSpecificDir;
    }

    // hyperlink to be used in report generated in outputDir
    static String createLink (Path contextDir,String resourceUnderAnalysis, Analyser analyser, String filename) {
        return contextDir.toFile().getAbsolutePath()+'/'+analyser.getClass().getName()+'/'+resourceUnderAnalysis+'/'+filename;
    }

    static String createLink (Analyser analyser, String filename) {
        // outputDir is context, can be ignored
        return analyser.getClass().getName()+'/'+filename;
    }

    static List<String> readLines(byte[] data, Charset charset) throws IOException {
        List<String> lines = new ArrayList<>();
        InputStream is = new ByteArrayInputStream(data);
        Reader isr = new InputStreamReader(is, charset);
        try (BufferedReader reader = new BufferedReader(isr)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    static void createErrorFile(Throwable x, String message, Path file) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(file.toFile()))) {
            out.println(message);
            out.println();
            x.printStackTrace(out);
        }
    }

    /**
     * Create a html page from a template and return the link.
     * @param outputDir
     * @param analyser
     * @param resource - the resource (file) under analysis
     * @param template - as resource reference (classpath)
     * @param fileName - the local fileName
     * @param bindings - binds ids if html elements in the template to values
     * @return a link to the created resource (instantiated template)
     */
    static String createReportFromTemplate(Path contextDir, Analyser analyser, String resource, URL template, String fileName, Map<String,String> bindings) throws IOException {

        Path templatePath = Path.of(template.getPath());
        String templ = Files.readString(templatePath);
        Document doc = Parser.htmlParser().parseInput(templ,"");

        // instantiate template
        for (String key: bindings.keySet()) {
            Element element = doc.getElementById(key);
            Preconditions.checkState(element!=null,"can't find element with id \"" + key + "\" in document " + templatePath);
            String value = bindings.get(key);
            element.append(value);
        }

        // write result
        Path dir = createResourceFolder (contextDir, resource, analyser) ;
        Path file = dir.resolve(fileName);
        Files.write(file, doc.html().getBytes());

        return createLink(contextDir,resource, analyser, fileName);
    }

    static void diff(Path file1, Path file2, Path diffFile) throws Exception {
        List<ProcessBuilder> pipeline = List.of(
            new ProcessBuilder("diff","-u",file1.toFile().getAbsolutePath(),file2.toFile().getAbsolutePath()),
            new ProcessBuilder("diff2html", "-i","stdin","-s","side","-F",diffFile.toFile().getAbsolutePath())
        );
        ProcessBuilder.startPipeline(pipeline);
        LOG.info("diffed {} and {}, output written to {}", file1, file2, diffFile);
    }

    static Map<String,String> newModifiableMap(String... keysAndValues) {
        Preconditions.checkArgument(keysAndValues.length % 2 == 0," number of arguments expected to be even");
        Map<String,String> map = new HashMap<>();
        for (int i=0;i<keysAndValues.length;i+=2) {
            String key = keysAndValues[i];
            String value = keysAndValues[i+1];
            map.put(key,value);
        }
        return map;
    }
}
