package io.github.bineq.daleq.cli;

import io.github.bineq.daleq.IOUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Checks whether a resource with a given name exists in both jars.
 * @author jens dietrich
 */
public class ResourceIsPresentAnalyser implements Analyser {

    private static final URL DIFF_TEMPLATE = DaleqAnalyser.class.getResource("/cli/io.github.bineq.daleq.cli.ResourceIsPresentAnalyser/diff.html");

    @Override
    public int positionHint() {
        return 10;
    }

    @Override
    public AnalysisResult analyse(String resource, Path jar1, Path jar2,Path contextDir, Map<String,Object> options) throws IOException {
        List<AnalysisResultAttachment> attachments = new ArrayList<>();
        Set<String> resources1 = IOUtil.nonDirEntries(jar1);
        Set<String> resources2 = IOUtil.nonDirEntries(jar2);
        if (resources1.contains(resource) && resources2.contains(resource)) {
            return new AnalysisResult(AnalysisResultState.PASS,"",attachments);
        }
        else if (resources1.contains(resource) && !resources2.contains(resource)) {
            Map<String,String> bindings = Map.of("resource",resource,"jar_present",jar1.toString(),"jar_missing",jar2.toString());
            String link = ResourceUtil.createReportFromTemplate(contextDir,this, resource, DIFF_TEMPLATE,"diff.html", bindings);
            attachments.add(new AnalysisResultAttachment("diff",link,AnalysisResultAttachment.Kind.DIFF));
            return new AnalysisResult(AnalysisResultState.FAIL,"resource missing in " + jar1.toString(),attachments);
        }
        else if (resources2.contains(resource) && !resources1.contains(resource)) {
            Map<String,String> bindings = Map.of("resource",resource,"jar_present",jar2.toString(),"jar_missing",jar1.toString());
            String link = ResourceUtil.createReportFromTemplate(contextDir,this, resource, DIFF_TEMPLATE,"diff.html", bindings);
            attachments.add(new AnalysisResultAttachment("diff",link,AnalysisResultAttachment.Kind.DIFF));
            return new AnalysisResult(AnalysisResultState.FAIL,"resource missing in " + jar2.toString(),attachments);
        }
        else {
            assert false : "resource missing in both jars";
            return new AnalysisResult(AnalysisResultState.ERROR,"resource missing in both jars",attachments);
        }
    }

    @Override
    public String name() {
        return "file present?";
    }

    @Override
    public String description() {
        return "check whether the respective resource is present in both jars compared";
    }
}
