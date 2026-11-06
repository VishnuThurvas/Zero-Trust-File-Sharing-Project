package com.secureshare.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="users")
public class User {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(unique=true,nullable=false) private String username;
    @Column(unique=true,nullable=false) private String email;
    @Column(nullable=false) private String password;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Role role;
    @Column(columnDefinition="TEXT") private String publicKey;
    @Column(columnDefinition="TEXT") private String encryptedPrivateKeyBlob;
    @Column private String pbkdf2Salt;
    @Column private String pbkdf2Iv;
    @Column(nullable=false) private boolean enabled=true;
    @Column(nullable=false) private boolean accountNonLocked=true;
    @Column(nullable=false) private int failedLoginAttempts=0;
    private LocalDateTime lockoutUntil;
    @Column(nullable=false,updatable=false) private LocalDateTime createdAt=LocalDateTime.now();
    private LocalDateTime lastLogin;
    private String lastLoginIp;
    public enum Role { ADMIN, OWNER, EDITOR, VIEWER }
    public User() {}
    public Long getId(){return id;} public String getUsername(){return username;} public String getEmail(){return email;}
    public String getPassword(){return password;} public Role getRole(){return role;} public String getPublicKey(){return publicKey;}
    public String getEncryptedPrivateKeyBlob(){return encryptedPrivateKeyBlob;} public String getPbkdf2Salt(){return pbkdf2Salt;}
    public String getPbkdf2Iv(){return pbkdf2Iv;} public boolean isEnabled(){return enabled;}
    public boolean isAccountNonLocked(){return accountNonLocked;} public int getFailedLoginAttempts(){return failedLoginAttempts;}
    public LocalDateTime getLockoutUntil(){return lockoutUntil;} public LocalDateTime getCreatedAt(){return createdAt;}
    public LocalDateTime getLastLogin(){return lastLogin;} public String getLastLoginIp(){return lastLoginIp;}
    public void setId(Long v){id=v;} public void setUsername(String v){username=v;} public void setEmail(String v){email=v;}
    public void setPassword(String v){password=v;} public void setRole(Role v){role=v;} public void setPublicKey(String v){publicKey=v;}
    public void setEncryptedPrivateKeyBlob(String v){encryptedPrivateKeyBlob=v;} public void setPbkdf2Salt(String v){pbkdf2Salt=v;}
    public void setPbkdf2Iv(String v){pbkdf2Iv=v;} public void setEnabled(boolean v){enabled=v;}
    public void setAccountNonLocked(boolean v){accountNonLocked=v;} public void setFailedLoginAttempts(int v){failedLoginAttempts=v;}
    public void setLockoutUntil(LocalDateTime v){lockoutUntil=v;} public void setLastLogin(LocalDateTime v){lastLogin=v;}
    public void setLastLoginIp(String v){lastLoginIp=v;}
    public static Builder builder(){return new Builder();}
    public static class Builder { private final User u=new User();
        public Builder username(String v){u.username=v;return this;} public Builder email(String v){u.email=v;return this;}
        public Builder password(String v){u.password=v;return this;} public Builder role(Role v){u.role=v;return this;}
        public Builder publicKey(String v){u.publicKey=v;return this;} public Builder encryptedPrivateKeyBlob(String v){u.encryptedPrivateKeyBlob=v;return this;}
        public Builder pbkdf2Salt(String v){u.pbkdf2Salt=v;return this;} public Builder pbkdf2Iv(String v){u.pbkdf2Iv=v;return this;}
        public User build(){return u;} }
}
