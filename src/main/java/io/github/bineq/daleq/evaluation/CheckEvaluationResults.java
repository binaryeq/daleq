package io.github.bineq.daleq.evaluation;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

/**
 * Script to check evaluation results.
 * Can be used while the main script RunEvaluation is still running.
 * @author jens dietrich
 */
public class CheckEvaluationResults {

    public static final Set<String> PROVIDERS = Set.of("gaoss","mvnc","obfs");

    public static void main(String[] args) throws Exception {
        File ROOT = new File("evaluation/db");

        int sameIDBCounter = 0;
        int diffIDBCounter = 0;
        int sameBytecodeCounter = 0;

        for (File gavDir : ROOT.listFiles(f -> f.isDirectory() && !f.isHidden())) {

            for (File classDir: gavDir.listFiles(f -> f.isDirectory() && !f.isHidden())) {
                List<File> idbs = new ArrayList<>();
                List<File> classes = new ArrayList<>();
                for (File providerDir: classDir.listFiles(f -> f.isDirectory() && !f.isHidden())) {
                    assert PROVIDERS.contains(providerDir.getName());

                    File idb = new File(providerDir, "idb-projected.txt");
                    if (idb.exists()) {
                        idbs.add(idb);
                    }

                    File[] classFiles = providerDir.listFiles(f -> !f.isDirectory() && f.getName().endsWith(".class"));
                    assert classFiles.length == 1;
                    classes.add(classFiles[0]);

                }
                for (int i=0;i<idbs.size();i++) {
                    for (int j=0;j<i;j++) {

                        byte[] bytecode1 = Files.readAllBytes(classes.get(i).toPath());
                        byte[] bytecode2 = Files.readAllBytes(classes.get(j).toPath());

                        // if the bytecode is the same this folder(s)/file(s) are not created
                        assert !Arrays.equals(bytecode1, bytecode2);

                        File idbF1 = idbs.get(i);
                        File idbF2 = idbs.get(j);
                        String idb1 = Files.readString(idbF1.toPath());
                        String idb2 = Files.readString(idbF2.toPath());
                        if (idb1.equals(idb2)) {
                            sameIDBCounter = sameIDBCounter + 1;
                        }
                        else {
                            diffIDBCounter = diffIDBCounter + 1;
                        }
                    }
                }
            }
        }


        System.out.println("same bytecode: " + sameBytecodeCounter);
        System.out.println("diff bytecode: " + diffIDBCounter);
        System.out.println("same idb: " + sameIDBCounter);
    }
}
