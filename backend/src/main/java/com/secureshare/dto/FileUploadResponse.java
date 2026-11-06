package com.secureshare.dto;
import java.time.LocalDateTime;
public class FileUploadResponse {
    private Long fileId; private String filename,sha256Hash,encryptedAesKey,aesIv,message; private LocalDateTime expiresAt; private Integer maxDownloads;
    public FileUploadResponse(){}
    public static Builder builder(){return new Builder();}
    public static class Builder{private final FileUploadResponse r=new FileUploadResponse();
        public Builder fileId(Long v){r.fileId=v;return this;} public Builder filename(String v){r.filename=v;return this;}
        public Builder sha256Hash(String v){r.sha256Hash=v;return this;} public Builder encryptedAesKey(String v){r.encryptedAesKey=v;return this;}
        public Builder aesIv(String v){r.aesIv=v;return this;} public Builder message(String v){r.message=v;return this;}
        public Builder expiresAt(LocalDateTime v){r.expiresAt=v;return this;} public Builder maxDownloads(Integer v){r.maxDownloads=v;return this;}
        public FileUploadResponse build(){return r;}}
    public Long getFileId(){return fileId;} public String getFilename(){return filename;}
    public String getSha256Hash(){return sha256Hash;} public String getEncryptedAesKey(){return encryptedAesKey;}
    public String getAesIv(){return aesIv;} public String getMessage(){return message;}
    public LocalDateTime getExpiresAt(){return expiresAt;} public Integer getMaxDownloads(){return maxDownloads;}
}
