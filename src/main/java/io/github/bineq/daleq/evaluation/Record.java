package io.github.bineq.daleq.evaluation;

import com.google.common.base.Preconditions;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Record read from the tsv index of the dataset containing alternative builds.
 * version -- official GAV version
 * providerVersion -- version assigned by provider (e.g. RH uses additional version extensions)
 * @author jens dietrich
 */
public record Record(String groupId , String artifactId, String version, String providerVersion, Path binMainFile, URL binMainUrl, String binMainMD5, URL binMainMD5Url, Path srcMainFile, URL srcMainUrl, String srcMainMd5, URL srcMainMd5Url) {

    public static Record parse(Path datasetRoot,String line) throws MalformedURLException {
        String[] tokens = line.split("\t");
        assert tokens.length == 12;

        String groupId = tokens[0];
        String artifactId = tokens[1];
        String version = tokens[2];
        String providerVersion = tokens[3];
        Path binMainFile = datasetRoot.resolve(tokens[4]);
        Preconditions.checkArgument(Files.exists(binMainFile),"file does not exist: " + binMainFile);
        URL binMainUrl = new URL(tokens[5]);
        String binMainMD5 = tokens[6];
        URL binMainMD5Url = new URL(tokens[7]);
        Path srcMainFile = datasetRoot.resolve(tokens[8]);
        Preconditions.checkArgument(Files.exists(srcMainFile));
        URL srcMainUrl = new URL(tokens[9]);
        String srcMainMd5 = tokens[10];
        URL srcMainMd5Url = new URL(tokens[11]);

        return new Record(groupId,artifactId,version,providerVersion,binMainFile,binMainUrl,binMainMD5,binMainMD5Url,srcMainFile,srcMainUrl,srcMainMd5,srcMainMd5Url);
    }

    public String gav() {
        return groupId + ':' + artifactId + ':' + version;
    }


}
