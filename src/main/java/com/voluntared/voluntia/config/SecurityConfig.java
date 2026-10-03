package com.voluntared.voluntia.config;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.*;
import com.voluntared.voluntia.repository.UsuarioRepository;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
@Configuration
public class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean UserDetailsService userDetailsService(@Value("${voluntia.admin.password}") String password, PasswordEncoder encoder, UsuarioRepository usuarios) {
     String adminHash = encoder.encode(password);
     return nombre -> {
         if ("admin".equals(nombre)) return User.withUsername("admin").password(adminHash).roles("ADMIN").build();
         var usuario = usuarios.findByEmailIgnoreCase(nombre.trim())
                 .orElseThrow(() -> new UsernameNotFoundException("Credenciales incorrectas"));
         if (!java.util.Set.of("VOLUNTARIO", "ORGANIZACION").contains(usuario.getRol())) {
             throw new UsernameNotFoundException("Perfil no disponible");
         }
         return User.withUsername(usuario.getEmail()).password(usuario.getContrasena()).roles(usuario.getRol()).build();
     };
 }
 @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
  return http.authorizeHttpRequests(a -> a
      .requestMatchers("/", "/login", "/registro", "/css/**", "/js/**", "/error").permitAll()
      .requestMatchers("/voluntarios/**", "/organizaciones/**", "/api/voluntarios/**", "/api/organizaciones/**").hasRole("ADMIN")
      .requestMatchers("/mi-cuenta/**").hasAnyRole("VOLUNTARIO", "ORGANIZACION")
      .requestMatchers("/mis-participaciones").hasRole("VOLUNTARIO")
      .requestMatchers("/mis-voluntariados", "/campanias/nuevo", "/campanias/editar/**", "/campanias/guardar", "/campanias/eliminar/**").hasAnyRole("ADMIN", "ORGANIZACION")
      .requestMatchers(HttpMethod.GET, "/campanias", "/campanias/{id}", "/api/campanias", "/api/campanias/{id}").permitAll()
      .anyRequest().authenticated())
      .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/campanias", true).permitAll())
      .logout(l -> l.logoutSuccessUrl("/"))
      .exceptionHandling(e -> e
          .defaultAuthenticationEntryPointFor((req, res, ex) -> {
              res.setStatus(401); res.setContentType("application/json;charset=UTF-8");
              res.getWriter().write("{\"mensaje\":\"Inicia sesión para continuar\"}");
          }, req -> req.getRequestURI().startsWith("/api/"))
          .defaultAuthenticationEntryPointFor(new LoginUrlAuthenticationEntryPoint("/login"), req -> !req.getRequestURI().startsWith("/api/")))
      .build();
 }
}
