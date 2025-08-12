package io.github.bineq.daleq;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility to access rules.
 * @author jens dietrich
 */
public interface Rules {

    Rules DEFAULT_RULES = () -> {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        return resolver.getResources("classpath*:rules/**/*souffle");
    };

    Rules SOUND_RULES = () -> {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        List<Resource> resources = Stream.of(defaultRules().get())
            .filter(r -> {
                try {
                    return ! r.getFile().getAbsolutePath().contains("/rules/normalisations/soundy/");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .collect(Collectors.toUnmodifiableList());

        return resources.toArray(new Resource[resources.size()]);
    };


    @Deprecated // replace by direct access to static var
    static Rules defaultRules() {
        return DEFAULT_RULES;
    }

    @Deprecated // replace by direct access to static var
    static Rules soundRules() {
        return SOUND_RULES;
    }

    Resource[] get() throws IOException;
}
