package com.secureshare.dto;
public class LoginResponse {
    private String token,refreshToken,username,role,email,publicKey,encryptedPrivateKeyBlob,pbkdf2Salt,pbkdf2Iv;
    public LoginResponse(){}
    public static Builder builder(){return new Builder();}
    public static class Builder{private final LoginResponse r=new LoginResponse();
        public Builder token(String v){r.token=v;return this;} public Builder refreshToken(String v){r.refreshToken=v;return this;}
        public Builder username(String v){r.username=v;return this;} public Builder role(String v){r.role=v;return this;}
        public Builder email(String v){r.email=v;return this;} public Builder publicKey(String v){r.publicKey=v;return this;}
        public Builder encryptedPrivateKeyBlob(String v){r.encryptedPrivateKeyBlob=v;return this;}
        public Builder pbkdf2Salt(String v){r.pbkdf2Salt=v;return this;} public Builder pbkdf2Iv(String v){r.pbkdf2Iv=v;return this;}
        public LoginResponse build(){return r;}}
    public String getToken(){return token;} public String getRefreshToken(){return refreshToken;}
    public String getUsername(){return username;} public String getRole(){return role;}
    public String getEmail(){return email;} public String getPublicKey(){return publicKey;}
    public String getEncryptedPrivateKeyBlob(){return encryptedPrivateKeyBlob;}
    public String getPbkdf2Salt(){return pbkdf2Salt;} public String getPbkdf2Iv(){return pbkdf2Iv;}
}
