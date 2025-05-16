package io.github.bineq.daleq.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
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

    static Path createResourceFolder (Path outputDir,String resource, Analyser analyser) {
        Path analysisSpecificDir = outputDir.resolve(analyser.getClass().getName());
        Path resourceSpecificDir = analysisSpecificDir.resolve(resource);
        resourceSpecificDir.toFile().mkdirs();
        return resourceSpecificDir;
    }

    // hyperlink to be used in report generated in outputDir
    static String createLink (Path outputDir,String resource, Analyser analyser, String filename) {
        // outputDir is context, can be ignored
        return analyser.getClass().getName()+'/'+resource+'/'+filename;
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
