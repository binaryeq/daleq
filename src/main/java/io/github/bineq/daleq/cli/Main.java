package io.github.bineq.daleq.cli;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import io.github.bineq.daleq.IOUtil;
import org.apache.commons.cli.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CLI. Produces a html report.
 * @author jens dietrich
 */
public class Main {

    // for compatibility -- aliases -p1 and -p2 for java
    private static final Option OPT_JAR1 = new Option("j1","jar1",true,"the first jar file to compare (required)");
    private static final Option OPT_JAR2 = new Option("j2","jar2",true,"the second jar file to compare (required)");

    private static final Option OPT_PCK1 = new Option("p1","pck1",true,"the first package file to compare - either .jar (for JVM/Maven packages) or .whl (for python packages) (required)");
    private static final Option OPT_PCK2 = new Option("p2","pck2",true,"the second package file to compare .jar (for JVM/Maven packages) or .whl (for python packages) (required)");
    private static final Option OPT_SRCJAR1 = new Option("s1","src1",true,"the first jar file with source code to compare (only if JVM/Maven packages are analysed - optional)");
    private static final Option OPT_SRCJAR2 = new Option("s2","src2",true,"the second jar file with source code to compare (only if JVM/Maven packages are analysed - optional)");
    private static final Option OUT = new Option("o","out",true,"the output folder where the report will be generated (required)");
    private static final Option OPT_AUTO_OPEN_REPORT = new Option("a","autoopen",false,"if set, the generated html report will be opened automatically (don't use this for CI integration)");
    private static final Option OPT_DALEQ = new Option("dq","daleq",true,"one of {sound,soundy,both}, default is soundy");

    private static final URL TEMPLATE = Main.class.getClassLoader().getResource("cli/report-template.html");
    private static final URL CSS = Main.class.getClassLoader().getResource("cli/daleq.css");

    enum DaleqAnalyserType {sound,soundy,both};
    enum PackageFormat {MavenArtifact,PythonWheel};
    private static final DaleqAnalyserType DEFAULT_DALEQ_ANALYSER_TYPE = DaleqAnalyserType.soundy;

    static {
        OPT_PCK1.setRequired(true);
        OPT_PCK2.setRequired(true);
        OUT.setRequired(true);
    }

    // keep track of exit state

    // EXIT STATES

    public static final int EXIT_CLASSES_EQUAL__RESOURCES_EQUAL__SOURCES_EQUIVALENT = 0;
    public static final int EXIT_CLASSES_EQUIVALENT__RESOURCES_EQUAL__SOURCES_EQUIVALENT = 1;
    public static final int EXIT_ALERT = 2;
    public static final int EXIT_CLI_ERROR = 3;

    // make testable
    static int EXIT_STATE = EXIT_CLASSES_EQUAL__RESOURCES_EQUAL__SOURCES_EQUIVALENT;

    public static final List<Analyser> ANALYSERS =
        ServiceLoader.load(Analyser.class).stream()
            .map(p -> p.get())
            .sorted(Comparator.comparingInt(Analyser::positionHint))
            .collect(Collectors.toUnmodifiableList());

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {

        Options options = new Options();
        options.addOption(OPT_PCK1);
        options.addOption(OPT_PCK2);
        options.addOption(OPT_SRCJAR1);
        options.addOption(OPT_SRCJAR2);
        options.addOption(OUT);
        options.addOption(OPT_AUTO_OPEN_REPORT);
        options.addOption(OPT_DALEQ);

        PackageFormat pckFormat = null;

        CommandLineParser parser = new DefaultParser();


        try {
            CommandLine cmd = parser.parse(options, args);

            String daleqAnalyserTypeName = cmd.getOptionValue(OPT_DALEQ);
            DaleqAnalyserType daleqAnalyserType = null;
            if (daleqAnalyserTypeName==null) {
                daleqAnalyserType = DEFAULT_DALEQ_ANALYSER_TYPE;
            }
            else {
                daleqAnalyserType = DaleqAnalyserType.valueOf(daleqAnalyserTypeName);
            }


            Preconditions.checkNotNull(cmd.hasOption(OPT_PCK1) || cmd.hasOption(OPT_JAR1),"Either " + OPT_PCK1.getArgName() + " or " + OPT_JAR1.getArgName() + " must be specified");
            Preconditions.checkNotNull(cmd.hasOption(OPT_PCK2) || cmd.hasOption(OPT_JAR2),"Either " + OPT_PCK2.getArgName() + " or " + OPT_JAR2.getArgName() + " must be specified");

            Path pck1 = cmd.hasOption(OPT_PCK1) ? Path.of(cmd.getOptionValue(OPT_PCK1)) :  Path.of(cmd.getOptionValue(OPT_JAR1));
            Path pck2 = cmd.hasOption(OPT_PCK2) ? Path.of(cmd.getOptionValue(OPT_PCK2)) :  Path.of(cmd.getOptionValue(OPT_JAR2));

            String extension1 = getExtension(pck1);
            String extension2 = getExtension(pck2);

            Preconditions.checkState(extension1.equals("whl") || extension1.equals("jar"),"unrecognised file extension: ."   + extension1);
            Preconditions.checkState(extension2.equals("whl") || extension2.equals("jar"),"unrecognised file extension: ."   + extension2);
            Preconditions.checkState(Objects.equals(extension1,extension2),"the file extensions (package formats) for the packages compared must be consistent");

            String pckFormatName = null;
            if (extension1.equals("whl")) {
                pckFormat = PackageFormat.PythonWheel;
                pckFormatName = "python wheels";
            }
            else if (extension2.equals("jar")) {
                pckFormat = PackageFormat.MavenArtifact;
                pckFormatName = "Maven artifacts";
            }
            assert pckFormat != null;



            LOG.info("Comparing {} {} and {}",pckFormatName,pck1,pck2);

            Path outPath = Path.of(cmd.getOptionValue(OUT));

            Preconditions.checkState(Files.exists(pck1),"File does not exist: " + pck1);
            Preconditions.checkState(!Files.isDirectory(pck1), "File is a folder: " + pck1);
            Preconditions.checkState(Files.exists(pck2),"File does not exist: " + pck2);
            Preconditions.checkState(!Files.isDirectory(pck2), "File is a folder: " + pck2);

            boolean autoOpenReport = cmd.hasOption(OPT_AUTO_OPEN_REPORT);

            Path src1 = null;
            Path src2 = null;
            if (cmd.hasOption(OPT_SRCJAR1)) {
                src1 = Path.of(cmd.getOptionValue(OPT_SRCJAR1));
                Preconditions.checkState(Files.exists(src1));
                Preconditions.checkState(!Files.isDirectory(src1));
            }
            if (cmd.hasOption(OPT_SRCJAR2)) {
                src2 = Path.of(cmd.getOptionValue(OPT_SRCJAR2));
                Preconditions.checkState(Files.exists(src2));
                Preconditions.checkState(!Files.isDirectory(src2));
            }

            if (!outPath.toFile().exists()) {
                Files.createDirectories(outPath);
            }
            Preconditions.checkState(Files.isDirectory(outPath));
            Preconditions.checkState(TEMPLATE!=null);

            analyse(pck1,pck2,src1,src2,outPath,autoOpenReport,daleqAnalyserType,pckFormat);

            System.exit(EXIT_STATE);

        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -DSOUFFLE=<souffle-executable> -jar <built-jar>", options);
            System.exit(EXIT_CLI_ERROR);
        }
    }

    static List<Analyser> getAnalysers(boolean javaSourcesAvailable, DaleqAnalyserType daleqAnalyserType,PackageFormat pckFormat) {
        return ANALYSERS.stream()

            // filter by package format
            .filter(analyser -> {
                if (analyser.analysedFiletypes().contains(AnalysedResourceType.Any)) {
                    return true;
                }
                else if (analyser.analysedFiletypes().contains(AnalysedResourceType.Text)) {
                    return true;
                }
                else if (analyser.analysedFiletypes().contains(AnalysedResourceType.Properties)) {
                    return true;
                }
                else if (pckFormat == PackageFormat.PythonWheel) {
                    return analyser.analysedFiletypes().contains(AnalysedResourceType.PythonSourceCode);
                }
                else if (pckFormat == PackageFormat.MavenArtifact) {
                    if  (analyser.analysedFiletypes().contains(AnalysedResourceType.JavaByteCode)) {
                        return true;
                    }
                    else if (javaSourcesAvailable && analyser.analysedFiletypes().contains(AnalysedResourceType.JavaSourceCode)) {
                        return true;
                    }
                }
                LOG.warn("Excluded analyser {} as it does not match inclusion patterns",analyser.name());
                return false;
            })

            // filter by soundness
            .filter(analyser -> {
                if (analyser instanceof AbstractDaleqAnalyser)  {
                    if (analyser.isSound() == SoundnessLevel.SOUND) {
                        return daleqAnalyserType==DaleqAnalyserType.sound || daleqAnalyserType==DaleqAnalyserType.both ;
                    }
                    else {
                        return daleqAnalyserType==DaleqAnalyserType.soundy || daleqAnalyserType==DaleqAnalyserType.both ;
                    }
                }
                else {
                    return true;
                }
            })
            .collect(Collectors.toUnmodifiableList());
    }

    private static String getExtension(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".") + 1) : "";
    }


    private static void analyse(Path jar1, Path jar2, Path src1, Path src2, Path outPath,boolean autoOpenReport,DaleqAnalyserType daleqAnalyserType,PackageFormat pckFormat) throws IOException {

        boolean sourceAvailable = src1!=null && src2!=null;
        List<Analyser> analysers = getAnalysers(sourceAvailable,daleqAnalyserType, pckFormat);

        LOG.info("Initialising analysers");
        for (Analyser analyser:analysers) {
            LOG.info("Initialising analyser {}", analyser.getClass().getSimpleName());
            analyser.init(outPath);
        }

        Set<String> content1 = IOUtil.nonDirEntries(jar1);
        Set<String> content2 = IOUtil.nonDirEntries(jar2);
        List<String> joinedContent = Sets.union(content1, content2).stream().sorted().collect(Collectors.toUnmodifiableList());

        String html = IOUtil.readAsString(TEMPLATE);
        Document document = Parser.htmlParser().parseInput(html,"");

        Element eJar1 = document.getElementById("jar1");
        Element eJar2 = document.getElementById("jar2");
        Element table = document.getElementById("result-table").getElementsByTag("tbody").get(0);
        assert eJar1!=null;
        assert eJar2!=null;
        assert table!=null;

        eJar1.append(jar1.toString());
        eJar2.append(jar2.toString());

        Path report = outPath.resolve("report.html");

        String headerRow = "<tr><th>resource</th>";
        for (Analyser analyser:analysers) {
            headerRow+=String.format("<th>%s</th>",analyser.name());
        }
        headerRow+="</tr>";
        table.append(headerRow);

        for (String resource:joinedContent) {
            String row = "<tr><td>";
            row+=resource;
            row+="</td>";
            for (Analyser analyser:analysers) {
                AnalysisResult analyserResult = analyser.isBytecodeAnalyser() ?
                    analyser.analyse(resource,jar1,jar2,outPath):
                    analyser.analyse(resource,src1,src2,outPath);
                row+=String.format("<td class=\"%s\">",getCSSClass(analyserResult));
                row+=analyserResult.state();

                // create links for attachments
                for (AnalysisResultAttachment attachment:analyserResult.attachments()) {
                    row+=String.format("  <a href=\"%s\" target=\"_blank\" title=\"%s\">%s</a>", attachment.link(),attachment.name(),getLinkSymbol(attachment.kind()));
                }
                row+="</td>";


                recordState(analyser,analyserResult,resource);
            }
            table.append(row);
        }

        Files.write(report, document.html().getBytes());
        Path css = outPath.resolve("daleq.css");
        Files.copy(CSS.openStream(),css, StandardCopyOption.REPLACE_EXISTING);
        LOG.info("report written to {}", report);

        if (autoOpenReport) {
            new ProcessBuilder("open", report.toFile().getAbsolutePath())
                .inheritIO()
                .start();
        }
    }

    /**
     * Record the state to be used to compute the return code.
     */
    static void recordState(Analyser analyser, AnalysisResult result,String resource) {

        if (resource.endsWith(".class")) {
            if (analyser instanceof SameContentAnalyser) {
                if (result.state()==AnalysisResultState.FAIL || result.state()==AnalysisResultState.ERROR) {
                    EXIT_STATE = Math.max(EXIT_STATE,EXIT_CLASSES_EQUIVALENT__RESOURCES_EQUAL__SOURCES_EQUIVALENT);
                }
            }
            else if (analyser instanceof AbstractDaleqAnalyser) {
                if (result.state()==AnalysisResultState.FAIL || result.state()==AnalysisResultState.ERROR) {
                    EXIT_STATE = Math.max(EXIT_STATE,EXIT_ALERT);
                }
            }
            else if (analyser instanceof EquivalentSourceCodeAnalyser) {
                if (result.state()==AnalysisResultState.FAIL || result.state()==AnalysisResultState.ERROR) {
                    EXIT_STATE = Math.max(EXIT_STATE,EXIT_ALERT);
                }
            }
        }
        else {
            if (analyser instanceof SameContentAnalyser) {
                //  assert result.state()!=AnalysisResultState.SKIP;
                // it is possible that result.state()==AnalysisResultState.SKIP
                // this happens of the resource only exists in one of the jars !
                if (result.state()==AnalysisResultState.FAIL || result.state()==AnalysisResultState.ERROR) {
                    EXIT_STATE = Math.max(EXIT_STATE,EXIT_ALERT);
                }
            }
        }
    }


    private static String getLinkSymbol(AnalysisResultAttachment.Kind kind) {
        return switch (kind) {
            case ERROR -> "&#9888;"; // or &#x26A0;
            case DIFF -> "&Delta;";
            case INFO -> "&#8505;";
            default -> "?";
        };
    }

    private static String getCSSClass(AnalysisResult analyserResult) {
        return switch (analyserResult.state()) {
            case FAIL -> "fail";
            case PASS -> "pass";
            case SKIP -> "skip";
            case ERROR -> "error";
            default -> "unknown";
        };
    }
}
