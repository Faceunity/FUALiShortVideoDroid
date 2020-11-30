package com.aliyun.svideo.editor.publish;

/**
 * Created by macpro on 2017/11/8.
 */

public class SecurityTokenInfo {

    private String AccessKeySecret;
    private String SecurityToken;
    private String Expiration;
    private String AccessKeyId;

    public String getAccessKeySecret() {
        return AccessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        AccessKeySecret = accessKeySecret;
    }

    public String getSecurityToken() {
        return SecurityToken;
    }

    public void setSecurityToken(String securityToken) {
        SecurityToken = securityToken;
    }

    public String getExpiration() {
        return Expiration;
    }

    public void setExpiration(String expiration) {
        Expiration = expiration;
    }

    public String getAccessKeyId() {
        return AccessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        AccessKeyId = accessKeyId;
    }

    @Override
    public String toString() {
        return "SecurityTokenInfo{" +
               "AccessKeySecret='" + AccessKeySecret + '\'' +
               ", SecurityToken='" + SecurityToken + '\'' +
               ", Expiration='" + Expiration + '\'' +
               ", AccessKeyId='" + AccessKeyId + '\'' +
               '}';
    }
}
