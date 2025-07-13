package io.github.bineq.daleq.evaluation.tools;

import io.github.bineq.daleq.edb.AbstractFactExtractionTest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DiffTest {

    Path disass1 = Path.of(AbstractFactExtractionTest.class.getResource("/evaluation/disassembly1.javap").getFile());
    Path disass2 = Path.of(AbstractFactExtractionTest.class.getResource("/evaluation/disassembly2.javap").getFile());

    @Test
    public void test() throws IOException {


        Path out = new File(".tests/diff.txt").toPath();
        if (!Files.exists(out.getParent())) {
            Files.createDirectories(out.getParent());
        }
        String s1 = Files.readString(disass1);
        String s2 = Files.readString(disass2);
        Diff.diffAndExport(s1,s2,out);

        // no oracle, inspect output to verify
        // focus is on compactness
    }

}
