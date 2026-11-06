package com.secureshare.util;
import org.springframework.stereotype.Component;
import javax.crypto.*; import javax.crypto.spec.*;
import java.security.*; import java.security.spec.*;
import java.util.Base64;
@Component
public class CryptoUtil {
    private static final int GCM_IV=12,GCM_TAG=128,AES_SIZE=256,RSA_SIZE=2048;
    public KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator kg=KeyPairGenerator.getInstance("RSA"); kg.initialize(RSA_SIZE,new SecureRandom()); return kg.generateKeyPair(); }
    public String publicKeyToBase64(PublicKey pk){return Base64.getEncoder().encodeToString(pk.getEncoded());}
    public String sha256Hash(byte[] data) throws NoSuchAlgorithmException {
        return Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(data));}
    public String computeLogHash(String prev,String data) throws NoSuchAlgorithmException {
        String combined=(prev!=null?prev:"GENESIS")+"|"+data;
        return Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(combined.getBytes()));}
    public String toBase64(byte[] data){return Base64.getEncoder().encodeToString(data);}
    public byte[] fromBase64(String data){
        String s=data.replace('-','+').replace('_','/');
        int pad=(4-s.length()%4)%4; s=s+"==".substring(0,Math.min(pad,2));
        return Base64.getDecoder().decode(s);}
    public String generateSecureToken(){byte[] b=new byte[32];new SecureRandom().nextBytes(b);return Base64.getUrlEncoder().withoutPadding().encodeToString(b);}
}
