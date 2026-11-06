package com.secureshare.service;
import com.secureshare.entity.User; import com.secureshare.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service; import java.util.List;
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository repo;
    public CustomUserDetailsService(UserRepository repo){this.repo=repo;}
    @Override public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u=repo.findByUsername(username).orElseThrow(()->new UsernameNotFoundException("User not found: "+username));
        return org.springframework.security.core.userdetails.User.builder()
            .username(u.getUsername()).password(u.getPassword())
            .authorities(List.of(new SimpleGrantedAuthority("ROLE_"+u.getRole().name())))
            .accountExpired(false).accountLocked(!u.isAccountNonLocked()).credentialsExpired(false).disabled(!u.isEnabled()).build();}
}
