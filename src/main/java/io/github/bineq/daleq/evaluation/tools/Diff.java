package io.github.bineq.daleq.evaluation.tools;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Simple diff utility.
 * @author jens dietrich
 */
public class Diff {

    public static void diffAndExport(String s1, String s2, Path file) throws IOException {
        List<String> lines1 = s1.lines().toList();
        List<String> lines2 = s2.lines().toList();
        Patch<String> patch = DiffUtils.diff(lines1,lines2);
        List<String> diff = UnifiedDiffUtils.generateUnifiedDiff("version1","version2",lines1,patch,3);
//        List<String> diff = new ArrayList<>();
//        for (AbstractDelta<String> delta : patch.getDeltas()) {
//            diff.add(delta.toString());
//        }
        Files.write(file,diff);
    }
}
