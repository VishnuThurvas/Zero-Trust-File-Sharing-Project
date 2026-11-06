package com.secureshare.dto;
public class RegisterResponse {
    private String message,username,publicKey;
    public RegisterResponse(){}
    public static Builder builder(){return new Builder();}
    public static class Builder{private final RegisterResponse r=new RegisterResponse();
        public Builder message(String v){r.message=v;return this;}
        public Builder username(String v){r.username=v;return this;}
        public Builder publicKey(String v){r.publicKey=v;return this;}
        public RegisterResponse build(){return r;}}
    public String getMessage(){return message;} public String getUsername(){return username;} public String getPublicKey(){return publicKey;}
}
