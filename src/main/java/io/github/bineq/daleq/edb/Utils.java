package io.github.bineq.daleq.edb;

import com.google.common.base.Preconditions;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
            return getClassFilesFromFolder(jarFileOrFolder);
        }
        else if (jarFileOrFolder.getFileName().toString().endsWith(".class")) {
            return List.of(jarFileOrFolder);
        }
        else if (jarFileOrFolder.getFileName().toString().endsWith(".jar") || jarFileOrFolder.getFileName().toString().endsWith(".zip")) {

            File tmp = com.google.common.io.Files.createTempDir();
            // unzip
            try {
                ZipFile zipFile = new ZipFile(jarFileOrFolder.toFile());
                zipFile.extractAll(tmp.getAbsolutePath());
            } catch (ZipException x) {
                throw new IOException("Error unzipping " + jarFileOrFolder, x);
            }

            return getClassFilesFromFolder(tmp.toPath());
        }
        else throw new IllegalStateException("This is neither a jar file nor a folder: " + jarFileOrFolder);
    }

    private static List<Path> getClassFilesFromFolder(Path folder) throws IOException {
        Preconditions.checkArgument(Files.isDirectory(folder), "Folder is not a folder: " + folder);
        return Files.walk(folder)
            .filter(p -> p.getFileName().toString().endsWith(".class"))
            .collect(Collectors.toList());
    }
}
