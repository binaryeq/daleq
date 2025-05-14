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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CLI. Produces a html report.
 * @author jens dieytrich
 */
public class Main {

    private static final Option OPT_JAR1 = new Option("j1","jar1",true,"the first jar file to compare");
    private static final Option OPT_JAR2 = new Option("j2","jar2",true,"the second jar file to compare");
    private static final Option OUT = new Option("o","out",true,"the output folder where the report will be generated");

    private static final URL TEMPLATE = Main.class.getClassLoader().getResource("cli/report-template.html");
    private static final URL CSS = Main.class.getClassLoader().getResource("cli/daleq.css");

    static {
        OPT_JAR1.setRequired(true);
        OPT_JAR2.setRequired(true);
        OUT.setRequired(true);
    }

    public static final Analyser[] ANALYSERS = new Analyser[]{
        new ResourceIsPresentAnalyser(),
        new SameContentAnalyser()
    };

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {

        Options options = new Options();
        options.addOption(OPT_JAR1);
        options.addOption(OPT_JAR2);
        options.addOption(OUT);

        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);

            Path jar1 = Path.of(cmd.getOptionValue(OPT_JAR1));
            Path jar2 = Path.of(cmd.getOptionValue(OPT_JAR2));
            Path outPath = Path.of(cmd.getOptionValue(OUT));

            Preconditions.checkState(Files.exists(jar1));
            Preconditions.checkState(!Files.isDirectory(jar1));
            Preconditions.checkState(Files.exists(jar2));
            Preconditions.checkState(!Files.isDirectory(jar2));
            Preconditions.checkState(Files.exists(outPath));
            Preconditions.checkState(Files.isDirectory(outPath));

            Preconditions.checkState(TEMPLATE!=null);

            analyse(jar1,jar2,outPath);

        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar <built-jar>", options);
            System.exit(1);
        }
    }

    private static void analyse(Path jar1, Path jar2, Path outPath) throws IOException {
        Set<String> content1 = IOUtil.nonDirEntries(jar1);
        Set<String> content2 = IOUtil.nonDirEntries(jar2);
        List<String> joinedContent = Sets.union(content1, content2).stream().sorted().collect(Collectors.toUnmodifiableList());

        String html = Files.readString(Path.of(TEMPLATE.getPath()));
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
        for (Analyser analyser:ANALYSERS) {
            headerRow+="<th>";
            headerRow+= analyser.name();
            headerRow+="</th>";
        }
        headerRow+="</tr>";
        table.append(headerRow);

        for (String resource:joinedContent) {
            String row = "<tr><td>";
            row+=resource;
            row+="</td>";
            for (Analyser analyser:ANALYSERS) {
                AnalysisResult analyserResult = analyser.analyse(resource,jar1,jar2);
                row+="<td class=\"";
                row+=getCSSClass(analyserResult);
                row+="\">";
                row+=analyserResult.state();
                row+="</td>";
            }
            table.append(row);
        }


        Files.write(report, document.html().getBytes());
        Path css = outPath.resolve("daleq.css");
        Files.copy(Path.of(CSS.getPath()),css, StandardCopyOption.REPLACE_EXISTING);
        LOG.info("report written to {}", report);

        new ProcessBuilder("open",report.toFile().getAbsolutePath())
            .inheritIO()
            .start();
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
