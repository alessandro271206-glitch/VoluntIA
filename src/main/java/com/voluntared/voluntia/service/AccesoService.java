package com.voluntared.voluntia.service;

import com.voluntared.voluntia.entity.Campania;
import com.voluntared.voluntia.entity.Usuario;
import com.voluntared.voluntia.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/** Resuelve el perfil desde la sesión, sin confiar en IDs recibidos del formulario. */
@Service
public class AccesoService {
    private final UsuarioRepository usuarios;
    private final OrganizacionRepository organizaciones;
    private final VoluntarioRepository voluntarios;

    public AccesoService(UsuarioRepository usuarios, OrganizacionRepository organizaciones, VoluntarioRepository voluntarios) {
        this.usuarios = usuarios;
        this.organizaciones = organizaciones;
        this.voluntarios = voluntarios;
    }

    public boolean rol(String rol) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + rol));
    }

    public boolean admin() { return rol("ADMIN"); }

    public void exigirAdmin() {
        if (!admin()) throw new AccessDeniedException("Acceso exclusivo del administrador");
    }

    private Usuario usuario(String rol) {
        if (!rol(rol)) throw new AccessDeniedException("Este perfil no puede realizar esta acción");
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarios.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AccessDeniedException("La cuenta ya no está disponible"));
        if (!rol.equals(usuario.getRol())) throw new AccessDeniedException("El perfil de la cuenta ha cambiado");
        return usuario;
    }

    public Integer organizacionId() {
        return organizaciones.findByUsuarioId(usuario("ORGANIZACION").getId())
                .orElseThrow(() -> new AccessDeniedException("Organización no disponible")).getId();
    }

    public Integer voluntarioId() {
        return voluntarios.findByUsuarioId(usuario("VOLUNTARIO").getId())
                .orElseThrow(() -> new AccessDeniedException("Voluntario no disponible")).getId();
    }

    public boolean puedeGestionar(Campania campania) {
        return admin() || (rol("ORGANIZACION") && campania.getOrganizacion() != null
                && campania.getOrganizacion().getId().equals(organizacionId()));
    }

    public void exigirGestion(Campania campania) {
        if (!puedeGestionar(campania)) throw new AccessDeniedException("Solo la organización propietaria puede gestionar este voluntariado");
    }

    public void exigirCuenta(Integer id, boolean organizacion) {
        if (admin()) return;
        boolean propia = organizacion ? rol("ORGANIZACION") && organizacionId().equals(id)
                : rol("VOLUNTARIO") && voluntarioId().equals(id);
        if (!propia) throw new AccessDeniedException("No puedes acceder a otra cuenta");
    }
}
