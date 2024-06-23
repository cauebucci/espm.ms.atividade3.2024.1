package com.example.usuario.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.usuario.kafka.KafkaProducer;
import com.example.usuario.model.Usuario;
import com.example.usuario.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository userRepository;

    @Autowired
        private KafkaProducer kafkaProducer;

    public Usuario criarUsuario(String user, String senha) {
        if (userRepository.findByUsuario(user).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        Usuario usuario = new Usuario();
        usuario.setUsuario(user);
        usuario.setSenha(senha);
        return userRepository.save(usuario);
    }

    public Usuario logar(String user, String senha) {
        Usuario usuario = userRepository.findByUsuario(user)
                .orElseThrow(() -> new IllegalArgumentException("Usuário ou senha incorreto"));
        
        if (usuario.bloqueado()) {
            throw new IllegalArgumentException("Usuário bloqueado!");
        }

        if (usuario.getSenha().equals(senha)) {
            usuario.setEntradas(usuario.getEntradas() + 1);
            if (usuario.getEntradas() > 10) {
                throw new IllegalArgumentException("A senha deve ser alterada!");
            }
        } else {
            usuario.setTentativas(usuario.getTentativas() + 1);
            if (usuario.getTentativas() > 5) {
                usuario.setBloqueado(true);
            }
            userRepository.save(usuario);
            kafkaProducer.sendMessage(user);
            throw new IllegalArgumentException("Usuário ou senha inváidos");
        }

        return userRepository.save(usuario);
    }

    public Usuario mudarSenha(String user, String senhaAntiga, String senhaNova) {
        Usuario usuario = userRepository.findByUsuario(user)
                .orElseThrow(() -> new IllegalArgumentException("Usuário ou senha inváidos"));

        if (!usuario.getSenha().equals(senhaAntiga)) {
            throw new IllegalArgumentException("Senha inválida");
        }

        usuario.setSenha(senhaAntiga);
        usuario.setEntradas(0);
        return userRepository.save(usuario);
    }

    public List<Usuario> getBloqueados() {
        return userRepository.findByBloqueado(true);
    }

    public Usuario desbloquearUsuario(String user) {
        Optional<Usuario> usuarioEncontrado = userRepository.findByUsuario(user);
        if (usuarioEncontrado.isPresent()) {
            Usuario usuario = usuarioEncontrado.get();
            usuario.setBloqueado(false);
            usuario.setTentativas(0);
            return userRepository.save(usuario);
        } else {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
    }
}
