package com.springboot.craftkit.framework.jwt;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public interface JwtExpCalculator {

    ZoneId ZONE_GMT = ZoneId.of("GMT");
    ZoneId ZONE_LOCAL = ZoneId.systemDefault();

    ZonedDateTime getExpireAt(JwtType jwtType, ZonedDateTime iat);

    ZonedDateTime getExpireAt(JwtType jwtType, ZonedDateTime iat, ZonedDateTime exps);
}
