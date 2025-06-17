package io.github.bineq.daleq.cli;

import io.github.bineq.daleq.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Checks whether Java source code resources are equal.
 * Equal means to check line-by-line, char-by-char.
 * @author jens dietrich
 */
public class SameSourceCodeAnalyser implements Analyser {

    private static final String DIFF_REPORT_NAME = "diff-src.html";
    private static final Logger LOG = LoggerFactory.getLogger(SameSourceCodeAnalyser.class);

    @Override
    public boolean isBytecodeAnalyser() {
        return false;
    }

    @Override
    public int positionHint() {
        return 30;
    }

    @Override
    public AnalysisResult analyse(String resource, Path jar1, Path jar2, Path contextDir) throws IOException {

        // locate source -- TODO: kotlin & co
        resource = resource.replace(".class", ".java");

        AnalysisResult analysisResult = checkResourceIsPresent(jar1,jar2,resource);
        if (analysisResult!=null) {
            return analysisResult;
        }

        if (ResourceUtil.isJavaSourcecode(resource)) {
            try {
                byte[] data1 = IOUtil.readEntryFromZip(jar1, resource);
                byte[] data2 = IOUtil.readEntryFromZip(jar2, resource);
                // try to read lines to get around issues with different new line encoding
                List<String> lines1 = ResourceUtil.readLines(data1, StandardCharsets.UTF_8);
                List<String> lines2 = ResourceUtil.readLines(data2, StandardCharsets.UTF_8);
                boolean result = lines1.containsAll(lines2) && lines2.containsAll(lines1);

                List<AnalysisResultAttachment> attachments = new ArrayList<>();

                // diff is meaningless if files are the same
                if (!result && ResourceUtil.isCharData(resource)) {
                    // create diff !
                    Path folder = ResourceUtil.createResourceFolder(contextDir,resource,this);
                    Path file1 = folder.resolve(resource.replace("/",".")+"__1");
                    Path file2 = folder.resolve(resource.replace("/",".")+"__2");
                    Files.write(file1, data1);
                    Files.write(file2, data2);
                    Path diff = folder.resolve(DIFF_REPORT_NAME);
                    try {
                        ResourceUtil.diff(file1, file2, diff);
                        String link = ResourceUtil.createLink(contextDir,resource, this, DIFF_REPORT_NAME);
                        attachments.add(new AnalysisResultAttachment("diff", link,AnalysisResultAttachment.Kind.DIFF));
                    }
                    catch (Exception e) {
                        LOG.error("error diffing content",e);
                    }
                }

                if (result) {
                    return new AnalysisResult(AnalysisResultState.PASS,"sources are the same",attachments);
                }
                else {
                    return new AnalysisResult(AnalysisResultState.FAIL,"sources are different content",attachments);
                }
            }
            catch (Exception x) {
                return new AnalysisResult(AnalysisResultState.ERROR,"error loading and comparing data of sources");
            }
        }
        else {
            return new AnalysisResult(AnalysisResultState.SKIP,"resource is not source code");
        }

    }


    @Override
    public String name() {
        return "same source?";
    }

    @Override
    public String description() {
        return "check whether the respective sources are the same in both jars compared";
    }


}
