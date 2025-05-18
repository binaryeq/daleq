package io.github.bineq.daleq.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility to create structures to resources like diff files created during analysis.
 * @author jens dietrich
 */
public class ResourceUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceUtil.class);


    static boolean isCharData (String resource) {
        return resource.endsWith(".xml")
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
    static String createLink (String resourceUnderAnalysis, Analyser analyser, String filename) {
        // outputDir is context, can be ignored
        return analyser.getClass().getName()+'/'+resourceUnderAnalysis+'/'+filename;
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

    static void diff(Path file1, Path file2, Path diffFile) throws Exception {
        // `diff -u idb-NoopCallback-1-full.txt idb-NoopCallback-2-full.txt | diff2html -i stdin -s side -F diff.html`

//        new ProcessBuilder("diff","-u",file1.toFile().getAbsolutePath(),file2.toFile().getAbsolutePath())
//            //.inheritIO()
//            .redirectOutput(ProcessBuilder.Redirect.to(diffFile.toFile()))
//            .start()
//            .waitFor();


//        new ProcessBuilder("diff","-u",file1.toFile().getAbsolutePath(),file2.toFile().getAbsolutePath())
//            //.inheritIO()
//            .redirectOutput(ProcessBuilder.Redirect.to(diffFile.toFile()))
//            .start()
//            .waitFor();

        List<ProcessBuilder> pipeline = List.of(
            new ProcessBuilder("diff","-u",file1.toFile().getAbsolutePath(),file2.toFile().getAbsolutePath()),
            new ProcessBuilder("diff2html", "-i","stdin","-s","side","-F",diffFile.toFile().getAbsolutePath())
        );

        ProcessBuilder.startPipeline(pipeline);



        LOG.info("diffed {} and {}, output written to {}", file1, file2, diffFile);
    }
}
