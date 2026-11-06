package com.secureshare.dto;
import java.time.LocalDateTime;
public class FileInfoResponse {
    private Long id; private String filename,mimeType,scanStatus,sha256Hash; private long fileSize;
    private LocalDateTime uploadedAt,expiresAt; private int downloadCount; private Integer maxDownloads; private boolean honeyFile;
    public FileInfoResponse(){}
    public static Builder builder(){return new Builder();}
    public static class Builder{private final FileInfoResponse r=new FileInfoResponse();
        public Builder id(Long v){r.id=v;return this;} public Builder filename(String v){r.filename=v;return this;}
        public Builder mimeType(String v){r.mimeType=v;return this;} public Builder scanStatus(String v){r.scanStatus=v;return this;}
        public Builder sha256Hash(String v){r.sha256Hash=v;return this;} public Builder fileSize(long v){r.fileSize=v;return this;}
        public Builder uploadedAt(LocalDateTime v){r.uploadedAt=v;return this;} public Builder expiresAt(LocalDateTime v){r.expiresAt=v;return this;}
        public Builder downloadCount(int v){r.downloadCount=v;return this;} public Builder maxDownloads(Integer v){r.maxDownloads=v;return this;}
        public Builder honeyFile(boolean v){r.honeyFile=v;return this;} public FileInfoResponse build(){return r;}}
    public Long getId(){return id;} public String getFilename(){return filename;} public String getMimeType(){return mimeType;}
    public String getScanStatus(){return scanStatus;} public String getSha256Hash(){return sha256Hash;}
    public long getFileSize(){return fileSize;} public LocalDateTime getUploadedAt(){return uploadedAt;}
    public LocalDateTime getExpiresAt(){return expiresAt;} public int getDownloadCount(){return downloadCount;}
    public Integer getMaxDownloads(){return maxDownloads;} public boolean isHoneyFile(){return honeyFile;}
}
