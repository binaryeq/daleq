package io.github.bineq.daleq.idb;

import com.google.common.base.Preconditions;
import io.github.bineq.daleq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * Utility to read all facts from an IDB folder.
 * @author jens dietrich
 */
public class IDBReader {

    public static final Logger LOG = LoggerFactory.getLogger(IDBReader.class);
    public static final String EXTENSION = ".csv";

    public static IDB read (Path idbFolder) throws IOException {
        Preconditions.checkArgument(Files.exists(idbFolder));
        Preconditions.checkArgument(Files.isDirectory(idbFolder));
        IDB idb = new IDB();
        Collection<Path> files = Files.walk(idbFolder)
            .filter(file -> !Files.isDirectory(file))
            .filter(file -> Files.isRegularFile(file))
            .filter(file -> file.getFileName().toString().endsWith(EXTENSION))
            .collect(Collectors.toUnmodifiableList());
        for (Path file : files) {
            LOG.info("Reading IDB file {}", file);
            readFacts(file,idb);
        }
        return idb;
    }

    static void readFacts(Path file,IDB idb) throws IOException {
        String predicateName = file.getFileName().toString().replace(EXTENSION, "");
        List<Fact> facts = Files.readAllLines(file).stream()
            .map(line -> line.split("\t"))
            .map(tokens -> parse(predicateName,tokens))
            .collect(Collectors.toUnmodifiableList());

        LOG.warn("TODO: classify facts");
    }

    static Fact parse(String predicateName,String[] tokens) {

        Predicate predicate = IDBPredicateRegistry.ALL_PREDICATES.get(predicateName);
        Preconditions.checkState(predicate != null,"unknown predicate " + predicateName);
        assert tokens.length==predicate.getSlots().length;

        List<Object> values = IntStream.range(0, tokens.length)
            .mapToObj(i -> readValue(predicate.getSlots()[i],tokens[i]))
            .collect(Collectors.toList());

        return new SimpleFact(predicate,values.toArray(new Object[values.size()]));
    }

    private static Object readValue(Slot slot, String token) {
        if (slot.type()== SlotType.SYMBOL) {
            return token;
        }
        else if (slot.type()== SlotType.NUMBER && slot.jtype()==Integer.TYPE.getName()) {
            return Integer.parseInt(token);
        }

        throw new UnsupportedOperationException("conversion is not yet supported");
    }

}
