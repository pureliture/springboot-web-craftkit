package com.teststrategy.multimodule.maven.sf.framework.application.setting;

import com.teststrategy.multimodule.maven.sf.framework.jwt.JwtType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;


@Data
@ToString
@NoArgsConstructor
@ConfigurationProperties(prefix = JwtProperties.PREFIX, ignoreUnknownFields = false)
public class JwtProperties {

    public static final String PREFIX = "sample-framework.jwt";

    private boolean enabled;
    private String issuer;
    private String secret;
    private String algorithm;
    private boolean cookieSecurable = true;
    private Map<JwtType, TokenProperties> tokens;

    @ToString
    @NoArgsConstructor
    public static class TokenProperties {
        private String cookiePath = "/";
        private long cookieMaxAge = -1;
        private Long tokenMaxAge;
        private Long[] tokenAgeRange;
        private LocalTime newDayTime;

        public final String getCookiePath() {
            return cookiePath;
        }

        public final void setCookiePath(String cookiePath) {
            this.cookiePath = cookiePath;
        }

        public final long getCookieMaxAge() {
            return cookieMaxAge;
        }

        public final void setCookieMaxAge(long cookieMaxAge) {
            this.cookieMaxAge = cookieMaxAge;
        }

        public final Long getTokenMaxAge() {
            return tokenMaxAge;
        }

        public final void setTokenMaxAge(Long tokenMaxAge) {
            this.tokenMaxAge = tokenMaxAge;
        }

        public final Long[] getTokenAgeRange() {
            return tokenAgeRange;
        }

        public final void setTokenAgeRange(Long[] tokenAgeRange) {
            this.tokenAgeRange = tokenAgeRange;
        }

        public final LocalTime getNewDayTime() {
            return newDayTime;
        }

        public final void setNewDayTime(String hmm) {
            if (hmm == null || hmm.trim().isEmpty()) {
                newDayTime = null;
            }
            newDayTime = LocalTime.parse(hmm, DateTimeFormatter.ofPattern("H:mm"));
        }
    }
}

