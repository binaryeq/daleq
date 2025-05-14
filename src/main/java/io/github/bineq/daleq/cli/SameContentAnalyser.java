package io.github.bineq.daleq.cli;

import io.github.bineq.daleq.IOUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;

/**
 * Checks whether a resource with a given name exists in both jars.
 * @author jens dietrich
 */
public class SameContentAnalyser implements Analyser {
    @Override
    public AnalysisResult analyse(String resource, Path jar1, Path jar2) throws IOException {
        Set<String> resources1 = IOUtil.nonDirEntries(jar1);
        Set<String> resources2 = IOUtil.nonDirEntries(jar2);
        if (!resources1.contains(resource)) {
            return new AnalysisResult(AnalysisResultState.SKIP,"resource is missing in jar1");
        }
        else if (!resources2.contains(resource)) {
            return new AnalysisResult(AnalysisResultState.SKIP,"resource is missing in jar2");
        }
        else {
            try {
                byte[] data1 = IOUtil.readEntryFromZip(jar1, resource);
                byte[] data2 = IOUtil.readEntryFromZip(jar2, resource);
                boolean result = Arrays.equals(data1, data2);
                if (result) {
                    return new AnalysisResult(AnalysisResultState.PASS,"resources have same content (same sequence of bytes)");
                }
                else {
                    return new AnalysisResult(AnalysisResultState.FAIL,"resources have different content (different sequences of bytes)");
                }
            }
            catch (Exception x) {
                return new AnalysisResult(AnalysisResultState.ERROR,"error loading and comparing data of resources");
            }

        }

    }

    @Override
    public String name() {
        return "same data?";
    }

    @Override
    public String description() {
        return "check whether the respective resource has the same data in both jars compared";
    }
}
