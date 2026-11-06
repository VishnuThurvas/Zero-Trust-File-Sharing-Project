package com.secureshare.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="file_shares")
public class FileShare {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="file_id",nullable=false) private FileRecord file;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="shared_with_id") private User sharedWith;
    @Column(unique=true) private String shareToken;
    @Enumerated(EnumType.STRING) private Permission permission=Permission.VIEWER;
    private LocalDateTime expiresAt; private boolean active=true;
    @Column(nullable=false,updatable=false) private LocalDateTime createdAt=LocalDateTime.now();
    @Column(columnDefinition="TEXT") private String recipientEncryptedKey;
    public enum Permission{VIEWER,EDITOR}
    public FileShare(){}
    public Long getId(){return id;} public FileRecord getFile(){return file;}
    public User getSharedWith(){return sharedWith;} public String getShareToken(){return shareToken;}
    public Permission getPermission(){return permission;} public LocalDateTime getExpiresAt(){return expiresAt;}
    public boolean isActive(){return active;} public String getRecipientEncryptedKey(){return recipientEncryptedKey;}
    public void setFile(FileRecord v){file=v;} public void setSharedWith(User v){sharedWith=v;}
    public void setShareToken(String v){shareToken=v;} public void setPermission(Permission v){permission=v;}
    public void setExpiresAt(LocalDateTime v){expiresAt=v;} public void setActive(boolean v){active=v;}
    public void setRecipientEncryptedKey(String v){recipientEncryptedKey=v;}
    public static Builder builder(){return new Builder();}
    public static class Builder{private final FileShare s=new FileShare();
        public Builder file(FileRecord v){s.file=v;return this;}
        public Builder sharedWith(User v){s.sharedWith=v;return this;}
        public Builder shareToken(String v){s.shareToken=v;return this;}
        public Builder permission(Permission v){s.permission=v;return this;}
        public Builder expiresAt(LocalDateTime v){s.expiresAt=v;return this;}
        public Builder recipientEncryptedKey(String v){s.recipientEncryptedKey=v;return this;}
        public FileShare build(){return s;}}
}
