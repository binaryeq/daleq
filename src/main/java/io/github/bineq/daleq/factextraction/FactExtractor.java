package io.github.bineq.daleq.factextraction;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Extracts facts from bytecode.
 * Facts are stored in map associating table (predicate)names with records.
 * @author jens dietrich
 */
public class FactExtractor   {

    public List<SimpleFact> extract (InputStream bytecode) throws IOException {
        ClassNode classNode = new ClassNode();
        byte[] bytes = bytecode.readAllBytes();
        new ClassReader(bytes).accept(classNode, 0);
        List<SimpleFact> facts = new ArrayList<>();

        facts.add(new SimpleFact(Predicate.SUPERCLASS, classNode.name, classNode.superName));
        for (String intrf:classNode.interfaces) {
            facts.add(new SimpleFact(Predicate.SUPERCLASS, classNode.name, intrf));
        }


        return facts;
    }
}
