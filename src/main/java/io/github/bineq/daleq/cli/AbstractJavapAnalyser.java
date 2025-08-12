package io.github.bineq.daleq.cli;

import io.github.bineq.daleq.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Analyser based on comparing the output of javap , concrete subclasses decide in arguments passed to javap.
 * @author jens dietrich
 */
public abstract class AbstractJavapAnalyser implements Analyser {

    public abstract String[] getJavapArgs();

    protected static final Logger LOG = LoggerFactory.getLogger(AbstractJavapAnalyser.class);
    protected static final String DIFF_REPORT_NAME = "diff.html";

    private String equivalenceIsInferredFromEqualityLink = null;

    @Override
    public void init(Path outDir) throws IOException {
        Analyser.super.init(outDir);
        String equivalenceIsInferredFromEqualityResourceHtmlReportName = "equivalence-inferred-from-equality.html";
        URL equivalenceIsInferredFromEqualityResource = this.getClass().getClassLoader().getResource("cli/"+this.getClass().getName()+'/'+equivalenceIsInferredFromEqualityResourceHtmlReportName);
        Path analysisFolder = ResourceUtil.createAnalysisFolder(outDir,this);
        IOUtil.copy(equivalenceIsInferredFromEqualityResource, analysisFolder.resolve(equivalenceIsInferredFromEqualityResourceHtmlReportName));
        equivalenceIsInferredFromEqualityLink = ResourceUtil.createLink(this,equivalenceIsInferredFromEqualityResourceHtmlReportName);
    }

    @Override
    public AnalysisResult analyse(String resource, Path jar1, Path jar2, Path contextDir, Map<String,Object> options) throws IOException {

        AnalysisResult analysisResult = checkResourceIsPresent(jar1,jar2,resource);
        List<AnalysisResultAttachment> attachments = new ArrayList<>();
        if (analysisResult!=null) {
            return analysisResult;
        }
        else if (resource.endsWith(".class")) {
            byte[] data1 = IOUtil.readEntryFromZip(jar1, resource);
            byte[] data2 = IOUtil.readEntryFromZip(jar2, resource);

            // early intervention: if bytecodes are the same, there is no need to run javap, it will be the same
            // this is of course assuming that javap is deterministic
            if (Arrays.equals(data1,data2)) {
                AnalysisResultAttachment attachment = new AnalysisResultAttachment("info",equivalenceIsInferredFromEqualityLink,AnalysisResultAttachment.Kind.INFO);
                attachments.add(attachment);
                return new AnalysisResult(AnalysisResultState.PASS, "disassemblies of identical .class files are identical",attachments);
            }

            else {
                Path folder = null;
                try {
                    folder = ResourceUtil.createResourceFolder(contextDir, resource, this);
                    Path dir1 = folder.resolve("jar1");
                    Path dir2 = folder.resolve("jar2");
                    Files.createDirectories(dir1);
                    Files.createDirectories(dir2);
                    String clazzFileName = resource.substring(resource.lastIndexOf('/') + 1);
                    Files.write(dir1.resolve(clazzFileName), data1);
                    Files.write(dir2.resolve(clazzFileName), data2);


                    Path disassembled1 = javap(dir1, data1, clazzFileName, getJavapArgs());
                    Path disassembled2 = javap(dir2, data2, clazzFileName, getJavapArgs());

                    String code1 = Files.readString(disassembled1);
                    String code2 = Files.readString(disassembled2);

                    if (code1.equals(code2)) {
                        return new AnalysisResult(AnalysisResultState.PASS, "disassemblies of .class files are identical", attachments);
                    } else {
                        Path diff = folder.resolve(DIFF_REPORT_NAME);
                        ResourceUtil.diff(disassembled1, disassembled2, diff);
                        String link = ResourceUtil.createLink(contextDir,resource, this, DIFF_REPORT_NAME);
                        attachments.add(new AnalysisResultAttachment("diff", link, AnalysisResultAttachment.Kind.DIFF));
                        return new AnalysisResult(AnalysisResultState.FAIL, "disassemblies of .class files are different", attachments);
                    }
                } catch (Exception x) {
                    LOG.error("Error disassembling and comparing .class files", x);

                    try {
                        Path errorFile = folder.resolve("error.txt");
                        ResourceUtil.createErrorFile(x, "Exception running analysis: \"" + this.name() + "\"", errorFile);
                        String link = ResourceUtil.createLink(contextDir,resource, this, "error.txt");
                        attachments.add(new AnalysisResultAttachment("error", link, AnalysisResultAttachment.Kind.ERROR));
                    }
                    catch (Exception y) {
                        // e.g. if folder==null
                        LOG.error("Error creating error attachments", y);
                    }
                    return new AnalysisResult(AnalysisResultState.ERROR, "Error disassembling and comparing .class files", attachments);
                }
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

        LOG.info("Class {} disassembled to {}", classFile,javapFile);
        return javapFile;
    }

}
