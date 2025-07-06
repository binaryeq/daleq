package io.github.bineq.daleq;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import java.io.IOException;

/**
 * Utility to access rules.
 * @author jens dietrich
 */
public interface Rules {

    static Rules defaultRules() {
        return new Rules () {
            public Resource[] get() throws IOException {
                PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
                return resolver.getResources("classpath*:rules/**/*souffle");
            }
        };
    }

    Resource[] get() throws IOException;
}
