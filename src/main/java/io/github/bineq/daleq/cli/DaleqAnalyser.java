package io.github.bineq.daleq.cli;

import io.github.bineq.daleq.IOUtil;
import io.github.bineq.daleq.Souffle;
import io.github.bineq.daleq.edb.FactExtractor;
import io.github.bineq.daleq.idb.IDB;
import io.github.bineq.daleq.idb.IDBPrinter;
import io.github.bineq.daleq.idb.IDBReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static io.github.bineq.daleq.Souffle.checkSouffleExe;

/**
 * Analyser based on comparing the output of daleq reports.
 * @author jens dietrich
 */
public class DaleqAnalyser implements Analyser {

    private static final Logger LOG = LoggerFactory.getLogger(DaleqAnalyser.class);
    private static final String DIFF_PROJECTED_REPORT_NAME = "diff-projected.html";
    private static final String DIFF_FULL_REPORT_NAME = "diff-full.html";
    private static final boolean SOUFFLE_AVAILABLE = checkSouffleExe();
    public static final String RULES = "/rules/advanced.souffle";


    @Override
    public AnalysisResult analyse(String resource, Path jar1, Path jar2, Path contextDir) throws IOException {

        AnalysisResult analysisResult = checkResourceIsPresent(jar1, jar2, resource);
        Map<String, String> attachments = new HashMap<>();
        if (analysisResult != null) {
            return analysisResult;
        } else if (!SOUFFLE_AVAILABLE) {
            return new AnalysisResult(AnalysisResultState.ERROR, "souffle is not available, check logs for details", attachments);
        } else if (resource.endsWith(".class")) {
            byte[] data1 = IOUtil.readEntryFromZip(jar1, resource);
            byte[] data2 = IOUtil.readEntryFromZip(jar2, resource);

            // early intervention: if bytecodes are the same, there is no need to run javap, it will be the same
            // this is of course assuming that javap is deterministic
            if (Arrays.equals(data1, data2)) {
                return new AnalysisResult(AnalysisResultState.PASS, ".class files are identical and will therefore be equivalent", attachments);
            }

            Path folder = ResourceUtil.createResourceFolder(contextDir, resource, this);
            Path dir1 = folder.resolve("jar1");
            Path dir2 = folder.resolve("jar2");
            Files.createDirectories(dir1);
            Files.createDirectories(dir2);
            String clazzFileName = resource.substring(resource.lastIndexOf('/') + 1);
            Path classFile1 = dir1.resolve(clazzFileName);
            Path classFile2 = dir2.resolve(clazzFileName);
            Files.write(classFile1, data1);
            Files.write(classFile2, data2);
            Path edbDir1 = dir1.resolve("edb");
            Path edbDir2 = folder.resolve("edb2");
            Files.createDirectories(edbDir1);
            Files.createDirectories(edbDir2);
            Path idbDir1 = dir1.resolve("idb");
            Path idbDir2 = folder.resolve("idb2");
            Files.createDirectories(idbDir1);
            Files.createDirectories(idbDir2);

            try {
                IDB idb1 = computeAndParseIDB(dir1, classFile1, edbDir1, idbDir1);
                IDB idb2 = computeAndParseIDB(dir2, classFile2, edbDir2, idbDir2);

                Path idbProjectedFile1 = dir1.resolve("idb-projected.txt");
                Path idbProjectedFile2 = dir2.resolve("idb-projected.txt");
                Path idbFullFile1 = dir1.resolve("idb-full.txt");
                Path idbFullFile2 = dir2.resolve("idb-full.txt");

                IDBPrinter.printIDB(idb1.project(), idbProjectedFile1);
                IDBPrinter.printIDB(idb2.project(), idbProjectedFile2);
                String idb1ProjectedAsString = Files.readString(idbProjectedFile1);
                String idb2ProjectedAsString = Files.readString(idbProjectedFile2);

                IDBPrinter.printIDB(idb1, idbFullFile1);
                IDBPrinter.printIDB(idb2, idbFullFile2);
                String idb1FullAsString = Files.readString(idbProjectedFile1);
                String idb2FullAsString = Files.readString(idbProjectedFile2);

                if (idb1ProjectedAsString.equals(idb2ProjectedAsString)) {
                    return new AnalysisResult(AnalysisResultState.PASS, "projected IDBs are identical", attachments);
                } else {

                    Path diffProjected = folder.resolve(DIFF_PROJECTED_REPORT_NAME);
                    ResourceUtil.diff(idbProjectedFile1, idbProjectedFile2, diffProjected);
                    String link = ResourceUtil.createLink(contextDir, resource, this, DIFF_PROJECTED_REPORT_NAME);
                    attachments.put("diff-projected", link);

                    Path diffFull = folder.resolve(DIFF_FULL_REPORT_NAME);
                    ResourceUtil.diff(idbFullFile1, idbFullFile2, diffFull);
                    String link2 = ResourceUtil.createLink(contextDir, resource, this, DIFF_FULL_REPORT_NAME);
                    attachments.put("diff-full", link2);
                    
                    return new AnalysisResult(AnalysisResultState.FAIL, "projected IDBs are different", attachments);
                }
            }
            catch (Exception e) {
                return new AnalysisResult(AnalysisResultState.ERROR, "Failed to compute and compare IDB", attachments);
            }
        }
        else {
            return new AnalysisResult(AnalysisResultState.SKIP,"analysis can only be applied to .class files");
        }

    }

    private IDB computeAndParseIDB(Path contextDir, Path classFile, Path edbDir, Path idbDir) throws Exception {

        if (Files.exists(edbDir)) {
            IOUtil.deleteDir(edbDir);
        } else {
            Files.createDirectories(edbDir);
        }
        if (Files.exists(idbDir)) {
            IOUtil.deleteDir(idbDir);
        } else {
            Files.createDirectories(idbDir);
        }

        Path edbDef = edbDir.resolve("db.souffle");
        FactExtractor.extractAndExport(classFile, edbDef, edbDir, true);

        Path rulesPath = Path.of(Souffle.class.getResource(RULES).getPath());
        Path mergedEDBAndRules = contextDir.resolve("mergedEDBAndRules.souffle");

        Souffle.createIDB(edbDef, rulesPath, edbDir, idbDir, mergedEDBAndRules);

        return IDBReader.read(idbDir);

    }


    @Override
    public String name() {
        return "daleq";
    }

    @Override
    public String description() {
        return "daleq based analyser";
    }
}
