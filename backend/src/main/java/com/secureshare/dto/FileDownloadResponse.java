package com.secureshare.dto;
public class FileDownloadResponse {
    private String filename,encryptedData,encryptedAesKey,aesIv,sha256Hash,mimeType;
    public FileDownloadResponse(){}
    public static Builder builder(){return new Builder();}
    public static class Builder{private final FileDownloadResponse r=new FileDownloadResponse();
        public Builder filename(String v){r.filename=v;return this;} public Builder encryptedData(String v){r.encryptedData=v;return this;}
        public Builder encryptedAesKey(String v){r.encryptedAesKey=v;return this;} public Builder aesIv(String v){r.aesIv=v;return this;}
        public Builder sha256Hash(String v){r.sha256Hash=v;return this;} public Builder mimeType(String v){r.mimeType=v;return this;}
        public FileDownloadResponse build(){return r;}}
    public String getFilename(){return filename;} public String getEncryptedData(){return encryptedData;}
    public String getEncryptedAesKey(){return encryptedAesKey;} public String getAesIv(){return aesIv;}
    public String getSha256Hash(){return sha256Hash;} public String getMimeType(){return mimeType;}
}
