package com.secureshare.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="file_records")
public class FileRecord {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String originalFilename;
    @Column(nullable=false,unique=true) private String storageKey;
    @Column(nullable=false) private long fileSize;
    private String mimeType;
    @Column(nullable=false) private String sha256Hash;
    @Column(columnDefinition="TEXT") private String encryptedAesKey;
    private String aesIv;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="owner_id",nullable=false) private User owner;
    @Enumerated(EnumType.STRING) private FileStatus status=FileStatus.ACTIVE;
    private Integer maxDownloads; private int downloadCount=0; private LocalDateTime expiresAt;
    @Column(nullable=false,updatable=false) private LocalDateTime uploadedAt=LocalDateTime.now();
    @Enumerated(EnumType.STRING) private ScanStatus scanStatus=ScanStatus.PENDING;
    private String scanResult; private boolean honeyFile=false;
    public enum FileStatus{ACTIVE,DELETED,EXPIRED,QUARANTINED}
    public enum ScanStatus{PENDING,CLEAN,INFECTED,SKIPPED}
    public FileRecord(){}
    public Long getId(){return id;} public String getOriginalFilename(){return originalFilename;}
    public String getStorageKey(){return storageKey;} public long getFileSize(){return fileSize;}
    public String getMimeType(){return mimeType;} public String getSha256Hash(){return sha256Hash;}
    public String getEncryptedAesKey(){return encryptedAesKey;} public String getAesIv(){return aesIv;}
    public User getOwner(){return owner;} public FileStatus getStatus(){return status;}
    public Integer getMaxDownloads(){return maxDownloads;} public int getDownloadCount(){return downloadCount;}
    public LocalDateTime getExpiresAt(){return expiresAt;} public LocalDateTime getUploadedAt(){return uploadedAt;}
    public ScanStatus getScanStatus(){return scanStatus;} public String getScanResult(){return scanResult;}
    public boolean isHoneyFile(){return honeyFile;}
    public void setId(Long v){id=v;} public void setOriginalFilename(String v){originalFilename=v;}
    public void setStorageKey(String v){storageKey=v;} public void setFileSize(long v){fileSize=v;}
    public void setMimeType(String v){mimeType=v;} public void setSha256Hash(String v){sha256Hash=v;}
    public void setEncryptedAesKey(String v){encryptedAesKey=v;} public void setAesIv(String v){aesIv=v;}
    public void setOwner(User v){owner=v;} public void setStatus(FileStatus v){status=v;}
    public void setMaxDownloads(Integer v){maxDownloads=v;} public void setDownloadCount(int v){downloadCount=v;}
    public void setExpiresAt(LocalDateTime v){expiresAt=v;} public void setScanStatus(ScanStatus v){scanStatus=v;}
    public void setScanResult(String v){scanResult=v;} public void setHoneyFile(boolean v){honeyFile=v;}
    public static Builder builder(){return new Builder();}
    public static class Builder{private final FileRecord f=new FileRecord();
        public Builder originalFilename(String v){f.originalFilename=v;return this;}
        public Builder storageKey(String v){f.storageKey=v;return this;}
        public Builder fileSize(long v){f.fileSize=v;return this;}
        public Builder mimeType(String v){f.mimeType=v;return this;}
        public Builder sha256Hash(String v){f.sha256Hash=v;return this;}
        public Builder encryptedAesKey(String v){f.encryptedAesKey=v;return this;}
        public Builder aesIv(String v){f.aesIv=v;return this;}
        public Builder owner(User v){f.owner=v;return this;}
        public Builder maxDownloads(Integer v){f.maxDownloads=v;return this;}
        public Builder expiresAt(LocalDateTime v){f.expiresAt=v;return this;}
        public Builder scanStatus(ScanStatus v){f.scanStatus=v;return this;}
        public Builder honeyFile(boolean v){f.honeyFile=v;return this;}
        public FileRecord build(){return f;}}
}
