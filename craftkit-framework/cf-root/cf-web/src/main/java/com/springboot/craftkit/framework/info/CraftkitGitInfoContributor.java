package com.springboot.craftkit.framework.info;

import com.springboot.craftkit.framework.application.setting.GitCommitProperties;
import org.springframework.boot.actuate.info.GitInfoContributor;
import org.springframework.boot.actuate.info.Info.Builder;

/**
 * Git commit info contributor for actuator/info endpoint.
 * Adds git commit information to the Spring Boot Actuator info endpoint.
 */
public class CraftkitGitInfoContributor extends GitInfoContributor {

    public CraftkitGitInfoContributor(GitCommitProperties gitCommitProperties) {
        super(gitCommitProperties.getGitProperties());
        this.gitCommitProperties = gitCommitProperties;
    }

    final GitCommitProperties gitCommitProperties;

    @Override
    public void contribute(Builder builder) {
        builder.withDetail("git", generateContent());
        builder.withDetail("craftkit-git-commit", gitCommitProperties);
    }

}
