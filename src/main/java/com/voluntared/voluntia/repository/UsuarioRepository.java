package com.voluntared.voluntia.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.voluntared.voluntia.entity.Usuario;
public interface UsuarioRepository extends JpaRepository<Usuario,Integer> {
    boolean existsByEmailIgnoreCaseAndIdNot(String email,Integer id);
    java.util.Optional<Usuario> findByEmailIgnoreCase(String email);
}
