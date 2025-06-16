package io.github.bineq.daleq.evaluation.resultanalysis;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Analyse results of an evaluation run.
 * @author jens dietrich
 */
public class AnalyseResults {

    final static Logger LOG = LoggerFactory.getLogger(AnalyseResults.class);

    public static void main (String[] args) throws Exception {
        Path root = Path.of("evaluation/db-mvnc-vs-gaoss");
        String provider1 = "gaoss";
        String provider2 = "mvnc";

        // extracts raw version fact from EDB
        Function<Path,String> extractor = path -> {
            Path edb = path.resolve("edb.zip");
            try {
                ZipFile zip = new ZipFile(edb.toFile());
                ZipEntry entry = zip.getEntry("edb/facts/VERSION.facts");
                InputStream inputStream = zip.getInputStream(entry);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                return reader.lines()
                    .map(line -> line.split("\t"))
                    .map(tokens -> tokens[2])// in particular ignore fact id
                    .collect(Collectors.joining(System.lineSeparator()));

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        analyse(root,provider1,provider2,extractor);

    }

    protected static void analyse(Path root, String provider1, String provider2, Function<Path,String> extraction) throws IOException {
        for (File gavDir:root.toFile().listFiles(f -> f.isDirectory() && !f.isHidden())) {

            for (File classDir:gavDir.listFiles(f -> f.isDirectory() && !f.isHidden())) {
                File providerDir1 = new File(classDir, provider1);
                File providerDir2 = new File(classDir, provider2);
                Preconditions.checkState(providerDir1.exists());
                Preconditions.checkState(providerDir2.exists());
                Preconditions.checkState(providerDir1.isDirectory());
                Preconditions.checkState(providerDir2.isDirectory());

                String content1 = extraction.apply(Path.of(providerDir1.getAbsolutePath()));
                String content2 = extraction.apply(Path.of(providerDir2.getAbsolutePath()));

                if (!content1.equals(content2)) {
                    LOG.info("Differences found in gav {}, class {}", gavDir,classDir);
                }
            }
        }
    }
}
