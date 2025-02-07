package com.teststrategy.multimodule.maven.sf.framework.util;

import com.teststrategy.multimodule.maven.sf.framework.application.ApplicationContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;


@Slf4j
public class PropertyUtil {

    private static final String DELIMITER = ",";
    private static String applicationName = "";
    private static List<String> profiles;

    public static boolean hasAnyProfileByEndWith(String... args) {
        try {
            if (CollectionUtils.isEmpty(profiles)) {
                Environment environment = ApplicationContextUtil.getApplicationContext().getEnvironment();
                String[] localProfiles = environment.getActiveProfiles();

                if (localProfiles.length == 0) {
                    localProfiles = environment.getDefaultProfiles();
                }

                profiles = Arrays.asList(localProfiles);
            }

            if (CollectionUtils.isEmpty(profiles))
                return false;

            return profiles.stream()
                    .anyMatch(profile -> Arrays.stream(args).anyMatch(arg ->
                            StringUtils.endsWithIgnoreCase(profile, arg)));
        } catch (Exception e) {
            return false;
        }
    }
}
