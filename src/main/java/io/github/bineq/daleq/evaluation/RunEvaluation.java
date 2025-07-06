package io.github.bineq.daleq.evaluation;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import io.github.bineq.daleq.IOUtil;
import io.github.bineq.daleq.Rules;
import io.github.bineq.daleq.Souffle;
import io.github.bineq.daleq.edb.FactExtractor;
import io.github.bineq.daleq.idb.IDB;
import io.github.bineq.daleq.idb.IDBPrinter;
import io.github.bineq.daleq.idb.IDBReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Run an analysis to evaluate daleq-based equivalence.
 * @author jens dietrich
 */
public class RunEvaluation {

    final static Logger LOG = LoggerFactory.getLogger(RunEvaluation.class);

    final static Path SAME_SOURCE_CACHE = Path.of("evaluation/same_sources.json");
    private static final Map<String,Map<String,Set<String>>> GAVS_WITH_SAME_RESOURCES = loadSameSourcesCache();
    private static Path VALIDATION_DB = null;
    private static final boolean REUSE_IDB = true;

    enum DB_RETENTION_POLICY {DELETE,KEEP,ZIP};
    private static final  DB_RETENTION_POLICY RETENTION_POLICY = DB_RETENTION_POLICY.ZIP;

    private static final boolean COUNT_ONLY = false;


    public static void main (String[] args) throws Exception {

        try {

            Preconditions.checkArgument(args.length > 1, "at least the output folder and two datasets (index files *.tsv) are required");

            int sourceEquivalenceMode = 1;

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
                    .map(f -> getProviderName(f))
                    .collect(Collectors.toUnmodifiableList());

            List<Set<Record>> setsOfRecords = datasets.stream()
                    .map(f -> {
                        try {
                            LOG.info("Parsing records from " + f);
                            Set<Record> records = parseRecords(f);
                            LOG.info("\t" + records.size() + " parsed");
                            return records;
                        } catch (IOException e) {
                            LOG.error("Error parsing " + f);
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toUnmodifiableList());

            List<ResultRecord> results = new ArrayList<>();
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
                    Set<PairOfRecords> pairsOfRecords = findMatchingRecordsWithSameSources(provider1, provider2, records1, records2, sourceEquivalenceMode);

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
                            Map<String, Content> classes1 = loadClasses(cache, pairOfRecords.left().binMainFile());
                            Map<String, Content> classes2 = loadClasses(cache, pairOfRecords.right().binMainFile());
                            if (classes1.size()==0 && classes2.size()==0) {
                                bothJarsEmptyCounter.incrementAndGet();
                            }
                            String gav = pairOfRecords.left().gav();
                            assert gav.equals(pairOfRecords.right().gav());
                            Set<String> commonClasses = Sets.intersection(classes1.keySet(), classes2.keySet());

                            commonClasses.stream().forEach(commonClass -> {
                                Content clazz1 = classes1.get(commonClass);
                                Content clazz2 = classes2.get(commonClass);

                                // LOG.info("TODO: compare classes {}",commonClass);
                                if (!COUNT_ONLY) {
                                    ResultRecord resultRecord = null;
                                    try {
                                        resultRecord = compare(pairOfRecords.left().gav(), provider1, provider2, commonClass, clazz1.load(), clazz2.load());
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                    results.add(resultRecord);
                                }
                                classesComparedCounter.incrementAndGet();
                            });
                        }
                        catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                }

                LOG.info("pairs of records processed: {}",pairOfRecordsCounter.get());
                LOG.info("pairs where both jars have no .class files: {}",bothJarsEmptyCounter.get());
                LOG.info("classes compared: {}",classesComparedCounter.get());

                int equalClassesCount = (int)results.stream().filter(r -> r.result()==ComparisonResult.EQUAL).count();
                int equivalentClassesCount = (int)results.stream().filter(r -> r.result()==ComparisonResult.EQUIVALENT).count();
                int classPairWithEvaluationErrorCount = (int)results.stream().filter(r -> r.result()==ComparisonResult.ERROR).count();
                int differentClassesCount = (int)results.stream().filter(r -> r.result()==ComparisonResult.NON_EQUIVALENT).count();

                LOG.info("pairs of classes with same bytecode: {}",equalClassesCount);
                LOG.info("pairs of classes are equivalent (same IDB but diff bytecode): {}",equivalentClassesCount);
                LOG.info("pairs of classes that are different: {}",differentClassesCount);
                LOG.info("pairs of classes with error during evaluation: {}",classPairWithEvaluationErrorCount);

                List<ResultRecord> aggregatedResultRecordsForJars = aggregate(results);

                int allClassesInJarEqualCount = (int)aggregatedResultRecordsForJars.stream().filter(r -> r.result()==ComparisonResult.EQUAL).count();
                int allClassesInJarEquivalentCount = (int)aggregatedResultRecordsForJars.stream().filter(r -> r.result()==ComparisonResult.EQUIVALENT).count();
                int someClassesInJarWithEvaluationErrorCount = (int)aggregatedResultRecordsForJars.stream().filter(r -> r.result()==ComparisonResult.ERROR).count();
                int someClassesInJarNonEquivalentCount = (int)aggregatedResultRecordsForJars.stream().filter(r -> r.result()==ComparisonResult.NON_EQUIVALENT).count();

                LOG.info("pairs of jars with all classes having the same bytecode: {}",allClassesInJarEqualCount);
                LOG.info("pairs of jars with all classes being equivalent: {}",allClassesInJarEquivalentCount);
                LOG.info("pairs of jars with some classes not being equivalent: {}",someClassesInJarNonEquivalentCount);
                LOG.info("pairs of jars with some errors during evaluation: {}",someClassesInJarWithEvaluationErrorCount);


            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    private static List<ResultRecord> aggregate(List<ResultRecord> results) {
        Map<String,ResultRecord> resultsByJar = new HashMap<>();
        for (ResultRecord resultRecord : results) {
            ResultRecord result1 = new ResultRecord(resultRecord.gav(),resultRecord.provider1(),resultRecord.provider2(),"any",resultRecord.result());
            String gav = result1.gav();
            ResultRecord result2 = resultsByJar.get(gav);
            if (result2==null || result2.result().compareTo(result1.result())<0) {
                // need to add or replace value
                resultsByJar.put(gav,result1);
            }
        }
        return resultsByJar.values().stream().collect(Collectors.toList());
    }

    private static ResultRecord compare(String gav, String provider1, String provider2, String commonClass, byte[] bytecode1, byte[] bytecode2) throws Exception {
        if (Arrays.equals(bytecode1, bytecode2)) {
            return new ResultRecord(gav,provider1,provider2,commonClass, ComparisonResult.EQUAL);
        }

        try {
            String idb1 = computeAndSerializeIDB(gav, provider1, commonClass, bytecode1);
            assert idb1 != null;
            String idb2 = computeAndSerializeIDB(gav, provider2, commonClass, bytecode2);
            assert idb2 != null;

            if (idb1.equals(idb2)) {
                return new ResultRecord(gav, provider1, provider2, commonClass, ComparisonResult.EQUIVALENT);
            } else {
                return new ResultRecord(gav, provider1, provider2, commonClass, ComparisonResult.NON_EQUIVALENT);
            }
        }
        catch (Exception e) {
            return new ResultRecord(gav, provider1, provider2, commonClass, ComparisonResult.ERROR);
        }

    }

    private static String computeAndSerializeIDB (String gav, String provider, String className, byte[] bytecode) throws Exception {
        String nClassName = className.replace("/",".").replace(".class","");

        // also replace $ char -- this creates issue with souffle
        nClassName = escapeDollarChar(nClassName);

        Path root = VALIDATION_DB.resolve(gav);
        root = root.resolve(nClassName);
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
                LOG.info("EBD extracted for {} in {} provided by {} in dir {}", nClassName, gav, provider, edbRoot);

                if (Files.exists(idbFactDir)) {
                    IOUtil.deleteDir(idbFactDir);
                } else {
                    Files.createDirectories(idbFactDir);
                }

                Souffle.createIDB(edbDef, Rules.defaultRules(), edbFactDir, idbFactDir, mergedEDBAndRules);
                LOG.info("IBD computed for {} in {} provided by {} in dir {}", nClassName, gav, provider, idbFactDir);

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
                cleanupDBDir(edbRoot, RETENTION_POLICY);
                cleanupDBDir(idbRoot, RETENTION_POLICY);
                cleanupFile(mergedEDBAndRules, RETENTION_POLICY);
                cleanupFile(idbPrintout, RETENTION_POLICY);

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

    private static void cleanupDBDir(Path dir, DB_RETENTION_POLICY retentionPolicy) throws IOException {
        if (retentionPolicy==DB_RETENTION_POLICY.DELETE) {
            IOUtil.deleteDir(dir);
            LOG.debug("deleted fact db {}",dir);
        }
        else if (retentionPolicy==DB_RETENTION_POLICY.ZIP) {
            IOUtil.zipAndDeleteDir(dir);
            LOG.debug("zipped and deleted fact db {}",dir);
        }
        // nothing to do for DB_RETENTION_POLICY.KEEP
    }

    private static void cleanupFile(Path file, DB_RETENTION_POLICY retentionPolicy) throws IOException {
        if (retentionPolicy==DB_RETENTION_POLICY.DELETE) {
            Files.delete(file);
            LOG.debug("deleted file {}",file);
        }
        else if (retentionPolicy==DB_RETENTION_POLICY.ZIP) {
            IOUtil.zipFile(file.toFile(),file.getParent().resolve(file.getFileName().toString()+".zip").toFile());
            Files.delete(file);
            LOG.debug("zipped and deleted {}",file);
        }
        // nothing to do for DB_RETENTION_POLICY.KEEP
    }

    private static String escapeDollarChar(String s) {
        // some change of collision here !
        return s.replace("$","_____");
    }


    // load the classes from a jar
    private static Map<String,Content> loadClasses(Map<Path,Map<String,Content>> cache,Path jar) throws IOException {

        if (cache.containsKey(jar)) {
            return cache.get(jar);
        }
        else {
            LOG.debug("Loading classes from " + jar);
            Map<String, Content> classes = new HashMap<>();
            Set<String> entries = entries(jar.toFile(), f -> f.endsWith(".class"));
            for (String entry : entries) {
                Content content = new Content(jar, entry);
                classes.put(entry, content);
            }
            cache.put(jar,classes);
            return classes;
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
        final Set<PairOfRecords> commonRecords = findCommonRecords(records1,records2);

        if (sourceEquivalenceMode == 0) {
            return commonRecords;    // No need to compute source equivalence
        }

        Set<String> gavsWithSameSources = readFromSameSourcesCache(provider1,provider2);
        assert gavsWithSameSources!=null;
        Set<PairOfRecords> pairsOfRecords = commonRecords.stream()
            .filter(p -> gavsWithSameSources.contains(p.left().gav()))
            .collect(Collectors.toSet());

        LOG.info("Found {} matching gavs for providers {} and {}", pairsOfRecords.size(), provider1, provider2);

        Set<PairOfRecords> selectedPairsOfRecords = select(pairsOfRecords);
        LOG.info("Selected {} matching gavs for providers {} and {}", selectedPairsOfRecords.size(), provider1, provider2);

        return selectedPairsOfRecords;

    }

    // select pairs for analysis
    public static Set<PairOfRecords> select(Set<PairOfRecords> set) {
        // strategy -- pick only the first version (GAV) for each artifact (GA)

        Set<PairOfRecords> selected = new HashSet<>();

        // index by GA
        Map<String,Set<PairOfRecords>> index = new HashMap<>();
        for (PairOfRecords p : set) {
            String k = p.left().groupId()+":"+p.right().artifactId();
            Set<PairOfRecords> records = index.computeIfAbsent(k,(k2-> new TreeSet<>((p1, p2) -> p1.left().version().compareTo(p2.left().version()))));
            records.add(p);
        }

        for (String key:index.keySet()) {
            List<PairOfRecords> records = index.get(key).stream().collect(Collectors.toUnmodifiableList());
            assert records.size()>0;
            selected.add(records.get(0));  // add first
            selected.add(records.get(records.size()-1));  // add last
        }

        return selected;
    }


    static Map<String,Map<String,Set<String>>>  loadSameSourcesCache() {
        Preconditions.checkArgument(Files.exists(SAME_SOURCE_CACHE));
        try (FileReader reader = new FileReader(SAME_SOURCE_CACHE.toFile())) {
            Map<String,Map<String,Set<String>>>  table = new Gson().fromJson(reader,HashMap.class);
            LOG.info("Same source cache loaded");
            return table;
        } catch (FileNotFoundException x) {
            LOG.info("Same source cache " + SAME_SOURCE_CACHE + " does not exist or is not accessible, using empty table");
            return new HashMap<>();
        } catch (IOException x) {
            LOG.error("Error loading same same source cache, using empty table",x);
            return new HashMap<>();
        }
    }

    private static Set<String> readFromSameSourcesCache(String provider1, String provider2) {
        Set<String> gavs = null;
        Collection<String> gavs2 = null;
        Map<String,Set<String>> map = GAVS_WITH_SAME_RESOURCES.get(provider1);
        if (map!=null) {
            gavs2  = map.get(provider2); // imported as ArrayList
            if (gavs2!=null) {
                gavs = gavs2.stream().collect(Collectors.toSet());
            }
        }
        // entries are symmetric !
        if (gavs==null) {
            map = GAVS_WITH_SAME_RESOURCES.get(provider2);
            if (map!=null) {
                gavs2  = map.get(provider1);
                if (gavs2!=null) {
                    gavs = gavs2.stream().collect(Collectors.toSet());
                }
            }
        }

        if (gavs!=null) {
            LOG.info("Same source cache with GAVs used for " + provider1 + " and " + provider2 + " - " + gavs.size() + " gavs");
        }
        return gavs;
    }

    private static String getProviderName(Path indexFile) {
        return indexFile.getFileName().toString()
            .replace(".tsv","")
            .replace("gav_","");
    }

    private static Set<Record> parseRecords(Path indexFile) throws IOException {
        return Files.lines(indexFile)
            .map(line -> {
                try {
                    return Record.parse(indexFile.getParent(),line);
                } catch (MalformedURLException e) {
                    LOG.error("Error parsing " + indexFile + " ,  line: " + line);
                    throw new RuntimeException(e);
                }
            })
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Find common records -- both records are for the same GAV.
     * @param records1
     * @param records2
     * @return
     */
    private static Set<PairOfRecords> findCommonRecords(Set<Record> records1, Set<Record> records2) {
        Map<String,Record> recordsByGAV1 = new HashMap<>();
        Map<String,Record> recordsByGAV2 = new HashMap<>();
        records1.stream().forEach(record -> recordsByGAV1.put(record.gav(),record));
        records2.stream().forEach(record -> recordsByGAV2.put(record.gav(),record));
        return Sets.intersection(recordsByGAV1.keySet(),recordsByGAV2.keySet())
            .stream()
            .map(gav -> new PairOfRecords(recordsByGAV1.get(gav),recordsByGAV2.get(gav)))
            .collect(Collectors.toSet());
    }

    public static Set<String> entries(File jar) throws IOException {
        try (ZipFile zip = new ZipFile(jar)) {
            Set<String> classes = new TreeSet<>();
            Enumeration<? extends ZipEntry> iter = zip.entries();
            while (iter.hasMoreElements()) {
                String s = iter.nextElement().getName();
                classes.add(s);
            }
            return Collections.unmodifiableSet(classes);
        }
        catch (Exception x) {
            throw new IOException("Error reading from zip file " + jar,x);
        }
    }

    public static Set<String> entries(File jar, Predicate<String> fileFilter) throws IOException {
        return entries(jar)
            .stream()
            .filter(fileFilter)
            .collect(Collectors.toSet());
    }

}
