package com.secureshare.dto;
import java.time.LocalDateTime;
public class ShareResponse {
    private String shareToken,sharedWith,permission,message; private LocalDateTime expiresAt;
    public ShareResponse(){}
    public static Builder builder(){return new Builder();}
    public static class Builder{private final ShareResponse r=new ShareResponse();
        public Builder shareToken(String v){r.shareToken=v;return this;} public Builder sharedWith(String v){r.sharedWith=v;return this;}
        public Builder permission(String v){r.permission=v;return this;} public Builder message(String v){r.message=v;return this;}
        public Builder expiresAt(LocalDateTime v){r.expiresAt=v;return this;} public ShareResponse build(){return r;}}
    public String getShareToken(){return shareToken;} public String getSharedWith(){return sharedWith;}
    public String getPermission(){return permission;} public String getMessage(){return message;}
    public LocalDateTime getExpiresAt(){return expiresAt;}
}
