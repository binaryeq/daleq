package io.github.bineq.daleq.cli;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import io.github.bineq.daleq.Fact;
import io.github.bineq.daleq.IOUtil;
import io.github.bineq.daleq.Predicate;
import io.github.bineq.daleq.Souffle;
import io.github.bineq.daleq.edb.FactExtractor;
import io.github.bineq.daleq.idb.IDB;
import io.github.bineq.daleq.idb.IDBPrinter;
import io.github.bineq.daleq.idb.IDBReader;
import io.github.bineq.daleq.souffle.provenance.DerivationNode;
import io.github.bineq.daleq.souffle.provenance.ProvenanceDB;
import io.github.bineq.daleq.souffle.provenance.ProvenanceParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private static final URL PROJECTED_IDB_TEMPLATE = DaleqAnalyser.class.getResource("/cli/io.github.bineq.daleq.cli.DaleqAnalyser/projected-idb.html");
    private static final URL ADVANCED_DIFF_TEMPLATE = DaleqAnalyser.class.getResource("/cli/io.github.bineq.daleq.cli.DaleqAnalyser/advanced-diff.html");
    public static final String RULES = "/rules/advanced.souffle";


    private String equivalenceIsInferredFromEqualityLink = null;

    @Override
    public void init(Path outDir) throws IOException {
        Analyser.super.init(outDir);
        String equivalenceIsInferredFromEqualityResourceHtmlReportName = "equivalence-inferred-from-equality.html";
        Path equivalenceIsInferredFromEqualityResource = Path.of(DaleqAnalyser.class.getClassLoader().getResource("cli/"+DaleqAnalyser.class.getName()+'/'+equivalenceIsInferredFromEqualityResourceHtmlReportName).getFile());
        Preconditions.checkState(Files.exists(equivalenceIsInferredFromEqualityResource));
        Path analysisFolder = ResourceUtil.createAnalysisFolder(outDir,this);
        Files.copy(equivalenceIsInferredFromEqualityResource, analysisFolder.resolve(equivalenceIsInferredFromEqualityResourceHtmlReportName), StandardCopyOption.REPLACE_EXISTING);
        equivalenceIsInferredFromEqualityLink = ResourceUtil.createLink(this,equivalenceIsInferredFromEqualityResourceHtmlReportName);
    }

    @Override
    public AnalysisResult analyse(String resource, Path jar1, Path jar2, Path contextDir) throws IOException {

        AnalysisResult analysisResult = checkResourceIsPresent(jar1, jar2, resource);
        List<AnalysisResultAttachment> attachments = new ArrayList<>();
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
                AnalysisResultAttachment attachment = new AnalysisResultAttachment("info",equivalenceIsInferredFromEqualityLink,AnalysisResultAttachment.Kind.INFO);
                attachments.add(attachment);
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
            Path edbDir2 = dir2.resolve("edb");
            Files.createDirectories(edbDir1);
            Files.createDirectories(edbDir2);
            Path idbDir1 = dir1.resolve("idb");
            Path idbDir2 = dir2.resolve("idb");
            Files.createDirectories(idbDir1);
            Files.createDirectories(idbDir2);
            Path mergedEDBAndRules1 = dir1.resolve("mergedEDBAndRules.souffle");
            Path mergedEDBAndRules2 = dir2.resolve("mergedEDBAndRules.souffle");

            try {
                IDB idb1 = computeAndParseIDB(dir1, classFile1, edbDir1, idbDir1,mergedEDBAndRules1);
                IDB idb2 = computeAndParseIDB(dir2, classFile2, edbDir2, idbDir2,mergedEDBAndRules2);

                ProvenanceDB provDB1 = new ProvenanceDB(edbDir1,idbDir1,mergedEDBAndRules1);
                ProvenanceDB provDB2 = new ProvenanceDB(edbDir2,idbDir2,mergedEDBAndRules2);

                Path idbProjectedFile1 = dir1.resolve("idb-projected.txt");
                Path idbProjectedFile2 = dir2.resolve("idb-projected.txt");
                Path idbFullFile1 = dir1.resolve("idb-full.txt");
                Path idbFullFile2 = dir2.resolve("idb-full.txt");

                IDBPrinter.printIDB(idb1, idbFullFile1);
                IDBPrinter.printIDB(idb2, idbFullFile2);
                IDBPrinter.printIDB(idb1.project(), idbProjectedFile1);
                IDBPrinter.printIDB(idb2.project(), idbProjectedFile2);

                String idb1FullAsString = Files.readString(idbFullFile1);
                String idb2FullAsString = Files.readString(idbFullFile2);
                String idb1ProjectedAsString = Files.readString(idbProjectedFile1);
                String idb2ProjectedAsString = Files.readString(idbProjectedFile2);


                if (idb1ProjectedAsString.equals(idb2ProjectedAsString)) {
                    if (!idb1FullAsString.equals(idb2FullAsString)) {
                        // still print diff
                        Path diffFull = folder.resolve(DIFF_FULL_REPORT_NAME);
                        ResourceUtil.diff(idbFullFile1, idbFullFile2, diffFull);
                        String link2 = ResourceUtil.createLink(resource, this, DIFF_FULL_REPORT_NAME);
                        attachments.add(new AnalysisResultAttachment("diff-full",link2,AnalysisResultAttachment.Kind.DIFF));
                    }

                    // print projected IDBs
                    Map<String,String> bindings = ResourceUtil.newModifiableMap(
                        "code",idb1ProjectedAsString,
                        "about","The textual representations of the projected IDBs generated for the classes compared are the same. Projected means that some elements like ids and instruction counters that are influenced by normalisation rules are ignored. Please check the main report for links to the full IDBs.",
                        "class",resource,
                        "jar1",asLink(jar1),
                        "jar2",asLink(jar2),
                        "edb1",asLink(edbDir1),
                        "edb2",asLink(edbDir2),
                        "idb1",asLink(idbDir1),
                        "idb2",asLink(idbDir2),
                        "idb1txt",asLink(idbFullFile1),
                        "idb2txt",asLink(idbFullFile2),
                        "pidb1txt",asLink(idbProjectedFile1),
                        "pidb2txt",asLink(idbProjectedFile2)
                    );
                    String link = ResourceUtil.createReportFromTemplate(contextDir,this, resource, PROJECTED_IDB_TEMPLATE,"idb-projected.html", bindings);
                    attachments.add(new AnalysisResultAttachment("diff-full",link,AnalysisResultAttachment.Kind.INFO));

                    // print advanced diff
                    Map<String,String> bindings2 = new HashMap<>();
                    bindings2.putAll(bindings);
                    bindings2.remove("code"); // not used in template
                    createBindingsForAdvancedDiff(bindings2,idb1,idb2,provDB1, provDB2);
                    String link2 = ResourceUtil.createReportFromTemplate(contextDir,this, resource, ADVANCED_DIFF_TEMPLATE,"advanced-diff.html", bindings2);
                    attachments.add(new AnalysisResultAttachment("advanced-diff",link2,AnalysisResultAttachment.Kind.INFO));

                    return new AnalysisResult(AnalysisResultState.PASS, "projected IDBs are identical", attachments);
                } else {

                    Path diffProjected = folder.resolve(DIFF_PROJECTED_REPORT_NAME);
                    ResourceUtil.diff(idbProjectedFile1, idbProjectedFile2, diffProjected);
                    String link = ResourceUtil.createLink(resource, this, DIFF_PROJECTED_REPORT_NAME);
                    attachments.add(new AnalysisResultAttachment("diff-projected",link,AnalysisResultAttachment.Kind.DIFF));

                    Path diffFull = folder.resolve(DIFF_FULL_REPORT_NAME);
                    ResourceUtil.diff(idbFullFile1, idbFullFile2, diffFull);
                    String link2 = ResourceUtil.createLink(resource, this, DIFF_FULL_REPORT_NAME);
                    attachments.add(new AnalysisResultAttachment("diff-full",link2,AnalysisResultAttachment.Kind.DIFF));

                    return new AnalysisResult(AnalysisResultState.FAIL, "projected IDBs are different", attachments);
                }
            }
            catch (Exception x) {
                Path errorFile = folder.resolve("error.txt");
                ResourceUtil.createErrorFile(x,"Exception running analysis: \"" + this.name()+"\"",errorFile);
                String link = ResourceUtil.createLink(resource, this, "error.txt");
                attachments.add(new AnalysisResultAttachment("error",link,AnalysisResultAttachment.Kind.ERROR));
                return new AnalysisResult(AnalysisResultState.ERROR, "Failed to compute and compare IDB", attachments);
            }
        }
        else {
            return new AnalysisResult(AnalysisResultState.SKIP,"analysis can only be applied to .class files");
        }

    }


    private IDB computeAndParseIDB(Path contextDir, Path classFile, Path edbDir, Path idbDir, Path mergedEDBAndRules) throws Exception {

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


    private void createBindingsForAdvancedDiff(Map<String, String> bindings, IDB idb1, IDB idb2,ProvenanceDB provDB1, ProvenanceDB provDB2) {
        // the binding is actual html
        StringBuffer html = new StringBuffer();

        ifDiffBeforeNormalisationAppend(idb1.getBytecodeVersionFact(),idb2.getBytecodeVersionFact(),provDB1,provDB2,"Bytecode Version Fact",html);
        ifDiffBeforeNormalisationAppend(idb1.getClassSignatureFact(),idb2.getClassSignatureFact(),provDB1,provDB2,"Class Signature Fact",html);
        ifDiffBeforeNormalisationAppend(idb1.getClassSuperclassFact(),idb2.getClassSuperclassFact(),provDB1,provDB2,"Class Superclass Fact",html);
        ifDiffBeforeNormalisationAppend(idb1.getClassInterfaceFacts(),idb2.getClassInterfaceFacts(),provDB1,provDB2,"Class Interface Fact",html);
        ifDiffBeforeNormalisationAppend(idb1.getClassRawAccessFact(),idb2.getClassRawAccessFact(),provDB1,provDB2,"Class Signature Fact",html);

        ifDiffBeforeNormalisationAppend(idb1.getMethodFactsAsList(),idb2.getMethodFactsAsList(),provDB1,provDB2,"Method Fact",html);
        ifDiffBeforeNormalisationAppend(idb1.getFieldFactsAsList(),idb2.getFieldFactsAsList(),provDB1,provDB2,"Field Fact",html);


        ifDiffBeforeNormalisationAppend(idb1.getRemovedMethodFactsAsList(),idb2.getRemovedMethodFactsAsList(),provDB1,provDB2,"Removed Method Fact (SPECIAL)",html);


        if (idb1.getRemovedMethodFacts().size()>0) {
            html.append("<h3>Removed Methods in Jar1</h3>");
            for (Fact fact:idb1.getRemovedMethodFacts()) {
                html.append(toHtml(fact,provDB1));
            }
        }
        if (idb2.getRemovedMethodFacts().size()>0) {
            html.append("<h3>Removed Methods in Jar2</h3>");
            for (Fact fact:idb2.getRemovedMethodFacts()) {
                html.append(toHtml(fact,provDB2));
            }
        }

        if (idb1.getRemovedFieldFacts().size()>0) {
            html.append("<h3>Removed Fields in Jar1</h3>");
            for (Fact fact:idb1.getRemovedFieldFacts()) {
                html.append(toHtml(fact,provDB1));
            }
        }
        if (idb2.getRemovedFieldFacts().size()>0) {
            html.append("<h3>Removed Fields in Jar2</h3>");
            for (Fact fact:idb2.getRemovedFieldFacts()) {
                html.append(toHtml(fact,provDB2));
            }
        }

        for (String method:idb1.getRemovedInstructionFacts().keySet()) {
            for (Fact fact:idb1.getRemovedInstructionFacts().get(method)) {
                html.append("<h3>Removed or Replaced Bytecode Instructions in Jar1, method: " + method+ "</h3>");
                html.append(toHtml(fact,provDB1));
            }
        }

        for (String method:idb2.getRemovedInstructionFacts().keySet()) {
            for (Fact fact:idb2.getRemovedInstructionFacts().get(method)) {
                html.append("<h3>Removed or Replaced Bytecode Instructions in Jar2, method: " + method+ "</h3>");
                html.append(toHtml(fact,provDB2));
            }
        }

        for (String method:idb1.getMethodMovedInstructionFacts().keySet()) {
            for (Fact fact:idb1.getMethodMovedInstructionFacts().get(method)) {
                html.append("<h3>Moved or Replaced Bytecode Instructions in Jar1, method: " + method+ "</h3>");
                html.append(toHtml(fact,provDB1));
            }
        }

        for (String method:idb2.getMethodMovedInstructionFacts().keySet()) {
            for (Fact fact:idb2.getMethodMovedInstructionFacts().get(method)) {
                html.append("<h3>Moved Bytecode Instructions in Jar2, method: " + method+ "</h3>");
                html.append(toHtml(fact,provDB2));
            }
        }

        Set<String> methods = Sets.union(idb1.getMethodInstructionFacts().keySet(),idb2.getMethodInstructionFacts().keySet());
        for (String method:methods) {
            List<Fact> instructions1 = idb1.getMethodInstructionFacts(method);
            List<Fact> instructions2 = idb2.getMethodInstructionFacts(method);
            ifDiffBeforeNormalisationAppend(instructions1,instructions2,provDB1,provDB2,"Method Instruction Fact for method " + method,html);
        }





//        Set<Fact> removedInstructionFacts1 = idb1.getRemovedInstructionFacts().values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
//        Set<Fact> removedInstructionFacts2 = idb2.getRemovedInstructionFacts().values().stream().flatMap(Collection::stream).collect(Collectors.toSet());

//        if (removedInstructionFacts1.size()>0) {
//            html+="<h3>Removed or Replaced Bytecode Instructions in Jar1</h3>";
//            for (Fact fact:removedInstructionFacts1) {
//                html+=toHtml(fact,provDB1);
//            }
//        }
//        if (removedInstructionFacts2.size()>0) {
//            html+="<h3>Removed or Replaced Bytecode Instructions in Jar2</h3>";
//            for (Fact fact:removedInstructionFacts2) {
//                html+=toHtml(fact,provDB2);
//            }
//        }

        bindings.put("diffs", html.toString());

    }

    private void ifDiffBeforeNormalisationAppend(List<Fact> facts1, List<Fact> facts2, ProvenanceDB provDB1,ProvenanceDB provDB2,String htmlHeader,StringBuffer html) {
        int l = Math.min(facts1.size(),facts2.size());
        for (int i=0;i<l;i++) {
            ifDiffBeforeNormalisationAppend(facts1.get(i),facts2.get(i),provDB1,provDB2,htmlHeader,html);
        }
        if (facts1.size()>l) {
            for (int i=l;i<facts1.size();i++) {
                ifDiffBeforeNormalisationAppend(facts1.get(i),null,provDB1,provDB2,htmlHeader,html);
            }
        }
        if (facts2.size()>l) {
            for (int i=l;i<facts2.size();i++) {
                ifDiffBeforeNormalisationAppend(null,facts2.get(i),provDB1,provDB2,htmlHeader,html);
            }
        }
    }

    private void ifDiffBeforeNormalisationAppend(Fact fact1, Fact fact2, ProvenanceDB provDB1,ProvenanceDB provDB2,String htmlHeader,StringBuffer html) {
        if (isDiffBeforeNormalisation(fact1, fact2, provDB1, provDB2)) {
            html.append("<h3>"+htmlHeader+"</h3>");
            html.append("<h4>"+htmlHeader+" -- Jar1</h4>");
            html.append(fact1==null?"no fact found":toHtml(fact1,provDB1));
            html.append("<h4>"+htmlHeader+" -- Jar2</h4>");
            html.append(fact2==null?"no fact found":toHtml(fact2,provDB2));
        }
    }

    /**
     * Whether two facts are different before normalisation -- this is established as follows:
     * - either different rules were used
     * - or facts used by those rules where different
     * @param fact1
     * @param fact2
     * @param provDB1 the first provenance db (to look up actual rules and facts)
     * @param provDB1 the first provenance db (to look up actual rules and facts)
     * @return
     */
    private boolean isDiffBeforeNormalisation(Fact fact1, Fact fact2, ProvenanceDB provDB1,ProvenanceDB provDB2) {
        String id1 = (String)fact1.values()[0];
        String id2 = (String)fact2.values()[0];

        try {
            DerivationNode root1 = ProvenanceParser.parse(id1);
            DerivationNode root2 = ProvenanceParser.parse(id2);
            return isDiffBeforeNormalisation(root1,root2,provDB1,provDB2);
        } catch (Exception x) {
            LOG.error("Error parsing provenance",x);
            return true; // show diff if something goes wrong!
        }
    }

    private boolean isDiffBeforeNormalisation(DerivationNode derivation1, DerivationNode derivation2, ProvenanceDB provDB1,ProvenanceDB provDB2) {
        if (derivation1.getChildren().size()==0) {
            if (derivation2.getChildren().size()>0) {
                return true; // derivations are different
            }
            else {
                if (derivation1.getId().startsWith("R_")) {
                    // this is a rule without premisses, e.g. uses to classify method types as "root"
                    // IS_ROOT_METHOD("R_IS_ROOT_METHOD_GETCLASS","getClass", "()Ljava/lang/Class;").
                    return !derivation1.getId().equals(derivation2.getId());
                }
                else {
                    // those are EDB facts -- leaves in the derivation tree
                    ProvenanceDB.FlatFact fact1 = provDB1.getEdbFact(derivation1.getId());
                    assert fact1!=null;
                    ProvenanceDB.FlatFact fact2 = provDB2.getEdbFact(derivation2.getId());
                    assert fact2!=null;

                    if (!fact1.predicateName().equals(fact2.predicateName())) {
                        return true;
                    }
                    else if (fact1.values().length!=fact2.values().length) {
                        return true;
                    }
                    else {
                        // skip id
                        // TODO: skip instruction counter too
                        for (int i = 1; i < fact1.values().length; i++) {
                            if (!fact1.values()[i].equals(fact2.values()[i])) {
                                return true;
                            }
                        }
                        return false;
                    }
                }
            }
        }
        else {
            if (!derivation1.getId().equals(derivation2.getId())) {
                return true;
            }
            else if (derivation1.getChildren().size()!=derivation2.getChildren().size()) {
                return true;
            }
            else {
                for (int i = 0; i < derivation1.getChildren().size(); i++) {
                    if (isDiffBeforeNormalisation(derivation1.getChildren().get(i),derivation2.getChildren().get(i),provDB1,provDB2)) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    private static String asLink(Path path) {
        String name = path.toString();
        String link = "file://"+path.toFile().getAbsolutePath().toString();
        return "<a target=\"_blank\" href=\""+link+"\">"+name+"</a>";
    }

    private String htmlTableRow(Object... values) {
        return Stream.of(values)
            .map(Object::toString)
            .collect(Collectors.joining("</td><td>","<tr><td>","</td></tr>"));
    }

    private String htmlTableRow(String firstColCssClass, Object... values) {
        String firstTD = "<td class="+firstColCssClass+">";
        return Stream.of(values)
            .map(Object::toString)
            .collect(Collectors.joining("</td><td>","<tr>"+firstTD,"</td></tr>"));
    }

    private String htmlTableHeaderRow(String... values) {
        return Stream.of(values).collect(Collectors.joining("</td><th>","<tr><th>","</th></tr>"));
    }

    // assume that all facts have the same schema
    private String toHtml(Fact fact,ProvenanceDB provDB) {
        String html="<table>";
        Predicate predicate = fact.predicate();
        html+=htmlTableHeaderRow(
            Streams.concat(
                Stream.of("predicate"),
                Arrays.stream(predicate.getSlots()).map(p -> p.name())
            ).collect(Collectors.toUnmodifiableList()).toArray(new String[]{}
        ));
        html+=htmlTableRow(
            Streams.concat(
                Stream.of(fact.predicate().getName()),
                Stream.of(fact.values())
            ).collect(Collectors.toUnmodifiableList()).toArray(new Object[]{}
        ));
        html+="</table>";

        // display provenance
        html += "<h5>provenance:</h5>";
        String id = (String)fact.values()[0];
        try {
            DerivationNode root = ProvenanceParser.parse(id);
            html += "<table>";
            html += htmlTableHeaderRow("derivation tree", "kind"); // skipped details for better layout
            html += toHtmlTableRow(root,0,provDB);
            html += "</table>";

        } catch (Exception x) {
            LOG.error("Error parsing provenance for id \""+id+"\"",x);
            html += "<div>error parsing provenance !</div>";
        }

        return html;

    }

    private String toHtmlTableRow(DerivationNode node, int i,ProvenanceDB provDB) {
        String id = node.getId();

        String kind = "unknown";
        String detail = provDB.getRule(id);
        if (detail != null) {
            kind = "rule";
        }
        else {
            ProvenanceDB.FlatFact ffact = provDB.getEdbFact(id);
            if (ffact != null) {
                kind = "base fact extracted from bytecode";
                detail = Stream.of(ffact.values()).map(Object::toString).collect(Collectors.joining("\t"));
            }
            else {
                ffact = provDB.getIdbFact(id);
                if (ffact != null) {
                    kind = "inferred fact";
                    detail = Stream.of(ffact.values()).map(Object::toString).collect(Collectors.joining("\t"));
                    // detail = ffact.predicateName() + "\t" + detail;
                }
            }

        }

        String cssClass = "derivation-level-"+i;
        String html = htmlTableRow(cssClass, id,kind);  // try to skip detail for better layout
        for (DerivationNode child:node.getChildren()) {
            html += toHtmlTableRow(child,i+1,provDB);
        }
        return html;
    }

}
