package io.github.bineq.daleq.cli;

import io.github.bineq.daleq.IOUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

/**
 * Checks whether a resource with a given name exists in both jars.
 * @author jens dietrich
 */
public class ResourceIsPresentAnalyser implements Analyser {
    @Override
    public AnalysisResult analyse(String resource, Path jar1, Path jar2,Path contextDir) throws IOException {
        Set<String> resources1 = IOUtil.nonDirEntries(jar1);
        Set<String> resources2 = IOUtil.nonDirEntries(jar2);
        if (resources1.contains(resource) && resources2.contains(resource)) {
            return new AnalysisResult(AnalysisResultState.PASS,"");
        }
        else if (resources1.contains(resource) && !resources2.contains(resource)) {
            return new AnalysisResult(AnalysisResultState.FAIL,"resource missing in " + jar1.toString());
        }
        else if (resources2.contains(resource) && !resources1.contains(resource)) {
            return new AnalysisResult(AnalysisResultState.FAIL,"resource missing in " + jar2.toString());
        }
        else {
            assert false : "resource missing in both jars";
            return new AnalysisResult(AnalysisResultState.ERROR,"resource missing in both jars");
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
