package com.gymmanager.gym_manager.controllers;

import java.math.BigDecimal;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gymmanager.gym_manager.config.SecurityUtils;
import com.gymmanager.gym_manager.entity.Usuario;
import com.gymmanager.gym_manager.repository.UsuarioRepository;
import com.gymmanager.gym_manager.services.ConfiguracionDePagoService;

@Controller
public class UsuarioController {

    private final UsuarioRepository          usuarioRepository;
    private final PasswordEncoder            passwordEncoder;
    private final ConfiguracionDePagoService configuracionDePagoService;
    private final SecurityUtils              securityUtils;

    public UsuarioController(UsuarioRepository          usuarioRepository,
                             PasswordEncoder            passwordEncoder,
                             ConfiguracionDePagoService configuracionDePagoService,
                             SecurityUtils              securityUtils) {
        this.usuarioRepository          = usuarioRepository;
        this.passwordEncoder            = passwordEncoder;
        this.configuracionDePagoService = configuracionDePagoService;
        this.securityUtils              = securityUtils;
    }

    // ── GET /usuarios — solo ADMIN ────────────────────────────────────────────

    @GetMapping("/usuarios")
    public String listar(Model model) {
        model.addAttribute("usuarios",  usuarioRepository.findAll());
        model.addAttribute("title",     "Gym Manager | Usuarios");
        model.addAttribute("header",    "Administración / Usuarios");
        model.addAttribute("vista",     "admin/usuarios");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active",    "usuarios");
        return "layouts/main";
    }

    // ── POST /usuarios/crear — solo ADMIN ─────────────────────────────────────

    @PostMapping("/usuarios/crear")
    public String crear(@RequestParam String username,
                        @RequestParam String password,
                        @RequestParam(required = false) String nombreGimnasio,
                        RedirectAttributes redirectAttributes) {

        String errorPassword = validarPassword(password, password);
        if (errorPassword != null) {
            redirectAttributes.addFlashAttribute("error", errorPassword);
            return "redirect:/usuarios";
        }
        if (usuarioRepository.existsByUsername(username.trim())) {
            redirectAttributes.addFlashAttribute("error", "El nombre de usuario ya existe.");
            return "redirect:/usuarios";
        }

        Usuario nuevo = new Usuario();
        nuevo.setUsername(username.trim());
        nuevo.setPassword(passwordEncoder.encode(password));
        nuevo.setNombreGimnasio(nombreGimnasio);
        nuevo.setRol("ROLE_USER");
        usuarioRepository.save(nuevo);
        crearMetodosIniciales(nuevo);

        redirectAttributes.addFlashAttribute("success",
                "Usuario '" + username + "' creado correctamente.");
        return "redirect:/usuarios";
    }

    // ── POST /usuarios/resetear-password/{id} — solo ADMIN ───────────────────
    // El admin puede resetear la contraseña de cualquier usuario

    @PostMapping("/usuarios/resetear-password/{id}")
    public String resetearPassword(@PathVariable Integer id,
                                   @RequestParam String nuevaPassword,
                                   RedirectAttributes redirectAttributes) {

        String error = validarPassword(nuevaPassword, nuevaPassword);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/usuarios";
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);

        redirectAttributes.addFlashAttribute("success",
                "Contraseña de '" + usuario.getUsername() + "' reseteada correctamente.");
        return "redirect:/usuarios";
    }

    // ── POST /usuarios/eliminar/{id} — solo ADMIN ─────────────────────────────

    @PostMapping("/usuarios/eliminar/{id}")
    public String eliminar(@PathVariable Integer id,
                           RedirectAttributes redirectAttributes) {
        Usuario actual = securityUtils.getUsuarioActual();
        if (actual.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "No podés eliminar tu propio usuario.");
            return "redirect:/usuarios";
        }
        usuarioRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Usuario eliminado.");
        return "redirect:/usuarios";
    }

    // ── GET /perfil ───────────────────────────────────────────────────────────

    @GetMapping("/perfil")
    public String perfil(Model model) {
        model.addAttribute("usuario",   securityUtils.getUsuarioActual());
        model.addAttribute("title",     "Gym Manager | Mi Perfil");
        model.addAttribute("header",    "Mi Perfil");
        model.addAttribute("vista",     "configuracion/perfil");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active",    "perfil");
        return "layouts/main";
    }

    // ── POST /perfil/cambiar-password ─────────────────────────────────────────

    @PostMapping("/perfil/cambiar-password")
    public String cambiarPassword(@RequestParam String passwordActual,
                                  @RequestParam String passwordNueva,
                                  @RequestParam String confirmar,
                                  RedirectAttributes redirectAttributes) {
        Usuario usuario = securityUtils.getUsuarioActual();

        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "La contraseña actual es incorrecta.");
            return "redirect:/perfil";
        }

        String error = validarPassword(passwordNueva, confirmar);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/perfil";
        }

        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);
        redirectAttributes.addFlashAttribute("success", "Contraseña actualizada correctamente.");
        return "redirect:/perfil";
    }

    // ── POST /perfil/actualizar ───────────────────────────────────────────────

    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(@RequestParam(required = false) String nombreGimnasio,
                                   RedirectAttributes redirectAttributes) {
        Usuario usuario = securityUtils.getUsuarioActual();
        usuario.setNombreGimnasio(nombreGimnasio);
        usuarioRepository.save(usuario);
        redirectAttributes.addFlashAttribute("success", "Perfil actualizado.");
        return "redirect:/perfil";
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String validarPassword(String password, String confirmar) {
        if (password == null || password.length() < 8)
            return "La contraseña debe tener al menos 8 caracteres.";
        if (!password.equals(confirmar))
            return "Las contraseñas no coinciden.";
        return null;
    }

    private void crearMetodosIniciales(Usuario usuario) {
        crearMetodo("NO_ESPECIFICADO", BigDecimal.ZERO,        usuario);
        crearMetodo("EFECTIVO",         BigDecimal.ZERO,        usuario);
        crearMetodo("TRANSFERENCIA",    BigDecimal.ZERO,        usuario);
        crearMetodo("TARJETA/CREDITO",  BigDecimal.valueOf(15), usuario);
    }

    private void crearMetodo(String nombre, BigDecimal recargo, Usuario usuario) {
        try {
            configuracionDePagoService.crearMetodoConRecargo(nombre, recargo, usuario);
        } catch (IllegalArgumentException e) {
            // Ya existe — ignorar
        }
    }
}