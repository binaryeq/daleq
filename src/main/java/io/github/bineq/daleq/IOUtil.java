package io.github.bineq.daleq;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * IO related utilities.
 * @author jens dietrich
 */
public class IOUtil {

    public static void deleteDir (Path dir) throws IOException  {
        Preconditions.checkArgument(Files.exists(dir));
        Preconditions.checkArgument(Files.isDirectory(dir));
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .peek(System.out::println)
                .forEach(File::delete);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    // zip and delete, infer the zip file from the dir name
    public static void zipAndDeleteDir (Path dir) throws IOException {
        zipDir (dir);
        deleteDir(dir);
    }

    public static void zipAndDeleteDir (Path dir, Path zip) throws IOException {
        zipDir (dir,zip);
        deleteDir(dir);
    }

    // zip, infer the zip file from the dir name
    public static void zipDir (Path dir) throws IOException {
        String name = dir.getFileName().toString();
        Path parent = dir.getParent();
        Path zip = parent.resolve(name+".zip");
        zipDir(dir,zip);
    }

    // from https://www.baeldung.com/java-compress-and-uncompress
    public static void zipDir (Path dir, Path zip) throws IOException {
        Preconditions.checkArgument(Files.exists(dir));
        Preconditions.checkArgument(Files.isDirectory(dir));
        FileOutputStream fos = new FileOutputStream(zip.toFile());
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = dir.toFile();
        zipFile(fileToZip, fileToZip.getName(), zipOut);
        zipOut.close();
        fos.close();
    }

    // from https://www.baeldung.com/java-compress-and-uncompress
    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }


}
