package io.github.bineq.daleq;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestRules {

    // metamorphic test
    @Test
    public void testSoundRulesAreSubsetOfAllRules() throws IOException {
        Set<Resource> soundRuleDefs = Arrays.stream(Rules.soundRules().get()).collect(Collectors.toSet());
        Set<Resource> allRuleDefs = Arrays.stream(Rules.defaultRules().get()).collect(Collectors.toSet());
        assertTrue(allRuleDefs.containsAll(soundRuleDefs));
    }

    @Test
    public void testSoundRulePaths() throws IOException {

        Set<Resource> soundRuleDefs = Arrays.stream(Rules.soundRules().get()).collect(Collectors.toSet());
        for (Resource resource : soundRuleDefs) {
            File f = resource.getFile();
            File dir = f.getParentFile();
            assertNotEquals("soundy",dir.getName());
        }
    }

}
