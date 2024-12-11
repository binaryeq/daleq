package io.github.bineq.daleq.factextraction;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Misc utilities.
 * @author jens dietrich
 */
public class Utils {

    public static List<Path> getClassFiles(Path jarFileOrFolder) throws IOException {

        Preconditions.checkArgument(Files.exists(jarFileOrFolder), "Jar file or folder does not exist");
        if (Files.isDirectory(jarFileOrFolder)) {
            return Files.walk(jarFileOrFolder)
                .filter(p -> p.getFileName().toString().endsWith(".class"))
                .collect(Collectors.toList());
        } else if (jarFileOrFolder.getFileName().toString().endsWith(".jar")) {
            URI uri = URI.create("jar:file:" + jarFileOrFolder.toFile().getAbsolutePath());
            try (FileSystem zipfs = FileSystems.newFileSystem(uri, new HashMap<>())) {
                // assume single root
                Iterator<Path> iter = zipfs.getRootDirectories().iterator();
                assert iter.hasNext();
                Path root = iter.next();
                return Files.walk(root)
                    .filter(p -> p.getFileName().toString().endsWith(".class"))
                    .collect(Collectors.toList());
            }
        } else {
            throw new IllegalArgumentException("Input is neither a jar file nor a folder: " + jarFileOrFolder);
        }
    }
}
