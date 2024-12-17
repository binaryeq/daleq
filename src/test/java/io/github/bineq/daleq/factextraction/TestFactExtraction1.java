package io.github.bineq.daleq.factextraction;

import io.github.bineq.daleq.factextraction.javap.JavapClassModel;
import io.github.bineq.daleq.factextraction.javap.JavapModelTest;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Test fact extraction, use javap as oracle.
 * @author jens dietrich
 */
public class TestFactExtraction1 {
    private JavapClassModel classModel;

    @BeforeEach
    public void setup() throws IOException {
        classModel = JavapClassModel.parse("mypck.MyClass", Path.of(JavapModelTest.class.getResource("/basic/mypck/MyClass.javap").getFile()));
    }
}
