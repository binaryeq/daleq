package io.github.bineq.daleq.cli;

import io.github.bineq.daleq.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Analyser based on comparing the output of javap -c -p
 * @author jens dietrich
 */
public class JavapAnalyser implements Analyser {

    public static String[] JAVAP_ARGS = new String[]{"-c","-p"};
    private static final Logger LOG = LoggerFactory.getLogger(JavapAnalyser.class);
    private static final String DIFF_REPORT_NAME = "diff.html";


    @Override
    public AnalysisResult analyse(String resource, Path jar1, Path jar2, Path contextDir) throws IOException {

        AnalysisResult analysisResult = checkResourceIsPresent(jar1,jar2,resource);
        Map<String,String> attachments = new HashMap<>();
        if (analysisResult!=null) {
            return analysisResult;
        }
        else if (resource.endsWith(".class")) {
            byte[] data1 = IOUtil.readEntryFromZip(jar1, resource);
            byte[] data2 = IOUtil.readEntryFromZip(jar2, resource);

            // early intervention: if bytecodes are the same, there is no need to run javap, it will be the same
            // this is of course assuming that javap is deterministic
            if (Arrays.equals(data1,data2)) {
                return new AnalysisResult(AnalysisResultState.PASS, "disassemblies of .class files are identical",attachments);
            }

            Path folder = ResourceUtil.createResourceFolder(contextDir,resource,this);
            Path dir1 = folder.resolve("jar1");
            Path dir2 = folder.resolve("jar2");
            Files.createDirectories(dir1);
            Files.createDirectories(dir2);
            String clazzFileName = resource.substring(resource.lastIndexOf('/')+1);

            try {
                Path disassembled1 = javap(dir1,data1,clazzFileName,JAVAP_ARGS);
                Path disassembled2 = javap(dir2,data2,clazzFileName,JAVAP_ARGS);

                String code1 = Files.readString(disassembled1);
                String code2 = Files.readString(disassembled2);

                if (code1.equals(code2)) {
                    return new AnalysisResult(AnalysisResultState.PASS, "disassemblies of .class files are identical",attachments);
                } else {
                    Path diff = folder.resolve(DIFF_REPORT_NAME);
                    ResourceUtil.diff(disassembled1, disassembled2, diff);
                    String link = ResourceUtil.createLink(contextDir, resource, this, DIFF_REPORT_NAME);
                    attachments.put("diff", link);
                    return new AnalysisResult(AnalysisResultState.FAIL, "disassemblies of .class files are different",attachments);
                }
            }
            catch (Exception e) {
                LOG.error("Error disassembling and comparing .class files",e);
                return new AnalysisResult(AnalysisResultState.ERROR, "Error disassembling and comparing .class files");
            }
        }
        else {
            return new AnalysisResult(AnalysisResultState.SKIP,"analysis can only be applied to .class files");
        }
    }

    // disassemble bytecode, write javap file into dir
    private Path javap(Path dir, byte[] data, String classFileName,String...  javapArgs) throws IOException, InterruptedException {
        Files.createDirectories(dir);
        Path classFile = dir.resolve(classFileName);
        Path javapFile = dir.resolve(classFileName.replace(".class",".javap"));

        List<String> cmd = new ArrayList<>();
        cmd.add("javap");
        for (String arg : javapArgs) {
            cmd.add(arg);
        }
        cmd.add(classFile.toString());

        new ProcessBuilder(cmd.toArray(new String[cmd.size()]))
            .inheritIO()
            .redirectOutput(ProcessBuilder.Redirect.to(javapFile.toFile()))
            .start()
            .waitFor();

        LOG.info("Class {} disassmbled to {}", classFile,javapFile);
        return javapFile;
    }

    @Override
    public String name() {
        return "javap -c -p";
    }

    @Override
    public String description() {
        return "using the standard java disassembler";
    }
}
