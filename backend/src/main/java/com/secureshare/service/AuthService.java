package com.secureshare.service;
import com.secureshare.dto.*; import com.secureshare.entity.AuditLog; import com.secureshare.entity.User;
import com.secureshare.repository.UserRepository; import com.secureshare.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*; import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
@Service
public class AuthService {
    private final UserRepository repo; private final PasswordEncoder enc; private final JwtUtil jwt;
    private final AuditLogService audit; private final AuthenticationManager auth;
    @Value("${app.security.max-login-attempts:5}") private int maxAttempts;
    @Value("${app.security.lockout-duration-minutes:15}") private int lockMins;
    public AuthService(UserRepository r,PasswordEncoder e,JwtUtil j,AuditLogService a,AuthenticationManager am){repo=r;enc=e;jwt=j;audit=a;auth=am;}
    @Transactional public RegisterResponse register(RegisterRequest req){
        if(repo.existsByUsername(req.getUsername())) throw new IllegalArgumentException("Username taken");
        if(repo.existsByEmail(req.getEmail())) throw new IllegalArgumentException("Email registered");
        User u=User.builder().username(req.getUsername()).email(req.getEmail()).password(enc.encode(req.getPassword()))
            .role(User.Role.OWNER).publicKey(req.getPublicKey())
            .encryptedPrivateKeyBlob(req.getEncryptedPrivateKeyBlob())
            .pbkdf2Salt(req.getPbkdf2Salt()).pbkdf2Iv(req.getPbkdf2Iv()).build();
        repo.save(u);
        audit.log(AuditLog.AuditAction.USER_REGISTERED,req.getUsername(),null,null,null,"system",null,"Registered");
        return RegisterResponse.builder().message("Success").username(u.getUsername()).publicKey(u.getPublicKey()).build();}
    @Transactional public LoginResponse login(LoginRequest req,String ip,String ua){
        User u=repo.findByUsername(req.getUsername()).orElseThrow(()->new BadCredentialsException("Invalid credentials"));
        if(!u.isAccountNonLocked()){
            if(u.getLockoutUntil()!=null&&LocalDateTime.now().isAfter(u.getLockoutUntil())){u.setAccountNonLocked(true);u.setFailedLoginAttempts(0);}
            else throw new BadCredentialsException("Account locked. Try in "+lockMins+" minutes.");}
        try{auth.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(),req.getPassword()));}
        catch(BadCredentialsException e){
            u.setFailedLoginAttempts(u.getFailedLoginAttempts()+1);
            if(u.getFailedLoginAttempts()>=maxAttempts){u.setAccountNonLocked(false);u.setLockoutUntil(LocalDateTime.now().plusMinutes(lockMins));
                audit.log(AuditLog.AuditAction.USER_LOCKED,u.getUsername(),null,null,null,ip,null,"Locked");}
            repo.save(u); audit.log(AuditLog.AuditAction.USER_LOGIN_FAILED,req.getUsername(),null,null,null,ip,ua,"Bad password");
            throw new BadCredentialsException("Invalid credentials");}
        u.setFailedLoginAttempts(0);u.setLastLogin(LocalDateTime.now());u.setLastLoginIp(ip);repo.save(u);
        audit.log(AuditLog.AuditAction.USER_LOGIN,u.getUsername(),null,null,null,ip,ua,"Login OK");
        return LoginResponse.builder().token(jwt.generateToken(u.getUsername(),u.getRole().name()))
            .refreshToken(jwt.generateRefreshToken(u.getUsername())).username(u.getUsername())
            .role(u.getRole().name()).email(u.getEmail()).publicKey(u.getPublicKey())
            .encryptedPrivateKeyBlob(u.getEncryptedPrivateKeyBlob())
            .pbkdf2Salt(u.getPbkdf2Salt()).pbkdf2Iv(u.getPbkdf2Iv()).build();}
}
