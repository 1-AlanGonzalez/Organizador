package com.gymmanager.gym_manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.Usuario;

public interface ActividadRepository extends JpaRepository<Actividad, Integer> {

    List<Actividad> findByUsuario(Usuario usuario);

    @Query("""
        SELECT a FROM Actividad a
        WHERE a.usuario = :usuario
          AND (:texto = '' OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :texto, '%')))
        """)
    Page<Actividad> buscarPagina(@Param("usuario") Usuario usuario,
                                 @Param("texto") String texto,
                                 Pageable pageable);

    Optional<Actividad> findByIdActividadAndUsuario(Integer id, Usuario usuario);
}
