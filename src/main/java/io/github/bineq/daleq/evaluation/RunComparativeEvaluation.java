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
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
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
                        try {
                            Map<String, Content> classes1 = RunEvaluation.loadClasses(cache, pairOfRecords.left().binMainFile());
                            Map<String, Content> classes2 = RunEvaluation.loadClasses(cache, pairOfRecords.right().binMainFile());
                            if (classes1.size()==0 && classes2.size()==0) {
                                bothJarsEmptyCounter.incrementAndGet();
                            }
                            String gav = pairOfRecords.left().gav();
                            assert gav.equals(pairOfRecords.right().gav());
                            Set<String> commonClasses = Sets.intersection(classes1.keySet(), classes2.keySet());

                            commonClasses.stream().forEach(commonClass -> {
                                Content clazz1 = classes1.get(commonClass);
                                Content clazz2 = classes2.get(commonClass);

                                String nClassName = commonClass.replace("/",".").replace(".class","");
                                // also replace $ char -- this creates issue with souffle
                                nClassName = RunEvaluation.escapeDollarChar(nClassName);
                                Path analysisDir = VALIDATION_DB.resolve(gav);
                                analysisDir = analysisDir.resolve(nClassName);

                                // LOG.info("TODO: compare classes {}",commonClass);

                                try {
                                    byte[] bytecode1 = clazz1.load();
                                    byte[] bytecode2 = clazz2.load();
                                    ComparisonResult result4Daleq = compareUsingDaleq(pairOfRecords.left().gav(), provider1, provider2, commonClass, bytecode1, bytecode2,analysisDir);
                                    ComparisonResult result4JNorm = ComparisonResult.UNKNOWN ; // TODO
                                    ComparisonResult result4Javap = compareUsingJavap(pairOfRecords.left().gav(), provider1, provider2, commonClass, bytecode1, bytecode2,analysisDir);

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
                LOG.info("pairs where both jars have no .class files: {}",bothJarsEmptyCounter.get());
                LOG.info("classes compared: {}",classesComparedCounter.get());





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
        Files.createDirectories(classFile.getParent());
        Files.write(classFile,bytecode);
        byte[] javap = Javap.run(classFile,javapFile);
        return new String(javap);
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



    /**
     * Find records with matching jars.
     * I.e. for each pair the following is true:
     * 1. the GAVs for both records are the same
     * 2. both have the same set of source files
     * 3. the commons source files have equivalent content (modulo some equivalence relation)
     * @param records1
     * @param records2
     * @return set of GAVs for which records exist in both sets
     */
    public static Set<PairOfRecords> findMatchingRecordsWithSameSources(String provider1, String provider2, Set<Record> records1, Set<Record> records2, int sourceEquivalenceMode) throws IOException {
        Preconditions.checkArgument(sourceEquivalenceMode >= -1 && sourceEquivalenceMode <= 1, "-se must be 1 (default), -1 or 0");

        // same GAVs
        final Set<PairOfRecords> commonRecords = RunEvaluation.findCommonRecords(records1,records2);

        if (sourceEquivalenceMode == 0) {
            return commonRecords;    // No need to compute source equivalence
        }

        Set<String> gavsWithSameSources = RunEvaluation.readFromSameSourcesCache(provider1,provider2);
        assert gavsWithSameSources!=null;
        Set<PairOfRecords> pairsOfRecords = commonRecords.stream()
            .filter(p -> gavsWithSameSources.contains(p.left().gav()))
            .collect(Collectors.toSet());

        LOG.info("Found {} matching gavs for providers {} and {}", pairsOfRecords.size(), provider1, provider2);

        Set<PairOfRecords> selectedPairsOfRecords = RunEvaluation.select(pairsOfRecords);
        LOG.info("Selected {} matching gavs for providers {} and {}", selectedPairsOfRecords.size(), provider1, provider2);

        return selectedPairsOfRecords;

    }


}
