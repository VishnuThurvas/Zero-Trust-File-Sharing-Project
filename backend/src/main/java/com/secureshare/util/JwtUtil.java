package com.secureshare.util;
import io.jsonwebtoken.*; import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key; import java.util.*; 
@Component
public class JwtUtil {
    @Value("${app.jwt.secret}") private String secret;
    @Value("${app.jwt.expiration}") private long expiration;
    @Value("${app.jwt.refresh-expiration}") private long refreshExpiration;
    private Key key(){return Keys.hmacShaKeyFor(secret.getBytes());}
    public String generateToken(String u,String role){Map<String,Object> c=new HashMap<>();c.put("role",role);c.put("type","access");return build(c,u,expiration);}
    public String generateRefreshToken(String u){Map<String,Object> c=new HashMap<>();c.put("type","refresh");return build(c,u,refreshExpiration);}
    public String generateShareToken(String u,Long fid,int hrs){Map<String,Object> c=new HashMap<>();c.put("type","share");c.put("fileId",fid);return build(c,u,(long)hrs*3600000);}
    private String build(Map<String,Object> c,String sub,long ms){
        return Jwts.builder().setClaims(c).setSubject(sub).setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis()+ms)).signWith(key(),SignatureAlgorithm.HS512).compact();}
    public String extractUsername(String t){return claims(t).getSubject();}
    public boolean isTokenValid(String t){try{return !claims(t).getExpiration().before(new Date());}catch(JwtException e){return false;}}
    private Claims claims(String t){return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(t).getBody();}
}
