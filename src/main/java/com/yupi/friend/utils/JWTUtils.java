package com.yupi.friend.utils;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.yupi.friend.config.ConstantPropertiesUtils.TOKEN_SECRET;

@Slf4j
public class JWTUtils {

    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000; // 一天

    private static final String token_secret = TOKEN_SECRET;  //密钥盐

    public static String getToken(Map<String, Object> userInfo){

        JwtBuilder builder = Jwts.builder();

        return builder
                // header
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS264")
                // payload
                .addClaims(userInfo)
                .setSubject("login")
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .setId(UUID.randomUUID().toString())
                // signature
                .signWith(SignatureAlgorithm.HS256, token_secret)
                .compact();
    }


    public static Map<String, Object> verifyToken(String token){
        JwtParser parser = Jwts.parser();
        Claims claims;
        try {
            claims = parser.setSigningKey(token_secret).parseClaimsJws(token).getBody();
        }
        catch (Exception e){
            log.error(e.getMessage());
            return null;
        }

        return claims;
    }

}
