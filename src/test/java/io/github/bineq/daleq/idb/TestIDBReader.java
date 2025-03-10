package io.github.bineq.daleq.idb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class TestIDBReader {

    private IDB idb = null;

    @BeforeEach
    public void setup() throws IOException {
        Path idbFolder = Path.of(TestIDBReader.class.getResource("/idb/idb1").getPath());
        idb = IDBReader.read(idbFolder);
    }

    @Test
    public void test() {

    }
}
