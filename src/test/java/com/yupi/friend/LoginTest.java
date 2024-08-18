package com.yupi.friend;

import com.yupi.friend.utils.JWTUtils;
import io.jsonwebtoken.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@SpringBootTest
public class LoginTest {

    // 一天
    private static final long time = 1000 * 60 * 60 * 24;

    private static final String signature = "salt";

    @Test
    void testJWT(){
        JwtBuilder builder = Jwts.builder();
        String jwtToken = builder
                // header
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS264")
                // payload
                .claim("username", "sheephappy")
                .claim("role", "admin")
                .setSubject("admin-test")
                .setExpiration(new Date(System.currentTimeMillis() + time))
                .setId(UUID.randomUUID().toString())
                // signature
                .signWith(SignatureAlgorithm.HS256, signature)
                .compact();

        System.out.println(jwtToken);

    }

    @Test
    void testParse(){
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InNoZWVwaGFwcHkiLCJyb2xlIjoiYWRtaW4iLCJzdWIiOiJhZG1pbi10ZXN0IiwiZXhwIjoxNzIzNjA3OTI4LCJqdGkiOiJiNWJlNTgxMC1jMDNkLTQ1YzctYWY2ZS1kMDMyODhiOGNlMzgifQ.DetcRCUAgZEG1P33fIQrxCAjeOcAN70YOnxG4lQydUU";
        JwtParser parser = Jwts.parser();
        Jws<Claims> claimsJws = parser.setSigningKey(signature).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        System.out.println(claims.get("username"));
        System.out.println(claims.get("role"));
        System.out.println(claims.getSubject());
        System.out.println(claims.getExpiration());
        System.out.println(claims.getId());


    }


    @Test
    void testJWTUtls(){
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6MSwidXNlclJvbGUiOjAsInN1YiI6ImxvZ2luIiwiZXhwIjoxNzIzNTMzNDMwLCJqdGkiOiJlYjA3MmM3YS0wMjBlLTRmZTctOTExYS04OWZhNTJmOWE1YjgifQ.H1-pGEtlQOHjmPVC9lJwK0k_mga0AvtboQFPCXwRJ8o";

        Map<String, Object> resultMap = JWTUtils.verifyToken(token);
        

    }
}
