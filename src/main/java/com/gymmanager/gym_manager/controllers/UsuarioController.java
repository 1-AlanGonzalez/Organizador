package com.gymmanager.gym_manager.controllers;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gymmanager.gym_manager.config.SecurityUtils;
import com.gymmanager.gym_manager.entity.Usuario;
import com.gymmanager.gym_manager.repository.UsuarioRepository;
import com.gymmanager.gym_manager.services.UsuarioAdminService;

@Controller
public class UsuarioController {

    private final UsuarioRepository          usuarioRepository;
    private final PasswordEncoder            passwordEncoder;
    private final SecurityUtils              securityUtils;
    private final UsuarioAdminService        usuarioAdminService;

    public UsuarioController(UsuarioRepository          usuarioRepository,
                             PasswordEncoder            passwordEncoder,
                             SecurityUtils              securityUtils,
                             UsuarioAdminService        usuarioAdminService) {
        this.usuarioRepository          = usuarioRepository;
        this.passwordEncoder            = passwordEncoder;
        this.securityUtils              = securityUtils;
        this.usuarioAdminService        = usuarioAdminService;
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

        try {
            usuarioAdminService.crearGimnasio(
                    username,
                    password,
                    nombreGimnasio
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Usuario '" + username + "' creado correctamente."
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

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
        try {
            usuarioAdminService.eliminarGimnasio(id, actual.getId());
            redirectAttributes.addFlashAttribute("success", "Usuario y datos del gimnasio eliminados.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se pudo eliminar la cuenta. No se realizaron cambios."
            );
        }
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
                                  @RequestParam(defaultValue = "false") boolean desdeConfiguracion,
                                  RedirectAttributes redirectAttributes) {
        Usuario usuario = securityUtils.getUsuarioActual();

        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "La contraseña actual es incorrecta.");
            return redireccionPerfil(desdeConfiguracion, redirectAttributes);
        }

        String error = validarPassword(passwordNueva, confirmar);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return redireccionPerfil(desdeConfiguracion, redirectAttributes);
        }

        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);
        redirectAttributes.addFlashAttribute("success", "Contraseña actualizada correctamente.");
        return redireccionPerfil(desdeConfiguracion, redirectAttributes);
    }

    // ── POST /perfil/actualizar ───────────────────────────────────────────────

    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(@RequestParam(required = false) String nombreGimnasio,
                                   @RequestParam(defaultValue = "false") boolean desdeConfiguracion,
                                   RedirectAttributes redirectAttributes) {
        Usuario usuario = securityUtils.getUsuarioActual();
        usuario.setNombreGimnasio(nombreGimnasio);
        usuarioRepository.save(usuario);
        redirectAttributes.addFlashAttribute("success", "Perfil actualizado.");
        return redireccionPerfil(desdeConfiguracion, redirectAttributes);
    }

    private String redireccionPerfil(boolean desdeConfiguracion,
                                     RedirectAttributes redirectAttributes) {
        if (desdeConfiguracion) {
            redirectAttributes.addFlashAttribute("tabActivo", "cuenta");
            return "redirect:/configuracion";
        }
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

}
