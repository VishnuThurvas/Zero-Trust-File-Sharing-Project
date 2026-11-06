package com.secureshare.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="audit_logs")
public class AuditLog {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private AuditAction action;
    @Column(nullable=false) private String actorUsername;
    private Long fileId; private String filename; private String targetUsername;
    private String ipAddress; private String userAgent;
    @Column(columnDefinition="TEXT") private String details;
    @Column(nullable=false,updatable=false) private LocalDateTime timestamp=LocalDateTime.now();
    @Column(nullable=false) private String recordHash;
    private String previousHash;
    public enum AuditAction{USER_REGISTERED,USER_LOGIN,USER_LOGIN_FAILED,USER_LOCKED,
        FILE_UPLOADED,FILE_DOWNLOADED,FILE_SHARED,FILE_DELETED,FILE_EXPIRED,
        SHARE_REVOKED,HONEY_FILE_ACCESSED,SUSPICIOUS_ACTIVITY,MALWARE_DETECTED}
    public AuditLog(){}
    public Long getId(){return id;} public AuditAction getAction(){return action;}
    public String getActorUsername(){return actorUsername;} public Long getFileId(){return fileId;}
    public String getFilename(){return filename;} public String getTargetUsername(){return targetUsername;}
    public String getIpAddress(){return ipAddress;} public String getUserAgent(){return userAgent;}
    public String getDetails(){return details;} public LocalDateTime getTimestamp(){return timestamp;}
    public String getRecordHash(){return recordHash;} public String getPreviousHash(){return previousHash;}
    public void setAction(AuditAction v){action=v;} public void setActorUsername(String v){actorUsername=v;}
    public void setFileId(Long v){fileId=v;} public void setFilename(String v){filename=v;}
    public void setTargetUsername(String v){targetUsername=v;} public void setIpAddress(String v){ipAddress=v;}
    public void setUserAgent(String v){userAgent=v;} public void setDetails(String v){details=v;}
    public void setTimestamp(LocalDateTime v){timestamp=v;} public void setRecordHash(String v){recordHash=v;}
    public void setPreviousHash(String v){previousHash=v;}
    public static Builder builder(){return new Builder();}
    public static class Builder{private final AuditLog a=new AuditLog();
        public Builder action(AuditAction v){a.action=v;return this;}
        public Builder actorUsername(String v){a.actorUsername=v;return this;}
        public Builder fileId(Long v){a.fileId=v;return this;}
        public Builder filename(String v){a.filename=v;return this;}
        public Builder targetUsername(String v){a.targetUsername=v;return this;}
        public Builder ipAddress(String v){a.ipAddress=v;return this;}
        public Builder userAgent(String v){a.userAgent=v;return this;}
        public Builder details(String v){a.details=v;return this;}
        public Builder previousHash(String v){a.previousHash=v;return this;}
        public Builder recordHash(String v){a.recordHash=v;return this;}
        public AuditLog build(){return a;}}
}
