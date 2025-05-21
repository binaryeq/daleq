package io.github.bineq.daleq.souffle.provenance;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public class ProvenanceDBTests {

    private static Path EDB_DIR = null;
    private static Path IDB_DIR = null;
    private static Path RULES = null;

    private static ProvenanceDB PROV_DB = null;

    @BeforeAll
    public static void init() throws IOException {
        Path dir = Path.of(ProvenanceDBTests.class.getResource("/provenancedb").getPath());
        EDB_DIR = dir.resolve("edb");
        IDB_DIR = dir.resolve("idb");
        RULES = dir.resolve("mergedEDBAndRules.souffle");

        assertTrue(Files.exists(EDB_DIR));
        assertTrue(Files.exists(IDB_DIR));
        assertTrue(Files.exists(RULES));

        PROV_DB = new ProvenanceDB(EDB_DIR, IDB_DIR, RULES);
    }

    @Test
    public void testEDBIndex1()  {

        // check random sample
        // F3540	ch/qos/logback/classic/net/LoggingEventPreSerializationTransformer::transform(Lch/qos/logback/classic/spi/ILoggingEvent;)Ljava/io/Serializable;	300
        ProvenanceDB.FlatFact fact = PROV_DB.getEdbFact("F3540");
        assertNotNull(fact);
        assertEquals("ACONST_NULL",fact.predicateName());
        assertEquals("F3540",fact.values()[0]);
        assertEquals("ch/qos/logback/classic/net/LoggingEventPreSerializationTransformer::transform(Lch/qos/logback/classic/spi/ILoggingEvent;)Ljava/io/Serializable;",fact.values()[1]);
        assertEquals("300",fact.values()[2]);
    }

    @Test
    public void testEDBIndex2() {

        // check random sample
        // F3565	ch/qos/logback/classic/net/LoggingEventPreSerializationTransformer::transform(Lch/qos/logback/classic/spi/ILoggingEvent;)Ljava/io/Serializable;	2800	ch/qos/logback/classic/spi/ILoggingEvent	getClass	()Ljava/lang/Class;	1
        ProvenanceDB.FlatFact fact = PROV_DB.getEdbFact("F3565");
        assertNotNull(fact);
        assertEquals("INVOKEINTERFACE",fact.predicateName());
        assertEquals("F3565",fact.values()[0]);
        assertEquals("ch/qos/logback/classic/net/LoggingEventPreSerializationTransformer::transform(Lch/qos/logback/classic/spi/ILoggingEvent;)Ljava/io/Serializable;", fact.values()[1]);
        assertEquals("2800",fact.values()[2]);
        assertEquals("ch/qos/logback/classic/spi/ILoggingEvent",fact.values()[3]);
        assertEquals("getClass",fact.values()[4]);
        assertEquals("()Ljava/lang/Class;",fact.values()[5]);
        assertEquals("1",fact.values()[6]);
    }

    @Test
    public void testIDBIndex1() {

        // check random sample
        // R_ADD_PR5165[F3565,R_IS_ROOT_METHOD_GETCLASS]	ch/qos/logback/classic/net/LoggingEventPreSerializationTransformer::transform(Lch/qos/logback/classic/spi/ILoggingEvent;)Ljava/io/Serializable;	2800	java/lang/Object	getClass	()Ljava/lang/Class;	1
        ProvenanceDB.FlatFact fact = PROV_DB.getIdbFact("R_ADD_PR5165[F3565,R_IS_ROOT_METHOD_GETCLASS]");
        assertNotNull(fact);
        assertEquals("IDB_INVOKEVIRTUAL",fact.predicateName());
        assertEquals("R_ADD_PR5165[F3565,R_IS_ROOT_METHOD_GETCLASS]",fact.values()[0]);
        assertEquals("ch/qos/logback/classic/net/LoggingEventPreSerializationTransformer::transform(Lch/qos/logback/classic/spi/ILoggingEvent;)Ljava/io/Serializable;",fact.values()[1]);
        assertEquals("2800",fact.values()[2]);
        assertEquals("java/lang/Object",fact.values()[3]);
        assertEquals("getClass",fact.values()[4]);
        assertEquals("()Ljava/lang/Class;",fact.values()[5]);
        assertEquals("1",fact.values()[6]);
    }

    @Test
    public void testRule() {

        // check random sample
        // IDB_INVOKEVIRTUAL(cat("R_ADD_PR5165","[",factid1,",",factid2,"]"),methodid,instructioncounter,"java/lang/Object",name,desc,itf) :- INVOKEINTERFACE(factid1,methodid,instructioncounter,_,name,desc,itf),IS_ROOT_METHOD(factid2,name,desc).
        String rule =  PROV_DB.getRule("R_ADD_PR5165");
        assertNotNull(rule);
        assertEquals("IDB_INVOKEVIRTUAL(cat(\"R_ADD_PR5165\",\"[\",factid1,\",\",factid2,\"]\"),methodid,instructioncounter,\"java/lang/Object\",name,desc,itf) :- INVOKEINTERFACE(factid1,methodid,instructioncounter,_,name,desc,itf),IS_ROOT_METHOD(factid2,name,desc).",rule);
    }
}
