package io.github.bineq.daleq.evaluation;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import io.github.bineq.daleq.IOUtil;
import io.github.bineq.daleq.Rules;
import io.github.bineq.daleq.Souffle;
import io.github.bineq.daleq.edb.FactExtractor;
import io.github.bineq.daleq.evaluation.tools.Diff;
import io.github.bineq.daleq.evaluation.tools.Javap;
import io.github.bineq.daleq.idb.IDB;
import io.github.bineq.daleq.idb.IDBPrinter;
import io.github.bineq.daleq.idb.IDBReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Run an analysis to evaluate daleq-based equivalence.
 * Compares it with other equivalences.
 * @author jens dietrich
 */
public class RunComparativeEvaluation {

    final static Logger LOG = LoggerFactory.getLogger(RunComparativeEvaluation.class);
    static final RunEvaluation.DB_RETENTION_POLICY RETENTION_POLICY = RunEvaluation.DB_RETENTION_POLICY.ZIP;

    private static Path VALIDATION_DB = null;
    private static final boolean REUSE_IDB = true;

    static final Path JNORM = Path.of("tools/jnorm-cli-1.0.0.jar");

    record ComparativeEvaluationResultRecord(String gav, String provider1, String provider2,String clazz, ComparisonResult result4javap, ComparisonResult result4jnorm, ComparisonResult result4daleq) {
        String toCSVLine() {
            return  List.of(gav,provider1,provider2,clazz,result4javap.toString(),result4jnorm.toString(),result4daleq.toString())
            .stream().collect(Collectors.joining("\t"));
        }
        static String getCSVHeaderLine() {
            return
                List.of("gav","provider1","provider2","class","javap","jnorm","daleq")
                .stream().collect(Collectors.joining("\t"));
        }
    }

    public static void main (String[] args) throws Exception {

        try {
            Preconditions.checkArgument(args.length > 1, "at least the output folder and two datasets (index files *.tsv) are required");
            Preconditions.checkArgument(Files.exists(JNORM));

            VALIDATION_DB = Path.of(args[0]);
            // delete db folder if it exists
            if (Files.exists(VALIDATION_DB) && !REUSE_IDB) {
                try {
                    IOUtil.deleteDir(VALIDATION_DB);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            // create new folder
            if (!Files.exists(VALIDATION_DB)) {
                try {
                    Files.createDirectories(VALIDATION_DB);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }


            List<Path> datasets = Stream.of(args).skip(1)
                    .map(arg -> {
                        Path path = Path.of(arg);
                        Preconditions.checkArgument(Files.exists(path));
                        Preconditions.checkArgument(path.toString().endsWith(".tsv"));
                        return path;
                    })
                    .collect(Collectors.toUnmodifiableList());

            LOG.info("Comparing: " + datasets.stream().map(p -> p.toString()).collect(Collectors.joining(",")));

            List<String> providers = datasets.stream()
                    .map(f -> RunEvaluation.getProviderName(f))
                    .collect(Collectors.toUnmodifiableList());

            List<Set<Record>> setsOfRecords = datasets.stream()
                    .map(f -> {
                        try {
                            LOG.info("Parsing records from " + f);
                            Set<Record> records = RunEvaluation.parseRecords(f);
                            LOG.info("\t" + records.size() + " parsed");
                            return records;
                        } catch (IOException e) {
                            LOG.error("Error parsing " + f);
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toUnmodifiableList());

            List<ComparativeEvaluationResultRecord> results = new ArrayList<>();
            Map<Path,Map<String,Content>> cache = new ConcurrentHashMap<>();

            int N = datasets.size()*(datasets.size()-1)/2;
            AtomicInteger pairsOfJarsRecordCounter = new AtomicInteger(0);
            AtomicInteger classesComparedCounter = new AtomicInteger(0);
            AtomicInteger pairOfRecordsCounter = new AtomicInteger(0);
            AtomicInteger bothJarsEmptyCounter = new AtomicInteger(0);
            AtomicInteger equalClassCounter = new AtomicInteger(0);
            AtomicInteger nonEqualClassCounter = new AtomicInteger(0);
            Set<String> gavs = new HashSet();

            for (int i = 0; i < datasets.size(); i++) {
                String provider1 = providers.get(i);
                Set<Record> records1 = setsOfRecords.get(i);
                for (int j = 0; j < i; j++) {
                    pairsOfJarsRecordCounter.incrementAndGet();

                    String provider2 = providers.get(j);
                    Set<Record> records2 = setsOfRecords.get(j);

                    // GUARD TO ONLY COMPARE RECORDS WITH MATCHING SOURCE FILES !
                    Set<PairOfRecords> pairsOfRecords = RunEvaluation.findMatchingRecordsWithSameSources(provider1, provider2, records1, records2, 1);

                    LOG.info("Matching records (GAVs with equivalent sources for both providers): " + pairsOfRecords.size());
                    LOG.info("\tprogress: " + pairsOfJarsRecordCounter + " / " + N);
                    LOG.info("\tprovider1: " + provider1);
                    LOG.info("\tprovider2: " + provider2);

                    AtomicInteger counter2 = new AtomicInteger(0);

                    // serial makes debugging easier as records appear in predictable order in results
                    pairsOfRecords.stream().forEach(pairOfRecords -> {
                        pairOfRecordsCounter.incrementAndGet();
                        counter2.incrementAndGet();
                        if (counter2.get()%10==0) {
                            LOG.info("\tprogress dataset pair " + pairsOfJarsRecordCounter.get() + "/" + N + " , jar(s) " + counter2.get() + "/" + pairsOfRecords.size());
                        }
                        LOG.debug("Loading classes for {} with providers {} and {}",pairOfRecords.left().gav(),provider1,provider2);

                        Path jar1 = pairOfRecords.left().binMainFile();
                        Path jar2 = pairOfRecords.right().binMainFile();

                        try {
                            Map<String, Content> classes1 = RunEvaluation.loadClasses(cache, jar1);
                            Map<String, Content> classes2 = RunEvaluation.loadClasses(cache, jar2);
                            if (classes1.size()==0 && classes2.size()==0) {
                                bothJarsEmptyCounter.incrementAndGet();
                            }
                            String gav = pairOfRecords.left().gav();
                            assert gav.equals(pairOfRecords.right().gav());
                            gavs.add(gav);
                            Set<String> commonClasses = Sets.intersection(classes1.keySet(), classes2.keySet());

                            commonClasses.stream().forEach(commonClass -> {
                                Content clazz1 = classes1.get(commonClass);
                                Content clazz2 = classes2.get(commonClass);

                                String nClassName = commonClass.replace("/",".").replace(".class","");
                                // also replace $ char -- this creates issue with souffle
                                nClassName = RunEvaluation.escapeDollarChar(nClassName);
                                Path analysisDir4Gav = VALIDATION_DB.resolve(gav);
                                Path analysisDir4GavNClass = analysisDir4Gav.resolve(nClassName);

                                // LOG.info("TODO: compare classes {}",commonClass);

                                try {
                                    byte[] bytecode1 = clazz1.load();
                                    byte[] bytecode2 = clazz2.load();

                                    // only compare if different
                                    if (!Arrays.equals(bytecode1,bytecode2)) {
                                        nonEqualClassCounter.incrementAndGet();
                                        ComparisonResult result4Daleq = compareUsingDaleq(pairOfRecords.left().gav(), provider1, provider2, commonClass, bytecode1, bytecode2, analysisDir4GavNClass);
                                        ComparisonResult result4JNorm = compareUsingJNorm(pairOfRecords.left().gav(), provider1, provider2, jar1, jar2, commonClass, bytecode1, bytecode2, analysisDir4Gav, analysisDir4GavNClass);
                                        ComparisonResult result4Javap = compareUsingJavap(pairOfRecords.left().gav(), provider1, provider2, commonClass, bytecode1, bytecode2, analysisDir4GavNClass);

                                        ComparativeEvaluationResultRecord resultRecord = new ComparativeEvaluationResultRecord(
                                            gav,
                                            provider1,
                                            provider2,
                                            commonClass,
                                            result4Javap,
                                            result4JNorm,
                                            result4Daleq
                                        );
                                        results.add(resultRecord);
                                    }
                                    else {
                                        equalClassCounter.incrementAndGet();
                                    }
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }

                                classesComparedCounter.incrementAndGet();
                            });
                        }
                        catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });

                    // save results
                    List<String> lines = results.stream()
                        .map(ComparativeEvaluationResultRecord::toCSVLine)
                        .collect(Collectors.toList());
                    lines.add(0,ComparativeEvaluationResultRecord.getCSVHeaderLine());

                    String resultFileName = provider1 + "-" + provider2 + ".csv";
                    Path resultFile = VALIDATION_DB.resolve(resultFileName);
                    Files.write(resultFile, lines);

                    LOG.info("results written to {}", resultFile);
                }

                LOG.info("pairs of records processed: {}",pairOfRecordsCounter.get());


                // some statistics
                LOG.info("jars compared: {}",gavs.size()*2);
                LOG.info("pairs where both jars have no .class files: {}",bothJarsEmptyCounter.get());
                LOG.info("classes compared: {}",classesComparedCounter.get());
                LOG.info("classes compared - equal: {}",equalClassCounter.get());
                LOG.info("classes compared - non-equal: {}",nonEqualClassCounter.get());
                LOG.info("classes equivalent wrt javap: {}",results.stream().filter(r -> r.result4javap==ComparisonResult.EQUIVALENT).count());
                LOG.info("classes equivalent wrt jnorm: {}",results.stream().filter(r -> r.result4jnorm==ComparisonResult.EQUIVALENT).count());
                LOG.info("classes equivalent wrt daleq: {}",results.stream().filter(r -> r.result4daleq==ComparisonResult.EQUIVALENT).count());
                LOG.info("classes with error wrt javap: {}",results.stream().filter(r -> r.result4javap==ComparisonResult.ERROR).count());
                LOG.info("classes with error wrt jnorm: {}",results.stream().filter(r -> r.result4jnorm==ComparisonResult.ERROR).count());
                LOG.info("classes with error wrt daleq: {}",results.stream().filter(r -> r.result4daleq==ComparisonResult.ERROR).count());

                LOG.info("classes equivalent wrt jnorm not daleq: {}",results.stream().filter(r -> r.result4jnorm==ComparisonResult.EQUIVALENT).filter(r -> r.result4daleq==ComparisonResult.NON_EQUIVALENT).count());
                LOG.info("classes equivalent wrt javap not daleq: {}",results.stream().filter(r -> r.result4javap==ComparisonResult.EQUIVALENT).filter(r -> r.result4daleq==ComparisonResult.NON_EQUIVALENT).count());

            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    private static ComparisonResult compareUsingJavap(String gav, String provider1, String provider2, String commonClass, byte[] bytecode1, byte[] bytecode2, Path analysisDir) throws Exception {
        if (Arrays.equals(bytecode1, bytecode2)) {
            return ComparisonResult.EQUAL;
        }

        try {
            String disassembled1 = javap(gav, provider1, commonClass, bytecode1,analysisDir);
            assert disassembled1 != null;
            String disassembled2 = javap(gav, provider2, commonClass, bytecode2,analysisDir);
            assert disassembled2 != null;

            if (disassembled1.equals(disassembled2)) {
                return ComparisonResult.EQUIVALENT;
            } else {
                Path diff = analysisDir.resolve("javap/javap-diff.txt");
                Diff.diffAndExport(disassembled1,disassembled2,diff);
                return ComparisonResult.NON_EQUIVALENT;
            }
        }
        catch (Exception e) {
            return ComparisonResult.ERROR;
        }

    }

    private static String javap(String gav, String provider, String className, byte[] bytecode,Path analysisDir) throws IOException {
        Path root = analysisDir.resolve("javap");
        root = root.resolve(provider);
        Path javapFile = root.resolve(className.replace(".class", ".javap"));
        Path classFile = root.resolve(className);
        if (!Files.exists(javapFile) || !Files.isRegularFile(classFile)) {
            Files.createDirectories(classFile.getParent());
            Files.write(classFile, bytecode);
            byte[] javap = Javap.run(classFile, javapFile);
            return new String(javap);
        }
        else {
            assert Files.exists(javapFile);
            return Files.readString(javapFile);
        }
    }

    private static ComparisonResult compareUsingDaleq(String gav, String provider1, String provider2, String commonClass, byte[] bytecode1, byte[] bytecode2, Path analysisDir) throws Exception {
        if (Arrays.equals(bytecode1, bytecode2)) {
            return ComparisonResult.EQUAL;
        }

        try {
            String idb1 = computeAndSerializeIDB(gav, provider1, commonClass, bytecode1,analysisDir);
            assert idb1 != null;
            String idb2 = computeAndSerializeIDB(gav, provider2, commonClass, bytecode2,analysisDir);
            assert idb2 != null;

            if (idb1.equals(idb2)) {
                return ComparisonResult.EQUIVALENT;
            } else {
                Path diff = analysisDir.resolve("daleq/daleq-diff.txt");
                Diff.diffAndExport(idb1,idb2,diff);
                return ComparisonResult.NON_EQUIVALENT;
            }
        }
        catch (Exception e) {
            return ComparisonResult.ERROR;
        }

    }

    private static String computeAndSerializeIDB (String gav, String provider, String className, byte[] bytecode,Path analysisDir) throws Exception {

        Path root = analysisDir.resolve("daleq");
        root = root.resolve(provider);
        Path edbRoot = root.resolve("edb");
        Path edbFactDir = edbRoot.resolve( "facts");
        Path edbDef = edbRoot.resolve("db.souffle");
        Path idbRoot = root.resolve("idb");
        Path idbFactDir = idbRoot.resolve( "facts");
        Path mergedEDBAndRules = root.resolve("mergedEDBAndRules.souffle");
        Path idbPrintout = root.resolve("idb-full.txt");
        Path idbProjectedPrintout = root.resolve("idb-projected.txt");

        if (Files.exists(idbProjectedPrintout)) {
            LOG.info("Using already computed IDB (projected printout) {}", idbProjectedPrintout);
            String report = Files.readAllLines(idbProjectedPrintout).stream().collect(Collectors.joining(System.lineSeparator()));
            return report;
        }
        else {

            long time = System.currentTimeMillis();
            Files.createDirectories(edbFactDir);

            // copy bytecode to file as fact extraction used files as input
            Path classFile = root.resolve(className.substring(className.lastIndexOf("/") + 1));
            Files.write(classFile, bytecode);

            // build EDB
            if (Files.exists(edbDef)) {
                IOUtil.deleteDir(edbFactDir);
            }
            else {
                Files.createDirectories(edbFactDir);
            }

            try {
                FactExtractor.extractAndExport(classFile, edbDef, edbFactDir, true);
                LOG.info("EBD extracted for {} in {} provided by {} in dir {}", className, gav, provider, edbRoot);

                if (Files.exists(idbFactDir)) {
                    IOUtil.deleteDir(idbFactDir);
                } else {
                    Files.createDirectories(idbFactDir);
                }

                Souffle.createIDB(edbDef, Rules.defaultRules(), edbFactDir, idbFactDir, mergedEDBAndRules);
                LOG.info("IBD computed for {} in {} provided by {} in dir {}", className, gav, provider, idbFactDir);

                // there might be a race condition is souffle that some background thread is still writing the IDB when createIDB returns
                // there have been cased when facts where missing, leading to NPEs when printing the IDB
                // but upon inspection, those facts where there
                // try to mitigate with this for now
                Thread.sleep(500);

                // load IDB
                IDB idb = IDBReader.read(idbFactDir);

                String idbOut = IDBPrinter.print(idb);
                String idbProjectedOut = IDBPrinter.print(idb.project());

                Files.write(idbPrintout, idbOut.getBytes());
                Files.write(idbProjectedPrintout, idbProjectedOut.getBytes());

                // cleanup !
                RunEvaluation.cleanupDBDir(edbRoot, RETENTION_POLICY);
                RunEvaluation.cleanupDBDir(idbRoot, RETENTION_POLICY);
                RunEvaluation.cleanupFile(mergedEDBAndRules, RETENTION_POLICY);
                RunEvaluation.cleanupFile(idbPrintout, RETENTION_POLICY);

                long duration = System.currentTimeMillis() - time;
                Path timeTaken = root.resolve("computation-time-in-ms.txt");
                Files.write(timeTaken, String.valueOf(duration).getBytes());

                return idbProjectedOut;
            }
            catch (Exception e) {
                Path errorLog = root.resolve("error.txt");
                try (PrintWriter out = new PrintWriter(errorLog.toFile())) {
                    e.printStackTrace(out);
                }
                throw e;
            }
        }
    }

    private static ComparisonResult compareUsingJNorm(String gav, String provider1, String provider2, Path jar1, Path jar2, String commonClass, byte[] bytecode1, byte[] bytecode2, Path analysisDir4Gav,Path analysisDir4GavNClass) throws Exception {
        if (Arrays.equals(bytecode1, bytecode2)) {
            return ComparisonResult.EQUAL;
        }

        try {
            String jimple1 = jnorm(gav, provider1, jar1, commonClass, bytecode1,analysisDir4Gav,analysisDir4GavNClass);
            assert jimple1 != null;
            String jimple2 = jnorm(gav, provider2, jar2, commonClass, bytecode2,analysisDir4Gav,analysisDir4GavNClass);
            assert jimple2 != null;

            if (jimple1.equals(jimple2)) {
                return ComparisonResult.EQUIVALENT;
            } else {
                Path diff = analysisDir4GavNClass.resolve("jnorm/jnorm-diff.txt");
                Diff.diffAndExport(jimple1,jimple2,diff);
                return ComparisonResult.NON_EQUIVALENT;
            }
        }
        catch (Exception e) {
            LOG.error("Error evaluating jnorm-based equivalance",e);
            return ComparisonResult.ERROR;
        }
    }

    private static String jnorm(String gav, String provider, Path jar, String className, byte[] bytecode,Path analysisDir4Gav,Path analysisDir4GavNClass) throws IOException, InterruptedException {
        Path dir1 = analysisDir4GavNClass.resolve("jnorm").resolve(provider);
        Path dir2 = analysisDir4Gav.resolve("__jnorm-jar-cache").resolve(provider);
        if (!Files.exists(dir1)) {
            Files.createDirectories(dir1);
        }
        if (!Files.exists(dir2)) {
            Files.createDirectories(dir2);
        }
        Path jnormOutDir = dir2.resolve("jnormalised");
        Path jnormError = dir2.resolve("jnorm-error.txt");
        Path jimpleFile = dir1.resolve(className.replace(".class", ".jimple"));
        Path classFile = dir1.resolve(className);
        if (!Files.exists(jnormOutDir)) {
            int status = jnorm(jar,jnormOutDir,jnormError);
            if (status != 0) {
                // todo: parse error file and check whether error is cased by jnorm (asm) not supporting bytecode version
                // could handle those as SKIP instead of error
                throw new IOException("Error running jnorm");
            }
        }

        assert Files.exists(jnormOutDir);
        String jnormOutputName = className.replace("/",".").replace(".class",".jimple");
        Path jnormOutput = jnormOutDir.resolve(jnormOutputName);

        if (!Files.exists(jnormOutput)) {
            LOG.warn("jimple file for {} :: {} ({}) not generated",gav,className,provider);
            throw new RuntimeException("jimple file not generated");
        }

        if (!Files.exists(jimpleFile.getParent())) {
            Files.createDirectories(jimpleFile.getParent());
        }
        Files.copy(jnormOutput, jimpleFile, StandardCopyOption.REPLACE_EXISTING);

        Files.write(classFile, bytecode);

        return Files.readString(jimpleFile);

    }

    private static int jnorm(Path jar, Path jnormJar,Path errorFile) throws IOException, InterruptedException {
        LOG.info("running jnorm on {} , output saved to {}", jar, jnormJar);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Process process = new ProcessBuilder()
            .command("/Library/Java/JavaVirtualMachines/jdk-11.0.11.jdk/Contents/Home/bin/java","-jar",JNORM.toString(),"-n","-i",jar.toString(),"-d",jnormJar.toString())
            .inheritIO()
            .redirectError(errorFile.toFile())
            .start();
        return process.waitFor();
    }

}
