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
        model.addAttribute("vista",     "usuarios");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active",    "usuarios");
        return "layouts/main";
    }

    // ── POST /usuarios/crear — solo ADMIN ─────────────────────────────────────

    @PostMapping("/usuarios/crear")
    public String crear(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) String nombreGimnasio,
            RedirectAttributes redirectAttributes) {

        // Validación de contraseña
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

        // Crear los métodos de pago por defecto para este usuario
        crearMetodosIniciales(nuevo);

        redirectAttributes.addFlashAttribute("success",
                "Usuario '" + username + "' creado correctamente.");
        return "redirect:/usuarios";
    }

    // ── POST /usuarios/eliminar/{id} — solo ADMIN ─────────────────────────────

    @PostMapping("/usuarios/eliminar/{id}")
    public String eliminar(@PathVariable Integer id,
                           RedirectAttributes redirectAttributes) {
        Usuario actual = securityUtils.getUsuarioActual();

        // No se puede borrar a uno mismo
        if (actual.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "No podés eliminar tu propio usuario.");
            return "redirect:/usuarios";
        }

        usuarioRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Usuario eliminado.");
        return "redirect:/usuarios";
    }

    // ── GET /perfil — cualquier usuario logueado ──────────────────────────────

    @GetMapping("/perfil")
    public String perfil(Model model) {
        model.addAttribute("usuario",   securityUtils.getUsuarioActual());
        model.addAttribute("title",     "Gym Manager | Mi Perfil");
        model.addAttribute("header",    "Mi Perfil");
        model.addAttribute("vista",     "perfil");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active",    "perfil");
        return "layouts/main";
    }

    // ── POST /perfil/cambiar-password — cualquier usuario logueado ────────────

    @PostMapping("/perfil/cambiar-password")
    public String cambiarPassword(
            @RequestParam String passwordActual,
            @RequestParam String passwordNueva,
            @RequestParam String confirmar,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = securityUtils.getUsuarioActual();

        // Verificar que la contraseña actual es correcta
        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "La contraseña actual es incorrecta.");
            return "redirect:/perfil";
        }

        // Validar nueva contraseña
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

    // ── POST /perfil/actualizar — cambiar nombre del gimnasio ─────────────────

    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(
            @RequestParam(required = false) String nombreGimnasio,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = securityUtils.getUsuarioActual();
        usuario.setNombreGimnasio(nombreGimnasio);
        usuarioRepository.save(usuario);

        redirectAttributes.addFlashAttribute("success", "Perfil actualizado.");
        return "redirect:/perfil";
    }

    // ── Validación de contraseña ──────────────────────────────────────────────

    private String validarPassword(String password, String confirmar) {
        if (password == null || password.length() < 8)
            return "La contraseña debe tener al menos 8 caracteres.";
        if (!password.equals(confirmar))
            return "Las contraseñas no coinciden.";
        return null;
    }

    // ── Métodos de pago por defecto para cada usuario nuevo ──────────────────

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