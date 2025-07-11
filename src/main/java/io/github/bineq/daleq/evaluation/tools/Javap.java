package io.github.bineq.daleq.evaluation.tools;

import com.google.common.base.Preconditions;
import org.apache.commons.io.output.TeeWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.spi.ToolProvider;

/**
 * Javap wrapper. From Tim White's code and https://mike-neck.medium.com/how-to-run-javap-programmatically-d39e17b4e39c .
 * @author tim white , jens dietrich
 */

public class Javap {
    public static final String ERR_MSG_JAVAP_HAS_FAILED = "javap error";

    private static final ToolProvider javap = ToolProvider.findFirst("javap").orElseThrow();

    /**
     * Run javap -c -p.  Result files are generated for provenance.
     * @param cachedByteCode
     * @param disassembledByteCode
     * @return a byte array containing the disassembled code, or null if disassembly fails
     * @throws IOException
     */
    public static byte[] run(Path cachedByteCode, Path disassembledByteCode)  throws IOException {
        Preconditions.checkArgument(Files.exists(cachedByteCode));

        // Adapted from
        StringWriter out = new StringWriter();
        int exitCode = 0;
        try (FileWriter cachedOutput = new FileWriter(disassembledByteCode.toFile())) {
            TeeWriter tee = new TeeWriter(cachedOutput, out);
            PrintWriter stdoutAndStderr = new PrintWriter(tee);
            exitCode = javap.run(
                    stdoutAndStderr,
                    stdoutAndStderr,
                    "-c", "-p", cachedByteCode.toString());

            stdoutAndStderr.flush();

            if (exitCode != 0) {
                throw new IOException("error running javap on " + cachedByteCode);
            }

            return out.toString().getBytes();
        }
    }
}
