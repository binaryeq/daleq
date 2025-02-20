package io.github.bineq.daleq.souffle.provenance;

import java.io.PrintStream;

/**
 * Print derivations.
 * @author jens dietrich
 */
public class DerivationPrinter {

    public static void toConsole(DerivationNode root) {
        toConsole(root,System.out);
    }

    public static void toConsole(DerivationNode root, PrintStream out) {
        toConsole(root, out, 0);
    }

    private static void toConsole(DerivationNode root, PrintStream out, int offset) {
        for (int i=0;i<offset;i++) {
            out.print("   ");
        }
        out.println(root.id());
        for (DerivationNode child : root.children()) {
            toConsole(child,out,offset+1);
        }
    }
}
