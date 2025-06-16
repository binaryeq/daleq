package io.github.bineq.daleq;

import com.google.common.base.Preconditions;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
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
                //.peek(System.out::println)
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

    public static void zipFile(File fileToZip, File zip) throws IOException {
        Preconditions.checkArgument(Files.exists(fileToZip.toPath()));
        FileOutputStream fos = new FileOutputStream(zip);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
        zipOut.closeEntry();
        zipOut.close();
        fos.close();
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


    // get the entries of a zip / jar as strings (paths within the zip)
    // the returned set is sorted
    public static Set<String> entries(Path file) throws IOException {
        return entries(file,ze -> true);
    }

    // get the non-directory entries of a zip / jar as strings (paths within the zip)
    // the returned set is sorted
    public static Set<String> nonDirEntries(Path file) throws IOException {
        return entries(file,ze -> !ze.isDirectory());
    }

    // get the entries of a zip / jar as strings (paths within the zip)
    // the returned set is sorted
    public static Set<String> entries(Path file,java.util.function.Predicate <ZipEntry> filter) throws IOException {
        try (ZipFile zip = new ZipFile(file.toFile())) {
            Set<String> entries = new TreeSet<>();
            Enumeration<? extends ZipEntry> iter = zip.entries();
            while (iter.hasMoreElements()) {
                ZipEntry entry = iter.nextElement();
                if (filter.test(entry)) {
                    String s = entry.getName();
                    entries.add(s);
                }
            }
            return Collections.unmodifiableSet(entries);
        }
        catch (Exception x) {
            throw new IOException("Error reading from zip file " + file,x);
        }
    }

    public static void copy(URL resource, Path file) throws IOException {
        try (InputStream in = resource.openStream()) {
            Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static String readAsString(URL resource) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        }

        return content.toString();
    }

    // read a zip entry
    public static byte[] readEntryFromZip(Path file,String entryName) throws IOException {
        try (ZipFile zip = new ZipFile(file.toFile())) {
            ZipEntry entry = zip.getEntry(entryName);
            try (InputStream inputStream = zip.getInputStream(entry)) {
                // Read and process the entry contents using the inputStream
                byte[] data = new byte[inputStream.available()];
                inputStream.read(data);
                return data;
            }
        }
        catch (Exception x) {
            throw new IOException("Error reading from zip file " + file,x);
        }
    }

}
