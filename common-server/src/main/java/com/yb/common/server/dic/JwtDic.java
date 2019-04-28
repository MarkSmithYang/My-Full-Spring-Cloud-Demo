package com.yb.common.server.dic;

/**
 * Description: jwt相关的字段
 * author biaoyang
 * date 2019/4/28 002810:12
 */
public class JwtDic {

    /**myJti*/
    public static final String REDIS_SET_JTI_KEY = "myJti";
    /**Authorization*/
    public static final String HEADERS_NAME = "Authorization";
    /**Bearer */
    public static final String HEADERS_VALUE_PREFIX = "Bearer ";
    /**Base64编码的secret*/
    public static final String BASE64_ENCODE_SECRET = "Snd0QmFzZTY0U2VjcmV0WWVz";//JwtBase64SecretYes

}
