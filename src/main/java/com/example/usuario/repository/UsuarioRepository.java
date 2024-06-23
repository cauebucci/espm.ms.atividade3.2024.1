package com.example.usuario.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.usuario.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsuario(String usuario);
    List<Usuario> findByBloqueado(boolean bloqueado);
}
