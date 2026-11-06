package com.secureshare.security;
import com.secureshare.service.CustomUserDetailsService; import com.secureshare.util.JwtUtil;
import jakarta.servlet.*; import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component; import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwt; private final CustomUserDetailsService uds;
    public JwtAuthenticationFilter(JwtUtil jwt,CustomUserDetailsService uds){this.jwt=jwt;this.uds=uds;}
    @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws ServletException,IOException {
        String h=req.getHeader("Authorization");
        if(h!=null&&h.startsWith("Bearer ")){
            String token=h.substring(7);
            try{if(jwt.isTokenValid(token)){String u=jwt.extractUsername(token);
                if(u!=null&&SecurityContextHolder.getContext().getAuthentication()==null){
                    UserDetails ud=uds.loadUserByUsername(u);
                    UsernamePasswordAuthenticationToken auth=new UsernamePasswordAuthenticationToken(ud,null,ud.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(auth);}}}
            catch(Exception e){System.out.println("[JWT] "+e.getMessage());}}
        chain.doFilter(req,res);}
}
