package io.github.bineq.daleq.cli;

import io.github.bineq.daleq.IOUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

/**
 * Abstract analyser for source code.
 * @author jens dietrich
 */
public abstract class AbstractSourceCodeAnalyser implements Analyser {

    protected String noSourceCodeAnalysisForInnerClassesLink = null;

    @Override
    public void init(Path outDir) throws IOException {
        Analyser.super.init(outDir);
        String noSourceCodeAnalysisForInnerClassesReportName = "inner-class-source-analysis.html";
        URL noSourceCodeAnalysisForInnerClassesResource = AbstractSourceCodeAnalyser.class.getClassLoader().getResource("cli/"+ AbstractSourceCodeAnalyser.class.getName()+'/'+noSourceCodeAnalysisForInnerClassesReportName);
        Path analysisFolder = ResourceUtil.createAnalysisFolder(outDir,this);
        IOUtil.copy(noSourceCodeAnalysisForInnerClassesResource, analysisFolder.resolve(noSourceCodeAnalysisForInnerClassesReportName));
        noSourceCodeAnalysisForInnerClassesLink = ResourceUtil.createLink(this,noSourceCodeAnalysisForInnerClassesReportName);
    }

    protected AnalysisResult checkInnerClass(String resource) {
        if (resource.contains("$")) {
            List<AnalysisResultAttachment> attachments = List.of(
                new AnalysisResultAttachment("info",noSourceCodeAnalysisForInnerClassesLink,AnalysisResultAttachment.Kind.INFO)
            );
            return new AnalysisResult(AnalysisResultState.SKIP,"for inner classes, source code analysis is delegated to the compilation units associated with the top-level class",attachments);
        }
        else {
            return null;
        }
    }

    @Override
    public boolean isBytecodeAnalyser() {
        return false;
    }

}
