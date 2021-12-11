package cn.freeprogramming.utils;


import io.jsonwebtoken.*;
import org.springframework.util.StringUtils;

import java.util.Date;

public class JwtUtils {

    /**
     * 签名秘钥
     */
    private static final String tokenSignKey = "freeprogramming.cn";

    /**
     *  根据参数生成token
     */

    public static String createToken(Integer userId) {

        /**
         * 过期时间
         */
        long tokenExpiration = 7 * 24 * 60 * 60 * 1000;
        String token = Jwts.builder()
                .setSubject("user")
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .claim("userId", userId)
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }

    public static String createToken(Integer userId,long tokenExpiration) {

        String token = Jwts.builder()
                .setSubject("user")
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .claim("userId", userId)
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }

    /**
     * 根据token字符串得到用户id
     */
    public static String getValue(String key,String token) {
        if(StringUtils.isEmpty(key) || StringUtils.isEmpty(token)) {
            return null;
        }

        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        return (String) claims.get(key);
    }



}

