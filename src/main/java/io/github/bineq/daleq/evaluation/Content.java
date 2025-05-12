package io.github.bineq.daleq.evaluation;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Represents content of a jar/zip or folder representation - bytecode, sourcecode, metadata, resources.
 * @author jens dietrich
 */
public class Content {

    private Path jarFileOrFolder = null;
    private String name = null;
    // derived & lazily initialised
    private byte[] data = null;

    public Content(Path jarFileOrFolder, String name) {
        Preconditions.checkNotNull(jarFileOrFolder);
        Preconditions.checkNotNull(name);
        Preconditions.checkState(Files.exists(jarFileOrFolder));
        this.jarFileOrFolder = jarFileOrFolder;
        this.name = name;
    }

    public Path getJarFileOrFolder() {
        return jarFileOrFolder;
    }

    public String getName() {
        return name;
    }


    /**
     * Establish whether the bytecode is available / can be loaded.
     * This can be used by clients to construct guards to record and report such cases.
     * @return
     */
    public boolean validate() {
        // trigger lazy initialisation
        try {
            load();
            return true;
        }
        catch (Exception x) {
            return false;
        }
    }

    /**
     * Get the actual data.
      * @return
     * @throws IOException
     */
    public byte[] load() throws IOException {
        if (data==null) {
            if (Files.isDirectory(jarFileOrFolder)) {
                // interpret as folder
                Optional<Path> match = Files.walk(jarFileOrFolder)
                    .filter(p -> name.equals(jarFileOrFolder.relativize(p).toString()))
                    .findFirst();

                Preconditions.checkState(match.isPresent(),"File not found: " + jarFileOrFolder + "/" + name);
                data = Files.readAllBytes(match.get());

            }
            else {
                // interpret as jar file
                ZipFile zip = new ZipFile(jarFileOrFolder.toFile());
                Preconditions.checkNotNull(zip);
                while (name.startsWith("/")) {
                    name = name.substring(1);
                }
                ZipEntry entry = zip.getEntry(name);
                Preconditions.checkNotNull(entry); // see validate !

                try (InputStream in = zip.getInputStream(entry)) {
                    data = ByteStreams.toByteArray(in);
                }
            }
            assert data!=null;
        }
        return data;
    }

    /**
     * Get the actual data as string
     * @return
     */
    public String loadChars() throws IOException {
        return loadChars(StandardCharsets.UTF_8);
    }

    /**
     * Get the actual data as string
     * @return
     */
    public String loadChars(Charset charSet) throws IOException {
        return new String(load(), charSet);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Content content = (Content) o;
        return Objects.equals(jarFileOrFolder, content.jarFileOrFolder) && Objects.equals(name, content.name) && Arrays.equals(data, content.data);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(jarFileOrFolder, name);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    public String toString() {
        return "Content{" + jarFileOrFolder + '#' + name + '}';
    }

    public boolean isClass() {
        return name.endsWith(".class");
    }

    public boolean isInnerClass() {
        return isClass() && name.contains("$");
    }
}
