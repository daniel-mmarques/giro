package com.giro.service;

import com.giro.entites.UtilizadorEntity;
import com.giro.repository.UtilizadorRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class SUserDetailsService implements UserDetailsService {

    private UtilizadorRepository userRepository;

    public SUserDetailsService(UtilizadorRepository utilizadorRepository) {
        this.userRepository = utilizadorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {

            var user = userRepository.findByEmailAndAtivo(email, Boolean.TRUE);

            if (user.isEmpty()) {
                return null;
            }

            return new org.springframework.security.core.userdetails.User(
                    user.get().getEmail(), user.get().getPassword(), getAuthories(user.get())
            );

        } catch (Exception e) {
            throw new UsernameNotFoundException("Erro na autenticação");
        }
    }

    private Set<GrantedAuthority> getAuthories(UtilizadorEntity user) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add((GrantedAuthority) () -> "USER");
        return authorities;
    }

/*    public void createUser(UserModel user) {
        UtilizadorModel newUser = new UtilizadorModel();
        newUser.setEmail(user.getEmail());
        newUser.setUsername(user.getUsername());
        newUser.setPassword(customPasswordEncoder.encode(user.getPassword()));
        userRepository.save(newUser);
    }*/

}