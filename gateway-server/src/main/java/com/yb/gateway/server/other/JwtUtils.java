package com.yb.gateway.server.other;

import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.lang3.StringUtils;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.*;

/**
 * Description: Jwt生成和解析工具
 * author biaoyang
 * date 2019/4/25 002511:05
 */
public class JwtUtils {

    /**
     * author biaoyang
     * Date: 2019/4/25 0025
     * Description:获取jwt唯一身份识别码jti
     */
    public static String createJti() {
        return String.valueOf(System.nanoTime());
    }

    /**
     * 通过加密算法和秘钥生成加密jwt的token的Key
     *
     * @param base64Secret 经过Base64编码的Secret(秘钥)
     * @param algorithm    加密算法
     * @return
     */
    private static Key getKey(String base64Secret, String algorithm) {
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(base64Secret);
        return new SecretKeySpec(apiKeySecretBytes, algorithm);
    }

    /**
     * 把对象转换为Map,需要commons-beanutils依赖
     *
     * @param object
     * @return
     */
    private static Map<String, Object> objectToMap(Object object) {
        Map<String, Object> map = new HashMap<>(16);
        new BeanMap(object).forEach((k, v) -> map.put(String.valueOf(k), v));
        return map;
    }

    /**
     * 验证jwt的token的 签名
     *
     * @param jsonWebToken
     * @param base64Secret 经过Base64编码的Secret(秘钥)
     * @return
     */
    public static boolean verifySignature(String jsonWebToken, String base64Secret) {
        return Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(base64Secret))
                .isSigned(jsonWebToken);
    }

    /**
     * 生成accessToken
     *
     * @param user
     * @param ttlMillis
     * @param base64Secret
     * @return
     */
    public static String createAccessToken(LoginUser user, long ttlMillis, String base64Secret) {
        // 采用椭圆曲线加密算法, 提升加密速度
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        //通过加密算法和秘钥生成加密jwt的token的Key
        Key key = getKey(base64Secret, SignatureAlgorithm.HS512.getJcaName());
        //添加构成JWT的参数
        JwtBuilder builder = Jwts.builder().setHeaderParam("typ", "JWT")
                //这个两个内容可要可不要
                //.setIssuer(iss)
                //.setAudience(aud)
                //添加jwt的id,也就是jti
                .setId(createJti())
                //装填用户信息到荷载
                .addClaims(objectToMap(user))
                .setSubject(user.getUsername())
                .signWith(SignatureAlgorithm.HS512, key);
        //添加Token过期时间
        if (ttlMillis > 0) {
            long expMillis = System.currentTimeMillis() + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp).setNotBefore(new Date(System.currentTimeMillis()));
        }
        //生成JWT
        return builder.compact();
    }

    /**
     * 创建刷新token
     *
     * @param username
     * @param ttlMillis
     * @param base64Secret
     * @return
     */
    public static String createRefreshToken(String username, long ttlMillis, String base64Secret) {
        //通过加密算法和秘钥生成加密jwt的token的Key
        Key key = getKey(base64Secret, SignatureAlgorithm.HS512.getJcaName());
        //添加构成JWT的参数
        JwtBuilder builder = Jwts.builder().setHeaderParam("typ", "JWT")
                //这个两个内容可要可不要
                //.setIssuer(iss)
                //.setAudience(aud)
                //添加jwt的id,也就是jti
                .setId(createJti())
                .claim("scope", "REFRESH")
                .setSubject(username)
                .signWith(SignatureAlgorithm.HS256, key);
        //添加Token过期时间
        if (ttlMillis > 0) {
            long expMillis = System.currentTimeMillis() + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp).setNotBefore(new Date(System.currentTimeMillis()));
        }
        //生成JWT
        return builder.compact();
    }

    /**
     * 校验token的合法性,并且获取荷载信息
     *
     * @param jwtWebToken
     * @return
     */
    public static LoginUser checkAndGetPayload(String jwtWebToken, String base64Secret) {
        //判断token的合法性
        if (StringUtils.isNotBlank(jwtWebToken) && jwtWebToken.startsWith("Bearer ")) {
            //验证签名
            if (verifySignature(jwtWebToken, base64Secret)) {
                //去掉头部的Bearer
                jwtWebToken = jwtWebToken.replaceFirst("Bearer ", "");
                //对jwt的token进行切割判断
                if (jwtWebToken.contains(".") && jwtWebToken.split("\\.").length == 3) {
                    //获取荷载内容
                    String payload = new String(Base64.getUrlDecoder().decode(jwtWebToken.split("\\.")[1]));
                    //解析荷载(封装的时候也要是JSON转的对象,才能反过来解析出来)
                    return StringUtils.isNotBlank(payload) ? JSON.parseObject(payload, LoginUser.class) : null;
                }
            }
        }
        //不满足条件返回null
        return null;
    }

}
