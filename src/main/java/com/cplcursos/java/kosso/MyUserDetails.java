package com.cplcursos.java.kosso;

import java.util.*;

import com.cplcursos.java.kosso.entities.Rol;
import com.cplcursos.java.kosso.entities.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class MyUserDetails implements UserDetails {
    private Usuario user;

    public MyUserDetails(Usuario user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<Rol> roles = user.getRoles();
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        for (Rol role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return this.user.getClave();
    }

    @Override
    public String getUsername() {
        return this.user.getEmail();
    }

    public void setFirstName(String firstName) {
        this.user.setNombre(firstName);
    }

    public void setLastName(String lastName) {
        this.user.setApellidos(lastName);
    }

    public void setDescripcion(String descripcion) {
        this.user.setDescripcion(descripcion);
    }

    public void setFoto(String foto) {
        this.user.setFoto(foto);
    }

    public void setClave(String clave) {
        this.user.setClave(clave);
    }

    public void setEmail(String email) {
        this.user.setEmail(email);
    }

    public void setRoles(List<Rol> roles) {
        this.user.setRoles(roles);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
