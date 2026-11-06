package com.secureshare.dto;
import jakarta.validation.constraints.*;
public class RegisterRequest {
    @NotBlank @Size(min=3,max=50) private String username;
    @NotBlank @Email private String email;
    @NotBlank @Size(min=8,max=100) private String password;
    @NotBlank private String encryptedPrivateKeyBlob;
    @NotBlank private String pbkdf2Salt;
    @NotBlank private String pbkdf2Iv;
    @NotBlank private String publicKey;
    public RegisterRequest(){}
    public String getUsername(){return username;} public String getEmail(){return email;}
    public String getPassword(){return password;} public String getEncryptedPrivateKeyBlob(){return encryptedPrivateKeyBlob;}
    public String getPbkdf2Salt(){return pbkdf2Salt;} public String getPbkdf2Iv(){return pbkdf2Iv;}
    public String getPublicKey(){return publicKey;}
    public void setUsername(String v){username=v;} public void setEmail(String v){email=v;}
    public void setPassword(String v){password=v;} public void setEncryptedPrivateKeyBlob(String v){encryptedPrivateKeyBlob=v;}
    public void setPbkdf2Salt(String v){pbkdf2Salt=v;} public void setPbkdf2Iv(String v){pbkdf2Iv=v;}
    public void setPublicKey(String v){publicKey=v;}
}
