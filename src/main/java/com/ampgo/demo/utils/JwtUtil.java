package com.ampgo.demo.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtil {

    private static final long EXPIRATIONTIME = 432_000_000; // 5天
    private static final String TOKEN_PREFIX = "Bearer "; // Token前缀

    public static String generateToken(Long userId, String account, String key) {
        Claims claims = Jwts.claims();
        claims.setSubject(userId.toString());
        claims.put("userId", userId);
        claims.put("account", account);
        claims.setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME));
        return Jwts.builder().setClaims(claims).signWith(getSecretKey(key)).compact();
    }

    public static Claims parseToken(String token, String key) {
        return Jwts.parser()
                .setSigningKey(getSecretKey(key))
                .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                .getBody();
    }

    private static Key getSecretKey(String key) {
        return Keys.hmacShaKeyFor(key.getBytes());
    }
}
