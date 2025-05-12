package io.github.bineq.daleq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility to compare databases.
 * @author jens dietrich
 */
public class DBCompare {

    private static final Logger LOG = LoggerFactory.getLogger(DBCompare.class);

    /**
     * Compare two files line by line.
     * @param f1
     * @param f2
     * @return
     * @throws IOException
     */
    public static boolean compare(Path f1, Path f2) throws IOException {
        List<String> lines1 = Files.readAllLines(f1);
        List<String> lines2 = Files.readAllLines(f2);

        if (lines1.size() != lines2.size()) {
            LOG.warn("Files have different NUMBER of lines: {}, {}",f1,f2);
            return false;
        }

        for (int i = 0; i < lines1.size(); i++) {
            if (!lines1.get(i).equals(lines2.get(i))) {
                LOG.warn("Files have different content: {}, {}, line {}",f1,f2,i);
                return false;
            }
        }
        return true;
    }

    /**
     * Compare all files in two folders line by line.
     * Only comparing files with the *.fact extension. not recursive.
     * @param dir1
     * @param dir2
     * @return
     * @throws IOException
     */
    public static boolean compareAll(Path dir1, Path dir2) throws IOException {
        return compareAll(dir1,dir2,p -> p.toFile().toString().endsWith(".facts"));
    }

    /**
     * Compare all files in two folders line by line.
     * Only compare files using a certain extension. not recursive.
     * @param dir1
     * @param dir2
     * @return
     * @throws IOException
     */
    public static boolean compareAll(Path dir1, Path dir2, Predicate<Path> fileFilter) throws IOException {

        List<Path> files1 =  Files.walk(dir1)
            .filter(Files::isRegularFile)
            .filter(p -> fileFilter.test(p))
            .sorted()
            .collect(Collectors.toUnmodifiableList());

        List<Path> files2 =  Files.walk(dir1)
            .filter(Files::isRegularFile)
            .filter(p -> fileFilter.test(p))
            .sorted()
            .collect(Collectors.toUnmodifiableList());

        if (files1.size() != files2.size()) {
            LOG.warn("Directories have different content: {}, {}",dir1,dir2);
            return false;
        }

        for (int i = 0; i < files1.size(); i++) {
            Path f1 = files1.get(i);
            Path f2 = files2.get(i);
            if (!compare(f1, f2)) {
                return false;
            }
        }

        return true;
    }


}
