package io.github.bineq.daleq.cli;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;
import io.github.bineq.daleq.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Checks whether Java source code resources are equivalent.
 * Differences in comments and formatting are ignored.
 * @author jens dietrich
 */
public class EquivalentSourceCodeAnalyser implements Analyser {

    private static final String DIFF_REPORT_NAME = "diff-src-equiv.html";
    private static final Logger LOG = LoggerFactory.getLogger(EquivalentSourceCodeAnalyser.class);
    public static final Predicate<Node> IS_COMMENT = node -> node instanceof Comment;

    @Override
    public boolean isBytecodeAnalyser() {
        return false;
    }

    @Override
    public AnalysisResult analyse(String resource, Path jar1, Path jar2, Path contextDir) throws IOException {

        // locate source -- TODO: kotlin & co
        resource = resource.replace(".class", ".java");

        AnalysisResult analysisResult = checkResourceIsPresent(jar1,jar2,resource);
        if (analysisResult!=null) {
            return analysisResult;
        }

        if (ResourceUtil.isCharData(resource)) {
            try {
                List<AnalysisResultAttachment> attachments = new ArrayList<>();
                byte[] data1 = IOUtil.readEntryFromZip(jar1, resource);
                byte[] data2 = IOUtil.readEntryFromZip(jar2, resource);

                CompilationUnit cu1 = null;
                CompilationUnit cu2 = null;

                boolean isEquivalent = false;

                // optimisation: if content are the same, then ASTs are the same
                String contentString1 = new String(data1, StandardCharsets.UTF_8);
                String contentString2 = new String(data2, StandardCharsets.UTF_8);

                if (contentString1.equals(contentString2)) {
                    isEquivalent = true;
                }

                try {
                    cu1 = StaticJavaParser.parse(contentString1);
                    cu2 = StaticJavaParser.parse(contentString2);
                    isEquivalent = compare(cu1, cu2);
                }
                catch (Exception x) {
                    return new AnalysisResult(AnalysisResultState.ERROR,"error parsing sources",attachments);
                }

                // diff is meaningless if files are the same
                if (!isEquivalent && ResourceUtil.isCharData(resource)) {
                    // create diff !
                    Path folder = ResourceUtil.createResourceFolder(contextDir,resource,this);
                    Path file1 = folder.resolve(resource.replace("/",".")+"__1eq");
                    Path file2 = folder.resolve(resource.replace("/",".")+"__2eq");
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

                if (isEquivalent) {
                    return new AnalysisResult(AnalysisResultState.PASS,"sources are equivalent",attachments);
                }
                else {
                    return new AnalysisResult(AnalysisResultState.FAIL,"sources are not equivalent",attachments);
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

    protected boolean compare(Node node1, Node node2) {

        List<Node> children1 = node1.getChildNodes().stream().filter(IS_COMMENT.negate()).collect(Collectors.toList());
        List<Node> children2 = node2.getChildNodes().stream().filter(IS_COMMENT.negate()).collect(Collectors.toList());

        boolean isEqual = true;

        if (children1.isEmpty() && children2.isEmpty()) {
            // compare values of leaves
            isEqual = isEqual && compareNodeProperties(node1,node2);
        }

        return isEqual && compareChildNodes(children1,children2);
    }

    protected boolean compareNodeProperties(Node node1, Node node2)  {
        return Objects.equals(node1.toString().trim(),node2.toString().trim());
    }

    protected boolean compareChildNodes(List<Node> childNodes1,List<Node> childNodes2) {
        if (childNodes1.size()!=childNodes2.size()) {
            return false;
        }
        boolean result = true;
        for (int i=0;i<childNodes1.size();i++) {
            Node childNode1 = childNodes1.get(i);
            Node childNode2 = childNodes2.get(i);
            result = result && compare(childNode1,childNode2);
        }
        return result;
    }


    @Override
    public String name() {
        return "equiv. source?";
    }

    @Override
    public String description() {
        return "check whether the respective sources are equivalent (ignoring formatting and comments) in both jars compared";
    }


}
