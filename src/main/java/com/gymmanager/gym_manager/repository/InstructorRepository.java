package com.gymmanager.gym_manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gymmanager.gym_manager.entity.Instructor;
import com.gymmanager.gym_manager.entity.Usuario;

public interface InstructorRepository extends JpaRepository<Instructor, Integer> {

    List<Instructor> findByUsuario(Usuario usuario);

    @Query("""
        SELECT i FROM Instructor i
        WHERE i.usuario = :usuario
          AND (:texto = ''
               OR LOWER(i.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(i.apellido) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(i.dni) LIKE LOWER(CONCAT('%', :texto, '%')))
        """)
    Page<Instructor> buscarPagina(@Param("usuario") Usuario usuario,
                                  @Param("texto") String texto,
                                  Pageable pageable);
    boolean existsByDniAndUsuario(String dni, Usuario usuario);

    Optional<Instructor> findByIdInstructorAndUsuario(
            Integer idInstructor,
            Usuario usuario
    );
}
